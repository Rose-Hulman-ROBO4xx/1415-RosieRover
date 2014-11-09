var heartBeatTime = 1000;

//ok, so apparently you can't just ask the keyboard which keys are currently pressed, it only shows up as events.  So 
//when a keyboard event happens we need to save the information
var w = 0;
var a = 0;
var s = 0;
var d = 0;
var up = 0;
var down = 0;
var left = 0;
var right = 0;

// same thing for mouse position and if the mouse is clicked
var mouseDown = 0;
var mouseX = 0;
var mouseY = 0;

// keep track of motor speeds
var leftMotor = 0;
var rightMotor = 0;

//keep track of if the lights are flashing and if we've hit the emergency stop
var warning = 0;
var emergency = 0;

$(document).ready( function() {	
	console.log("loaded");

	// Alternate between being an "emergency" button and an "emergency is over" button
	$("#emergencyStopButton").click( function() {
		if($(this).hasClass("Moving")){  
			console.log("Emergency Stop");
			$(this).css('background-color','green');
			$(this).html("Restart");
			$(this).removeClass("Moving");
			emergency = 1;
		} else {
			console.log("Restarted");
			$(this).css('background-color','red');
			$(this).html("STOP!");
			$(this).addClass("Moving");
			emergency = 0;
		}
	});

	// warning lights
	$("#warningLightsAndBuzzerButton").click( function() {
		console.log("Warning Lights and Buzzer");
		warning = 1;
	});

	// go make a game
	$("#createGameButton").click( function() {
		window.location.href = "/createGamePage";
	});

	// return to the welcome page
	$("#disconnectButton").click( function() {
		window.location.href = "/welcomePage";
	});

	// note that the keys were pressed
	$(document).on('keydown', function(event) {
		switch (event.keyCode){
		case 38: // up arrow
			up = 1;
			break;
		case 87: // w
			w = 1;
			break;
		case 39: // right arrow
			right = 1;
			break;
		case 68: // d
			d = 1;
			break;
		case 37: // left arrow
			left = 1;
			break;
		case 65: // a
			a = 1;
			break;
		case 40: // down arrow
			down = 1;
			break;
		case 83: // s
			s = 1;
			break;
		};
	});

	//	note that the keys were released
	$(document).on('keyup', function(event) {	
		switch (event.keyCode){
		case 38: // up arrow
			up = 0;
			break;
		case 87: // w
			w = 0;
			break;
		case 39: // right arrow
			right = 0;
			break;
		case 68: // d
			d = 0;
			break;
		case 37: // left arrow
			left = 0;
			break;
		case 65: // a
			a = 0;
			break;
		case 40: // down arrow
			down = 0;
			break;
		case 83: // s
			s = 0;
			break;
		};
	});

	// remember where the mouse is
	$(document).mousemove(function(event) {
		mouseX = event.pageX;
		mouseY = event.pageY;
	});
	
	// remember if the mouse has been pressed or released
	$(document).mouseup(function() {
	    mouseDown = 0;
	});
	
	$(document).mousedown(function() {
		mouseDown = 1;
	});
	

	// start the heartBeat
	setInterval(function(){heartBeat()}, heartBeatTime);
});

heartBeat = function() {
	// figure out what command we should be sending
	findMotorCommands();

	// create the status message to be send
	// format: STATUS(LeftMotor, RightMotor, FlashingLights, EmergencyStop)
	statusMessage = "STATUS:" + leftMotor.toString() + "," + rightMotor.toString() + "," + warning.toString() + "," + emergency.toString() + ";";
	dataToSend = {status:statusMessage};
	console.log("heartBeat: " + statusMessage);
	
	$.post("/heartBeat",dataToSend).done(function(data) {
		// console.log("Success " + JSON.stringify(data));
	}).fail(function(jqxhr, textStatus, error){
		console.log("POST Request failed: " + textStatus + ", " + error);
	});
};

findMotorCommands = function() {
	//	arrow keys trump all
	
	//	if they are using wasd and up/down/left/right at the same time, don't move the robot
	if( (w || a || s || d) && (up || down || left || right) ) {
		leftMotor = 0;
		rightMotor = 0;
		return;
	}
	
	// if they push opposing keys (ie. up and down, left and right, a and d, w and s), don't move
	if( (up&&down) || (left&&right) || (w&&s) || (a&&d) ) {
		leftMotor = 0;
		rightMotor = 0;
		return;
	}
	
	// move forwards and right
	if ( (up&&right) || (w&&d)){
		leftMotor = 255;
		rightMotor = 100;
		return;
	}
	
	// move forwards and left
	if ( (up&&left) || (w&&a)){
		leftMotor = 100;
		rightMotor = 255;
		return;
	}
	
	// move backwards and right
	if ( (down&&right) || (s&&d)){
		leftMotor = -255;
		rightMotor = -100;
		return;
	}
	
	// move backwards and left
	if ( (down&&left) || (s&&a)){
		leftMotor = -100;
		rightMotor = -255;
		return;
	}
	
	// move forwards
	if ( up || w ){
		leftMotor = 255;
		rightMotor = 255;
		return;
	}
	
	// move backwards
	if ( down || s ){
		leftMotor = -255;
		rightMotor = -255;
		return;
	}
	
	// turn right
	if ( right || d ){
		leftMotor = 255;
		rightMotor = -255;
		return;
	}
	
	// turn left
	if ( left || a ){
		leftMotor = -255;
		rightMotor = 255;
		return;
	}
	
	// so if they're not using the arrow keys, we use mouse position
	// to drive they need to hold the mouse button down
	// cursor position will determine direction and speed
	
	// if they aren't holding down the mouse button or hitting the arrow keys, don't move	
	if(!mouseDown){
		leftMotor = 0;
		rightMotor = 0;
		return;
	}
	
	// find what the x y position is in a -1 to 1 by -1 to 1 "square"
	var width = $(window).width();
	var height = $(window).height();
	var x = 2*mouseX/width - 1;
	var y = -(2*mouseY/height - 1);

	// find theta
	var theta = Math.atan2(y,x) * 180/ 3.1415926535;
	
	// find the distance from center, r
	var r = Math.sqrt(x^2 + y^2);	
	
	// TODO
	// if x and y are sufficiently close to 0, don't do anything
//	var centerLimit = 0.1;
//	if ( r < centerLimit){
//		leftMotor = 0;
//		rightMotor = 0;
//		return;
//	}
	
	// forward
	if (theta > 3*90/4.0 && theta < 5*90/4.0){
		leftMotor = 255;
		rightMotor = 255;
		return;
	}
	
	// forward left
	if (theta > 5*90/4.0 && theta < 7*90/4.0){
		leftMotor = 100;
		rightMotor = 255;
		return;
	}
	
	// left - this one is a bit different since theta ranges from -180 to 180
	if (theta > 7*90/4.0 || theta < -7*90/4.0){
		leftMotor = -255;
		rightMotor = 255;
		return;
	}
	
	// back left
	if (theta > -7*90/4.0 && theta < -5*90/4.0){
		leftMotor = -100;
		rightMotor = -255;
		return;
	}
	
	// back
	if (theta > -5*90/4.0 && theta < -3*90/4.0){
		leftMotor = -255;
		rightMotor = -255;
		return;
	}

	// back right
	if (theta > -3*90/4.0 && theta < -1*90/4.0){
		leftMotor = -255;
		rightMotor = -100;
		return;
	}
	
	// right
	if (theta > -1*90/4.0 && theta < 1*90/4.0){
		leftMotor = -255;
		rightMotor = 255;
		return;
	}
	
	// forward right
	if (theta > 1*90/4.0 && theta < 3*90/4.0){
		leftMotor = 255;
		rightMotor = 100;
		return;
	}
	
};

