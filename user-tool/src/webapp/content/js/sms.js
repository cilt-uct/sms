/**
 * JS library for the SMS User Tool's send SMS view.
 * @author lovemore.nalube@uct.ac.za
 **/
(function($) {
    // Define class
    $.fn.SMS = function(options) {
        // build main options before class instantiation
        $.extend({}, $.fn.SMS.defaults, options);
    };
    // define and expose our format function
    $.fn.SMS.get = {
        otherthing: 'yes',
        preserveDomSelections: false,
        preserveNewDomSelections: false,
        selectionsHaveChanged: false,

        previousSelectionsRoles: [],
        previousSelectionsNumbers: [],
        previousSelectionsGroups: [],
        previousSelectionsNames: [],

        people: function() {
            renderPeople();
        },

        peopleByName: function() {
            getPeople();
        },
        getSelectedRecipientsListNames : function() {
            getSelectedRecipientsList.length('names');
        },
        getSelectedRecipientsListRoles : function() {
          return selectedRecipientsList;
          //  getSelectedRecipientsList.length('names');
        },
        getSelectedRecipientsListIDs : function(filter) {
            var type = getSelectedRecipientsList.array(filter);
            var tempIDs = [];
            if (filter === "numbers") {
                for (var i = 0; i < type.length; i++) {
                    tempIDs.push(type[i]);
                }
            } else {
                for (var n = 0; n < type.length; n++) {
                    tempIDs.push(type[n][0]);
                }
            }
            return tempIDs;
        },
        isSelectionMade: function() {
            var bool = false;
            bool = (getSelectedRecipientsList.length("roles") > 0 || getSelectedRecipientsList.length("groups") > 0 || getSelectedRecipientsList.length("names") > 0 || getSelectedRecipientsList.length("numbers") > 0 || $("#copy-me:checked").length !== 0 );
            return bool;
        },
        errorEb: null,
        isIndividualsLoaded: false
    };
    $.fn.SMS.set = {
        init: function() {
            selectedRecipientsList = { //Reset the Selected Recipients
                roles:[],
                groups: [],
                names: [],
                numbers: []};
        },
        processCalculate: function(domElements, _this) {
            $('#checkNumbers').click(); // Fire numbers check function
            if ($.fn.SMS.get.isSelectionMade()) {
                _this.disabled = true;
                $("#facebox .loadingImage").show();
                $("#sakaiUserIds").val($.fn.SMS.get.getSelectedRecipientsListIDs("names").toString());
                var entityList = [];
                if ($.fn.SMS.get.getSelectedRecipientsListIDs("groups").length > 0) {
                    entityList.push($.fn.SMS.get.getSelectedRecipientsListIDs("groups"));
                }
                if ($.fn.SMS.get.getSelectedRecipientsListIDs("roles").length > 0) {
                    entityList.push($.fn.SMS.get.getSelectedRecipientsListIDs("roles"));
                }
                $("#deliveryEntityList").val(entityList.toString() === "," ? null : entityList.toString()); //set deliveryEntityList to null if no groups or roles are selected.
                $("#deliveryMobileNumbersSet").val($.fn.SMS.get.getSelectedRecipientsListIDs("numbers").toString());
                $.ajax({
                    url: "/direct/sms-task/calculate",
                    type: "POST",
                    dataType: "json",
                    data: smsParams(domElements),
                    beforeSend: function() {
                        $("div[id^=errorFacebox]").slideUp('fast');
                        $("#cReportConsole").slideUp('fast');
                    },
                    success: function(json) {
                        _this.disabled = false;
                        $("#recipientsCmd")
                                .removeAttr("disabled")
                                .addClass("active");
	                    var cSelected = json.groupSizeEstimate,
	                    cCredits = json.creditEstimate,
	                    cCost = json.costEstimate;
	                    $("#cReportConsole .console-selected").text(cSelected);
	                    $("#cReportConsole .console-credits").text(cCredits);
                        //make sure there are 2 decimal points in the cost
                        var cost = String(cCost);
                        if(cost.length - ( cost.indexOf(".") + 1) > 1){
                        	cost = String(Math.round(cost*100)/100);
                    	}
                        if ( cost.search(/\./) === -1 ){
                           cost = ( cost + ".00" );
                        }else if ( cost.length - ( cost.indexOf(".") + 1) === 1){
                            cost = ( cost + "0" );
                        }
	                    $("#cReportConsole .console-cost").text($("#currency").val() + cost);
                        $("#cReportConsole").slideDown('fast', function() {
                            $(this).effect('highlight', 'fast');
                            $.fn.SMS.set.frameGrow($("#cReportConsole").height(), "grow");
                        });
                        if( entityList.length > 0 ){
                            //Show the may change alert message since we have selected at least one group/role
                            $("#cReportConsole .mayChange").show();
                        }else{
                            $("#cReportConsole .mayChange").hide();
                        }
                        $("#facebox .loadingImage").hide();
                        return false;
                    },
                    error: function(xhr) {
                        console.log(xhr);
                        smsUtils.error.server(xhr, $(_this), "errorFacebox");
                        $("#recipientsCmd")
                                .attr("disabled", "disabled")
                                .removeClass("active");
                    }
                });
            } else {
                smsUtils.error.dom($("#errorNoSelections").text());
                $("#cReportConsole").slideUp('fast');
            }

        },
        processSubmitTask: function(domElements, _this) {
            $("div[id^=error]").fadeOut("fast");
            $("h4.expiry").removeClass("smsAlert");
            if ( validateDates() ){
            _this.disabled = true;
            var _url = "";
            if ($("#statusType").val() !== null && $("#statusType").val() === "EDIT") {
                _url = "/direct/sms-task/" + $("#smsId").val() + "/edit";
            } else {
                _url = "/direct/sms-task/new";
            }
            $.ajax({
                url: _url,
                type: "POST",
                data: smsParams(domElements),
                beforeSend: function() {
                    $(".loadingImage").show();
                },
                error: function(xhr) {
                    smsUtils.error.server(xhr, $(_this), "error");
                },
                success: function(_id) {
                    $("div[id^=error]").slideUp('fast');
                    window.location.href = $("#goto-home").attr('href') + "?id=" + _id + "&status=" + Number(new Date());
                    $(".loadingImage").hide();
                    return true;
                }
            });
            }else{
                _this.disabled = false;
                //warn the user and reset the expiry date
                $("#errorDateValidation").fadeIn("fast");
                $("h4.exipry").addClass("smsAlert");
                $("[id=smsDatesExpiryDate:1:date-field]").focus();
                $(".loadingImage").hide();
                return false;
            }
        }   ,
        setSubmitTaskButton: function() {
            if ($.fn.SMS.get.preserveNewDomSelections) {
                $.fn.SMS.set.restoreSelections();
            }
            var isRecipientsChosen = ($.fn.SMS.get.isSelectionMade() || $("input[id^=task]").length > 0);
            /*error = "Before you can send: ";

            if( !smsUtils.error.isMessageLengthValid){
               error += " Reduce the length of your message.";
                $("#smsBoxCounter").attr("title", error);
            }else{
                $("#smsBoxCounter").removeAttr("title");
            }
            if( !isRecipientsChosen){
               error += " Choose at least one recipient.";
            }*/

            if ( isRecipientsChosen && smsUtils.error.isMessageLengthValid ) {
                $("#smsSend")
                        .removeAttr("disabled")
                        .addClass("active");
                //error = "";
                //$("#errorNoSelections").hide();
            } else {
                $("#smsSend")
                        .attr("disabled", "disabled")
                        .removeClass("active");
            }
            //$("#errorNoSelections").text(error).show();
        },
        addSelectedRecipientsListName: function(array) {
            selectedRecipientsList.names.push([array[1],array[0]]);
            return true;
        },
        addSelectedRecipientsListByType: function(_type, recipientString) {
          if (selectedRecipientsList[_type] && Array.isArray(selectedRecipientsList[_type])) {
            selectedRecipientsList[_type].push(recipientString);
          }
        },
        restoreTaskSelections: function() {
            $.fn.SMS.set.init();
            var entities = ( $("#savedEntityList").length !== 0 && $("#savedEntityList").val() !== null ) ? $("#savedEntityList").val().toString().split(',') : null;
            var names = ( $("#savedUserIds").length !== 0 && $("#savedUserIds").val() !== null ) ? $("#savedUserIds").val().toString().split(',') : null;
            var numbers = ( $("#savedDeliveryMobileNumbersSet").length !== 0 && $("#savedDeliveryMobileNumbersSet").val() !== null ) ? $("#savedDeliveryMobileNumbersSet").val().toString().split(',') : null;
            $.fn.SMS.set.savedTaskEntities(entities, names, numbers);
        },
        restoreSelections: function() {
            $.fn.SMS.set.init();
            $.fn.SMS.set.savedTaskEntities(
                    $.fn.SMS.get.previousSelectionsRoles.concat($.fn.SMS.get.previousSelectionsGroups),
                    $.fn.SMS.get.previousSelectionsNames,
                    $.fn.SMS.get.previousSelectionsNumbers);
            $.fn.SMS.get.preserveNewDomSelections = false;
        },
        sliceSelectedRecipientsListName: function(id) {
            $.each(selectedRecipientsList.names, function(i, parent) {
                if (parent) {
                    if (parent[0] === id) {
                        selectedRecipientsList.names.splice(Number(i), 1);
                    }
                }
            });
        },
        savedTaskEntities: function(entities, userIds, numbers) {
            if ($("#id").length !== 0 && $("#id").val() !== "") {
                $("#cReportConsole").show();
            }
            //Re-select saved Entity selections   ie: roles and groups
            if (entities !== null && entities.length !== 0) {
                //Render saved entity selections
                $.each(entities, function(i, entity) {
                    if (entity !== "" && entity !== null) {
                        var elem = 'input[type=checkbox][value=' + entity + ']';
                        $(elem).each(function() {
                            this.checked = true;
                            checkEntityboxAction(this, entity.split("/")[3] + "s");
                        });
                    }
                });
            }

            //Re-select saved users selections ie: individuals
            if (userIds !== null && userIds.length !== 0) {
                //Render saved entity selections
                if (var_getEveryoneInSite_participants.length > 16) {
                   //Autocomplete is activated now populate saved names
                    $.each(userIds, function(i, entityId) {
                        if (entityId !== "" && entityId !== null) {
                            $.each(var_getEveryoneInSite_participants, function(n, userArray) {
                                if( userArray[1] === entityId ){
                                    var v = '<li class="acfb-data" personName="' + userArray[0] + '" personId="' + userArray[1] + '"><span>' + userArray[0] + '</span> <img class="p" src="' + $.fn.SMS.settings.images.deleteAutocompleteImage + '"/></li>';
                                    //add this user to the selected list
                                    selectedRecipientsList.names.push([userArray[1], userArray[0]]);
                                    $(v).insertBefore("#peopleListNamesSuggest input.acfb-input");
                                    $('#peopleTabsNames span[rel=recipientsSum]').fadeIn().text(getSelectedRecipientsList.length('names'));
                                }
                            });
                        }
                    });
                    //bind delete images
                    $(".acfb-data img.p").click(function() {
                        $.fn.SMS.set.sliceSelectedRecipientsListName($(this).parent().attr("personId"));
                        $(this).parent().fadeOut(function() {
                            $(this).remove();
                        });
                        //Refresh {peopleTabsNames} Number on TAB
                        if (getSelectedRecipientsList.length('names') > 0){
                            $('#peopleTabsNames span[rel=recipientsSum]').text(getSelectedRecipientsList.length('names'));
                        }
                        else{
                            $('#peopleTabsNames span[rel=recipientsSum]').fadeOut();
                        }
                         $.fn.SMS.set.disableContinue();
                    });


                }else if (var_getEveryoneInSite_participants.length > 0) {
                    $.each(userIds, function(i, entityId) {
                        if (entityId !== "" && entityId !== null) {
                            $('input[type=checkbox][value=' + entityId + ']').each(function() {
                                this.checked = true;
                                checkNameboxAction(this);
                            });
                        }
                    });
                }
            }

            if (numbers !== null && numbers.length !== 0) {
                //Render saved numbers
                if (numbers !== null && numbers.length > 0) {
                    $("#peopleListNumbersBox").text(numbers.join('\n'));
                    $('#checkNumbers').click();
                }
            }
        },
        setEntityBoxAction: function(el, elType) {
           checkEntityboxAction(el, elType);
        },
        frameGrow: function(height, updown) {
        var _height = height === "" ? 280 : Number(height) + 40;
        var frame = (parent ? parent.document.getElementById(window.name) : null);
        try {
            if (frame) {
                var clientH = '';
                if (updown === 'shrink') {
                    clientH = document.body.clientHeight - _height;
                }
                else {
                    clientH = document.body.clientHeight + _height;
                }
                $(frame).height(clientH);
            }
        } catch(e) {
        }
    },
        removeRecipientByTypeAndIndex: function(_type, index) {
          try {
            if (selectedRecipientsList[_type] && Array.isArray(selectedRecipientsList[_type]) && index < selectedRecipientsList[_type].length) {
              selectedRecipientsList[_type].splice(index, 1);
            }
          } catch(e) {
          }
        },
        removeRecipientFromRolesByIndex: function(i) {
          try {
             selectedRecipientsList.roles.splice(i, 1);
          } catch(e) {
          }
        },
        removeRecipientFromGroupsByIndex: function(i) {
          try {
             selectedRecipientsList.groups.splice(i, 1);
          } catch(e) {
          }
        },
        disableTab: function(tabName, tabErrorElement){
            var err = $('#'+ tabErrorElement ).text() === "" ? $('#'+ tabErrorElement ).val() : $('#'+ tabErrorElement ).text();
            $('#' + tabName + ' a')
         //           .css($.fn.SMS.settings.css.tabDisabled)
                    .attr('title', err);
  /*          $('#' + tabName + ' a')
			 		.unbind('click')
                    .bind('click', function(){
                return false;
            });*/
        },
        disableContinue: function(){
            //Disable the continue button and force user to calculate before saving any new changes
            $("#recipientsCmd")
                    .attr("disabled", "disabled")
                    .removeClass("active");
        },
        displaySelectedNumbersInDOM: function() {
          showSelectedNumbersInDOM();
        },
        isRecipientsLinkClicked: false
    };

    // SMS class defaults
    $.fn.SMS.settings = {
        //icon locations
        images: {
            base: '/library/image/silk/',
            busy: 'spinner.gif',
            deleteAutocompleteImage: '/sms-user-tool/content/images/delete.gif',
            status: {
                completed: ['tick.png', 'Completed'],
                failed: ['cancel.png','Failed'],
                scheduled: ['time.png','Scheduled'],
                progress: ['bullet_go.png','Progress'],
                edit: ['page_white_edit.png','Edit']
            }
        },
        css: {
            tabDisabled: {
                'background-color':'#999999',
                'background-image': 'none',
                color: '#666666'
            },
            tabBusy: {
                'background-color':'#fff',
                color: '#000',
                'background-image':'url(/library/image/sakai/spinner.gif)',
                'background-position':'0 50%',
                'background-repeat':'no-repeat'
            }
        }
    };
    // end of public methods

    /**
     * Private variables
     */

    var var_getEveryoneInSite,     // to hold full people list
    var_getEveryoneInSite_participants = [],     // to hold full participants list
    selectedRecipientsList = { //Object with multidimetional Dimensional Arrays to hold the Selected Recipients
        roles:[],
        groups: [],
        names: [],
        numbers: []
    };


    function getEveryoneInSite(filter) {
        if (filter && filter === "names"){
            return var_getEveryoneInSite_participants;
        }
        else{
            return var_getEveryoneInSite;
        }
    }

    var getSelectedRecipientsList = {
        length: function(filter) {
            switch (filter) {
                case "roles":
                    return selectedRecipientsList.roles.length;
                case "groups":
                    return selectedRecipientsList.groups.length;
                case "names":
                    return selectedRecipientsList.names.length;
                case "numbers":
                    return selectedRecipientsList.numbers.length;
                case "all":
                    return selectedRecipientsList.length;
            }
        },
        array: function(filter) {
            switch (filter) {
                case "roles":
                    return selectedRecipientsList.roles;
                case "groups":
                    return selectedRecipientsList.groups;
                case "names":
                    return selectedRecipientsList.names;
                case "numbers":
                    return selectedRecipientsList.numbers;
                case "all":
                    return selectedRecipientsList;
            }
        }
    };
    /**
     * Getter for individuals tab
     */
    function getPeople() {
        var disableTab = function(){
            tabFinishedLoading();
            $.fn.SMS.set.disableTab('peopleTabsNames', 'errorEb');
        },
        tabLoading = function(){
            $('#peopleTabsNames a[name=loading]')
                    .unbind("click")
                    .bind("click", function(){ return false; })
                    .css($.fn.SMS.settings.css.tabBusy);
        },
        tabFinishedLoading = function(){
            $('#peopleTabsNames a[name=loading]').hide();
            $('#peopleTabsNames a').not("[name=loading]").show();
        },
        initTab = function(list) {
                    $.fn.SMS.get.isIndividualsLoaded = true;
                    //Initialise the Individuals Tab
                    if (list.length > 16) {
                        tabFinishedLoading();
                        // Create autocomplete object
                        $("#peopleListNamesSuggest").autoCompletefb({
                                urlLookup  : list,
                                acOptions  : {
                                    minChars: 1,
                                    matchContains:  true,
                                    selectFirst:    false,
                                    width:  300,
                                    formatItem: function(row) {
                                        return row[0] + ' (' + row[2] + ')';
                                    }
                                },
                                foundClass : ".acfb-data",
                                inputClass : ".acfb-input",
                                deleteImage: $.fn.SMS.settings.images.deleteAutocompleteImage
                        });
                        $("#instructionsNames").show();
                        $(".autocompleteParent").click(function(){
                            $(".ac_input").focus();
                        });
                    } else if (list.length > 0) {
                        tabFinishedLoading();
                        $("#peopleListNamesSuggest")
                                .removeClass('first acfb-holder')
                                .html(renderPeopleAsCheckboxes("Names"));
                        $('#peopleListNamesSuggest > div[rel=Names] input').click(function() {
                            var id = $(this).val();
                            $.fn.SMS.get.selectionsHaveChanged = true;
                            //Fn for the check event
                            if (this.checked) {
                                checkNameboxAction(this);
                            } else
                            //Fn for the UNcheck event
                            {
                                //Remove data from selectedRecipientsList
                                $.each(selectedRecipientsList.names, function(i, parent) {
                                    if (parent) {
                                        //log(selectedRecipientsList.names.toString());
                                        //log("Item: "+item);
                                        if (parent[0] === id) {
                                            selectedRecipientsList.names.splice(Number(i), 1);
                                        }
                                    }
                                });
                                //Refresh {selectedRecipients} Number on TAB
                                if (getSelectedRecipientsList.length('names') > 0) {
                                    $('#peopleTabsNames span[rel=recipientsSum]').text(getSelectedRecipientsList.length('names'));
                                }
                                else    {
                                    $('#peopleTabsNames span[rel=recipientsSum]').fadeOut();
                                }
                                 $.fn.SMS.set.disableContinue();
                            }
                            // log(selectedRecipientsList.names.toString());
                        });

                    } else {
                        disableTab();
                    }
        };
        
        if ($('input[name=sakaiSiteId]').val() !== null && !$.fn.SMS.get.isIndividualsLoaded && var_getEveryoneInSite_participants.length === 0) {
            $.ajax({
                url: '/direct/sms-task/memberships/site/' + $('input[name=sakaiSiteId]').val() + '.json',
                dataType: "json",
                cache: false,
                beforeSend: function(){
                    tabLoading();
                },
                success: function(data) {
                    var query = [];
                    $.each(data["sms-task_collection"], function(i, item) {
                        query.push([item.sortName, item.id, item.displayId]);
                    });
                    var_getEveryoneInSite_participants = query;
                    initTab(query);
                },
                error: function(){
                    disableTab();
                }
            });
        }else if ($.fn.SMS.get.isIndividualsLoaded && var_getEveryoneInSite_participants.length !== 0 ){
             initTab(var_getEveryoneInSite_participants);
        }
    }

    function returnThis(d) {
        return d;
    }

    //
    // Debugging
    //
    function log($obj) {
        if (window.console && window.console.log) {
            window.console.log('obj: ' + $obj);
        }
        else {
            alert($obj);
        }
    }
    
    /**
     * @param string Takes one of these strings: "Roles", "Groups", "Names"
     */
    function renderPeopleAsCheckboxes(item) {
        var map = var_getEveryoneInSite_participants;
        var elem = "";
        for (var n in map) {
            if (n !== null && n.length !== 0) {
                elem += '<div class="columnBoxes" rel="' + item + '"><input type="checkbox" id="peopleList-' + map[n][0] + '-' + map[n][1] + '" title="' + map[n][0] + '" value="' + map[n][1] + '" />' +
                        '<label for="peopleList-' + map[n][0] + '-' + map[n][1] + '" name="' + map[n][0] + '" ' + item + 'Name="' + map[n][0] + '" ' + item + 'Id="' + map[n][1] + '">' + map[n][0] + '' +
                        ' ('+ map[n][2] +')</label></input></div>';
            }
        }
        return elem;
    }

    function renderPeople() {
        //Bind checkbox event listeners
        //for the Roles Tab
        $('#peopleListRoles input[type=checkbox]').on('click', function() {
            $.fn.SMS.get.selectionsHaveChanged = true;
            //Fn for the check event
            if (this.checked) {
                checkEntityboxAction(this, "Roles");
            } else
            //Fn for the UNcheck event
            {
                var thisRoleId = $(this).parent().find('label').attr('RolesId');
                //Remove data from selectedRecipientsList
                $.each(selectedRecipientsList.roles, function(i, parent) {
                    if (parent) {
                        //$.each(parent, function(n, item) {
                        if (parent[0] === thisRoleId) {
                            selectedRecipientsList.roles.splice(Number(i), 1);
                        }
                    }
                });
                //Refresh {selectedRecipients} Number on TAB
                if (getSelectedRecipientsList.length('roles') > 0){
                    $('#peopleTabsRoles span[rel=recipientsSum]').text(getSelectedRecipientsList.length('roles'));
                }
                else{
                    $('#peopleTabsRoles span[rel=recipientsSum]').fadeOut();
                }
                 $.fn.SMS.set.disableContinue();
            }

        });
        //for the Groups Tab
        $('#peopleListGroups input[type=checkbox]').on('click', function() {
            $.fn.SMS.get.selectionsHaveChanged = true;
            //Fn for the check event
            if (this.checked) {
                checkEntityboxAction(this, "Groups");
            } else
            //Fn for the UNcheck event
            {
                var thisRoleId = $(this).parent().find('label').attr('GroupsId');
                //log(thisRoleId);
                //Remove data from selectedRecipientsList
                $.each(selectedRecipientsList.groups, function(i, parent) {
                    if (parent) {
                        if (parent[0] === thisRoleId) {
                            selectedRecipientsList.groups.splice(Number(i), 1);
                        }
                    }
                });
                //Refresh {selectedRecipients} Number on TAB
                if (getSelectedRecipientsList.length('groups') > 0){
                    $('#peopleTabsGroups span[rel=recipientsSum]').text(getSelectedRecipientsList.length('groups'));
                }
                else  {
                    $('#peopleTabsGroups span[rel=recipientsSum]').fadeOut();
                }
                 $.fn.SMS.set.disableContinue();
            }

        });

        //Clear selectedRecipientsList
        $(document).on('selections.clear', function() {
            if (selectedRecipientsList.roles.length > 0) {
                selectedRecipientsList.roles = [];
            }
            if (selectedRecipientsList.groups.length > 0) {
                selectedRecipientsList.groups = [];
            }
            if (selectedRecipientsList.numbers.length > 0) {
                selectedRecipientsList.numbers = [];
            }
            if (selectedRecipientsList.names.length > 0) {
                selectedRecipientsList.names = [];
            }
            $.fn.SMS.get.preserveDomSelections = false;
            $('span[rel=recipientsSum]').fadeOut();
            $("#cReportConsole").slideUp('fast');
            $('div[id^=peopleList] input').each(function() {
                //log("Clearing all fields.");
                var t = this.type, tag = this.tagName.toLowerCase();
                if (t === 'text' || tag === 'textarea'){
                    this.value = '';
                }
                else if (t === 'checkbox' || t === 'radio'){
                    this.checked = false;
                }
            });
        });

        //Events for the Numbers textarea

        $('#checkNumbers').on('click', function() {
            var that = $('#peopleListNumbersBox'),
            that2 = $('#peopleListNumbersBox2'),
            strippedNumbers = [];
            //hide duplicate notice
            $("#peopleListNumbersDuplicates").fadeOut("fast");
            $.fn.SMS.get.selectionsHaveChanged = true;
            if (that.val()) {
                var numbers = that.val().split("\n"),
                nums_invalid = [];

                $.each(numbers, function(i, item) {
                    var num = item.split(' ').join('');
                    if (num.length > 9 && ((num.match(/^[0-9]/) || num.match(/^[+]/) || num.match(/^[(]/)) && (num.split('-').join('').split('(').join('').split(')').join('').match(/^[+]?\d+$/)))) {
                        //verify the unformattd version of the number is not a duplicate
                        var found = false,
                        strippedItem = item.replace(/[+]/g, "").replace(/[\-]/g, "").replace(/[ ]/g, "").replace(/[(]/g, "").replace(/[)]/g, "");//unformat the number
                        for (var x = 0; x < strippedNumbers.length; x++){
                           if ( strippedItem === strippedNumbers[x] ){
                               found = true;
                           }
                        }
                        if (! found){
                            strippedNumbers.push(strippedItem); // add to simple format numbers list
                            selectedRecipientsList.numbers.push(item); //add to real numbers list
                        }else{
                            //number is a duplicate  - tell the user
                            $("#peopleListNumbersDuplicates").fadeIn("fast");
                            //log("User entered this duplicate number: " + item);
                        }
                    } else {
                        nums_invalid.push(item);
                    }

                });

                //Remove duplicates
                selectedRecipientsList.numbers = unique(selectedRecipientsList.numbers);

                //Log report on valid numbers
                if (getSelectedRecipientsList.length('numbers') > 0) {
                    showSelectedNumbersInDOM();

                    $.fn.SMS.set.disableContinue();

                    //log(nums_invalid.length);
                    if (nums_invalid.length > 0) {
                        that.val(nums_invalid.toString().split(',').join('\n'));
                        $("#numbersInvalid .msg").fadeIn('fast', function() {
                            $(this).effect("highlight", 'slow');
                        });
                        //log('Not empty');
                    } else {
                        that.val('');
                        $("#numbersInvalid .msg").fadeOut();
                    }                                                                                                       
                    var temp = "";
                    $.each(selectedRecipientsList.numbers, function(i, item) {
                        temp += '<li class="acfb-data"><span>' + item + '</span> <img class="numberDel" rel="' + item + '" src="' + $.fn.SMS.settings.images.deleteAutocompleteImage + '"/></li>';
                    });
                    that2
                            .show()
                            .addClass('numbers-holder')
                            .html(temp);

                    $("#numbersValid")
                            .addClass('smsMessageSuccess')
                            .fadeIn('fast', function() {
                        $(this).effect("highlight", 'slow');
                    });

                    //bind delete image event
                    that2.find('li img.numberDel').bind('click', function() {
                        var number = $(this).attr('rel');
                        $.each(selectedRecipientsList.numbers, function(i, item) {
                            if (item && item == number) {
                                selectedRecipientsList.numbers.splice(i, 1);
                            }
                        });
                        $(this).parent().fadeOut(function() {
                            $(this).remove();
                        });
                        showSelectedNumbersInDOM();
                         $.fn.SMS.set.disableContinue();
                        that.focus();
                        //log(selectedRecipientsList.numbers.toString());
                    });
                } else {
                    $("#numbersInvalid .msg").fadeIn('fast');
                    //hide duplicate notice
                    $("#peopleListNumbersDuplicates").fadeOut("fast");
                    that.focus();
                }
            } else {
                $("#numbersInvalid .msg").fadeOut('fast');
                //hide duplicate notice
                $("#peopleListNumbersDuplicates").fadeOut("fast");
                that.focus();
            }
            //log(selectedRecipientsList.numbers.toString());
            return false;
        });

        function showSelectedNumbersInDOM() {
            if (getSelectedRecipientsList.length('numbers') > 0) {
                //Log report on valid numbers
                var realText = $("#peopleListNumbersLogText").text().replace('XXX', getSelectedRecipientsList.length('numbers'));
                $('#peopleListNumbersLog')
                        .fadeIn('fast')
                        .text(realText);
                //Refresh {selectedRecipients} Number on TAB
                $('#peopleTabsNumbers span[rel=recipientsSum]').fadeIn().text(getSelectedRecipientsList.length('numbers'));
            } else{
                $('#peopleTabsNumbers span[rel=recipientsSum]').fadeOut();
                $("#numbersValid").fadeOut();
                $("#numbersInvalid .msg").fadeOut();
                $("#peopleListNumbersLog").fadeOut();
                //hide duplicate notice
                $("#peopleListNumbersDuplicates").fadeOut("fast");
            }
        }
    }

    /**
     * Serialise all recipient values
     */
    function serializeRecipients() {
        //Force Validate Numbers Textarea
        if ($('#peopleListNumbersBox').val() !== "") {
            $('#checkNumbers').trigger('click');
        }
        if ($('#peopleListNumbersBox').val() === "") {
            //return false;
            var serial = "";
            var filterValues = ['roles', 'groups', 'names', 'numbers'];
            $.each(filterValues, function(n, filter) {
                var tempArray = [];
                if (getSelectedRecipientsList.length(filter) > 0) {
                    $.each(getSelectedRecipientsList.array(filter), function(i, item) {
                        tempArray.push(item[0]);
                    });
                    serial += filter + "=" + tempArray.toString() + "&";
                }
            });
            // log("Serialized: "+serial);
            return serial;
        } else {
            $('#peopleTabsNumbers a').trigger('click');
            return false;
        }
    }

    function checkNameboxAction(_this) {
        //Save data into selectedRecipientsList
        $.fn.SMS.get.selectionsHaveChanged = true;
        selectedRecipientsList.names.push([$(_this).val(), $(_this).attr('title')]);
        //Refresh {selectedRecipients} Number on TAB
        $('#peopleTabsNames span[rel=recipientsSum]').fadeIn().text(getSelectedRecipientsList.length('names'));
         $.fn.SMS.set.disableContinue();
    }

    function checkEntityboxAction(_this, type) {
        //Save data into selectedRecipientsList
        $.fn.SMS.get.selectionsHaveChanged = true;
        if (type.toLowerCase() === "Groups".toLowerCase()) {
            selectedRecipientsList.groups.push([$(_this).val(), $(_this).attr('title')]);
            //Refresh {selectedRecipients} Number on TAB
            $('#peopleTabsGroups span[rel=recipientsSum]').fadeIn().text(getSelectedRecipientsList.length('groups'));
        } else if (type.toLowerCase() === "Roles".toLowerCase()) {
            selectedRecipientsList.roles.push([$(_this).val(), $(_this).attr('title')]);
            //Refresh {selectedRecipients} Number on TAB
            $('#peopleTabsRoles span[rel=recipientsSum]').fadeIn().text(getSelectedRecipientsList.length('roles'));
        }
         $.fn.SMS.set.disableContinue();
    }

    function smsParams(domElements) {
        var tempParams = [];
        var includeSendDate = false;
        $.each(domElements, function(i, item) {
            if( item === "dateToSend"){
                includeSendDate = true;
            }
            var val = $('[name=' + item + ']').val() ;
            if (val !== null && val !== '' && val !== undefined) {
                    tempParams.push({name:item, value:val});
                //log(item +" --domElements--- "+ val);
                }
        });
        //set defaults for some params
        var savedElements = ["tasksakaiUserIds", "taskdeliveryEntityList", "taskdeliveryMobileNumbersSet"];
        if ($("#facebox").length === 0) {
            $.each(savedElements, function(i, item) {
                var val = $('[name=' + item + ']').val() ;
                if (val !== null && val !== '' && val !== undefined) {
                        tempParams.push({name:item.replace("task", ""), value:val});
                    //log(item +" ---savedElements-- "+ val);
                    }
            });
        }
        //if the recipients are edited in any way, set copyMe variable to the choose-recipients copy me dom value
        var copyMe = $("#copy-me").length !== 0 ? $("#copy-me:checked").length !== 0 : $("#taskcopyMe").val();
        if (copyMe !== null && copyMe) {
            tempParams.push({name:"copyMe", value:copyMe});
        }
        if ($("#statusType").val() === "EDIT") {
            tempParams.push({name:"id", value:$("#smsId").val()});
        }
        //send through a schedule date if dateToSend is included in @param domElements.
        if( includeSendDate ){
            tempParams.push({name:"dateToSend", value:$("#booleanScheduleDate input").val()});
        }
        if ($("#booleanExpiry:checked").length !== 0) {
            tempParams.push({name:"dateToExpire", value:$("#booleanExpiryDate input").val()});
        }
        return $.param(tempParams);
    }

    /**
     * To parse date object into a timestamp.
     * @param _date  ISO8601 format date
     */
    function parseIsoToTimestamp(_date) {
        if ( _date !== null ) {
            var s = $.trim(_date);
            s = s.replace(/-/, "/").replace(/-/, "/");
            s = s.replace(/-/, "/").replace(/-/, "/");
            s = s.replace(/:00.000/, "");
            s = s.replace(/T/, " ").replace(/Z/, " UTC");
            s = s.replace(/([\+\-]\d\d)\:?(\d\d)/, " $1$2"); // -04:00 -> -0400
            return Number(new Date(s));
        }
        return null;
    }

        function unique(a)
        {
            var r = [];
            o:for(var i = 0, n = a.length; i < n; i++) {
                for(var x = i + 1 ; x < n; x++)
                {
                    if(a[x]===a[i]){
                        //Duplicate found
                        $("#peopleListNumbersDuplicates").fadeIn("fast");
                        continue o;
                    }
                }
                r[r.length] = a[i];
            }
           return r;
        }

    function validateDates(){
        if ($("#booleanSchedule:checked").length !== 0 && $("#booleanExpiry:checked").length !== 0 ){
            return parseIsoToTimestamp($("#booleanScheduleDate input").val()) <= parseIsoToTimestamp($("#booleanExpiryDate input").val());
        }
        return true;
    }

})(jQuery);
