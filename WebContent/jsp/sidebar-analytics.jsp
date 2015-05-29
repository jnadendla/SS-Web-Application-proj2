<%@page import="java.util.List" import="helpers.*"%>
<%
boolean nextRows = false;
boolean nextCols = false;
String colClick = request.getParameter("cols");
if(colClick != null && colClick.equals("Next 10")) {
   nextCols = true;
}

String displayFilter = request.getParameter("displayFilter");
if(displayFilter == null) {
	displayFilter = "";
}

String sortFilter = request.getParameter("sortFilter");
if(sortFilter == null) {
	sortFilter = "";
}

String categoryFilter = request.getParameter("categoryFilter");
if(categoryFilter == null) {
	categoryFilter = "";
}

	List<CategoryWithCount> categories = CategoriesHelper
			.listCategories();
%>
<div class="panel panel-default">
	<div class="panel-body">
		<div class="bottom-nav">
			<h4>Options</h4>
			<!-- Put your part 2 code here -->
			<ul class="nav nav-list">
				<form action="analytics" method="GET">
					<li><select name="categoryFilter" <%if(nextCols || nextRows) {%>disabled<%} %>>
							<option value="all">Show All Categories</option>
							<%
									for (CategoryWithCount cwc : categories) {
								%>
							<option value=<%=cwc.getId() %> <% if(categoryFilter.equals(Integer.toString(cwc.getId()))) { %> selected<%} %>><%=cwc.getName()%></option>

							<%
									}
								%>
					</select></li> <br>
					<li><select name="displayFilter" <%if(nextCols || nextRows) {%>disabled<%} %>>
							<option value="customers">Display By</option>
							<option value="customers" <% if(displayFilter.equals("customers")) { %> selected<%} %>>Customers</option>
							<option value="states" <% if(displayFilter.equals("states")) { %> selected<%} %>>States</option>
					</select></li> <br>
					<li><select name="sortFilter" <%if(nextCols || nextRows) {%>disabled<%} %>>
							<option value="alphabetical">Sort By</option>
							<option value="alphabetical" <% if(sortFilter.equals("alphabetical")) { %> selected<%} %>>Alphabetical</option>
							<option value="topk" <% if(sortFilter.equals("topk")) { %> selected<%} %>>Top-K</option>
					</select></li> <br>
					<li><input type="submit" value="Run Query" name="runQ" <%if(nextCols || nextRows) {%>disabled<%} %>></li>
				</form>

			</ul>
		</div>
	</div>
</div>