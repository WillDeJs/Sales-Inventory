package Gui;

import Utilities.Trace;
import Database.InventoryObject;
import Database.InventoryLoader;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;


import java.net.Inet4Address;

/**
 * Custom cell factory to include delete button on each cell
 * 
 * Created by WillDeJs on 1/7/2017.
 */
public class ButtonCellFactory extends TableCell<InventoryObject, Button> {

    private Button editButton;
/**
 * Build custom cell factory
 * @param table table the cell belongs to
 */
    public ButtonCellFactory(TableView table){
        editButton = new Button("Delete");
        editButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                TableView table = getTableView();
                InventoryObject iv = (InventoryObject) getTableRow().getItem();
                table.getItems().clear();
                try {
                    InventoryLoader loader = new InventoryLoader();
                    loader.deleteEntry(iv.getId());
                    table.getItems().addAll(loader.getInvetory());
                }catch(Exception ex){
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Could not delete selected entry.");
                    alert.setHeaderText("Error deleting entry");
                    alert.setTitle("Deletion Failed");
                    alert.showAndWait();
                    // log error
                    Trace.getTrace().log(this.getClass(), Trace.Levels.ERROR, ex);
                }
            }
        });
    }

    /**
     * Update cell item
     * @param item  item being updated
     * @param empty is cell empty?
     */
    @Override
    protected void updateItem(Button item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setGraphic(null);
        }
        else {
            setGraphic(editButton);
            getGraphic().setVisible(false);
        }

    }

}
