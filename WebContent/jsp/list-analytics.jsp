<!-- Put your Project 2 code here -->
<%@page import="java.sql.ResultSet"%>
<%@page
    import="java.util.List"
    import="helpers.*"%>
<%
    List<Sales> sales = AnalyticsHelper.listSales(request);
if(!sales.isEmpty()) {
   out.print("entered");
%>
<table align="center">
    <thead>
        <tr align="center">
            <th width="20%"><B>Product Name</B></th>
            <th width="20%"><B>SKU</B></th>
            <th width="20%"><B>Category Name</B></th>
            <th width="20%"><B>Price</B></th>
            <th
                width="20%"
                colspan="2"><B>Operations</B></th>
        </tr>
        <tr></tr>
        <%
        String currUser = "";
        boolean newrow = false;
        for(int i=0; i < sales.size(); ++i) {
           Sales s = sales.get(i);
           String user = s.getUser();
           double total = s.getPrice();
           String product = s.getProduct();
           if(!currUser.equals(user)) {
              currUser = user;
              newrow = true;
           }   
           if(newrow) {
           %>
               <tr></tr>
               <td><%=user %></td>
           <%
           }
           %>
           <td><%=total %></td>
           <td><%=product %></td>
        <%
           newrow = false;
        }
        %>
    </thead>
</table>
<%
}
%>