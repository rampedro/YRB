/*============================================================================
CustTotal: A JDBC APP to list total sales for a customer from the YRB DB.

Parke Godfrey
2013 March 26 [revised]
2004 March    [original]
============================================================================*/

import java.util.*;
import java.net.*;
import java.text.*;
import java.lang.*;
import java.io.*;
import java.sql.*;

/*============================================================================
CLASS CustTotal
============================================================================*/

public class CustTotal {
    private Connection conDB;   // Connection to the database system.
    private String url;         // URL: Which database?

    private Integer custID;     // Who are we tallying?
    private String  custName;   // Name of that customer.

    // Constructor
    public CustTotal (String[] args) {
        // Set up the DB connection.
        try {
            // Register the driver with DriverManager.
            Class.forName("com.ibm.db2.jcc.DB2Driver").newInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(0);
        } catch (InstantiationException e) {
            e.printStackTrace();
            System.exit(0);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            System.exit(0);
        }


        System.out.println("Done creating a new instance of DB2 Driver");

        // URL: Which database?
        url = "jdbc:db2:c3421a";

        // Initialize the connection.
        try {
            // Connect with a fall-thru id & password
            conDB = DriverManager.getConnection(url);
        } catch(SQLException e) {
            System.out.print("\nSQL: database connection error.\n");
            System.out.println(e.toString());
            System.exit(0);
        }    

        System.out.println("Done connecting to database " + url);

        // Let's have autocommit turned off.  No particular reason here.
        try {
            conDB.setAutoCommit(false);
        } catch(SQLException e) {
            System.out.print("\nFailed trying to turn autocommit off.\n");
            e.printStackTrace();
            System.exit(0);
        }    

        // Who are we tallying?
        if (args.length != 1) {
            // Don't know what's wanted.  Bail.
            System.out.println("\nUsage: java CustTotal cust#");
            System.exit(0);
        } else {
            try {
                custID = new Integer(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("\nUsage: java CustTotal cust#");
                System.out.println("Provide an INT for the cust#.");
                System.exit(0);
            }
        }

        // Is this custID for real?
        if (!customerCheck()) {
            System.out.print("There is no customer #");
            System.out.print(custID);
            System.out.println(" in the database.");
            System.out.println("Bye.");
            System.exit(0);
        }

        // Report total sales for this customer.
        reportSalesForCustomer();

        // Commit.  Okay, here nothing to commit really, but why not...
        try {
            conDB.commit();
        } catch(SQLException e) {
            System.out.print("\nFailed trying to commit.\n");
            e.printStackTrace();
            System.exit(0);
        }    
        // Close the connection.
        try {
            conDB.close();
        } catch(SQLException e) {
            System.out.print("\nFailed trying to close the connection.\n");
            e.printStackTrace();
            System.exit(0);
        }    

    }

    public boolean customerCheck() {
        String            queryText = "";     // The SQL text.
        PreparedStatement querySt   = null;   // The query handle.
        ResultSet         answers   = null;   // A cursor.

        boolean           inDB      = false;  // Return.

        queryText =
            "SELECT name       "
          + "FROM yrb_customer "
          + "WHERE cid = ?     ";

        // Prepare the query.
        try {
            querySt = conDB.prepareStatement(queryText);
        } catch(SQLException e) {
            System.out.println("SQL#1 failed in prepare");
            System.out.println(e.toString());
            System.exit(0);
        }

        // Execute the query.
        try {
            querySt.setInt(1, custID.intValue());
            answers = querySt.executeQuery();
        } catch(SQLException e) {
            System.out.println("SQL#1 failed in execute");
            System.out.println(e.toString());
            System.exit(0);
        }

        // Any answer?
        try {
            if (answers.next()) {
                inDB = true;
                custName = answers.getString("name");
            } else {
                inDB = false;
                custName = null;
            }
        } catch(SQLException e) {
            System.out.println("SQL#1 failed in cursor.");
            System.out.println(e.toString());
            System.exit(0);
        }

        // Close the cursor.
        try {
            answers.close();
        } catch(SQLException e) {
            System.out.print("SQL#1 failed closing cursor.\n");
            System.out.println(e.toString());
            System.exit(0);
        }

        // We're done with the handle.
        try {
            querySt.close();
        } catch(SQLException e) {
            System.out.print("SQL#1 failed closing the handle.\n");
            System.out.println(e.toString());
            System.exit(0);
        }

        return inDB;
    }

    public void reportSalesForCustomer() {
        String            queryText = "";     // The SQL text.
        PreparedStatement querySt   = null;   // The query handle.
        ResultSet         answers   = null;   // A cursor.

        queryText =
            "SELECT SUM(P.qnty * O.price) as total          "
          + "    FROM yrb_purchase P, yrb_offer O           "
          + "    WHERE P.cid = ?                            "
          + "      AND P.title = O.title AND P.year = O.year";

        // Prepare the query.
        try {
            querySt = conDB.prepareStatement(queryText);
        } catch(SQLException e) {
            System.out.println("SQL#2 failed in prepare");
            System.out.println(e.toString());
            System.exit(0);
        }

        // Execute the query.
        try {
            querySt.setInt(1, custID.intValue());
            answers = querySt.executeQuery();
        } catch(SQLException e) {
            System.out.println("SQL#2 failed in execute");
            System.out.println(e.toString());
            System.exit(0);
        }

        // Variables to hold the column value(s).
        float  sales;
        
        DecimalFormat df = new DecimalFormat("####0.00");

        // Walk through the results and present them.
        try {
            System.out.print("#");
            System.out.print(custID);
            System.out.print(" (" + custName + ") has spent $");
            if (answers.next()) {
                sales = answers.getFloat("total");
                System.out.print(df.format(sales));
            } else {
                System.out.print(df.format(0));
            }
            System.out.println(".");
        } catch(SQLException e) {
            System.out.println("SQL#2 failed in cursor.");
            System.out.println(e.toString());
            System.exit(0);
        }

        // Close the cursor.
        try {
            answers.close();
        } catch(SQLException e) {
            System.out.print("SQL#2 failed closing cursor.\n");
            System.out.println(e.toString());
            System.exit(0);
        }

        // We're done with the handle.
        try {
            querySt.close();
        } catch(SQLException e) {
            System.out.print("SQL#2 failed closing the handle.\n");
            System.out.println(e.toString());
            System.exit(0);
        }

    }

    public static void main(String[] args) {
        CustTotal ct = new CustTotal(args);
    }
}

