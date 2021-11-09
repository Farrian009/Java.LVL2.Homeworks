package Client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    public TextArea textArea;

    @FXML
    public TextField textField;

    @FXML
    public TextField loginField;

    @FXML
    public TextField passwordField;

    @FXML
    public HBox authPanel;

    @FXML
    public HBox messagePanel;

    @FXML
    public ListView<String> clientList;

    private Socket client;
    private DataInputStream inServer;
    private DataOutputStream outServer;
    private final String IP_ADDRESS = "localhost";
    private final int PORT = 8189;

    private boolean auth;
    private String nickname;

    private Stage stage;
    private Stage regStage;
    private RegController regController;

    public void setAuth(boolean auth) {
        this.auth = auth;
        authPanel.setVisible(!auth);
        authPanel.setManaged(!auth);
        messagePanel.setVisible(auth);
        messagePanel.setManaged(auth);
        clientList.setVisible(auth);
        clientList.setManaged(auth);


        if (!auth) {
            nickname = "";
        }
        setTitle(nickname);
        textArea.clear();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(()->{
            stage = (Stage) textArea.getScene().getWindow();
            stage.setOnCloseRequest(event -> {
                System.out.println("bye");
                if (client != null && !client.isClosed()) {
                    try {
                        outServer.writeUTF("/end");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        });
        setAuth(false);
    }

    private void connect() {
        try {
            client = new Socket(IP_ADDRESS, PORT);
            inServer = new DataInputStream(client.getInputStream());
            outServer = new DataOutputStream(client.getOutputStream());

            new Thread(()->{
                try {
                    while (true) {
                        String message = inServer.readUTF();
                        if (message.startsWith("/")) {
                            if (message.equals("/end")) {
                                break;
                            }
                            if (message.startsWith("/auth_ok")) {
                                nickname = message.split("\\s+")[1];
                                setAuth(true);
                                break;
                            }
                            if (message.startsWith("/reg_ok")) {
                                regController.showResult("/reg_ok");
                            }
                            if (message.startsWith("/reg_no")) {
                                regController.showResult("/reg_no");
                            }


                        } else {
                                textArea.appendText(message + "\n");
                            }
                        }

                    while (auth) {
                        String message = inServer.readUTF();
                        if (message.startsWith("/")) {
                            if (message.equals("/end")) {
                                break;
                            }
                            if (message.startsWith("/clientList")) {
                                String[] token = message.split("\\s+");
                                Platform.runLater(()-> {
                                    clientList.getItems().clear();
                                    for (int i = 1; i < token.length; i++) {
                                        clientList.getItems().add(token[i]);
                                    }
                                });
                            }
                        } else
                        textArea.appendText(message + "\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    System.out.println("Client disconnected");
                    setAuth(false);
                    try {
                        client.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void clickSend() {
        try {
            outServer.writeUTF(textField.getText());
            textField.clear();
            textField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryToAuth () {
        if (client == null || client.isClosed()) {
            connect();
        }
        String msg = String.format("/auth %s %s", loginField.getText().trim(), passwordField.getText().trim());
        try {
            outServer.writeUTF(msg);
            passwordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setTitle(String nickname) {
        Platform.runLater(()->{
            if (nickname.equals(""))
                stage.setTitle("My Open Chat");
            else
                stage.setTitle(String.format("My Open Chat for [%s]", nickname));

        });
    }

    public void clickClientList(javafx.scene.input.MouseEvent mouseEvent) {
        String receiver = clientList.getSelectionModel().getSelectedItem();
        textField.setText("/w " + receiver + " ");
        textField.requestFocus();
    }

    private void createRegWindow(){
        try{
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/FXML/reg.fxml"));
            Parent root = fxmlLoader.load();
            regStage = new Stage();
            regStage.setTitle("My Open Chat Registration");
            regStage.setScene(new Scene(root, 400, 400));
            regStage.initModality(Modality.APPLICATION_MODAL);
            regStage.initStyle(StageStyle.UTILITY);
            regController = fxmlLoader.getController();
            regController.setController(this);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryToReg(ActionEvent actionEvent) {
        if (regStage == null){
            createRegWindow();
        }
        Platform.runLater(()->{
            regStage.show();
        });
    }

    public  void registration(String login, String password, String nickname) {
        if (client == null || client.isClosed()) {
            connect();
        }
        String msg = String.format("/reg %s %s %s", login, password, nickname);
        try {
            outServer.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
