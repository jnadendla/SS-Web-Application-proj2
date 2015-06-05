////////////// PROJECT 3 CODE ////////////////////////

function refresh() {

	var params = new Array();
	(window.onpopstate = function() {
		var match, pl = /\+/g, // Regex for replacing addition symbol with a
		// space
		search = /([^&=]+)=?([^&]*)/g, decode = function(s) {
			return decodeURIComponent(s.replace(pl, " "));
		}, query = window.location.search.substring(1);

		while (match = search.exec(query)) {
			params.push(decode(match[1]) + "=" + decode(match[2]));
		}
	})();

	console.log(params);

	$
			.ajax({
				type : 'POST',
				url : "process_ajax_refresh.jsp?" + params.join("&"),
				beforeSend : function() {
					// Update Stats
					$('#status').html('Request Sent');
				},
				success : function(result) {

					var response = $.parseJSON(result);

					var data = response.data;
					var productTotals = response.productTotals;
					var purchaserTotals = response.purchaserTotals;

					console.log(data);
					console.log(productTotals);
					console.log(purchaserTotals);

					for (i = 0; i < data.length; i++) { // loop through all
						// states
						var stateJSON = data[i];

						for (j = 0; j < stateJSON.values.length; j++) {

							var state = stateJSON.state.replace(/\s+/g, '');

							// var price = $(state+j).html();
							var price = document.getElementById(state + j).innerHTML;

							if (price != stateJSON.values[j]) {
								document.getElementById(state + j).innerHTML = stateJSON.values[j];
								$("#" + state + j).css("color", "red");
							} else {
								$("#" + state + j).css("color", "black");
							}
						}
					}

					for (i = 0; i < productTotals.length; i++) {
						var product = productTotals[i].product.replace(/\s+/g,
								'');

						var oldHeader = document.getElementById(product).innerHTML;
						var newHeader = productTotals[i].product + " ("
								+ productTotals[i].total + ")";

						if (oldHeader != newHeader) {
							document.getElementById(product).innerHTML = newHeader;
							$("#" + product).css("color", "red");
						} else {
							$("#" + product).css("color", "black");
						}
					}

					for (i = 0; i < purchaserTotals.length; i++) {
						var purchaser = purchaserTotals[i].purchaser.replace(/\s+/g,
								'');

						var oldHeader = document.getElementById(purchaser).innerHTML;
						var newHeader = purchaserTotals[i].purchaser + " ("
								+ purchaserTotals[i].total + ")";

						if (oldHeader != newHeader) {
							document.getElementById(purchaser).innerHTML = newHeader;
							$("#" + purchaser).css("color", "red");
						} else {
							$("#" + purchaser).css("color", "black");
						}
					}

				},
				error : function() {
					// Failed request
					$('#status').html('Oops! Error.');
				}
			});

}