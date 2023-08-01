import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class j1{
    private static final String DB_URL = "jdbc:mysql://localhost:3306/stock1";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Selvan@8203";

    private static List<Stock> stockMarket = new ArrayList<>();

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            loadStocksFromDatabase(connection);

            User currentUser = performLogin(connection);
            if (currentUser == null) {
                System.out.println("Login failed. Exiting...");
                return;
            }

            // Show main menu
            showMainMenu(connection, currentUser);

            connection.close();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    private static void loadStocksFromDatabase(Connection connection) throws SQLException {
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

    private static User performLogin(Connection connection) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("===== Stock Trading Simulation =====");
        System.out.print("Enter your username: ");
        String username = scanner.nextLine();
        System.out.print("Enter your password: ");
        String password = scanner.nextLine();

        // Check user login credentials in the database
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

    private static void showMainMenu(Connection connection, User currentUser) {
        Scanner scanner = new Scanner(System.in);
        int choice;
        do {
            System.out.println("\n===== Main Menu =====");
            System.out.println("1. View Stock Market");
            System.out.println("2. Buy Stocks");
            System.out.println("3. Sell Stocks");
            System.out.println("4. View Portfolio");
            System.out.println("5. Exit");
            System.out.print("Enter your choice: ");
            choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    viewStockMarket();
                    break;
                case 2:
                    buyStocks(connection, currentUser);
                    break;
                case 3:
                    sellStocks(connection, currentUser);
                    break;
                case 4:
                    viewPortfolio(connection, currentUser);
                    break;
                case 5:
                    System.out.println("Thank you for using the Stock Trading Simulation!");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
                    break;
            }
        } while (choice != 5);
    }

  
   
    private static class Stock {
        private int stockId;
        private String symbol;
        private String name;
        private double price;

        public Stock(int stockId, String symbol, String name, double price) {
            this.stockId = stockId;
            this.symbol = symbol;
            this.name = name;
            this.price = price;
        }

    }

    private static class User {
        private int userId;
        private String username;

        public User(int userId, String username) {
            this.userId = userId;
            this.username = username;
        }

    }




private static void viewStockMarket() {
    System.out.println("\n===== Stock Market =====");
    System.out.printf("%-10s %-10s %-20s %-10s%n", "Stock ID", "Symbol", "Name", "Price");
    for (Stock stock : stockMarket) {
        System.out.printf("%-10d %-10s %-20s %-10.2f%n", stock.stockId, stock.symbol, stock.name, stock.price);
    }
}

private static void buyStocks(Connection connection, User currentUser) {
    Scanner scanner = new Scanner(System.in);
    System.out.println("\n===== Buy Stocks =====");
    viewStockMarket();
    System.out.print("Enter the Stock ID you want to buy: ");
    int stockId = scanner.nextInt();

    Stock selectedStock = null;
    for (Stock stock : stockMarket) {
        if (stock.stockId == stockId) {
            selectedStock = stock;
            break;
        }
    }

    if (selectedStock == null) {
        System.out.println("Invalid Stock ID. Please try again.");
        return;
    }

    System.out.print("Enter the quantity you want to buy: ");
    int quantityToBuy = scanner.nextInt();
    double totalCost = selectedStock.price * quantityToBuy;

    if (totalCost <= getAvailableFunds(connection, currentUser)) {
       
        String insertQuery = "INSERT INTO portfolio (user_id, stock_id, quantity) VALUES (?, ?, ?)";
        try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
            insertStatement.setInt(1, currentUser.userId);
            insertStatement.setInt(2, stockId);
            insertStatement.setInt(3, quantityToBuy);
            int rowsInserted = insertStatement.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Stocks purchased successfully!");
            } else {
                System.out.println("Failed to purchase stocks. Please try again.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    } else {
        System.out.println("Insufficient funds to buy the stocks.");
    }
}

private static void sellStocks(Connection connection, User currentUser) {
    Scanner scanner = new Scanner(System.in);
    System.out.println("\n===== Sell Stocks =====");
    viewPortfolio(connection, currentUser);
    System.out.print("Enter the Stock ID you want to sell: ");
    int stockId = scanner.nextInt();

    // Check if the user owns the stock with the entered stock ID
    boolean stockOwned = false;
    String query = "SELECT * FROM portfolio WHERE user_id=? AND stock_id=?";
    try (PreparedStatement pst = connection.prepareStatement(query)) {
        pst.setInt(1, currentUser.userId);
        pst.setInt(2, stockId);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            stockOwned = true;
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    if (!stockOwned) {
        System.out.println("You don't own any stocks with the entered Stock ID.");
        return;
    }

    System.out.print("Enter the quantity you want to sell: ");
    int quantityToSell = scanner.nextInt();

    // Check if the user has the specified quantity of the stock to sell
    int ownedQuantity = getOwnedQuantity(connection, currentUser, stockId);
    if (quantityToSell > ownedQuantity) {
        System.out.println("You don't own enough quantity of the stock to sell.");
        return;
    }

    // Calculate the total amount received from selling the stocks
    double stockPrice = getStockPrice(connection, stockId);
    double totalAmountReceived = stockPrice * quantityToSell;

    // Delete the sold stocks from the portfolio table
    String deleteQuery = "DELETE FROM portfolio WHERE user_id=? AND stock_id=? LIMIT ?";
    try (PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery)) {
        deleteStatement.setInt(1, currentUser.userId);
        deleteStatement.setInt(2, stockId);
        deleteStatement.setInt(3, quantityToSell);
        int rowsDeleted = deleteStatement.executeUpdate();
        if (rowsDeleted > 0) {
            // Add the total amount received to the user's available funds
            updateAvailableFunds(connection, currentUser, totalAmountReceived);
            System.out.println("Stocks sold successfully!");
        } else {
            System.out.println("Failed to sell stocks. Please try again.");
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
}

private static void viewPortfolio(Connection connection, User currentUser) {
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


private static double getAvailableFunds(Connection connection, User currentUser) {
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

private static int getOwnedQuantity(Connection connection, User currentUser, int stockId) {
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

private static double getStockPrice(Connection connection, int stockId) {
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

private static void updateAvailableFunds(Connection connection, User currentUser, double amount) {
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

