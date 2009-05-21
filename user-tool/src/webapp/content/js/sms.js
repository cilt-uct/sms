/**
 * JS library for the SMS User Tool's send SMS view.
 * @author lovemore.nalube@uct.ac.za
 **/
(function($) {
    // Define class
    $.fn.SMS = function(options) {
        init($.fn.SMS.settings.initList);
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
        }
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
                            frameGrow($("#cReportConsole").height(), "grow");
                        });
                        var cSelected = json.groupSizeEstimate;
                        var cCredits = json.creditEstimate;
                        var cCost = json.costEstimate;
                        var cTotal = $("#cReportConsole .console-total").text();
                        $("#cReportConsole .console-selected").text(cSelected);
                        $("#cReportConsole .console-credits").text(cCredits);
                        $("#cReportConsole .console-cost").text(cCost);

                        if (cTotal < cCredits) {
                            _this.disabled = false;
                            $("#errorStatus406").slideDown('fast', function() {
                                $(this).effect('highlight', 'slow');
                            });
                            $("#recipientsCmd").attr("disabled", "disabled");
                        } else {
                            $("#recipientsCmd").removeAttr("disabled");
                        }
                        $(".loadingImage").hide();
                        return false;
                    },
                    error: function(xhr, ajaxOptions, thrownError) {
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
                            frameGrow($("#cReportConsole").height(), "grow");
                            _this.disabled = false;
                        }
                        $(".loadingImage").hide();
                        return false;
                    }
                });
            } else {
                alert("Make a selection first.");
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
                error: function(xhr, ajaxOptions, thrownError) {
                    if (xhr.status === 403) {
                        //status is 403 - FORBIDDEN
                        _this.disabled = false;
                        $("#errorSend403").slideDown('fast', function() {
                            $(this).effect('highlight', 'slow');
                        });
                        frameGrow(50, 'grow');
                    } else {
                        $("#errorSendOther").slideDown('fast', function() {
                            $(this).effect('highlight', 'slow');
                        });
                        frameGrow(50, 'grow');
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
            if (((
                    $.fn.SMS.get.getSelectedRecipientsListIDs("roles").length > 0 ||
                    $.fn.SMS.get.getSelectedRecipientsListIDs("groups").length > 0 ||
                    $.fn.SMS.get.getSelectedRecipientsListIDs("names").length > 0 ||
                    $.fn.SMS.get.getSelectedRecipientsListIDs("numbers").length > 0
                    ) || ($("#statusType").val() === "EDIT" || $("#statusType").val() === "REUSE")) && $("#messageBody").val().length !== 0 ) {
                $("#smsSend").removeAttr("disabled");
            } else {
                $("#smsSend").attr("disabled", "disabled");
            }
        },
        setSelectedRecipientsListName: function(array) {
            selectedRecipientsList.names = array;
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
                $.each(userIds, function(i, entityId) {
                    if (entityId !== "" && entityId !== null) {
                        $('input[type=checkbox][value=' + entityId + ']').each(function() {
                            this.checked = true;
                            checkNameboxAction(this);
                        });
                    }
                });
            }

            if (numbers !== null && numbers.length !== 0) {
                //Render saved numbers
                if (numbers !== null && numbers.length > 0) {
                    $("#peopleListNumbersBox").text(numbers.join('\n'));
                    $('#checkNumbers').click();
                }
            }
        }
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
        inited:  false,
        initList: ({
            "items": [{
                    "fname": "init_smsBoxCounter",
                    "fdelay": 0
                },
                {
                    "fname": "setDateListners",
                    "fdelay": 0
                }
            ]
        })

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
        //log(filter);
        if (filter !== null && (filter === "Roles" || filter === "Groups" || filter === "Names")) {
            if (var_getEveryoneInSite === null){
                init($.fn.SMS.settings.initList);
            }
            var query = [];
            if (filter === "Names") {
                    if ($('input[name=sakaiSiteId]').val() !== null) {
                        $.ajax({
                            url: '/direct/membership/site/' + $('input[name=sakaiSiteId]').val() + '.json',
                            dataType: "json",
                            cache: true,
                            success: function(data) {
                                $.each(data.membership_collection, function(i, item) {
                                    query.push([item.userDisplayName, item.userId]);
                                });
                            },
                            error: function(xhr, ajaxOptions, thrownError) {
                                alert("An error occured and you will not have ability to select participants by their names");
                                return false;
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
                elem += '<div rel="' + item + '"><input type="checkbox" id="peopleList-' + map[n][0] + '-' + map[n][1] + '" title="' + map[n][0] + '" value="' + map[n][1] + '" />' +
                        '<label for="peopleList-' + map[n][0] + '-' + map[n][1] + '" name="' + map[n][0] + '" ' + item + 'Name="' + map[n][0] + '" ' + item + 'Id="' + map[n][1] + '">' + map[n][0] + '' +
                        '</label></input></div>';
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
            $("#peopleListNamesSuggest").autoCompletefb();
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
                }
                // log(selectedRecipientsList.names.toString());
            });

        } else {
            $('#peopleListNamesSuggest').hide();
            $('#peopleListNamesSuggest p').hide();
            $('#errorNoNames').show();
        }

        //Events for the Numbers textarea

        $('#checkNumbers').bind('click', function() {
            var that = $('#peopleListNumbersBox');
            var that2 = $('#peopleListNumbersBox2');
            $.fn.SMS.get.selectionsHaveChanged = true;
            if (that.val()) {
                var numbers = that.val().split("\n");
                var nums_invalid = [];

                $.each(numbers, function(i, item) {
                    var num = item.split(' ').join('');
                    if (num.length > 9 && ((num.match(/^[0-9]/) || num.match(/^[+]/) || num.match(/^[(]/)) && (num.split('-').join('').split('(').join('').split(')').join('').match(/^[+]?\d+$/)))) {
                        selectedRecipientsList.numbers.push([item]);
                    } else {
                        nums_invalid.push(item);
                    }

                });
                //Log report on valid numbers
                if (getSelectedRecipientsList.length('numbers') > 0) {
                    showSelectedNumbersInDOM();
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
                        temp += '<li class="acfb-data"><span>' + item + '</span> <img class="numberDel" src="' + $.fn.SMS.settings.images.deleteAutocompleteImage + '"/></li>';
                    });
                    that2
                            .show()
                            .css({
                        border: 'none',
                        height: '100px',
                        overflow: 'auto',
                        width: '180px'
                    })
                            .addClass('acfb-holder')
                            .html(temp);

                    $("#numbersValid")
                            .addClass('messageSuccess')
                            .fadeIn('fast', function() {
                        $(this).effect("highlight", 'slow');
                    });

                    //bind delete image event
                    that2.find('li img.numberDel').bind('click', function() {
                        var tempText = $(this).parent().find('span').text();
                        $.each(selectedRecipientsList.numbers, function(i, item) {
                            if (item && item === tempText) {
                                selectedRecipientsList.numbers.splice(i, 1);
                            }
                        });
                        $(this).parent().fadeOut(function() {
                            $(this).remove();
                        });
                        showSelectedNumbersInDOM();
                        that.focus();
                        //log(selectedRecipientsList.numbers.toString());
                    });
                } else {
                    $("#numbersInvalid .msg").fadeIn('fast');
                    that.focus();
                }
            } else {
                $("#numbersInvalid .msg").fadeOut('fast');
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
            }
        }
    }

    $(document).bind('init_smsBoxCounter', function(){
        //Counter for the SMS Textarea
        $("#messageBody")
                .change(function(e) {
            $(this).keypress();
        })
                .keyup(function(e) {
            $(this).keypress();
        })
                .keydown(function(e) {
            $(this).keypress();
        })
                .focus(function(e) {
            $(this).keypress();
        })
                .click(function(e) {
            $(this).keypress();
        })
                .keypress(function(e) {
            var limit = 160;
            var len = $(this).val().length;
            if (len <= limit && len >= 0) {
                $('#smsBoxCounter').text(limit - len);
            } else {
                $(this).val($(this).val().substr(0, limit));
            }
            $.fn.SMS.set.setSubmitTaskButton();
        });
    });

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
    }

    function frameGrow(height, updown) {
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
    }

    function smsParams(domElements) {
        var tempParams = [];
        $.each(domElements, function(i, item) {
            var val = $('[name=' + item + ']').val() ;
            if (val !== null) {
                if (val !== '') {
                    tempParams.push({name:item, value:val});
                //log(item +" ----- "+ val);
                }
            }
        });
        //set defaults for some params
        var savedElements = ["tasksakaiUserIds", "taskdeliveryEntityList", "taskdeliveryMobileNumbersSet"];
        if ($("#facebox").length === 0) {
            $.each(savedElements, function(i, item) {
                var val = $('[name=' + item + ']').val() ;
                if (val !== null) {
                    if (val !== ''){    // Don't combine if statements to avoid RSF template translation error
                        tempParams.push({name:item.replace("task", ""), value:val});
                    //log(item +" ----- "+ val);
                    }
                }
            });
        }
        //if the recipients are edited in any way, set copyMe variable to the choose-recipients copy me dom value
        var copyMe = $("#copy-me").length !== 0 ? $("#copy-me:checked").length !== 0 : $("#taskcopyMe").val();
        if (copyMe !== null && copyMe) {
            tempParams.push({name:"copyMe", value:copyMe});
        }
        tempParams.push({name:"messageBody", value:$("#messageBody").val()});
        if ($("#statusType").val() === "EDIT") {
            tempParams.push({name:"id", value:$("#smsId").val()});
        }
        //ALWAYS send through a schedule date.
        tempParams.push({name:"dateToSend", value:$("[id=smsDatesScheduleDate:1:true-date]").val()});
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


    $(document).bind('setDateListners', function(){
        $.each(["booleanSchedule", "booleanExpiry"], function(i, item) {
            $("#" + item).bind('click', function() {
                if (this.checked) {
                    $("#" + item + "Date").slideDown('normal');
                    frameGrow(70, 'grow');
                } else {
                    $("#" + item + "Date").slideUp('normal');
                    frameGrow(20, 'shrink');
                }
            });
            $("#" + item + ":checked").each(function() {
                if (this.checked) {
                    $(this).triggerHandler('click');
                }
            });
        });
    });

    /**
     * @param functionList {JSON Object} A list of function names to be initialised on application load. Has fname {String} and fdelay {Int} as child values.
     */

    function init(functionList) {
        $.each(functionList.items, function(i, item) {
            if (item.fname !== null && item.fdelay !== null) {
                setTimeout(function() {
                    $(document).trigger(item.fname);
                }, item.fdelay);
                $.fn.SMS.settings.inited = true;
            } else
            {
                $.fn.SMS.settings.inited = false;
            }

        });
    }

})(jQuery);
