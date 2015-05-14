package helpers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

public class AnalyticsHelper {

    public static List<Sales> listSales(HttpServletRequest request) {
        List<Sales> sales = new ArrayList<Sales>();
        ResultSet rs;
        Connection conn = null;
        Statement stmt = null;
        String select = "";
        String searchFrom ="", categoryFrom = "";
        String categoryFilter = "", additionalFilter = "";
        String orderBy = "";
        
        //Get search item
        String display = "";
        try {
            display = request.getParameter("displayFilter");
            if (display.equals("")) {
               return sales;
            }
            if(display.equals("customers")) {
               select = "u.name AS name, (s.price * s.quantity) AS total, p.name AS product";
               searchFrom = "users AS u, sales AS s, products AS p"; 
               additionalFilter = "u.id = s.uid AND u.role = 'customer' AND p.id = s.pid ";
            }
            else if(display.equals("states")) {
               select = "t.name AS name, (s.price * s.quantity) AS total, p.name AS product";
               searchFrom = "users AS u, states t, sales AS s, products AS p";
               additionalFilter = "t.id = u.state AND u.id = s.uid AND u.role = 'customer' AND p.id = s.pid ";
            }
        } catch (Exception e) {
        }
        
        //Get category filter
        try {
            String category = request.getParameter("categoryFilter");
            if (category != null && !category.isEmpty() && !category.equals("all")) {
               categoryFrom = " , categories AS c";
               categoryFilter = "AND c.id = p.cid AND c.name = " + category;
            }
        } catch (Exception e) {
        }
        
        //Get total filter
        String filter = " WHERE " + additionalFilter + categoryFilter;
        
        //Get ordering
        try {
           String order = request.getParameter("sortFilter");
           if (order != null && !order.isEmpty())
               if(order.equals("alphabetical")) {
                  if(display.equals("customers")) {
                     orderBy = "u.name, p.name";
                  }
                  else if(display.equals("states"))
                     orderBy = "t.name, p.name";
               }
               else if(order.equals("topk")) {
                  if(display.equals("customers")) {
                     String subQuery = "(SELECT SUM(s.price * s.quantity) AS sm FROM sales AS s, users AS u WHERE u.id = s.uid GROUP BY u.name)";
                     orderBy = " GROUP BY u.name, s.price, s.quantity, p.name ORDER BY SUM(s.price * s.quantity) DESC, p.name";
                  }
                  else if(display.equals("states")) {
                     String subQuery = "(SELECT SUM(s.price * s.quantity) AS sm FROM sales AS s, users AS u WHERE u.id = s.uid GROUP BY u.name)";
                     orderBy = " GROUP BY t.name, s.price, s.quantity, p.name ORDER BY t.name, SUM(s.price * s.quantity) DESC, p.name";
                  }

               }
       } catch (Exception e) {
       }
        
        try {
            try {
                conn = HelperUtils.connect();
            } catch (Exception e) {
                System.err.println("Internal Server Error. This shouldn't happen.");
                return sales;
            }
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT SUM(s.price * s.quantity) AS sm FROM sales AS s, users AS u WHERE u.id = s.uid GROUP BY u.name ORDER BY SUM(s.price * s.quantity) DESC");
            while(rs.next()) {
               double total = rs.getDouble("sm");
               System.out.println(total);
            }
            
            String query = "SELECT " + select + " FROM " + searchFrom + categoryFrom + filter + orderBy;
            System.out.println(query);
            rs = stmt.executeQuery(query);
            
            //populate list
            boolean everyOther = false;
            Sales temp = null;
            while (rs.next()) {
               String user = rs.getString("name");
               double price = rs.getDouble("total");
               String product = rs.getString("product");
               
               if(everyOther) {
                  if(temp.getUser().equals(user) && temp.getProduct().equals(product)) {
                     String tempUser = temp.getUser();
                     double tempPrice = temp.getPrice() + price;
                     String tempProduct = temp.getProduct();
                     temp = new Sales(tempUser, tempPrice, tempProduct);
                  }
                  else {   
                     sales.add(temp);
                     temp = new Sales(user, price, product);
                  }
               }
               else {
                  everyOther = true;
                  temp = new Sales(user, price, product);
               }
            }
            return sales;
        } catch (Exception e) {
            System.err.println("Some error happened!<br/>" + e.getLocalizedMessage());
            return sales;
        } finally {
            try {
                stmt.close();
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static List<String> listProductsAlphabetically() {
       List<String> products = new ArrayList<String>();
       ResultSet rs;
       Connection conn = null;
       Statement stmt = null;
       
       try {
          try {
              conn = HelperUtils.connect();
          } catch (Exception e) {
              System.err.println("Internal Server Error. This shouldn't happen.");
              return products;
          }
          stmt = conn.createStatement();
          String query = "SELECT name FROM products ORDER BY name";
          rs = stmt.executeQuery(query);
          
          //populate list
          while (rs.next()) {
             String product = rs.getString("name");
             products.add(product);
          }
          return products;
      } catch (Exception e) {
          System.err.println("Some error happened!<br/>" + e.getLocalizedMessage());
          return products;
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