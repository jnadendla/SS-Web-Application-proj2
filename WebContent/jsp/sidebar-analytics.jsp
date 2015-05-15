<%@page import="java.util.List" import="helpers.*"%>
<%
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
					<li><select name="categoryFilter">
							<option value="all">Show All Categories</option>
							<%
									for (CategoryWithCount cwc : categories) {
								%>
							<option value=<%=cwc.getId() %>><%=cwc.getName()%></option>

							<%
									}
								%>
					</select></li>
					<br>
					<li><select name="displayFilter">
							<option value="customers">Display By</option>
							<option value="customers">Customers</option>
							<option value="states">States</option>
					</select></li>
					<br>
					<li><select name="sortFilter">
							<option value="alphabetical">Sort By</option>
							<option value="alphabetical">Alphabetical</option>
							<option value="topk">Top-K</option>
					</select></li>
					<br>
					<li><input type="submit" value="Run Query"></li>
				</form>

			</ul>
		</div>
	</div>
</div>