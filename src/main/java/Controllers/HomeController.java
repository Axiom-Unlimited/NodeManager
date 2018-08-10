package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import javax.xml.soap.Text;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;


public class HomeController
{
    // Load configuration file variables
    @FXML
    TextField configXMLPath;

    @FXML
    Button buildSystemButton;

    // build configuration variables
    @FXML
    TextField numOfNodes;

    @FXML
    Button numOfNodesSubmit;

    // node setup menu variable
    @FXML
    GridPane nodePane;

    @FXML
    Button buildNodeSystemButton;

    public void buildSystemFromConfigFile(MouseEvent mouseEvent)
    {
        //todo:: will likely be a xml file but this needs to be implemented.
    }

    public void createSetupDropDownMenu(MouseEvent mouseEvent)
    {
        //get the number of nodes
        if (this.numOfNodes.getText().isEmpty()){return;}

        int numOfNodes = Integer.parseInt(this.numOfNodes.getText());

        for (int i = 0; i < numOfNodes; i++)
        {
            try
            {
                AnchorPane node = FXMLLoader.load(getClass().getResource("/NodeSettingsForm.fxml"));
                //set label
                Label label = ((Label)node.lookup("#nodeLabel"));
                label.setText("Node" + String.valueOf(i));
                //set default node port adjuster
                TextField refNodePortAdj = ((TextField)node.lookup("#nodePortAdjuster"));
                refNodePortAdj.setText(String.valueOf(i));
                this.nodePane.add(node,0,i);

            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

        }

        this.buildNodeSystemButton.visibleProperty().setValue(true);

    }

    public void buildNodeSystem(MouseEvent mouseEvent)
    {
        // build the Node system
        try
        {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/NodeSystem.fxml"));
            AnchorPane nodeSystem = loader.load();
            NodeSystemController controller = loader.<NodeSystemController>getController();

            List<Node> children = nodePane.getChildren();

            List<Manager.Node> cameraNodes = new ArrayList<>();
            List<AnchorPane> cameraNodesGUI = new ArrayList<>();

            //get hostIPAddress, basePortNum, nodePortAdjuster
            for (int i = 1; i < children.size(); i++)
            {
                TextField hostIpAddress     = (TextField)   children.get(i).lookup("#hostIPAddress");
                TextField basePortNum       = (TextField)   children.get(i).lookup("#basePortNum");
                TextField nodePortAdjuster  = (TextField)   children.get(i).lookup("#nodePortAdjuster");
                Label nodeLabel             = (Label)       children.get(i).lookup("#nodeLabel");

                AnchorPane cameraGUINode = FXMLLoader.load(getClass().getResource("/CameraNode.fxml"));
                Optional<Node> anchorPane = ((SplitPane)cameraGUINode.getChildren().get(0)).getItems().stream().findAny().filter(node -> node.getId().equals("labelHolder"));
                Label tempLabel = (Label)anchorPane.get().lookup("#nodeLabel");
                tempLabel.setText(" " + nodeLabel.getText());
                cameraNodesGUI.add(cameraGUINode);

                cameraNodes.add(new Manager.Node(hostIpAddress.getText(),Integer.parseInt(basePortNum.getText()), Integer.parseInt(nodePortAdjuster.getText())));
            }

            controller.setCameraNodes(cameraNodes,cameraNodesGUI);

            Stage secondaryStage = new Stage();
            secondaryStage.setTitle("Node System");
            secondaryStage.setScene(new Scene(nodeSystem, -1, -1));
            secondaryStage.show();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
