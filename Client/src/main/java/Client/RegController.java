package Client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class RegController {

    private Controller controller;

    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField nickField;
    @FXML
    private TextArea textArea;

    @FXML
    public void tryToReg(ActionEvent actionEvent) {
        String login = loginField.getText().trim();
        String password = passwordField.getText().trim();
        String nickname = nickField.getText().trim();
        controller.registration(login, password, nickname);
    }

    public void showResult(String result){
        if(result.equals("/reg_ok")) {
            textArea.appendText("Registration passed successfully.\n");

        } else
            textArea.appendText("Registration failed. \n Nickname already exist.");
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }
}
