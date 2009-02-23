/*
 * jQuery plugin: autoCompletefb(AutoComplete Facebook)
 * @requires jQuery v1.2.2 or later
 * using plugin:jquery.autocomplete.js
 *
 * Credits:
 * - Idea: Facebook
 * - Guillermo Rauch: Original MooTools script
 * - InteRiders <http://interiders.com/> 
 *
 * Copyright (c) 2008 Widi Harsojo <wharsojo@gmail.com>, http://wharsojo.wordpress.com/
 * Dual licensed under the MIT and GPL licenses:
 *   http://www.opensource.org/licenses/mit-license.php
 *   http://www.gnu.org/licenses/gpl.html
 */
 
jQuery.fn.autoCompletefb = function(options) 
{
	var tmp = this;
	var settings = 
	{
		ul         : tmp,
		urlLookup  : $.fn.SMS.get.peopleByName(),
		acOptions  : {},
		foundClass : ".acfb-data",
		inputClass : ".acfb-input",
        deleteImage: "/library/image/silk/cancel.png"
	}
	if(options) jQuery.extend(settings, options);
	
	var acfb = 
	{
		params  : settings,
		getData : function()
		{	
			var result = '';
		    $(settings.foundClass,tmp).each(function(i)
			{
				if (i>0)result+=',';
			    result += $('span',this).html();
		    });
			return result;
		},
		clearData : function()
		{	
		    $(settings.foundClass,tmp).remove();
			$(settings.inputClass,tmp).focus();
			return tmp.acfb;
		},
		removeFind : function(o){
			$(o).unbind('click').parent().remove();
			$(settings.inputClass,tmp).focus();
            if ($('#peopleListNamesSuggest > li').length > 0)
                            $('#peopleTabsNames span[rel=recipientsSum]').fadeIn().text($('#peopleListNamesSuggest > li').length);
                        else
                            $('#peopleTabsNames span[rel=recipientsSum]').fadeOut();
			return tmp.acfb;
		}
	}
	
	$(settings.foundClass+" img.p").click(function(){
		acfb.removeFind(this);
	});
	
	$(settings.inputClass,tmp).autocomplete(settings.urlLookup,settings.acOptions);
	$(settings.inputClass,tmp).result(function(e,d,f){
		var f = settings.foundClass.replace(/\./,'');
		var v = '<li class="'+f+'"><span>'+d[0]+'</span> <img class="p" src="'+settings.deleteImage+'"/></li>';
		var x = $(settings.inputClass,tmp).before(v);
		$('.p',x[0].previousSibling).click(function(){
			acfb.removeFind(this);
		});
		$(settings.inputClass,tmp).val('').focus();
        $.fn.SMS.set.setSelectedRecipientsListName(d);
        $('#peopleTabsNames span[rel=recipientsSum]').fadeIn().text($('#peopleListNamesSuggest > li').length);
	});
	$(settings.inputClass,tmp).focus();
	return acfb;
}