/**
 * JS Onload code for the SMS User Tool's Send view.
 * @Author lovemore.nalube@uct.ac.za
 **/

$(document).ready(function() {
    $("input[@rel=back]").bind('click', function() {
        history.go(-1);
        return false;
    });
    $.fn.SMS.get.peopleByName(); //Populate the people lists ie:individuals

    //focus on messageBody and update char counter
    $("#messageBody")
            .focus()
            .click();

    $("#smsAddRecipients").bind('click', function() {
        if ($.fn.SMS.get.preserveDomSelections && $.fn.SMS.get.selectionsHaveChanged && $("#facebox").length !== 0) {
            //Keep latest selections in case we want to restore them later on
            $.fn.SMS.get.previousSelectionsRoles = $.fn.SMS.get.getSelectedRecipientsListIDs("roles");
            $.fn.SMS.get.previousSelectionsNumbers = $.fn.SMS.get.getSelectedRecipientsListIDs("numbers");
            $.fn.SMS.get.previousSelectionsGroups = $.fn.SMS.get.getSelectedRecipientsListIDs("groups");
            $.fn.SMS.get.previousSelectionsNames = $.fn.SMS.get.getSelectedRecipientsListIDs("names");
            $("#facebox").fadeIn('fast');
        } else {
            $.facebox({ajax: this.href});
        }
        return false;
    });


    $("#smsSend").bind('click', function() {
        //Bind EB submit action
        $(".loadingImage").show();
        var domElements = [ "sakaiUserIds", "deliveryEntityList", "deliveryMobileNumbersSet", "sakaiSiteId", "senderUserName", "senderUserId", "messageBody"];
        $.fn.SMS.set.processSubmitTask(domElements, this);
    });

    if ($("#statusType").length !== 0) {
        if ($("#statusType").val() === "EDIT" || $("#statusType").val() === "REUSE") {
            $("#smsSend").removeAttr("disabled");
            $.fn.SMS.get.preserveDomSelections = true;
        }
    }
});