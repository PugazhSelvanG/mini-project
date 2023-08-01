package Main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import Main.Main.User;
public class Updatefunds {
	
	 double getAvailableFunds(Connection connection, User currentUser) {
	    double availableFunds = 0;
	    String query = "SELECT available_funds FROM available_funds WHERE user_id=?";
	    try (PreparedStatement pst = connection.prepareStatement(query)) {
	        pst.setInt(1, currentUser.userId);
	        ResultSet rs = pst.executeQuery();
	        if (rs.next()) {
	            availableFunds = rs.getDouble("available_funds");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return availableFunds;
	}
	void getAvailableFunds(Connection connection, User currentUser, double amount) {
	    double currentFunds = getAvailableFunds(connection, currentUser);
	    double updatedFunds = currentFunds + amount;
	    String updateQuery = "UPDATE available_funds SET available_funds=? WHERE user_id=?";
	    try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
	        updateStatement.setDouble(1, updatedFunds);
	        updateStatement.setInt(2, currentUser.userId);
	        int rowsUpdated = updateStatement.executeUpdate();
	        if (rowsUpdated > 0) {
	            System.out.println("Available funds updated successfully!");
	        } 
	        else {
	            System.out.println("Failed to update available funds. Please try again.");
	        }
	    } 
	    catch (SQLException e) {
	        e.printStackTrace();
	    }
	    
	}
	

}
