<%@page
	import="java.util.*, org.json.simple.JSONObject, org.json.simple.JSONArray, helpers.*"%>

<%------------- Retrieve new data -------------------%>
<%
	JSONArray data = new JSONArray();
	JSONObject tempDataJSON = new JSONObject(); // store values for individual states
	JSONArray tempDataArray = new JSONArray();

	JSONArray productTotals = new JSONArray();
	JSONObject tempProductJSON = new JSONObject();

	JSONArray purchaserTotals = new JSONArray();
	JSONObject tempPurchaserJSON = new JSONObject();

	List<Sales> sales = AnalyticsHelper
			.listSales(request, false, false);
	List<String> products = AnalyticsHelper
			.listProductsAlphabetically();

	for (int k = 0; k < products.size(); ++k) {
		String product = products.get(k);

		tempProductJSON = new JSONObject();
		tempProductJSON.put("product", product); // add purchaser
		double productTotal = AnalyticsHelper.getProductTotal(request,
				product);
		tempProductJSON.put("total", Double.toString(productTotal));
		
		productTotals.add(tempProductJSON);

	}

	if (!sales.isEmpty() && !products.isEmpty()) {
		//System.out.println("found sales and products");
		Iterator<Sales> salesIter = sales.iterator();

		String currState = "";
		boolean emptyRow = false;

		boolean newrow = false;
		boolean getnext = true;
		Sales s = null;

		int id = 0;

		for (int i = 0; i < products.size(); ++i) {
			//System.out.println(products.size());
			if (getnext && salesIter.hasNext()) {
				s = salesIter.next();
				getnext = false;
			}

			String purchaser = s.getPurchaser();//can be a user or state
			double total = s.getPrice();
			String product = s.getProduct();
			if (!currState.equals(purchaser) && i == 0) {

				currState = purchaser;
				newrow = true;
				--i;
				if (product.equals("")) {
					emptyRow = true;
				} else {
					emptyRow = false;
				}
			}
			if (newrow) {
				tempDataJSON = new JSONObject();
				tempDataJSON.put("state", purchaser); // add state name

				tempDataArray = new JSONArray();

				tempPurchaserJSON = new JSONObject();
				tempPurchaserJSON.put("purchaser", purchaser); // add purchaser

				double purchaserTotal = AnalyticsHelper
						.getPurchaserTotal(request, purchaser);
				tempPurchaserJSON.put("total", Double.toString(purchaserTotal));
				
				purchaserTotals.add(tempPurchaserJSON);

			} else if (products.get(i).equals(product)
					&& currState.equals(purchaser)) {
				getnext = true;

				//System.out.println("adding to json array");
				tempDataArray.add(Double.toString(total));
				id++;

			} else {
				//System.out.println("adding to json array");
				tempDataArray.add("0.0");
				id++;

			}

			newrow = false;
			if (i == products.size() - 1 && product == ""
					&& emptyRow == true) {
				getnext = true;
			}

			//Loop back around if there are more sales to print and you
			//are end of the row of products. You must have more sales available,
			//or the current sale you are on must not have been placed in the table yet
			if (i == products.size() - 1
					&& (salesIter.hasNext() || getnext == false)) {
				//System.out.println(getnext);

				i = -1;
				tempDataJSON.put("values", tempDataArray);
				data.add(tempDataJSON); // add data for current state
			}
		}
		tempDataJSON.put("values", tempDataArray);
		data.add(tempDataJSON); // add data for last state in list
	}

	JSONObject result = new JSONObject();

	result.put("data", data);
	result.put("productTotals", productTotals);
	result.put("purchaserTotals", purchaserTotals);
	out.print(result);
	out.flush();
%>
