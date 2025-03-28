package Cinema.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import Cinema.database.DBUtility;
import Cinema.database.DBUtility.LoginResult;
import Cinema.database.Form;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class Login implements Initializable {
    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    private AnchorPane loginFormContainer;

    @FXML
    private Label errorEmailAddress;

    @FXML
    private Label errorPassword;

    @FXML
    private Label errorLoginMessage;

    @FXML
    private TextField inputLoginEmailField;

    @FXML
    private PasswordField inputLoginPasswordField;

    @FXML
    private TextField inputLoginPasswordField1;

    @FXML
    private Button btnLogin;

    @FXML
    private ImageView togglePasswordVisibility;

    private boolean isPasswordVisible = false;

    private Image eyeOpen;
    private Image eyeClosed;

    @FXML
    public void resetErrorMessage(InputEvent event) throws Exception {
        errorEmailAddress.setVisible(false);
        errorPassword.setVisible(false);
        errorLoginMessage.setVisible(false);
    }

    @FXML
    public void login(ActionEvent event) throws IOException {
        String emailAddress = inputLoginEmailField.getText();
        String password = isPasswordVisible ? inputLoginPasswordField1.getText() : inputLoginPasswordField.getText();

        Object[] emailValidationResult = Form.validateEmail(emailAddress);
        boolean isEmailValid = (boolean) emailValidationResult[0];
        String emailErrorMessage = (String) emailValidationResult[1];

        Object[] passwordValidationResult = Form.validatePassword(password);
        boolean isPasswordValid = (boolean) passwordValidationResult[0];
        String passwordErrorMessage = (String) passwordValidationResult[1];

        if (isEmailValid && isPasswordValid) {
            errorEmailAddress.setVisible(false);
            errorPassword.setVisible(false);

            LoginResult loginResult = DBUtility.validateLogin(emailAddress, password);

            if (loginResult.isValid()) {
                errorLoginMessage.setVisible(false);
                String fxmlPath = loginResult.getIsSuperUser() == 1 ? "/Cinema/UI/AdminMainPanel.fxml" : "/Cinema/UI/Home.fxml";
                root = FXMLLoader.load(getClass().getResource(fxmlPath));
                stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                scene = new Scene(root);
                stage.setScene(scene);
                stage.setMaximized(true);
                stage.show();
            } else {
                errorLoginMessage.setVisible(true);
                errorLoginMessage.setText("Invalid Email or Password.");
            }
        } else {
            errorEmailAddress.setVisible(true);
            errorPassword.setVisible(true);
            errorEmailAddress.setText(emailErrorMessage);
            errorPassword.setText(passwordErrorMessage);
        }
    }

    @FXML
    public void signUp(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("/Cinema/UI/SignUp.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        double currentWidth = stage.getWidth();
        double currentHeight = stage.getHeight();
        scene = new Scene(root, currentWidth, currentHeight);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    public void forgetPassword(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("/Cinema/UI/ForgotPassword.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        double currentWidth = stage.getWidth();
        double currentHeight = stage.getHeight();
        scene = new Scene(root, currentWidth, currentHeight);
        stage.setMaximized(true);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    public void togglePasswordVisibility(MouseEvent event) {
        if (isPasswordVisible) {
            inputLoginPasswordField.setText(inputLoginPasswordField1.getText());
            inputLoginPasswordField.setVisible(true);
            inputLoginPasswordField1.setVisible(false);
            togglePasswordVisibility.setImage(eyeOpen);
            isPasswordVisible = false;
        } else {
            inputLoginPasswordField1.setText(inputLoginPasswordField.getText());
            inputLoginPasswordField.setVisible(false);
            inputLoginPasswordField1.setVisible(true);
            togglePasswordVisibility.setImage(eyeClosed);
            isPasswordVisible = true;
        }
    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        inputLoginEmailField.setText("");
        inputLoginPasswordField.setText(null);
            inputLoginPasswordField1.setText(null);
       

        try {
            eyeOpen = new Image(getClass().getResource("/Cinema/image/icons8-eye-24.png").toExternalForm());
            eyeClosed = new Image(getClass().getResource("/Cinema/image/icons8-hide-24.png").toExternalForm());
            if (togglePasswordVisibility != null) {
                togglePasswordVisibility.setImage(eyeOpen);
            } else {
                System.err.println("togglePasswordVisibility is null!");
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi tải hình ảnh: " + e.getMessage());
            eyeOpen = null;
            eyeClosed = null;
        }
    }
}