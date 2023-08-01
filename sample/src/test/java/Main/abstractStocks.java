package Main;

import java.sql.Connection;
import java.sql.SQLException;

abstract class abstractStocks {
	public abstract void loadStocksFromDatabase(Connection connection) throws SQLException;
        
}
