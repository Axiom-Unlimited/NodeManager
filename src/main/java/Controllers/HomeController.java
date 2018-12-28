package Controllers;

import DataStructures.BroadcastPacket;
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
import java.net.*;
import java.nio.ByteBuffer;
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

    // broadcast listener variables
    @FXML
    TextField numNodes;

    @FXML
    Button listenButton;

    @FXML
    TextArea broadcastOutput;




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
                TextField refNodeServerIpAddress = ((TextField)node.lookup("#hostIPAddress"));
                refNodeServerIpAddress.setText(InetAddress.getLocalHost().getHostAddress());
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

                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/CameraNode.fxml"));
//                AnchorPane cameraGUINode = FXMLLoader.load(getClass().getResource("/CameraNode.fxml"));
                Manager.Node nodeController = new Manager.Node(hostIpAddress.getText(),Integer.parseInt(basePortNum.getText()), Integer.parseInt(nodePortAdjuster.getText()));

                fxmlLoader.setController(nodeController);

                AnchorPane cameraGUINode = fxmlLoader.load();
                //todo: there has to be a better way of doing this, will probably have to use recursion!!!!
                Optional<Node> anchorPane = ((SplitPane)((AnchorPane)((SplitPane)cameraGUINode.getChildren().get(0)).getItems().get(0)).getChildren().get(0)).getItems().stream().findAny().filter(node -> node.getId().equals("labelHolder"));
                Label tempLabel = (Label)anchorPane.get().lookup("#nodeLabel");
                tempLabel.setText(" " + nodeLabel.getText());
                cameraNodesGUI.add(cameraGUINode);

                cameraNodes.add(nodeController);
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

    public void listenForBroadcasts(MouseEvent mouseEvent)
    {
        int numOfNodes2Find = Integer.parseInt(numNodes.getText());
        List foundAddresses = new ArrayList<String>();

        try
        {
            String hostAddress = Inet4Address.getLocalHost().getHostAddress();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/NodeSystem.fxml"));
            AnchorPane nodeSystem = loader.load();
            NodeSystemController controller = loader.<NodeSystemController>getController();

            List<Node> children = nodePane.getChildren();

            List<Manager.Node> cameraNodes = new ArrayList<>();
            List<AnchorPane> cameraNodesGUI = new ArrayList<>();

            byte[] buff = new byte[1024];
            DatagramSocket listenSock = new DatagramSocket(60000);
            DatagramPacket packet = new DatagramPacket(buff,1024);
            listenSock.setSoTimeout(1000);

            while (foundAddresses.size() < numOfNodes2Find)
            {
                    try
                    {
                        listenSock.receive(packet);
                        BroadcastPacket broadcastPacket = new BroadcastPacket();
                        broadcastPacket.setByteBuffer(ByteBuffer.wrap(packet.getData()),0);
                        if (!foundAddresses.contains(broadcastPacket.port.get()))
                        {
                            foundAddresses.add(broadcastPacket.port.get());
                            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/CameraNode.fxml"));
                            Manager.Node nodeController = new Manager.Node(hostAddress,broadcastPacket.port.get());
                            fxmlLoader.setController(nodeController);
                            AnchorPane cameraGUINode = fxmlLoader.load();
                            //todo: there has to be a better way of doing this, will probably have to use recursion!!!!
                            Optional<Node> anchorPane = ((SplitPane)((AnchorPane)((SplitPane)cameraGUINode.getChildren().get(0)).getItems().get(0)).getChildren().get(0)).getItems().stream().findAny().filter(node -> node.getId().equals("labelHolder"));
                            Label tempLabel = (Label)anchorPane.get().lookup("#nodeLabel");
                            tempLabel.setText(" Node" + String.valueOf(broadcastPacket.port.get() - 50000) );
                            cameraNodesGUI.add(cameraGUINode);

                            cameraNodes.add(nodeController);
                        }
                    }
                    catch (SocketTimeoutException e)
                    {
                        System.out.println("No packet received.");
                    }

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
