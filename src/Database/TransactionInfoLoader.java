package Database;

import Utilities.Trace;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;

/**
 * Created by WillDeJs on 1/9/2017.
 */
public class TransactionInfoLoader {

    // connection to database and file objects
    private Connection connection;
    private SqliteConnection sqliteConn;

    /**
     * Default constructor to build an CustomerInfoLoder Object
     *
     * @throws IOException
     */
    public TransactionInfoLoader() throws IOException {
        sqliteConn = new SqliteConnection();
        connection = SqliteConnection.getConnection();
    }

    /**
     * Loads CustumerInfor table into into database
     * @param file File containing comma delimited information to load DB
     * @return state of transaction
     */
    public synchronized boolean  loadTransaction(File file) throws Exception{
                // Reader file
            CSVReader reader = new CSVReader(new FileReader(file));

           // delete and recreate table
           deleteCustomerInfoTable();
           createCustomerInfoTable();

           // first line of text containing headers
           // always assume header is present
           String[] header = reader.readNext();

           // load every line into the database
           String[] values;
           while ((values = reader.readNext()) != null) {
               // assumes the text file contains every element in order
               // ID CUSTOMER TRANSACTION BALANCE DATE

               for (int i = 0; i < values.length; i++) {
                   if ((i == 2 || i == 3) && values[i].equals("")) {
                       values[i] = "0";
                   }
                   if (values[i].equals(""))
                       values[i] = "-";
               }
               // load the entry
               if (!loadEntry(values[0], values[1], 
                       Double.parseDouble(values[2].trim()), values[4])) 
                return false;
           }
           return true;
           
    }

    /**
     * Loads CustumerInfor table into into database
     * @param file File containing comma delimited information to load DB
     * @return state of transaction
     */
    public synchronized boolean  loadCustomerInfo (String file) throws Exception {
        return loadTransaction(new File(file));
    }
    /**
     * Loads a single entry into the customer_info table
     * @param customer customer owning the transaction
     * @param transaction   description of the transaction
     * @param balance       balance/price of the transaction
     * @param date         date
     * @return              true if successful false otherwise
     * @throws SQLException
     */
    public synchronized boolean loadEntry(String customer,
            String transaction, double balance,  String date) {

        boolean result = false;
        
        // query to execute
     
      String query2 ="INSERT INTO CUSTOMER_INFO (CUSTOMER, TRANS, BALANCE, DATE) VALUES(?, ?, ?, ?)";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query2);

            //setup arguments
            preparedStatement.setString(1, customer);
            preparedStatement.setString(2, transaction);
            preparedStatement.setDouble(3, balance);
            preparedStatement.setString(4, date);

            // execute query
            result = preparedStatement.execute();
            preparedStatement.close();
        } catch (Exception ex) {
            Trace.getTrace().log(this.getClass(), Trace.Levels.ERROR, ex); 
        }
        return result;
    }

    /**
     * Updates a database entry of customer_info
     * @param id            id of the entry
     * @param customer      customer owning the entry
     * @param transaction   description of the transaction
     * @param balance       price of of the transaction
     * @param date          additional information about the transaction
     * @return
     * @throws SQLException
     */
    public synchronized boolean updateEntry(int id,String customer,
            String transaction, double balance,  String date) 
                throws SQLException {
        boolean result = false;
        // query to execute
        String query = "UPDATE CUSTOMER_INFO  SET CUSTOMER=?, TRANS=?,"
                + " BALANCE=?, DATE=? WHERE ID=?";

        PreparedStatement preparedStatement = connection.prepareStatement(query);

        //setup arguments
        preparedStatement.setString(1, customer);
        preparedStatement.setString(2, transaction);
        preparedStatement.setDouble(3, balance);
        preparedStatement.setString(4, date);
        preparedStatement.setInt(5, id);

        // execute query
        int changes = preparedStatement.executeUpdate();
        preparedStatement.close();
       
        preparedStatement.close();

        return changes != 0;

    }

    /**
     * Retrieves CustomerInfo from database
     *
     * @return List of CustomerInfo, null if reading fails
     */
    public synchronized ObservableList<TransactionInfoObject> getRecord() {
         String query = "SELECT * FROM CUSTOMER_INFO";
        ObservableList customerInfoList;
        try {
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery(query);
            customerInfoList = FXCollections.observableArrayList();

            while (result.next()) {
                customerInfoList.add(new TransactionInfoObject(
                        result.getInt(1),
                        result.getString(2),
                        result.getString(3),
                        result.getDouble(4),
                        result.getString(5)));
            }

            result.close();
            statement.close();
            return customerInfoList;
        } catch (Exception ex) {
            // log error
            Trace.getTrace().log(this.getClass(), Trace.Levels.ERROR, ex);
            return null;
        }
    }

    /**
     * Updates an CustomerInfoObject to database
     *
     * @param iv CustomerInfo containing data to be changed
     * @return State of loading true if successful
     */
    public synchronized boolean updateEntry(TransactionInfoObject iv) {
        try {
            return TransactionInfoLoader.this.updateEntry(iv.getId(), iv.getCustomer(), iv.getTransaction(),
                    iv.getBalance(), iv.getDate());

        } catch (Exception ex) {
            // log error
            Trace.getTrace().log(this.getClass(), Trace.Levels.ERROR, ex);
            return false;
        }
    }

    /**
     * Loads an CustomerInfo Object in database
     *
     * @param iv CustomerInfoObject containing data to be changed
     * @return state of loading true if successful
     */
    public synchronized boolean loadEntry(TransactionInfoObject iv) {
        try {
            return TransactionInfoLoader.this.loadEntry(iv.getCustomer(), iv.getTransaction(),
                    iv.getBalance(), iv.getDate());

        } catch (Exception ex) {
            // log error
            Trace.getTrace().log(this.getClass(), Trace.Levels.ERROR, ex);
            return false;
        }
    }

    /* Helper  to delete table contents. */
    private boolean deleteCustomerInfoTable() throws SQLException {
        String query = "DROP TABLE IF EXISTS 'CUSTOMER_INFO';";
        Statement statement = connection.createStatement();
        boolean result = statement.execute(query);
        statement.close();
        return result;
    }

    /* Helper function to recreate empty table. */
    public synchronized boolean createCustomerInfoTable() throws SQLException {
        String query = "CREATE TABLE IF NOT EXISTS'CUSTOMER_INFO' (" +
                " 'ID' INTEGER PRIMARY KEY AUTOINCREMENT, " +
                " 'CUSTOMER' TEXT NOT NULL, " +
                " 'TRANS' TEXT, " +
                " 'BALANCE' TEXT, " +
                " 'DATE' TEXT " +
                ");";
        Statement statement = connection.createStatement();
        boolean result = statement.execute(query);
        statement.close();
        return result;
    }

    /**
     * Query database based on user input
     * @param st    string to search for
     * @return      list of CustomerInfo objects, null if  no result
     */
    public synchronized ObservableList<TransactionInfoObject> searchCustomerInfoList(String st) {
        String query = "SELECT * FROM CUSTOMER_INFO WHERE CUSTOMER LIKE '%" 
                + st + "%'" + " OR TRANS LIKE '%" + st + "%';";
        ArrayList<TransactionInfoObject> list = new ArrayList<>();
        try {

            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery(query);

            while (result.next()) {
                list.add(new TransactionInfoObject(
                        result.getInt(1),
                        result.getString(2),
                        result.getString(3),
                        result.getDouble(4),
                        result.getString(5)));
            }
            statement.close();
            result.close();
            return FXCollections.observableArrayList(list);
        } catch (Exception ex) {
            // log error
            Trace.getTrace().log(this.getClass(), Trace.Levels.ERROR, ex);
            return null;
        }
    }

    /**
     * Deletes a single entry from the database
     * @param id ID (primary key) of entry in database
     * @return true if deletion succeeds, false otherwise
     * @throws SQLException 
     */
    public synchronized boolean deleteEntry(int id) throws SQLException {
        // query to execute
        String query = "DELETE FROM CUSTOMER_INFO WHERE ID=?;";

        PreparedStatement preparedStatement = connection.prepareStatement(query);

        //setup arguments
        preparedStatement.setInt(1, id);

        // execute query
        int changes = preparedStatement.executeUpdate();
        preparedStatement.close();
        return changes > 0;
    }

    /**
     * Exports Transaction Information to file specified by user
     * @param f File to contain customer information from database
     * @throws Exception
     */
    public synchronized void exportTransactionInfo(File f) throws Exception{
        ObservableList<TransactionInfoObject> list = getRecord();
        try (CSVWriter out = new CSVWriter(new PrintWriter(f))) {
            out.writeNext("CUSTOMER, TRANS, BALANCE, DATE".split(","));
            for (TransactionInfoObject obj : list ) {
                out.writeNext(obj.getObjetAsStringArray());
            }
        }
    }

    /* Clean up*/
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        connection.commit();
        connection.close();
    }
}
