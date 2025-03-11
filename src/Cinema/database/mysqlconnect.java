package Cinema.database;

import java.sql.Connection;
import java.sql.DriverManager;

public class mysqlconnect {
    
    Connection conn = null;
    public static Connection ConnectDb(String url, String username, String password){
        try {
        	Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = (Connection) DriverManager.getConnection(url,username,password);
           //JOptionPane.showMessageDialog(null, "Connection Established");
            return conn;
        } catch (Exception e) {
            //JOptionPane.showMessageDialog(null, e);
            return null;
        }
    
    }
    
}