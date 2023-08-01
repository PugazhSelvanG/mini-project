package Main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
public class viewPortfolio extends Main {
	static void viewPortfolio(Connection connection, User currentUser) {
	    System.out.println("\n===== Portfolio =====");
	    System.out.printf("%-10s %-10s %-20s %-10s %-10s%n", "Stock ID", "Symbol", "Name", "Price", "Quantity");
	    String query = "SELECT p.stock_id, s.stock_symbol, s.stock_name, s.stock_price, p.quantity " +
	            "FROM portfolio p " +
	            "JOIN stocks s ON p.stock_id = s.stock_id " +
	            "WHERE p.user_id=?";
	    try (PreparedStatement pst = connection.prepareStatement(query)) {
	        pst.setInt(1, currentUser.userId);
	        ResultSet rs = pst.executeQuery();
	        while (rs.next()) {
	            int stockId = rs.getInt("stock_id");
	            String stockSymbol = rs.getString("stock_symbol");
	            String stockName = rs.getString("stock_name");
	            double stockPrice = rs.getDouble("stock_price");
	            int quantity = rs.getInt("quantity");
	            System.out.printf("%-10d %-10s %-20s %-10.2f %-10d%n", stockId, stockSymbol, stockName, stockPrice, quantity);
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
}
