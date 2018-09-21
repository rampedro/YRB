/*============================================================================
Book: A JDBC APP to list total sales for a customer from the YRB DB.

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
CLASS Book
============================================================================*/

public class Book {
    private Connection conDB;   // Connection to the database system.
    private String url;         // URL: Which database?

    private Integer custID;     // Who are we tallying?
    private String  custName;   // Name of that customer.
    private String catName;
    private String titleName;
    private String year;
    private double lowestPrice;
    private String club;
    private String when;
    private int num;
    private String potID;

    // Constructor
    public Book (String[] args) {
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

        // Let's have autocommit turned off.  No particular reason here.
        try {
            conDB.setAutoCommit(false);
        } catch(SQLException e) {
            System.out.print("\nFailed trying to turn autocommit off.\n");
            e.printStackTrace();
            System.exit(0);
        }    


	Scanner sc = new Scanner(System.in);

	// Step 1: get the right customer ID
	while (true) {
	
			boolean isNumeric = false;//This will be set to true when numeric val entered

		while(!isNumeric)
 		try {

			System.out.println("Please provide your customer ID:");
			custID = Integer.parseInt(sc.nextLine());
			isNumeric = true;

		} catch(NumberFormatException nfe) {
    					
	       				//Display Error message
    			 System.out.println("Invalid character found, Please enter Integer  values only !!");
			 
  		}

		if (!customerCheck()) {
            		System.out.println("There is no customer with ID " + custID);
		} 
		else {	break;	}

	}
		
	System.out.println("");

	while (true) {
		// Step 2: list and display all categories
		listCategories();

		System.out.println("");


		// Step 3: let user pick a category
		System.out.print("Please type in the name of a category: ");
		catName = sc.nextLine()	;
			
		System.out.println("=================");


		listBooks();

		// Step 4: let user pick a title
		//
		System.out.println("Please type in the name of a tile (in category " + catName + "): ");

		
		System.out.println("=================");


		System.out.println(" ");

		titleName = sc.nextLine();
		
		if (getBookInformation()) {	break;	}else{
		
			System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
			System.out.println("Either the name of Categorty or the Book is Wrong !");

			System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		}
	


	}


	// Step 5: 
	getMinimumPrice();

	// Step 6: ask how many books the client wants
	System.out.print("How many you want: ");
	int numBooks = Integer.parseInt(sc.nextLine());
	num = numBooks;

	System.out.println("The total price is " + numBooks * lowestPrice);

	System.out.print("Are you sure you wan to buy this many books? (y/n) ");
	String confirm = sc.nextLine();
	if (!confirm.equals("y")) {
		System.out.println("Oh too bad. bye!");
		return;
	}

	// Step 7: record the purchase in the purchase table.


	insertPurchase();
	

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





    public void insertPurchase() {

	Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        PreparedStatement querySt   = null;   // The query handle.
	String newTitle= titleName.replace("'","''");

        String queryText =
            "INSERT INTO yrb_purchase(cid, club, title, year, when, qnty) "
          + "values(" + custID + ",'" + club + "', '" + newTitle + "'," + year + ",'" + timestamp + "'," + num + ")";

        // Prepare the query.
        try {
            querySt = conDB.prepareStatement(queryText);
        } catch(SQLException e) {
            System.out.println("SQL#1 failed in prepare");
            System.out.println(e.toString());
            System.exit(0);
        }

        // Execute the UPDATE.
      try {
            querySt.executeUpdate();
        } catch(SQLException e) {
            System.out.println("SQL#1 failed in execute");
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

    }







    public void listCategories() {

        PreparedStatement querySt   = null;   // The query handle.
        ResultSet         answers   = null;   // A cursor.

        String queryText =
            "SELECT * "
          + "FROM yrb_category";

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
            answers = querySt.executeQuery();
        } catch(SQLException e) {
            System.out.println("SQL#1 failed in execute");
            System.out.println(e.toString());
            System.exit(0);
        }

	System.out.println("Here are our categories:");
        try {
	    while (answers.next()) {
		System.out.println(answers.getString("cat"));
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

    }





    public void listBooks() {

        PreparedStatement querySt   = null;   // The query handle.
        ResultSet         answers   = null;   // A cursor.

        String queryText =
            "SELECT Title "
          + "FROM yrb_book "
	  + "WHERE cat='"+ catName + "'" ;

        // Prepare the query.
        try {
            querySt = conDB.prepareStatement(queryText);
        } catch(SQLException e) {
            System.out.println("LB SQL#1 failed in prepare");
            System.out.println(e.toString());
            System.exit(0);
        }

        // Execute the query.
        try {
            answers = querySt.executeQuery();
        } catch(SQLException e) {
            System.out.println("LB SQL#1 failed in execute");
            System.out.println(e.toString());
            System.exit(0);
        }

	System.out.println("List of Books in this cat:");
	System.out.println("---------------------------");
        try {
	    while (answers.next()) {
		System.out.println(answers.getString("Title"));
	    }
        } catch(SQLException e) {
            System.out.println("LB SQL#1 failed in cursor.");
            System.out.println(e.toString());
            System.exit(0);
        }

        // Close the cursor.
        try {
            answers.close();
        } catch(SQLException e) {
            System.out.print("LB SQL#1 failed closing cursor.\n");
            System.out.println(e.toString());
            System.exit(0);
        }

        // We're done with the handle.
        try {
            querySt.close();
        } catch(SQLException e) {
            System.out.print("LB SQL#1 failed closing the handle.\n");
            System.out.println(e.toString());
            System.exit(0);
        }

    }






    public void getMinimumPrice() {
        String            queryText = "";     // The SQL text.
        PreparedStatement querySt   = null;   // The query handle.
        ResultSet         answers   = null;   // A cursor.
	String newTitle = titleName.replace("'","''");

        queryText =
            "SELECT * "
          + "FROM yrb_offer, yrb_member "
          + "WHERE yrb_offer.title = '" + newTitle
	  + "' AND yrb_offer.year = " + year
	  + " AND yrb_member.club = yrb_offer.club" 
	  + " AND yrb_member.cid = " + custID;


        // Prepare the query.
        try {
            querySt = conDB.prepareStatement(queryText);
        } catch(SQLException e) {
            System.out.println("GM SQL#1 failed in prepare");
            System.out.println(e.toString());
            System.exit(0);
        }

        // Execute the query.
        try {
            answers = querySt.executeQuery();
        } catch(SQLException e) {
            System.out.println("GM SQL#1 failed in execute");
            System.out.println(e.toString());
            System.exit(0);
        }

	System.out.println("Here is the minimum price for this book: ");
        try {

	    lowestPrice = 99999999;
	    while (answers.next()) {
	    	    club = answers.getString("club");
		    double price = answers.getDouble("price");
		    if (price < lowestPrice) {
			    lowestPrice = price;
		    }
	    }
	    System.out.println("lowest price is " + lowestPrice);;

         }
         catch(SQLException e) {
            System.out.println("GM SQL#1 failed in cursor.");
            System.out.println(e.toString());
            System.exit(0);
        }

        // Close the cursor.
        try {
            answers.close();
        } catch(SQLException e) {
            System.out.print("GM SQL#1 failed closing cursor.\n");
            System.out.println(e.toString());
            System.exit(0);
        }

        // We're done with the handle.
        try {
            querySt.close();
        } catch(SQLException e) {
            System.out.print("GM SQL#1 failed closing the handle.\n");
            System.out.println(e.toString());
            System.exit(0);
        }
	    
    }


 



    public boolean getBookInformation() {
        String            queryText = "";     // The SQL text.
        PreparedStatement querySt   = null;   // The query handle.
        ResultSet         answers   = null;   // A cursor.
	String newTitle = titleName.replace("'","''");
        boolean           inDB      = false;  // Return.


        queryText =
            "SELECT * "
          + "FROM yrb_book "
          + "WHERE cat = '" + catName
	  + "' AND title = '" + newTitle
	  + "'";


        // Prepare the query.
        try {
            querySt = conDB.prepareStatement(queryText);
        } catch(SQLException e) {
            System.out.println("GBI SQL#1 failed in prepare");
            System.out.println(e.toString());
            System.exit(0);
        }

        // Execute the query.
        try {
            //querySt.setInt(1, custID.intValue());
            answers = querySt.executeQuery();
        } catch(SQLException e) {
            System.out.println("GBI SQL#1 failed in execute");
            System.out.println(e.toString());
            System.exit(0);
        }

        // Any answer?
        try {
            if (answers.next()) {
                inDB = true;
		System.out.println("Title: " + answers.getString("title") 
				+ " year: " + answers.getString("year") 
				+ " language: " + answers.getString("language") 
				+ " weight: " + answers.getString("weight"));
		year = answers.getString("year");
            } else {
                inDB = false;
            }
        } catch(SQLException e) {
            System.out.println("GBI SQL#1 failed in cursor.");
            System.out.println(e.toString());
            System.exit(0);
        }

        // Close the cursor.
        try {
            answers.close();
        } catch(SQLException e) {
            System.out.print("GBI SQL#1 failed closing cursor.\n");
            System.out.println(e.toString());
            System.exit(0);
        }

        // We're done with the handle.
        try {
            querySt.close();
        } catch(SQLException e) {
            System.out.print("GBI SQL#1 failed closing the handle.\n");
            System.out.println(e.toString());
            System.exit(0);
        }

        return inDB;

    }





    public boolean customerCheck() {
        String            queryText = "";     // The SQL text.
        PreparedStatement querySt   = null;   // The query handle.
        ResultSet         answers   = null;   // A cursor.

        boolean           inDB      = false;  // Return.

        queryText =
            "SELECT * "
          + "FROM yrb_customer "
          + "WHERE cid = " + custID.intValue();

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
            //querySt.setInt(1, custID.intValue());
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
		System.out.println("Name: " + answers.getString("name") + " ID: " + answers.getString("cid") + " " + answers.getString("city") );
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


    public static void main(String[] args) {
        Book ct = new Book(args);
    }
}

