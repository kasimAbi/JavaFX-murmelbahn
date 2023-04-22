package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("sample.fxml"));
        Object page = loader.load();
        Controller controller = loader.getController();
        Scene scene = new Scene((Parent) page);
        primaryStage.setScene(scene);
        primaryStage.show();
        controller.initial();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
