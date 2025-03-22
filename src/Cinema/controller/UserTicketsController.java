package Cinema.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import Cinema.database.JSONUtility;
import Cinema.database.mysqlconnect;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class UserTicketsController implements Initializable {

    @FXML private ComboBox<String> filterTypeComboBox;
    @FXML private ComboBox<String> sortTypeComboBox;
    @FXML private HBox filterContainer;
    @FXML private DatePicker datePicker;
    @FXML private Label monthLabel;
    @FXML private ComboBox<String> monthFilter;
    @FXML private Label yearLabelForMonth;
    @FXML private ComboBox<String> yearFilterForMonth;
    @FXML private Label yearLabel;
    @FXML private ComboBox<String> yearFilter;
    @FXML private TableView<Ticket> ticketTable;
    @FXML private TableColumn<Ticket, String> colTicketId;
    @FXML private TableColumn<Ticket, String> colMovieName;
    @FXML private TableColumn<Ticket, Integer> colTotalPrice;
    @FXML private TableColumn<Ticket, String> colSeats;
    @FXML private TableColumn<Ticket, String> colShowtime;
    @FXML private Label backButton;

    private ObservableList<Ticket> ticketList = FXCollections.observableArrayList();
    private ObservableList<Ticket> filteredTicketList = FXCollections.observableArrayList();
    private Controller parentController;
    private String userID; // ID của người dùng hiện tại
    private static final String URL = "jdbc:mysql://localhost/Cinema_DB";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    // Lớp đại diện cho một vé
    public static class Ticket {
        private String ticketId;
        private String movieName;
        private int totalPrice;
        private String seats;
        private String showtime;
        private LocalDateTime bookingDate;

        public Ticket(String ticketId, String movieName, int totalPrice, String seats, String showtime, LocalDateTime bookingDate) {
            this.ticketId = ticketId;
            this.movieName = movieName;
            this.totalPrice = totalPrice;
            this.seats = seats;
            this.showtime = showtime;
            this.bookingDate = bookingDate;
        }

        public String getTicketId() {
            return ticketId;
        }

        public String getMovieName() {
            return movieName;
        }

        public int getTotalPrice() {
            return totalPrice;
        }

        public String getSeats() {
            return seats;
        }

        public String getShowtime() {
            return showtime;
        }

        public LocalDateTime getBookingDate() {
            return bookingDate;
        }
    }
    
    public void setParentController(Controller parentController) {
        this.parentController = parentController;
    }

    @Override
    public void initialize(java.net.URL location, java.util.ResourceBundle resources) {
        // Lấy userID từ file userdata.json
        JSONUtility.User user = JSONUtility.getUserData();
        if (user != null) {
            this.userID = String.valueOf(user.getUserId());
        } else {
            this.userID = "1"; // Giá trị mặc định nếu không lấy được userID
        }

        // Thiết lập TableView
        setupTable();

        // Khởi tạo bộ lọc và sắp xếp
        setupFiltersAndSort();

        // Tải dữ liệu vé
        loadTicketsFromDatabase();

        // Xử lý sự kiện khi thay đổi kiểu lọc
        filterTypeComboBox.setOnAction(event -> updateFilterComponents());

        // Xử lý sự kiện khi thay đổi kiểu sắp xếp
        sortTypeComboBox.setOnAction(event -> filterAndSortTickets());

        // Xử lý sự kiện khi thay đổi giá trị bộ lọc
        datePicker.setOnAction(event -> filterAndSortTickets());
        monthFilter.setOnAction(event -> filterAndSortTickets());
        yearFilterForMonth.setOnAction(event -> filterAndSortTickets());
        yearFilter.setOnAction(event -> filterAndSortTickets());

        // Xử lý nút "Quay lại"
        backButton.setOnMouseClicked(event -> {
        	parentController.loadCenterContent("/Cinema/UI/Home_Center.fxml", backButton);
        });
    }

    private void setupTable() {
        colTicketId.setCellValueFactory(new PropertyValueFactory<>("ticketId"));
        colMovieName.setCellValueFactory(new PropertyValueFactory<>("movieName"));
        colTotalPrice.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        colSeats.setCellValueFactory(new PropertyValueFactory<>("seats"));
        colShowtime.setCellValueFactory(new PropertyValueFactory<>("showtime"));
        ticketTable.setItems(filteredTicketList);
    }

    private void setupFiltersAndSort() {
        // Khởi tạo kiểu lọc
        filterTypeComboBox.setItems(FXCollections.observableArrayList("Tất cả", "Theo ngày", "Theo tháng và năm", "Theo năm"));
        filterTypeComboBox.setValue("Tất cả");

        // Khởi tạo kiểu sắp xếp
        sortTypeComboBox.setItems(FXCollections.observableArrayList(
            "Không sắp xếp",
            "Giá vé: Thấp tới cao",
            "Giá vé: Cao tới thấp",
            "Tên phim: A-Z",
            "Tên phim: Z-A",
            "Ngày đặt: Gần đây nhất",
            "Ngày đặt: Lâu nhất"
        ));
        sortTypeComboBox.setValue("Không sắp xếp");

        // Khởi tạo danh sách tháng
        List<String> months = new ArrayList<>();
        months.add("Tất cả");
        for (int i = 1; i <= 12; i++) {
            months.add(String.format("%02d", i));
        }
        monthFilter.setItems(FXCollections.observableArrayList(months));
        monthFilter.setValue("Tất cả");

        // Khởi tạo danh sách năm
        List<String> years = new ArrayList<>();
        years.add("Tất cả");
        for (int i = 2020; i <= 2025; i++) {
            years.add(String.valueOf(i));
        }
        yearFilter.setItems(FXCollections.observableArrayList(years));
        yearFilter.setValue("Tất cả");
        yearFilterForMonth.setItems(FXCollections.observableArrayList(years));
        yearFilterForMonth.setValue("Tất cả");
    }

    private void updateFilterComponents() {
        // Ẩn tất cả các thành phần lọc
        datePicker.setVisible(false);
        datePicker.setManaged(false);
        monthLabel.setVisible(false);
        monthLabel.setManaged(false);
        monthFilter.setVisible(false);
        monthFilter.setManaged(false);
        yearLabelForMonth.setVisible(false);
        yearLabelForMonth.setManaged(false);
        yearFilterForMonth.setVisible(false);
        yearFilterForMonth.setManaged(false);
        yearLabel.setVisible(false);
        yearLabel.setManaged(false);
        yearFilter.setVisible(false);
        yearFilter.setManaged(false);

        // Hiển thị thành phần lọc tương ứng
        String filterType = filterTypeComboBox.getValue();
        if (filterType == null) {
            filterType = "Tất cả"; // Đặt giá trị mặc định nếu filterType là null
        }

        switch (filterType) {
            case "Theo ngày":
                datePicker.setVisible(true);
                datePicker.setManaged(true);
                break;
            case "Theo tháng và năm":
                monthLabel.setVisible(true);
                monthLabel.setManaged(true);
                monthFilter.setVisible(true);
                monthFilter.setManaged(true);
                yearLabelForMonth.setVisible(true);
                yearLabelForMonth.setManaged(true);
                yearFilterForMonth.setVisible(true);
                yearFilterForMonth.setManaged(true);
                break;
            case "Theo năm":
                yearLabel.setVisible(true);
                yearLabel.setManaged(true);
                yearFilter.setVisible(true);
                yearFilter.setManaged(true);
                break;
            case "Tất cả":
            default:
                // Không hiển thị thành phần lọc nào
                break;
        }

        // Lọc và sắp xếp lại dữ liệu
        filterAndSortTickets();
    }

    private void loadTicketsFromDatabase() {
        ticketList.clear();
        String query = "SELECT t.id, t.movieID, t.totalPrice, t.seatNumbers, t.createDate, s.date, s.time, m.name " +
                      "FROM bookedTickets t " +
                      "JOIN showtimes s ON t.showtimeID = s.id_lichchieu " +
                      "JOIN movies m ON t.movieID = m.id " +
                      "WHERE t.userID = ?";

        try (Connection conn = mysqlconnect.ConnectDb(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, userID);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String ticketId = rs.getString("id");
                String movieName = rs.getString("name");
                int totalPrice = rs.getInt("totalPrice");
                String seats = rs.getString("seatNumbers");
                String date = rs.getString("date"); // Định dạng: "2025-03-20"
                String time = rs.getString("time").substring(0, 5); // Định dạng: "14:00"
                String showtime = time + ", " + formatDate(date); // Định dạng: "14:00, 20-03-2025"
                LocalDateTime bookingDate = rs.getTimestamp("createDate").toLocalDateTime();

                ticketList.add(new Ticket(ticketId, movieName, totalPrice, seats, showtime, bookingDate));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy dữ liệu vé: " + e.getMessage());
            e.printStackTrace();
        }

        // Lọc và sắp xếp toàn bộ vé ban đầu
        filterAndSortTickets();
    }

    private void filterAndSortTickets() {
        // Bước 1: Lọc dữ liệu
        filteredTicketList.clear();
        String filterType = filterTypeComboBox.getValue();
        if (filterType == null) {
            filterType = "Tất cả"; // Đặt giá trị mặc định nếu filterType là null
        }

        switch (filterType) {
            case "Theo ngày":
                LocalDate selectedDate = datePicker.getValue();
                if (selectedDate != null) {
                    for (Ticket ticket : ticketList) {
                        LocalDate bookingDate = ticket.getBookingDate().toLocalDate();
                        if (bookingDate.equals(selectedDate)) {
                            filteredTicketList.add(ticket);
                        }
                    }
                } else {
                    filteredTicketList.addAll(ticketList); // Nếu không chọn ngày, hiển thị tất cả
                }
                break;

            case "Theo tháng và năm":
                String selectedMonth = monthFilter.getValue();
                String selectedYearForMonth = yearFilterForMonth.getValue();
                for (Ticket ticket : ticketList) {
                    LocalDateTime bookingDate = ticket.getBookingDate();
                    boolean matchesMonth = selectedMonth.equals("Tất cả") || bookingDate.getMonthValue() == Integer.parseInt(selectedMonth);
                    boolean matchesYear = selectedYearForMonth.equals("Tất cả") || bookingDate.getYear() == Integer.parseInt(selectedYearForMonth);
                    if (matchesMonth && matchesYear) {
                        filteredTicketList.add(ticket);
                    }
                }
                break;

            case "Theo năm":
                String selectedYear = yearFilter.getValue();
                for (Ticket ticket : ticketList) {
                    LocalDateTime bookingDate = ticket.getBookingDate();
                    boolean matchesYear = selectedYear.equals("Tất cả") || bookingDate.getYear() == Integer.parseInt(selectedYear);
                    if (matchesYear) {
                        filteredTicketList.add(ticket);
                    }
                }
                break;

            case "Tất cả":
            default:
                filteredTicketList.addAll(ticketList); // Hiển thị tất cả vé
                break;
        }

        // Bước 2: Sắp xếp dữ liệu
        String sortType = sortTypeComboBox.getValue();
        if (sortType == null) {
            sortType = "Không sắp xếp"; // Đặt giá trị mặc định nếu sortType là null
        }

        switch (sortType) {
            case "Giá vé: Thấp tới cao":
                FXCollections.sort(filteredTicketList, Comparator.comparingInt(Ticket::getTotalPrice));
                break;
            case "Giá vé: Cao tới thấp":
                FXCollections.sort(filteredTicketList, Comparator.comparingInt(Ticket::getTotalPrice).reversed());
                break;
            case "Tên phim: A-Z":
                FXCollections.sort(filteredTicketList, Comparator.comparing(Ticket::getMovieName));
                break;
            case "Tên phim: Z-A":
                FXCollections.sort(filteredTicketList, Comparator.comparing(Ticket::getMovieName).reversed());
                break;
            case "Ngày đặt: Gần đây nhất":
                FXCollections.sort(filteredTicketList, Comparator.comparing(Ticket::getBookingDate).reversed());
                break;
            case "Ngày đặt: Lâu nhất":
                FXCollections.sort(filteredTicketList, Comparator.comparing(Ticket::getBookingDate));
                break;
            case "Không sắp xếp":
            default:
                // Không làm gì, giữ nguyên thứ tự
                break;
        }
    }

    // Phương thức định dạng ngày từ "2025-03-20" thành "20-03-2025"
    private String formatDate(String date) {
        String[] parts = date.split("-");
        return parts[2] + "-" + parts[1] + "-" + parts[0];
    }
}