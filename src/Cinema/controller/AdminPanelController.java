package Cinema.controller;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import Cinema.database.mysqlconnect;
import Cinema.util.Movie;
import Cinema.util.Showtime;
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
    @FXML private TableColumn<Movie, Void> col_release1; // Cột hiển thị biểu tượng lịch chiếu

    @FXML
    private VBox rootPane;

    private static final String URL = "jdbc:mysql://localhost/Cinema_DB";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private ObservableList<Movie> movieList = FXCollections.observableArrayList();
    private Map<Integer, Boolean> expandedRows = new HashMap<>(); // Không cần nữa nhưng giữ lại để tương thích
    private Map<Integer, Double> originalRowHeights = new HashMap<>(); // Không cần nữa nhưng giữ lại để tương thích

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        loadMoviesFromDatabase();

        movieTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                update_btn.setDisable(false);
                delete_btn.setDisable(false);
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
        col_status.setCellValueFactory(new PropertyValueFactory<>("status"));
        col_release.setCellValueFactory(new PropertyValueFactory<>("movieRealeseDate"));

        // Tùy chỉnh cột "Lịch chiếu" với biểu tượng
        setupShowtimesColumn();
        
        movieTable.setItems(movieList);
    }

    private void setupShowtimesColumn() {
        col_release1.setCellFactory(col -> new TableCell<>() {
            private final Button showtimesButton = new Button();
            private final ImageView imageView = new ImageView();

            {
                // Tải hình ảnh từ file
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
                    // Sử dụng ký tự mặc định nếu không tìm thấy file
                    showtimesButton.setText("📅");
                }
                showtimesButton.setStyle("-fx-background-color: transparent;");

                // Xử lý sự kiện khi nhấp vào biểu tượng
                showtimesButton.setOnAction(event -> {
                    Movie movie = getTableView().getItems().get(getIndex());
                    openShowtimeWindow(movie); // Mở cửa sổ mới
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
                movieList.add(movie);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

            // Lấy controller của trang cập nhật
            UpdateMovieController updateController = loader.getController();
            
            // Truyền dữ liệu phim vào trang cập nhật
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

        // Refresh danh sách phim
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
        // Tạo Stage mới
        Stage showtimeStage = new Stage();
        showtimeStage.setTitle("Lịch chiếu của " + movie.getMovieName());
        showtimeStage.initStyle(StageStyle.DECORATED); // Có thể thay đổi thành UNDECORATED nếu muốn

        // Tạo ScrollPane chứa nội dung
        ScrollPane scrollPane = createShowtimePane(movie);

        // Tạo Scene và đặt ScrollPane làm nội dung
        Scene scene = new Scene(scrollPane, 800, 400); // Kích thước cửa sổ: 800x400

        // Áp dụng CSS (nếu có)
        URL cssURL = getClass().getResource("/css/Showtime.css");
        if (cssURL != null) {
            scene.getStylesheets().add(cssURL.toExternalForm());
        } else {
            System.err.println("Không tìm thấy file CSS: /resource/css/Showtime.css");
        }

        // Hiển thị cửa sổ
        showtimeStage.setScene(scene);
        showtimeStage.show();
    }

    private ScrollPane createShowtimePane(Movie movie) {
        // Tạo TableView nhỏ để hiển thị lịch chiếu
        TableView<Showtime> showtimeTable = new TableView<>();
        showtimeTable.getStyleClass().add("showtime-table"); // Thêm class để áp dụng CSS

        // Cấu hình các cột cho TableView nhỏ
        TableColumn<Showtime, String> idCol = new TableColumn<>("ID Lịch chiếu");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(100);

        TableColumn<Showtime, String> movieIdCol = new TableColumn<>("ID Phim");
        movieIdCol.setCellValueFactory(new PropertyValueFactory<>("movieId"));
        movieIdCol.setPrefWidth(100);

        TableColumn<Showtime, String> dateCol = new TableColumn<>("Ngày chiếu");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("showDate"));
        dateCol.setPrefWidth(150);

        TableColumn<Showtime, String> timeCol = new TableColumn<>("Giờ chiếu");
        timeCol.setCellValueFactory(new PropertyValueFactory<>("showTime"));
        timeCol.setPrefWidth(100);

        TableColumn<Showtime, Integer> totalSeatsCol = new TableColumn<>("Tổng số ghế");
        totalSeatsCol.setCellValueFactory(new PropertyValueFactory<>("totalNumberSeats"));
        totalSeatsCol.setPrefWidth(100);

        TableColumn<Showtime, Integer> bookedSeatsCol = new TableColumn<>("Số ghế đã đặt");
        bookedSeatsCol.setCellValueFactory(new PropertyValueFactory<>("bookedSeatsCount"));
        bookedSeatsCol.setPrefWidth(100);
        
        TableColumn<Showtime, Integer> screenCol = new TableColumn<>("Phòng chiếu");
        screenCol.setCellValueFactory(new PropertyValueFactory<>("screen"));
        screenCol.setPrefWidth(100);

        showtimeTable.getColumns().addAll(idCol, movieIdCol, dateCol, timeCol, totalSeatsCol, bookedSeatsCol,screenCol);

        // Tải dữ liệu lịch chiếu từ bảng showtimes
        ObservableList<Showtime> showtimes = FXCollections.observableArrayList();
        String query = "SELECT id_lichchieu, id_movie, date, time, totalNumberSeats, bookedSeatsCount, screen FROM showtimes WHERE id_movie = ?";

        try (Connection conn = mysqlconnect.ConnectDb(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, movie.getMovieID());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String id = rs.getString("id_lichchieu");
                String movieId = rs.getString("id_movie");
                String showDate = rs.getString("date");
                String showTime = rs.getString("time");
                Integer totalNumberSeats = rs.getInt("totalNumberSeats");
                Integer bookedSeatsCount = rs.getInt("bookedSeatsCount");
                Integer screen = rs.getInt("screen");
                showtimes.add(new Showtime(id, movieId, showDate, showTime, totalNumberSeats, bookedSeatsCount,screen));
            }

            showtimeTable.setItems(showtimes);
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy dữ liệu lịch chiếu: " + e.getMessage());
        }

        // Nếu không có lịch chiếu, hiển thị thông báo
        if (showtimes.isEmpty()) {
            showtimeTable.setPlaceholder(new Label("Không có lịch chiếu cho phim này."));
        }

        // Tạo nút "Thêm lịch chiếu"
        Button addShowtimeButton = new Button("Thêm lịch chiếu");
        addShowtimeButton.getStyleClass().add("add-showtime-button"); // Thêm class để áp dụng CSS
        addShowtimeButton.setOnAction(event -> openAddShowtimePanel(movie));

        // Sử dụng HBox để đặt nút bên phải
        HBox buttonBox = new HBox(addShowtimeButton);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        buttonBox.setPadding(new javafx.geometry.Insets(5));
        buttonBox.setPrefHeight(40); // Chiều cao cố định cho HBox

        // Tạo VBox chứa TableView và nút
        VBox contentBox = new VBox(showtimeTable, buttonBox);
        contentBox.setSpacing(5);
        contentBox.setStyle("-fx-padding: 10; -fx-background-color: #f0f0f0;");

        // Tạo ScrollPane và đặt nội dung là contentBox
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(contentBox);
        scrollPane.setFitToWidth(true); // Fit với chiều rộng của cửa sổ
        scrollPane.setFitToHeight(true); // Fit với chiều cao của cửa sổ
        scrollPane.setStyle("-fx-background-color: #f0f0f0;"); // Đồng bộ màu nền với contentBox

        return scrollPane;
    }

    private void openAddShowtimePanel(Movie movie) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Cinema/UI/AddShowtime.fxml"));
            Parent root = loader.load();

            // Lấy controller của giao diện thêm lịch chiếu
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

    // Phương thức để làm mới danh sách phim sau khi thêm lịch chiếu
    public void refreshShowtimes() {
        // Làm mới danh sách phim
        loadMoviesFromDatabase();
    }
}