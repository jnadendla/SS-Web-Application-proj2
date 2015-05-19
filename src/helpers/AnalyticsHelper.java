package helpers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class AnalyticsHelper {
    private static HttpSession session;
    private static int rowOffset = 0;
    private static int colOffset = 0;

    public static List<Sales> listSales(HttpServletRequest request, boolean nextCols, boolean nextRows) {
        List<Sales> sales = new ArrayList<Sales>();
        List<String> names = new ArrayList<String>();
        ResultSet rs;
        Connection conn = null;
        Statement stmt = null;
        String select = "";
        String searchFrom ="", categoryFrom = "";
        String categoryFilter = "", additionalFilter = "", category = "";
        String orderBy = "";
        
        //When next button is clicked, increment the offsets
        if(nextCols)
           colOffset += 10;
        else if(nextRows)
           rowOffset += 20;
        else {
           //New data means we start at offset 0
           rowOffset = 0;
           colOffset = 0;
        }

        String limitRows = "LIMIT 20 OFFSET " + rowOffset;
        String limitCols = " LIMIT 10 OFFSET " + colOffset;
        
        //If the RUN QUERY button has been pressed this should capture the new
        //filter option it has selected, but if nothing new has been selected,
        //we use the previous filter data.
        session = request.getSession();
        String d = request.getParameter("displayFilter");
        String c = request.getParameter("categoryFilter");
        String o = request.getParameter("sortFilter");
        if(d != null && !d.equals("") 
           && o != null && !o.equals("")) {
           session.setAttribute("displayFilter", d);
           session.setAttribute("categoryFilter", c);
           session.setAttribute("sortFilter", o);
        }       
                
        //This area determines what to filter the category by. If ALL is selected by
        //the category, we leave the filter and from strings as empty and then they
        //will not affect the query.
        try {
            category = (String) session.getAttribute("categoryFilter");
            if (category != null && !category.isEmpty() && !category.equals("all")) {
               categoryFrom = ", categories AS c";
               categoryFilter = "AND p.cid = c.id AND c.id = " + category + " ";
            }
        } catch (Exception e) {
        }
        
        //Here we look at where we filter by user or customer and create a string that determines
        //what items we will want to select in the SELECT clause of the query. There is another
        //string that will reflect which tables we need to query from.  
        //If we filter by states there is an additional table to query from (states).  
        //be placed in the WHERE clause of the query.
        String display = "";
        try {
            display = (String) session.getAttribute("displayFilter");
            if (display == null || display.equals("")) {
               return sales;
            }
            if(display.equals("customers") || display.equals("")) {
               select = "u.name AS name, (s.price * s.quantity) AS total, p.name AS product";
               searchFrom = "users AS u, sales AS s, (SELECT * FROM products AS n ORDER BY n.name" + limitCols + ") p"; 
               additionalFilter = "u.id = s.uid AND u.role = 'customer' AND p.id = s.pid ";
    
            }
            else if(display.equals("states")) {
               select = "t.name AS name, (s.price * s.quantity) AS total, p.name AS product";
               searchFrom = "users AS u, states t, sales AS s, (SELECT * FROM products AS n ORDER BY n.name" + limitCols + ") p";
               additionalFilter = "t.id = u.state AND u.id = s.uid AND u.role = 'customer' AND p.id = s.pid ";
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
           String order = (String) session.getAttribute("sortFilter");
           if (order != null && !order.isEmpty())
               if(order.equals("alphabetical") || order.equals("")) {
                  if(display.equals("customers")) {
                     orderBy = "ORDER BY u.name, p.name";
                  }
                  else if(display.equals("states"))
                     orderBy = "ORDER BY t.name, p.name";
               }
               else if(order.equals("topk")) {
                  //User helper method to build list and then return immediately
            	  sales = listByTopK(display, categoryFrom, category, filter);//////TOPK ORDER HAPPENS IN A SEPARATE FUNCIONT CALLED HERE
            	  return sales;
               }
       } catch (Exception e) {
       }
        
        
        try {
            try {
            	//acquire connection
                conn = HelperUtils.connect();
            } catch (Exception e) {
                System.err.println("Internal Server Error. This shouldn't happen.");
                return sales;
            }
            stmt = conn.createStatement();

            //Get the aplphabetical ordering or users/states, if you want to LIMIT and
            //OFFSET the number of users, here is where you do that
            /////////////QUERY NAME ORDERING////////////////////////////
            String query = "";
            String name = "";
            if(display.equals("customers")||display.equals("")) {
            	// NOTE - current limit seems to be applied to the number of sales
                query = "SELECT u.name AS name FROM sales AS s, users AS u, products AS p"
                         +  " WHERE " + "p.id = s.pid AND u.id = s.uid " 
                         +  "GROUP BY u.name ORDER BY u.name " + limitRows;
                name = "u.name";
            }
            else if(display.equals("states")) {
                query = "SELECT t.name AS name FROM sales AS s, users AS u, states AS t, products AS p"
                         + " WHERE " + "p.id = s.pid AND t.id = u.state AND u.id = s.uid "
                         + "GROUP BY t.name ORDER BY t.name " + limitRows;
                name = "t.name";
            }
            
            System.out.println(query);
            rs = stmt.executeQuery(query);     
            //populate list
            while (rs.next()) {
               String purchaser = rs.getString("name");
               names.add(purchaser);
            }
            
            ///////////////////////////////////////////////////////
            //////////SEPARATE QUERY//////////////////////////////
            //For each user/state we get the products they have bought
            for(int i=0; i < names.size(); ++i) {
            
               //create the entire query string based of the strings we have created individually
               //If you want to LIMIT and OFFSET the number of products, here is where you do that
               query = "SELECT " + select + " FROM " + searchFrom + categoryFrom + filter + 
                       "AND " + name + " = '" + names.get(i) + "' " + orderBy;
               System.out.println(query);
               rs = stmt.executeQuery(query);
               
               if(!rs.isBeforeFirst()) {
                  Sales s = new Sales(names.get(i), 0.0, "");
                  sales.add(s);
                  continue;
               }
               
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
                     if(temp.getPurchaser().equals(user) && temp.getProduct().equals(product)) {
                        String tempUser = temp.getPurchaser();
                        double tempPrice = temp.getPrice() + price;
                        String tempProduct = temp.getProduct();
                        temp = new Sales(tempUser, tempPrice, tempProduct);
                        if(rs.isLast())//make sure to add item it is the last one
                           sales.add(temp);
                     }
                     else {   
                        sales.add(temp);
                        temp = new Sales(user, price, product);
                        if(rs.isLast())
                       	 sales.add(temp);
                     }
                  }
                  else {
                     everyOther = true;
                     temp = new Sales(user, price, product);
                     if(rs.isLast())//make sure to add last item
                        sales.add(temp);
                  }
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
       String categoryFilter = "";
       String limitCols = "LIMIT 10 OFFSET " + colOffset;
       
       try {
          String category = (String) session.getAttribute("categoryFilter");
          if(category == null || category.equals(""))
             return products;
          
          if(category != null && !category.isEmpty() && !category.equals("all")) {
       	   categoryFilter = ", categories AS c WHERE p.cid = c.id AND c.id = " + category + "";
          }
       } catch(Exception e) {}
       
       try {
          try {
              conn = HelperUtils.connect();
          } catch (Exception e) {
              System.err.println("Internal Server Error. This shouldn't happen.");
              return products;
          }
          
          stmt = conn.createStatement();
          String query = "SELECT p.name AS name FROM products AS p" + categoryFilter + " ORDER BY p.name " + limitCols;
          System.out.println("Product Query: " + query);
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
    private static List<Sales> listByTopK(String display, String categoryFrom, String category, String filter) {
    	List<Sales> sales = new ArrayList<Sales>();
    	List<String> topk = new ArrayList<String>();
    	ResultSet rs, order;
        Connection conn = null;
        Statement stmt = null;
        String limitRows = " LIMIT 20 OFFSET " + rowOffset;
        String limitCols = " LIMIT 10 OFFSET " + colOffset;
        String products;
    	
    	try {
            try {
                conn = HelperUtils.connect();
            } catch (Exception e) {
                System.err.println("Internal Server Error. This shouldn't happen.");
                return sales;
            }
            stmt = conn.createStatement();
            String query = "";
            
            String categoryFilter = "";
            String productsFrom = "";
            if(!category.equals("all")) {
               categoryFilter = "AND s.pid = p.id AND p.cid = c.id AND c.id = '" + category + "' ";
               productsFrom = ", products AS p";
            }
            
            products = ", (SELECT * FROM products AS n ORDER BY n.name" + limitCols + ") p";

            //Get the topk ordering or users/states
            if(display.equals("customers") || display.equals(""))
            	query = "SELECT u.name AS name FROM sales AS s, users AS u" + productsFrom
            	         + categoryFrom + " WHERE u.id = s.uid " + categoryFilter 
            	         + "GROUP BY u.name ORDER BY SUM(s.price * s.quantity) DESC" + limitRows;
            else if(display.equals("states"))
            	query = "SELECT t.name AS name FROM sales AS s, users AS u, states AS t" + productsFrom
            	         + categoryFrom + " WHERE t.id = u.state AND u.id = s.uid " + categoryFilter 
            	         + "GROUP BY t.name ORDER BY SUM(s.price * s.quantity) DESC" + limitRows;
            
            System.out.println(query);
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
            	if(display.equals("customers") || display.equals(""))
            		nameFilter = " AND u.name = '" + name + "'";
            	else if(display.equals("states"))
            		nameFilter = " AND t.name = '" + name + "'";
            	
	        	String query = "";
	        	if(display.equals("customers") || display.equals("")) {
	        		query = "SELECT u.name AS name, (s.price * s.quantity) AS total, p.name AS product "
	        				+ "FROM sales AS s, users AS u" + products + categoryFrom 
	        				+ filter + nameFilter + " ORDER BY p.name";
	        	}
	        	else if(display.equals("states")) {
	        		query = "SELECT t.name AS name, (s.price * s.quantity) AS total, p.name AS product "
	        				+ "FROM sales AS s, users AS u, states AS t" + products + categoryFrom
	        				+  filter + nameFilter + " ORDER BY p.name";
	        	}
	        	
	        	System.out.println(query);
	            rs = stmt.executeQuery(query);
	            
   	            if(!rs.isBeforeFirst()) {
   	               Sales s = new Sales(topk.get(i), 0.0, "");
   	               sales.add(s);
   	               continue;
   	            }
	            
	            //populate list
	            boolean everyOther = false;
	            Sales temp = null;
	            while (rs.next()) {
	               String purchaser = rs.getString("name");
	               double price = rs.getDouble("total");
	               String product = rs.getString("product");

	               //The everyOther aspect is designed so have the sales from the previous loop index
	               //and we can check if the current sale is mad by the same user and of the same
	               //product.  If so, we just mold these sales into 1 sale.  This is neccessary
	               //because users can purchase items multiple times but at different instances, so
	               //the database will contain multiple sales of the same product.
	               if(everyOther) {
	                  if(temp.getPurchaser().equals(purchaser) && temp.getProduct().equals(product)) {
	                     String tempPurchaser = temp.getPurchaser();
	                     double tempPrice = temp.getPrice() + price;
	                     String tempProduct = temp.getProduct();
	                     temp = new Sales(tempPurchaser, tempPrice, tempProduct);
	                     if(rs.isLast())//make sure to add item it is the last one
	                        sales.add(temp);
	                  }
	                  else {   
	                     sales.add(temp);
	                     temp = new Sales(purchaser, price, product);
	                     if(rs.isLast())//make sure to add item it is the last one
	                        sales.add(temp);
	                  }
	               }
	               else {
	                  everyOther = true;
	                  temp = new Sales(purchaser, price, product);
                      if(rs.isLast())//make sure to add last item
                         sales.add(temp);
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