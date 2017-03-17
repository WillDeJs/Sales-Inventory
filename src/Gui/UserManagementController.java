/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Gui;

import Database.UserInfo;
import Utilities.PasswordHash;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Wilmer
 */
public class UserManagementController implements Initializable {

    private enum ActionType {
        ADD, DELETE, CHANGE_PASSWORD
    };

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // load users into listview
        loadUserList();
    }

    private void loadUserList() {
        UserInfo userInfo = new UserInfo();
        ObservableList<String> list = FXCollections.observableArrayList(userInfo.getUsers().stream().map(e -> e.getUserName()).collect(Collectors.toList()));
        usersListView.setItems(list);
        
        // By Default select first element
        usersListView.getSelectionModel().select(0);
    }

    public void showUserManagementDialog() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("UserManagement.fxml"));
        Stage stage = new Stage();
        stage.setScene(new Scene(root, 500, 400));
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("User Management");
        stage.show();

    }

    /**
     * User add action triggered by user click on addUser button
     *
     * @param event Event triggered action
     */
    public void addUser(ActionEvent event) {
        UserInfo userInfo = new UserInfo();
        // create window, to stay on tup of application
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);

        // create controls
        TextField user = new TextField();
        user.setPromptText("Username");
        Label label = new Label("Add User");
        dialog.setTitle("Adding User");
        PasswordField pass = new PasswordField();
        pass.setPromptText("Enter Password");

        PasswordField pass2 = new PasswordField();
        pass2.setPromptText("Re-enter Password");

        // if enter is pressed validate user
        // if escape is pressed close window
        user.setOnKeyPressed(keyPressedEventHandler(dialog));
        pass.setOnKeyPressed(keyPressedEventHandler(dialog));
        pass2.setOnKeyPressed(keyPressedEventHandler(dialog));

        // accept and cancel button, default is accept
        Button accept = new Button("Accept");
        accept.setDefaultButton(true);
        Button cancel = new Button("Cancel");

        // execute action if enter is pressed
        accept.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                boolean result = false;
                String username = user.getText();
                String password1 = pass.getText();
                String password2 = pass.getText();

                // do passwords match? are password length right? 
                // if so change password
                if (password1.equals(password2) && password1.length() >= 6) {
                    if (!userInfo.userExists(username)) {
                        result = userInfo.addUser(user.getText(), PasswordHash.getHashedPassword(password1));
                        loadUserList();
                        
                        if (result) {
                            
                            // was change successful?
                            dialog.hide();
                             Tooltip tip = new Tooltip();
                            tip.setText("User added successfully");
                            tip.setAutoHide(true);
                            tip.show(deleteUserButton.getScene().getWindow());
                        } else {
                            
                            // change failed
                            (new Alert(Alert.AlertType.ERROR,
                                "Error adding user. Please try again.",
                                ButtonType.OK))
                                .showAndWait();
                        }
                            
                    } else {
                        
                        // user already exists
                         (new Alert(Alert.AlertType.ERROR,
                            "User already exists. Please enter new user.",
                            ButtonType.OK))
                            .showAndWait();
                    }
                    
                } else {
                    
                    // Passwords do not match
                    (new Alert(Alert.AlertType.ERROR,
                            "Error adding user, please verify passwords"
                            + " match and have a minimum length of 6 characters.",
                            ButtonType.OK))
                            .showAndWait();
                }
                
            }
        });

        // exit if escape is pressed
        cancel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                dialog.hide();
            }
        });

        // set and view stage
        HBox hBox = new HBox(20, accept, cancel);
        VBox vbox = new VBox();
        vbox.setMargin(hBox, new Insets(10, 30, 10, 30));
        vbox.setMargin(label, new Insets(10, 10, 10, 10));
        vbox.setMargin(user, new Insets(10, 10, 10, 10));
        vbox.setMargin(pass, new Insets(10, 10, 10, 10));
        vbox.setMargin(pass2, new Insets(10, 10, 10, 10));

        // add elements to box, limit stage size and show it.
        vbox.getChildren().addAll(label, user, pass, pass2, hBox);
        dialog.setScene(new Scene(vbox, 400, 300));
        dialog.setResizable(false);
        dialog.show();
    }

    /**
     * Delete user action triggered by user click on deleteUser button
     *
     * @param event Event triggered action
     */
    public void deleteUser(ActionEvent event) {
        String user = usersListView.getSelectionModel().getSelectedItem().toString();
        UserInfo userInfo = new UserInfo();
        boolean deleted = false;
        
        // if user exists and is not admin then delete
        if (!userInfo.isAdmin(user)) {
            Alert delete = new Alert(Alert.AlertType.CONFIRMATION,
                            "Are you sure you would like to delete user " + user 
                            + "?", ButtonType.YES, ButtonType.NO);
            Optional<ButtonType> result = delete.showAndWait();
            if (result.isPresent() && result.get().equals(ButtonType.YES)) {
                if (userInfo.deleteUser(user)) {
                    Tooltip tip = new Tooltip();
                    tip.setText("User deleted successfully");
                    tip.setAutoHide(true);
                    tip.show(deleteUserButton.getScene().getWindow());
                    loadUserList();
                } else {
                     Alert notDeleted = new Alert(Alert.AlertType.ERROR,
                            "Something went wrong. User " + user+ " not deleted.",
                             ButtonType.OK);
                     notDeleted.showAndWait();
                }
            }
        } else {
             Alert notDeleted = new Alert(Alert.AlertType.INFORMATION,
                            "User not deleted. Make sure user exists and is not administrator. ",
                            ButtonType.OK);
             notDeleted.showAndWait();
        }
    }

    /**
     * Change password action triggered by user click on changePassword button
     *
     * @param event Event triggered action
     */
    public void changePassword(ActionEvent event) {
        UserInfo userInfo = new UserInfo();
        // create window, to stay on tup of application
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);

        // create controls
        TextField user = new TextField();
        user.setPromptText("Username");
        Label label = label = new Label("Change Password");
        dialog.setTitle("Change Password");
        PasswordField pass = new PasswordField();
        pass.setPromptText("Current Password");
        PasswordField pass2 = new PasswordField();
        pass2.setPromptText("Enter New Password");
        PasswordField pass3 = new PasswordField();
        pass3.setPromptText("Re-enter New Password");

        // if enter is pressed validate user
        // if escape is pressed close window
        user.setOnKeyPressed(keyPressedEventHandler(dialog));
        pass.setOnKeyPressed(keyPressedEventHandler(dialog));
        pass2.setOnKeyPressed(keyPressedEventHandler(dialog));
        pass3.setOnKeyPressed(keyPressedEventHandler(dialog));

        // accept and cancel button, default is accept
        Button accept = new Button("Accept");
        accept.setDefaultButton(true);
        Button cancel = new Button("Cancel");

        user.setEditable(false);
        user.setText(usersListView.getSelectionModel().getSelectedItem().toString());

        // execute action if enter is pressed
        accept.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                boolean result = false;
                String username = user.getText();
                String currentPass = PasswordHash.getHashedPassword(pass.getText());
                String newPass = PasswordHash.getHashedPassword(pass2.getText());
                String newPass2 = PasswordHash.getHashedPassword(pass3.getText());
                if (newPass2.equals(newPass)) {
                    result = userInfo.changePassword(username, currentPass, newPass);
                } 

                // passwords dont match
                else {
                    (new Alert(Alert.AlertType.ERROR,
                            "Passwords do not match. Please try again.",
                            ButtonType.OK))
                            .showAndWait();
                }

                // if action was completed close window
                if (result) {
                    dialog.hide();
                     Tooltip tip = new Tooltip();
                    tip.setText("User updated successfully");
                    tip.setAutoHide(true);
                    tip.show(deleteUserButton.getScene().getWindow());
                } // otherwise let user know
                else {
                    (new Alert(Alert.AlertType.ERROR,
                            "Could not change password. Please verify"
                            + " entered information.",
                            ButtonType.OK)).showAndWait();
                }
            }
        });

        // exit if escape is pressed
        cancel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                dialog.hide();
            }
        });

        // set and view stage
        HBox hBox = new HBox(20, accept, cancel);
        VBox vbox = new VBox();
        vbox.setMargin(hBox, new Insets(10, 30, 10, 30));
        vbox.setMargin(label, new Insets(10, 10, 10, 10));
        vbox.setMargin(user, new Insets(10, 10, 10, 10));
        vbox.setMargin(pass, new Insets(10, 10, 10, 10));
        vbox.setMargin(pass2, new Insets(10, 10, 10, 10));
        vbox.setMargin(pass3, new Insets(10, 10, 10, 10));

        // add elements to box, limit stage size and show it.
        vbox.getChildren().addAll(label, user, pass, pass2, pass3, hBox);
        dialog.setScene(new Scene(vbox, 400, 300));
        dialog.setResizable(false);
        dialog.show();

    }

    /* Helper, creates action handler for handling keybpressed in user edit
     dialog */
    private EventHandler<KeyEvent> keyPressedEventHandler(Stage dialog) {
        return new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode().equals(KeyCode.ENTER)) {
                    if (event.getCode().equals(KeyCode.ESCAPE)) {
                        dialog.hide();
                    }
                }
            }
        };
    }
    // properties
    @FXML
    Button addUserButton;

    @FXML
    Button changePasswordButton;

    @FXML
    Button deleteUserButton;

    @FXML
    ListView usersListView;

}
