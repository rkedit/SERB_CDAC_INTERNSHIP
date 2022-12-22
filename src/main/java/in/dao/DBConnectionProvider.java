package in.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
 


public class DBConnectionProvider
{
	//static reference to itself
    private static DBConnectionProvider instance = new DBConnectionProvider();
    private static final String URL = "jdbc:mysql://localhost:3306/defect_data";
    private static final String USER = "root";
    private static final String PASSWORD = "root123"; //Password is root at the HPC machine
    private static final String DRIVER_CLASS = "com.mysql.jdbc.Driver"; 
     
    //private constructor
    private DBConnectionProvider() {
        try {
            //Step 2: Load MySQL Java driver
            Class.forName(DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
     
    private Connection createConnection() {
 
        Connection connection = null;
        try {
            //Step 3: Establish Java MySQL connection
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.out.println("ERROR: Unable to Connect to Database.");
        }
        return connection;
    }   
     
    public static Connection getConnection() 
    {
        return instance.createConnection();
    }
	
}
