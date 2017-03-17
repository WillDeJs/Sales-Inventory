package Gui;

import Database.UserInfo;
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

    Parent root;
    Stage stage;

    /**
    * Constructor used in singleton structure
    *
    */
    public PasswordChangeGuiController(){
        stage = new Stage();
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // Hide accept button
        acceptButton.setDisable(true);

        // show accept button only if all fields are filled and contain more than 6 characters
        oldPassword.addEventFilter(KeyEvent.KEY_PRESSED, showChangeButton());
        newPassword1.addEventFilter(KeyEvent.KEY_PRESSED, showChangeButton());
        newPassword2.addEventFilter(KeyEvent.KEY_PRESSED, showChangeButton());
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
    public void ChangePassword(ActionEvent event) throws SQLException {
        /*This could better encapsulated, having a change password function in some other class being called here
        * and handling the result of the change here*/
        String newPass1 = PasswordHash.getHashedPassword(newPassword1.getText());
        String newPass2 = PasswordHash.getHashedPassword(newPassword2.getText());
        String oldPass = PasswordHash.getHashedPassword(oldPassword.getText());
        String username = RuntimeInfo.getLoggedUser().getUserName();
        UserInfo userInfo = new UserInfo();


        if (newPass1.equals(newPass2)) {
            if (userInfo.changePassword(username, oldPass, newPass1)) {
                Alert success = new Alert(Alert.AlertType.INFORMATION, "Password Changed.");
                success.setHeaderText("User Updated");
                success.setTitle("Password Changed");
                Optional<ButtonType> ok = success.showAndWait();
                if (ok.isPresent())
                    acceptButton.getScene().getWindow().hide();

            } else {
                Alert fail = new Alert(Alert.AlertType.ERROR, "Passwords dont"
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
                if (oldPassword.getText().length() >= 6 &&
                        newPassword1.getText().length() >= 6 &&
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
        Parent passwordRoot = FXMLLoader.load(getClass().getResource("PasswordChangeGui.fxml"));
        stage.setScene(new Scene(passwordRoot));
        stage.setTitle("Change Password");
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();

    }
}
