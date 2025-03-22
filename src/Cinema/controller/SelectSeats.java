package Cinema.controller;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.ArrayList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

import Cinema.database.JSONUtility;
import Cinema.database.JSONUtility.MovieData;
import Cinema.database.mysqlconnect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;

public class SelectSeats implements Initializable {
    private Stage stage;
    private Scene scene;
    private Parent root;

    public JSONUtility util;

    public String selectedSeats[] = {};
    public int totalPrice = 0, basePrice = 0;
    private String id_lichchieu; // Lưu id_lichchieu của suất chiếu
    private int totalNumberSeats; // Tổng số ghế của suất chiếu
    private static final DecimalFormat formatter = new DecimalFormat("#,###");

    @FXML
    private AnchorPane seatsPane;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private HBox premiumHbox, normalHbox, vipHbox;

    @FXML
    private Label premiumPrice, normalPrice, vipPrice;

    @FXML
    private Button proceedToPaymentBtn, cancelBtn;

    // Lưu giá của từng ghế dựa trên loại ghế
    private static class SeatButton extends Button {
        private int seatPrice; // Giá của ghế

        public SeatButton(String text, int seatPrice) {
            super(text);
            this.seatPrice = seatPrice;
        }

        public int getSeatPrice() {
            return seatPrice;
        }
    }

    public SelectSeats() {
        this.util = new JSONUtility();
    }

    // Phương thức để khởi tạo với id_lichchieu và totalNumberSeats
    public void initializeSeatSelection(String id_lichchieu, int totalNumberSeats) {
        this.id_lichchieu = id_lichchieu;
        this.totalNumberSeats = totalNumberSeats;
        initialize(null, null); // Gọi lại initialize để cập nhật giao diện
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        MovieData moviedata = util.getMovieJson();

        if (moviedata == null) {
            cancelBtn.fire();
            return;
        }
        

     // Trong phương thức
     int basePrice = moviedata.basePrice;

     premiumPrice.setText(formatter.format(basePrice + 50000) + " đ");
     normalPrice.setText(formatter.format(basePrice + 30000) + " đ");
     vipPrice.setText(formatter.format(basePrice) + " đ");
     
        double paneWidth = Screen.getPrimary().getBounds().getWidth();
        seatsPane.setPrefWidth(paneWidth);
        GridPane selectSeatsWrap1 = new GridPane(); // Premium
        selectSeatsWrap1.setHgap(10);
        GridPane selectSeatsWrap2 = new GridPane(); // Normal
        selectSeatsWrap2.setVgap(10);
        selectSeatsWrap2.setHgap(10);
        GridPane selectSeatsWrap3 = new GridPane(); // VIP
        selectSeatsWrap3.setHgap(10);

        // Lấy id_lichchieu từ JSON (thay vì từ biến instance)
        String jsonIdLichChieu = moviedata.id;

        // Lấy danh sách ghế đã đặt từ CSDL dựa trên id_lichchieu từ JSON
        String[] bookedSeats = getBookedSeatsFromDatabase(jsonIdLichChieu);
        moviedata.bookedSeats = bookedSeats; // Cập nhật bookedSeats trong JSON

        // Hiển thị ghế dựa trên totalNumberSeats
        int maxSeats = Math.min(totalNumberSeats, 150);
        for (int i = maxSeats - 1; i >= 0; i--) {
            String seatCode = getSeatCode(i);
            int seatPrice;

            // Xác định giá của ghế dựa trên loại ghế
            if (i >= 140) { // Premium (A-J, hàng 19-20)
                seatPrice = basePrice + 50000;
            } else if (i < 10) { // VIP (H-J, hàng 0-1)
                seatPrice = basePrice;
            } else { // Normal (K-G, hàng 2-18)
                seatPrice = basePrice + 30000;
            }

            SeatButton btn = new SeatButton(seatCode, seatPrice);
            btn.setText(seatCode);

            boolean isBooked = checkAvailability(seatCode, bookedSeats);

            if (isBooked) {
                btn.getStyleClass().add("booked-seats");
                btn.setDisable(true);
            } else {
                btn.getStyleClass().add("available-seats");
                btn.setOnAction(event -> handleSelection(event));
            }

            if (i >= 140) { // Premium (A-J, hàng 19-20)
                selectSeatsWrap1.add(btn, i % 10, i / 10);
            } else if (i < 10) { // VIP (H-J, hàng 0-1)
                selectSeatsWrap3.add(btn, i % 10, i / 10);
            } else { // Normal (K-G, hàng 2-18)
                selectSeatsWrap2.add(btn, i % 10, 14 - i / 10);
            }
        }

        premiumHbox.setPadding(new Insets(10, 0, 20, 0));
        normalHbox.setPadding(new Insets(10, 0, 20, 0));
        vipHbox.setPadding(new Insets(10, 0, 20, 0));
        premiumHbox.getChildren().add(selectSeatsWrap1);
        normalHbox.getChildren().add(selectSeatsWrap2);
        vipHbox.getChildren().add(selectSeatsWrap3);

        // Khởi tạo totalPrice từ các ghế đã chọn trước đó (nếu có)
        totalPrice = 0;
        if (moviedata.selectedSeats != null) {
            selectedSeats = moviedata.selectedSeats;
            for (String seat : selectedSeats) {
                int seatLevel = seatLevel(seat);
                if (seatLevel == 2) { // Premium
                    totalPrice += basePrice + 30000;
                } else if (seatLevel == 1) { // VIP
                    totalPrice += basePrice + 50000;
                } else { // Normal
                    totalPrice += basePrice;
                }
            }
            moviedata.totalPrice = totalPrice;
            util.updateMovieJson(selectedSeats, totalPrice);
        }
    }

    // Lấy danh sách ghế đã đặt từ CSDL dựa trên id_lichchieu từ JSON
    private String[] getBookedSeatsFromDatabase(String id_lichchieu) {
        List<String> bookedSeatsList = new ArrayList<>();
        String url = "jdbc:mysql://localhost/Cinema_DB";
        String username = "root";
        String password = "";

        try (Connection conn = mysqlconnect.ConnectDb(url, username, password);
             PreparedStatement pstmt = conn.prepareStatement("SELECT seatNumbers FROM bookedTickets WHERE showTimeID = ?")) {

            pstmt.setString(1, id_lichchieu);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String seatNumbers = rs.getString("seatNumbers");
                if (seatNumbers != null && !seatNumbers.isEmpty()) {
                    // Giả định seatNumbers là chuỗi phân tách bởi dấu phẩy
                    String[] seats = seatNumbers.split(", ");
                    bookedSeatsList.addAll(Arrays.asList(seats));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy ghế đã đặt: " + e.getMessage());
            e.printStackTrace();
        }

        return bookedSeatsList.toArray(new String[0]);
    }

    public void handleCancelBtnClick(ActionEvent event) throws IOException {
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        
        stage.close();
    }

    public void handleProceedToPaymentPageClick(ActionEvent event) throws IOException {
        if (selectedSeats.length > 0) {
            MovieData moviedata = util.getMovieJson();
            moviedata.totalPrice = totalPrice;
            moviedata.selectedSeats = selectedSeats;
            moviedata.numberOfSeats = selectedSeats.length;
            util.updateMovieJson(selectedSeats, totalPrice);

            // Chuyển sang màn hình xác nhận
            Parent root = FXMLLoader.load(getClass().getResource("/Cinema/UI/ConfirmTicketUI.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, 800, 496);

            // Đặt Stage ở giữa màn hình
            stage.setScene(scene);

            // Tính toán vị trí trung tâm thủ công
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            double screenWidth = screenBounds.getWidth();
            double screenHeight = screenBounds.getHeight();
            double stageWidth = 800;  // Kích thước Scene
            double stageHeight = 496; // Kích thước Scene

            stage.setX((screenWidth - stageWidth) / 2);
            stage.setY((screenHeight - stageHeight) / 2);

            stage.show();
        }
    }

    public String getSeatCode(int num) {
        char[] chs = new char[10];
        char startChar = 'A';
        for (int i = 0; i < chs.length; i++) {
            chs[i] = (char) (startChar + i);
        }
        String st2 = Character.toString(chs[num % 10]);
        String str = Integer.toString(num / 10 + 1) + st2;
        return str;
    }

    public int getSeatNumber(String seatCode) {
        char[] chs = new char[10];
        char startChar = 'A';
        for (int i = 0; i < chs.length; i++) {
            chs[i] = (char) (startChar + i);
        }
        int seatCol = -1;
        for (int i = 0; i < chs.length; i++) {
            if (chs[i] == seatCode.toCharArray()[1]) {
                seatCol = i;
                break;
            }
        }
        int seatRow = (Integer.parseInt(seatCode.substring(0, 1)) - 1) * 10;
        int seatNum = seatRow + seatCol;
        return seatNum;
    }

    public boolean checkAvailability(String seat, String[] booked) {
        List<String> list = Arrays.asList(booked);
        return list.contains(seat);
    }

    public int seatLevel(String str) {
        if (str.charAt(0) == 'A') return 2; // Premium
        else if (str.charAt(0) == 'H') return 1; // VIP
        return 0; // Normal
    }

    public String[] getUpdatedSelection(String[] orgArr, int method, String el) {
        String[] newArr = {};
        if (method == 1) {
            newArr = Arrays.copyOf(orgArr, orgArr.length + 1);
            newArr[orgArr.length] = el;
        }
        if (method == 0) {
            for (int i = 0; i < orgArr.length; i++) {
                if (!el.equals(orgArr[i])) {
                    newArr[i] = orgArr[i];
                }
            }
        }
        return newArr;
    }

    public void handleSelection(ActionEvent event) {
        SeatButton btn = (SeatButton) event.getSource();
        boolean alreadySelected = Arrays.stream(selectedSeats).anyMatch(e -> e.equals(btn.getText()));
        if (alreadySelected) {
            // Bỏ chọn ghế
            selectedSeats = Arrays.stream(selectedSeats).filter(el -> !el.equals(btn.getText())).toArray(String[]::new);
            selectedSeats = Arrays.stream(selectedSeats).filter(el -> el != null).toArray(String[]::new);
            btn.getStyleClass().remove("selected-seats");
            totalPrice -= btn.getSeatPrice(); // Giảm totalPrice dựa trên giá của ghế
        } else {
            // Chọn ghế
            selectedSeats = Arrays.copyOf(selectedSeats, selectedSeats.length + 1);
            selectedSeats[selectedSeats.length - 1] = btn.getText();
            btn.getStyleClass().add("selected-seats");
            totalPrice += btn.getSeatPrice(); // Tăng totalPrice dựa trên giá của ghế
        }
        // Cập nhật totalPrice và selectedSeats trong JSON
        MovieData moviedata = util.getMovieJson();
        moviedata.totalPrice = totalPrice;
        moviedata.selectedSeats = selectedSeats;
        moviedata.numberOfSeats = selectedSeats.length;
        util.updateMovieJson(selectedSeats, totalPrice);
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