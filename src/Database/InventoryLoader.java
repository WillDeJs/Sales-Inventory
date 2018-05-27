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
 * Class to create InventoryLoader
 * Allows the retrieving/writing inventory objects from/to database
 * Created by WillDeJs on 1/6/2017.
 */
public class InventoryLoader  extends DatabaseLoader {
    
   
    /**
     * Default constructor to build an InventoryLoader Object
     *
     * @throws IOException
     */
    public InventoryLoader() throws IOException {
        super();
    }


    /**
     * Loads inventory from file into database
     * @params file File to contain inventory
     * @return state of transaction
     */
    public synchronized boolean loadInventory(File file) {
        try {
            // file
            CSVReader reader = new CSVReader(new FileReader(file));
            
            // delete and recreate table
            deleteInventoryTable();
            createInventoryTable();
            
            // first line of text containing headers
            // always assume header is present
            String[] header = reader.readNext();
            
            // load every line into the database
            String[] values;
            while ((values = reader.readNext()) != null) {
                // assumes the text file contains every element in order
                // ID PRODUCT DESCRIPTION STOCK PRICE COMMENT

                for (int i = 0; i < values.length; i++) {
                    if ((i == 2 || i == 3) && values[i].equals("")) {
                        values[i] = "0";
                    }
                    if (values[i].equals(""))
                        values[i] = "-";
                }
                loadEntry(values[0], values[1], Integer.parseInt(values[2].trim()),
                        Double.parseDouble(values[3].trim()), values[4]);
            }
             // close csv reader
                reader.close();
            return true;
        } catch (Exception ex) {
            // Somethign went wrong
            Trace.getTrace().log(this.getClass(), Trace.Levels.ERROR, ex);
            return false;
        }
    }
    
    /**
     * Loads inventory from file into database
     * @params file File to contain inventory
     * @return state of transaction
     */
    public synchronized boolean loadInventory(String file) {
        return loadInventory(new File(file));
    } 

    /**
     * Loads single entry in database
     * @param product     Entry product
     * @param description Entry description
     * @param stock       Entry stock
     * @param price       Entry price
     * @param comment     Entry Comment
     * @return State of update, true if successful, false otherwise
     */
    public synchronized boolean loadEntry(String product, String description,
            int stock, double price, String comment) throws Exception{

        boolean result = false;
        // query to execute
        String query = "INSERT INTO INVENTORY (PRODUCT, DESCRIPTION, STOCK, PRICE,"
                + " COMMENT)" + " values ( ?, ?, ?, ?, ?);";

 
            PreparedStatement preparedStatement = getDBConnection().prepareStatement(query);

            // setup arguments
            preparedStatement.setString(1, product);
            preparedStatement.setString(2, description);
            preparedStatement.setInt(3, stock);
            preparedStatement.setDouble(4, price);
            preparedStatement.setString(5, comment);

            // execute query
            result = preparedStatement.execute();
            preparedStatement.close();
            return result;
    }

    /**
     * Updates a existing entry in database
     *
     * @param id          Inventory entry ID
     * @param product     Entry product
     * @param description Entry description
     * @param stock       Entry stock
     * @param price       Entry price
     * @param comment     Entry Comment
     * @return State of update, true if successful
     * @throws SQLException
     */
    public synchronized boolean updateEntry(int id, String product,
        String description, int stock, double price, String comment) {
        int changes = 0;
        // query to execute
        String query = "UPDATE INVENTORY  SET PRODUCT=?, DESCRIPTION=?, STOCK=?,"
                + " PRICE=?, COMMENT=? WHERE ID=?;";
        try {
             
            // Prepared statement to query database
            PreparedStatement preparedStatement = getDBConnection().prepareStatement(query);

            //setup arguments
            preparedStatement.setString(1, product);
            preparedStatement.setString(2, description);
            preparedStatement.setInt(3, stock);
            preparedStatement.setDouble(4, price);
            preparedStatement.setString(5, comment);
            preparedStatement.setInt(6, id);

            // execute query
            changes = preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch(Exception ex ){
            // log error
            Trace.getTrace().log(this.getClass(), Trace.Levels.ERROR, ex);
        }
        return changes > 0;

    }

    /**
     * Retrieves inventory from database
     *
     * @return list of InventoryObjects, null if reading fails
     */
    public synchronized ObservableList<InventoryObject> getInvetory() {
        ObservableList<InventoryObject> inventoryList = FXCollections.observableArrayList();
        String query = "SELECT * FROM INVENTORY;";
        try {
            Statement statement = getDBConnection().createStatement();
            ResultSet result = statement.executeQuery(query);
            
            while (result.next()) {
                inventoryList.add(new InventoryObject(
                        result.getInt(1),
                        result.getString(2),
                        result.getString(3),
                        result.getInt(4),
                        result.getDouble(5),
                        result.getString(6)));
            }
            result.close();
        } catch (Exception ex) {
            // log error
            Trace.getTrace().log(this.getClass(), Trace.Levels.ERROR, ex);
            return null;
        }
        return inventoryList;
    }

    /**
     * Updates an InventoryObject to database
     *
     * @param iv InventoryObject containing data to be changed
     * @return state of loading true if successful, false otherwise
     */
    public synchronized boolean updateEntry(InventoryObject iv) {
        try {
            return updateEntry(iv.getId(), iv.getProduct(),
                    iv.getDescription(), iv.getStock(),
                    iv.getPrice(), iv.getComment());

        } catch (Exception ex) {
            // log error
            Trace.getTrace().log(this.getClass(), Trace.Levels.ERROR, ex);
            return false;
        }
    }

    /**
     * Loads an InventoryObject in database
     *
     * @param iv InventoryObject containing data to be changed
     * @return state of loading true if successful
     */
    public synchronized boolean loadEntry(InventoryObject iv) {
        try {
            return loadEntry(iv.getProduct(),
                    iv.getDescription(), iv.getStock(),
                    iv.getPrice(), iv.getComment());

        } catch (Exception ex) {
            // log error
            Trace.getTrace().log(this.getClass(), Trace.Levels.ERROR, ex);
            return false;
        }
    }

    /* Helper  to delete table contents*/
    private synchronized boolean deleteInventoryTable() throws SQLException {
        String query = "DROP TABLE IF EXISTS 'INVENTORY';";
        Statement statement = getDBConnection().createStatement();
        boolean result = statement.execute(query);
        statement.close();
        return result;
    }

    /* Helper function to recreate empty table*/
    private synchronized boolean createInventoryTable() throws SQLException {
        String query = "CREATE TABLE IF NOT EXISTS'INVENTORY' (" +
                " 'ID' INTEGER PRIMARY KEY AUTOINCREMENT, " +
                " 'PRODUCT' TEXT NOT NULL, " +
                " 'DESCRIPTION' TEXT, " +
                " 'STOCK' TEXT, " +
                " 'PRICE' REAL, " +
                " 'COMMENT' TEXT " +
                ");";
        Statement statement = getDBConnection().createStatement();
        boolean result = statement.execute(query);
        statement.close();
        return result;
    }

    /**
     * Query database based on unset input
     * @param st    string to search for
     * @return      list of inventory objects, null if  no result
     */
    public synchronized ObservableList<InventoryObject> searchInventory(String st) {
        String query = "SELECT * FROM INVENTORY WHERE PRODUCT LIKE '%" + st
                + "%' OR DESCRIPTION LIKE '%" + st + "%';" ;
        ArrayList<InventoryObject> list = new ArrayList<>();
        try {

            Statement statement = getDBConnection().createStatement();
            ResultSet result = statement.executeQuery(query);

            while (result.next()) {
                list.add(new InventoryObject(
                        result.getInt(1),
                        result.getString(2),
                        result.getString(3),
                        result.getInt(4),
                        result.getDouble(5),
                        result.getString(6)));
            }
            result.close();
            return FXCollections.observableArrayList(list);
        } catch (Exception ex) {
            // log error
            Trace.getTrace().log(this.getClass(), Trace.Levels.ERROR, ex);
            return null;
        }

    }

    /**
     * Deletes an Inventory entry from the database
     * @param id    ID of the entry to be deleted
     * @return      True if deletion was successful, false otherwise
     *
     */
    public synchronized boolean deleteEntry(int id)  {
        boolean result = false;
        // query to execute
        String query = "DELETE FROM INVENTORY WHERE ID=?;";

        try {
            PreparedStatement preparedStatement = getDBConnection().prepareStatement(query);

            //setup arguments
            preparedStatement.setInt(1, id);

            // execute query
            int changes = preparedStatement.executeUpdate();
            preparedStatement.close();
            if (changes != 0) {
                result = true;
            }
        } catch(Exception ex ){
            // log error
            Trace.getTrace().log(this.getClass(), Trace.Levels.ERROR, ex);
        }
        return result;

    }

    /**
     * Retrieves an entry from the database in the form of InventoryObject
     * @param id     Id of the entry to retrieve
     * @return       inventory object with corresponding id, null if not found
     */
    public synchronized InventoryObject getEntry (int id) {
        InventoryObject inventoryObject = null;
        // query to execute
        try {
            String query = "SELECT * FROM INVENTORY WHERE ID=" + id +";";

            Statement preparedStatement = getDBConnection().createStatement();

            // execute query
            ResultSet result = preparedStatement.executeQuery(query);
            if (result.next()) {
                inventoryObject = new InventoryObject(result.getInt(1),
                        result.getString(2),
                        result.getString(3),
                        result.getInt(4),
                        result.getDouble(5),
                        result.getString(6));
            }
            result.close();
            preparedStatement.close();
        } catch (Exception ex) {
            // log error
            Trace.getTrace().log(this.getClass(), Trace.Levels.ERROR, ex);
        }
        return inventoryObject ;
    }
    /**
     * Exports inventory to file
     * @params  f output file to contain csv version of inventory
     * @throws Exception
     */
    public synchronized void exportInventory(File f) throws Exception{
        ObservableList<InventoryObject> list = getInvetory();
        try (CSVWriter out = new CSVWriter(new PrintWriter(f))) {
            out.writeNext("PROdUCT, DESCRIPTION, STOCK, PRICE, COMMENT".split(","));
            for (InventoryObject obj : list ) {
                out.writeNext(obj.getObjectAsStringArray());
            }
        }
    }
}