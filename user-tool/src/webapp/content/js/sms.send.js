/**
 * Library for the send sms page.
 */

$(document).ready(function(){
	$("input[@rel=back]").bind('click', function(){
        history.go(-1);
        return false;
    });
                //$(document).SMS().bind('reveal.facebox', function(){
					//$.get( "/direct/membership/site/" + $('input[name=sakaiSiteId]').val() + ".json" );
                   	$.fn.SMS.get.peopleByName(); //Populate the people lists ie:individuals
                //});

                //if we are reopening a task, focus on messageBody and update char counter
                $("#messageBody")
                        .focus()
                        .click();

                    $("#smsAddRecipients").bind('click',function() {
                        if($.fn.SMS.get.preserveDomSelections && $("#facebox").length != 0 ){
                           $("#facebox").fadeIn('fast');
                        }else{
                            $.facebox({ajax: this.href});
                        }
                        return false;
                    });
                //});

                $("#smsSend").bind('click', function(){
                    //validate variables

                    //Bind EB submit action
                    $(".loadingImage").show();
                         var domElements = [ "sakaiUserIds", "deliveryEntityList", "deliveryMobileNumbersSet", "sakaiSiteId", "senderUserName", "senderUserId", "messageBody"];
                        $.fn.SMS.set.processSubmitTask(domElements, this);
                        window.location.href = $("#goto-home").attr('href');
                    });

                if($("#statusType").length != 0 ){
                    if($("#statusType").val() == "EDIT" || $("#statusType").val() == "REUSE"){
                        $("#smsSend").removeAttr("disabled");
                        $.fn.SMS.get.preserveDomSelections = true;
                    }
                }
            });