package Gui;

import Database.SqliteConnection;
import Database.UserInfo;
import Utilities.Trace;
import Utilities.BusinessProperties;
import Utilities.PasswordHash;
import Utilities.RuntimeInfo;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import javafx.scene.Scene;

import java.util.ResourceBundle;


/**
 * Controller for Login.fxml file
 * Created by WillDeJs on 12/3/2016.
 */
public class LoginController implements Initializable {
    //public  LoginModel loginModel = LoginModel.getInstance();
    private SqliteConnection sqliteConn = new SqliteConnection();
    @FXML
    private Label connectionStatus;

    @FXML
    private TextField userField;

    @FXML
    private TextField passwordField;

    @FXML
    private Text welcomeText ;
    
     /**
     * Constructor builds a LoginController object
     */
    public LoginController(){
    }
    /**
     * Initialize method runs when gui loads
     * @param location  location
     * @param resources  resources
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        // if connected show label text in blue, otherwise in red
        if(sqliteConn.isConnected()) {
            connectionStatus.setText("Connected to database...");
            connectionStatus.setStyle("-fx-text-fill:blue");
        }
        else {
            connectionStatus.setText("Not connected to database...");
            connectionStatus.setStyle("-fx-text-fill:red");
        }

        // add button filter for pressing ENTER to login automatically
        Parent root = connectionStatus.getParent().getParent();
        root.addEventFilter(KeyEvent.KEY_PRESSED, enterPressedLogin());
        
        welcomeText.setText("Welcome to " + BusinessProperties.getProperty("BUSINESS"));
    }

    /* Helper function, eventhandler for when enter is pressed */
    private EventHandler<KeyEvent> enterPressedLogin() {
        return new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode().equals(KeyCode.ENTER)) {
                    try {
                        login(new ActionEvent());
                        event.consume();
                    } catch (IOException e) {
                        // log error
                        Trace.getTrace().log(this.getClass(), Trace.Levels.ERROR, e);
                    }

                }
            }
            
        };
    }

    /**
     * Shows the login window for user input
     * @throws IOException
     */
    public void showLoginWindow() throws IOException{
        Parent root = FXMLLoader.load(getClass().getResource("login.fxml"));
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Log in");
        stage.setResizable(false);
        stage.centerOnScreen();
        try {
            stage.getIcons().add(new Image(
                new FileInputStream("resources" + File.separator + "icon.png")));
        } catch (Exception ex) {
            Trace.getTrace().log(getClass(), Trace.Levels.ERROR,
                    "Could not load login icons", ex);
        }
        stage.show();
        String a = new String();
    }

    /**
     * Logs a user into the system
     * @param e  event that trigger the login action
     * @throws IOException
     */
    public void login(ActionEvent e) throws IOException {
       UserInfo userInfo = new UserInfo();
       String user = userField.getText();
       String password = PasswordHash.getHashedPassword(passwordField.getText());
        if (userInfo.isValidUser(user, password)) {
             // set user information in runtime
            RuntimeInfo.setLoggedUser(userInfo.getUser(userField.getText()));
            //close current login window
            userField.getScene().getWindow().hide();
            
            //show main gui
            MainGuiController mg = new MainGuiController();
            mg.showMainGui();

            
            Trace.getTrace().log(this.getClass(), Trace.Levels.INFO,
                    userField.getText() + " logged in successfully");
            
           

        }
        else {
            new Alert(Alert.AlertType.ERROR, "Could not log in",
                    ButtonType.OK).showAndWait();
        }
    }
  
}
