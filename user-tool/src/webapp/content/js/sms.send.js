/**
 * JS Onload code for the SMS User Tool's Send view.
 * @Author lovemore.nalube@uct.ac.za
 *
 * Edited By edmore.moyo@uct.ac.za
 *
 **/

$(document).ready(function() {
    var smsPopup = new SmsPopup();
    document.body.appendChild(smsPopup.createPopup());
    
    $("input[rel=back]").on('click', function(e) {
        e.preventDefault();
        window.location = "../";
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

            //is message is too long or empty?
            smsUtils.error.isMessageLengthValid = lengthLeft >= 0 && lengthLeft < 160;

            $.fn.SMS.set.setSubmitTaskButton();
        });


    //focus on messageBody and update char counter
    $("#messageBody")
            .focus()
            .click();

    $("#smsAddRecipients").bind('click', function() {
        if(! $.fn.SMS.set.isRecipientsLinkClicked ){
            if ($.fn.SMS.get.preserveDomSelections && $.fn.SMS.get.selectionsHaveChanged && $("#smsPopup").length !== 0) {
                //Keep latest selections in case we want to restore them later on
                $.fn.SMS.get.previousSelectionsRoles = $.fn.SMS.get.getSelectedRecipientsListIDs("roles");
                $.fn.SMS.get.previousSelectionsNumbers = $.fn.SMS.get.getSelectedRecipientsListIDs("numbers");
                $.fn.SMS.get.previousSelectionsGroups = $.fn.SMS.get.getSelectedRecipientsListIDs("groups");
                $.fn.SMS.get.previousSelectionsNames = $.fn.SMS.get.getSelectedRecipientsListIDs("names");
                $("#recipientsCmd")
                        .attr("disabled", "disabled")
                        .removeClass("active");
            } else {
                smsPopup.displayPopup({url: this.href, selector: '#chooseForm'}, triggerPopupEvents);
            }
            $.fn.SMS.set.isRecipientsLinkClicked = true;
        } else {
          smsPopup.showPopupToUser();
        }
        return false;
    });

    $("#smsSend").on('click', function() {
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
            $("#editScheduleChange").on('click', function(){
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
            $("#" + item).on('click', function() {
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

    //Attach events after popup is created and populated
    function triggerPopupEvents(){
        $('#peopleList ul a').each(function(i, el) {
          $(el).on('click', smsPopup.switchView);
          if (i == 0) $(el).click();
        });

        //disable tabs that contain no entries in them eg. if there are no groups in site
        if ( smsPopup.getElements('#peopleList div[rel="Roles"] input').length === 0 ){
            $.fn.SMS.set.disableTab('peopleTabsRoles', 'error-no-roles');
        }
        if ( smsPopup.getElements('#peopleListGroups div[rel=Groups] input').length === 0 ){
            $.fn.SMS.set.disableTab('peopleTabsGroups', 'error-no-groups');
        }
        //Start membership loading event action
        $.fn.SMS.get.peopleByName(); //trigger event to populate the people lists ie:individuals

        $("#calculateCmd").on('click', function(){
            var domElements = ["sakaiUserIds", "sakaiSiteId","deliveryEntityList", "deliveryMobileNumbersSet"];
            $.fn.SMS.set.processCalculate(domElements, this);
        });
        $("[rel=closeFB]").on('click', function(e){
            e.preventDefault();
            smsPopup.closePopup();
            $.fn.SMS.get.preserveDomSelections = false;
            $.fn.SMS.get.preserveNewDomSelections = true;
            $.fn.SMS.set.isRecipientsLinkClicked = false;
            return false;
            //TODO: remove selections on cancel?
        });
        $("#recipientsCmd").on('click', function(){
            smsPopup.closePopup();
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
       //invoke checkbox bindings
        $('div[id^="peopleList"] input[type=checkbox]').on('click', function() {
            $.fn.SMS.get.selectionsHaveChanged = true;
            var listType = this.getAttribute('data-listtype');
            var listUCfirst = listType.charAt(0).toUpperCase() + listType.substring(1);
            //Fn for the check event
            if (this.checked) {
                $.fn.SMS.set.setEntityBoxAction(this, listType);
            } else
            //Fn for the UNcheck event
            {
                var typeLabel = listUCfirst + 'Id';
                var thisId = $(this).parent().find('label').attr(typeLabel);
                //Remove data from selectedRecipientsList
                $.each($.fn.SMS.get.getSelectedRecipientsListIDs(listType), function(i, parent) {
                    if (parent) {
                        //$.each(parent, function(n, item) {
                        if (parent === thisId) {
                            var func = 'removeRecipientFrom' + listUCfirst + 'ByIndex';
                            if ($.fn.SMS.set[func]) {
                              $.fn.SMS.set[func](Number(i));
                            }
                        }
                    }
                });
                //Refresh {selectedRecipients} Number on TAB
                if ($.fn.SMS.get.getSelectedRecipientsListIDs(listType).length > 0){
                  //  var recipientTotal = 99;
                    $('#peopleTabs' + listUCfirst + ' span[rel=recipientsSum]').text($.fn.SMS.get.getSelectedRecipientsListIDs(listType).length);
                }
                else{
                    $('#peopleTabs' + listUCfirst + ' span[rel=recipientsSum]').fadeOut();
                }
                 $.fn.SMS.set.disableContinue();
            }

        });
       //Re-select saved selections
        if ($.fn.SMS.get.preserveNewDomSelections){
            $.fn.SMS.set.restoreSelections();
        }

        //TODO: see if there is a need for this
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

        $('#checkNumbers').on('click', function(e) {
            function showSelectedNumbersInDOM() {
              var currentNumberList = $.fn.SMS.get.getSelectedRecipientsListIDs('numbers');
              if (currentNumberList.length > 0) {
                //Log report on valid numbers
                  var realText = $("#peopleListNumbersLogText").text().replace('XXX', currentNumberList.length);
                  $('#peopleListNumbersLog')
                        .fadeIn('fast')
                        .text(realText);
                //Refresh {selectedRecipients} Number on TAB
                  $('#peopleTabsNumbers span[rel=recipientsSum]').fadeIn().text(currentNumberList.length);
              } else{
                $('#peopleTabsNumbers span[rel=recipientsSum]').fadeOut();
                $("#numbersValid").fadeOut();
                $("#numbersInvalid .msg").fadeOut();
                $("#peopleListNumbersLog").fadeOut();
                //hide duplicate notice
                $("#peopleListNumbersDuplicates").fadeOut("fast");
              }
            }
            e.preventDefault();
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
                            $.fn.SMS.set.addSelectedRecipientsListByType('numbers', item); //add to real numbers list
                        }else{
                            //number is a duplicate  - tell the user
                            $("#peopleListNumbersDuplicates").fadeIn("fast");
                            //log("User entered this duplicate number: " + item);
                        }
                    } else {
                        nums_invalid.push(item);
                    }

                });

                //TODO: Remove duplicates
 //               $.fn.SMS.get.getSelectedRecipientsListIDs('numbers') = unique(selectedRecipientsList.numbers);

                //Log report on valid numbers
                var currentNumbers = $.fn.SMS.get.getSelectedRecipientsListIDs('numbers');
                if (currentNumbers.length > 0) {
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
                    $.each(currentNumbers, function(i, item) {
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
                    that2.on('click', 'li img.numberDel', function() {
                        var number = $(this).attr('rel');
                        $.each(currentNumbers, function(i, item) {
                            if (item && item == number) {
                              $.fn.SMS.set.removeRecipientByTypeAndIndex('numbers', i);
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


    }
});
