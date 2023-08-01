package Main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

import Main.Main.User;

public class Login extends Main {
	  static User performLogin(Connection connection) {
	        Scanner scanner = new Scanner(System.in);
	        System.out.println("===== Stock Trading Simulation =====");
	        System.out.print("Enter your username: ");
	        String username = scanner.nextLine();
	        System.out.print("Enter your password: ");
	        String password = scanner.nextLine();

	        
	        String query = "SELECT * FROM users WHERE username=? AND password=?";
	        try (PreparedStatement pst = connection.prepareStatement(query)) {
	            pst.setString(1, username);
	            pst.setString(2, password);
	            ResultSet rs = pst.executeQuery();
	            if (rs.next()) {
	                int userId = rs.getInt("user_id");
	                return new User(userId, username);
	            } else {
	                return null;
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	            return null;
	        }
	    }

}
