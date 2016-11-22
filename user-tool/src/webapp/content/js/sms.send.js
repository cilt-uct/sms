/**
 * JS Onload code for the SMS User Tool's Send view.
 * @Author lovemore.nalube@uct.ac.za
 *
 * Edited By edmore.moyo@uct.ac.za
 *
 **/

$(document).ready(function() {
    $("input[rel=back]").bind('click', function() {
        history.go(-1);
        return false;
    });

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
            var limit,
            boxValue = $(this).val(),
            len = boxValue.length,
            counterColour = {
                enough: "#CCCCCC",
                few:    "#5C0002",
                fewer:  "#D40D12"
            },
            countOfEscapedChars = 0,
            lengthLeft = 160;
            if (len > 0) {
                //SMS-170: When entering an SMS message, detect non-GSM chars and adjust max length to 70
                //sms has 140 bytes. if it's utf-16, that's 70 chars, if it's gsm alphabet, 160 chars are packed into 140 bytes (because the alphabet is 7bit)

                limit = !smsUtils.isEncodeableInGsm0338(boxValue) ? 70 : 160;
                lengthLeft = limit - len;

                // Get a count of the escape-encoded GSM characters in the string
                if( limit === 160 ){
                    countOfEscapedChars = smsUtils.numberOfEscapeEncodedGSMChars(boxValue);
                    lengthLeft = lengthLeft - countOfEscapedChars;
                }

                if( lengthLeft >= 20 ){
                    $('#smsBoxCounter').css("color", counterColour.enough);
                }else if( lengthLeft < 20 && lengthLeft > 9 ){
                    $('#smsBoxCounter').css("color", counterColour.few);
                }else{
                    $('#smsBoxCounter').css("color", counterColour.fewer);
                }
            }

            $('#smsBoxCounter').text(lengthLeft);

            //is message is too long?
            smsUtils.error.isMessageLengthValid = lengthLeft >= 0;

            $.fn.SMS.set.setSubmitTaskButton();
        });


    //focus on messageBody and update char counter
    $("#messageBody")
            .focus()
            .click();

    $("#smsAddRecipients").bind('click', function() {
        if(! $.fn.SMS.set.isRecipientsLinkClicked ){
            if ($.fn.SMS.get.preserveDomSelections && $.fn.SMS.get.selectionsHaveChanged && $("#facebox").length !== 0) {
                //Stop binding facebox onload events
                $(document).unbind('afterReveal.facebox');
                //Keep latest selections in case we want to restore them later on
                $.fn.SMS.get.previousSelectionsRoles = $.fn.SMS.get.getSelectedRecipientsListIDs("roles");
                $.fn.SMS.get.previousSelectionsNumbers = $.fn.SMS.get.getSelectedRecipientsListIDs("numbers");
                $.fn.SMS.get.previousSelectionsGroups = $.fn.SMS.get.getSelectedRecipientsListIDs("groups");
                $.fn.SMS.get.previousSelectionsNames = $.fn.SMS.get.getSelectedRecipientsListIDs("names");
                $("#recipientsCmd")
                        .attr("disabled", "disabled")
                        .removeClass("active");
                $("#facebox").fadeIn('fast');
            } else {
                $(document).unbind('afterReveal.facebox');
                $(document).bind('afterReveal.facebox', function(){
                    triggerFaceboxOnloadEvents();
                });
                $.facebox({ajax: this.href});
            }
            $.fn.SMS.set.isRecipientsLinkClicked = true;
        }
        return false;
    });

    $("#smsSend").bind('click', function() {
        //Bind EB submit action
        $(".loadingImage").show();
        var domElements = [ "messageBody", "sakaiUserIds", "deliveryEntityList", "deliveryMobileNumbersSet", "sakaiSiteId", "dateToSend"];
        $.fn.SMS.set.processSubmitTask(domElements, this);
    });

    if ($("#statusType").length !== 0) {
        if ($("#statusType").val() === "EDIT" || $("#statusType").val() === "REUSE") {
            $("#smsSend")
                    .removeAttr("disabled")
                    .addClass("active");
            $.fn.SMS.get.preserveDomSelections = true;
            //Hide set controls for dates
            $("#newSchedule").hide();
            $("#newExpiry").hide();
            $("#booleanScheduleDate").hide();
            $("#booleanExpiryDate").hide();
            //bind change date events
            $("#editScheduleChange").bind('click', function(){
                $("#editSchedule").hide();
                $("#newSchedule").show();
                $("#booleanSchedule").triggerHandler('click');
                return false;
            });
            $("#editExpiryChange").bind('click', function(){
                $("#editExpiry").hide();
                $("#newExpiry").show();
                $("#booleanExpiry").triggerHandler('click');
                return false;
            });
        }else{
			$("#reportConsole").hide();
            //Hide edit controls for dates
            $("#editSchedule").hide();
            $("#editExpiry").hide();
		}
    }

    // setup the Date listners
        $.each(["booleanSchedule", "booleanExpiry"], function(i, item) {
            $("#" + item).bind('click', function() {
                if (this.checked) {
                    $("#" + item + "Date").slideDown('normal');
                    $.fn.SMS.set.frameGrow(70, 'grow');
                } else {
                    if ($("#statusType").val() === "EDIT" || $("#statusType").val() === "REUSE") {
                        if(item.search(/Schedule/) === -1){
                            //Hide set controls for dates
                             $("#newExpiry").hide();
                             //Show edit controls for dates
                            $("#editExpiry").show();
                        }else{
                            //Hide set controls for dates
                            $("#newSchedule").hide();
                            //Show edit controls for dates
                            $("#editSchedule").show();
                        }
                    }
                    $("#" + item + "Date").slideUp('normal');
                    $.fn.SMS.set.frameGrow(-20, 'shrink');
                }
            });
        });

    //Move all facebox onload events to  this function to fix SMS-45

    function triggerFaceboxOnloadEvents(){
        $("#facebox .body .header").text($("#chooseTitle").text());
        $("#peopleList").tabs();

        //disable tabs that contain no entries in them eg. if there are no groups in site
        if ( $('#peopleListRoles > div[rel=Roles] input').length === 0 ){
            $.fn.SMS.set.disableTab('peopleTabsRoles', 'error-no-roles');
            }
        if ( $('#peopleListGroups > div[rel=Groups] input').length === 0 ){
            $.fn.SMS.set.disableTab('peopleTabsGroups', 'error-no-groups');
           }

        //Start membership loading event action
        $.fn.SMS.get.peopleByName(); //trigger event to populate the people lists ie:individuals

        $("#calculateCmd").bind('click', function(){
            var domElements = ["sakaiUserIds", "sakaiSiteId","deliveryEntityList", "deliveryMobileNumbersSet"];
            $.fn.SMS.set.processCalculate(domElements, this);
        });
        $("[rel=closeFB]").click(function(){
            $(document).trigger("close.facebox");
            $.fn.SMS.get.preserveDomSelections = false;
            $.fn.SMS.get.preserveNewDomSelections = true;
            $.fn.SMS.set.isRecipientsLinkClicked = false;
            return false;
        });
        $("#recipientsCmd").bind('click', function(){
            $("#facebox").hide('fast');
            $.fn.SMS.set.isRecipientsLinkClicked = false;
            $.fn.SMS.get.preserveDomSelections = true;
            $.fn.SMS.set.setSubmitTaskButton(this);
            //Update billing report
            $('#reportConsole')
                    .show()
                    .html( $('#cReportConsole').html() );
            //Extract edit text from rel attribute and use that for new link text.
            $("#smsAddRecipients").text($("#smsAddRecipients").attr("rel").split(",")[1]);
        });
       //envoke checkbox bindings
        $.fn.SMS.get.people();
       //Re-select saved selections
        if ($.fn.SMS.get.preserveNewDomSelections){
            $.fn.SMS.set.restoreSelections();
        }

        if($("#statusType").length !== 0 ){
            if($("#statusType").val() === "EDIT" || $("#statusType").val() === "REUSE"){
                $.fn.SMS.get.preserveDomSelections = true;
                if($("#facebox").length !== 0){
                    $.fn.SMS.set.restoreTaskSelections();
                }
            }
        }
        //Disable form submission compleletly to fix auto form submit bug SMS-136
        $("form[id=chooseForm]").bind("submit", function(){
            return false;
        });
        //preload small cancel icon. Dimensions are not important
        var preload1 = new Image(12,12);
        preload1.src = $.fn.SMS.settings.images.deleteAutocompleteImage;

    }

});
