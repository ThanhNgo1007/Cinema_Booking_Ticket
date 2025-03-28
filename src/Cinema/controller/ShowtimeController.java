package Cinema.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import Cinema.database.JSONUtility;
import Cinema.database.mysqlconnect;

public class ShowtimeController {

    @FXML
    private Button cancelButton;

    @FXML
    private GridPane dateContainer;

    @FXML
    private HBox showtimeContainer;

    private LocalDate selectedDate;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private List<Showtime> showtimes;
    private String movieID;
    private List<Button> dayButtons = new ArrayList<>();

    public static class Showtime {
        private String id_lichchieu;
        private LocalDate date;
        private String time;
        private String id_movie;
        private int bookedSeatsCount;
        private int totalNumberSeats;

        public Showtime(String id_lichchieu, LocalDate date, String time, String id_movie, int bookedSeatsCount, int totalNumberSeats) {
            this.id_lichchieu = id_lichchieu;
            this.date = date;
            this.time = time;
            this.setId_movie(id_movie);
            this.bookedSeatsCount = bookedSeatsCount;
            this.totalNumberSeats = totalNumberSeats;
        }

        public String getId_lichchieu() {
            return id_lichchieu;
        }

        public LocalDate getDate() {
            return date;
        }

        public String getTime() {
            return time;
        }

        public int getAvailableSeats() {
            return totalNumberSeats - bookedSeatsCount;
        }

        public int getTotalNumberSeats() {
            return totalNumberSeats;
        }

        public String getId_movie() {
            return id_movie;
        }

        public void setId_movie(String id_movie) {
            this.id_movie = id_movie;
        }
    }

    public void setMovieId(String movieID) {
        this.movieID = movieID;
        loadShowtimesFromDatabase();
        displayCalendar();
    }

    @FXML
    public void initialize() {
        if (movieID == null) {
            movieID = "";
        }
        loadShowtimesFromDatabase();
        displayCalendar();

        cancelButton.setOnAction(event -> {
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.close();
        });
    }

    private void displayCalendar() {
        dateContainer.getChildren().clear();
        dayButtons.clear();

        LocalDate today = LocalDate.now();
        int dayOfWeek = today.getDayOfWeek().getValue() % 7;

        int numberOfDaysToShow = 14;
        int row = 0, col = dayOfWeek;

        for (int i = 0; i < numberOfDaysToShow; i++) {
            LocalDate date = today.plusDays(i);
            Button dayButton = new Button(String.format("%02d\n%s", date.getDayOfMonth(), date.getDayOfWeek().toString().substring(0, 3)));
            
            dayButton.getStyleClass().add("day-button");
            if (date.equals(selectedDate)) {
                dayButton.getStyleClass().add("day-button-selected");
            }

            GridPane.setHalignment(dayButton, HPos.CENTER);
            GridPane.setValignment(dayButton, VPos.CENTER);
            GridPane.setMargin(dayButton, new Insets(5));

            dayButton.setUserData(date);
            dayButton.setOnAction(event -> {
                selectedDate = (LocalDate) dayButton.getUserData();
                displayShowtimesForDate(selectedDate);
                updateDayButtonStyles();
            });

            dateContainer.add(dayButton, col, row);
            dayButtons.add(dayButton);

            col++;
            if (col == 7) {
                col = 0;
                row++;
            }
        }

        if (selectedDate == null) {
            selectedDate = today;
            updateDayButtonStyles();
        }
        displayShowtimesForDate(selectedDate);
    }

    private void updateDayButtonStyles() {
        for (Button dayButton : dayButtons) {
            LocalDate date = (LocalDate) dayButton.getUserData();
            dayButton.getStyleClass().remove("day-button-selected");
            if (date.equals(selectedDate)) {
                dayButton.getStyleClass().add("day-button-selected");
            }
        }
    }

    private void displayShowtimesForDate(LocalDate date) {
        showtimeContainer.getChildren().clear();

        for (Showtime showtime : showtimes) {
            if (showtime.getDate().equals(date) && showtime.getId_movie().equals(movieID)) {
                Button showtimeButton = new Button(String.format("%s ( %d/%d )", showtime.getTime(), showtime.getAvailableSeats(), showtime.getTotalNumberSeats()));
                showtimeButton.getStyleClass().add("showtime-button");
                showtimeButton.setStyle("-fx-pref-height: 40px;");

                if (showtime.getAvailableSeats() > 0) {
                    showtimeButton.setOnAction(event -> {
                        goToSeatSelection(showtime.getId_lichchieu(), showtime.getTotalNumberSeats());
                    });
                } else {
                    showtimeButton.setDisable(true);
                    showtimeButton.setStyle("-fx-pref-width: 120px; -fx-pref-height: 40px; -fx-text-fill: gray;");
                }

                showtimeContainer.getChildren().add(showtimeButton);
            }
        }

        if (showtimeContainer.getChildren().isEmpty()) {
            Label noShowtimeLabel = new Label("Không có suất chiếu cho ngày này.");
            showtimeContainer.getChildren().add(noShowtimeLabel);
        }
    }

    private void goToSeatSelection(String id_lichchieu, int totalNumberSeats) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Cinema/UI/SeatSelection.fxml"));
            Parent root = loader.load();
            SelectSeats controller = loader.getController();

            String url = "jdbc:mysql://localhost/Cinema_DB";
            String username = "root";
            String password = "";
            String movieName = "";
            String date = "";
            String time = "";
            String timing = "";
            String movieId = "";
            int basePrice = 0;

            try (Connection conn = mysqlconnect.ConnectDb(url, username, password);
                 PreparedStatement pstmt = conn.prepareStatement("SELECT date, time, id_movie FROM showtimes WHERE id_lichchieu = ?")) {
                pstmt.setString(1, id_lichchieu);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    date = rs.getDate("date").toString();
                    time = rs.getTime("time").toString().substring(0, 5);
                    timing = time + ", " + formatDate(date);
                    movieId = rs.getString("id_movie");

                    PreparedStatement pstmtMovie = conn.prepareStatement("SELECT name, basePrice FROM movies WHERE id = ?");
                    pstmtMovie.setString(1, movieId);
                    ResultSet rsMovie = pstmtMovie.executeQuery();
                    if (rsMovie.next()) {
                        movieName = rsMovie.getString("name");
                        basePrice = rsMovie.getInt("basePrice");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            JSONUtility jsonUtil = new JSONUtility();
            jsonUtil.createMovieJson(id_lichchieu, movieName, timing, "", basePrice);
            controller.initializeSeatSelection(id_lichchieu, totalNumberSeats);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.show();

            Stage currentStage = (Stage) cancelButton.getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String formatDate(String date) {
        String[] parts = date.split("-");
        return parts[2] + "-" + parts[1] + "-" + parts[0];
    }

    private void loadShowtimesFromDatabase() {
        showtimes = new ArrayList<>();

        String url = "jdbc:mysql://localhost/Cinema_DB";
        String username = "root";
        String password = "";

        try (Connection conn = mysqlconnect.ConnectDb(url, username, password)) {
            PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM showtimes WHERE id_movie = ?");
            pstmt.setString(1, movieID);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String id_lichchieu = rs.getString("id_lichchieu");
                LocalDate date = rs.getDate("date").toLocalDate();
                String time = rs.getTime("time").toString().substring(0, 5);
                String id_movie = rs.getString("id_movie");
                int bookedSeatsCount = rs.getInt("bookedSeatsCount");
                int totalNumberSeats = rs.getInt("totalNumberSeats");

                showtimes.add(new Showtime(id_lichchieu, date, time, id_movie, bookedSeatsCount, totalNumberSeats));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi kết nối CSDL: " + e.getMessage());
            e.printStackTrace();
        }
    }
}