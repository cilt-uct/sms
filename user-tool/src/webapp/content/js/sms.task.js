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
        EB_UPDATE_PATH: "/direct/sms-task/:ID:/edit",
        SUCCESS_PATH: function(){return "index?status=" + Number(new Date())},
        getSmsId: function(){return $("#smsId").val();},
        getAbortCode: function(){return $("#abortCode").val()},
        getAbortMessage: function(){return $("#abortMessage").val()}
    }

     function init(_that) {
         if(_that.id == "smsDelete"){
             smsDelete(_that);
         }else if(_that.id == "smsStop"){
             smsStop(_that);
         }
     }

    function smsDelete(_that){
        if(smsConfirm($("#actionDelete").text())){
            $.ajax({
                url: $.fn.task.defaults.EB_DELETE_PATH.replace(':ID:', $.fn.task.defaults.getSmsId()),
                type: "DELETE",
                error: function(xhr, ajaxOptions, thrownError){
                    $.facebox("ERROR:: "+ xhr.status + ": "+ xhr.statusText);
                },
                success: function(){
                    window.location.href = $.fn.task.defaults.SUCCESS_PATH();
                }
            });
        }
    }


    function smsStop(_that){
        if(smsConfirm($("#actionAbort").text())){
            $.ajax({
                url: $.fn.task.defaults.EB_UPDATE_PATH.replace(':ID:', $.fn.task.defaults.getSmsId()),
                data: [{name:"statusCode", value:$.fn.task.defaults.getAbortCode()}, {name:"failReason" , value:$.fn.task.defaults.getAbortMessage()}],
                type: "POST",
                error: function(xhr, ajaxOptions, thrownError){
                    $.facebox("ERROR: "+ xhr.statusCode + ": "+ xhr.statusText);
                },
                success: function(){
                    window.location.href = $.fn.task.defaults.SUCCESS_PATH();
                }
            });
        }
    }

    function smsConfirm(msg){
        return confirm(msg);
    }

})(jQuery);