package helpers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

public class AnalyticsHelper {

    public static List<ResultSet> listProducts(HttpServletRequest request) {
        List<ResultSet> sales = new ArrayList<ResultSet>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        String searchFrom ="";
        String categoryFilter = "", searchFilter = "", additionalFilter = "";
        String orderBy = "";
        
        //Get search item
        String display = "";
        try {
            display = request.getParameter("displayFilter");
            if (display != null && !display.isEmpty()) {
                if(display == "customers") {
                   searchFrom = "users AS u, sales AS s, products AS p, categories AS c"; 
                   additionalFilter = "u.id = s.uid AND u.role = \"customer\" AND p.id = S.pid ";
                }
                else if(display == "states") {
                   searchFrom = "users AS u, states t, sales AS s, products AS p, categories AS c ";
                   additionalFilter = "t.id = u.state AND u.id = s.uid AND u.role = \"customer\" AND p.id = s.pid ";
                }
            }
        } catch (Exception e) {
        }
        
        //Get category filter
        try {
            String category = request.getParameter("categoryFilter");
            if (category != null && !category.isEmpty())
                categoryFilter = "AND c.id = p.cid AND c.name = " + category;
        } catch (Exception e) {
        }
        
        //Get total filter
        String filter = null;
        if (categoryFilter.isEmpty()) {
           
        } else {
           filter = " WHERE " + additionalFilter + categoryFilter;
        }
        
        //Get ordering
        try {
           String order = request.getParameter("sortFilter");
           if (order != null && !order.isEmpty())
               if(order == "alphabetical") {
                  if(display == "customers") {
                     orderBy = "u.name";
                  }
                  else if(display == "states")
                     orderBy = "t.name";
               }
               else if(order == "topk") {
                  orderBy = "(s.price * s.quantity) DESC";
               }
       } catch (Exception e) {
       }
        
        try {
            try {
                conn = HelperUtils.connect();
            } catch (Exception e) {
                System.err.println("Internal Server Error. This shouldn't happen.");
                return new ArrayList<ResultSet>();
            }
            stmt = conn.createStatement();
            String query = "SELECT t.name AS name, (s.price * s.quantity) AS total, p.name AS product"
                    + " FROM " + searchFrom + filter + " ORDER BY " + orderBy;
            
            rs = stmt.executeQuery(query);
            return sales;
        } catch (Exception e) {
            System.err.println("Some error happened!<br/>" + e.getLocalizedMessage());
            return new ArrayList<ResultSet>();
        } finally {
            try {
                stmt.close();
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}