

function refresh(){

$.ajax({
	  type: 'POST',
	  url: "process_ajax_refresh.jsp",
	  beforeSend:function(){
		//Update Stats
		$('#status').html('Request Sent');
	  },
	  success:function(result){
	  
	  var response = $.parseJSON(result);
	  var data = response.data;
	  console.log(data);
	  
	  for( i = 0; i < data.length; i++) { // loop through all states
		  var stateJSON = data[i];
		  
		  for(j = 0; j < stateJSON.values.length; j++) {
			  
			  console.log(stateJSON.state+j);
			  var state = stateJSON.state.replace(/\s+/g, '');
			  
			  //var price = $(state+j).html();
			  var price = document.getElementById(state+j).innerHTML;
			  console.log(price);
			  
			  if(price != stateJSON.values[j]) {
				  console.log("old: " + price + ", new: " + data[j]);
				  document.getElementById(state+j).innerHTML = stateJSON.values[j];
				  $("#"+state+j).css("color", "red");
			  } else {
				  $("#"+state+j).css("color", "black");
			  }
		  }
	  }
	  
	  },
	  error:function(){
		// Failed request
		$('#status').html('Oops! Error.');
	  }
	});
	
}