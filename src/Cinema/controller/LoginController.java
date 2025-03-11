package Cinema.controller;

import java.awt.Color;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import Cinema.database.mysqlconnect;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class LoginController implements Initializable{
	
	@FXML
	private Label lblErrors;
	
	@FXML
    private Button btnFB;

    @FXML
    private Button btnGG;

    @FXML
    private Button btnSignin;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private TextField txtUsername;
    
    @FXML
    private TextField txtPasswordVisible;
    
    @FXML
    private ImageView iconEye;

    
    @FXML
    private void handleButtonAction(ActionEvent event) {
    		if (event.getSource() == btnSignin) {
                //login here
                if (logIn().equals("Success")) {
                    try {

                        //add you loading or delays - ;-)
                        Node node = (Node) event.getSource();
                        Stage stage = (Stage) node.getScene().getWindow();
                        //stage.setMaximized(true);
                        stage.close();
                        Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/tableviewusers/student.fxml")));
                        stage.setScene(scene);
                        stage.show();

                    } catch (IOException ex) {
                        System.err.println(ex.getMessage());
                    }

                }
            }
    	}

    
    public LoginController() {
    	conn = mysqlconnect.ConnectDb("jdbc:mysql://localhost/Users","root", null);
    }
    
    Connection conn = null;
    PreparedStatement pst = null;
    ResultSet rs = null;
    
    private String logIn() {
    	String status = "Success";
        String username = txtUsername.getText();
        String password = txtPassword.getText();
        if(username.isEmpty() || password.isEmpty()) {
            setLblError(Color.RED, "Empty credentials");
            status = "Error";
        } else {
            //query
            String sql = "SELECT * FROM users Where username = ? and password = ?";
            try {
                pst = conn.prepareStatement(sql);
                pst.setString(1, username);
                pst.setString(2, password);
                rs = pst.executeQuery();
                if (!rs.next()) {
                  
                    status = "Error";
                } else {
                    setLblError(Color.GREEN, "Login Successful..Redirecting..");
                }
            } catch (SQLException ex) {
                System.err.println(ex.getMessage());
                status = "Exception";
            }
        }
        
        return status;
    }
    
    private void setLblError(Color color, String text) {
        lblErrors.setText(text);
        System.out.println(text);
    }
    
    private void alert(String info, String header, String title) {
    	Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    	alert.setContentText(info);
    	alert.setHeaderText(header);
    	alert.showAndWait();
    	
    }
    private boolean isPasswordVisible = false;
    
    // Hide and Unhide password
    @FXML
    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;
        int carePosition = txtPassword.getCaretPosition();// luu vi tri con tro

        if (isPasswordVisible) {
            txtPassword.setVisible(false);
            txtPasswordVisible.setVisible(true);
            txtPasswordVisible.requestFocus();
            txtPasswordVisible.positionCaret(carePosition);
            iconEye.setImage(new Image(getClass().getResourceAsStream("image/icons8-eye-24.png"))); // Đổi icon mắt mở
        } else {
            txtPassword.setVisible(true);
            txtPasswordVisible.setVisible(false);
            txtPassword.requestFocus();
            txtPassword.positionCaret(carePosition);
            iconEye.setImage(new Image(getClass().getResourceAsStream("image/icons8-hide-24.png"))); // Đổi icon mắt đóng
        }
    }
    
    public void initialize(URL url, ResourceBundle rb) {
    	//TODO
    	if (conn == null) {
            lblErrors.setText("Server Error : Check");
        } else {
            lblErrors.setText("Server is up : Good to go");
        }
    	txtPasswordVisible.textProperty().bindBidirectional(txtPassword.textProperty());//Rang buoc gia tri cua 2 o du lieu
    }
}
