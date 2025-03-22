package Cinema.controller;

import java.io.IOException;

import Cinema.database.DBUtility;
import Cinema.database.DBUtility.LoginResult;
import Cinema.database.Form;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class Login {
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
    private Button btnLogin;

    @FXML
    public void resetErrorMessage(InputEvent event) throws Exception {
        errorEmailAddress.setVisible(false);
        errorPassword.setVisible(false);
        errorLoginMessage.setVisible(false);
    }

    @FXML
    public void login(ActionEvent event) throws IOException {
        String emailAddress = inputLoginEmailField.getText();
        String password = inputLoginPasswordField.getText();

        // Validate email
        Object[] emailValidationResult = Form.validateEmail(emailAddress);
        boolean isEmailValid = (boolean) emailValidationResult[0];
        String emailErrorMessage = (String) emailValidationResult[1];

        // Validate password
        Object[] passwordValidationResult = Form.validatePassword(password);
        boolean isPasswordValid = (boolean) passwordValidationResult[0];
        String passwordErrorMessage = (String) passwordValidationResult[1];

        // Check for the credentials in DB
        if (isEmailValid && isPasswordValid) {
            errorEmailAddress.setVisible(false);
            errorPassword.setVisible(false);

            // SQL Query
            LoginResult loginResult = DBUtility.validateLogin(emailAddress, password);

            // Credentials valid - redirect based on isSuperUser
            if (loginResult.isValid()) {
                errorLoginMessage.setVisible(false);

                // Chuyển hướng dựa trên isSuperUser
                String fxmlPath = loginResult.getIsSuperUser() == 1 ? "/Cinema/UI/AdminMainPanel.fxml" : "/Cinema/UI/Home.fxml";
                root = FXMLLoader.load(getClass().getResource(fxmlPath));
                stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                scene = new Scene(root);

                stage.setScene(scene);
                stage.setMaximized(true);
                stage.show();
            } else {
                // Credentials invalid - show Error message
                errorLoginMessage.setVisible(true);
                errorLoginMessage.setText("Invalid Email or Password.");
            }
        } else {
            // Show the Error message Labels and update Texts
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
}