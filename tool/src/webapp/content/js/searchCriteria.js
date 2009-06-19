function getToday(){
	var now = new Date();
	var y = now.getFullYear();
	var m = now.getMonth() + 1;
	var d = now.getDate();
	var dateString = m + '/' + d + '/' + y; 
	return dateString;
}

function checkNumbers(input){
	var fossil = document.getElementsByName(input.id + '-fossil')[0];
	var check = fossil.value.substring(0,33); // Don't want to check value part as well
	if (check == 'istring#{searchMessageLog.number}') {
		// This is a mobile number, allow +
		input.value = input.value.replace(/[^0-9/+]/g, '');
	} else {
		input.value = input.value.replace(/[^0-9]/g, '');
	}
}

//delete when deployed into sakai since this function will be available
function openWindow(url, title, options)
{
	var win = top.window.open(url, title, options);
	win.focus();
	return win;
}


function make_task_row_open_popup(row) {
		
	var link = document.getElementById(row.id + 'link');
	openWindow(link.href, 'SMSTask', 'resizable=yes,toolbar=no,scrollbars=yes,menubar=yes,width=460,height=500'); 
}
