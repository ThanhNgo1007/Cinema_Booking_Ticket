package Cinema.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

import Cinema.database.JSONUtility;
import Cinema.database.JSONUtility.MovieData;
import Cinema.database.JSONUtility.User;
import Cinema.database.mysqlconnect;

public class ConfirmTicketController {
    private Stage stage;
    private Scene scene;
    private Parent root;

    private JSONUtility util = new JSONUtility();
    private MovieData moviedata;

    @FXML
    private Label titleLabel, movieNameLabel, showtimeLabel, cinemaLabel, seatLabel, totalPriceLabel, totalPriceLabel1;
    private static final DecimalFormat formatter = new DecimalFormat("#,###");

    public void initialize() {
        moviedata = util.getMovieJson();
        if (moviedata != null) {
            movieNameLabel.setText(moviedata.name);
            showtimeLabel.setText("Thời gian chiếu: " + moviedata.timing);
            cinemaLabel.setText("Rạp: CGV Sense City Cần Thơ ");
            seatLabel.setText("Ghế: " + String.join(", ", moviedata.selectedSeats));
            totalPriceLabel.setText(formatter.format(moviedata.totalPrice) + " đ");
            totalPriceLabel1.setText(formatter.format(moviedata.totalPrice) + " đ");
        }
    }

    @FXML
    public void handleConfirmBtnClick(ActionEvent event) throws IOException {
        JSONUtility.User userData = JSONUtility.getUserData();
        if (userData == null || userData.userId == 0) {
            System.err.println("Không thể lấy thông tin người dùng hợp lệ từ userdata.json.");
            return;
        }

        saveTicketToDatabase(userData.userId);
        updateNumberOfSeatsInShowtimes();

        root = FXMLLoader.load(getClass().getResource("/Cinema/UI/Booked.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root, 800, 500); // Kích thước cho Booked.fxml

        stage.setScene(scene);
        centerStage(stage, 800, 500); // Căn giữa với kích thước 900x600
        stage.show();
    }

    @FXML
    public void handleCancelBtnClick(ActionEvent event) throws IOException {
        // Xóa các ghế đã chọn và tổng tiền trong MovieData
        moviedata.selectedSeats = new String[]{};
        moviedata.totalPrice = 0;
        moviedata.numberOfSeats = 0;
        util.updateMovieJson(moviedata.selectedSeats, moviedata.totalPrice);

        // Load SeatSelection.fxml
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Cinema/UI/SeatSelection.fxml"));
        root = loader.load();

        // Lấy controller của SelectSeats
        SelectSeats seatsController = loader.getController();

        // Lấy id_lichchieu từ moviedata
        String id_lichchieu = moviedata.id;

        // Lấy totalNumberSeats từ cơ sở dữ liệu hoặc sử dụng giá trị mặc định
        int totalNumberSeats = getTotalNumberSeatsFromDatabase(id_lichchieu);
        if (totalNumberSeats == 0) {
            totalNumberSeats = 150; // Giá trị mặc định nếu không lấy được từ CSDL
        }

        // Gọi initializeSeatSelection để truyền dữ liệu
        seatsController.initializeSeatSelection(id_lichchieu, totalNumberSeats);

        // Cập nhật Stage
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root); // Đặt kích thước lớn hơn cho SeatSelection.fxml

        stage.setScene(scene);
        centerStage(stage, 1367, 800); // Căn giữa với kích thước 1200x700
        stage.show();
    }

    private int getTotalNumberSeatsFromDatabase(String id_lichchieu) {
        String url = "jdbc:mysql://localhost/Cinema_DB";
        String username = "root";
        String password = "";
        int totalNumberSeats = 0;

        try (Connection conn = mysqlconnect.ConnectDb(url, username, password)) {
            if (conn == null) {
                System.err.println("Không thể kết nối đến cơ sở dữ liệu.");
                return totalNumberSeats;
            }

            String sql = "SELECT totalNumberSeats FROM showtimes WHERE id_lichchieu = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, id_lichchieu);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    totalNumberSeats = rs.getInt("totalNumberSeats");
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy totalNumberSeats: " + e.getMessage());
            e.printStackTrace();
        }
        return totalNumberSeats;
    }

    private void saveTicketToDatabase(int userId) {
        String url = "jdbc:mysql://localhost/Cinema_DB";
        String username = "root";
        String password = "";

        try (Connection conn = mysqlconnect.ConnectDb(url, username, password)) {
            if (conn == null) {
                System.err.println("Không thể kết nối đến cơ sở dữ liệu.");
                return;
            }

            String sql = "INSERT INTO bookedTickets (userId, movieId, seatNumbers, showTimeID, basePrice, totalPrice, status, currentStatus) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, userId);
                pstmt.setString(2, getMovieId());
                pstmt.setString(3, String.join(", ", moviedata.selectedSeats));
                pstmt.setString(4, moviedata.id);
                pstmt.setInt(5, moviedata.basePrice);
                pstmt.setInt(6, moviedata.totalPrice);
                pstmt.setInt(7, 1);
                pstmt.setBoolean(8, true);

                pstmt.executeUpdate();
                System.out.println("Đã lưu vé vào cơ sở dữ liệu cho userId: " + userId);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lưu vé vào cơ sở dữ liệu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateNumberOfSeatsInShowtimes() {
        String url = "jdbc:mysql://localhost/Cinema_DB";
        String username = "root";
        String password = "";

        try (Connection conn = mysqlconnect.ConnectDb(url, username, password)) {
            if (conn == null) {
                System.err.println("Không thể kết nối đến cơ sở dữ liệu.");
                return;
            }

            int currentNumberOfSeats = 0;
            String selectSql = "SELECT bookedSeatsCount FROM showtimes WHERE id_lichchieu = ?";
            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                selectStmt.setString(1, moviedata.id);
                ResultSet rs = selectStmt.executeQuery();
                if (rs.next()) {
                    currentNumberOfSeats = rs.getInt("bookedSeatsCount");
                }
            }

            int newSeats = moviedata.selectedSeats.length;
            int updatedNumberOfSeats = currentNumberOfSeats + newSeats;

            String updateSql = "UPDATE showtimes SET bookedSeatsCount = ? WHERE id_lichchieu = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setInt(1, updatedNumberOfSeats);
                updateStmt.setString(2, moviedata.id);
                updateStmt.executeUpdate();
                System.out.println("Đã cập nhật bookedSeatsCount trong showtimes: " + updatedNumberOfSeats + " ghế cho suất chiếu " + moviedata.id);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi cập nhật bookedSeatsCount trong showtimes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String getMovieId() {
        String url = "jdbc:mysql://localhost/Cinema_DB";
        String username = "root";
        String password = "";
        String movieId = "";

        try (Connection conn = mysqlconnect.ConnectDb(url, username, password)) {
            if (conn == null) {
                System.err.println("Không thể kết nối đến cơ sở dữ liệu.");
                return movieId;
            }

            try (PreparedStatement pstmt = conn.prepareStatement("SELECT id_movie FROM showtimes WHERE id_lichchieu = ?")) {
                pstmt.setString(1, moviedata.id);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    movieId = rs.getString("id_movie");
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy movieId: " + e.getMessage());
            e.printStackTrace();
        }
        return movieId;
    }

    // Hàm căn giữa Stage với kích thước tùy chỉnh
    private void centerStage(Stage stage, double width, double height) {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double screenWidth = screenBounds.getWidth();
        double screenHeight = screenBounds.getHeight();

        stage.setX((screenWidth - width) / 2);
        stage.setY((screenHeight - height) / 2);
    }
}