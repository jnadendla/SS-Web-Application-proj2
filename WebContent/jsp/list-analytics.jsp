<!-- Put your Project 2 code here -->
<%@page import="java.sql.ResultSet"%>
<%@page import="java.util.List" import="java.util.Iterator"
	import="helpers.*"%>
<%
	boolean nextCols = false;
	boolean runQ = false;
	String colClick = request.getParameter("cols");
	String runClick = request.getParameter("runQ");
	if (colClick != null && colClick.equals("Next 50")) {
		nextCols = true;
	} else if(runClick != null && runClick.equals("Run Query")) {
		runQ = true;
	}

	List<Sales> sales = AnalyticsHelper.listSales(request, runQ, nextCols);
	List<String> products = AnalyticsHelper
			.listProductsAlphabetically();
	if (!sales.isEmpty() && !products.isEmpty()) {
		Iterator<Sales> salesIter = sales.iterator();
		int numCols = products.size();
		int numRows = 0;
%>
<table class="table table-striped" align="center">
	<thead>
		<tr align="center">
			<th></th>
			<%
				//Loop through all the products, and fill up one row at a time
					//This means some columns may have an empty sale, in this case we
					//simply print out 0
					for (int k = 0; k < products.size(); ++k) {
						String product = products.get(k);
			%>
			<th width="50%"><B><%=product + " ("
							+ AnalyticsHelper.getProductTotal(request, product)
							+ ")"%></B></th>
			<%
				}
			%>
			<td>
				<form action="">
					<input type="submit" value="Next 50" name="cols"
						<%if (numCols < 50) {%> disabled <%}%>>
				</form>
				<button onclick="">Refresh</button>
			</td>
		</tr>
		<tr></tr>
		<%
			String currState = "";
				boolean emptyRow = false;
				
				boolean newrow = false;
				boolean getnext = true;
				Sales s = null;
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
						numRows++;
						--i;
						if(product.equals("")){
							emptyRow = true;
						} else {
							emptyRow = false;
						}
					}
					if (newrow) {
		%>
		<tr></tr>
		<td><B><%=purchaser
								+ " ("
								+ AnalyticsHelper.getPurchaserTotal(request,
										purchaser) + ")"%></B></td>
		<%
			} else if (products.get(i).equals(product) && currState.equals(purchaser)) {
						getnext = true;
		%>
		<td><%=total%></td>
		<%
			} else {
		%>
		<td>0.0</td>
		<%
			}

					newrow = false;
					if (i == products.size() - 1 && product == "" && emptyRow == true) {
						getnext = true;
					}

					//Loop back around if there are more sales to print and you
					//are end of the row of products. You must have more sales available,
					//or the current sale you are on must not have been placed in the table yet
					if (i == products.size() - 1
							&& (salesIter.hasNext() || getnext == false)) {
						//System.out.println(getnext);

						i = -1;
					}
				}
		%>
	</thead>
</table>
<%
	}
%>