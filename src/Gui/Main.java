package Gui;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    // javafx start
    @Override
    public void start(Stage primaryStage) throws Exception{
        LoginController lg = new LoginController();
        lg.showLoginWindow();

    }

    // Main method lauches javafx application
    public static void main(String[] args) {
        launch(args);
    }
}
