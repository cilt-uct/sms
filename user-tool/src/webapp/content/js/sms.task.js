/**
 * JS library for the SMS User Tool's support view views.
 * @Author lovemore.nalube@uct.ac.za
 **/
$(document).ready(function() {
    $("input[rel=task]").task();
    $("input[rel=back]").on('click', function(e) {
        e.preventDefault();
        window.location = "../";
    });
});

(function($) {
    // Define class
    $.fn.task = function(options) {
        $.extend({}, $.fn.task.defaults, options);

        return this.each(function() {
            $(this).on('click', function() {
                init(this);
            });
        });
    };

    $.fn.task.defaults = {
        EB_DELETE_PATH: "/direct/sms-task/:ID:/delete",
        SUCCESS_PATH: function() {
            return "index?status=" + Number(new Date());
        },
        getSmsId: function() {
            return $("#smsId").val();
        }
    };

    function init(_that) {
        smsDelete(_that);
    }

    function smsDelete(_that) {
        if (smsConfirm()) {
            _that.disabled = true;
            $.ajax({
                url: $.fn.task.defaults.EB_DELETE_PATH.replace(':ID:', $.fn.task.defaults.getSmsId()) + getTaskParams(),
                type: "DELETE",
                error: function(xhr, ajaxOptions, thrownError) {
                     smsUtils.error.server(xhr, $(_that), "error");
                },
                success: function() {
                    window.location.href = $.fn.task.defaults.SUCCESS_PATH();
                }
            });
        }
    }

    function smsConfirm() {
        var msg = $("span[id*=actionDelete]").eq(0).text();
        return confirm(msg);
    }

    function getTaskParams(){
        var status = $("#statusToShow").val();
        if ( status !== undefined ){
            var params = [{name:"status", value:$("#statusToShow").val()}];
            return "?" + $.param(params);
        }
        return "";
    }
})(jQuery);
