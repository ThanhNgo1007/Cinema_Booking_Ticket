package Cinema.controller;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import Cinema.database.mysqlconnect;
import Cinema.util.Movie;
import Cinema.util.Showtime;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class AdminPanelController implements Initializable {

    @FXML private Button add_btn;
    @FXML private Button delete_btn;
    @FXML private Button update_btn;
    @FXML private Button resetButton;
    
    @FXML private TableView<Movie> movieTable;
    @FXML private TableColumn<Movie, String> col_id;
    @FXML private TableColumn<Movie, String> col_name;
    @FXML private TableColumn<Movie, String> col_gener;
    @FXML private TableColumn<Movie, String> col_director;
    @FXML private TableColumn<Movie, String> col_duration;
    @FXML private TableColumn<Movie, String> col_date;
    @FXML private TableColumn<Movie, String> col_updatedate;
    @FXML private TableColumn<Movie, Integer> col_status;
    @FXML private TableColumn<Movie, String> col_release;
    @FXML private TableColumn<Movie, Void> col_release1;

    @FXML private ComboBox<String> filterComboBox;

    @FXML private VBox rootPane;

    private static final String URL = "jdbc:mysql://localhost/Cinema_DB";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private ObservableList<Movie> movieList = FXCollections.observableArrayList();
    private ObservableList<Movie> fullMovieList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        loadMoviesFromDatabase();

        // Thiết lập danh sách cho ComboBox
        filterComboBox.getItems().addAll(
            "Không lọc",
            "Status: Đang chiếu",
            "Status: Ngừng chiếu",
            "Ngày ra mắt: Gần đây nhất",
            "Ngày ra mắt: Lâu nhất",
            "Ngày thêm: Gần đây nhất",
            "Ngày thêm: Lâu nhất",
            "Ngày cập nhật: Gần đây nhất",
            "Ngày cập nhật: Lâu nhất"
        );
        filterComboBox.setValue("Không lọc");

        // Thêm listener để tự động lọc khi chọn item trong ComboBox
        filterComboBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                applyFilters(newValue);
            }
        });

        movieTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                update_btn.setDisable(false);
                delete_btn.setDisable(newSelection.getStatus() == 0);
                add_btn.setDisable(true);
            } else {
                update_btn.setDisable(true);
                delete_btn.setDisable(true);
                add_btn.setDisable(false);
            }
        });

        rootPane.setOnMouseClicked(event -> {
            if (!movieTable.getBoundsInParent().contains(event.getX(), event.getY())) {
                movieTable.getSelectionModel().clearSelection();
            }
        });
    }

    private void setupTable() {
        col_id.setCellValueFactory(new PropertyValueFactory<>("movieID"));
        col_name.setCellValueFactory(new PropertyValueFactory<>("movieName"));
        col_gener.setCellValueFactory(new PropertyValueFactory<>("movieGener"));
        col_director.setCellValueFactory(new PropertyValueFactory<>("director"));
        col_duration.setCellValueFactory(new PropertyValueFactory<>("movieTime"));
        col_date.setCellValueFactory(new PropertyValueFactory<>("createDate"));
        col_updatedate.setCellValueFactory(new PropertyValueFactory<>("updateDate"));
        col_release.setCellValueFactory(new PropertyValueFactory<>("movieRealeseDate"));

        col_status.setCellValueFactory(new PropertyValueFactory<>("status"));
        col_status.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                } else {
                    setText(status == 1 ? "Đang chiếu" : "Ngừng chiếu");
                }
            }
        });

        setupShowtimesColumn();
        
        movieTable.setItems(movieList);
    }

    private void setupShowtimesColumn() {
        col_release1.setCellFactory(col -> new TableCell<>() {
            private final Button showtimesButton = new Button();
            private final ImageView imageView = new ImageView();

            {
                Image icon = null;
                try {
                    InputStream inputStream = getClass().getResourceAsStream("/Cinema/image/icons8-calendar-24.png");
                    if (inputStream != null) {
                        icon = new Image(inputStream);
                    } else {
                        System.err.println("Không tìm thấy file biểu tượng: /Cinema/image/icons8-calendar-24.png");
                    }
                } catch (Exception e) {
                    System.err.println("Không thể tải biểu tượng lịch chiếu: " + e.getMessage());
                }

                if (icon != null) {
                    imageView.setImage(icon);
                    imageView.setFitWidth(16);
                    imageView.setFitHeight(16);
                    showtimesButton.setGraphic(imageView);
                } else {
                    showtimesButton.setText("📅");
                }
                showtimesButton.setStyle("-fx-background-color: transparent;");

                showtimesButton.setOnAction(event -> {
                    Movie movie = getTableView().getItems().get(getIndex());
                    openShowtimeWindow(movie);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(showtimesButton);
                }
            }
        });
    }

    public void loadMoviesFromDatabase() {
        fullMovieList.clear();
        movieList.clear();
        String query = "SELECT * FROM movies";

        try (Connection conn = mysqlconnect.ConnectDb(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String getMovieName = rs.getString("name");
                String getMovieID = rs.getString("id");
                String getMovieDescription = rs.getString("description");
                String getMovieRating = rs.getString("ratings");
                String getMovieGener = rs.getString("gener");
                InputStream getMoviePoster = rs.getBinaryStream("posterImage");
                String getActorsList = rs.getString("actorList");
                String getMovieRealeseDate = rs.getString("releaseDate");
                String getDirector = rs.getString("director");
                String getMovieDuration = rs.getString("duration");
                String getMovieTrailer = rs.getString("trailer");
                String getCreateDate = rs.getString("createDate");
                String getUpdateDate = rs.getString("updateDate");
                Integer status = rs.getInt("status");
                String getMoviePrice = rs.getString("basePrice");

                Movie movie = new Movie();
                movie.setMovieName(getMovieName);
                movie.setMovieDescription(getMovieDescription);
                movie.setMovieRating(getMovieRating);
                movie.setMovieGener(getMovieGener);
                movie.setMovieRealeseDate(getMovieRealeseDate);
                movie.setMoviePosterFromBlob(getMoviePoster);
                movie.setMovieActor(getActorsList);
                movie.setMovieID(getMovieID);
                movie.setDirector(getDirector);
                movie.setMovieTime(getMovieDuration);
                movie.setMovieTrailer(getMovieTrailer);
                movie.setCreateDate(getCreateDate);
                movie.setUpdateDate(getUpdateDate);
                movie.setStatus(status);
                movie.setPrice(getMoviePrice);
                fullMovieList.add(movie);
            }
            movieList.addAll(fullMovieList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Phương thức lọc dựa trên giá trị ComboBox
    private void applyFilters(String selectedFilter) {
        movieList.clear();

        switch (selectedFilter) {
            case "Không lọc":
                movieList.addAll(fullMovieList);
                break;
            case "Status: Đang chiếu":
                movieList.addAll(fullMovieList.filtered(movie -> movie.getStatus() == 1));
                break;
            case "Status: Ngừng chiếu":
                movieList.addAll(fullMovieList.filtered(movie -> movie.getStatus() == 0));
                break;
            case "Ngày ra mắt: Gần đây nhất":
                movieList.addAll(fullMovieList);
                movieList.sort(Comparator.comparing(Movie::getMovieRealeseDate, Comparator.nullsLast(Comparator.reverseOrder())));
                break;
            case "Ngày ra mắt: Lâu nhất":
                movieList.addAll(fullMovieList);
                movieList.sort(Comparator.comparing(Movie::getMovieRealeseDate, Comparator.nullsLast(Comparator.naturalOrder())));
                break;
            case "Ngày thêm: Gần đây nhất":
                movieList.addAll(fullMovieList);
                movieList.sort(Comparator.comparing(Movie::getCreateDate, Comparator.nullsLast(Comparator.reverseOrder())));
                break;
            case "Ngày thêm: Lâu nhất":
                movieList.addAll(fullMovieList);
                movieList.sort(Comparator.comparing(Movie::getCreateDate, Comparator.nullsLast(Comparator.naturalOrder())));
                break;
            case "Ngày cập nhật: Gần đây nhất":
                movieList.addAll(fullMovieList);
                movieList.sort(Comparator.comparing(Movie::getUpdateDate, Comparator.nullsLast(Comparator.reverseOrder())));
                break;
            case "Ngày cập nhật: Lâu nhất":
                movieList.addAll(fullMovieList);
                movieList.sort(Comparator.comparing(Movie::getUpdateDate, Comparator.nullsLast(Comparator.naturalOrder())));
                break;
            default:
                movieList.addAll(fullMovieList);
                break;
        }
    }

    @FXML
    public void resetFilters(MouseEvent event) {
        filterComboBox.setValue("Không lọc"); // Đặt lại ComboBox về "Không lọc"
        movieList.clear();
        movieList.addAll(fullMovieList); // Đặt lại danh sách phim về trạng thái gốc
    }

    @FXML
    public void openAddPanel(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Cinema/UI/AddMovie.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Admin Panel");
            stage.setScene(new Scene(root));
            stage.initStyle(StageStyle.UNDECORATED); 
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public AdminPanelController getAdminPanelController() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Cinema/UI/AdminPanel.fxml"));
        try {
            loader.load();
            return loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    @FXML
    public void handleUpdateMovie() {
        Movie selectedMovie = movieTable.getSelectionModel().getSelectedItem();
        if (selectedMovie == null) {
            System.out.println("Please select a movie to update.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Cinema/UI/UpdateMovie.fxml"));
            Parent root = loader.load();

            UpdateMovieController updateController = loader.getController();
            updateController.setMovieData(selectedMovie);
            updateController.setAdminPanelController(this);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Update Movie");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    public void handleDeleteMovie() {
        Movie selectedMovie = movieTable.getSelectionModel().getSelectedItem();
        if (selectedMovie == null) {
            System.out.println("Please select a movie to delete.");
            return;
        }

        int currentStatus = selectedMovie.getStatus();

        if (currentStatus == 1) {
            updateMovieStatus(selectedMovie.getMovieID(), 0);
            System.out.println("Movie status changed to 0 (deleted).");
        } else {
            System.out.println("Movie status is already 0. No action taken.");
        }

        loadMoviesFromDatabase();
    }

    private void updateMovieStatus(String movieId, int newStatus) {
        String sql = "UPDATE movies SET status = ? WHERE id = ?";

        try (Connection con = mysqlconnect.ConnectDb(URL, USER, PASSWORD);
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, newStatus);
            ps.setString(2, movieId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void openShowtimeWindow(Movie movie) {
        Stage showtimeStage = new Stage();
        showtimeStage.setTitle("Lịch chiếu của " + movie.getMovieName());
        showtimeStage.initStyle(StageStyle.DECORATED);

        ScrollPane scrollPane = createShowtimePane(movie);

        Scene scene = new Scene(scrollPane, 800, 400);
        URL cssURL = getClass().getResource("/css/Showtime.css");
        if (cssURL != null) {
            scene.getStylesheets().add(cssURL.toExternalForm());
        } else {
            System.err.println("Không tìm thấy file CSS: /resource/css/Showtime.css");
        }

        showtimeStage.setScene(scene);
        showtimeStage.show();
    }

    private ScrollPane createShowtimePane(Movie movie) {
        TableView<Showtime> showtimeTable = new TableView<>();
        showtimeTable.getStyleClass().add("showtime-table");

        TableColumn<Showtime, String> idCol = new TableColumn<>("ID Lịch chiếu");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id_lichchieu"));
        idCol.setPrefWidth(100);

        TableColumn<Showtime, String> movieIdCol = new TableColumn<>("ID Phim");
        movieIdCol.setCellValueFactory(new PropertyValueFactory<>("id_movie"));
        movieIdCol.setPrefWidth(100);

        TableColumn<Showtime, String> dateCol = new TableColumn<>("Ngày chiếu");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date")); // Sử dụng trực tiếp date (String)
        dateCol.setPrefWidth(150);

        TableColumn<Showtime, String> timeCol = new TableColumn<>("Giờ chiếu");
        timeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        timeCol.setPrefWidth(100);

        TableColumn<Showtime, String> endTimeCol = new TableColumn<>("Giờ kết thúc");
        endTimeCol.setCellValueFactory(new PropertyValueFactory<>("end_time"));
        endTimeCol.setPrefWidth(100);

        TableColumn<Showtime, Integer> totalSeatsCol = new TableColumn<>("Tổng số ghế");
        totalSeatsCol.setCellValueFactory(new PropertyValueFactory<>("totalNumberSeats"));
        totalSeatsCol.setPrefWidth(100);
        
        TableColumn<Showtime, Integer> screenCol = new TableColumn<>("Phòng chiếu");
        screenCol.setCellValueFactory(new PropertyValueFactory<>("screen"));
        screenCol.setPrefWidth(100);

        showtimeTable.getColumns().addAll(idCol, movieIdCol, dateCol, timeCol, endTimeCol, totalSeatsCol, screenCol);

        ObservableList<Showtime> showtimes = FXCollections.observableArrayList();
        String query = "SELECT id_lichchieu, id_movie, date, time, end_time, totalNumberSeats, bookedSeatsCount, screen FROM showtimes WHERE id_movie = ?";

        try (Connection conn = mysqlconnect.ConnectDb(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, movie.getMovieID());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String id = rs.getString("id_lichchieu");
                String movieId = rs.getString("id_movie");
                String showDate = rs.getDate("date").toString(); // Chuyển đổi sang LocalDate
                String showTime = rs.getTime("time").toString().substring(0, 5);
                String end_time = rs.getTime("end_time").toString().substring(0, 5);
                Integer totalNumberSeats = rs.getInt("totalNumberSeats");
                Integer bookedSeatsCount = rs.getInt("bookedSeatsCount");
                Integer screen = rs.getInt("screen");
                showtimes.add(new Showtime(id, showDate, showTime, movieId, bookedSeatsCount, totalNumberSeats, screen, end_time));
            }

            showtimeTable.setItems(showtimes);
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy dữ liệu lịch chiếu: " + e.getMessage());
        }

        if (showtimes.isEmpty()) {
            showtimeTable.setPlaceholder(new Label("Không có lịch chiếu cho phim này."));
        }

        Button addShowtimeButton = new Button("Thêm lịch chiếu");
        addShowtimeButton.getStyleClass().add("add-showtime-button");
        addShowtimeButton.setOnAction(event -> openAddShowtimePanel(movie));

        HBox buttonBox = new HBox(addShowtimeButton);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        buttonBox.setPadding(new javafx.geometry.Insets(5));
        buttonBox.setPrefHeight(40);

        VBox contentBox = new VBox(showtimeTable, buttonBox);
        contentBox.setSpacing(5);
        contentBox.setStyle("-fx-padding: 10; -fx-background-color: #f0f0f0;");

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(contentBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: #f0f0f0;");

        return scrollPane;
    }

    private void openAddShowtimePanel(Movie movie) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Cinema/UI/AddShowtime.fxml"));
            Parent root = loader.load();

            AddShowtimeController addShowtimeController = loader.getController();
            addShowtimeController.setMovie(movie);
            addShowtimeController.setAdminPanelController(this);

            Stage stage = new Stage();
            stage.setTitle("Thêm lịch chiếu");
            stage.setScene(new Scene(root));
            stage.initStyle(StageStyle.UNDECORATED);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void refreshShowtimes() {
        loadMoviesFromDatabase();
    }
}