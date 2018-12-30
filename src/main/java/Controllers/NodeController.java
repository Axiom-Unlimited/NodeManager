package Controllers;

import DataStructures.GenericReturnStruct;
import DataStructures.ReturnStruct;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.shape.Rectangle;

public class NodeController extends Controller
{
    @FXML
    public Label nodeLabel;

    @FXML
    public Rectangle nodeIdiotLight;

    @FXML
    public TextArea nodeTextOutput;

    private Integer nodeId;

    @Override
    public void create(ReturnStruct param)
    {
        this.nodeId = (Integer) ((GenericReturnStruct)param).getVar();
    }

    public Integer getNodeId()
    {
        return nodeId;
    }
}
