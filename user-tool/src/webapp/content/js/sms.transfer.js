var smsTransfer = function($){

    var     messgeBundlePath = "/direct/sms-resources/message-bundle.json",
            log = function(string){
                 _alert(string,'l');
            },
            info = function(string){
                 _alert(string,'i');
            },
            warn = function(string){
                 _alert(string,'w');
            },
            error = function(string){
                 _alert(string,'e');
            },
            messageBundle = {},
            //retrieve the message strings for key
            messageLocator = function(key, params){
                return fluid.messageLocator( messageBundle )([key], params);
            },
            //images
            images = {
                src: {
                    loadingImage: "/library/image/sakai/spinner.gif"
                },
                dom: {
                    loadingImage: '<img class="img-loading" src="/library/image/sakai/spinner.gif" alt="Working..." title="Working..."/>'
                }
            },

            //Object definitions
            account = {
                id: null,
                credits: null,
                siteId: null,
                siteName: null,
                transferAmount: null
            },
            accounts = [],             //holds: account objects
            selectedFromAccount = [],    //holds: accountId
            selectedToAccounts = [],    //holds: [accountId,transferAmount, ebEntities]
            selectedAccountsEntities = [],    //holds: strings
            siteIdEntities = [],         //holds: strings
            creditsTotalToTransfer = 0,
            selectedFromAccountBalance = 0,

            _getAccounts = function(){
                $.getJSON(
                        "/direct/sms-account.json",
                        function(rawAcounts){
                            accounts = [];
                            for(var i in rawAcounts['sms-account_collection']){
                                var rawAccount = rawAcounts['sms-account_collection'][i],
                                account = {
                                    id: null,
                                    credits: null,
                                    siteId: null,
                                    siteName: null,
                                    transferAmount: null
                                };
                                account.id = rawAccount.id;
                                account.credits = _formatCredits(rawAccount.credits);
                                account.siteId = rawAccount.sakaiSiteId;
                                account.transferAmount = 0;
                                account.siteName = null;
                                siteIdEntities.push("/direct/site/"+account.siteId+".json");
                                accounts.push(account);
                            }
                            if(accounts.length > 0){
                                _getSites();
                            }else{
                                error(messageLocator("ui.transfer.js.serverNoAccounts"));
                            }
                        });
            },
            _getSites = function(){
                if(accounts.length > 0 && siteIdEntities.length > 0){
                    var siteIdEntitiesBatched = [], batchLimit = 30,   //to contain 2D array of grouped EB paths
                    numGroups = Math.ceil(siteIdEntities.length/batchLimit);
                    // populate sites in batches of 30 due to limitation in SMS-230 comment
                    for(var i = 0; i <numGroups; i ++){
                        siteIdEntitiesBatched.push(siteIdEntities.slice(i*batchLimit,(i*batchLimit)+batchLimit));
                    }

                    for(var batch in siteIdEntitiesBatched){
                        $.ajax({
                            url: "/direct/batch?_refs=" + siteIdEntitiesBatched[batch].toString(),
                            complete: _processRawSite,
                            success: function(){$("#loading").show();},
                            error: function(){$("#loading").show();},
                            global: false
                        });
                    }
                   $("#loading").show();
                   setTimeout(_showUI, 2000*siteIdEntitiesBatched.length);  //give each response 1 second to fetch from server
                }
            },
            _processRawSite = function(rawSites, callStatus){
                var rawSitesJSON = null;
                try{
                    rawSitesJSON = JSON.parse(rawSites.responseText.toString());
                }catch(err){}
                for(var n in rawSitesJSON){
                    var rawSite = rawSitesJSON[n];
                    var status = rawSite.status;
                    if( status === 200 || status === 201 || status === 204 ){
                      for(var i in accounts){
                          if(accounts[i].siteId === rawSite.data.id){
                              accounts[i].siteName = rawSite.data.title;
                              break;
                          }
                      }
                    }
                 }
            },
            _showUI = function(){
                //remove null sites
                for(var acc in accounts){
                    if(accounts[acc].siteName === null || accounts[acc].siteName.length === 0){
                        accounts.splice(acc,1);
                    }
                }

                //sort
                accounts.sort(_sortGroupsComparator('siteName', false, function(a){
                    if(a !== null){
                        return a.toUpperCase();
                    }
                }));

                //Show UI
                $("#account-select").html("");
                //render table
                var table =  $("#accounts-table"),
                    row = table.find("tr[name=acc-row]");

                 //remove current dom row
                 row.remove();
                $("#account-select").append('<option value="null" name="choose">--- Choose an account ---</option>');

                for(var s in accounts){
                    var a = accounts[s];
                    //render dropdown
                    $("#account-select").append('<option value="'+a.id+'" name="'+a.credits+'">'+a.siteName+'</option>');

                    var rowTemp = row.clone();
                    rowTemp.attr("id", "acc:" + a.id);
                    rowTemp.find("td[name=acc-id]").text(a.id);
                    rowTemp.find("td[name=acc-site-name]").text(a.siteName);
                    rowTemp.find("input[name=acc-id]").val(a.id);
                    rowTemp.find("td[name=acc-credits]").text(a.credits);
                    rowTemp.find("td[name=acc-credits]").attr("id", "acc-credits-for:" + a.id);
                    rowTemp.find("td input[id^=\"acc-transfer-amount-for:\"]").attr("id", "acc-transfer-amount-for:" + a.id);
                    //add to dom
                    table.find("tbody").append(rowTemp);
                }

                $("input[id^=\"acc-transfer-amount-for:\"]").val('');

                if(accounts.length === 1){
                    $("#loading").hide();
                    $("#accounts-body").show();
                    $("div.act").hide();
                    $("#account-from").hide();
                    $("input[id^=\"acc-transfer-amount-for:\"]").attr("disabled", "disabled");
                    error(messageLocator("ui.transfer.js.serverOneAccount"));
                    return false;    //exit
                }

                $("#account-select").change(function(){
                    var thisAccountId = this.options[this.selectedIndex].value;
                    $("tr[id^=\"acc:\"]:hidden").fadeIn(0);
                    $("#alert").hide();
                    $("#account-balance-parent").removeAttr("class");
                    if(thisAccountId !== "null"){
                        selectedFromAccount = thisAccountId;
                        selectedFromAccountBalance  = this.options[this.selectedIndex].getAttribute("name");
                        if(selectedFromAccountBalance > 0){
                            $("input[id^=\"acc-transfer-amount-for:\"]").removeAttr("disabled");
                            $("tr[id=\"acc:"+thisAccountId+"\"]").fadeOut(0);
                            $("tr[id=\"acc:"+thisAccountId+"\"] input[type=text]").val("");
                            $("#account-balance").text(selectedFromAccountBalance);
                            $("#account-balance-parent").fadeIn("fast");
                        }else{
                            $("#account-balance").text(selectedFromAccountBalance);
                            $("#account-balance-parent")
                                    .fadeIn("fast")
                                    .addClass("messageValidation");
                        }
                    }else{
                        $("#account-balance-parent").fadeOut("fast");
                    }
                });

                //Bind event for live validation of transfer amount.
                //check input boxes for non-numbers
                $("td input[id^=\"acc-transfer-amount-for:\"]").bind("keyup", function(){
                    selectedFromAccountBalance = $("#account-select option:selected").attr("name");
                    if(selectedFromAccountBalance === "choose"){
                        warn(messageLocator("ui.transfer.js.chooseFromAccount"));
                        return false;
                    }
                    if(isNaN(this.value)){
                        this.value = this.value.substring(0, this.value.length-1);
                        return false;
                    }else{
                        var totalUserEntered = _getTotalUserEntered(),
                        validTotal =  totalUserEntered*1 - this.value*1;

                        if(totalUserEntered > selectedFromAccountBalance){
                            //this.value = selectedFromAccountBalance*1 - validTotal;
                        }
                    }
                });

                //Bind event for clear and submit
                $("#doAction").bind("click", function(){
                    var totalToTransfer = 0;
                    //prepare fromAccount
                    selectedFromAccountBalance = $("#account-select option:selected").attr("name");
                    selectedFromAccount = $("#account-select option:selected").val();
                    if(selectedFromAccount === "null"){
                        warn(messageLocator("ui.transfer.js.chooseFromAccount"));
                        return false;
                    }
                    _startWork();
                    selectedToAccounts = [];
                    selectedAccountsEntities = [];
                    //prepare entered amounts
                    $("input[name=amount]").each(function(){
                        if(this.value > 0){
                            var toAcc = this.id.replace("acc-transfer-amount-for:","");
                            selectedToAccounts.push([toAcc, this.value, "/direct/sms-account/transfer?fromAccount=" +
                                    selectedFromAccount + "&toAccount=" + toAcc + "&credits=" + this.value]);
                            totalToTransfer = totalToTransfer*1 + this.value*1;
                            selectedAccountsEntities.push("/direct/sms-account/transfer?fromAccount%3D" +
                                    selectedFromAccount + "%26toAccount%3D" + toAcc + "%26credits%3D" + this.value);
                            creditsTotalToTransfer = totalToTransfer;
                        }
                    });
                    if(selectedAccountsEntities.length > 0){
                        //check suffiecient funds
                        if (totalToTransfer > selectedFromAccountBalance){
                            warn(messageLocator("ui.transfer.js.insufficientFunds", [totalToTransfer*1 - selectedFromAccountBalance*1]));
                        }else{
                            _doTransfer();
                        }
                    }else{
                        warn(messageLocator("ui.transfer.js.noTransferAmount"));
                    }
                });

                $("#doClear").bind("click", function(){
                    $("input[name=amount]").val("");
                    $("#alert").slideUp();
                    return false;
                });

                $("#loading").hide();
                $("#accounts-body").show();
            },
            _doTransfer = function(){
                $.ajax({
                        url: "/direct/batch.json?_refs=" + selectedAccountsEntities.toString(),
                        global: false,
                        type: "post",
                        processData: true,
                        complete: _processServerResponse,
                        error: function(){}
                });
            },

            _processServerResponse = function(serverResponses, callStatus){
                var serverResponsesJSON = null;
                try{
                    serverResponsesJSON = JSON.parse(serverResponses.responseText.toString());
                }catch(err){
                    error("Oops, the server could not process your action due to this error: " + serverResponses.statusText + " (" + serverResponses.status + ").");
                    _stopWork();
                    return false;
                }
                if( serverResponsesJSON !== null ){
                    var failedTransfers = 0,
                        errors = [],
                        successes = [];
                    $.each(serverResponsesJSON, function(i, accountREF){
                        var status;
                        for(var a in selectedToAccounts){
                            if(selectedToAccounts[a][2] === accountREF.entityURL){
                                status = accountREF.status;
                                if( status === 200 || status === 201 || status === 204 ){
                                    //update this to account
                                    var creditsCell = $("td[id=\"acc-credits-for:"+ selectedToAccounts[a][0] +"\"]"),
                                        newCredits = creditsCell.text() *1 + selectedToAccounts[a][1]*1;
                                    $("input[id=\"acc-transfer-amount-for:"+ selectedToAccounts[a][0] +"\"]").val('');
                                    creditsCell.text(newCredits);
                                    $("#account-select option[value="+selectedToAccounts[a][0]+"]").attr("name", newCredits);
                                    //log("saved account: " + selectedToAccounts[a]);
                                    successes.push(selectedToAccounts[a]);
                                } else{
                                    failedTransfers = failedTransfers + selectedToAccounts[a][1]*1;
                                    //warn("problem saving account: " + selectedToAccounts[a]);
                                    errors.push(selectedToAccounts[a][0]);
                                }
                            }
                        }
                    });
                    //update selected account balance
                    var selectedCell = $("td[id=\"acc-credits-for:"+ selectedFromAccount[0] +"\"]"),
                        newSelectedCredits = selectedCell.text() *1 - (creditsTotalToTransfer *1 - failedTransfers *1);
                    $.getJSON(
                            "/direct/sms-account/"+selectedFromAccount[0]+".json?_id=" + new Date().getTime(),
                            function(acc){
                                newSelectedCredits = _formatCredits(acc.credits);
                            });
                    selectedCell.text(newSelectedCredits);
                    $("#account-balance").text(newSelectedCredits);
                    if(newSelectedCredits === 0){
                        $("#account-balance-parent").addClass("messageValidation");
                    }else{
                        $("#account-balance-parent").removeAttr("class");
                    }
                    $("#account-select option[value="+selectedFromAccount[0]+"]").attr("name", newSelectedCredits);

                    //Handle alerts
                    if(errors.length > 0 && successes.length === 0){
                        /*No successes, only errors*/
                        //highlight rows affected
                        var errorSites = [];
                        for(var e in errors){
                            _highlight($("tr[id=\"acc:"+errors[e]+"\"]"));
                            for(var a in accounts){
                                if(errors[e]*1 === accounts[a].id*1){
                                    errorSites.push(accounts[a].siteName);
                                    break;
                                }
                            }
                        }
                        //show only error message based on how many errors ie plural etc
                        error(messageLocator("ui.transfer.js.serverAccountError", [errorSites.join(" and ")]));
                    }else if(errors.length > 0){
                        /*some successes & errors*/
                        //highlight rows affected by error
                        var errorSites = [],
                            msg = "",
                            tempMsg = "";
                        for(var e in errors){
                            _highlight($("tr[id=\"acc:"+errors[e]+"\"]"));
                            for(var a in accounts){
                                if(errors[e]*1 === accounts[a].id*1){
                                    errorSites.push(accounts[a].siteName);
                                    break;
                                }
                            }
                        }
                        msg = '<div class="messageError"><span>'+messageLocator("ui.transfer.js.serverAccountError", [errorSites.join(" and ")])+'</span></div>';
                        for(var s in successes){
                            for(var b in accounts){
                                if(successes[s][0]*1 === accounts[b].id*1){
                                    tempMsg += messageLocator("ui.transfer.js.transfer", [successes[s][1], accounts[b].siteName]);
                                    break;
                                }
                            }
                        }
                        //show both message types based on how many types ie plural etc
                        //use warn style, not alert or success
                        info(msg+'<div class="messageSuccess"><span>'+tempMsg+'</span></div><p class="closeMe" />');
                        //****CATASTROPHIC ERROR ****//
                        //This app has info inconsistant with the server. probably an account was deleted or edited shomehow. we need to refetch data
                        alert(messageLocator("ui.transfer.js.serverNeedSync"));
                        location.reload(true);
                    }else if(errors.length === 0 && successes.length > 0){
                        /*No errors, only successes*/
                        var msg = "";
                        for(var s in successes){
                            for(var b in accounts){
                                if(successes[s][0]*1 === accounts[b].id*1){
                                    msg += messageLocator("ui.transfer.js.transfer", [successes[s][1], accounts[b].siteName]);
                                    break;
                                }
                            }
                        }
                        //show only successes message based on how many successes ie plural etc
                        log(msg);
                    }
                }
                _stopWork();
                },

            //Any field type comparator. Use like this:
            //       Sort by price high to low
            //          homes.sort(sort_by('price', true, parseInt));
            //      Sort by city, case-insensitive, A-Z
            //          homes.sort(sort_by('city', false, function(a){return a.toUpperCase()}));

            _sortGroupsComparator = function(field, reverse, primer) {
                reverse = (reverse) ? -1 : 1;
                return function(a, b) {
                    a = a[field];
                    b = b[field];
                    if (typeof(primer) !== 'undefined' || primer !== null) {
                        a = primer(a);
                        b = primer(b);
                    }
                    if (a < b) return reverse * -1;
                    if (a > b) return reverse * 1;
                    return 0;
                };
            },

            _getTotalUserEntered = function(){
                var total = 0;
                $("input[id^=\"acc-transfer-amount-for:\"]").each(function(i, t){
                    total = total*1 + this.value*1;
                });
                return total;
            },

            _startWork = function(){
                $("#alert").hide();
                $(".img-loading").show();
                $("input[type=button]").attr("disabled", "disabled");
            },
            _stopWork = function(){
                $("#loading").hide();
                $(".img-loading").hide();
                $("input[type=button]").removeAttr("disabled");
            },

            _alert = function(string,type){
                var alert = $("#alert");
                if(type === 'e'){
                    alert.attr("class", "messageError");
                }else if(type === 'l'){
                    alert.attr("class", "messageSuccess");
                }else if(type === 'i'){
                    alert.removeAttr("class");
                }else if(type === 'w'){
                    alert.attr("class", "messageValidation");
                }
                alert.find("span.alertText").html(string);
                alert.show();
                _stopWork();
            },
            _highlight = function(t){
                return t.effect('highlight', {color: 'red'}, 2500);
            },
        /**
         * TRUNCATE to one decimal place if need be, else credit is an int
         * @param credits Credits to truncate
         */
            _formatCredits = function(credits){
                var c = credits.toString(), //change to string to that .split() can work
                finalCredits = 0;
                if(c.split('.')[1]*1 > 0){
                    finalCredits = (c.split('.')[0] + '.' + c.split('.')[1].charAt(0))*1;
                }else{
                    finalCredits = c.split('.')[0]*1;
                }
                return finalCredits;
            },
            // Format bundle path for user locale
            _loadMessageBundle = function(){
                $.ajax({
                    url: messgeBundlePath,
                    global: false,
                    cache: true,
                    dataType : "json",
                    success: function(messageBundleJSON){
                        messageBundle = messageBundleJSON.data;
                    }
                });
            },
            _init = function(){
            //preload icons. Dimensions are not important
            var preload = new Image();
            for (var key in images.src) {
               preload.src = images.src[key];
            }
            //ajax globals
            $.ajaxSetup({
                beforeSend: function(){
                    _startWork();
                },
                complete: function(){
                    _stopWork();
                },
                error: function(xhr) {
                    alert(messageLocator('GeneralAjaxChannelError', [xhr.statusText, xhr.status]));
                    return false;
                }
            });

            // Message Locale bundle loader
            _loadMessageBundle();

            _getAccounts();

            $("#doBack").click(function(){
                history.go(-1);
            });
        };

    return {
        init: _init
    };
}(jQuery);


//acivate
$(document).ready(function() {
    smsTransfer.init();
});
