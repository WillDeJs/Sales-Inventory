package Database;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Class creates a transaction record containing all transaction information 
 * Including customer, product, amount, total and date
 * Created by WillDeJs on 1/9/2017.
 */
public class TransactionInfoObject {
    public TransactionInfoObject(int id, String customer, String transaction, double balance, String date) {
        this.customer = new SimpleStringProperty(customer);
        this.transaction = new SimpleStringProperty(transaction);
        this.balance = new SimpleDoubleProperty(balance);
        this.date = new SimpleStringProperty(date);
        this.id = new SimpleIntegerProperty(id);
    }

    /**
     * Retrieve customer
     * @return customer name
     */
    public String getCustomer() {
        return customer.get();
    }

    /**
     * Retrieve CutustomerProperty
     * @return CustomerProperty
     */
    public SimpleStringProperty customerProperty() {
        return customer;
    }

     /**
     * Set CutustomerProperty
     * 
     */
    public void setCustomer(String customer) {
        this.customer.set(customer);
    }

    /**
     * Retrieve Transaction
     * @return 
     */
    public String getTransaction() {
        return transaction.get();
    }

    /**
     * Retrieve Transaction Property
     * @return transaction
     */
    public SimpleStringProperty transactionProperty() {
        return transaction;
    }

    /**
     * Set transaction
     * @param transaction transaction
     */
    public void setTransaction(String transaction) {
        this.transaction.set(transaction);
    }

   /**
    * Retrieve ID
    * @return id
    */
    public int getId() {
        return id.get();
    }

    /**
     * Retrieve ID Property
     * @return ID Property
     */
    public SimpleIntegerProperty idProperty() {
        return id;
    }

    /**
     * Set Id
     * @param id 
     */
    public void setId(int id) {
        this.id.set(id);
    }

    /**
     * Retrieve balance
     * @return balance
     */
    public double getBalance() {
        return balance.get();
    }

    /**
     * Retrieve balance property
     * @return Balance Property
     */
    public SimpleDoubleProperty balanceProperty() {
        return balance;
    }

    /**
     * Set balance
     * @param balance balance
     */
    public void setBalance(double balance) {
        this.balance.set(balance);
    }

    /**
     * Retrieve object as a list of string
     * @return object's properties as strings
     */
    public String[] getObjetAsStringArray() {
        return new String[]{ getCustomer(),
                getTransaction(),
                String.valueOf(getBalance()),
                getDate()
        };
    }
    
    /**
     * Retrieve date
     * @return date
     */
    public String getDate() {
        return date.get();
    }

    /**
     * Retrieve date property
     * @return Date Property
     */
    public SimpleStringProperty dateProperty() {
        return date;
    }

    /**
     * Set date
     * @param date transactions date
     */
    public void setDate(String date) {
        this.date.set(date);
    }

    /**
     * Transform object to string
     * @return string representation of object
     */
    public String toString(){

        return "[" +
                getId() + ", "+
                getCustomer()+ ", " +
                getTransaction() + ", "+
                getBalance() + ", " +

                 "]";
    }

    // properties
    private SimpleStringProperty customer;
    private SimpleStringProperty transaction;
    private SimpleStringProperty date;
    private SimpleIntegerProperty id;
    private SimpleDoubleProperty balance;
}
