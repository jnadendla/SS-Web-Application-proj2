<!-- Put your Project 2 code here -->
<%@page import="java.sql.ResultSet"%>
<%@page
    import="java.util.List"
    import="helpers.*"%>
<%
    List<ResultSet> sales = AnalyticsHelper.listSales(request);
%>