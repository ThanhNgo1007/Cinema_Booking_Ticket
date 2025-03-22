package Cinema.controller;

import Cinema.database.mysqlconnect;
import Cinema.database.Movie; // Sử dụng Movie từ Cinema.database
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class AddShowtimeController {

    @FXML private Label movieNameLabel;
    @FXML private TextField showtimeIdField;
    @FXML private TextField showDateField;
    @FXML private TextField showTimeField;

    private Movie movie;
    private AdminPanelController adminPanelController;

    public void setMovie(Movie movie) {
        this.movie = movie;
        movieNameLabel.setText("Phim: " + movie.getMovieName());
    }

    public void setAdminPanelController(AdminPanelController adminPanelController) {
        this.adminPanelController = adminPanelController;
    }

    @FXML
    private void addShowtime() {
        String showtimeId = showtimeIdField.getText();
        String showDate = showDateField.getText();
        String showTime = showTimeField.getText();

        // Kiểm tra dữ liệu đầu vào
        if (showtimeId.isEmpty() || showDate.isEmpty() || showTime.isEmpty()) {
            showAlert("Lỗi", "Vui lòng điền đầy đủ thông tin.");
            return;
        }

        // Kiểm tra xem ID lịch chiếu có bị trùng không
        if (isShowtimeIdExists(showtimeId)) {
            showAlert("Lỗi", "ID lịch chiếu đã tồn tại. Vui lòng nhập ID khác.");
            return;
        }

        // Kiểm tra định dạng ngày (YYYY-MM-DD)
        try {
            LocalDate.parse(showDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (DateTimeParseException e) {
            showAlert("Lỗi", "Ngày chiếu phải có định dạng YYYY-MM-DD (ví dụ: 2025-03-20).");
            return;
        }

        // Kiểm tra định dạng giờ (HH:MM)
        try {
            LocalTime.parse(showTime, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException e) {
            showAlert("Lỗi", "Giờ chiếu phải có định dạng HH:MM (ví dụ: 14:00).");
            return;
        }

        // Lưu lịch chiếu vào cơ sở dữ liệu
        String query = "INSERT INTO showtimes (id_lichchieu, id_movie, date, time, totalNumberSeats, bookedSeatsCount) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = mysqlconnect.ConnectDb("jdbc:mysql://localhost/Cinema_DB", "root", "");
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, showtimeId); // ID lịch chiếu do người dùng nhập
            pstmt.setString(2, movie.getMovieID()); // ID phim từ phim đang chọn
            pstmt.setString(3, showDate); // Ngày chiếu
            pstmt.setString(4, showTime); // Giờ chiếu
            pstmt.setInt(5, 150); // Tổng số ghế mặc định là 150
            pstmt.setInt(6, 0); // Số ghế đã đặt mặc định là 0

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