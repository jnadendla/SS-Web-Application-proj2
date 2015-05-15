<!-- Put your Project 2 code here -->
<%@page import="java.sql.ResultSet"%>
<%@page
    import="java.util.List"
    import="java.util.Iterator"
    import="helpers.*"%>
<%
    List<Sales> sales = AnalyticsHelper.listSales(request);
    List<String> products = AnalyticsHelper.listProductsAlphabetically(request);
if(!sales.isEmpty() && !products.isEmpty()) {
    Iterator<Sales> salesIter = sales.iterator();
%>
<table class="table table-striped" align="center">
    <thead>
        <tr align="center">
            <th></th>
            <%
            for(int k=0; k < products.size(); ++k) {
               String product = products.get(k);
            %>
                <th width="50%"><B><%=product %></B></th>
            <%
            }
            %>
        </tr>
        <tr></tr>
        <%
        String currUser = "";
        boolean newrow = false;
        boolean getnext = true;
        Sales s = null;
        for(int i=0; i < products.size(); ++i) {
           if(getnext && salesIter.hasNext()) {
              s = salesIter.next();
              getnext = false;
           }

           String user = s.getUser();
           double total = s.getPrice();
           String product = s.getProduct();
           if(!currUser.equals(user) && i == 0) {
              currUser = user;
              newrow = true;
           }   
           if(newrow) {
           %>
              <tr></tr>
              <td><%=user %></td>
           <%
           } else if(products.get(i).equals(product)) {
        	   getnext = true;
           %>
              <td><%=total %></td>
           <%
           } else {
           %>
           	  <td>0.0</td>
        <%
           }
           newrow = false;
           if(i == products.size() - 1 && salesIter.hasNext()) {
        	   i = -1;
           }
        }
        %>
    </thead>
</table>
<%
}
%>