package Controllers;

import DataStructures.GenericReturnStruct;
import DataStructures.ReturnStruct;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;


public class NodeSystemController extends Controller implements PropertyChangeListener
{

    @FXML
    public Button captureButton;

    @FXML
    public TextField captureNameInput;

    @FXML
    public ComboBox nodeDropDown;

    @FXML
    public TextArea managerOutput;

    @FXML
    public AnchorPane gridViewAnchor;

    public GridPane gridView;

    private Integer numOfNodes;

    public NodeSystemController()
    {
        super();
    }

    @Override
    public void create(ReturnStruct param)
    {
        this.messageManager = param.getMessageManager();
        this.messageManager.init((Integer) ((GenericReturnStruct)param).getVar());
        this.settings = param.getSettings();
        this.numOfNodes = (Integer )((GenericReturnStruct)param).getVar();

        ArrayList observableList = new ArrayList();
        // set the combo box
        observableList.add("Broadcast to All Nodes");
        for (int i = 0; i < this.numOfNodes; i++)
        {
            observableList.add("Node" + String.valueOf(i));
        }

        this.nodeDropDown.setItems(FXCollections.observableList(observableList));

        // set grid pane for the nodes
        int rows = (int) this.settings.get("rows");
        int cols = (int) Math.ceil(numOfNodes/rows);

        if (cols < 1){ cols = 1; }

        this.gridView = new GridPane();
        ColumnConstraints columnConstraints = new ColumnConstraints();
        columnConstraints.setPercentWidth(100.0/(double)cols);
        this.gridView.getColumnConstraints().add(columnConstraints);
        RowConstraints rowConstraints = new RowConstraints();
        rowConstraints.setPercentHeight(100.0/(double)rows);
        this.gridView.getRowConstraints().add(rowConstraints);
        this.gridView.setGridLinesVisible(true);
        this.gridViewAnchor.getChildren().add(this.gridView);

        AnchorPane.setBottomAnchor(this.gridView,0.0);
        AnchorPane.setTopAnchor(this.gridView,0.0);
        AnchorPane.setLeftAnchor(this.gridView,0.0);
        AnchorPane.setRightAnchor(this.gridView,0.0);

//         create nodes and add them to the grid pane
        GenericReturnStruct struct = new GenericReturnStruct<Integer>();
        int nodeIdx = 0;
        for (int i = 0; i < rows; i++)
        {
            for (int j = 0; j < cols; j++)
            {
                // create the new node
                FXMLLoader fxmlLoader = new FXMLLoader();
                try
                {
                    if (nodeIdx >= this.numOfNodes)
                    {
                        break;
                    }
                    else
                    {
                        struct.setVar(nodeIdx);
                        Node node = fxmlLoader.load(getClass().getResourceAsStream("/CameraNode.fxml"));
                        Controller tmp = fxmlLoader.getController();
                        ((NodeController)tmp).nodeLabel.setText("Node" + String.valueOf(nodeIdx));
                        tmp.create(struct);
                        this.gridView.add(node,j,i);
                        nodeIdx++;
                    }
                }
                catch (IOException e)
                {
                    //todo : create an bit manager to handle all errors and display them on the output console
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        //todo: received a message from the message manager, set statuses and outputs.
    }

    public void sendCaptureCommand(MouseEvent mouseEvent)
    {
        //todo: build command and send it to the message manager for handling.
        System.out.println("hello");
    }

}
