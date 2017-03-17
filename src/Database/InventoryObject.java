package Database;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Class creates an inventory object typically read from database
 * Created by WillDeJs on 1/6/2017.
 */
public class InventoryObject {

    /**
     * Build an Invetory Object
     * @param producto      producto ID
     * @param descripcion   product description
     * @param comentario    product comment
     * @param restantes     number of products left
     */
    public  InventoryObject (int id, String producto, String descripcion, int restantes, double precio, String comentario) {

        this.product       = new SimpleStringProperty(producto);
        this.description    = new SimpleStringProperty(descripcion);
        this.comment     = new SimpleStringProperty(comentario);
        this.stock      = new SimpleIntegerProperty(restantes);
        this.price         = new SimpleDoubleProperty(precio);
        this.id             = new SimpleIntegerProperty(id);

    }

    public int getId() {
        return id.get();
    }

    public SimpleIntegerProperty idProperty() {
        return id;
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public double getPrice() {
        return price.get();
    }

    public SimpleDoubleProperty preceProperty() {
        return price;
    }

    public void setPrice(double price) {
        this.price.set(price);
    }

    public String getProduct() {
        return product.get();
    }

    public SimpleStringProperty productProperty() {
        return product;
    }

    public void setProduct(String product) {
        this.product.set(product);
    }

    public String getDescription() {
        return description.get();
    }

    public SimpleStringProperty descriptionProperty() {
        return description;
    }

    public void setDescripcion(String description) {
        this.description.set(description);
    }

    public String getComment() {
        return comment.get();
    }

    public SimpleStringProperty commentProperty() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment.set(comment);
    }

    public int getStock() {
        return stock.get();
    }

    public SimpleIntegerProperty stockProperty() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock.set(stock);
    }

    public String toString(){
        return "[" + getProduct() + ", " + getDescription() + ", " + getPrice() +
                ", " + getStock() + ", " + getComment() +"]";
    }
    public String[] getObjectAsStringArray(){
        return new String[]{getProduct(),
                getDescription(),
                String.valueOf(getStock()),
                String.valueOf(getPrice()),
                getComment()
        };
    }

    /* private properties*/
    private SimpleStringProperty product ;
    private SimpleStringProperty description;
    private  SimpleStringProperty comment;
    private SimpleIntegerProperty stock;
    private SimpleDoubleProperty price;
    private SimpleIntegerProperty id;


}
