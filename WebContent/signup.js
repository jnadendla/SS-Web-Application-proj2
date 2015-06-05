////////////// PROJECT 3 CODE ////////////////////////

$(function(){
	$(document).on("submit", "#signup-form", function(e){
		e.preventDefault();
		
		document.getElementById("nameError").innerHTML = "";
		document.getElementById("ageError").innerHTML = "";
	if($("#name").val() == "" || $("#name").val() == null) {
		document.getElementById("nameError").innerHTML = "Name not provided.";
		$("#nameError").css("color", "red");
	}
	if($("#age").val() == "" || $("#age").val() == null) {
		document.getElementById("ageError").innerHTML = "Age not provided.";
		$("#ageError").css("color", "red");
	}
	
	console.log($("#name").val())
	return true;
	})
});