<!-- Put your Project 2 code here -->
<%@page import="java.sql.ResultSet"%>
<%@page import="java.util.List" import="java.util.Iterator"
	import="helpers.*"%>
<%
boolean nextRows = false;
boolean nextCols = false;
String colClick = request.getParameter("cols");
String rowClick = request.getParameter("rows");
if(colClick != null && colClick.equals("Next 10")) {
   nextCols = true;
} else if(rowClick != null && rowClick.equals("Next 20")) {
   nextRows = true;
}

	List<Sales> sales = AnalyticsHelper.listSales(request, nextCols, nextRows);
	List<String> products = AnalyticsHelper.listProductsAlphabetically();
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
			<th width="50%"><B><%=product%></B></th>
			<%
				}
			%>
			<td>
				<form action="">
					<input type="submit" value="Next 10" name="cols" <%if (numCols < 10) { %>disabled <%} %>>
				</form>
			</td>
		</tr>
		<tr></tr>
		<%
			String currUser = "";
				boolean newrow = false;
				boolean getnext = true;
				Sales s = null;
				for (int i = 0; i < products.size(); ++i) {
					//System.out.println(i);
					if (getnext && salesIter.hasNext()) {
						s = salesIter.next();
						getnext = false;
					}

					String purchaser = s.getPurchaser();//can be a user or state
					double total = s.getPrice();
					String product = s.getProduct();
					if (!currUser.equals(purchaser) && i == 0) {
						currUser = purchaser;
						newrow = true;
						numRows++;
						--i;
					}
					if (newrow) {
		%>
		<tr></tr>
		<td><B><%=purchaser%></B></td>
		<%
			} else if (products.get(i).equals(product)) {
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
					if(i == products.size() - 1 && product == "") {
					   getnext = true;
					}
					
					//Loop back around if there are more sales to print and you
					//are end of the row of products. You must have more sales available,
					//or the current sale you are on must not have been placed in the table yet
					if (i == products.size() - 1
							&& (salesIter.hasNext() || getnext == false)) {
						System.out.println(getnext);
						
						i = -1;
					}
				}
		%>
		<tr>
			<td>
				<form action="">
					<input type="submit" value="Next 20" name="rows" <%if (numRows < 20) { %>disabled <%} %>>
				</form>
			</td>
		</tr>
	</thead>
</table>
<%
	}
%>