package Cinema.controller;

import Cinema.database.mysqlconnect;
import Cinema.util.Ticket;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

public class AdminTicketsController implements Initializable {

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
    @FXML private TableColumn<Ticket, Double> colTotalPrice;
    @FXML private TableColumn<Ticket, String> colSeats;
    @FXML private TableColumn<Ticket, String> colShowtime;
    @FXML private TableColumn<Ticket, String> colBookingDate;
    @FXML private TableColumn<Ticket, String> colUserInfo;
    @FXML private TableColumn<Ticket, String> colStatus;
    @FXML private TextField searchField; // Thêm ô tìm kiếm

    private ObservableList<Ticket> ticketList = FXCollections.observableArrayList();
    private ObservableList<Ticket> filteredTicketList = FXCollections.observableArrayList();
    private Controller parentController;
    private static final String URL = "jdbc:mysql://localhost/Cinema_DB";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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

        // Xử lý sự kiện khi người dùng nhập từ khóa tìm kiếm
        searchField.setOnKeyReleased(event -> {
            // Chỉ gọi filterAndSortTickets() khi người dùng nhập xong một ký tự
            filterAndSortTickets();
        });
    }

    public void setParentController(Controller parentController) {
        this.parentController = parentController;
    }

    private void setupTable() {
        colTicketId.setCellValueFactory(new PropertyValueFactory<>("ticketId"));
        colMovieName.setCellValueFactory(new PropertyValueFactory<>("movieName"));
        colTotalPrice.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        colSeats.setCellValueFactory(new PropertyValueFactory<>("seats"));
        colShowtime.setCellValueFactory(new PropertyValueFactory<>("showtime"));
        colBookingDate.setCellValueFactory(new PropertyValueFactory<>("bookingDate"));
        colUserInfo.setCellValueFactory(new PropertyValueFactory<>("userName"));
        colStatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus() == 1 ? "Active" : "Deactivated"));

        ticketTable.setItems(filteredTicketList);

        // Thiết lập SelectionModel
        ticketTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // Thêm sự kiện nhấp đúp để mở trang chi tiết
        ticketTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && ticketTable.getSelectionModel().getSelectedItem() != null) {
                Ticket selectedTicket = ticketTable.getSelectionModel().getSelectedItem();
                openTicketDetail(selectedTicket);
            }
        });
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
        String query = "SELECT t.id, t.userID, t.movieID, t.showtimeID, t.seatNumbers, t.totalPrice, t.status, t.createDate, " +
                      "s.date, s.time, m.name, u.first_name, u.last_name, u.email, s.screen " +
                      "FROM bookedTickets t " +
                      "JOIN showtimes s ON t.showtimeID = s.id_lichchieu " +
                      "JOIN movies m ON t.movieID = m.id " +
                      "JOIN users u ON t.userID = u.id";

        try (Connection conn = mysqlconnect.ConnectDb(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int ticketId = rs.getInt("id");
                int userId = rs.getInt("userID");
                String movieName = rs.getString("name");
                double totalPrice = rs.getDouble("totalPrice");
                String seats = rs.getString("seatNumbers");
                int status = rs.getInt("status");
                String date = rs.getString("date"); // Định dạng: "2025-03-20"
                String time = rs.getString("time");
                // Kiểm tra và xử lý time
                if (time == null || time.length() < 5) {
                    System.err.println("Time không hợp lệ cho ticket " + ticketId + ": " + time);
                    time = "00:00";
                } else {
                    time = time.substring(0, 5); // Định dạng: "14:00"
                }
                String showtime = time + ", " + formatDate(date); // Định dạng: "14:00, 20-03-2025"
                LocalDateTime bookingDateTime = rs.getTimestamp("createDate").toLocalDateTime();
                String bookingDate = bookingDateTime.format(DateTimeFormatter.ofPattern("HH:mm, dd-MM-yyyy"));
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String userName = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
                userName = userName.trim();
                if (userName.isEmpty()) userName = "Unknown User";
                String userEmail = rs.getString("email");
                int screen = rs.getInt("screen");

                ticketList.add(new Ticket(ticketId, userId, movieName, totalPrice, seats, showtime, bookingDate, bookingDateTime, status, userName, userEmail, screen));
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

        // Lấy từ khóa tìm kiếm, làm sạch và chuyển thành chữ thường
        String searchText = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        // Loại bỏ các ký tự không mong muốn (nếu cần)
        searchText = searchText.replaceAll("\\s+", " "); // Chuẩn hóa khoảng trắng

        // Lọc theo tiêu chí ngày, tháng, năm
        List<Ticket> tempList = new ArrayList<>();
        switch (filterType) {
            case "Theo ngày":
                LocalDate selectedDate = datePicker.getValue();
                if (selectedDate != null) {
                    for (Ticket ticket : ticketList) {
                        String bookingDate = ticket.getBookingDate();
                        LocalDate ticketDate = parseBookingDate(bookingDate);
                        if (ticketDate != null && ticketDate.equals(selectedDate)) {
                            tempList.add(ticket);
                        }
                    }
                } else {
                    tempList.addAll(ticketList); // Nếu không chọn ngày, hiển thị tất cả
                }
                break;

            case "Theo tháng và năm":
                String selectedMonth = monthFilter.getValue();
                String selectedYearForMonth = yearFilterForMonth.getValue();
                if (selectedMonth == null || selectedYearForMonth == null) {
                    tempList.addAll(ticketList); // Nếu không chọn tháng hoặc năm, hiển thị tất cả
                    break;
                }
                for (Ticket ticket : ticketList) {
                    String bookingDate = ticket.getBookingDate();
                    LocalDate ticketDate = parseBookingDate(bookingDate);
                    if (ticketDate != null) {
                        boolean matchesMonth = selectedMonth.equals("Tất cả") || ticketDate.getMonthValue() == Integer.parseInt(selectedMonth);
                        boolean matchesYear = selectedYearForMonth.equals("Tất cả") || ticketDate.getYear() == Integer.parseInt(selectedYearForMonth);
                        if (matchesMonth && matchesYear) {
                            tempList.add(ticket);
                        }
                    }
                }
                break;

            case "Theo năm":
                String selectedYear = yearFilter.getValue();
                if (selectedYear == null) {
                    tempList.addAll(ticketList); // Nếu không chọn năm, hiển thị tất cả
                    break;
                }
                for (Ticket ticket : ticketList) {
                    String bookingDate = ticket.getBookingDate();
                    LocalDate ticketDate = parseBookingDate(bookingDate);
                    if (ticketDate != null) {
                        boolean matchesYear = selectedYear.equals("Tất cả") || ticketDate.getYear() == Integer.parseInt(selectedYear);
                        if (matchesYear) {
                            tempList.add(ticket);
                        }
                    }
                }
                break;

            case "Tất cả":
            default:
                tempList.addAll(ticketList); // Hiển thị tất cả vé
                break;
        }

        // Lọc theo từ khóa tìm kiếm
        if (!searchText.isEmpty()) {
            for (Ticket ticket : tempList) {
                boolean matchesSearch = String.valueOf(ticket.getTicketId()).toLowerCase().contains(searchText) ||
                                       ticket.getMovieName().toLowerCase().contains(searchText) ||
                                       ticket.getSeats().toLowerCase().contains(searchText) ||
                                       ticket.getShowtime().toLowerCase().contains(searchText) ||
                                       ticket.getBookingDate().toLowerCase().contains(searchText) ||
                                       ticket.getUserName().toLowerCase().contains(searchText) ||
                                       (ticket.getStatus() == 1 ? "active" : "deactivated").contains(searchText);
                if (matchesSearch) {
                    filteredTicketList.add(ticket);
                }
            }
        } else {
            filteredTicketList.addAll(tempList);
        }

        // Bước 2: Sắp xếp dữ liệu
        String sortType = sortTypeComboBox.getValue();
        if (sortType == null) {
            sortType = "Không sắp xếp"; // Đặt giá trị mặc định nếu sortType là null
        }

        switch (sortType) {
            case "Giá vé: Thấp tới cao":
                FXCollections.sort(filteredTicketList, Comparator.comparingDouble(Ticket::getTotalPrice));
                break;
            case "Giá vé: Cao tới thấp":
                FXCollections.sort(filteredTicketList, Comparator.comparingDouble(Ticket::getTotalPrice).reversed());
                break;
            case "Tên phim: A-Z":
                FXCollections.sort(filteredTicketList, Comparator.comparing(Ticket::getMovieName));
                break;
            case "Tên phim: Z-A":
                FXCollections.sort(filteredTicketList, Comparator.comparing(Ticket::getMovieName).reversed());
                break;
            case "Ngày đặt: Gần đây nhất":
                FXCollections.sort(filteredTicketList, Comparator.comparing(Ticket::getBookingDateTime).reversed());
                break;
            case "Ngày đặt: Lâu nhất":
                FXCollections.sort(filteredTicketList, Comparator.comparing(Ticket::getBookingDateTime));
                break;
            case "Không sắp xếp":
            default:
                // Không làm gì, giữ nguyên thứ tự
                break;
        }
    }
    private void openTicketDetail(Ticket ticket) {
        try {
            URL fxmlLocation = getClass().getResource("/Cinema/UI/TicketDetailView.fxml");

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            TicketDetailController controller = loader.getController();
            controller.setTicket(ticket);
            controller.setAdminTicketsController(this);

            Stage stage = new Stage();
            stage.setTitle("Ticket Details - ID: " + ticket.getTicketId());
            stage.setScene(new Scene(root));
            stage.initStyle(StageStyle.UNDECORATED); // Ẩn thanh tiêu đề
            stage.show();
         // Khi cửa sổ mất focus, tự động đóng lại
            stage.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (!isNowFocused) {
                    stage.close();
                }
            });
            
        } catch (IOException e) {
            System.err.println("Lỗi khi mở trang chi tiết vé: " + e.getMessage());
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Không thể mở trang chi tiết vé: " + e.getMessage());
            alert.showAndWait();
        }
    }

    public void refreshTickets() {
        loadTicketsFromDatabase();
    }

    // Phương thức định dạng ngày từ "2025-03-20" thành "20-03-2025"
    private String formatDate(String date) {
        String[] parts = date.split("-");
        return parts[2] + "-" + parts[1] + "-" + parts[0];
    }

    // Phương thức phân tích bookingDate để lấy ngày (dùng cho lọc)
    private LocalDate parseBookingDate(String bookingDate) {
        try {
            // bookingDate có định dạng "HH:mm, dd-MM-yyyy"
            String datePart = bookingDate.split(", ")[1]; // Lấy phần "dd-MM-yyyy"
            String[] dateParts = datePart.split("-");
            int day = Integer.parseInt(dateParts[0]);
            int month = Integer.parseInt(dateParts[1]);
            int year = Integer.parseInt(dateParts[2]);
            return LocalDate.of(year, month, day);
        } catch (Exception e) {
            System.err.println("Lỗi khi phân tích bookingDate: " + bookingDate);
            return null;
        }
    }
}