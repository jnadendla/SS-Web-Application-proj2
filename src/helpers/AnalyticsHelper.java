package helpers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class AnalyticsHelper {

	private static HttpSession session;
	private static int colOffset = 0;
	private static long startTime = 0;
	private static long timeElapsed = 0;
	private static Map<String,String> stateTotals = new HashMap<String,String>();

	public static List<Sales> listSales(HttpServletRequest request, boolean runQuery,
			boolean nextCols) {
		List<Sales> sales = new ArrayList<Sales>();
		String categoryFrom = "";
		String categoryFilter = "", additionalFilter = "", category = "", categoryTotalsFilter = "";
		boolean refresh = false;

		// If the RUN QUERY button has been pressed this should capture the new
		// filter option it has selected, but if nothing new has been selected,
		// we use the previous filter data.
		session = request.getSession();
		String c = request.getParameter("categoryFilter");
		session.setAttribute("categoryFilter", c);
		
		// When next button is clicked, increment the offsets
		if (nextCols)
			colOffset += 50;
		else if(runQuery) {
			// New data means we start at offset 0
			colOffset = 0;
		}

		addIndeciesToTables();

		// This area determines what to filter the category by. If ALL is
		// selected by
		// the category, we leave the filter and from strings as empty and then
		// they
		// will not affect the query.
		try {
			category = (String) session.getAttribute("categoryFilter");
			if (category != null && !category.isEmpty()
					&& !category.equals("all")) {
				categoryFrom = ", categories AS c ";
				categoryFilter = "AND p.cid = c.id AND c.id = " + category
						+ " AND c.id = t.cid ";
				categoryTotalsFilter = "AND p.cid = c.id AND c.id = " + category + "";
			}
		} catch (Exception e) {
		}

		// Here we look at where we filter by user or customer and create a
		// string that determines
		// what items we will want to select in the SELECT clause of the query.
		// There is another
		// string that will reflect which tables we need to query from.
		// If we filter by states there is an additional table to query from
		// (states).
		// be placed in the WHERE clause of the query.
		additionalFilter = "t.id = u.state AND u.id = s.uid AND u.role = 'customer' AND p.id = s.pid ";
		

		// This simply combines the filter string with a category to filter
		// by if one exists.
		String filter = " WHERE " + additionalFilter + categoryFilter;

		// Here is the sort string the will by placed in the ORDER BY clause
		// of the query. Based on if the sort is alphabetical, we then sort by
		// the name and product of the users/states. If the sort is topk, then
		// we call a helper method to build the rest of the sales list.
		sales = listByTopK(categoryFrom, categoryFilter, categoryTotalsFilter);// ////TOPK		// HERE
		dropIndeciesOnTables();
		return sales;
	}

	public static List<String> listProductsAlphabetically() {
		List<String> products = new ArrayList<String>();
		ResultSet rs;
		Connection conn = null;
		PreparedStatement stmt = null;
		String categoryFilter = "";
		String limitCols = "LIMIT 50 OFFSET " + colOffset;

		try {
			String category = (String) session.getAttribute("categoryFilter");
			if (category == null || category.equals(""))
				return products;

			if (category != null && !category.isEmpty()
					&& !category.equals("all")) {
				categoryFilter = ", categories AS c WHERE p.cid = c.id AND c.id = "
						+ category + "";
			}
		} catch (Exception e) {
		}

		try {
			try {
				conn = HelperUtils.connect();
			} catch (Exception e) {
				System.err
						.println("Internal Server Error. This shouldn't happen.");
				return products;
			}

			// stmt = conn.createStatement();
			String query = "SELECT p.name AS name FROM products AS p"
					+ categoryFilter + " ORDER BY p.name " + limitCols;
			
			stmt = conn.prepareStatement(query);
			
			startTime = System.nanoTime();
			rs = stmt.executeQuery();
			timeElapsed = System.nanoTime() - startTime;
			System.out.println("Product Query: " + query + " : " + timeElapsed);
			

			// populate list
			while (rs.next()) {
				String product = rs.getString("name");

				products.add(product);
			}
			return products;
		} catch (Exception e) {
			System.err.println("Some error happened!<br/>"
					+ e.getLocalizedMessage());
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
	 * Helper method gets the list of sales when the topk sort is selected. This
	 * is neccessary because we have to individually get a row at a time. We
	 * perfore 2 queries, one to see the orer in which the users/states will
	 * appear in a topk approach, and another to get all the sales based on that
	 * ordering.
	 */
	private static List<Sales> listByTopK(String categoryFrom,
			String categoryFilter, String categoryTotalsFilter) {
		List<Sales> sales = new ArrayList<Sales>();
		List<String> topk = new ArrayList<String>();
		ResultSet rs, order;
		Connection conn = null;
		PreparedStatement stmt = null;
		String limitCols = " LIMIT 50 OFFSET " + colOffset;
		String products;
		
		List<String> purchasers = new ArrayList<String>();
		String[] stateList = {"Alabama", "Alaska", "Arizona", "Arkansas", "California", "Colorado", "Connecticut", "Delaware",
		      "Florida", "Georgia", "Hawaii", "Idaho", "Illinois", "Indiana", "Iowa",
		      "Kansas", "Kentucky", "Louisiana", "Maine", "Maryland", "Massachusetts", "Michigan", "Minnesota",
		      "Mississippi", "Missouri", "Montana", "Nebraska", "Nevada", "New Hampshire", "New Jersey", "New Mexico",
		      "New York", "North Carolina", "North Dakota", "Ohio", "Oklahoma", "Oregon", "Pennsylvania", "Rhode Island",
		      "South Carolina", "South Dakota", "Tennessee", "Texas", "Utah", "Vermont", "Virginia", "Washington",
		      "West Virginia", "Wisconsin", "Wyoming"};
		for(int i=0; i < stateList.length; ++i) {
		   String state = stateList[i];
		   purchasers.add(state);
		}
	    
	      try {
	            try {
	                conn = HelperUtils.connect();
	            } catch (Exception e) {
	                System.err
	                        .println("Internal Server Error. This shouldn't happen.");
	                return sales;
	            }
	            // stmt = conn.createStatement();

	            // Loop through all the names ordered by topk and query sales
	            // information in topk order
   
   
	            String query = "";
	            String category = (String) session.getAttribute("categoryFilter");
	            System.out.println(category);
	            if(category != null && !category.equals("all")) {
      	            query = "SELECT s.name AS state, p.name AS product, o.price AS price "
      	                  + "FROM users AS u, states AS s, ordered AS o, totals AS t, categories AS c"
      	                  + ", (SELECT * FROM products AS n ORDER BY n.name" + limitCols + ") p "
      	                  + "WHERE u.id = o.uid AND s.id = u.state " + categoryFilter 
      	                  + "AND p.id = o.pid AND s.id = t.state "
      	                  + "ORDER BY t.total DESC, p.name";
	            } else {
	                query = "SELECT s.name AS state, p.name AS product, o.price AS price "
	                      + "FROM users AS u, states AS s, ordered AS o, allTotals AS t"
	                      + ", (SELECT * FROM products AS n ORDER BY n.name" + limitCols + ") p "
	                      + "WHERE u.id = o.uid AND s.id = u.state "
	                      + "AND p.id = o.pid AND s.id = t.state ORDER BY t.total DESC, p.name";
	            }
	            
	            
	            stmt = conn.prepareStatement(query);
	            System.out.println(query);
	            startTime = System.nanoTime();
	            rs = stmt.executeQuery();
	            timeElapsed = System.nanoTime() - startTime;
	            System.out.println(query + " : " + timeElapsed);
	            
	            boolean ignore = false;
	            if (!rs.isBeforeFirst()) {
                   ignore = true;
                }
	            
	            // populate list
	            boolean everyOther = false;
	            Sales temp = null;
	            while (ignore == false && rs.next()) {
	               String purchaser = rs.getString("state");
	               double price = rs.getDouble("price");
	               String product = rs.getString("product");
	               topk.add(purchaser);
	               	               
	               // The everyOther aspect is designed so have the sales from
	               // the previous loop index
	               // and we can check if the current sale is mad by the same
	               // user and of the same
	               // product. If so, we just mold these sales into 1 sale.
	               // This is neccessary
	               // because users can purchase items multiple times but at
	               // different instances, so
	               // the database will contain multiple sales of the same
	               // product.
	               if (everyOther) {
	                  if (temp.getPurchaser().equals(purchaser)
	                        && temp.getProduct().equals(product)) {
	                     String tempPurchaser = temp.getPurchaser();
	                     double tempPrice = temp.getPrice() + price;
	                     String tempProduct = temp.getProduct();
	                     temp = new Sales(tempPurchaser, tempPrice,
	                           tempProduct);
	                     if (rs.isLast())// make sure to add item it is the
	                        // last one
	                        sales.add(temp);
	                     } else {
	                        sales.add(temp);
	                        temp = new Sales(purchaser, price, product);
	                        if (rs.isLast())// make sure to add item it is the// last one
	                           sales.add(temp);
	                    }
	               } else {
	                  everyOther = true;
	                  temp = new Sales(purchaser, price, product);
	                  if (rs.isLast())// make sure to add last item
	                     sales.add(temp);
	                  }
	            }
	            
	            String pQuery = "SELECT s.name AS name, SUM(o.price) AS total "
	                          + "FROM states AS s, ordered AS o, users AS u, products AS p " + categoryFrom
	                          + "WHERE o.uid = u.id AND u.state = s.id AND o.pid = p.id " + categoryTotalsFilter
	                          + "GROUP BY s.name ORDER BY s.name ";
	            
	            stmt = conn.prepareStatement(pQuery);
	            
	            startTime = System.nanoTime();
	            order = stmt.executeQuery();
	            timeElapsed = System.nanoTime() - startTime;
	            System.out.println(pQuery + " : " + timeElapsed);
	            stateTotals.clear();
	            
	            while(order.next()) {
	                String purchaser = order.getString("name");
	                double total = order.getDouble("total");
	                stateTotals.put(purchaser, String.valueOf(total));
	            }

	            if(sales.size() < 50) {
	                for(int i = 0; i < purchasers.size(); i++) {
	                    if(!topk.contains(purchasers.get(i))) {
	                        sales.add(new Sales(purchasers.get(i), 0.0, ""));
	                    }
	                }
	            }
	            
	        } catch (Exception e) {
	            System.err.println("Some error happened!<br/>"
	                    + e.getLocalizedMessage());
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

	private static void addIndeciesToTables() {
		Connection conn = null;
		PreparedStatement stmt = null;

		try {
			try {
				conn = HelperUtils.connect();
			} catch (Exception e) {
				System.err
						.println("Internal Server Error. This shouldn't happen.");
			}
			String index = "CREATE INDEX idx_name_onUsers ON users (name)";
			stmt = conn.prepareStatement(index);
			stmt.execute();
			index = "CREATE INDEX idx_role_onUsers ON users (role)";
			stmt = conn.prepareStatement(index);
			stmt.execute();
			index = "CREATE INDEX idx_state_onUsers ON users (state)";
			stmt = conn.prepareStatement(index);
			stmt.execute();
			index = "CREATE INDEX idx_name_onStates ON states (name)";
			stmt = conn.prepareStatement(index);
			stmt.execute();
			index = "CREATE INDEX idx_name_onCategories ON categories (name)";
			stmt = conn.prepareStatement(index);
			stmt.execute();
			index = "CREATE INDEX idx_cid_onProducts ON products (cid)";
			stmt = conn.prepareStatement(index);
			stmt.execute();
			index = "CREATE INDEX idx_uid_onSales ON sales (uid)";
			stmt = conn.prepareStatement(index);
			stmt.execute();
			index = "CREATE INDEX idx_pid_onSales ON sales (pid)";
			stmt = conn.prepareStatement(index);
			stmt.execute();
			
		} catch (Exception e) {
			System.err.println("Some error happened adding index!<br/>"
					+ e.getLocalizedMessage());
		} finally {
			try {
				stmt.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	private static void dropIndeciesOnTables() {
		Connection conn = null;
		PreparedStatement stmt = null;

		try {
			try {
				conn = HelperUtils.connect();
			} catch (Exception e) {
				System.err
						.println("Internal Server Error. This shouldn't happen.");
			}
			String index = "DROP INDEX idx_name_onUsers";
			stmt = conn.prepareStatement(index);
			stmt.execute();
			index = "DROP INDEX idx_role_onUsers";
			stmt = conn.prepareStatement(index);
			stmt.execute();
			index = "DROP INDEX idx_state_onUsers";
			stmt = conn.prepareStatement(index);
			stmt.execute();
			index = "DROP INDEX idx_name_onStates";
			stmt = conn.prepareStatement(index);
			stmt.execute();
			index = "DROP INDEX idx_name_onCategories";
			stmt = conn.prepareStatement(index);
			stmt.execute();
			index = "DROP INDEX idx_cid_onProducts";
			stmt = conn.prepareStatement(index);
			stmt.execute();
			index = "DROP INDEX idx_uid_onSales";
			stmt = conn.prepareStatement(index);
			stmt.execute();
			index = "DROP INDEX idx_pid_onSales";
			stmt = conn.prepareStatement(index);
			stmt.execute();

		} catch (Exception e) {
			System.err.println("Some error happened dropping index!<br/>"
					+ e.getLocalizedMessage());
		} finally {
			try {
				stmt.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public static double getPurchaserTotal(HttpServletRequest request,
			String purchaser) throws Exception {
		session = request.getSession();
		String d = request.getParameter("displayFilter");
		String c = request.getParameter("categoryFilter");
		String o = request.getParameter("sortFilter");
		if (d != null && !d.equals("") && o != null && !o.equals("")) {
			session.setAttribute("displayFilter", d);
			session.setAttribute("categoryFilter", c);
			session.setAttribute("sortFilter", o);
		}

		double total = 0;
		
		String value = stateTotals.get(purchaser);
		if(value == null) return 0.0;
		total += Double.parseDouble(value);

		return total;
	}

	public static double getProductTotal(HttpServletRequest request,
			String product) throws Exception {
		double total = 0;

		ResultSet rs;
		Connection conn = HelperUtils.connect();

		PreparedStatement stmt = null;

		stmt = conn
				.prepareStatement("SELECT (s.price * s.quantity) AS total "
						+ "FROM sales AS s, products AS p WHERE p.name = ? AND s.pid = p.id");

		stmt.setString(1, product);
		
		startTime = System.nanoTime();
		rs = stmt.executeQuery();
		timeElapsed = System.nanoTime() - startTime;
		System.out.println(stmt.toString() + " : " + timeElapsed);

		while (rs.next()) {
			total += rs.getDouble("total");
		}

		stmt.close();
		conn.close();

		return total;
	}
	
}
