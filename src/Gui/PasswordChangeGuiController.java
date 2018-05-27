package Gui;

import Database.UserInfoObject;
import Database.UserManager;
import Utilities.PasswordHash;
import Utilities.RuntimeInfo;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.Label;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;


/**
 * Controller for password change gui
 * 
 * Created by WillDeJs on 12/20/2016.
 */
public class PasswordChangeGuiController implements Initializable {
    @FXML
    Button cancelButton;
    @FXML
    Button acceptButton;
    @FXML
    PasswordField oldPassword;
    @FXML
    PasswordField newPassword1;
    @FXML
    PasswordField newPassword2;
    @FXML
    Label userLabel;
    @FXML
    Label oldPasswordLabel;

    Parent root;
    Stage stage;
    String username;
    boolean oldPasswordMatters = true;

    /**
    * Constructor, build stage for password change for the given user
    * @param username to which the password is to be changed
    */
    public PasswordChangeGuiController(String username){
        stage = new Stage();
        this.username = username;
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // Hide accept button
        acceptButton.setDisable(true);
   
        // show accept button only if all fields are filled and contain more than 6 characters
        oldPassword.addEventFilter(KeyEvent.KEY_PRESSED, showChangeButton());
        newPassword1.addEventFilter(KeyEvent.KEY_PRESSED, showChangeButton());
        newPassword2.addEventFilter(KeyEvent.KEY_PRESSED, showChangeButton());
        
        userLabel.setText(username);
        userLabel.setStyle("-fx-font-weight:bold; -fx-text-fill:blue;");
        UserInfoObject loggedUser = RuntimeInfo.getLoggedUser();
        
        // If this is the admin user and its not changing its own password,
        // Dont show the "current password" field.
        if (loggedUser.isAdmin() && !loggedUser.getUserName().equals(username)) {
            oldPassword.setVisible(false);
            oldPasswordLabel.setVisible(false);
            oldPasswordMatters = false;
        }
    }
    /**
     * Action executed when the user cancels the password change dialog
     */
    public void cancelPasswordChange(ActionEvent event) {
        cancelButton.getScene().getWindow().hide();

    }

   /**
    * Action triggered when password is to be changed
    * @param event the even which triggered the password change.
    * */
    public void ChangePassword(ActionEvent event)  throws SQLException {
         /*This could better encapsulated, having a change password function in some other class being called here
        * and handling the result of the change here*/
        String newPass1 = PasswordHash.getHashedPassword(newPassword1.getText());
        String newPass2 = PasswordHash.getHashedPassword(newPassword2.getText());
        String oldPass = PasswordHash.getHashedPassword(oldPassword.getText());
        UserManager userManager = UserManager.getManager();
       
        if (oldPasswordMatters && 
                !oldPass.equals(userManager.getUser(username).getPassword())) {
                        Alert fail = new Alert(Alert.AlertType.ERROR, "Error changing password. "
                    + "Invalid existing password.");
            fail.setHeaderText("User not updated");
            fail.setTitle("Password not changed");
            fail.showAndWait();
            return;
        }

        if (newPass1.equals(newPass2)) {
            if (userManager.changePassword(username, newPass1)) {
                Alert success = new Alert(Alert.AlertType.INFORMATION, "Password Changed.");
                success.setHeaderText("User Updated");
                success.setTitle("Password Changed");
                Optional<ButtonType> ok = success.showAndWait();
                if (ok.isPresent())
                    acceptButton.getScene().getWindow().hide();

            } else {
                Alert fail = new Alert(Alert.AlertType.ERROR, "Passwords don't"
                        + " match. Make sure that the entered passwords are "
                        + "correct and are at least 6 characters in legth.");
                fail.setHeaderText("");
                fail.setTitle("Password not changed");
                fail.showAndWait();
            }
        } else {
            Alert fail = new Alert(Alert.AlertType.ERROR, "Error changing password."
                    + "Double check entered values and try again.");
            fail.setHeaderText("User not updated");
            fail.setTitle("Password not changed");
            fail.showAndWait();
        }

    }
    /*Only show change password button if text fields match minimum length*/
    private EventHandler<KeyEvent> showChangeButton(){

        return new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (newPassword1.getText().length() >= 6 &&
                        newPassword2.getText().length() >= 6) {
                    acceptButton.setDisable(false);
                }
            }
        };
    }
    /**
     * Displays the password change dialog
     * @throws IOException
     */
    public void showPasswordChangeDialog() throws IOException {
        // If no user was passed, then we are on the logged user
        FXMLLoader fxmlLoader =  new FXMLLoader(getClass().getResource("PasswordChangeGui.fxml"));
        fxmlLoader.setController(this); // need to set controller outside of fxml file to pass arguments
        Parent passwordRoot = fxmlLoader.load();
        stage.setScene(new Scene(passwordRoot));
        stage.setTitle("Change Password");
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }
}
