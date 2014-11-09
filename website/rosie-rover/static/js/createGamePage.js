var currentGameURLSafeKey = null; 

$(document).ready( function() {
	$('.deleteGame').click( function() {
		$("#deleteGameModal input")[0].value = $(this)[0].name;
	});

	$('#addGoalButton').click( function() {
		$("#addGoalModal input")[0].value = currentGameURLSafeKey;
	});

	$("#returnToWelcomeButton").click( function() {
		window.location.href = "/welcomePage";
	});

	$('.sidebar-link').click(function() {
		// Update the sidebar
		$('.sidebar-link').removeClass('active');
		$(this).addClass('active');
		// Update the list of grades shown in the table.
		currentGameURLSafeKey = $(this).attr('id');
		updateGoalsTable();
		$(".row-offcanvas").removeClass("active");
		updatePageTitle();
	});

	updateGoalsTable = function() {
		// make all of the goals invisible
		$("#goalsTableBody tr").addClass("hidden");
		// make the ones we want visable
		$("."+currentGameURLSafeKey).removeClass("hidden");
	};

	updatePageTitle = function() {
		$("#gameHeader").html(currentGameURLSafeKey);
	};
	
	updateGoalsTable();
});
