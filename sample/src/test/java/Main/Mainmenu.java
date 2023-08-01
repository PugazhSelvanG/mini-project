package Main;

import java.sql.Connection;
import java.util.Scanner;

public class Mainmenu extends Main{

    void showMainMenu(Connection connection, User currentUser) {
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
             viewPortfolio vp=new viewPortfolio();
             Stocks sts=new Stocks();
            switch (choice) {
                case 1:
                    sts.viewStockMarket();
                    break;
                case 2:
                    sts.buyStocks(connection, currentUser);
                    break;
                case 3:
                    sts.sellStocks(connection, currentUser);
                    break;
                case 4:
                    viewPortfolio.viewPortfolio(connection, currentUser);
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

}
