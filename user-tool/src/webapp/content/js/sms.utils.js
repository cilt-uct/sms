/**
 * JS library for the SMS User Tool Utilities.
 * @author lovemore.nalube@uct.ac.za
 **/

var smsUtils = (function($) {

    //Deal with an ajax error
    var ajaxError = function(XMLHttpRequest, status, _this, element) {
        var errorElement = $("#" + element),
                errorMessage = $(XMLHttpRequest.responseText).find("u").eq(0).text(),
                marker;
        //Remove any Server exception messages not meant for the user
        marker = errorMessage.indexOf(":");
        errorMessage = errorMessage.slice(marker + 2, errorMessage.length);
        marker = errorMessage.indexOf(":");
        errorMessage = errorMessage.slice(marker + 2, errorMessage.length);

        $(_this).removeAttr("disabled");
        errorElement.find("div").text(status.toUpperCase());
        errorElement.find("span").text(errorMessage);
        errorElement.slideDown('fast', function() {
            $(this).effect('highlight', 'slow');
        });
        $.fn.SMS.set.frameGrow(50, 'grow');
        $(".loadingImage").hide();
        return false;
    };


    //public methods
    return {
        error: {
            server: function(XMLHttpRequest, status, _this, element) {
                ajaxError(XMLHttpRequest, status, _this, element);
            },
            dom: function() {
            }
        }
    };
})($);