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
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.google.gson.Gson;
import java.util.List;
import java.util.stream.Collectors;

public class AccountSettingController {

    @FXML
    private Button applyButton;

    @FXML
    private Button changePasswordButton;

    @FXML
    private ComboBox<String> cityField;

    @FXML
    private ComboBox<String> quanField;

    @FXML
    private ComboBox<String> phuongField;

    @FXML
    private TextField homeAddressField;

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
    private String initialQuan;
    private String initialPhuong;
    private String initialHomeAddress;
    private int userId;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    private List<Province> provinces; // Lưu toàn bộ dữ liệu tỉnh, quận/huyện, phường/xã

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

        loadAllDataFromApi();
        setupChangeListeners();
    }

    private void loadAllDataFromApi() {
        System.out.println("Loading all data from API with depth=3...");
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://provinces.open-api.vn/api/?depth=3"))
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("API response status: " + response.statusCode());
            if (response.statusCode() == 200) {
                provinces = List.of(gson.fromJson(response.body(), Province[].class));
                ObservableList<String> cityNames = FXCollections.observableArrayList(
                    provinces.stream().map(Province::getName).collect(Collectors.toList())
                );
                cityField.setItems(cityNames);
                System.out.println("Loaded " + cityNames.size() + " provinces: " + cityNames);

                if (initialCity != null && cityNames.contains(initialCity)) {
                    cityField.setValue(initialCity);
                    updateDistricts(initialCity);
                } else {
                    cityField.setValue("Thành phố Hà Nội");
                    updateDistricts("Thành phố Hà Nội");
                }
            } else {
                System.err.println("API call failed with status: " + response.statusCode());
                cityField.setItems(FXCollections.observableArrayList("Hà Nội", "Hồ Chí Minh", "Đà Nẵng"));
            }
        } catch (Exception e) {
            System.err.println("Error loading data: " + e.getMessage());
            cityField.setItems(FXCollections.observableArrayList("Hà Nội", "Hồ Chí Minh", "Đà Nẵng"));
        }
    }

    private void updateDistricts(String cityName) {
        System.out.println("Updating districts for city: " + cityName);
        quanField.getItems().clear();
        phuongField.getItems().clear();

        if (provinces == null) {
            System.err.println("Provinces list is null!");
            return;
        }

        Province selectedProvince = provinces.stream()
            .filter(p -> p.getName().equals(cityName))
            .findFirst()
            .orElse(null);

        if (selectedProvince == null) {
            System.err.println("No province found for: " + cityName);
            return;
        }

        List<District> districts = selectedProvince.getDistricts();
        if (districts == null || districts.isEmpty()) {
            System.err.println("No districts found for province: " + cityName);
            return;
        }

        ObservableList<String> districtNames = FXCollections.observableArrayList(
            districts.stream().map(District::getName).collect(Collectors.toList())
        );
        quanField.setItems(districtNames);
        System.out.println("Loaded " + districtNames.size() + " districts: " + districtNames);

        if (initialQuan != null && districtNames.contains(initialQuan)) {
            quanField.setValue(initialQuan);
            updateWards(cityName, initialQuan);
        } else if (!districtNames.isEmpty()) {
            quanField.setValue(districtNames.get(0));
            updateWards(cityName, districtNames.get(0));
        }
    }

    private void updateWards(String cityName, String districtName) {
        System.out.println("Updating wards for district: " + districtName + " in city: " + cityName);
        phuongField.getItems().clear();

        if (provinces == null) {
            System.err.println("Provinces list is null!");
            return;
        }

        Province selectedProvince = provinces.stream()
            .filter(p -> p.getName().equals(cityName))
            .findFirst()
            .orElse(null);

        if (selectedProvince == null) {
            System.err.println("No province found for: " + cityName);
            return;
        }

        District selectedDistrict = selectedProvince.getDistricts().stream()
            .filter(d -> d.getName().equals(districtName))
            .findFirst()
            .orElse(null);

        if (selectedDistrict == null) {
            System.err.println("No district found for: " + districtName);
            return;
        }

        List<Ward> wards = selectedDistrict.getWards();
        if (wards == null || wards.isEmpty()) {
            System.err.println("No wards found for district: " + districtName);
            return;
        }

        ObservableList<String> wardNames = FXCollections.observableArrayList(
            wards.stream().map(Ward::getName).collect(Collectors.toList())
        );
        phuongField.setItems(wardNames);
        System.out.println("Loaded " + wardNames.size() + " wards: " + wardNames);

        if (initialPhuong != null && wardNames.contains(initialPhuong)) {
            phuongField.setValue(initialPhuong);
        } else if (!wardNames.isEmpty()) {
            phuongField.setValue(wardNames.get(0));
        }
    }

    private void loadUserData(String userEmail) {
        String query = "SELECT first_name, last_name, phone_num, city, quan, phuong, homeAddress FROM users WHERE email = ?";
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
                initialQuan = rs.getString("quan");
                initialPhuong = rs.getString("phuong");
                initialHomeAddress = rs.getString("homeAddress");

                firstNameField.setText(initialFirstName);
                lastNameField.setText(initialLastName);
                phoneField.setText(initialPhone);
                homeAddressField.setText(initialHomeAddress);
            } else {
                System.out.println("User not found in database for email: " + userEmail);
            }
        } catch (Exception e) {
            System.out.println("Error loading user data: " + e.getMessage());
        }
    }

    private void setupChangeListeners() {
        cityField.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                updateDistricts(newValue);
            }
        });

        quanField.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null && cityField.getValue() != null) {
                updateWards(cityField.getValue(), newValue);
            }
        });

        ChangeListener<String> changeListener = (observable, oldValue, newValue) -> checkForChanges();
        firstNameField.textProperty().addListener(changeListener);
        lastNameField.textProperty().addListener(changeListener);
        phoneField.textProperty().addListener(changeListener);
        cityField.valueProperty().addListener(changeListener);
        quanField.valueProperty().addListener(changeListener);
        phuongField.valueProperty().addListener(changeListener);
        homeAddressField.textProperty().addListener(changeListener);
    }

    private void checkForChanges() {
        String currentFirstName = firstNameField.getText();
        String currentLastName = lastNameField.getText();
        String currentPhone = phoneField.getText();
        String currentCity = cityField.getValue();
        String currentQuan = quanField.getValue();
        String currentPhuong = phuongField.getValue();
        String currentHomeAddress = homeAddressField.getText();

        boolean hasChanges = !(currentFirstName.equals(initialFirstName) &&
                              currentLastName.equals(initialLastName) &&
                              currentPhone.equals(initialPhone) &&
                              (currentCity == null ? initialCity == null : currentCity.equals(initialCity)) &&
                              (currentQuan == null ? initialQuan == null : currentQuan.equals(initialQuan)) &&
                              (currentPhuong == null ? initialPhuong == null : currentPhuong.equals(initialPhuong)) &&
                              (currentHomeAddress == null ? initialHomeAddress == null : currentHomeAddress.equals(initialHomeAddress)));

        applyButton.setDisable(!hasChanges);
    }

    @FXML
    private void handleApplyButton() {
        String email = emailField.getText();
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String phone = phoneField.getText();
        String city = cityField.getValue();
        String quan = quanField.getValue();
        String phuong = phuongField.getValue();
        String homeAddress = homeAddressField.getText();

        String query = "UPDATE users SET first_name = ?, last_name = ?, phone_num = ?, city = ?, quan = ?, phuong = ?, homeAddress = ? WHERE email = ?";
        try (Connection con = mysqlconnect.ConnectDb(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, firstName);
            ps.setString(2, lastName);
            ps.setString(3, phone);
            ps.setString(4, city);
            ps.setString(5, quan);
            ps.setString(6, phuong);
            ps.setString(7, homeAddress);
            ps.setString(8, email);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("User data updated successfully");
                initialFirstName = firstName;
                initialLastName = lastName;
                initialPhone = phone;
                initialCity = city;
                initialQuan = quan;
                initialPhuong = phuong;
                initialHomeAddress = homeAddress;
                applyButton.setDisable(true);

                JSONUtility.storeUserData(userId, firstName, lastName, email, phone, city, quan, phuong, homeAddress);
                System.out.println("userdata.json updated successfully");

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Thành công");
                alert.setHeaderText(null);
                alert.setContentText("Cập nhật thông tin thành công!");
                alert.showAndWait().ifPresent(response -> {
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

    @FXML
    private void handleChangePasswordButton() {

    }

    private static class Province {
        private String name;
        private int code;
        private List<District> districts;

        public String getName() { return name; }
        public int getCode() { return code; }
        public List<District> getDistricts() { return districts; }
    }

    private static class District {
        private String name;
        private int code;
        private String division_type;
        private List<Ward> wards;

        public String getName() { return name; }
        public int getCode() { return code; }
        public String getDivisionType() { return division_type; }
        public List<Ward> getWards() { return wards; }
    }

    private static class Ward {
        private String name;
        private int code;
        private String division_type;

        public String getName() { return name; }
        public int getCode() { return code; }
        public String getDivisionType() { return division_type; }
    }
}