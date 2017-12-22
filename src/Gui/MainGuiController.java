package Gui;

import Utilities.Trace;
import Utilities.BusinessProperties;
import Database.SalesEntryObject;
import Database.TransactionInfoLoader;
import Database.InventoryObject;
import Database.InventoryLoader;
import Database.TransactionInfoObject;
import Database.UserInfoObject;
import Utilities.RuntimeInfo;
import java.awt.Desktop;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.print.PrinterJob;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.converter.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.layout.HBox;

/**
 * Controller for MainGui/ Main window Created by WillDeJs on 12/3/2016.
 */
public class MainGuiController implements Initializable {

    private enum InventoryUpdate {
        PRODUCT, DESCRIPTION, PRICE, STOCK, COMMENT
    }
    private Stage stage; // stage for the window

    /**
     * Constructor builds a MainGuiController instance
     */
    public MainGuiController() {
        userIsAdmin = new SimpleBooleanProperty(RuntimeInfo.getLoggedUser().isAdmin());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        //close menu
        closeMenuItem.setAccelerator(KeyCombination.keyCombination("CTRL + F4"));

        // make user editing/record only when user is admin
        manageUsersMenuItem.visibleProperty().bind(userIsAdmin);
        addProductLabel.visibleProperty().bind(userIsAdmin);
        addProductHbox.visibleProperty().bind(userIsAdmin);

        //setUp tabs
        setUpRecordSaleTab();
        setUpInventoryTab();
        setUpSaleTab();
    }

    /* Setup sales record tab */
    private void setUpRecordSaleTab() {

        // sales record table set up
        recordCustomerColumn.setCellValueFactory(new PropertyValueFactory<TransactionInfoObject, String>("customer"));
        recordTransactionColumn.setCellValueFactory(new PropertyValueFactory<TransactionInfoObject, String>("transaction"));
        recordTotalColumn.setCellValueFactory(new PropertyValueFactory<TransactionInfoObject, Integer>("balance"));
        recordDateColumn.setCellValueFactory(new PropertyValueFactory<TransactionInfoObject, String>("date"));

        // deleting/printing a transaction
        MenuItem deleteItem = new MenuItem("Delete Transaction");
        MenuItem printItem = new MenuItem("Print Transaction");
        
        // only visible if user is admin
        deleteItem.visibleProperty().bind(userIsAdmin);
        ContextMenu recordContextMenu = new ContextMenu(printItem, deleteItem);

        printItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                TransactionInfoObject cb = (TransactionInfoObject) recordSalesTable.getSelectionModel().getSelectedItem();
                try {
                    showReceiptPrintDialog(cb.getCustomer(), cb.getTransaction(),
                            cb.getBalance(), cb.getDate());
                } catch (Exception ex) {
                    Trace.getTrace().log(getClass(), Trace.Levels.ERROR,
                            "Error printing receipt", ex);
                }
            }
        });

        deleteItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                deleteSalesRecordEntry();
            }
        });

        recordSalesTable.setContextMenu(recordContextMenu);
        recordQueryTextField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                // update table if backspace is pressed and textfield is empty
                if (event.getCode().equals(KeyCode.BACK_SPACE)) {
                    if (recordQueryTextField.getText().equals("")) {
                        updateRecordTable();
                        return;
                    }
                }
                // update field when enter is pressed
                if (event.getCode().equals(KeyCode.ENTER)) {
                    if (!recordQueryTextField.getText().equals("")) {
                        querySalesRecord(new ActionEvent());
                    } else {
                        updateRecordTable();
                    }
                }
            }
        });

        // update record table
        updateRecordTable();
    }

    /* Delete selected sales record */
    private void deleteSalesRecordEntry() {
        UserInfoObject user = (UserInfoObject) RuntimeInfo.getLoggedUser();
        try {
            if (user.isAdmin()) {
                TransactionInfoLoader cm = new TransactionInfoLoader();
                TransactionInfoObject obj = (TransactionInfoObject) recordSalesTable.getSelectionModel().getSelectedItem();
                if (obj != null) {
                    cm.deleteEntry(obj.getId());
                    recordSalesTable.getItems().remove(recordSalesTable.getSelectionModel().getSelectedIndex());
                    updateRecordTable(); // update table with changes

                    // log change in record
                    Trace.getTrace().log(getClass(), Trace.Levels.INFO, user.getUserName()
                            + " deleted entry from sales"
                            + obj.getCustomer() + " "
                            + obj.getDate() + " " + obj.getTransaction());
                    stage.close();
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION,
                        "User does not have permission to execute action.");
                alert.setTitle("Insufficient Permissions");
                alert.setHeaderText("Insufficient Permissions");
                alert.showAndWait();

            }
        } catch (Exception ex) {
            Trace.getTrace().log(getClass(), Trace.Levels.ERROR, ex);
        }
    }


    /* Helper to update record table*/
    private void updateRecordTable() {
        try {
            TransactionInfoLoader cl = new TransactionInfoLoader();
            reloadRecordTable(cl);
        } catch (Exception ex) {
            Trace.getTrace().log(getClass(), Trace.Levels.ERROR, ex);
        }
    }

    /* Helper to reload recordTable */
    private void reloadRecordTable(TransactionInfoLoader cl) {
        recordSalesTable.getItems().clear();
        recordSalesTable.getItems().addAll(cl.getRecord());
    }

    /* Helper to ensure integer is inputed in text fields */
    private EventHandler<KeyEvent> integerValidation() {
        return new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (!event.getCharacter().matches("[0-9]")) {
                    event.consume();
                }

            }
        };
    }

    /* Set up sales tab */
    private void setUpSaleTab() {

        // add event fielters to text fields, limit their input to numbers only
        salesQuantityTextField.addEventFilter(KeyEvent.KEY_TYPED, integerValidation());
        salesItemIdTextField.addEventFilter(KeyEvent.KEY_TYPED, integerValidation());
        stockTextField.addEventFilter(KeyEvent.KEY_TYPED, integerValidation());
        salesQuantityTextField.addEventFilter(KeyEvent.KEY_TYPED, integerValidation());

        // total text is shown blod and in italic at slightly bigger font
        totalText.setFont(Font.font("Monospaced", FontWeight.BOLD, FontPosture.ITALIC, 16));

        // add context menu to delete an item from table upon right click
        ContextMenu deleteContextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Delete");
        if (!RuntimeInfo.getLoggedUser().isAdmin()) {
            deleteItem.disableProperty().setValue(true);
        }
        deleteItem.setOnAction(event -> {
            SalesEntryObject cobj = (SalesEntryObject) customerInfoTable
                    .getSelectionModel()
                    .getSelectedItem();
            customerInfoTable.getItems().remove(customerInfoTable
                                        .getSelectionModel()
                                        .getSelectedIndex()
            );
            updateSalesTotal();

        });

        // customer text field entere is pressed process sale
        customerNameTextField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode().equals(KeyCode.ENTER)) {
                    processSale(new ActionEvent());
                }
            }
        });

        // sales entry information dealings
        deleteContextMenu.getItems().add(deleteItem);
        customerInfoTable.setContextMenu(deleteContextMenu);
        salesItemIdTextField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode().equals(KeyCode.ENTER)) {
                    addItemToSales(new ActionEvent());
                }
            }
        });

        // quantity text field when enter is pressed, item is added to sales talbe
        salesQuantityTextField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode().equals(KeyCode.ENTER)) {
                    addItemToSales(new ActionEvent());
                }
            }
        });

        // set cell factories to contain correct values and rows to contain sales entry objects
        salesProductColumn.setCellValueFactory(new PropertyValueFactory<SalesEntryObject, String>("product"));
        salesDescriptionColumn.setCellValueFactory(new PropertyValueFactory<SalesEntryObject, String>("description"));
        salesQuantityColumn.setCellValueFactory(new PropertyValueFactory<SalesEntryObject, Integer>("quantity"));
        salesUnitPriceColumn.setCellValueFactory(new PropertyValueFactory<SalesEntryObject, Double>("unitPrice"));
        salesLinePriceColumn.setCellValueFactory(new PropertyValueFactory<SalesEntryObject, Double>("linePrice"));

        // sales delete column to contain delete button when hovered over
        salesDeleteColumn.setCellFactory(new Callback<TableColumn, TableCell>() {
            @Override
            public TableCell call(TableColumn param) {
                return new ButtonCellFactory(customerInfoTable);
            }
        });
    }

    /* Set up Inventory tab */
    private void setUpInventoryTab() {

        // insertion to database text fields configuration
        priceTextField.addEventFilter(KeyEvent.KEY_TYPED, numericInputValidation());
        stockTextField.addEventFilter(KeyEvent.KEY_TYPED, numericInputValidation());

        // when pressed enter on text field it will search
        inventoryQuery.addEventFilter(KeyEvent.KEY_PRESSED, getInventoryQueryEventFilter());

        // set context menu for deletion of an item upon right click.
        // only show if user is admin
        ContextMenu deleteContextMenu1 = new ContextMenu();
        MenuItem deleteItem1 = new MenuItem("Delete");
        deleteItem1.visibleProperty().bind(userIsAdmin);
        deleteItem1.setOnAction(getDeleteItemActionEventEventHandler());
        deleteContextMenu1.getItems().add(deleteItem1);
        inventoryTableView.setContextMenu(deleteContextMenu1);

        // table cell value factory for inventory objects and appropiate types
        idColumn.setCellValueFactory(new PropertyValueFactory<InventoryObject, Integer>("id"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<InventoryObject, Integer>("price"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<InventoryObject, Double>("stock"));
        commentColumn.setCellValueFactory(new PropertyValueFactory<InventoryObject, String>("comment"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<InventoryObject, String>("description"));
        productColumn.setCellValueFactory(new PropertyValueFactory<InventoryObject, String>("product"));
        commentColumn.setCellFactory(TextFieldTableCell.forTableColumn());

        // Set columns cell factory to proper types
        priceColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        stockColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        descriptionColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        productColumn.setCellFactory(TextFieldTableCell.forTableColumn());

        // When enter or backspace are pressed we query inventory
        inventoryQuery.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode().equals(KeyCode.ENTER)) {
                    queryInventory(new ActionEvent());
                }
                if (event.getCode().equals(KeyCode.BACK_SPACE)) {
                    queryInventory(new ActionEvent());
                }
            }
        });

        /*
        * Change of plans, only set every column as editable if the logged user
        * has administrator privileges
        */
        inventoryTableView.editableProperty().bind(userIsAdmin);
        priceColumn.editableProperty().bind(userIsAdmin);
        commentColumn.editableProperty().bind(userIsAdmin);
        stockColumn.editableProperty().bind(userIsAdmin);;
        productColumn.editableProperty().bind(userIsAdmin);
        descriptionColumn.editableProperty().bind(userIsAdmin);

        // Therefore their changes are only committed if user has privileges
        commentColumn.setOnEditCommit(columnCommitEventHandler(InventoryUpdate.COMMENT));
        priceColumn.setOnEditCommit(columnCommitEventHandler(InventoryUpdate.PRICE));
        stockColumn.setOnEditCommit(columnCommitEventHandler(InventoryUpdate.STOCK));
        descriptionColumn.setOnEditCommit(columnCommitEventHandler(InventoryUpdate.DESCRIPTION));
        productColumn.setOnEditCommit(columnCommitEventHandler(InventoryUpdate.PRODUCT));

        // load initial inventory
        reloadInventory();
    }

    /**
     * Helper, to provided an EventHandler for when changes are committed to a
     * cell.
     *
     * @param update Column being updated (comment,product.. etc)
     * @return EventHandler for the commit
     */
    private EventHandler<TableColumn.CellEditEvent> columnCommitEventHandler(InventoryUpdate update) {
        return new EventHandler<TableColumn.CellEditEvent>() {
            @Override
            public void handle(TableColumn.CellEditEvent event) {
                updateTableRow(event, update);
            }
        };
    }

    /* Event filter for searching inventory. When enter is pressed, query is 
        executed*/
    private EventHandler<KeyEvent> getInventoryQueryEventFilter() {
        return new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode().equals(KeyCode.ENTER)) {
                    queryInventory(new ActionEvent());
                }
            }
        };
    }

    /* Event handler for when an inventory entry is to be deleted.*/
    private EventHandler<ActionEvent> getDeleteItemActionEventEventHandler() {
        return event -> {
            InventoryObject cobj = (InventoryObject) inventoryTableView
                                    .getSelectionModel()
                                    .getSelectedItem();
            
            // dont do anything if list is empty
            if (inventoryTableView.getItems().size() < 1)
                return;
            Alert confirmDelete = new Alert(Alert.AlertType.CONFIRMATION,
                    "Are you sure you would like permanently delete the product "
                    + cobj.getProduct() + "?", ButtonType.NO, ButtonType.YES);
            Button yes = (Button) confirmDelete.getDialogPane()
                                               .lookupButton(ButtonType.YES);
            yes.setDefaultButton(false);
            yes.setText("Yes");

            confirmDelete.setHeaderText("Delete Product");
            confirmDelete.setTitle("Delete Product?");
            Optional<ButtonType> deleted = confirmDelete.showAndWait();
            if (!deleted.isPresent()) {
                return;
            }
            if (deleted.get().getText().equals("No")) {
                return;
            }
            try {
                inventoryTableView.getItems().remove(inventoryTableView
                        .getSelectionModel()
                        .getSelectedIndex());
                InventoryLoader iv = new InventoryLoader();
                iv.deleteEntry(cobj.getId());
            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR,
                        "Error deleting entry!");
                alert.setHeaderText("Could not delete entry.");
                alert.setTitle("Error");
                alert.showAndWait();
                // log error
                Trace.getTrace().log(this.getClass(), Trace.Levels.ERROR,
                        "Could not delete database entry");
            }
        };
    }

    /**
     * Show the main GUI window
     *
     * @throws IOException
     */
    public void showMainGui() throws IOException {
        Parent userGui = FXMLLoader.load(getClass().getResource("MainGui.fxml"));
        // create new window and add the MainGui to it
        Stage stage = new Stage();
        stage.setScene(new Scene(userGui));

        try {
            stage.setTitle(BusinessProperties.getProperty("BUSINESS") 
                    + " - Administration Program");
            FileInputStream image = new FileInputStream("resources" 
                    + File.separator 
                    + "icon.png"); 
            Image icon = new Image(image);
            stage.getIcons().addAll(icon);
        } catch (Exception ex) {
            Trace.getTrace().log(this.getClass(), Trace.Levels.ERROR, ex);
        }
        stage.setMinWidth(1100);
        stage.setMinHeight(700);
        stage.showAndWait();
    }

    /**
     * Exit application
     *
     * @param event contains information about the event triggered to close the
     * application
     *
     */
    public void closeApp(ActionEvent event) {
        Platform.exit();
    }

    /**
     * Ensures that TextField only accepts numbers
     *
     */
    public EventHandler<KeyEvent> numericInputValidation() {
        return new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (!event.getCharacter().matches("[0-9.]")) {
                    event.consume();
                } // if text field cotans a . period, only accept one
                else if (event.getCharacter().matches("\\.")) {
                    TextField textField = (TextField) event.getSource();
                    if (textField.getText().contains(".")) {
                        event.consume();
                    }

                }
            }
        };

    }

    /**
     * Opens Change password dialog.
     *
     * @param event contains information about the event triggered to close the
     * application
     */
    public void passwordChangePrompt(ActionEvent event) throws Exception {
        PasswordChangeGuiController pd = new PasswordChangeGuiController();
        pd.showPasswordChangeDialog();
    }

    /**
     * Opens the manage users dialog
     *
     * @param event Event that triggered action
     * @throws Exception
     */
    public void showManageUsersDialog(ActionEvent event) throws Exception {
        UserManagementController userMngt = new UserManagementController();
        userMngt.showUserManagementDialog();
    }

    /**
     * Logs a user out of the system.
     *
     * @param event Event that triggered the action
     */
    public void logOut(ActionEvent event) throws IOException {
        LoginController lg = new LoginController();
        tabPane.getScene().getWindow().hide();
        lg.showLoginWindow();
        Trace.getTrace().log(this.getClass(), Trace.Levels.INFO, "User logout");
    }

    /**
     * Imports an inventory from a csv, dat file
     *
     * @param event Event that triggers the action from menu selection
     */
    public void importInventory(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Inventory to Import");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV", "*.csv"),
                new FileChooser.ExtensionFilter("All", "*.*")
        );
        File file = fileChooser.showOpenDialog(stage);

        try {
            if (file != null) {
                InventoryLoader iv = new InventoryLoader();
                iv.loadInventory(file);
                inventoryTableView.getItems().clear();
                reloadInventory();
                Alert success = new Alert(Alert.AlertType.INFORMATION,
                        "Inventory imported successfully.");
                success.setHeaderText("Importing from  " + file.getName() + ".");
                success.setTitle("Importing Inventory");
                success.showAndWait();
            }
        } catch (Exception ex) {
            Alert failure = new Alert(Alert.AlertType.ERROR, "Inventory imported"
                    + " successfully.");
            failure.setHeaderText("Importing inventory to " + file.getName()
                    + " has failed. Ensure file exists and has correct format.");
            failure.setTitle("Inventory not Imported");
            failure.showAndWait();
            // log error
            Trace.getTrace().log(this.getClass(), Trace.Levels.ERROR,
                    "Could not reload database using file " + file.getName());
        }
    }

    /**
     * reload the inventory table with values from database
     */
    private void reloadInventory() {
        try {
            InventoryLoader iv = new InventoryLoader();
            inventoryTableView.getItems().clear();
            inventoryTableView.getItems().addAll(iv.getInvetory());
            Trace.getTrace().log(this.getClass(), Trace.Levels.INFO,
                    "Inventory retrieved from database successfully");
        } catch (Exception ex) {
            Trace.getTrace().log(this.getClass(), Trace.Levels.ERROR, ex);
            ex.printStackTrace();
        }

    }

    /**
     * Determine what change occurred in the table and update the database
     * accordingly
     */
    private boolean updateTableRow(CellEditEvent event, InventoryUpdate element) {

        // Prompt user to accept or cancell change to inventory
        Alert change = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to change "
                + event.getOldValue() + " to " + event.getNewValue() + "?",
                new ButtonType("Yes"), new ButtonType("No"));
        change.setHeaderText("Confirm change");
        change.setTitle("Change Product");
        Optional<ButtonType> result = change.showAndWait();

        // if user doesnt accept change dont execute
        if (!result.isPresent()) {
            event.consume();
            reloadInventory();
            return false;
        }

        // if user doesnt accept change dont execute
        if (result.get().getText().equals("No")) {
            event.consume();
            reloadInventory();
            return false;
        }

        // update invetory loader and load object to database
        try {
            InventoryObject inventoryObject = (InventoryObject) event.getRowValue();
            switch (element) {
                case PRODUCT:
                    inventoryObject.setProduct(event.getNewValue().toString());
                    break;
                case DESCRIPTION:
                    inventoryObject.setDescripcion(event.getNewValue().toString());
                    break;
                case PRICE:
                    inventoryObject.setPrice((Double) event.getNewValue());
                    break;
                case COMMENT:
                    inventoryObject.setComment(event.getNewValue().toString());
                    break;
                case STOCK:
                    inventoryObject.setStock((Integer) event.getNewValue());
                    break;
                default:
                    break;
            }

            InventoryLoader iv = new InventoryLoader();
            //boolean result = iv.updateObjectEntry(inventoryObject);
            if (!iv.updateEntry(inventoryObject)) {
                throw new Exception("Failed to load object in database");
            }
            inventoryTableView.getItems().clear();
            reloadInventory();
            Trace.getTrace().log(this.getClass(), Trace.Levels.INFO,
                    "Enty ID: "
                    + inventoryObject.getId()
                    + " successfully updated");
            return true;
        } catch (Exception ex) {
            Alert alerta = new Alert(Alert.AlertType.ERROR,
                    "Error updating database "
                    + "Hint: verify entered values");
            alerta.setHeaderText("Error updating database");
            alerta.setTitle("Error");
            alerta.showAndWait();
            Trace.getTrace().log(this.getClass(), Trace.Levels.ERROR, ex);
            return false;
        }
    }

    /**
     * Retrieves a user define query from the user
     *
     * @param event triggered event
     */
    public void queryInventory(ActionEvent event) {
        try {
            InventoryLoader iv = new InventoryLoader();
            ObservableList<InventoryObject> list = null;
            String query = inventoryQuery.getText();
            if (!query.equals("")) {
                list = iv.searchInventory(query);
                inventoryTableView.getItems().clear();
                inventoryTableView.getItems().addAll(list);

            } else {
                list = iv.getInvetory();
                inventoryTableView.getItems().clear();
                inventoryTableView.getItems().addAll(list);
            }
        } catch (Exception ex) {
            Trace.getTrace().log(this.getClass(), Trace.Levels.ERROR, ex);
            new Alert(Alert.AlertType.ERROR, "failed search").showAndWait();
        }
    }

    /**
     * Action executed when user adds entry to inventory table
     *
     * @param event Triggered event
     */
    public void addToInventory(ActionEvent event) {
        String product = productTextField.getText();
        String description = descriptionTextField.getText().equals("") ? "-" 
                : descriptionTextField.getText();
        String comment = commentTextField.getText().equals("") 
                ? "-" 
                : commentTextField.getText();

        double price = priceTextField.getText().equals("") 
                ? 0.0 
                : Double.parseDouble(priceTextField.getText().trim());
        int stock = stockTextField.getText().equals("") 
                ? 0 
                : (int) Double.parseDouble(stockTextField.getText().trim());

        if (product.equals("")) {
            Tooltip tip;
            tip = new Tooltip("Product cannot be empty.");
            tip.setAutoHide(true);
            tip.show(salesAddButton.getScene().getWindow());
            return;
        } else {
            try {
                InventoryLoader iv = new InventoryLoader();
                iv.loadEntry(product, description, stock, price, comment);
                inventoryTableView.getItems().clear();
                inventoryTableView.getItems().addAll(iv.getInvetory());
                // clear text fields after update
                productTextField.clear();
                descriptionTextField.clear();
                stockTextField.clear();
                priceTextField.clear();
                commentTextField.clear();

            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR,
                        "Cannot update database. "
                        + "Make sure entered data is correct.");
                alert.setTitle("Error, could not update databas. ");
                alert.setHeaderText("Update Failed.");
                alert.showAndWait();
                // log error
                Trace.getTrace().log(this.getClass(), Trace.Levels.ERROR, ex);
            }

        }
    }

    /**
     * Action executed when the user selects export inventory
     *
     * @param event Triggered event
     * @throws Exception
     */
    public void exportInventory(ActionEvent event) throws Exception {
        InventoryLoader iv = new InventoryLoader();
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV", "*.csv"),
                new FileChooser.ExtensionFilter("All", "*.*")
        );
        fileChooser.setTitle("Exporta Inventory");
        File file = fileChooser.showSaveDialog(new Stage());
        if (file != null) {
            iv.exportInventory(file);
        }
    }

    /**
     * Handles event triggered by user adding item to sales account
     *
     * @param event Event triggered by action
     */
    public void addItemToSales(ActionEvent event) {
        String id = salesItemIdTextField.getText();
        String q = salesQuantityTextField.getText();

        SalesEntryObject salesObj = null;
        InventoryObject obj = null;
        Tooltip tip = null;
        if (id.equals("")) {
            tip = new Tooltip("ID cannot be empty.");
            tip.setAutoHide(true);
            tip.show(salesAddButton.getScene().getWindow());
            return;
        }
        if (q.equals("")) {
            tip = new Tooltip("Amount cannot be empty.");
            tip.setAutoHide(true);
            tip.show(salesAddButton.getScene().getWindow());
            return;
        }
        try {
            InventoryLoader iv = new InventoryLoader();

            obj = iv.getEntry(Integer.parseInt(id));
            if (obj != null) {
                salesObj = new SalesEntryObject(obj.getProduct(),
                        obj.getDescription(), Integer.parseInt(q),
                        obj.getPrice(), obj.getId());
                if (Integer.parseInt(q) <= obj.getStock()) {
                    customerInfoTable.getItems().add(salesObj);
                    salesQuantityTextField.clear();
                    salesItemIdTextField.clear();
                    updateSalesTotal();
                } else {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION,
                            "Not enough " + obj.getProduct() + " in Inventory.");
                    alert.setTitle("Insufficient Inventory");
                    alert.setHeaderText("Insufficient Inventory");
                    alert.showAndWait();
                }

            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Item ID " 
                        + id + " does not exist.");
                alert.setHeaderText("Item is not in inventoryo");
                alert.setTitle("No inventory item");
                alert.showAndWait();
            }
        } catch (Exception ex) {
            // log error
            Trace.getTrace().log(this.getClass(), Trace.Levels.ERROR, ex);
        }

    }

    /**
     * Processes a sale transaction Gets inventory information from database
     * determines stock state and updates database with transaction information
     *
     * @param event User triggered event
     */
    public void processSale(ActionEvent event) {

        // if customer not specified show hint
        if (customerNameTextField.getText().equals("")) {
            Tooltip tip = new Tooltip("Customer not added!");
            tip.show(customerNameTextField.getScene().getWindow());
            tip.setAutoHide(true);
            return;
        }

        // if transaction not specified show hint
        if (customerInfoTable.getItems().size() <= 0) {
            Tooltip tip = new Tooltip("Account not added!");
            tip.show(customerNameTextField.getScene().getWindow());
            tip.setAutoHide(true);
            return;
        }

        /* prompt the user that they are about to accept a transaction */
        Alert accept = new Alert(Alert.AlertType.CONFIRMATION,
                "Process transaction?.", ButtonType.OK, ButtonType.CANCEL);
        accept.setHeaderText("Confirm Transaction");
        accept.setTitle("Confirm Transaction");

        /* make sure user accepts transaction*/
        Optional a = accept.showAndWait();
        if (!a.isPresent()) {
            return;
        }
        if (a.get() != ButtonType.OK) {
            return;
        }

        SalesEntryObject salesObj = null;
        try {
            InventoryLoader iv = new InventoryLoader();
            InventoryObject inObj = null;
            int rest = 0;

            /* Prepare transaciton/receipt string to be printed*/
            StringBuffer buffer = new StringBuffer(
                    String.format("%s\t%20s\t%-2s\n", "#", "Product:", "Price:")
            );
            for (Object obj : customerInfoTable.getItems()) {
                
                // get every object in sales table
                salesObj = (SalesEntryObject) obj;
                inObj = iv.getEntry(salesObj.getInventoryId());
                rest = inObj.getStock();
                
                // Reduce their numbers according to purchase quantity
                // Only if the number has not changed from the time it was added
                // To cart
                if (rest >= salesObj.getQuantity()) {
                    inObj.setStock(rest - salesObj.getQuantity());
                
                    // update inventory in database
                    iv.updateEntry(inObj);
                
                    // update inventory table
                    buffer.append(String.format("%d\t%20s\t%-2s\n", salesObj.getQuantity(), salesObj.getProduct(),
                        salesObj.getUnitPrice() * ((SalesEntryObject) obj).getQuantity()));
                }
                else {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION,
                                    "Not enough " + salesObj.getProduct() + " in Inventory.\n"
                    + "Please modify your transaction.");
                    alert.setTitle("Insufficient Inventory");
                    alert.setHeaderText("Insufficient Inventory");
                    alert.showAndWait();
                    return;
                }
            }

            // prepare sales transaction to be logged in database
            Date today = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

            // update inventory table
            reloadInventory();
            updateCustomerInfoDatabase(customerNameTextField.getText(), buffer.toString(),
                    Double.parseDouble(totalText.getText()), dateFormat.format(today));

            // hint that transaction was successfull
            Tooltip tip = new Tooltip("Transaction successful!");
            tip.setAutoHide(true);
            tip.show(customerInfoTable.getScene().getWindow());
            // show printing dialog
            showReceiptPrintDialog(customerNameTextField.getText(), buffer.toString(),
                    Double.parseDouble(totalText.getText()), dateFormat.format(today));

            // update sales table
            updateRecordTable();

            customerInfoTable.getItems().clear();
            customerNameTextField.clear();
            totalText.setText("0.0");
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR, 
                    "Error with Transaction");
            alert.setHeaderText("Error in transaction");
            alert.setTitle("Error");
            
            // log error
            Trace.getTrace().log(this.getClass(), Trace.Levels.ERROR,
                    "Error processing transaction.");
            alert.showAndWait();
        }
    }

    /* Update a transaction in database*/
    private void updateCustomerInfoDatabase(String customer,
            String transaction,
            double balance,
            String date) throws Exception {
        TransactionInfoLoader cl = new TransactionInfoLoader();
        cl.loadEntry(customer, transaction, balance, date);
    }

    /* Show print dialog for receipts/transactions */
    private void showReceiptPrintDialog(String customer,
            String transaction,
            double balance,
            String date) throws Exception {

        // get company properties
        String company = BusinessProperties.getProperty("BUSINESS");
        String address = BusinessProperties.getProperty("ADDRESS");
        String contact = BusinessProperties.getProperty("PHONE");

        // receipt header
        String header = String.format("\t%s\n %s\n \t%s\n\t%s\n", company,
                address, contact, date);
        Text receiptText = new Text(header + transaction + "\nTotal:\t"
                + balance + "\nCustomer:\t"
                + customer + "\nX.__________________");
        receiptText.setFont(new Font("monospace", 10));
        receiptText.setStyle("-fx-font-family: monospace");

        // create new pane and show the receipt
        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(5, 5, 5, 5));
        pane.setCenter(new ScrollPane(receiptText));

        // add ability to print the receipt, print button
        Button printButton = new Button("Print");
        printButton.setOnAction(printButtonAction(receiptText));
        pane.setBottom(printButton);

        // set window
        Stage receiptStage = new Stage();
        receiptStage.setScene(new Scene(pane));
        receiptStage.setMaxWidth(600);
        receiptStage.setMaxHeight(600);

        receiptStage.setMinWidth(450);
        receiptStage.setMinHeight(250);
        //receiptStage.setResizable(false);
        receiptStage.setTitle("Receipt");
        receiptStage.show(); // show window
    }

    /* Action execute when user clicks the printButton to print a receipt*/
    private EventHandler<ActionEvent> printButtonAction(final Text receiptText) {
        return new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                PrinterJob printer = PrinterJob.createPrinterJob();

                if (printer.showPrintDialog(receiptText.getScene().getWindow())) {
                    boolean success = printer.printPage(receiptText);
                    if (success) {
                        printer.endJob();
                    }
                }
            }
        };
    }

    /*
     * Update the total of the sale shown in gui
     */
    private void updateSalesTotal() {
        double total = getSalesTotal();
        totalText.setText(String.valueOf(total));
    }

    /* calculate total in sales table*/
    private double getSalesTotal() {
        double total = 0;
        for (Object sobj : customerInfoTable.getItems()) {
            total += ((SalesEntryObject) sobj).getLinePrice();
        }
        return total;
    }

    /**
     * Query database for sales record and return retrieved results to table
     *
     * @param event
     */
    public void querySalesRecord(ActionEvent event) {
        String query = recordQueryTextField.getText();
        try {
            TransactionInfoLoader loader = new TransactionInfoLoader();
            recordSalesTable.getItems().clear();
            recordSalesTable.getItems().addAll(loader.searchCustomerInfoList(query));
        } catch (Exception ex) {
            Trace.getTrace().log(getClass(), Trace.Levels.ERROR, ex);
        }
    }

    /**
     * Exports the sales record to a file. Action triggered by user
     *
     * @param event Event triggered by action
     */
    public void exportSalesRecord(ActionEvent event) {
        try {
            TransactionInfoLoader iv = new TransactionInfoLoader();
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("CSV", "*.csv"),
                    new FileChooser.ExtensionFilter("All", "*.*"));
            fileChooser.setTitle("Export Sales Recoprd");
            File file = fileChooser.showSaveDialog(new Stage());
            if (file != null) {
                iv.exportTransactionInfo(file);
            }
        } catch (Exception ex) {
            Trace.getTrace().log(getClass(), Trace.Levels.ERROR, ex);
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Error! Could not complete export.");
            alert.setHeaderText("Error exporting!");
            alert.setTitle("Error");
            alert.showAndWait();
        }

    }

    /**
     * Loads and shows user manual/tutorial
     *
     * @param event event triggered
     */
    public void showTutorial(ActionEvent event) {
        try {
            Desktop.getDesktop().browse((new File("help"
                    + File.separator
                    + "help.html")).toURI());
        } catch (Exception io) {
            Trace.getTrace().log(MainGuiController.class, Trace.Levels.ERROR,
                    "Could not load tutorial", io);
        }
    }

    /* Control structures */
    @FXML
    TextField customerNameTextField;
    @FXML
    TabPane tabPane;
    @FXML
    MenuItem closeMenuItem;
    @FXML
    TableColumn idColumn;
    @FXML
    TableColumn productColumn;
    @FXML
    TableColumn descriptionColumn;
    @FXML
    TableColumn stockColumn;
    @FXML
    TableColumn priceColumn;
    @FXML
    TableColumn commentColumn;
    @FXML
    TableView inventoryTableView;
    @FXML
    TableColumn editColumn;
    @FXML
    TextField inventoryQuery;
    @FXML
    TextField productTextField;
    @FXML
    TextField descriptionTextField;
    @FXML
    TextField priceTextField;
    @FXML
    TextField stockTextField;
    @FXML
    TextArea commentTextField;
    @FXML
    TextField queryCustomerInfoTextField;
    @FXML
    TableView customerInfoTable;
    @FXML
    TableColumn salesProductColumn;
    @FXML
    TableColumn salesDescriptionColumn;
    @FXML
    TableColumn salesQuantityColumn;
    @FXML
    TableColumn salesUnitPriceColumn;
    @FXML
    TableColumn salesLinePriceColumn;
    @FXML
    TableColumn salesDeleteColumn;
    @FXML
    TextField salesQuantityTextField;
    @FXML
    TextField salesItemIdTextField;
    @FXML
    Button salesAddButton;
    @FXML
    Text totalText;
    @FXML
    TableColumn recordCustomerColumn;
    @FXML
    TableColumn recordTransactionColumn;
    @FXML
    TableColumn recordDateColumn;
    @FXML
    TableColumn recordTotalColumn;
    @FXML
    RadioButton customerRadioButton;
    @FXML
    RadioButton transactionRadioButton;
    @FXML
    TextField recordQueryTextField;
    @FXML
    TableView recordSalesTable;
    @FXML
    MenuItem manageUsersMenuItem;
    @FXML
    HBox addProductHbox;
    @FXML
    Label addProductLabel;
    private SimpleBooleanProperty userIsAdmin;
}
