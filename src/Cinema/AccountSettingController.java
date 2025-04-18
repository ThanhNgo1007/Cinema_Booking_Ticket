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
import Cinema.util.User;
import Cinema.util.District;
import Cinema.util.Province;
import Cinema.util.Ward;
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
    private User initialUser; // Thay thế các biến riêng lẻ bằng một đối tượng User
    private int userId;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    private List<Province> provinces; // Lưu danh sách tỉnh

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

        loadProvincesFromApi(); // Tải danh sách tỉnh trực tiếp từ API
        setupChangeListeners();
    }

    private void loadProvincesFromApi() {
        System.out.println("Loading provinces from API...");
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://provinces.open-api.vn/api/")) // Chỉ lấy danh sách tỉnh
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

                if (initialUser != null && initialUser.getCityName() != null && cityNames.contains(initialUser.getCityName())) {
                    cityField.setValue(initialUser.getCityName());
                    loadDistrictsForProvince(initialUser.getCityName());
                } else {
                    cityField.setValue("Thành phố Hà Nội");
                    loadDistrictsForProvince("Thành phố Hà Nội");
                }
            } else {
                System.err.println("API call failed with status: " + response.statusCode());
                cityField.setItems(FXCollections.observableArrayList("Hà Nội", "Hồ Chí Minh", "Đà Nẵng"));
            }
        } catch (Exception e) {
            System.err.println("Error loading provinces: " + e.getMessage());
            cityField.setItems(FXCollections.observableArrayList("Hà Nội", "Hồ Chí Minh", "Đà Nẵng"));
        }
    }

    private void loadDistrictsForProvince(String provinceName) {
        System.out.println("Loading districts for province: " + provinceName);
        quanField.getItems().clear();
        phuongField.getItems().clear();

        Province selectedProvince = provinces.stream()
            .filter(p -> p.getName().equals(provinceName))
            .findFirst()
            .orElse(null);

        if (selectedProvince == null) {
            System.err.println("No province found for: " + provinceName);
            return;
        }

        int provinceCode = selectedProvince.getCode();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://provinces.open-api.vn/api/p/" + provinceCode + "?depth=2"))
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                Province provinceData = gson.fromJson(response.body(), Province.class);
                List<District> districts = provinceData.getDistricts();
                ObservableList<String> districtNames = FXCollections.observableArrayList(
                    districts.stream().map(District::getName).collect(Collectors.toList())
                );
                quanField.setItems(districtNames);
                System.out.println("Loaded " + districtNames.size() + " districts: " + districtNames);

                if (initialUser != null && initialUser.getQuan() != null && districtNames.contains(initialUser.getQuan())) {
                    quanField.setValue(initialUser.getQuan());
                    loadWardsForDistrict(initialUser.getQuan(), districts);
                } else if (!districtNames.isEmpty()) {
                    quanField.setValue(districtNames.get(0));
                    loadWardsForDistrict(districtNames.get(0), districts);
                }
            } else {
                System.err.println("Failed to load districts, status: " + response.statusCode());
            }
        } catch (Exception e) {
            System.err.println("Error loading districts: " + e.getMessage());
        }
    }

    private void loadWardsForDistrict(String districtName, List<District> districts) {
        System.out.println("Loading wards for district: " + districtName);
        phuongField.getItems().clear();

        District selectedDistrict = districts.stream()
            .filter(d -> d.getName().equals(districtName))
            .findFirst()
            .orElse(null);

        if (selectedDistrict == null) {
            System.err.println("No district found for: " + districtName);
            return;
        }

        int districtCode = selectedDistrict.getCode();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://provinces.open-api.vn/api/d/" + districtCode + "?depth=2"))
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                District districtData = gson.fromJson(response.body(), District.class);
                List<Ward> wards = districtData.getWards();
                ObservableList<String> wardNames = FXCollections.observableArrayList(
                    wards.stream().map(Ward::getName).collect(Collectors.toList())
                );
                phuongField.setItems(wardNames);
                System.out.println("Loaded " + wardNames.size() + " wards: " + wardNames);

                if (initialUser != null && initialUser.getPhuong() != null && wardNames.contains(initialUser.getPhuong())) {
                    phuongField.setValue(initialUser.getPhuong());
                } else if (!wardNames.isEmpty()) {
                    phuongField.setValue(wardNames.get(0));
                }
            } else {
                System.err.println("Failed to load wards, status: " + response.statusCode());
            }
        } catch (Exception e) {
            System.err.println("Error loading wards: " + e.getMessage());
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
                // Tạo đối tượng User để lưu trữ thông tin ban đầu
                initialUser = new User(
                    userId,
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    userEmail,
                    rs.getString("phone_num"),
                    rs.getString("city"),
                    rs.getString("quan"),
                    rs.getString("phuong"),
                    rs.getString("homeAddress")
                );

                firstNameField.setText(initialUser.getFirstName());
                lastNameField.setText(initialUser.getLastName());
                phoneField.setText(initialUser.getPhoneNumber());
                homeAddressField.setText(initialUser.getHomeAddress());
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
                loadDistrictsForProvince(newValue);
            }
        });

        quanField.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null && cityField.getValue() != null) {
                Province selectedProvince = provinces.stream()
                    .filter(p -> p.getName().equals(cityField.getValue()))
                    .findFirst()
                    .orElse(null);
                if (selectedProvince != null) {
                    loadWardsForDistrict(newValue, selectedProvince.getDistricts());
                }
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

        boolean hasChanges = !(currentFirstName.equals(initialUser.getFirstName()) &&
                              currentLastName.equals(initialUser.getLastName()) &&
                              currentPhone.equals(initialUser.getPhoneNumber()) &&
                              (currentCity == null ? initialUser.getCityName() == null : currentCity.equals(initialUser.getCityName())) &&
                              (currentQuan == null ? initialUser.getQuan() == null : currentQuan.equals(initialUser.getQuan())) &&
                              (currentPhuong == null ? initialUser.getPhuong() == null : currentPhuong.equals(initialUser.getPhuong())) &&
                              (currentHomeAddress == null ? initialUser.getHomeAddress()== null : currentHomeAddress.equals(initialUser.getHomeAddress())));

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
                // Cập nhật lại initialUser với dữ liệu mới
                initialUser = new User(userId, firstName, lastName, email, phone, city, quan, phuong, homeAddress);
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
}