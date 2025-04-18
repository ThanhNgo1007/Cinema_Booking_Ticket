package Cinema.controller;

import java.io.IOException;
import java.text.DecimalFormat;

import Cinema.database.EmailUtility;
import Cinema.database.JSONUtility;
import Cinema.database.JSONUtility.MovieData;
import Cinema.util.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class Booked {

    @FXML
    private AnchorPane downloadTicketBt;
    @FXML
    private Button completeButton;

    private JSONUtility jsonUtility;
    private static final DecimalFormat formatter = new DecimalFormat("#,###");

    public Booked() {
        this.jsonUtility = new JSONUtility();
    }

    @FXML
    void goBack(ActionEvent event) throws IOException {
    	 // Lấy thông tin người dùng
        User user = jsonUtility.getUserData();
        if (user == null || user.getEmail() == null || user.getEmail().isEmpty()) {
            System.err.println("Không tìm thấy dữ liệu người dùng hoặc email không hợp lệ.");
            return;
        }

        // Lấy thông tin vé từ JSONUtility
        MovieData movieData = jsonUtility.getMovieJson();
        if (movieData == null || movieData.selectedSeats == null || movieData.selectedSeats.length == 0) {
            System.err.println("Không có dữ liệu vé hợp lệ để gửi email.");
            return;
        }

        // Tạo nội dung email
        String subject = "Xác nhận đặt vé xem phim - " + movieData.name;
        String message = buildTicketEmailMessage(movieData, user);

        // Gửi email
        boolean emailSent = EmailUtility.sendEmail(message, subject, user.getEmail());
        if (emailSent) {
            System.out.println("Email xác nhận đã được gửi đến " + user.getEmail());
        } else {
            System.err.println("Không thể gửi email xác nhận đến " + user.getEmail() + ".");
        }

        // Xóa thông tin vé trong moviedata.json sau khi gửi email
        boolean cleared = jsonUtility.clearMovieData();
        if (cleared) {
            System.out.println("Đã xóa thông tin vé trong moviedata.json.");
        } else {
            System.err.println("Không thể xóa thông tin vé trong moviedata.json.");
        }

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
    
    // Phương thức để tạo nội dung email
    private String buildTicketEmailMessage(MovieData movieData, User user) {
        StringBuilder message = new StringBuilder();
        message.append("Chào ").append(user.getUserName()).append(",\n\n");
        message.append("Cảm ơn bạn đã đặt vé xem phim tại rạp của chúng tôi! Dưới đây là thông tin chi tiết về vé của bạn:\n\n");

        // Thêm thông tin vé
        message.append("Tên phim: ").append(movieData.name != null ? movieData.name : "N/A").append("\n");
        message.append("Thời gian chiếu: ").append(movieData.timing != null ? movieData.timing : "N/A").append("\n");
        message.append("Ghế đã đặt: ").append(String.join(", ", movieData.selectedSeats)).append("\n");
        message.append("Số lượng vé: ").append(movieData.numberOfSeats).append("\n");
        message.append("Tổng giá: ").append(formatter.format(movieData.totalPrice)).append(" đ\n\n");
        message.append("Vui lòng đưa email này cho nhân viên tại quầy trước giờ chiếu để nhận vé.\n\n");
        message.append("Nếu có bất kỳ thắc mắc nào, hãy liên hệ với chúng tôi.\n\n");
        message.append("Chúc bạn có một buổi xem phim vui vẻ!\n");
        message.append("Trân trọng,\n");
        message.append("Đội ngũ rạp chiếu phim");

        return message.toString();
    }
}