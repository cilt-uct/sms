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
                    shadow.find('td[rel=status]').html($("<img/>")
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

        peopleByName: function(){
            return getPeople('Names');
        }


    };
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

    function renderPeople() {
        var list = new Array("Roles", "Groups");
        for (i in list) {
            var map = getPeople(list[i]);
            var tag = '#peopleList' + list[i];
            for (n in map) {
                var elem = '\
                   <div rel="' + list[i] + '"><input type="checkbox" id="peopleList-' + map[n][0] + '-' + map[n][1] + '" name="' + map[n][0] + '">\
                   <label for="peopleList-' + map[n][0] + '-' + map[n][1] + '" name="' + map[n][0] + '" '+list[i]+'Name="' + map[n][0] + '" '+list[i]+'Id="' + map[n][1] + '">' + map[n][0] + '\
                   </label>\
                   </div></input>\
                   ';
                $(tag).append(elem);
            }
        }

        //Bind checkbox event listeners

        //for the Roles Tab
        $('#peopleListRoles > div[rel=Roles]').each(function() {
            $(this).toggle(
                //Fn for the check event
                    function() {
                        $(this).addClass('selectedItem');
                        $(this).find('input')
                                .hide()
                                .attr('checked', 'checked');
                        //Save data into selectedRecipientsList
                        selectedRecipientsList.roles.push(new Array($(this).find('label').attr('RolesId'), $(this).find('label').attr('RolesName')));

                        //Refresh {selectedRecipients} Number on TAB
                        $('#peopleTabsRoles span[rel=recipientsSum]').fadeIn().text(getSelectedRecipientsList.length('roles'));
                    },
                //Fn for the UNcheck event
                    function() {
                        $(this).removeClass('selectedItem');
                        $(this).find('input')
                                .show()
                                .removeAttr('checked');
                        var thisRoleId = $(this).find('label').attr('RolesId');
                        //Remove data from selectedRecipientsList
                        $.each(selectedRecipientsList.roles, function(i, parent) {
                            if (parent) {
                                $.each(parent, function(n, item) {
                                    if (item == thisRoleId) {
                                        selectedRecipientsList.roles.splice(parseInt(i), 1);
                                    }
                                });
                            }
                        });
                        //Refresh {selectedRecipients} Number on TAB
                        if (getSelectedRecipientsList.length('roles') > 0)
                            $('#peopleTabsRoles span[rel=recipientsSum]').text(getSelectedRecipientsList.length('roles'));
                        else
                            $('#peopleTabsRoles span[rel=recipientsSum]').fadeOut();
                    }
                    );
        });

        //for the Groups Tab
        $('#peopleListGroups > div[rel=Groups]').each(function() {
            $(this).toggle(
                //Fn for the check event
                    function() {
                        $(this).addClass('selectedItem');
                        $(this).find('input')
                                .hide()
                                .attr('checked', 'checked');
                        //Save data into selectedRecipientsList
                        selectedRecipientsList.groups.push(new Array($(this).find('label').attr('GroupsId'), $(this).find('label').attr('GroupsName')));

                        //Refresh {selectedRecipients} Number on TAB
                        $('#peopleTabsGroups span[rel=recipientsSum]').fadeIn().text(getSelectedRecipientsList.length('groups'));
                    },
                //Fn for the UNcheck event
                    function() {
                        $(this).removeClass('selectedItem');
                        $(this).find('input')
                                .show()
                                .removeAttr('checked');
                        var thisRoleId = $(this).find('label').attr('GroupsId');
                        //Remove data from selectedRecipientsList
                        $.each(selectedRecipientsList.groups, function(i, parent) {
                            if (parent) {
                                $.each(parent, function(n, item) {
                                    if (item == thisRoleId) {
                                        selectedRecipientsList.groups.splice(parseInt(i), 1);
                                    }
                                });
                            }
                        });
                        //Refresh {selectedRecipients} Number on TAB
                        if (getSelectedRecipientsList.length('roles') > 0)
                            $('#peopleTabsGroups span[rel=recipientsSum]').text(getSelectedRecipientsList.length('groups'));
                        else
                            $('#peopleTabsGroups span[rel=recipientsSum]').fadeOut();
                    }
                    );
        });

        //Clear selectedRecipientsList on facebox exit
        $(document).bind('afterClose.facebox', function(){
             if(selectedRecipientsList.roles.length > 0) selectedRecipientsList.roles = new Array();
            if(selectedRecipientsList.groups.length > 0) selectedRecipientsList.groups = new Array();
            if(selectedRecipientsList.numbers.length > 0) selectedRecipientsList.numbers = new Array();
            if(selectedRecipientsList.names.length > 0) selectedRecipientsList.names = new Array();
 });

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
