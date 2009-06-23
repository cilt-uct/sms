/**
 * JS library for the SMS User Tool Utilities.
 * @author lovemore.nalube@uct.ac.za
 **/

var smsUtils = (function($) {

    //Deal with an ajax error
    var error = function(errorMessage, _this, element) {
        var errorElement = $("#" + element),
                marker;
        //if we are dealing witha server-side request
        if(_this !== null){
            //Remove any Server exception messages not meant for the user
            marker = errorMessage.indexOf(":");
            errorMessage = errorMessage.slice(marker + 2, errorMessage.length);
            marker = errorMessage.indexOf(":");
            errorMessage = errorMessage.slice(marker + 2, errorMessage.length);
            //remove the (rethrown) string if it exists
            errorMessage = errorMessage.replace(/\(rethrown\)/g, '');
            $(_this).removeAttr("disabled");
        }
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
            server: function(XMLHttpRequest, _this, element) {
                error($(XMLHttpRequest.responseText).find("u").eq(0).text(), _this, element);
            },
            dom: function(errorText) {
                 error(errorText, null, "errorFacebox");
            }
        }
    };
})($);