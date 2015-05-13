// when we load this page we should see if anyone is logged in
// if no one is logged in, return to the welcome page
if (!sessionStorage.user){
	window.location.href = "/welcomePage";
}
var user = sessionStorage.user;

// store a variable to keep track of if the user is authorized
userAuthStatus = false;

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

//same thing for if an arrow button is pressed
var upButton = 0;
var downButton = 0;
var leftButton = 0;
var rightButton = 0;

//keep track of motor speeds
var leftMotor = 90; // 180/2 = 90
var rightMotor = 90;

//keep track of if the lights are flashing and if we've hit the emergency stop
var warning = 0;
var emergency = 0;
var fire = 0;
var ignoreus = 0; // ignore ultra sound

// keep track of how to move the turret
var tilt = 0;
var pan = 0;

// used to remember last gps locations
// initializes them to Moench
var GPSx = -87.323788;
var GPSy = 39.483414;

// used to remember the battery voltages
var phoneBattery = 1;
var robotBattery = 1;

$(document).ready( function() {	
	$(".goalTable").addClass("hidden");
	setLocationStar();
	
	setInterval(updateServerVars, 250);

	// if the window is resized we need to recalculate where the location star should be
	$(window).resize(function() {
		setLocationStar();
	});

	$("#PickedAGame").click( function() {
		$(".goalTable").addClass("hidden"); 
		var gameName = $(".SelectNewGame :selected").text();
		$("." + gameName).removeClass("hidden");
	});

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
	
	// fire
	$("#fireDartButton").click( function() {
		console.log("Fire");
		fire = 1; 
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

	// rover arrow buttons pressed
	$("#downRover").mousedown(function(){
		downButton = 1; 
	});
	$("#upRover").mousedown(function(){
		upButton = 1; 
	});
	$("#rightRover").mousedown(function(){
		rightButton = 1; 
	});
	$("#leftRover").mousedown(function(){
		leftButton = 1;
	});
	
	// rover control buttons released
	$("#downRover").mouseup(function(){
		downButton = 0; 
	});
	$("#upRover").mouseup(function(){
		upButton = 0;
	});
	$("#rightRover").mouseup(function(){
		rightButton = 0; 
	});
	$("#leftRover").mouseup(function(){
		leftButton = 0;
	});
	
	// control the turret
	$("#downTurret").mousedown(function(){
		tilt = -1; 
	});
	$("#upTurret").mousedown(function(){
		tilt = 1;
	});
	$("#rightTurret").mousedown(function(){
		pan = 1;
	});
	$("#leftTurret").mousedown(function(){
		pan = -1;
	});

});

updateServerVars = function() {
	checkControlAuthorizationStatus();
	
	// check to see if the user is allowed to control the rover
	if (!userAuthStatus){
		return;
	}
	
	findMotorCommands();
	
	dataToSend = {warning:warning,
					emergency:emergency,
					fire:fire,
					ignoreus:ignoreus,
					leftMotor:leftMotor,
					rightMotor:rightMotor,
					pan:pan,
					tilt:tilt};
	
	console.log(dataToSend);
	
	$.post("/updateComVars",dataToSend).done(function(data) {
		console.log("Success " + JSON.stringify(data));
		data = jQuery.parseJSON( data ); // it returned a string that we need to convert to an object

		// set fire to 0
		fire = 0;
		
		// we have GPS locations, update the star location
		GPSx = data.GPSx;
		GPSy = data.GPSy;	
		setLocationStar();
		
		// we have battery information, update the battery bars
		phoneBattery = data.phoneBatteryLife;
		robotBattery = data.robotBatteryLife;
		updateBatteryVoltages();
		
		// decide if we are supposed to display the video or not
		if(data.displayVideo){
			$("#RoverImage").addClass("hidden");
			// the controller broke, so we no longer hide the video stream. TODO fix the controller, then uncomment the line below and delete the one above
			//$("#RoverImage").removeClass("hidden");
		} else {
			$("#RoverImage").addClass("hidden");
		}
		
	}).fail(function(jqxhr, textStatus, error){
		console.log("POST Request failed: " + textStatus + ", " + error);
	});
	
	pan = 0;
	tilt = 0;
}

findMotorCommands = function() {
	// get the value from the slider and convert it to a decimal
	speedModifier = document.getElementById("speedSlider").value / 100;
	
	// check if we're doing stairs
	if (document.getElementById("stairCheckBox").checked){
		ignoreus = 1;
		// if we're ignoring the ultra sonic, cap the speed at 50%
		speedModifier = min(0.5, speedModifier);
	} else {
		ignoreus = 0;
	}
		
	// ask Brandon what range he wants sent
	fullReverse = 0;
	noMove = 90;
	fullForward = 180;
	
	// send no movement if the emergency button is pressed
	if(emergency == 1){
		leftMotor = noMove;
		rightMotor = noMove;
		return;
	}
	
	// modify the values based on the speed slider
	reverse = Math.floor(noMove - (noMove - fullReverse)*speedModifier);
	slowReverse = Math.floor(noMove - (noMove - fullReverse)*speedModifier/2);
	forward = Math.ceil(noMove + (fullForward - noMove)*speedModifier);
	slowForward = Math.ceil(noMove + (fullForward - noMove)*speedModifier/2);
	
	//	arrow buttons trump all
	// forward
	if (upButton){
		leftMotor = forward;
		rightMotor = forward;
		return;
	}

	// left
	if (leftButton){
		leftMotor = reverse;
		rightMotor = forward;
		return;
	}

	// back
	if (downButton){
		leftMotor = reverse;
		rightMotor = reverse;
		return;
	}

	// right
	if (rightButton){
		leftMotor = forward;
		rightMotor = reverse;
		return;
	}

	// see if the user is using the arrow keys
	
	//	if they are using wasd and up/down/left/right at the same time, don't move the robot
	if( (w || a || s || d) && (up || down || left || right) ) {
		leftMotor = noMove;
		rightMotor = noMove;
		return;
	}

	// if they push opposing keys (ie. up and down, left and right, a and d, w and s), don't move
	if( (up&&down) || (left&&right) || (w&&s) || (a&&d) ) {
		leftMotor = noMove;
		rightMotor = noMove;
		return;
	}

	// move forwards and right
	if ( (up&&right) || (w&&d)){
		leftMotor = forward;
		rightMotor = slowForward;
		return;
	}

	// move forwards and left
	if ( (up&&left) || (w&&a)){
		leftMotor = slowForward;
		rightMotor = forward;
		return;
	}

	// move backwards and right
	if ( (down&&right) || (s&&d)){
		leftMotor = reverse;
		rightMotor = slowReverse;
		return;
	}

	// move backwards and left
	if ( (down&&left) || (s&&a)){
		leftMotor = slowReverse;
		rightMotor = reverse;
		return;
	}

	// move forwards
	if ( up || w ){
		leftMotor = forward;
		rightMotor = forward;
		return;
	}

	// move backwards
	if ( down || s ){
		leftMotor = reverse;
		rightMotor = reverse;
		return;
	}

	// turn right
	if ( right || d ){
		leftMotor = forward;
		rightMotor = reverse;
		return;
	}

	// turn left
	if ( left || a ){
		leftMotor = reverse;
		rightMotor = forward;
		return;
	}
	
	// nothing is being pressed
	leftMotor = noMove;
	rightMotor = noMove;
};

setLocationStar = function() {
	// bottom left
	// 39.479025, -87.332176
	
	// top right
	// 39.486812, -87.313785
	
	// GPS coordinates
	var GPSxmin = -87.332176;
	var GPSxmax = -87.313785;
	var GPSymin = 39.486812;
	var GPSymax = 39.479025;
	
	// get the screen coordinates of the map
	var mapCoords = $("#CampusMap").offset();
	// the top and left location are given from the edge of the padding
	var leftPadding = parseInt($("#CampusMap").css("padding-left"));
	var topPadding = parseInt($("#CampusMap").css("padding-top"));
	var xScreenMax = mapCoords.left + $("#CampusMap").width() + leftPadding;
	var xScreenMin = mapCoords.left + leftPadding;
	var yScreenMax = mapCoords.top + $("#CampusMap").height() + topPadding;
	var yScreenMin = mapCoords.top + topPadding;	

	// convert from GPS coordinates to screen coordinates
	var xScreen = (xScreenMax-xScreenMin) * (GPSx-GPSxmin)/(GPSxmax-GPSxmin) + xScreenMin;
	var yScreen = (yScreenMax-yScreenMin) * (GPSy-GPSymin)/(GPSymax-GPSymin) + yScreenMin; 

	// limit it to be somewhere on the map
	xScreen = max(yScreenMin, min(xScreenMax, xScreen));
	yScreen = min(yScreenMin, max(yScreenMax, yScreen));

	$("#locationStar").offset({ top: yScreen, left: xScreen });
}

updateBatteryVoltages = function(){
	var robotBatteryVoltageString = robotBattery*100 + "%";
	var phoneBatteryVoltageString = phoneBattery*100 + "%";
	$("#robotBatteryVoltage").css('width',robotBatteryVoltageString);
	$("#phoneBatteryVoltage").css('width',phoneBatteryVoltageString);
}

max = function(x,y){
	if(x>y){ return x;}
	return y;
}

min = function(x,y){
	if(x<y){ return x;}
	return y;
}

checkControlAuthorizationStatus = function(){
	dataToSend = {userName:user};
	
	$.post("/checkUserAuthStatus",dataToSend).done(function(data) {
		console.log("Success " + JSON.stringify(data));
		data = jQuery.parseJSON( data ); // it returned a string that we need to convert to an object
		
		// decide which status string we should display
		if(data.userAuthStatus == "true"){
			console.log("authorized");
			$("#authorizationStatusIndicator_authorized").removeClass("hidden");
			$("#authorizationStatusIndicator_notAuthorized").addClass("hidden");
			userAuthStatus = true;
		} else {
			console.log("not authorized");
			$("#authorizationStatusIndicator_authorized").addClass("hidden");
			$("#authorizationStatusIndicator_notAuthorized").removeClass("hidden");
			userAuthStatus = false;
		}	
	}).fail(function(jqxhr, textStatus, error){
		console.log("POST Request failed: " + textStatus + ", " + error);
	});
}