package Cinema.controller;

import Cinema.database.mysqlconnect;
import Cinema.util.Ticket;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class TicketDetailController {

    @FXML private Label ticketIdLabel;
    @FXML private Text movieNameLabel;
    @FXML private Label totalPriceLabel;
    @FXML private Label seatsLabel;
    @FXML private Label showtimeLabel;
    @FXML private Label bookingDateLabel; // Thêm bookingDateLabel
    @FXML private Label statusLabel;
    @FXML private Label userNameLabel;
    @FXML private Label userEmailLabel;
    @FXML private Button deactivateButton;

    private Ticket ticket;
    private AdminTicketsController adminTicketsController;
    private static final String URL = "jdbc:mysql://localhost/Cinema_DB";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
        displayTicketDetails();
        updateDeactivateButton();
    }

    public void setAdminTicketsController(AdminTicketsController controller) {
        this.adminTicketsController = controller;
    }

    private void displayTicketDetails() {
        ticketIdLabel.setText("Mã vé: " + ticket.getTicketId());
        movieNameLabel.setText("Tên phim: " + ticket.getMovieName());
        totalPriceLabel.setText("Tổng giá: " + ticket.getTotalPrice());
        seatsLabel.setText("Ghế: " + ticket.getSeats());
        showtimeLabel.setText("Giờ chiếu: " + ticket.getShowtime());
        bookingDateLabel.setText("Ngày đặt: " + ticket.getBookingDate());
        statusLabel.setText("Trạng thái: " + (ticket.getStatus() == 1 ? "Active" : "Deactivated"));
        userNameLabel.setText("Người đặt: " + ticket.getUserName());
        userEmailLabel.setText("Email: " + ticket.getUserEmail());
    }

    private void updateDeactivateButton() {
        if (ticket.getStatus() == 0) {
            deactivateButton.setDisable(true);
            deactivateButton.setText("Vé đã bị vô hiệu");
        } else {
            deactivateButton.setDisable(false);
            deactivateButton.setText("Vô hiệu hóa vé");
        }
    }

    @FXML
    private void deactivateTicket() {
        try (Connection conn = mysqlconnect.ConnectDb(URL, USER, PASSWORD)) {
            if (conn == null) {
                System.err.println("Không thể kết nối đến cơ sở dữ liệu");
                return;
            }
            String sql = "UPDATE bookedTickets SET status = 0 WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, ticket.getTicketId());
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Vé " + ticket.getTicketId() + " đã được vô hiệu hóa thành công.");
                ticket.setStatus(0);
                displayTicketDetails();
                updateDeactivateButton();
                if (adminTicketsController != null) {
                    adminTicketsController.refreshTickets();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}