=======================================================================
 PHARMACY INVENTORY MANAGEMENT SYSTEM (PIMS)
 HealthFirst Pharmacy  |  PRO732  |  Java Swing + JDBC + MySQL
=======================================================================

-----------------------------------------------------------------------
 1. The DEFAULT LOGIN CREDENTIALS
-----------------------------------------------------------------------
   ADMINISTRATOR   username: admin      password: admin123
   CASHIER         username: cashier    password: cash123
   CASHIER (2)     username: thabo      password: thabo123

   Passwords are stored in MySQL as SHA-256 hashes, never in plain text.

-----------------------------------------------------------------------
 2. WHAT YOU NEED
-----------------------------------------------------------------------
   * Apache NetBeans (any version 12 or newer)
   * JDK 8 or newer
   * MySQL Server 5.7 / 8.x (XAMPP or MySQL Installer both work)
   * MySQL Connector/J driver JAR (mysql-connector-j-8.x.x.jar)
     Download: https://dev.mysql.com/downloads/connector/j/
     (Choose "Platform Independent" -> ZIP -> extract the .jar)

-----------------------------------------------------------------------
 3. SET UP THE DATABASE (do this FIRST)
-----------------------------------------------------------------------
   a) Start MySQL (in XAMPP: start Apache + MySQL).
   b) Open MySQL Workbench, phpMyAdmin, or the MySQL command line.
   c) Open the file  database.sql  and run the whole script.
      Command line alternative:
          mysql -u root -p < database.sql
   d) It creates the database  pims_db  with 5 tables and sample data
      (3 users, 4 suppliers, 14 medicines and 4 past sales).

-----------------------------------------------------------------------
 4. RUN THE PROJECT IN APACHE NETBEANS
-----------------------------------------------------------------------
   STEP 1 - Create the project
      File > New Project > Java with Ant > Java Application
      (On newer NetBeans: Java with Ant > Java Application. If that
       category is missing, install the "Java SE" plugin via
       Tools > Plugins.)
      Project Name: PIMS
      UNTICK "Create Main Class"  ->  Finish

   STEP 2 - Add the source code
      In your file explorer, open the new project folder (e.g.
      C:\Users\YourName\Documents\NetBeansProjects\PIMS\src)
      Copy the whole  pims  folder from this zip's /src into that
      src folder. NetBeans will show a package called "pims"
      containing all the .java files. (Press F5 / right-click the
      project > Refresh if it does not appear immediately.)

   STEP 3 - Add the MySQL driver
      Right-click the project > Properties > Libraries >
      Add JAR/Folder > select mysql-connector-j-8.x.x.jar > OK

   STEP 4 - Set the database password
      Open  pims/DBConnection.java  and change:
          private static String USER = "root";
          private static String PASS = "root";
      to your own MySQL username and password.
      (XAMPP users usually have user "root" with an EMPTY password,
       so set PASS = "")

      OR create a file called db.properties next to the project and put:
          url=jdbc:mysql://localhost:3306/pims_db
          user=root
          password=

   STEP 5 - Set the main class and run
      Right-click the project > Properties > Run >
      Main Class: pims.Main  > OK
      Then press F6 (Run Project). The login screen appears.

-----------------------------------------------------------------------
 5. HOW TO USE THE SYSTEM
-----------------------------------------------------------------------
   LOGIN
     Enter a username and password. Admins are sent to the
     Administrator Dashboard, cashiers to the Point of Sale screen.
     Wrong details show an error message on the login screen.

   ADMINISTRATOR DASHBOARD
     Manage Medicines - add / update / delete stock items.
        Click a row in the table to load it into the form, edit it,
        then press Update. Rows shaded RED are at or below the
        reorder level; ORANGE rows expire within 30 days.
        Expiry dates must be typed as yyyy-MM-dd (e.g. 2027-03-31).
     Manage Suppliers - full CRUD for supplier records.
     Manage Users     - create cashier/admin accounts, update details,
                        reset passwords, delete accounts.
     Reports          - four reports (see below), each can be
                        exported to CSV.

   CASHIER DASHBOARD
     Point of Sale - type part of a medicine name, press Search,
        select a row, set the quantity, press "Add to Cart".
        The cart shows subtotals and a running total. Use
        "Remove Item" or "Clear Cart" to correct mistakes, then
        press CHECKOUT.
        Checkout runs as a single database transaction: it writes
        the sales header, the sale_items lines, and subtracts the
        stock. If anything fails, the whole sale is rolled back.
        A Bill window then opens which can be printed or saved.
     Stock Check - read-only price and availability lookup.
        Cashiers cannot add or edit medicines anywhere in the system.

-----------------------------------------------------------------------
 6. THE FOUR REPORTS
-----------------------------------------------------------------------
   1. Sales Report      - every transaction in a chosen date range,
                          with the cashier who processed it, the
                          number of line items and a grand total.
   2. Item-Wise Report  - units sold and revenue per medicine,
                          sorted by best seller.
   3. Low Stock Report  - medicines where stock <= reorder level,
                          including the supplier's phone number.
   4. Expiry Report     - medicines expiring within the next month,
                          showing the days remaining.

   The date boxes at the top only affect reports 1 and 2.

-----------------------------------------------------------------------
 7. BUILDING THE .EXE FOR SUBMISSION
-----------------------------------------------------------------------
   a) In NetBeans press F11 (Clean and Build). This creates
      dist/PIMS.jar plus a dist/lib folder holding the MySQL driver.
   b) Convert the JAR to an EXE with Launch4j (free):
        - Output file : yourname_pims.exe
        - Jar         : dist/PIMS.jar
        - Tick "Don't wrap the jar"  OR  bundle it, either works
        - JRE min version: 1.8.0
        - Press the gear icon to build.
   c) Keep dist/lib next to the .exe so the MySQL driver is found.
   d) Include database.sql in the zip so the marker can create the
      database, and mention the default logins (section 1 above).

-----------------------------------------------------------------------
 8. TROUBLESHOOTING
-----------------------------------------------------------------------
   "MySQL JDBC Driver not found"
        The connector JAR was not added - redo STEP 3.
   "Cannot connect to the MySQL database"
        MySQL is not running, or the password in DBConnection.java
        is wrong, or database.sql was never executed.
   "Unknown database 'pims_db'"
        Run database.sql.
   "Access denied for user 'root'@'localhost'"
        Wrong MySQL password in DBConnection.java.
   Login says invalid details
        Make sure you ran the supplied database.sql (it hashes the
        demo passwords with SHA2). Use admin / admin123.
   Cannot delete a supplier or medicine
        It is linked to existing records. Delete the sales/medicines
        that reference it first, or just leave it in place.

-----------------------------------------------------------------------
 11. FILE LIST
-----------------------------------------------------------------------
   src/pims/Main.java              - entry point, Nimbus look and feel
   src/pims/DBConnection.java      - JDBC connection helper
   src/pims/Session.java           - logged-in user + SHA-256 hashing
   src/pims/UITheme.java           - shared colours, fonts, styling
   src/pims/LoginFrame.java        - authentication + role redirection
   src/pims/AdminDashboard.java    - admin window with the four tabs
   src/pims/MedicinePanel.java     - medicine CRUD
   src/pims/SupplierPanel.java     - supplier CRUD
   src/pims/UserPanel.java         - user account management
   src/pims/ReportsPanel.java      - the four reports + CSV export
   src/pims/CashierDashboard.java  - Point of Sale, cart, checkout
   src/pims/StockCheckPanel.java   - read-only stock lookup
   src/pims/BillWindow.java        - printable customer bill
   database.sql                    - schema + sample data
   README.txt                      - this particular file
=======================================================================
