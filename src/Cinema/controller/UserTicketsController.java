package Cinema.controller;

import Cinema.database.JSONUtility;
import Cinema.database.mysqlconnect;
import Cinema.util.Ticket;
import Cinema.util.User;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

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
    @FXML private TableColumn<Ticket, Double> colTotalPrice;
    @FXML private TableColumn<Ticket, String> colSeats;
    @FXML private TableColumn<Ticket, String> colShowtime;
    @FXML private TableColumn<Ticket, String> colBookingDate;
    @FXML private TableColumn<Ticket, Integer> colScreen;

    @FXML private TextField searchField;

    @FXML private Button btnFirstPage;
    @FXML private Button btnPrevPage;
    @FXML private Label lblPageInfo;
    @FXML private Button btnNextPage;
    @FXML private Button btnLastPage;
    @FXML private Label lblTotalTickets;

    private ObservableList<Ticket> ticketList = FXCollections.observableArrayList();
    private ObservableList<Ticket> filteredTicketList = FXCollections.observableArrayList();
    private ObservableList<Ticket> pagedTicketList = FXCollections.observableArrayList();
    
    private String userID;
    private static final String URL = "jdbc:mysql://localhost/Cinema_DB";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    
    // Biến phân trang
    private static final int ITEMS_PER_PAGE = 10;
    private int currentPage = 1;
    private int totalPages = 1;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Lấy userID từ file userdata.json
        User user = JSONUtility.getUserData();
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

        // Xử lý sự kiện khi thay đổi từ khóa tìm kiếm
        searchField.textProperty().addListener((obs, oldValue, newValue) -> filterAndSortTickets());
        
        // Xử lý sự kiện cho các nút phân trang
        btnFirstPage.setOnAction(event -> goToFirstPage());
        btnPrevPage.setOnAction(event -> goToPrevPage());
        btnNextPage.setOnAction(event -> goToNextPage());
        btnLastPage.setOnAction(event -> goToLastPage());
    }


    private void setupTable() {
        colTicketId.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getTicketId())));
        colMovieName.setCellValueFactory(new PropertyValueFactory<>("movieName"));
        colTotalPrice.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        colSeats.setCellValueFactory(new PropertyValueFactory<>("seats"));
        colShowtime.setCellValueFactory(new PropertyValueFactory<>("showtime"));
        colScreen.setCellValueFactory(new PropertyValueFactory<>("screen"));
        colBookingDate.setCellValueFactory(new PropertyValueFactory<>("bookingDate"));
        ticketTable.setItems(pagedTicketList); // Sử dụng pagedTicketList thay vì filteredTicketList
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
        String query = "SELECT t.id, t.movieID, t.totalPrice, t.seatNumbers, t.createDate, s.date, s.time, m.name, s.screen " +
                      "FROM bookedTickets t " +
                      "JOIN showtimes s ON t.showtimeID = s.id_lichchieu " +
                      "JOIN movies m ON t.movieID = m.id " +
                      "WHERE t.userID = ?";

        try (Connection conn = mysqlconnect.ConnectDb(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, userID);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int ticketId = rs.getInt("id");
                String movieName = rs.getString("name");
                double totalPrice = rs.getDouble("totalPrice");
                String seats = rs.getString("seatNumbers");
                String date = rs.getString("date"); // Định dạng: "2025-03-20"
                String time = rs.getString("time");
                int screen = rs.getInt("screen");
                if (time == null || time.length() < 5) {
                    System.err.println("Time không hợp lệ cho ticket " + ticketId + ": " + time);
                    time = "00:00";
                } else {
                    time = time.substring(0, 5); // Định dạng: "14:00"
                }
                String showtime = time + ", " + formatDate(date); // Định dạng: "14:00, 20-03-2025"
                LocalDateTime bookingDateTime = rs.getTimestamp("createDate").toLocalDateTime();
                String bookingDate = bookingDateTime.format(DateTimeFormatter.ofPattern("HH:mm, dd-MM-yyyy"));

                ticketList.add(new Ticket(
                    ticketId,
                    Integer.parseInt(userID),
                    movieName,
                    totalPrice,
                    seats,
                    showtime,
                    bookingDate,
                    bookingDateTime,
                    1,
                    "",
                    "",
                    screen
                    
                ));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy dữ liệu vé: " + e.getMessage());
            e.printStackTrace();
        }

        // Lọc và sắp xếp toàn bộ vé ban đầu
        filterAndSortTickets();
    }

    	private void filterAndSortTickets() {
            // Bước 1: Lọc dữ liệu (giữ nguyên như cũ)
            filteredTicketList.clear();
            String filterType = filterTypeComboBox.getValue();
            if (filterType == null) {
                filterType = "Tất cả";
            }

            String searchText = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";

            List<Ticket> tempList = new ArrayList<>();
            switch (filterType) {
            case "Theo ngày":
                LocalDate selectedDate = datePicker.getValue();
                if (selectedDate != null) {
                    for (Ticket ticket : ticketList) {
                        LocalDate bookingDate = ticket.getBookingDateTime().toLocalDate();
                        if (bookingDate.equals(selectedDate)) {
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
                    LocalDateTime bookingDate = ticket.getBookingDateTime();
                    boolean matchesMonth = selectedMonth.equals("Tất cả") || bookingDate.getMonthValue() == Integer.parseInt(selectedMonth);
                    boolean matchesYear = selectedYearForMonth.equals("Tất cả") || bookingDate.getYear() == Integer.parseInt(selectedYearForMonth);
                    if (matchesMonth && matchesYear) {
                        tempList.add(ticket);
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
                    LocalDateTime bookingDate = ticket.getBookingDateTime();
                    boolean matchesYear = selectedYear.equals("Tất cả") || bookingDate.getYear() == Integer.parseInt(selectedYear);
                    if (matchesYear) {
                        tempList.add(ticket);
                    }
                }
                break;

            case "Tất cả":
            default:
                tempList.addAll(ticketList); // Hiển thị tất cả vé
                break;
        }

            if (!searchText.isEmpty()) {
                for (Ticket ticket : tempList) {
                    boolean matchesSearch = String.valueOf(ticket.getTicketId()).toLowerCase().contains(searchText) ||
                                           ticket.getMovieName().toLowerCase().contains(searchText) ||
                                           String.valueOf(ticket.getTotalPrice()).toLowerCase().contains(searchText) ||
                                           ticket.getSeats().toLowerCase().contains(searchText) ||
                                           ticket.getShowtime().toLowerCase().contains(searchText) ||
                                           ticket.getBookingDate().toLowerCase().contains(searchText);
                    if (matchesSearch) {
                        filteredTicketList.add(ticket);
                    }
                }
            } else {
                filteredTicketList.addAll(tempList);
            }

            // Bước 2: Sắp xếp dữ liệu (giữ nguyên như cũ)
            String sortType = sortTypeComboBox.getValue();
            if (sortType == null) {
                sortType = "Không sắp xếp";
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
            // Cập nhật tổng số vé
            updateTotalTicketsCount();
            updatePagination();
    }
    	private void updatePagination() {
            currentPage = 1; // Reset về trang đầu tiên khi lọc/sắp xếp
            totalPages = (int) Math.ceil((double) filteredTicketList.size() / ITEMS_PER_PAGE);
            if (totalPages == 0) totalPages = 1;
            
            updatePage();
        }

        private void updatePage() {
            // Tính toán chỉ số bắt đầu và kết thúc
            int fromIndex = (currentPage - 1) * ITEMS_PER_PAGE;
            int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, filteredTicketList.size());
            
            // Cập nhật danh sách hiển thị
            pagedTicketList.setAll(filteredTicketList.subList(fromIndex, toIndex));
            
            // Cập nhật thông tin trang
            lblPageInfo.setText(String.format("Trang %d/%d", currentPage, totalPages));
            
            // Cập nhật trạng thái các nút
            btnFirstPage.setDisable(currentPage == 1);
            btnPrevPage.setDisable(currentPage == 1);
            btnNextPage.setDisable(currentPage == totalPages);
            btnLastPage.setDisable(currentPage == totalPages);
        }

        private void goToFirstPage() {
            currentPage = 1;
            updatePage();
        }

        private void goToPrevPage() {
            if (currentPage > 1) {
                currentPage--;
                updatePage();
            }
        }

        private void goToNextPage() {
            if (currentPage < totalPages) {
                currentPage++;
                updatePage();
            }
        }

        private void goToLastPage() {
            currentPage = totalPages;
            updatePage();
        }
        
     // Thêm phương thức cập nhật tổng số vé
        private void updateTotalTicketsCount() {
            int total = filteredTicketList.size();
            lblTotalTickets.setText("Tổng số vé: " + total);
        }
        
    // Phương thức định dạng ngày từ "2025-03-20" thành "20-03-2025"
    private String formatDate(String date) {
        String[] parts = date.split("-");
        return parts[2] + "-" + parts[1] + "-" + parts[0];
    }
}