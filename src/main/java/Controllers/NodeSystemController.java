package Controllers;

import DataStructures.StatusReport;
import Manager.Node;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;


public class NodeSystemController implements PropertyChangeListener
{

    @FXML
    public GridPane nodeList;

    // capture variables
    @FXML
    public TextField captureTitle;

    @FXML
    public Button captureButton;

    // configuration save variables
    @FXML
    public TextField saveConfigPath;

    @FXML
    public Button saveConfigButton;

    private List<Node> cameraNodes;
    private List<Thread> cameraThreads;


    public NodeSystemController()
    {
        cameraThreads = new ArrayList<>();
//        Thread updateThread = new Thread(new UpdateClass());
//        updateThread.start();
    }

    public void setCameraNodes(List<Node> cameraNodes, List<AnchorPane> cameraGuiNodes)
    {
        this.cameraNodes = cameraNodes;
        for (Node node : this.cameraNodes )
        {
            node.addPropertyChangeListener(this);
            this.cameraThreads.add(new Thread(node));
        }

        // add the gui nodes to the nodes list
        for (int i = 0; i < cameraGuiNodes.size(); i++)
        {
            this.nodeList.add(cameraGuiNodes.get(i),0,i);
        }

        // start the threads
        for (Thread thread : this.cameraThreads)
        {
            thread.start();
        }
    }

    public void captureCommand(MouseEvent mouseEvent)
    {
        System.out.println("button pressed!!!!!!!!!!");
    }

    public void saveConfiguration(MouseEvent mouseEvent)
    {
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        String nodeId = " " + evt.getPropertyName();
        StatusReport statusReport = (StatusReport) evt.getNewValue();

        Platform.runLater(()->{
            Label changeLabel = null;
            Rectangle capLight = null;
            for(javafx.scene.Node anchorPane : this.nodeList.getChildren())
            {
                Class clazz = anchorPane.getClass();
                if (!clazz.getName().equals("javafx.scene.layout.AnchorPane"))
                {
                    continue;
                }
                AnchorPane temp = (AnchorPane) anchorPane;
                Label tempLabel = ((Label)((AnchorPane)((SplitPane)temp.getChildren().get(0)).getItems().get(0)).getChildren().get(0));
                if (tempLabel.getText().equals(nodeId))
                {
                    changeLabel = tempLabel;
                    capLight = (Rectangle) ((AnchorPane)((SplitPane)temp.getChildren().get(0)).getItems().get(1)).getChildren().get(0);
                    break;
                }

            }

            Rectangle finalCapLight = capLight;
            if (statusReport.status.get() == 0) // node is not capturing
            {
                assert finalCapLight != null;
                finalCapLight.setFill(Color.RED);
            }
            else if (statusReport.status.get() == 1)
            {
                assert finalCapLight != null;
                finalCapLight.setFill(Color.GREEN);
            }

        });
    }
//
//    class UpdateClass extends Task
//    {
//        @Override
//        protected Object call() throws Exception
//        {
//            try
//            {
//                sleep(10);
//                while(true)
//                {
//                }
//            }
//            catch (InterruptedException e)
//            {
//                e.printStackTrace();
//            }
//
//            return null;
//        }
//    }
}
