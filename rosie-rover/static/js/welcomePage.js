$(document).ready( function() {	
	displayUser();

	$("#logOutButton").click( function(){
		logOut();
	});

	$("#submitLogInButton").click( function(){
		logIn();
	});

	$("#submitCreateNewUserButton").click( function(){
		createNewUser();
	});
	
	$("#proceedButton").click( function() {
		// redirect the user to the rover control page
		window.location.href = "/roverControlPage";
	});
});

function displayUser() { // and choose which of the New User, Log In, and Log Out Buttons are visible
	if(typeof(Storage) !== "undefined") {
		if (sessionStorage.user) { // user is logged in

			document.getElementById("user").innerHTML = "Hello " + sessionStorage.user;

			// log out and proceed buttons are visible
			$("#logOutButton").removeClass("hidden");
			$("#proceedButton").removeClass("hidden");

			// log in and new user buttons are invisible
			$("#logInButton").addClass("hidden");
			$("#newUserButton").addClass("hidden");

		} else { // user is not logged in

			document.getElementById("user").innerHTML = "Hello";

			// log out and proceed buttons are invisible
			$("#logOutButton").addClass("hidden");
			$("#proceedButton").addClass("hidden");

			// log in and new user buttons are visible
			$("#logInButton").removeClass("hidden");
			$("#newUserButton").removeClass("hidden");
		}
	} else {
		document.getElementById("user").innerHTML = "Sorry, your browser does not support web storage...";
	}
}

function logOut() {
	sessionStorage.removeItem("user");
	displayUser();
}

function logIn() {
	// grab the values that the user just entered
	var username = $("#logInUsername").val();
	var password = $("#logInPassword").val();
	
	// create the message to be sent to main
	dataToSend = {username:username, password:password};
	
	// ajax request to main to save the info in the database if the username is a new one
	$.post("/userLogin",dataToSend).done(function(data) {
		data = jQuery.parseJSON( data ); // it returned a string that we need to convert to an object
		if(data.correctPassword){
			// save the current user in the session storage
			sessionStorage.user = username;
			// update the page
			displayUser();	
			// make sure the error message is not displayed
			document.getElementById("warningLabel").innerHTML = "";
		}else{
			// warning that the username/password combo is incorrect
			document.getElementById("warningLabel").innerHTML = "That username/password combination is incorrect.";
		};

	}).fail(function(jqxhr, textStatus, error){
		console.log("POST Request failed: " + textStatus + ", " + error);
	});	
}

function createNewUser() {
	// grab the values that the user just entered
	var username = $("#createNewUserUsername").val();
	var password = $("#createNewUserPassword").val();

	// check that they entered things into both fields
	if (username == "" || password == ""){
		// inform the user that they can't have empty usernames and passwords
		document.getElementById("warningLabel").innerHTML = "Username and password fields cannot be left blank.";
		return;
	}
	
	// make sure the error message is not displayed
	document.getElementById("warningLabel").innerHTML = "";

	// create the message to be sent to main
	dataToSend = {username:username, password:password};

	// ajax request to main to save the info in the database if the username is a new one
	$.post("/createNewUser",dataToSend).done(function(data) {
		data = jQuery.parseJSON( data ); // it returned a string that we need to convert to an object
		if(data.alreadyExists){
			// show a warning that the username is already in use
			document.getElementById("warningLabel").innerHTML = "That username is already in use.";
		}else{
			// save the current user in the session storage if that user didn't already exist
			sessionStorage.user = username;
			// update the page
			displayUser();
		};

	}).fail(function(jqxhr, textStatus, error){
		console.log("POST Request failed: " + textStatus + ", " + error);
	});	
}