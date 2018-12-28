package Controllers;

import DataStructures.CaptureCommand;
import DataStructures.StatusReport;
import Manager.Node;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
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

    @FXML
    public TextArea consoleOutput;

    private List<Node> cameraNodes;
    private List<Thread> cameraThreads;
    private StringBuilder stringBuilder;
    private PrintStream printStream;
    private Console console;


    public NodeSystemController()
    {
        cameraThreads = new ArrayList<>();
        stringBuilder = new StringBuilder(100);

//        Thread updateThread = new Thread(new UpdateClass());
//        updateThread.start();
    }

    public void setCameraNodes(List<Node> cameraNodes, List<AnchorPane> cameraGuiNodes)
    {
//        console = new Console(consoleOutput);
//        printStream = new PrintStream(console,true);
//        System.setOut(printStream);
//        System.setErr(printStream);

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
        if (captureButton.getText().equals("Start Capture")) // send a start capture command
        {
            String nextCapTitle = captureTitle.getText();
            CaptureCommand startCapCmd = new CaptureCommand();
            startCapCmd.setByteBuffer(ByteBuffer.wrap(new byte[startCapCmd.size()]),0);
            startCapCmd.size.set(startCapCmd.size());
            startCapCmd.type.set(1);
            startCapCmd.command.set(1);
            if (nextCapTitle.equals(""))
            {
                startCapCmd.filename.set("none");
            }
            else
            {
                startCapCmd.filename.set(nextCapTitle);
            }


            for (Node node : this.cameraNodes)
            {
                node.sendCommand(startCapCmd);
            }
            captureButton.setText("Stop Capture"); // set capture button to show stop
        }
        else // send a stop capture command
        {
            CaptureCommand startCapCmd = new CaptureCommand();
            startCapCmd.setByteBuffer(ByteBuffer.wrap(new byte[startCapCmd.size()]),0);
            startCapCmd.size.set(startCapCmd.size());
            startCapCmd.type.set(1);
            startCapCmd.command.set(0);

            for (Node node : this.cameraNodes)
            {
                node.sendCommand(startCapCmd);
            }
            captureButton.setText("Start Capture"); // set capture button to show start
        }
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
            Rectangle capLight = null;
            for(javafx.scene.Node anchorPane : this.nodeList.getChildren())
            {
                Class clazz = anchorPane.getClass();
                if (!clazz.getName().equals("javafx.scene.layout.AnchorPane"))
                {
                    continue;
                }
                AnchorPane temp = (AnchorPane) anchorPane;
                Label tempLabel = (Label) ((AnchorPane)((SplitPane)((AnchorPane)((SplitPane)temp.getChildren().get(0)).getItems().get(0)).getChildren().get(0)).getItems().get(0)).getChildren().get(0);
                if (tempLabel.getText().equals(nodeId))
                {
                    capLight = (Rectangle) ((AnchorPane)((SplitPane)((AnchorPane)((SplitPane)temp.getChildren().get(0)).getItems().get(0)).getChildren().get(0)).getItems().get(1)).getChildren().get(0);
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

    public static class Console extends OutputStream
    {

        private TextArea output;

        public Console(TextArea ta) {
            this.output = ta;
        }

        @Override
        public void write(int i) throws IOException
        {
            Platform.runLater(()->{
                output.appendText(String.valueOf((char) i));
            });

        }
    }
}
