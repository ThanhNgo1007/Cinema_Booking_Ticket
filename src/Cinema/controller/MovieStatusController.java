package Cinema.controller;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import Cinema.database.DBUtility;
import Cinema.database.mysqlconnect;
import Cinema.util.Movie;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MovieStatusController {
    @FXML
    private Text movieActors, movieDescription, director;
    @FXML
    private Text movieTitle, movieGener, movieReleaseDate, movieTime, rating;

    @FXML
    private Button bookTicketButtonClicked;
    
    @FXML
    private ImageView goBackButtonClicked;
    
    @FXML
    private Button trailerButton;

    @FXML
    private Pane movieImg;
    
    @FXML
    private ImageView moviePoster;
    
    @FXML
    private Text baseClassTicket;

    @FXML
    private Text secondClassTicket;

    @FXML
    private Text firstClassTicket;
    
    @FXML
    private WebView trailerField;

    private static final String DB_URL = "jdbc:mysql://localhost/Cinema_DB"; 
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    private String movieTrailerUrl;
    private String movieID;

    public void handleBackBtnClicked(MouseEvent event) throws IOException {
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Stage parentStage = (Stage) currentStage.getOwner();
        parentStage.getScene().getRoot().setEffect(null);
        currentStage.close();
    }

    public void handleBookTicketBtnClicked(MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Cinema/UI/ShowtimeSelection.fxml"));
        Parent root = loader.load();
        ShowtimeController controller = loader.getController();
        
        controller.setMovieId(movieID);

        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Stage newStage = new Stage();
        Scene scene = new Scene(root);
        newStage.setScene(scene);
        newStage.initStyle(StageStyle.UNDECORATED);
        newStage.show();
        newStage.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                newStage.close();
            }
        });
    }

    public void setMovieData(String ID, String name, String gener, String duration, String releaseDate) {
        try {
            this.movieID = ID;
            movieTitle.setText(name);
            movieGener.setText(gener);
            movieTime.setText(duration);
            movieReleaseDate.setText(releaseDate);

            setMovieDetails(ID);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    public void setMovieDetails(String movieID) {
        String searchQuery = "SELECT * FROM movies WHERE id = ?";
        List<Movie> searchResults = searchMoviesInDatabase(searchQuery, movieID);
        
        if (!searchResults.isEmpty()) {
            Movie selectedMovie = searchResults.get(0);

            movieDescription.setText(selectedMovie.getMovieDescription());
            movieActors.setText("Diễn viên: " + selectedMovie.getMovieActor());
            director.setText("Đạo diễn: " + selectedMovie.getDirector());
            movieTrailerUrl = selectedMovie.getMovieTrailer();
            moviePoster.setImage(selectedMovie.getMoviePoster());
            rating.setText("Rated: " + selectedMovie.getMovieRating());
            
         // Đảm bảo trạng thái ban đầu: hiển thị movieDescription, ẩn trailerField
            if (movieDescription != null) {
                movieDescription.setVisible(true);
                movieDescription.setManaged(true);
            }
            if (trailerField != null) {
                trailerField.setVisible(false);
                trailerField.setManaged(false);
            }
        } else {
            System.out.println("Movie not found!");
        }
    }

    public List<Movie> searchMoviesInDatabase(String searchQuery, String movieTitle) {
        List<Movie> movies = new ArrayList<>();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = mysqlconnect.ConnectDb(DB_URL, DB_USER, DB_PASSWORD);
            ps = con.prepareStatement(searchQuery);
            ps.setString(1, movieTitle);
            rs = ps.executeQuery();

            DBUtility.getMoviesData(rs, movies);
        } catch (Exception e) {
            System.out.println(e.toString());
        } finally {
            try {
                if (rs != null)
                    rs.close();
                if (ps != null)
                    ps.close();
                if (con != null)
                    con.close();
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
        return movies;
    }
    
    // Phương thức xử lý khi nhấn nút "TRAILER"
    @FXML
    public void handleTrailerBtnClicked() {
        if (movieID != null && !movieID.isEmpty()) {
        	
                    movieDescription.setVisible(false);
                    movieDescription.setManaged(false);
                    trailerField.setVisible(true);
                    trailerField.setManaged(true);
                trailerField.getEngine().loadContent("<h3>Đang tải trailer...</h3>");
                showMovieTrailer(movieID);
            // Làm mới WebView trước khi load trailer mới
            trailerField.getEngine().loadContent("<h3>Đang tải trailer...</h3>");
            showMovieTrailer(movieID);
        } else {
            trailerField.getEngine().loadContent("<h3 style='color: red;'>Vui lòng chọn phim trước</h3>");
        }
    }
    
    public void handleDescriptionBtnClicked() {
    	movieDescription.setVisible(true);
        movieDescription.setManaged(true);
        trailerField.setVisible(false);
        trailerField.setManaged(false);
    }
    // Phương thức hiển thị trailer trong WebView
    public void showMovieTrailer(String movieID) {
        String searchQuery = "SELECT trailer FROM movies WHERE id = ?";
        String trailerUrl = null;
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = mysqlconnect.ConnectDb(DB_URL, DB_USER, DB_PASSWORD);
            if (con == null) {
                trailerField.getEngine().loadContent("<h3 style='color: red;'>Không thể kết nối đến cơ sở dữ liệu</h3>");
                return;
            }

            ps = con.prepareStatement(searchQuery);
            ps.setString(1, movieID);
            rs = ps.executeQuery();

            if (rs.next()) {
                trailerUrl = rs.getString("trailer");
            }

            if (trailerUrl != null && !trailerUrl.trim().isEmpty()) {
                // Xử lý URL YouTube
                if (trailerUrl.contains("youtube.com") || trailerUrl.contains("youtu.be")) {
                    String videoId = null;
                    if (trailerUrl.contains("watch?v=")) {
                        videoId = trailerUrl.split("v=")[1].split("&")[0];
                    } else if (trailerUrl.contains("youtu.be")) {
                        videoId = trailerUrl.split("youtu.be/")[1].split("[?&]")[0];
                    }
                    if (videoId != null) {
                        trailerUrl = "https://www.youtube.com/embed/" + videoId ;
                    }
                }
                
                // Nếu đã là URL embed hoặc không xác định được nền tảng, dùng trực tiếp URL
                trailerField.getEngine().load(trailerUrl);
            } else {
                trailerField.getEngine().loadContent("<h3 style='color: red;'>Trailer không khả dụng</h3>");
            }
            
            
        } catch (Exception e) {
            System.out.println("Error loading trailer: " + e.toString());
            trailerField.getEngine().loadContent("<h3 style='color: red;'>Lỗi khi tải trailer: " + e.getMessage() + "</h3>");
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
    }
}