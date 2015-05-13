$(document).ready( function() {
	var userToDelete = "";

	$('.deleteGame').click( function() {
		$("#deleteGameModal input")[0].value = $(this)[0].name;
	});

	$('.deleteUser').click( function() {
		$("#deleteUserModal input")[0].value = $(this)[0].name;
	});

	$("#AddAdminButton").click( function() {
		email = $("#AddAdminInput").val();

		// create the message to be sent
		dataToSend = {email:email};

		// ajax request to main to save the info in the database
		$.post("/createNewAmin",dataToSend).done(function(data) {
			// success	
			window.location.href = "/superSecretAdminPage";
		}).fail(function(jqxhr, textStatus, error){
			console.log("POST Request failed: " + textStatus + ", " + error);
		});	
	});

	$('#authSubmitButton').click( function() {
		// get the values from the admin
		startDateTime = $('#authStartDateTime').val();
		endDateTime = $('#authEndDateTime').val();
		userName = $('#authUsername').val();
		
		// send the info to the server to put in the datastore
		dataToSend = {"startDateTime":startDateTime, "endDateTime":endDateTime, "userName":userName};
		$.post("/addUserAuthTime",dataToSend).done(function(data) {
			// we don't need anything returned		
		}).fail(function(jqxhr, textStatus, error){
			console.log("POST Request failed: " + textStatus + ", " + error);
		});
	});
});
