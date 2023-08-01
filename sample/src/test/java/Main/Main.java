package Main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
public class Main extends abstractStocks{
    private static final String DB_URL = "jdbc:mysql://localhost:3306/stock1";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Selvan@8203";

    protected static List<Stock> stockMarket = new ArrayList<>();

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            Main main = new Main();
			main.loadStocksFromDatabase(connection);
             Login lg=new Login();
            User currentUser = lg.performLogin(connection);
            if (currentUser == null) {
                System.out.println("Login failed. Exiting...");
                return;
            }

            Mainmenu mm=new Mainmenu();
            mm.showMainMenu(connection, currentUser);

            connection.close();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public void loadStocksFromDatabase(Connection connection) throws SQLException {
        String query = "SELECT * FROM stocks";
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(query);

        while (resultSet.next()) {
            int stockId = resultSet.getInt("stock_id");
            String stockSymbol = resultSet.getString("stock_symbol");
            String stockName = resultSet.getString("stock_name");
            double stockPrice = resultSet.getDouble("stock_price");

            Stock stock = new Stock(stockId, stockSymbol, stockName, stockPrice);
            stockMarket.add(stock);
        }

        resultSet.close();
        statement.close();
    }
    public static class Stock {
        private int stockId;
        private String symbol;
        private String name;
        double price;

        public Stock(int stockId, String symbol, String name, double price) {
            this.stockId = stockId;
            this.symbol = symbol;
            this.name = name;
            this.price = price;
        }
        public int getStockId() {
        	return stockId;
        }
        String getSymbol() {
        	return symbol;
        }
        public  String getName() {
        	return name;
        }
        public void setStockId(int stockId) {
        	this.stockId=stockId;
        }
        public void setSymbol(String symbol) {
        	this.symbol=symbol;
        }
        public void setStockId(String name) {
        	this.name=name;
        }
    }

    public static class User {
        int userId;
        private String username;

        public User(int userId, String username) {
            this.userId = userId;
            this.username = username;
        }

    }




protected static int getOwnedQuantity(Connection connection, User currentUser, int stockId) {
    int ownedQuantity = 0;
    String query = "SELECT quantity FROM portfolio WHERE user_id=? AND stock_id=?";
    try (PreparedStatement pst = connection.prepareStatement(query)) {
        pst.setInt(1, currentUser.userId);
        pst.setInt(2, stockId);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            ownedQuantity = rs.getInt("quantity");
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return ownedQuantity;
}

public static double getStockPrice(Connection connection, int stockId) {
    double stockPrice = 0;
    String query = "SELECT stock_price FROM stocks WHERE stock_id=?";
    try (PreparedStatement pst = connection.prepareStatement(query)) {
        pst.setInt(1, stockId);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            stockPrice = rs.getDouble("stock_price");
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return stockPrice;
}

}

