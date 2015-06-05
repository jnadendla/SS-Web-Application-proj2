package helpers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.http.HttpSession;

public class PurchaseHelper {

	public static String purchaseCart(ShoppingCart cart, Integer uid) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		Statement stmt = null;
		try {
			try {
				conn = HelperUtils.connect();
			} catch (Exception e) {
				return HelperUtils
						.printError("Internal Server Error. This shouldn't happen.");
			}
			stmt = conn.createStatement();
			for (int i = 0; i < cart.getProducts().size(); i++) {
				ProductWithCategoryName p = cart.getProducts().get(i);
				int quantity = cart.getQuantities().get(i);
				conn.setAutoCommit(false);
				String SQL_1 = "INSERT INTO cart_history (uid) VALUES (" + uid
						+ ");";
				stmt.execute(SQL_1);
				// Gets latest inserted id. See this stackoverflow for more
				// information
				// http://stackoverflow.com/questions/2944297/postgresql-function-for-last-inserted-id
				String SQL_2 = "SELECT lastval();";
				ResultSet rs = stmt.executeQuery(SQL_2);
				rs.next();
				int cart_id = rs.getInt(1);
				String SQL_3 = "INSERT INTO sales (uid, pid, cart_id, quantity, price) VALUES("
						+ uid
						+ ",'"
						+ p.getId()
						+ "','"
						+ cart_id
						+ "','"
						+ quantity + "', " + p.getPrice() + ");";
				stmt.execute(SQL_3);

				// //////////// PROJECT 3 CODE ////////////////////////
				// Insert into Ordered Table
				String query = "INSERT INTO ordered (uid, pid, price) VALUES("
						+ uid + ", " + p.getId() + ", (" + quantity + " * "
						+ p.getPrice() + "))";
				System.out.println(query);
				pstmt = conn.prepareStatement(query);
				pstmt.execute();
				// .....

				conn.commit();
				conn.setAutoCommit(true);
			}
			cart.empty();
			return HelperUtils.printSuccess("Purchase successful!");
		} catch (SQLException e) {
			e.printStackTrace();
			return HelperUtils
					.printError("Oops! Looks like the product you want to buy is no longer available...");
		} finally {
			try {
				stmt.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public static ShoppingCart obtainCartFromSession(HttpSession session) {
		ShoppingCart cart;
		try {
			cart = (ShoppingCart) session.getAttribute("cart");
			if (cart == null) {
				cart = new ShoppingCart();
			}
		} catch (Exception e) {
			cart = new ShoppingCart();
		}
		session.setAttribute("cart", cart);
		return cart;
	}

}
