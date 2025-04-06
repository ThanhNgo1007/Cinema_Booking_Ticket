package Cinema.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import Cinema.database.DBUtility;
import Cinema.database.Form;
import Cinema.database.JSONUtility;
import Cinema.util.User;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ChangePasswordController implements Initializable {

    @FXML
    private AnchorPane ChangePasswordContainer;

    @FXML
    private Button btnChangePassword;

    @FXML
    private Label errorNewPassword;

    @FXML
    private Label errorConfirmPasswordMessage;

    @FXML
    private Label errorCurrentPassword;

    @FXML
    private Label errorPasswordMessage;

    @FXML
    private PasswordField inputConfirmPassword;

    @FXML
    private TextField inputConfirmPassword1;

    @FXML
    private PasswordField inputCurrentPassword;

    @FXML
    private PasswordField inputPassword;

    @FXML
    private TextField inputPassword1;

    @FXML
    private ImageView togglePasswordVisibility;

    private boolean passwordVisible = false;
    
    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        btnChangePassword.setOnAction(event -> handleChangePassword());
        togglePasswordVisibility.setOnMouseClicked(event -> togglePasswordVisibility());
        inputPassword1.setVisible(false);
        inputConfirmPassword1.setVisible(false);
    }

    private void handleChangePassword() {
        clearErrorMessages();
        
        String currentPassword = inputCurrentPassword.getText();
        String newPassword = passwordVisible ? inputPassword1.getText() : inputPassword.getText();
        String confirmPassword = passwordVisible ? inputConfirmPassword1.getText() : inputConfirmPassword.getText();
        
        // 1. Verify current password
        if (!verifyCurrentPassword(currentPassword)) return;
        
        // 2. Validate new password
        if (!validateNewPassword(currentPassword, newPassword)) return;
        
        // 3. Check password confirmation
        if (!checkPasswordConfirmation(newPassword, confirmPassword)) return;
        
        // 4. Update password in database
        updatePasswordInDatabase(newPassword);
    }

    private boolean verifyCurrentPassword(String currentPassword) {
        User currentUser = JSONUtility.getUserData();
        if (currentUser == null) {
            errorCurrentPassword.setText("Không tìm thấy thông tin người dùng");
            return false;
        }
        
        if (currentPassword.isEmpty()) {
            errorCurrentPassword.setText("Vui lòng nhập mật khẩu hiện tại");
            return false;
        }

        if (!DBUtility.verifyCurrentPassword(currentUser.getEmail(), currentPassword)) {
            errorCurrentPassword.setText("Mật khẩu hiện tại không đúng");
            return false;
        }
        return true;
    }

    private boolean validateNewPassword(String currentPassword, String newPassword) {
        // Check if new password is empty
        if (newPassword.isEmpty()) {
            errorPasswordMessage.setText("Vui lòng nhập mật khẩu mới");
            return false;
        }

        // Check if new password is same as current password
        if (newPassword.equals(currentPassword)) {
            errorPasswordMessage.setText("Mật khẩu mới không được trùng mật khẩu cũ");
            return false;
        }

        // Validate password format
        Object[] passwordValidation = Form.validatePassword(newPassword);
        if (!(boolean) passwordValidation[0]) {
            errorPasswordMessage.setText((String) passwordValidation[1]);
            return false;
        }
        return true;
    }

    private boolean checkPasswordConfirmation(String newPassword, String confirmPassword) {
        if (confirmPassword.isEmpty()) {
            errorConfirmPasswordMessage.setText("Vui lòng xác nhận mật khẩu");
            return false;
        }

        if (!newPassword.equals(confirmPassword)) {
            errorConfirmPasswordMessage.setText("Mật khẩu xác nhận không khớp");
            return false;
        }
        return true;
    }

 private void updatePasswordInDatabase(String newPassword) {
	   User currentUser = JSONUtility.getUserData();
	    if (currentUser != null) {
	        boolean success = DBUtility.updateUsersPassword(currentUser.getEmail(), newPassword);
	        if (success) {
	            showSuccessMessage();
	            clearPasswordFields();
	            
	            // Thêm delay 30 giây trước khi đăng xuất
	            PauseTransition delay = new PauseTransition(Duration.seconds(3));
	            delay.setOnFinished(event -> {
	                try {
	                    logOut(); // Gọi hàm logOut sau 30 giây
	                } catch (IOException e) {
	                    e.printStackTrace();
	                    errorNewPassword.setText("Lỗi khi đăng xuất. Vui lòng thử lại.");
	                }
	            });
	            delay.play();
	        } else {
	            errorNewPassword.setText("Đổi mật khẩu thất bại. Vui lòng thử lại.");
	        }
	    }
	}

	public void logOut() throws IOException {
	    if (parentController != null) {
	        // Lấy Stage từ ChangePasswordContainer
	        Stage stage = (Stage) ChangePasswordContainer.getScene().getWindow();
	        parentController.performLogout(stage); // Truyền Stage sang Controller
	    }
	}

    private void showSuccessMessage() {
        errorNewPassword.setText("Đổi mật khẩu thành công!");
        errorNewPassword.setStyle("-fx-text-fill: green;");
    }

    private void clearPasswordFields() {
        inputCurrentPassword.clear();
        inputPassword.clear();
        inputConfirmPassword.clear();
        inputPassword1.clear();
        inputConfirmPassword1.clear();
    }

    private void clearErrorMessages() {
        errorCurrentPassword.setText("");
        errorPasswordMessage.setText("");
        errorConfirmPasswordMessage.setText("");
        errorNewPassword.setText("");
        errorNewPassword.setStyle("-fx-text-fill: red;");
    }

    public void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;
        
        if (passwordVisible) {
            inputConfirmPassword1.setText(inputConfirmPassword.getText());
            inputConfirmPassword1.setVisible(true);
            inputConfirmPassword.setVisible(false);
            inputPassword1.setText(inputPassword.getText());
            inputPassword1.setVisible(true);
            inputPassword.setVisible(false);
        } else {
            inputConfirmPassword.setText(inputConfirmPassword1.getText());
            inputConfirmPassword.setVisible(true);
            inputConfirmPassword1.setVisible(false);
            inputPassword.setText(inputPassword1.getText());
            inputPassword.setVisible(true);
            inputPassword1.setVisible(false);
        }
    }
    
    protected Controller parentController;
    
    public void setParentController(Controller parentController) {
        this.parentController = parentController;
    }
    public void logOut(MouseEvent event) throws IOException {
    	 parentController.logOutButton(event);
    }
    @FXML
    protected void goBack() {
        if (parentController != null) {
            parentController.goBack();
        }
    }
}