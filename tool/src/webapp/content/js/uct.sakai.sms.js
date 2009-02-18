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
        //log(this);
        //log(options.toSource());
        // build main options before class instantiation
        var opts = $.extend({}, $.fn.SMS.defaults, options);

        return this.each(function() {
            // log('eref');
            //$.fn.SMS.get.report_table();
            alert(getEveryoneInSite());
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
    // private function for debugging
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
    //
    // define and expose our format function
    //
    $.fn.SMS.get = {
        status_console: function() {
            $.getJSON($.fn.SMS.defaults.URL_EB_GET_ACC_REPORT, 'parameters', function(data) {
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
            $.getJSON($.fn.SMS.defaults.URL_EB_GET_ALL_SMSES, function(data) {
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
                            status_icon = $.fn.SMS.defaults.images.status.completed;
                            break;
                        case "1":
                            status_icon = $.fn.SMS.defaults.images.status.failed;
                            break;
                        case "2":
                            status_icon = $.fn.SMS.defaults.images.status.scheduled;
                            break;
                        case "3":
                            status_icon = $.fn.SMS.defaults.images.status.progress;
                            break;
                        case "4":
                            status_icon = $.fn.SMS.defaults.images.status.edit;
                            break;
                    }
                    shadow.find('td[rel=status]').html($("<img/>")
                            .attr("src", $.fn.SMS.defaults.images.base + status_icon[0])
                            .attr('title', status_icon[1])
                            .attr('alt', status_icon[1])
                            );
                    shadow.find('td[rel=author]').text(item.author);
                    shadow.find('td[rel=date]').text(item.date_taken);
                    shadow.find('td[rel=recipients]').text(item.author_id);
                    shadow.find('td[rel=credits]').text(item.tags);
                    $('#reportTable').find('tbody').append(shadow);
                });

                //sort on date column
                $('#reportTable').tablesorter({
                    sortList: [[3,0]]
                });

                log(getEveryoneInSite());

            });
        },

        /**
         * Getters for recipients page
         */
        people: {
            byRole: function() {

            },
            byGroup: function() {

            },
            byName: function(name) {

            }

        }


    };
    //
    // SMS class defaults
    //
    $.fn.SMS.defaults = {
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
        }

    };
    //
    // end of public methods
    //

    /**
     *  Method used to retrieve full people list
     */

    var getEveryoneInSite = function() {
        var t;
    /*t = {"menu": {
  "id": "file",
  "value": "File",
  "popup": {
    "menuitem": [
      {"value": "New", "onclick": "CreateNewDoc()"},
      {"value": "Open", "onclick": "OpenDoc()"},
      {"value": "Close", "onclick": "CloseDoc()"}
    ]
  }
}}; */
        //log(t.menu.value);
        //$.getJSON($.fn.SMS.defaults.URL_EB_GET_PEOPLE, function(d){log(d); t = d;});
        $.ajax({
            url: $.fn.SMS.defaults.URL_EB_GET_PEOPLE,
            async: true,
            dataType: "JSON",
            success: function(data){
                t = data;
                alert(t);
                //return t;
            }
        });
        //log(t.toSource())
        //var r = t;
        if(t != null){
            return t;
        }
        else{
            //getEveryoneInSite();
        }

        /*return (
        //$.get(
               // $.fn.SMS.defaults.URL_EB_GET_PEOPLE).responseText
                $.ajax({
  url: $.fn.SMS.defaults.URL_EB_GET_PEOPLE,
  async: false,
                    dataType: 'JSON'
 }).responseText

                )  ;*/
        //return ((jsonData != null) ? "yes":"no");
        //log("json "+jsonData);
        //return true;
    };

    function returnThis(d){ log(d) ; return d;}


})(jQuery);
