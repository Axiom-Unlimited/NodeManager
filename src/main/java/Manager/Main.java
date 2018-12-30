package Manager;

import Controllers.Controller;
import DataStructures.ReturnStruct;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Main extends Application implements PropertyChangeListener
{
    private Stage primaryStage;
    private MessageManager messageManager;
    private JSONObject settings;

    @Override
    public void start(Stage primaryStage) throws Exception{
        this.primaryStage = primaryStage;
        this.messageManager  = new MessageManager();
        // read settings json file
        JSONTokener tokenizer = new JSONTokener(getClass().getResourceAsStream("/Settings"));
        this.settings = new JSONObject(tokenizer);

        ReturnStruct struct = new ReturnStruct();
        struct.setMessageManager(messageManager);
        struct.setSettings(this.settings);
        struct.setNextScene("/Home.fxml");
        showScene(struct,this.primaryStage);
    }


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        try
        {
            showScene((ReturnStruct)evt.getNewValue(),this.primaryStage);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void showScene(ReturnStruct struct, Stage primaryStage) throws IOException
    {
        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getResourceAsStream(struct.getNextScene()));
        Controller tempCont = fxmlLoader.getController();
        tempCont.create(struct);
        tempCont.addPropertyChangeListener(this);
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

}
