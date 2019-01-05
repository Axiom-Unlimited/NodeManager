package Controllers;

import DataStructures.GenericReturnStruct;
import DataStructures.ReturnStruct;
import DatagramStructures.BroadcastPackage;
import DatagramStructures.NodeState;
import DatagramStructures.NodeStatusPackage;
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
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;


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

    private GridPane gridView;

    private Integer numOfNodes;

    private List nodeControllers;

    // these are for locking the gui
    private Boolean guiLock;
    private Integer lockingNode;

    public NodeSystemController()
    {
        super();
    }

    @Override
    public void create(ReturnStruct param)
    {
        this.guiLock = false;
        this.messageManager = param.getMessageManager();
        this.settings = param.getSettings();
        this.messageManager.init((Integer) ((GenericReturnStruct)param).getVar(),this.settings,this);
        this.numOfNodes = (Integer )((GenericReturnStruct)param).getVar();

        ArrayList observableList = new ArrayList();
        // set the combo box
        observableList.add("broadcast to All Nodes");
        for (int i = 0; i < this.numOfNodes; i++)
        {
            observableList.add("Node " + String.valueOf(i));
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
        this.nodeControllers = new ArrayList();
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
                        ((NodeController)tmp).nodeLabel.setText("Node " + String.valueOf(nodeIdx));
                        tmp.create(struct);
                        this.nodeControllers.add(tmp);
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
        NodeStatusPackage state = (NodeStatusPackage) evt.getNewValue();
        NodeController controllerRef = (NodeController) this.nodeControllers.get(state.nodeId.get());

        if (state.captureState.get()) // capturing
        {
            controllerRef.nodeIdiotLight.setFill(Color.GREEN);
        }
        else // not capturing
        {
            controllerRef.nodeIdiotLight.setFill(Color.RED);
        }

        // todo: add bit messages to the gui text output

    }

    public void sendCaptureCommand(MouseEvent mouseEvent)
    {
        // get the name of the capture file if there is one else send the date time.
        String capName = captureNameInput.getText();
        if (capName.equals(""))
        {
            capName = "none";
        }

        if(nodeDropDown.getValue().toString().equals("broadcast to All Nodes"))
        {
            if(this.guiLock)
            {
                //todo: send message to the bit manager
            }
            else
            {
                BroadcastPackage broadcastPackage = new BroadcastPackage();
                broadcastPackage.setByteBuffer(ByteBuffer.wrap(new byte[broadcastPackage.size()]),0);
                broadcastPackage.type.set(0xf001);

                boolean cap = false;
                if (captureButton.getText().equals("Halt"))
                {
                    captureButton.setText("Capture");
                }
                else
                {
                    cap = true;
                    captureButton.setText("Halt");
                }
                // broadcast to all nodes
                for (int i = 0; i < numOfNodes; i++)
                {
                    broadcastPackage.nodeStates[i].captureName.set(capName);
                    broadcastPackage.nodeStates[i].captureState.set(cap);
                }

                broadcastPackage.type.set(0xf001);
                this.messageManager.broadcast(broadcastPackage);
            }
        }
        else
        {
            // broadcast to one node
            BroadcastPackage broadcastPackage = new BroadcastPackage();
            broadcastPackage.setByteBuffer(ByteBuffer.wrap(new byte[broadcastPackage.size()]),0);
            if (this.guiLock)
            {
                //check to see if the node that is locking the gui is being told to unlock it and halt its capture
                String[] vals = this.nodeDropDown.getValue().toString().split("\\s+");
                if (Integer.parseInt(vals[1]) == this.lockingNode)
                {
                    this.lockingNode = -1;
                    this.guiLock = false;
                    broadcastPackage.type.set(0xf001);
                    broadcastPackage.nodeStates[Integer.parseInt(vals[1])].captureName.set(capName);
                    broadcastPackage.nodeStates[Integer.parseInt(vals[1])].captureState.set(false);
                    captureButton.setText("Capture");
                    this.messageManager.broadcast(broadcastPackage);
                }
                else
                {
                    //todo: send message to the bit manager
                }
            }
            else
            {
                String[] vals = this.nodeDropDown.getValue().toString().split("\\s+");
                this.guiLock = true;
                this.lockingNode = Integer.parseInt(vals[1]);
                broadcastPackage.type.set(0xf001);
                broadcastPackage.nodeStates[Integer.parseInt(vals[1])].captureName.set(capName);
                broadcastPackage.nodeStates[Integer.parseInt(vals[1])].captureState.set(true);
                captureButton.setText("Halt");
                this.messageManager.broadcast(broadcastPackage);
            }
        }

    }

}
