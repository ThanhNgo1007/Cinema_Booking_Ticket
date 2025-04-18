package Cinema.controller;

import java.io.IOException;

import Cinema.database.DBUtility;
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
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class UpdatePassword {
    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    private AnchorPane updatePasswordContainer;

    @FXML
    private Label errorNewPassword;

    @FXML
    private Label errorConfirmPassword;

    @FXML
    private PasswordField inputUpdatePasswordField;

    @FXML
    private PasswordField inputUpdatePasswordConfirmField;

    @FXML
    private Button btnUpdatePassword;

    // Validate password, check if new password matches old password, and update password in DB
    @FXML
    public void updateUserPassword(ActionEvent event) throws IOException {
        String inputNewPassword = inputUpdatePasswordField.getText();
        String inputConfirmPassword = inputUpdatePasswordConfirmField.getText();

        // Validate password
        Object[] passwordValidationResult = Form.validatePassword(inputNewPassword);
        boolean isPasswordValid = (boolean) passwordValidationResult[0];
        String passwordErrorMessage = (String) passwordValidationResult[1];

        // Validate confirm password
        Object[] confirmPasswordValidationResult = Form.validatePassword(inputConfirmPassword);
        boolean isConfirmPasswordValid = (boolean) confirmPasswordValidationResult[0];
        String confirmPasswordErrorMessage = (String) confirmPasswordValidationResult[1];

        if (isPasswordValid && isConfirmPasswordValid) {
            Boolean isBothPasswordSame = inputNewPassword.equals(inputConfirmPassword);

            if (isBothPasswordSame) {
                // Kiểm tra xem mật khẩu mới có trùng với mật khẩu cũ không
                boolean isNewPasswordSameAsOld = DBUtility.verifyCurrentPassword(ForgotPassword.userEmailAddress, inputNewPassword);

                if (isNewPasswordSameAsOld) {
                    errorNewPassword.setText("Mật khẩu mới không được trùng với mật khẩu cũ.");
                    return;
                }

                // Nếu không trùng, tiến hành cập nhật mật khẩu
                Boolean isPasswordUpdated = DBUtility.updateUsersPassword(ForgotPassword.userEmailAddress, inputNewPassword);

                if (isPasswordUpdated) {
                    // Chuyển về màn hình đăng nhập
                    root = FXMLLoader.load(getClass().getResource("/Cinema/UI/Login.fxml"));
                    stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    double currentWidth = stage.getWidth();
                    double currentHeight = stage.getHeight();
                    scene = new Scene(root, currentWidth, currentHeight);

                    stage.setScene(scene);
                    stage.show();
                } else {
                    errorConfirmPassword.setText("Oops! Không thể cập nhật mật khẩu. Vui lòng thử lại.");
                }
            } else {
                errorConfirmPassword.setText("Oops! Mật khẩu xác nhận không khớp với mật khẩu mới.");
            }
        } else {
            errorNewPassword.setVisible(true);
            errorConfirmPassword.setVisible(true);

            errorNewPassword.setText(passwordErrorMessage);
            errorConfirmPassword.setText(confirmPasswordErrorMessage);
        }
    }
}