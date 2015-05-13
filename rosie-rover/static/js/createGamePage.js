//when we load this page we should see if anyone is logged in
// if no one is logged in, return to the welcome page
if (!sessionStorage.user){
	window.location.href = "/welcomePage";
}
var user = sessionStorage.user;

var currentGameName = null; 

$(document).ready( function() {
	$('.deleteGame').click( function() {
		$("#deleteGameModal input")[0].value = $(this)[0].name;
	});

	$('#addGoalButton').click( function() {
		$("#addGoalModal input")[0].value = currentGameName;
	});

	$("#returnToWelcomeButton").click( function() {
		window.location.href = "/welcomePage";
	});

	$("#returnToControlButton").click( function() {
		window.location.href = "/roverControlPage";
	});

	$("#addGoalsButton").click(function(){
		// retrieve the information that the user entered
		destination = document.getElementById("addGoalsDestination").value;
		point = document.getElementById("addGoalsPoint").value;
			
		// clear the input
		document.getElementById("addGoalsDestination").value = "";
		document.getElementById("addGoalsPoint").value = "";
		
		// check to ensure point is an int
		if (point == parseInt(point)){
		    // point is integer, continue processing
			document.getElementById("goalCreationWarningMessage").innerHTML = "";
		}else{
			// display a warning message
			document.getElementById("goalCreationWarningMessage").innerHTML = "Point value must be an integer.";
		    return;
		}
		
		// add the goal to the table
		// create the necessary parts of a new row in the table
		var rowstrStart = '<tr>';
		var rowstrEnd = '</tr>';
		
		// create the destination table cell
		var deststr1 = '<td><p>';
		var deststr2 = destination;
		var deststr3 = '</p></td>';
		var deststr = deststr1.concat(deststr2).concat(deststr3);
		
		// create the point table cell
		var ptstr1 = '<td><p>';
		var ptstr2 = point;
		var ptstr3 = '</p></td>';
		var ptstr = ptstr1.concat(ptstr2).concat(ptstr3);
		
		// combine them to create a complete table row
		var tableRowString = rowstrStart.concat(deststr).concat(ptstr).concat(rowstrEnd);
		
		// add the row to the table
		$('#tableOfGoalsBody').append($(tableRowString));
		
		// update the database
		dataToSend = {"destination":destination,"pointValue":point,"currentGame":currentGameName};
		$.post("/createNewGoal",dataToSend).done(function(data) {
			// success	
		}).fail(function(jqxhr, textStatus, error){
			console.log("POST Request failed: " + textStatus + ", " + error);
		});
		
	});
	
	updatePageTitle = function(button) {
		$("#gameHeader").html(button.firstChild.nodeValue);
		currentGameName = button.firstChild.nodeValue;
	};
	
	updateGoalsTable = function(button){
		// make sure the warning message isn't displayed
		document.getElementById("goalCreationWarningMessage").innerHTML = "";
		
		// request and then display the goals for the current game
		dataToSend = {"user":user, "game":button.firstChild.nodeValue};
		$.post("/getGoals",dataToSend).done(function(data) {
			data = jQuery.parseJSON( data ); // it returned a string that we need to convert to an object
			
			// remove all of the goals that were in the table
			var tableBody = document.getElementById('tableOfGoalsBody');
		    while (tableBody.hasChildNodes()) {   
		    	tableBody.removeChild(tableBody.firstChild);
		    }

		    // add all of the goals to the table
			for(var i=0; i<data.points.length; i++){
				// create the necessary parts of a new row in the table
				var rowstrStart = '<tr>';
				var rowstrEnd = '</tr>';
				
				// create the destination table cell
				var deststr1 = '<td><p>';
				var deststr2 = data.destinations[i];
				var deststr3 = '</p></td>';
				var deststr = deststr1.concat(deststr2).concat(deststr3);
				
				// create the point table cell
				var ptstr1 = '<td><p>';
				var ptstr2 = data.points[i];
				var ptstr3 = '</p></td>';
				var ptstr = ptstr1.concat(ptstr2).concat(ptstr3);
				
				// combine them to create a complete table row
				var tableRowString = rowstrStart.concat(deststr).concat(ptstr).concat(rowstrEnd);
				
				// add the row to the table
				$('#tableOfGoalsBody').append($(tableRowString));
			}			
		}).fail(function(jqxhr, textStatus, error){
			console.log("POST Request failed: " + textStatus + ", " + error);
		});
	}

	// set a hidden entry on the game creator modal to the current user so if they decide to create a game we will know who it was
	$("#createNewGameModal input")[0].value = user;

	// update the header to show the user's name
	$("#userHeader").html(user);

	// request and then display the current user's games
	dataToSend = {"user":user};
	$.post("/getUserGames",dataToSend).done(function(data) {
		data = jQuery.parseJSON( data ); // it returned a string that we need to convert to an object

		for(var i=0; i<data.games.length; i++){
			var str1 = '<button class="btn btn-default gameButton sidebar-link list-group-item">';
			var str2 = data.games[i];
			var str3 = '</button>';
			var btnStr = str1.concat(str2).concat(str3);
			$('#sidebar-game-buttons').append($(btnStr));
		}
		
		$('.gameButton').click(function() { // this doesn't seem to work unless it is in here.  I guess it only applies to things that were already existing when it was executed?
			// Update the list of goals shown in the table.
			updateGoalsTable(this);
			// update the current game title
			updatePageTitle(this);
		});		
	}).fail(function(jqxhr, textStatus, error){
		console.log("POST Request failed: " + textStatus + ", " + error);
	});
});

