package Controllers;

import DataStructures.GenericReturnStruct;
import DataStructures.ReturnStruct;
import Manager.MessageManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;


public class HomeController extends Controller
{
    private MessageManager messageManagerRef;

    @FXML
    public Button numSubmit;

    @FXML
    public TextField numOfNodes;

    public HomeController()
    {
        super();
    }

    public void createNodeSystem(MouseEvent mouseEvent)
    {
        int num = Integer.parseInt(numOfNodes.getText());
        GenericReturnStruct struct = new GenericReturnStruct();
        struct.setNextScene("/NodeSystem.fxml");
        struct.setVar(num);
        struct.setMessageManager(messageManagerRef);
        struct.setSettings(this.settings);
        this.support.firePropertyChange("String",null,struct);
    }

    @Override
    public final void create(ReturnStruct param)
    {
        this.messageManagerRef = param.getMessageManager();
        this.settings = param.getSettings();
    }
}
