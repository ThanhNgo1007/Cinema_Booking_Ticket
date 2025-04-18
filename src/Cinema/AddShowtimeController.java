package Cinema.controller;

import Cinema.database.mysqlconnect;
import Cinema.util.Movie;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class AddShowtimeController implements Initializable {

    @FXML private Label movieNameLabel;
    @FXML private TextField showtimeIdField;
    @FXML private DatePicker showDateField; // Changed to DatePicker
    @FXML private TextField showTimeField; // Kept as TextField
    @FXML private ComboBox<Integer> screen;

    private Movie movie;
    private AdminPanelController adminPanelController;
    private ObservableList<Integer> availableScreens = FXCollections.observableArrayList();
    private int movieDuration; // Thời lượng phim (phút)

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Khởi tạo danh sách phòng chiếu
        loadScreens();
        screen.setItems(availableScreens);

        // Lắng nghe sự kiện thay đổi ngày và giờ để cập nhật danh sách phòng chiếu
        showDateField.valueProperty().addListener((obs, oldValue, newValue) -> updateAvailableScreens());
        showTimeField.textProperty().addListener((obs, oldValue, newValue) -> updateAvailableScreens());
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
        movieNameLabel.setText("Phim: " + movie.getMovieName());
        // Lấy duration của phim từ cơ sở dữ liệu
        loadMovieDuration();
    }

    public void setAdminPanelController(AdminPanelController adminPanelController) {
        this.adminPanelController = adminPanelController;
    }

    private void loadMovieDuration() {
        String query = "SELECT duration FROM movies WHERE id = ?";
        try (Connection conn = mysqlconnect.ConnectDb("jdbc:mysql://localhost/Cinema_DB", "root", "");
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, movie.getMovieID());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String durationStr = rs.getString("duration"); // Ví dụ: "117 phút"
                // Lấy số phút từ chuỗi duration
                movieDuration = Integer.parseInt(durationStr.replaceAll("[^0-9]", "")) + 30;
            } else {
                movieDuration = 120; // Giá trị mặc định nếu không tìm thấy duration
                showAlert("Cảnh báo", "Không tìm thấy thời lượng phim. Sử dụng giá trị mặc định 120 phút.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            movieDuration = 120; // Giá trị mặc định nếu có lỗi
            showAlert("Lỗi", "Không thể lấy thời lượng phim: " + e.getMessage());
        }
    }

    @FXML
    private void addShowtime() {
        String showtimeId = showtimeIdField.getText();
        LocalDate showDate = showDateField.getValue();
        String showTime = showTimeField.getText();
        Integer selectedScreen = screen.getValue();

        // Kiểm tra dữ liệu đầu vào
        if (showtimeId.isEmpty() || showDate == null || showTime.isEmpty() || selectedScreen == null) {
            showAlert("Lỗi", "Vui lòng điền đầy đủ thông tin, bao gồm ngày, giờ và phòng chiếu.");
            return;
        }

        // Kiểm tra xem ID lịch chiếu có bị trùng không
        if (isShowtimeIdExists(showtimeId)) {
            showAlert("Lỗi", "ID lịch chiếu đã tồn tại. Vui lòng nhập ID khác.");
            return;
        }

        // Định dạng ngày từ DatePicker
        String formattedDate = showDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // Kiểm tra định dạng giờ (HH:MM)
        LocalTime startTime;
        try {
            startTime = LocalTime.parse(showTime, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException e) {
            showAlert("Lỗi", "Giờ chiếu phải có định dạng HH:MM (ví dụ: 14:00).");
            return;
        }

        // Tính end_time dựa trên duration
        LocalDateTime startDateTime = LocalDateTime.of(showDate, startTime);
        LocalDateTime endDateTime = startDateTime.plusMinutes(movieDuration);
        LocalTime endTime = endDateTime.toLocalTime();

        // Lưu lịch chiếu vào cơ sở dữ liệu
        String query = "INSERT INTO showtimes (id_lichchieu, id_movie, date, time, end_time, totalNumberSeats, bookedSeatsCount, screen) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = mysqlconnect.ConnectDb("jdbc:mysql://localhost/Cinema_DB", "root", "");
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, showtimeId); // ID lịch chiếu
            pstmt.setString(2, movie.getMovieID()); // ID phim
            pstmt.setString(3, formattedDate); // Ngày chiếu
            pstmt.setString(4, showTime); // Giờ chiếu
            pstmt.setString(5, endTime.format(DateTimeFormatter.ofPattern("HH:mm"))); // Giờ kết thúc
            pstmt.setInt(6, 150); // Tổng số ghế mặc định là 150
            pstmt.setInt(7, 0); // Số ghế đã đặt mặc định là 0
            pstmt.setInt(8, selectedScreen); // Phòng chiếu

            pstmt.executeUpdate();

            showAlert("Thành công", "Đã thêm lịch chiếu thành công!");
            adminPanelController.refreshShowtimes(); // Làm mới danh sách
            closeWindow();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể thêm lịch chiếu: " + e.getMessage());
        }
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) showDateField.getScene().getWindow();
        stage.close();
    }

    private void loadScreens() {
        // Giả sử danh sách phòng (screen) được lưu trong cơ sở dữ liệu hoặc cố định
        // Ở đây tôi giả định có các phòng từ 1 đến 5
        availableScreens.clear();
        for (int i = 1; i <= 5; i++) {
            availableScreens.add(i);
        }
    }

    private void updateAvailableScreens() {
        LocalDate showDate = showDateField.getValue();
        String showTime = showTimeField.getText();

        // Kiểm tra xem ngày và giờ đã được nhập hợp lệ chưa
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;
        if (showDate == null || showTime.isEmpty()) {
            // Nếu ngày hoặc giờ chưa được nhập, hiển thị tất cả phòng chiếu
            loadScreens();
            screen.setItems(availableScreens);
            return;
        }

        try {
            LocalTime startTime = LocalTime.parse(showTime, DateTimeFormatter.ofPattern("HH:mm"));
            startDateTime = LocalDateTime.of(showDate, startTime);
            endDateTime = startDateTime.plusMinutes(movieDuration);
        } catch (DateTimeParseException e) {
            // Nếu giờ không hợp lệ, hiển thị tất cả phòng chiếu
            loadScreens();
            screen.setItems(availableScreens);
            return;
        }

        // Lấy danh sách các phòng chiếu đã có suất chiếu trong khoảng thời gian này
        List<Integer> bookedScreens = new ArrayList<>();
        String query = "SELECT screen FROM showtimes WHERE date = ? AND " +
                      "((time <= ? AND end_time >= ?) OR " +
                      "(time <= ? AND end_time >= ?) OR " +
                      "(time >= ? AND end_time <= ?))";
        try (Connection conn = mysqlconnect.ConnectDb("jdbc:mysql://localhost/Cinema_DB", "root", "");
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, showDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            pstmt.setString(2, startDateTime.toLocalTime().toString());
            pstmt.setString(3, startDateTime.toLocalTime().toString());
            pstmt.setString(4, endDateTime.toLocalTime().toString());
            pstmt.setString(5, endDateTime.toLocalTime().toString());
            pstmt.setString(6, startDateTime.toLocalTime().toString());
            pstmt.setString(7, endDateTime.toLocalTime().toString());

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                bookedScreens.add(rs.getInt("screen"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể kiểm tra phòng chiếu: " + e.getMessage());
            return;
        }

        // Cập nhật danh sách phòng chiếu khả dụng
        availableScreens.clear();
        for (int i = 1; i <= 5; i++) {
            if (!bookedScreens.contains(i)) {
                availableScreens.add(i);
            }
        }
        screen.setItems(availableScreens);

        // Nếu phòng chiếu hiện tại không còn khả dụng, xóa lựa chọn
        if (screen.getValue() != null && !availableScreens.contains(screen.getValue())) {
            screen.setValue(null);
        }
    }

    private boolean isShowtimeIdExists(String showtimeId) {
        String query = "SELECT COUNT(*) FROM showtimes WHERE id_lichchieu = ?";
        try (Connection conn = mysqlconnect.ConnectDb("jdbc:mysql://localhost/Cinema_DB", "root", "");
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, showtimeId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0; // Trả về true nếu ID đã tồn tại
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}