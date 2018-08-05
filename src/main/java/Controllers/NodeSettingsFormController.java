package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class NodeSettingsFormController
{
    @FXML
    public TextField hostIPAddress;

    @FXML
    public TextField basePortNum;

    @FXML
    public TextField nodePortAdjuster;

    @FXML
    public Label nodeLabel;
}
