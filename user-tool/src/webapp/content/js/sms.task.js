/**
 *
 * For the EB interaction on sms user support pages.
 *
 **/
$(document).ready(function(){
    $("input[@rel=task]").task();
    $("input[@rel=back]").bind('click', function(){
        history.go(-1);
        return false;
    });
});

(function($) {

   //
    // Define class
    //
    $.fn.task = function(options) {
        $.extend({}, $.fn.task.defaults, options);

        return this.each(function() {
            $(this).bind('click', function(){init(this)});
        });
    };

    $.fn.task.defaults = {
        EB_DELETE_PATH: "/direct/sms-task/:ID:/delete",
        EB_UPDATE_PATH: "/direct/sms-task/:ID:/delete",
        SUCCESS_PATH: "index",
        getSmsId: function(){
            return $("#smsId").val();
        },
        getAbortCode: function(){
            return $("#abortCode").val();
        }
    }

     function init(_that) {
         if(_that.id = "smsDelete"){
             smsDelete(_that);
         }else if(_that.id = "smsStop"){
             smsStop(_that);
         }
     }

    function smsDelete(_that){
        if(smsConfirm("Sure?")){
            $.ajax({
                url: $.fn.task.defaults.EB_DELETE_PATH.replace(':ID:', $.fn.task.defaults.getSmsId()),
                type: "DELETE",
                error: function(event, XMLHttpRequest, ajaxOptions, thrownError){
                    //alert(thrownError + " &&" +event.info);
                },
                success: function(){
                    window.location.href = $.fn.task.defaults.SUCCESS_PATH;
                }
            });
        }
    }


    function smsStop(_that){
        if(smsConfirm("Sure to stop?")){
            $.ajax({
                url: $.fn.task.defaults.EB_UPDATE_PATH.replace(':ID:', $.fn.task.defaults.getAbortCode()),
                type: "POST",
                error: function(event, XMLHttpRequest, ajaxOptions, thrownError){
                    //alert(thrownError + " &&" +event.info);
                },
                success: function(){
                    window.location.href = $.fn.task.defaults.SUCCESS_PATH;
                }
            });
        }
    }

    function smsConfirm(msg){
        return confirm(msg);
    }

})(jQuery);