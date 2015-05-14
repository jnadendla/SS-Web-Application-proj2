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
        
        //Here we look at where we filter by user or customer and create a string that determines
        //what items we will want to select in the SELECT clause of the query. There is another
        //string that will reflect which tables we need to query from.  
        //If we filter by states there is an additional table to query from (states).  
        //We also create a filter string that contains the string that will
        //be placed in the WHERE clause of the query.
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
        
        //This area determines what to filter the category by. If ALL is selected by
        //the category, we leave the filter and from strings as empty and then they
        //will not affect the query.
        try {
            String category = request.getParameter("categoryFilter");
            if (category != null && !category.isEmpty() && !category.equals("all")) {
               categoryFrom = " , categories AS c";
               categoryFilter = "AND c.id = p.cid AND c.name = '" + category + "' ";
            }
        } catch (Exception e) {
        }
        
        //This simply combines the filter string with a category to filter
        //by if one exists.
        String filter = " WHERE " + additionalFilter + categoryFilter;
        
        //Here is the sort string the will by placed in the ORDER BY clause
        //of the query. Based on if the sort is alphabetical, we then sort by
        //the name and product of the users/states.  If the sort is topk, then
        //we call a helper method to build the rest of the sales list.
        try {
           String order = request.getParameter("sortFilter");
           if (order != null && !order.isEmpty())
               if(order.equals("alphabetical")) {
                  if(display.equals("customers")) {
                     orderBy = "ORDER BY u.name, p.name";
                  }
                  else if(display.equals("states"))
                     orderBy = "ORDER BY t.name, p.name";
               }
               else if(order.equals("topk")) {
                  //User helper method to build list and then return immediately
            	  sales = listByTopK(display, categoryFrom, filter);
            	  return sales;
               }
       } catch (Exception e) {
       }
        
        try {
            try {
            	//aquire connection
                conn = HelperUtils.connect();
            } catch (Exception e) {
                System.err.println("Internal Server Error. This shouldn't happen.");
                return sales;
            }
            stmt = conn.createStatement();
            
            //create the entire query string based of the strings we have created individually
            String query = "SELECT " + select + " FROM " + searchFrom + categoryFrom + filter + orderBy;
            System.out.println(query);
            rs = stmt.executeQuery(query);
            
            //populate list (we cannot return a ResultSet because it's bad programming).
            //Simply fill our list with sales by looping through the result set and getting
            //the information to build sales.
            boolean everyOther = false;
            Sales temp = null;
            while (rs.next()) {
               String user = rs.getString("name");
               double price = rs.getDouble("total");
               String product = rs.getString("product");
               
               //The everyOther aspect is designed so have the sales from the previous loop index
               //and we can check if the current sale is mad by the same user and of the same
               //product.  If so, we just mold these sales into 1 sale.  This is neccessary
               //because users can purchase items multiple times but at different instances, so
               //the database will contain multiple sales of the same product.
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
    
    
    /*
     * Helper method gets the list of sales when the topk sort is selected. This is neccessary
     * because we have to individually get a row at a time. We perfore 2 queries, one to see
     * the orer in which the users/states will appear in a topk approach, and another to
     * get all the sales based on that ordering.
     */
    private static List<Sales> listByTopK(String display, String categoryFrom, String filter) {
    	List<Sales> sales = new ArrayList<Sales>();
    	List<String> topk = new ArrayList<String>();
    	ResultSet rs, order;
        Connection conn = null;
        Statement stmt = null;
    	
    	try {
            try {
                conn = HelperUtils.connect();
            } catch (Exception e) {
                System.err.println("Internal Server Error. This shouldn't happen.");
                return sales;
            }
            stmt = conn.createStatement();
            String query = "";
            if(display.equals("customers"))
            	query = "SELECT u.name AS name FROM sales AS s, users AS u WHERE u.id = s.uid GROUP BY u.name ORDER BY SUM(s.price * s.quantity) DESC";
            else if(display.equals("states"))
            	query = "SELECT t.name AS name FROM sales AS s, users AS u, states AS t WHERE t.id = u.state AND u.id = s.uid GROUP BY t.name ORDER BY SUM(s.price * s.quantity) DESC";
            
            order = stmt.executeQuery(query);
            
            //populate list
            while (order.next()) {
               String user = order.getString("name");
               topk.add(user);
            }
        } catch (Exception e) {
            System.err.println("Some error happened!<br/>" + e.getLocalizedMessage());
            return sales;
        }
    	
    	try {
            try {
                conn = HelperUtils.connect();
            } catch (Exception e) {
                System.err.println("Internal Server Error. This shouldn't happen.");
                return sales;
            }
            stmt = conn.createStatement();
            
            //Loop through all the names ordered by topk and query sales information in topk order
            for(int i=0; i<topk.size(); ++i) {
            	String name = topk.get(i);
            	String nameFilter = "";
            	if(display.equals("customers"))
            		nameFilter = " AND u.name = '" + name + "'";
            	else if(display.equals("states"))
            		nameFilter = " AND t.name = '" + name + "'";
            	
	        	String query = "";
	        	if(display.equals("customers"))
	        		query = "SELECT u.name AS name, (s.price * s.quantity) AS total, p.name AS product "
	        				+ "FROM sales AS s, users AS u, products AS p" + categoryFrom 
	        				+ filter + nameFilter + " ORDER BY p.name";
	        	else if(display.equals("states"))
	        		query = "SELECT t.name AS name, (s.price * s.quantity) AS total, p.name AS product "
	        				+ "FROM sales AS s, users AS u, states AS t, products AS p" + categoryFrom
	        				+  filter + nameFilter + " ORDER BY p.name";
	        	
	        	System.out.println(query);
	            rs = stmt.executeQuery(query);
	            
	            //populate list
	            boolean everyOther = false;
	            Sales temp = null;
	            while (rs.next()) {
	               String user = rs.getString("name");
	               double price = rs.getDouble("total");
	               String product = rs.getString("product");
	               
	               //The everyOther aspect is designed so have the sales from the previous loop index
	               //and we can check if the current sale is mad by the same user and of the same
	               //product.  If so, we just mold these sales into 1 sale.  This is neccessary
	               //because users can purchase items multiple times but at different instances, so
	               //the database will contain multiple sales of the same product.
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
            }
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
    	
    	return sales;
    }

}