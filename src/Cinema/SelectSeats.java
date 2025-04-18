package Cinema.controller;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import Cinema.database.JSONUtility;
import Cinema.database.JSONUtility.MovieData;
import Cinema.database.mysqlconnect;
import Cinema.util.SeatButton;
import Cinema.util.SeatType;
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

public class SelectSeats implements Initializable {
    private Stage stage;
    private Scene scene;
    private Parent root;

    public JSONUtility util;
    public String[] selectedSeats = {};
    public int totalPrice = 0, basePrice = 0;
    private String id_lichchieu;
    private int totalNumberSeats;
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

    

    public SelectSeats() {
        this.util = new JSONUtility();
    }

    public void initializeSeatSelection(String id_lichchieu, int totalNumberSeats) {
        this.id_lichchieu = id_lichchieu;
        this.totalNumberSeats = totalNumberSeats;
        initialize(null, null);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        MovieData moviedata = util.getMovieJson();
        if (moviedata == null) {
            cancelBtn.fire();
            return;
        }

        basePrice = moviedata.basePrice;
        premiumPrice.setText(formatter.format(basePrice + SeatType.PREMIUM.getPriceOffset()) + " đ");
        normalPrice.setText(formatter.format(basePrice + SeatType.NORMAL.getPriceOffset()) + " đ");
        vipPrice.setText(formatter.format(basePrice + SeatType.VIP.getPriceOffset()) + " đ");

        double paneWidth = Screen.getPrimary().getBounds().getWidth();
        seatsPane.setPrefWidth(paneWidth);

        GridPane premiumGrid = new GridPane();
        premiumGrid.setHgap(10);
        GridPane vipGrid = new GridPane();
        vipGrid.setVgap(10);
        vipGrid.setHgap(10);
        GridPane normalGrid = new GridPane();
        normalGrid.setHgap(10);

        String jsonIdLichChieu = moviedata.id;
        String[] bookedSeats = getBookedSeatsFromDatabase(jsonIdLichChieu);

        int maxSeats = Math.min(totalNumberSeats, 150);
        for (int i = maxSeats - 1; i >= 0; i--) {
            String seatCode = getSeatCode(i);
            SeatType seatType;
            int seatPrice;

            // Assign seat types and prices (VIP and NORMAL swapped)
            if (i >= 140) { // Premium: Rows 19-20 (A-J)
                seatType = SeatType.PREMIUM;
                seatPrice = basePrice + SeatType.PREMIUM.getPriceOffset();
            } else if (i >= 10 && i < 140) { // VIP: Rows 2-18 (K-G)
                seatType = SeatType.VIP;
                seatPrice = basePrice + SeatType.VIP.getPriceOffset();
            } else { // Normal: Rows 0-1 (H-J)
                seatType = SeatType.NORMAL;
                seatPrice = basePrice + SeatType.NORMAL.getPriceOffset();
            }

            SeatButton btn = new SeatButton(seatCode, seatPrice);
            btn.setText(seatCode);

            boolean isBooked = checkAvailability(seatCode, bookedSeats);
            if (isBooked) {
                btn.getStyleClass().add("booked-seats");
                btn.setDisable(true);
            } else {
                btn.getStyleClass().add("available-seats");
                btn.setOnAction(this::handleSelection);
            }

            // Place buttons in the appropriate grid
            if (seatType == SeatType.PREMIUM) {
                premiumGrid.add(btn, i % 10, i / 10);
            } else if (seatType == SeatType.VIP) {
                vipGrid.add(btn, i % 10, 14 - i / 10);
            } else { // NORMAL
                normalGrid.add(btn, i % 10, i / 10);
            }
        }

        premiumHbox.setPadding(new Insets(10, 0, 20, 0));
        vipHbox.setPadding(new Insets(10, 0, 20, 0));
        normalHbox.setPadding(new Insets(10, 0, 20, 0));
        premiumHbox.getChildren().add(premiumGrid);
        vipHbox.getChildren().add(vipGrid);
        normalHbox.getChildren().add(normalGrid);

        totalPrice = 0;
        if (moviedata.selectedSeats != null) {
            selectedSeats = moviedata.selectedSeats;
            for (String seat : selectedSeats) {
                totalPrice += basePrice + getSeatType(seat).getPriceOffset();
            }
            moviedata.totalPrice = totalPrice;
            util.updateMovieJson(selectedSeats, totalPrice);
        }
    }

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
                    bookedSeatsList.addAll(Arrays.asList(seatNumbers.split(", ")));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching booked seats: " + e.getMessage());
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

            Parent root = FXMLLoader.load(getClass().getResource("/Cinema/UI/ConfirmTicketUI.fxml"));
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            scene = new Scene(root, 1100, 553);
            stage.setScene(scene);
            centerStage(stage, 1100, 553);
            stage.show();
        }
    }

    public String getSeatCode(int num) {
        char[] chs = new char[10];
        char startChar = 'A';
        for (int i = 0; i < chs.length; i++) {
            chs[i] = (char) (startChar + i);
        }
        return (num / 10 + 1) + Character.toString(chs[num % 10]);
    }

    public boolean checkAvailability(String seat, String[] booked) {
        return Arrays.asList(booked).contains(seat);
    }

    // Determine seat type based on seat code
    private SeatType getSeatType(String seatCode) {
        int seatNum = getSeatNumber(seatCode);
        if (seatNum >= 140) return SeatType.PREMIUM;
        else if (seatNum >= 10) return SeatType.VIP;
        else return SeatType.NORMAL;
    }

    public int getSeatNumber(String seatCode) {
        char[] chs = new char[10];
        char startChar = 'A';
        for (int i = 0; i < chs.length; i++) {
            chs[i] = (char) (startChar + i);
        }
        int seatCol = -1;
        for (int i = 0; i < chs.length; i++) {
            if (chs[i] == seatCode.charAt(seatCode.length() - 1)) {
                seatCol = i;
                break;
            }
        }
        int seatRow = (Integer.parseInt(seatCode.substring(0, seatCode.length() - 1)) - 1) * 10;
        return seatRow + seatCol;
    }

    public void handleSelection(ActionEvent event) {
        SeatButton btn = (SeatButton) event.getSource();
        boolean alreadySelected = Arrays.stream(selectedSeats).anyMatch(e -> e.equals(btn.getText()));
        if (alreadySelected) {
            selectedSeats = Arrays.stream(selectedSeats).filter(el -> !el.equals(btn.getText())).toArray(String[]::new);
            btn.getStyleClass().remove("selected-seats");
            totalPrice -= btn.getSeatPrice();
        } else {
            selectedSeats = Arrays.copyOf(selectedSeats, selectedSeats.length + 1);
            selectedSeats[selectedSeats.length - 1] = btn.getText();
            btn.getStyleClass().add("selected-seats");
            totalPrice += btn.getSeatPrice();
        }

        MovieData moviedata = util.getMovieJson();
        moviedata.totalPrice = totalPrice;
        moviedata.selectedSeats = selectedSeats;
        moviedata.numberOfSeats = selectedSeats.length;
        util.updateMovieJson(selectedSeats, totalPrice);
    }

    private void centerStage(Stage stage, double width, double height) {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX((screenBounds.getWidth() - width) / 2);
        stage.setY((screenBounds.getHeight() - height) / 2);
    }
}