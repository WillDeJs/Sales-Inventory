package Database;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Class allows the creation of a sales entry for a particular item in inventory
 * Each item in a transaction is a sales entry. 
 * Created by WillDeJs on 1/13/2017.
 */
public class SalesEntryObject {

    /**
     * Constructor builds a SalesEntryObject with a transaction item information
     * @param product   Name of product
     * @param description   Product description
     * @param quantity      Quantity of products in entry
     * @param unitPrice     Price per product
     * @param inventoryId   ID of product in inventory table
     */
    public SalesEntryObject( String product, String description, int quantity,
            double unitPrice, int inventoryId) {
        this.product = new SimpleStringProperty(product);
        this.description = new SimpleStringProperty(description);
        this.linePrice = new SimpleDoubleProperty();
        this.unitPrice = new SimpleDoubleProperty(unitPrice);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.linePrice.bind(this.unitPrice.multiply(this.quantity));
        this.inventoryId = new SimpleIntegerProperty(inventoryId);
    }

    /**
     * Retrieve Inventory Id
     * @return id
     */
    public int getInventoryId() {
        return inventoryId.get();
    }

    /**
     * Retrieve Inventory Id Property
     * @return ID Property
     */
    public SimpleIntegerProperty inventoryIdProperty() {
        return inventoryId;
    }

    /**
     * Set inventory ID
     * @param inventoryId ID to set
     */
    public void setInventoryId(int inventoryId) {
        this.inventoryId.set(inventoryId);
    }
    
    /**
     * Retrieve customer
     * @return  customer
     */
    public String getCustomer() {
        return customer.get();
    }

    /**
     * Retrieve customer property
     * @return customer property
     */
    public SimpleStringProperty customerProperty() {
        return customer;
    }

    /**
     * Set customer
     * @param customer customer 
     */
    public void setCustomer(String customer) {
        this.customer.set(customer);
    }

    public String getProduct() {
        return product.get();
    }

    /**
     * Retrieve product property
     * @return product
     */
    public SimpleStringProperty productProperty() {
        return product;
    }

    /**
     * Set Customer
     * @param product customer property 
     */
    public void setProduct(String product) {
        this.product.set(product);
    }

    public String getDescription() {
        return description.get();
    }

    /**
     * Retrieve description property
     * @return  description property
     */
    public SimpleStringProperty descriptionProperty() {
        return description;
    }

    /**
     * Set inventory description
     * @param description    description
     */
    public void setDescription(String description) {
        this.description.set(description);
    }

    /**
     * Retrieve item amount
     * @return number of items 
     */
    public int getQuantity() {
        return quantity.get();
    }

    /**
     * Retrieve quantity property
     * @return  quantity property
     */
    public SimpleIntegerProperty quantityProperty() {
        return quantity;
    }

    /** Set quantity
     * 
     * @param quantity number of items
     */
    public void setQuantity(int quantity) {
        this.quantity.set(quantity);
    }

    /**
     * Retrieve Unit price
     * @return unit price
     */
    public double getUnitPrice() {
        return unitPrice.get();
    }

    /**
     * Retrieve Unit price property
     * @return unit price property
     */
    public DoubleProperty unitPriceProperty() {
        return unitPrice;
    }

    /**
     * Set unit price
     * @param unitPrice price per unit 
     */
    public void setUnitPrice(double unitPrice) {
        this.unitPrice.set(unitPrice);
    }

    /**
     * Retrieve line price
     * @return retrieve total of quantity * unit price
     */
    public double getLinePrice() {
        return linePrice.get();
    }

    /**
     * Retrieve line price property
     * @return line price property
     */
    public DoubleProperty linePriceProperty() {
        return linePrice;
    }

    /**
     * Set line price
     * @param linePrice  set line price
     */
    public void setLinePrice(double linePrice) {
        this.linePrice.set(linePrice);
    }

    // properties
    private SimpleStringProperty customer;
    private SimpleStringProperty product;
    private SimpleStringProperty description;
    private SimpleIntegerProperty quantity;
    private SimpleIntegerProperty inventoryId;
    private DoubleProperty unitPrice;
    private DoubleProperty linePrice;
}
