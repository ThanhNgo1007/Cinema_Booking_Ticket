package Cinema.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import Cinema.database.JSONUtility;
import Cinema.database.JSONUtility.User;
import Cinema.database.mysqlconnect;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class AccountSettingController {

    @FXML
    private Button applyButton;

    @FXML
    private Button changePasswordButton;

    @FXML
    private ComboBox<String> cityField;

    @FXML
    private Text emailField;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField phoneField;

    private static final String DB_URL = "jdbc:mysql://localhost/Cinema_DB";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    private String userEmail;
    private String initialFirstName;
    private String initialLastName;
    private String initialPhone;
    private String initialCity;
    private int userId;

    @FXML
    public void initialize() {
        applyButton.setDisable(true);

        User user = JSONUtility.getUserData();
        if (user != null && !user.getEmail().isEmpty()) {
            userEmail = user.getEmail();
            userId = user.getUserId();
            loadUserData(userEmail);
        } else {
            System.out.println("Error: Could not retrieve user data from JSONUtility");
            emailField.setText("Không xác định được người dùng");
        }
        loadCities();

        setupChangeListeners();
    }

    // Hàm lấy thông tin người dùng từ bảng users
    private void loadUserData(String userEmail) {
        String query = "SELECT first_name, last_name, phone_num, city FROM users WHERE email = ?";
        try (Connection con = mysqlconnect.ConnectDb(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, userEmail);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                emailField.setText(userEmail);
                initialFirstName = rs.getString("first_name");
                initialLastName = rs.getString("last_name");
                initialPhone = rs.getString("phone_num");
                initialCity = rs.getString("city");

                firstNameField.setText(initialFirstName);
                lastNameField.setText(initialLastName);
                phoneField.setText(initialPhone);
                cityField.setValue(initialCity);
            } else {
                System.out.println("User not found in database for email: " + userEmail);
            }
        } catch (Exception e) {
            System.out.println("Error loading user data: " + e.getMessage());
        }
    }

    // Hàm lấy danh sách tỉnh/thành từ bảng cities
    private void loadCities() {
        ObservableList<String> cities = FXCollections.observableArrayList();
        String query = "SELECT city_name FROM cities";
        try (Connection con = mysqlconnect.ConnectDb(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = con.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                cities.add(rs.getString("city_name"));
            }
            cityField.setItems(cities);
        } catch (Exception e) {
            System.out.println("Error loading cities: " + e.getMessage());
            cities.addAll("Hanoi", "Ho Chi Minh City", "Da Nang", "Hai Phong");
            cityField.setItems(cities);
        }
    }

    // Theo dõi thay đổi trong các trường
    private void setupChangeListeners() {
        ChangeListener<String> changeListener = new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                checkForChanges();
            }
        };

        firstNameField.textProperty().addListener(changeListener);
        lastNameField.textProperty().addListener(changeListener);
        phoneField.textProperty().addListener(changeListener);
        cityField.valueProperty().addListener(changeListener);
    }

    // Kiểm tra xem có thay đổi nào không
    private void checkForChanges() {
        String currentFirstName = firstNameField.getText();
        String currentLastName = lastNameField.getText();
        String currentPhone = phoneField.getText();
        String currentCity = cityField.getValue();

        boolean hasChanges = !(currentFirstName.equals(initialFirstName) &&
                              currentLastName.equals(initialLastName) &&
                              currentPhone.equals(initialPhone) &&
                              (currentCity == null ? initialCity == null : currentCity.equals(initialCity)));

        applyButton.setDisable(!hasChanges);
    }

    // Xử lý sự kiện nút "Lưu"
    @FXML
    private void handleApplyButton() {
        String email = emailField.getText();
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String phone = phoneField.getText();
        String city = cityField.getValue();

        String query = "UPDATE users SET first_name = ?, last_name = ?, phone_num = ?, city = ? WHERE email = ?";
        try (Connection con = mysqlconnect.ConnectDb(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, firstName);
            ps.setString(2, lastName);
            ps.setString(3, phone);
            ps.setString(4, city);
            ps.setString(5, email);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("User data updated successfully");
                initialFirstName = firstName;
                initialLastName = lastName;
                initialPhone = phone;
                initialCity = city;
                applyButton.setDisable(true);

                // Cập nhật userdata.json
                JSONUtility.storeUserData(userId, firstName, lastName, email, phone, city);
                System.out.println("userdata.json updated successfully");

                // Hiển thị thông báo thành công
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Thành công");
                alert.setHeaderText(null);
                alert.setContentText("Cập nhật thông tin thành công!");
                alert.showAndWait().ifPresent(response -> {
                    // Sau khi nhấn OK, chuyển về màn hình Home
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Cinema/UI/Home.fxml"));
                        Parent root = loader.load();
                        Stage stage = (Stage) applyButton.getScene().getWindow();
                        Scene scene = new Scene(root);
                        stage.setScene(scene);
                        stage.setTitle("Home");
                        stage.setMaximized(true);
                        stage.show();
                    } catch (Exception e) {
                        System.out.println("Error loading Home.fxml: " + e.getMessage());
                    }
                });
            } else {
                System.out.println("No user found to update for email: " + email);
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Lỗi");
                alert.setHeaderText(null);
                alert.setContentText("Không tìm thấy người dùng để cập nhật!");
                alert.showAndWait();
            }
        } catch (Exception e) {
            System.out.println("Error updating user data: " + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText(null);
            alert.setContentText("Có lỗi xảy ra khi cập nhật thông tin: " + e.getMessage());
            alert.showAndWait();
        }
    }

    // Xử lý sự kiện nút "Đổi mật khẩu"
    @FXML
    private void handleChangePasswordButton() {
        System.out.println("Change password button clicked - Implement this feature!");
    }
}