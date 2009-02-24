/**
 *
 * For the interaction with EB.
 *
 **/
(function($) {
    //
    // Define class
    //
    $.fn.SMS = function(options) {
        init($.fn.SMS.settings.initList);
        //log(this);
        //log(options.toSource());
        // build main options before class instantiation
        $.extend({}, $.fn.SMS.defaults, options);

        return this.each(function() {
            // log('eref');
            //$.fn.SMS.get.report_table();
            //setTimeout(function() {
            //   log($.fn.SMS.get.peopleByName().toString())
            //}, 2000);
            //while(var_getEveryoneInSite == null)
            //log(var_getEveryoneInSite);
            //return false;
        });
        /* iterate and reformat each matched element
         return this.each(function() {
         $this = $(this);
         // build element specific options
         var o = $.meta ? $.extend({}, opts, $this.data()) : opts;
         // update element styles
         $this.css({
         backgroundColor: o.background,
         color: o.foreground
         });
         var markup = $this.html();
         // call our format function
         markup = $.fn.hilight.format(markup);
         $this.html(markup);
         });
         */
    };
    //
    // define and expose our format function
    //
    $.fn.SMS.get = {
        status_console: function() {
            $.getJSON($.fn.SMS.settings.URL_EB_GET_ACC_REPORT, 'parameters', function(data) {
                //Extract Json and populate object
                $('#reportConsole #creditsAvail').text(data.credits);
                $('#reportConsole #value').text(data.value);
                if (data.creditsReq)
                    $('#reportConsole #creditsReq').text(data.creditsReq);
                if (data.valueReq)
                    $('#reportConsole #valueReq').text(data.valueReq);
                if ($('#recipientsNumSelected').length > 0)
                    $('#reportConsole .recipientsNumSelected').text(parseInt($('#recipientsNumSelected').text()));

            });
        },
        report_table: function() {
            $.getJSON($.fn.SMS.settings.URL_EB_GET_ALL_SMSES, function(data) {
                //Extract Json and populate object
                var cell = $('#reportTable').find('tbody tr:eq(0)').clone();
                /**
                 * iterate, install values and show each row
                 */
                $.each(data.items, function(i, item) {
                    var shadow = cell.clone();
                    shadow.find('td[rel=title] a').text(item.title);
                    var status_icon;
                    switch (item.status) {
                        case "0":
                            status_icon = $.fn.SMS.settings.images.status.completed;
                            break;
                        case "1":
                            status_icon = $.fn.SMS.settings.images.status.failed;
                            break;
                        case "2":
                            status_icon = $.fn.SMS.settings.images.status.scheduled;
                            break;
                        case "3":
                            status_icon = $.fn.SMS.settings.images.status.progress;
                            break;
                        case "4":
                            status_icon = $.fn.SMS.settings.images.status.edit;
                            break;
                    }
                    shadow.find('td[rel=status]').html(
                            $("<img/>")
                            .attr("src", $.fn.SMS.settings.images.base + status_icon[0])
                            .attr('title', status_icon[1])
                            .attr('alt', status_icon[1])
                            );
                    shadow.find('td[rel=author]').text(item.author);
                    shadow.find('td[rel=date]').text(item.date_taken);
                    shadow.find('td[rel=recipients]').text(item.author_id);
                    shadow.find('td[rel=credits]').text(item.tags);
                    $('#reportTable').find('tbody').append(shadow);
                });

                //sort on date (4th) column
                $('#reportTable').tablesorter({
                    sortList: [[3,0]]
                });

                //log(getEveryoneInSite());

            });
        },


        people: function() {
            renderPeople();
        },

        peopleByName: function() {
            return getPeople('Names');
        }



    };
    $.fn.SMS.set = {
        setSelectedRecipientsListName: function(array) {
            selectedRecipientsList.names.push(array);
            return true;
        },
        sliceSelectedRecipientsListName: function(id) {
            $.each(selectedRecipientsList.names, function(i, parent) {
                if (parent) {
                    //$.each(parent, function(n, item) {
                        if (parent[0] == id) {
                            selectedRecipientsList.names.splice(parseInt(i), 1);
                        }
                    //});
                }
            });
        }
    }

    //
    // SMS class defaults
    //
    $.fn.SMS.settings = {
        URL_EB_GET_ALL_SMSES: '/sms-tool/content/js/json.js',
        URL_EB_GET_THIS_SMS: '/direct/',
        URL_EB_GET_ACC_REPORT: '/direct/',
        URL_EB_GET_PEOPLE: '/sms-tool/content/js/json-people.js',
        /**
         * Set URLs
         **/
        URL_EB_SET_SMS: '/direct/',
        /**
         * Image/icons locations
         */
        images: {
            base: '/library/image/silk/',
            busy: 'spinner.gif',
            deleteAutocompleteImage: '../js/jquery.autocomplete/delete.gif',
            status: {
                completed: ['tick.png', 'Completed'],
                failed: ['cancel.png','Failed'],
                scheduled: ['time.png','Scheduled'],
                progress: ['bullet_go.png','Progress'],
                edit: ['page_white_edit.png','Edit']
            }
        },
        /**
         * Language Strings
         */
        lang_strings: {
            report_alert_cost: '',
            report_alert_credits: ''
        },

        /**
         * Initialiser
         */
        inited:  false,
        initList: ({
            "items": [{
                "fname": "init_smsBoxCounter()",
                "fdelay": "0"
            },
                {
                    "fname": "setEveryoneInSite()",
                    "fdelay": "1"
                }
            ]
        })

    };
    //
    // end of public methods
    //

    /**
     * Private variables
     */

    var var_getEveryoneInSite;     // to hold full people list
    var selectedRecipientsList = { //Object with multidimetional Dimensional Arrays to hold the Selected Recipients
        roles:
                new Array()
        ,
        groups: new Array()
        ,
        names: new Array()
        ,
        numbers: new Array()

    }

    /**
     *  Method used to retrieve full people list
     */

    function setEveryoneInSite() {
        $.getJSON($.fn.SMS.settings.URL_EB_GET_PEOPLE, function(data) {
            var_getEveryoneInSite = data;
        });

    }
    ;

    function getEveryoneInSite() {
        return var_getEveryoneInSite;
    }

    var getSelectedRecipientsList = {
        /*//var filterValues = new Array('length','roles','groups','names','numbers');
         if(filter){
         switch(filter){
         case 'length':
         return selectedRecipientsList.
         }
         }*/
        length: function(filter) {
            switch (filter) {
                case "roles":
                    return selectedRecipientsList.roles.length;
                    break;
                case "groups":
                    return selectedRecipientsList.groups.length;
                    break;
                case "names":
                    return selectedRecipientsList.names.length;
                    break;
                case "numbers":
                    return selectedRecipientsList.numbers.length;
                    break;
            }
        }
        //toString: function(){return selectedRecipientsList}
    }
    /**
     * Getter for recipients page
     * @param filter Search list by {string} variable. Returns a Two Dimensional array
     */
    function getPeople(filter) {
        //log(filter);
        if (filter != null && (filter == "Roles" || filter == "Groups" || filter == "Names")) {
            if (var_getEveryoneInSite == null)init($.fn.SMS.settings.initList);
            var query = new Array();
            switch (filter) {
                case "Roles":
                    $.each(var_getEveryoneInSite.people[0].roles, function(i, item) {
                        query.push(new Array(item.rname, item.rid));
                    });
                    break;
                case "Groups":
                    $.each(var_getEveryoneInSite.people[0].groups, function(i, item) {
                        query.push(new Array(item.gname, item.gid));
                    });
                    break;
                case "Names":
                    $.each(var_getEveryoneInSite.people[0].participants, function(i, item) {
                        query.push(new Array(item.name, item.to));
                    });
                    break;
            }

            //log(query);
            return query;

        }
    }

    function returnThis(d) {
        log(d);
        return d;
    }
    //
    // Debugging
    //
    function log($obj) {
        if (window.console && window.console.log) {
            //window.console.log('toString: ' + $obj.toString());
            //window.console.log('Size: ' + $obj.size());
            window.console.log('obj: ' + $obj);
        }
        else {
            alert($obj);
        }
    }
    ;

    /**
     * @param functionList {JSON Objact} A list of function names to be initialised on application load. Has fname {String} and fdelay {Int} as child values.
     */

    function init(functionList) {
        $.each(functionList.items, function(i, item) {
            if (item.fname != null && item.fdelay != null) {
                setTimeout(function() {
                    eval(item.fname)
                }, parseInt(item.fdelay));
                $.fn.SMS.settings.inited = true;
            } else
            {
                $.fn.SMS.settings.inited = false;
            }

        });
    }
    /**
     *
     * @param string Takes one of these strings: "Roles", "Groups", "Names"
     */
    function renderPeopleAsCheckboxes(item) {
        var map = getPeople(item);
        var elem = "";
        for (n in map) {
            elem += '\
                   <div rel="' + item + '"><input type="checkbox" id="peopleList-' + map[n][0] + '-' + map[n][1] + '" name="' + map[n][0] + '">\
                   <label for="peopleList-' + map[n][0] + '-' + map[n][1] + '" name="' + map[n][0] + '" ' + item + 'Name="' + map[n][0] + '" ' + item + 'Id="' + map[n][1] + '">' + map[n][0] + '\
                   </label>\
                   </input></div>\
                   ';
        }
        return elem;
    }

    function renderPeople() {

        $('#peopleListRoles').append(renderPeopleAsCheckboxes("Roles"));
        $('#peopleListGroups').append(renderPeopleAsCheckboxes("Groups"));


        //Bind checkbox event listeners

        //for the Roles Tab
        $('#peopleListRoles > div[@rel=Roles] input').bind('click', function() {
            //Fn for the check event
            if (this.checked) {
                //Save data into selectedRecipientsList
                selectedRecipientsList.roles.push(new Array($(this).parent().find('label').attr('RolesId'), $(this).parent().find('label').attr('RolesName')));
                //Refresh {selectedRecipients} Number on TAB
                $('#peopleTabsRoles span[rel=recipientsSum]').fadeIn().text(getSelectedRecipientsList.length('roles'));

            } else
            //Fn for the UNcheck event
            {
                var thisRoleId = $(this).parent().find('label').attr('RolesId');
                //Remove data from selectedRecipientsList
                $.each(selectedRecipientsList.roles, function(i, parent) {
                    if (parent) {
                        //$.each(parent, function(n, item) {
                            if (parent[0] == thisRoleId) {
                                selectedRecipientsList.roles.splice(parseInt(i), 1);
                            }
                        //});
                    }
                });
                //Refresh {selectedRecipients} Number on TAB
                if (getSelectedRecipientsList.length('roles') > 0)
                    $('#peopleTabsRoles span[rel=recipientsSum]').text(getSelectedRecipientsList.length('roles'));
                else
                    $('#peopleTabsRoles span[rel=recipientsSum]').fadeOut();
            }

        });

        //for the Groups Tab
        $('#peopleListGroups > div[@rel=Groups] input').bind('click', function() {
            //Fn for the check event
            if (this.checked) {
                //Save data into selectedRecipientsList
                selectedRecipientsList.groups.push(new Array($(this).parent().find('label').attr('GroupsId'), $(this).parent().find('label').attr('GroupsName')));
                //Refresh {selectedRecipients} Number on TAB
                $('#peopleTabsGroups span[rel=recipientsSum]').fadeIn().text(getSelectedRecipientsList.length('groups'));
            } else
            //Fn for the UNcheck event
            {
                var thisRoleId = $(this).parent().find('label').attr('GroupsId');
                //log(thisRoleId);
                //Remove data from selectedRecipientsList
                $.each(selectedRecipientsList.groups, function(i, parent) {
                    if (parent) {
                        //log(selectedRecipientsList.groups.toString());
                        //$.each(parent, function(n, item) {
                            //log("Item: "+item);
                            if (parent[0] == thisRoleId) {
                               selectedRecipientsList.groups.splice(parseInt(i), 1);
                            }
                        //});
                    }
                });
                //Refresh {selectedRecipients} Number on TAB
                if (getSelectedRecipientsList.length('groups') > 0)
                    $('#peopleTabsGroups span[rel=recipientsSum]').text(getSelectedRecipientsList.length('groups'));
                else
                    $('#peopleTabsGroups span[rel=recipientsSum]').fadeOut();
            }

        });

        //Clear selectedRecipientsList on facebox exit
        $(document).bind('afterClose.facebox', function() {
            if (selectedRecipientsList.roles.length > 0) selectedRecipientsList.roles = new Array();
            if (selectedRecipientsList.groups.length > 0) selectedRecipientsList.groups = new Array();
            if (selectedRecipientsList.numbers.length > 0) selectedRecipientsList.numbers = new Array();
            if (selectedRecipientsList.names.length > 0) selectedRecipientsList.names = new Array();
        });

        //Initialise the Individuals Tab

        if ($.fn.SMS.get.peopleByName().length > 16) {
            $("#peopleListNamesSuggest").autoCompletefb();
        } else if($.fn.SMS.get.peopleByName().length > 0) {
            $("#peopleListNamesSuggest")
                    .removeClass('first acfb-holder')
                    .html(renderPeopleAsCheckboxes("Names"));
            //for the Groups Tab
            $('#peopleListNamesSuggest > div[@rel=Names] input').bind('click', function() {
                //Fn for the check event
                if (this.checked) {
                    //Save data into selectedRecipientsList
                    selectedRecipientsList.names.push(new Array($(this).parent().find('label').attr('NamesId'), $(this).parent().find('label').attr('NamesName')));
                    //Refresh {selectedRecipients} Number on TAB
                    $('#peopleTabsNames span[rel=recipientsSum]').fadeIn().text(getSelectedRecipientsList.length('names'));
                } else
                //Fn for the UNcheck event
                {
                    var thisRoleId = $(this).parent().find('label').attr('NamesId');
                    //log(thisRoleId);
                    //Remove data from selectedRecipientsList
                    $.each(selectedRecipientsList.names, function(i, parent) {
                        if (parent) {
                            //log(selectedRecipientsList.groups.toString());
                            //$.each(parent, function(n, item) {
                                //log("Item: "+item);
                                if (parent[0] == thisRoleId) {
                                   selectedRecipientsList.names.splice(parseInt(i), 1);
                                }
                            //});
                        }
                    });
                    //Refresh {selectedRecipients} Number on TAB
                    if (getSelectedRecipientsList.length('names') > 0)
                        $('#peopleTabsNames span[rel=recipientsSum]').text(getSelectedRecipientsList.length('names'));
                    else
                        $('#peopleTabsNames span[rel=recipientsSum]').fadeOut();
                }

            });

        }

        //Events for the Numbers textarea

        $('#checkNumbers').bind('click', function() {
            var that = $('#peopleListNumbersBox');
            var that2 = $('#peopleListNumbersBox2');
            if (that.val()) {
                var numbers = that.val().split("\n");
                var nums_invalid = new Array();

                $.each(numbers, function(i, item) {
                    var num = item.split(' ').join('');
                    if (num.length > 9 && ((num.match(/^[0-9]/) || num.match(/^[+]/) || num.match(/^[(]/)) && (num.split('-').join('').split('(').join('').split(')').join('').match(/^[+]?\d+$/)))) {
                        selectedRecipientsList.numbers.push(new Array(item));
                    } else {
                        nums_invalid.push(item);
                    }

                });


                //Log report on valid numbers
                showSelectedNumbersInDOM();
                that
                    //.addClass('messageEr ror')
                        .val(nums_invalid.toString().split(',').join('\n'));
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
                $("#numbersValid").fadeIn().effect("highlight", 'slow');
                $("#numbersInvalid").fadeIn().effect("highlight", 'slow');
                //bind delete image event
                that2.find('li img.numberDel').bind('click', function() {
                    var tempText = $(this).parent().find('span').text();
                    $.each(selectedRecipientsList.numbers, function(i, item) {
                        if (item && item == tempText) {
                            selectedRecipientsList.numbers.splice(i, 1);
                        }
                    });
                    $(this).parent().fadeOut(function() {
                        $(this).remove();
                    });
                    showSelectedNumbersInDOM();
                    log(selectedRecipientsList.numbers.toString());
                });
            }
            that.focus();
            return false;
        });

        function showSelectedNumbersInDOM() {
            if (getSelectedRecipientsList.length('numbers') > 0) {
                //Log report on valid numbers
                $('#peopleListNumbersLog')
                        .slideDown()
                        .addClass('')
                        .html('\
                    You have ' + getSelectedRecipientsList.length('numbers') + ' valid numbers.\
                    <br /> \
                    \
                    \
                  ');
                $('#checkNumbers').text("Check new/edited numbers again");
                //Refresh {selectedRecipients} Number on TAB
                $('#peopleTabsNumbers span[rel=recipientsSum]').fadeIn().text(getSelectedRecipientsList.length('numbers'));
            } else
                $('#peopleTabsNumbers span[rel=recipientsSum]').fadeOut();

        }

        /****Restore Selected Recipients List control items
         if(selectedRecipientsList.roles.length > 0){
         $.each(selectedRecipientsList.roles, function(i, parent) {
         if (parent) {
         $.each(parent, function(n, item) {
         var elem = 'label[rolesId='+item+']';
         if($(elem).length > 0){
         $(elem).parent().find('input')
         .addClass('selectedItem')
         .hide()
         .attr('checked', 'checked');
         // $(elem).parent().triggerHandler('click');
         }
         });
         }
         });
         }
         ******/

        //for list of individuals in site
        /*var map = getPeople("Names");
         for (n in map) {
         var elem = '\
         <div><input type="checkbox" id="peopleList-'+map[n][1]+'" name="'+map[n][0]+'">\
         <label for="peopleList-'+map[n][1]+'" name="'+map[n][0]+'">'+map[n][0]+'\
         </label>\
         </div></input>\
         ';
         $('#peopleListNames').append(elem);
         }*/


    }

    function init_smsBoxCounter() {
        //Counter for the SMS Textarea
        $("#smsMessage")
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
        });
    }


})(jQuery);
