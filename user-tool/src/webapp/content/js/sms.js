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

        peopleByName: function(type) {
            if (type == null) {    //putting 3 equal signs (ie: ===) will break this check
                var_getEveryoneInSite_participants = getPeople('Names');
            }
            return var_getEveryoneInSite_participants;
        },
        getSelectedRecipientsListNames : function() {
            getSelectedRecipientsList.length('names');
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
        errorEb: null
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
                $(".loadingImage").show();
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
                        $("div[id^=errorStatus]").slideUp('fast');
                        $("#cReportConsole").slideUp('fast');
                    },
                    success: function(json) {
                        _this.disabled = false;
                        $("#cReportConsole").slideDown('fast', function() {
                            $(this).effect('highlight', 'fast');
                            $.fn.SMS.set.frameGrow($("#cReportConsole").height(), "grow");
                        });
                        var cSelected = json.groupSizeEstimate;
                        var cCredits = json.creditEstimate;
                        var cCost = roundOffNumber(json.costEstimate, 2);
                        var cTotal = $("#cReportConsole .console-total").text();
                        $("#cReportConsole .console-selected").text(cSelected);
                        $("#cReportConsole .console-credits").text(cCredits);
                        $("#cReportConsole .console-cost").text($("#currency").val() + cCost);

                        if (cTotal < cCredits) {
                            _this.disabled = false;
                            $("#errorStatus406").slideDown('fast', function() {
                                $(this).effect('highlight', 'slow');
                            });
                            $("#recipientsCmd").attr("disabled", "disabled");
                        } else {
                            $("#recipientsCmd").removeAttr("disabled");
                        }
                        if( entityList.length > 0 ){
                            //Show the may change alert message since we have selected at least one group/role
                            $("#cReportConsole .mayChange").show();
                        }else{
                            $("#cReportConsole .mayChange").hide();
                        }
                        $(".loadingImage").hide();
                        return false;
                    },
                    error: function(xhr) {
                        if (xhr.status === 403) {
                            //status is 403 - FORBIDDEN
                            _this.disabled = false;
                            $("#errorStatus403").slideDown('fast', function() {
                                $(this).effect('highlight', 'slow');
                            });
                            $("#recipientsCmd").attr("disabled", "disabled");
                        } else {
                            $("#errorStatusOther").slideDown('fast', function() {
                                $(this).effect('highlight', 'slow');
                            });
                            $("#recipientsCmd").attr("disabled", "disabled");
                            $.fn.SMS.set.frameGrow($("#cReportConsole").height(), "grow");
                            _this.disabled = false;
                        }
                        $(".loadingImage").hide();
                        return false;
                    }
                });
            } else {
                alert($("#errorNoSelections").text());
                $("div[id^=errorStatus]").slideUp('fast');
                $("#cReportConsole").slideUp('fast');
            }

        },
        processSubmitTask: function(domElements, _this) {
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
                    if (xhr.status === 400) {
                        //status is 400 - ERROR
                        _this.disabled = false;
                        $("#errorSend400").slideDown('fast', function() {
                            $(this).effect('highlight', 'slow');
                        });
                        $.fn.SMS.set.frameGrow(50, 'grow');
                    }else if (xhr.status === 401) {
                        //status is 401 - UNAUTHORIZED
                        _this.disabled = false;
                        $("#errorSend401").slideDown('fast', function() {
                            $(this).effect('highlight', 'slow');
                        });
                        $.fn.SMS.set.frameGrow(50, 'grow');
                    }else if (xhr.status === 405) {
                        //status is 405 - METHOD NOT ALLOWED
                        _this.disabled = false;
                        $("#errorSend405").slideDown('fast', function() {
                            $(this).effect('highlight', 'slow');
                        });
                        $.fn.SMS.set.frameGrow(50, 'grow');
                    }else if (xhr.status === 406) {
                        //status is 406 - NOT ACCEPTABLE
                        _this.disabled = false;
                        $("#errorSend406").slideDown('fast', function() {
                            $(this).effect('highlight', 'slow');
                        });
                        $.fn.SMS.set.frameGrow(50, 'grow');
                    } else {
                        $("#errorSendOther").slideDown('fast', function() {
                            $(this).effect('highlight', 'slow');
                        });
                        $.fn.SMS.set.frameGrow(50, 'grow');
                    }
                    $(".loadingImage").hide();
                    return false;
                },
                success: function(_id) {
                    $("div[id^=errorStatus]").slideUp('fast');
                    window.location.href = $("#goto-home").attr('href') + "?id=" + _id + "&status=" + Number(new Date());
                    $(".loadingImage").hide();
                    return true;
                }
            });
        }   ,
        setSubmitTaskButton: function() {
            //log($("#messageBody").val().length);
            if ($.fn.SMS.get.preserveNewDomSelections) {
                $.fn.SMS.set.restoreSelections();
            }
            var isRecipientsChosen = ($.fn.SMS.get.isSelectionMade() || $("input[id^=task]").length > 0);
            if ( isRecipientsChosen && $("#messageBody").val().length > 0 ) {
                $("#smsSend").removeAttr("disabled");
            } else {
                $("#smsSend").attr("disabled", "disabled");
            }
        },
        addSelectedRecipientsListName: function(array) {
            selectedRecipientsList.names.push([array[1],array[0]]);
            return true;
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
                    if (parent[1] === id) {
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

                                    //bind delete image event
                                    v.find('img.p').bind('click', function() {
                                        $.fn.SMS.set.sliceSelectedRecipientsListName(userArray[0]);
                                        $(this).parent().fadeOut(function() {
                                            $(this).remove();
                                        });
                                        
                                    });
                                    $(".ac_input").insertBefore(v);
                                    $('#peopleTabsNames span[rel=recipientsSum]').fadeIn().text(getSelectedRecipientsList.length('names'));
                                }
                            });
                        }
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
        frameGrow: function(height, updown) {
        var _height = height === "" ? 280 : Number(height) + 40;
        var frame = parent.document.getElementById(window.name);
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
        disableTab: function(tabName, tabErrorElement){
            var err = $('#'+ tabErrorElement ).text() === "" ? $('#'+ tabErrorElement ).val() : $('#'+ tabErrorElement ).text();
            $('#' + tabName + ' a')
                    .css($.fn.SMS.settings.css.tabDisabled)
                    .attr('title', err)
                    .unbind('click')
                    .bind('click', function(){
                return false;
            });
        },
        disableContinue: function(){
            //Disable the continue button and force user to calculate before saving any new changes
            $("#recipientsCmd").attr("disabled", "disabled");
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
            }
        }
    };
    // end of public methods

    /**
     * Private variables
     */

    var var_getEveryoneInSite;     // to hold full people list
    var var_getEveryoneInSite_participants = [];     // to hold full participants list
    var selectedRecipientsList = { //Object with multidimetional Dimensional Arrays to hold the Selected Recipients
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
     * Getter for recipients page
     * @param filter Search list by {string} variable. Returns a Two Dimensional array
     */
    function getPeople(filter) {
        if (filter !== null && (filter === "Roles" || filter === "Groups" || filter === "Names")) {
            var query = [];
            if (filter === "Names") {
                    if ($('input[name=sakaiSiteId]').val() !== null) {
                        $.ajax({
                            url: '/direct/sms-task/' + $('input[name=sakaiSiteId]').val() + '/memberships.json',
                            dataType: "json",
                            cache: true,
                            success: function(data) {
                                $.each(data["sms-task_collection"], function(i, item) {
                                    query.push([item.sortName, item.id, item.eid]);
                                });
                            }
                        });
                    }
            }

            //log(query);
            return query;

        }
        return null;
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
        $('#peopleListRoles > div[@rel=Roles] input').bind('click', function() {
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
        $('#peopleListGroups > div[@rel=Groups] input').bind('click', function() {
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
        $(document).bind('selections.clear', function() {
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

        //Initialise the Individuals Tab
        if (var_getEveryoneInSite_participants.length > 16) {
            $("#instructionsNames").show();
            $(".autocompleteParent").click(function(){
                $(".ac_input").focus();
            });
        } else if (var_getEveryoneInSite_participants.length > 0) {
            $("#peopleListNamesSuggest")
                    .removeClass('first acfb-holder')
                    .html(renderPeopleAsCheckboxes("Names"));
            $('#peopleListNamesSuggest > div[@rel=Names] input').click(function() {
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
            $('#peopleListNamesSuggest').hide();
            $('#peopleListNamesSuggest p').hide();
            $('#withNumbersOnlyNames').hide();
            $('#errorNoNames').show();
            $.fn.SMS.set.disableTab('peopleTabsNames', 'errorEb');
        }

        //Events for the Numbers textarea

        $('#checkNumbers').bind('click', function() {
            var that = $('#peopleListNumbersBox'),
            that2 = $('#peopleListNumbersBox2'),
            strippedNumbers = [];
            //hide duplicate notice
            $("#peopleListNumbersDuplicates").fadeOut("fast");
            $.fn.SMS.get.selectionsHaveChanged = true;
            if (that.val()) {
                var numbers = that.val().split("\n");
                var nums_invalid = [];

                $.each(numbers, function(i, item) {
                    var num = item.split(' ').join('');
                    if (num.length > 9 && ((num.match(/^[0-9]/) || num.match(/^[+]/) || num.match(/^[(]/)) && (num.split('-').join('').split('(').join('').split(')').join('').match(/^[+]?\d+$/)))) {
                        //verify the unformattd version of the number is not a duplicate
                        var found = false,
                        strippedItem = item.replace(/[+]/g, "").replace(/[-]/g, "").replace(/[ ]/g, "").replace(/[(]/g, "").replace(/[)]/g, "");//unformat the number
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
            tempParams.push({name:"dateToSend", value:$("[id=smsDatesScheduleDate:1:true-date]").val()});
        }
        if ($("#booleanExpiry:checked").length !== 0) {
            tempParams.push({name:"dateToExpire", value:$("[id=smsDatesExpiryDate:1:true-date]").val()});
        }
        return $.param(tempParams);
    }

    /**
     * To parse date object into a timestamp. NB:This is not yet being used due to tests on the EP SimpleDateFormat converter code.
     * @param _date  ISO8601 format date
     */
    function parseIsoToTimestamp(_date) {
        if (isNaN(_date)) {
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

    /**
     * Rounding off to any number of decimal places. 
     * http://www.xfriday.com/support/index.php?_m=knowledgebase&_a=viewarticle&kbarticleid=3
     * @param myNum    Actual number to be rounded off
     * @param numOfDec   Number of decimal places
     */
    function roundOffNumber(myNum, numOfDec)
    {
        var decimal = 1;
        for (i = 1; i <= numOfDec; i++){
            decimal = decimal * 10
        }
        var myFormattedNum = (Math.round(myNum * decimal) / decimal).toFixed(numOfDec)
        return(myFormattedNum)
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

})(jQuery);
