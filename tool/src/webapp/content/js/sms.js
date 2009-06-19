// Calls a link (used for onclick)
function call_link(link_id) {
		var link = document.getElementById(link_id);
		window.location.href = link.href;
}

// Makes a button call a link (used for InitBlocks)
function make_button_call_link(button_id, link_id) {
	var button = document.getElementById(button_id);
	var link = document.getElementById(link_id);
	button.onclick = function() { window.location.href = link.href;};
}
