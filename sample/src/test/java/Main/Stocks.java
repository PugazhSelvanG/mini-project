package Main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
//import Main.Main.Stock;
//import Main.Main.User;
public class Stocks extends Main {
	
    public void viewStockMarket() {
    System.out.println("\n===== Stock Market =====");
    System.out.printf("%-10s %-10s %-20s %-10s%n", "Stock ID", "Symbol", "Name", "Price");
    for (Stock stock : stockMarket) {
        System.out.printf("%-10d %-10s %-20s %-10.2f%n", stock.getStockId(), stock.getSymbol(), stock.getName(), stock.price);
    }
}

protected void buyStocks(Connection connection, User currentUser) {
	Updatefunds uf=new Updatefunds();
    Scanner scanner = new Scanner(System.in);
    System.out.println("\n===== Buy Stocks =====");
    viewStockMarket();
    System.out.print("Enter the Stock ID you want to buy: ");
    int stockId = scanner.nextInt();

    Stock selectedStock = null;
    for (Stock stock : stockMarket) {
        if (stock.getStockId() == stockId) {
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
    
    if (totalCost <= uf.getAvailableFunds(connection, currentUser)) {
       
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

protected void sellStocks(Connection connection, User currentUser) {
    Scanner scanner = new Scanner(System.in);
    System.out.println("\n===== Sell Stocks =====");
    viewPortfolio vp=new viewPortfolio();
    vp.viewPortfolio(connection, currentUser);
    System.out.print("Enter the Stock ID you want to sell: ");
    int stockId = scanner.nextInt();
    
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

    
    double stockPrice = getStockPrice(connection, stockId);
    double totalAmountReceived = stockPrice * quantityToSell;

  
    String deleteQuery = "DELETE FROM portfolio WHERE user_id=? AND stock_id=? LIMIT ?";
    Updatefunds uf=new Updatefunds();
    try (PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery)) {
        deleteStatement.setInt(1, currentUser.userId);
        deleteStatement.setInt(2, stockId);
        deleteStatement.setInt(3, quantityToSell);
        int rowsDeleted = deleteStatement.executeUpdate();
        if (rowsDeleted > 0) {
            // Add the total amount received to the user's available funds
            uf.getAvailableFunds(connection, currentUser, totalAmountReceived);
            System.out.println("Stocks sold successfully!");
        } else {
            System.out.println("Failed to sell stocks. Please try again.");
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
}




}
