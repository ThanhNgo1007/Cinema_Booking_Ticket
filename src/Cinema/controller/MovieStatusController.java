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
import Cinema.database.Movie;
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
import javafx.stage.Stage;

public class MovieStatusController {
	private Stage stage;
	private Scene scene;
	private Parent root;

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

    // Thay đổi URL để kết nối MySQL
    private static final String DB_URL = "jdbc:mysql://localhost/Cinema_DB"; 
    private static final String DB_USER = "root"; // Thay bằng username của bạn
    private static final String DB_PASSWORD = ""; // Thay bằng mật khẩu của bạn
    private String movieTrailerUrl;

	public void handleBackBtnClicked(MouseEvent event) throws IOException {
		// Lấy cửa sổ hiện tại (cửa sổ MovieStatus)
	    Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
	    
	    // Lấy cửa sổ gốc (FilmUI) đã bị làm mờ
	    Stage parentStage = (Stage) currentStage.getOwner();
	    
	        parentStage.getScene().getRoot().setEffect(null);
	    
	    // Đóng cửa sổ hiện tại
	    currentStage.close();
	}

	public void handleBookTicketBtnClicked(ActionEvent event) throws IOException {
		root = FXMLLoader.load(getClass().getResource("/application/fxml/SeatsSelection.fxml"));
		stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
		double currentWidth = stage.getWidth();
		double currentHeight = stage.getHeight();
		scene = new Scene(root, currentWidth, currentHeight);

		stage.setMaximized(true);
		stage.setScene(scene);
		stage.show();
	}
	
	public void handleTrailerBtnClicked() {
		 if (movieTrailerUrl == null || movieTrailerUrl.isEmpty()) {
	            System.out.println("Không có URL trailer!");
	            return;
	        }
	        try {
	            Desktop.getDesktop().browse(new URI(movieTrailerUrl));
	        } catch (IOException | URISyntaxException e) {
	            e.printStackTrace();
	            System.out.println("Không thể mở trailer!");
	        }
		
	}

	public void setMovieData(String ID, String name, String gener, String duration, String releaseDate ) {
		try {
			
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
			movieActors.setText(selectedMovie.getMovieActor());
			director.setText(selectedMovie.getDirector());
			movieTrailerUrl = selectedMovie.getMovieTrailer(); // Lấy trailer từ DB
			moviePoster.setImage(selectedMovie.getMoviePoster());
			rating.setText(selectedMovie.getMovieRating());
			
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
//	       // Kết nối MySQL
            con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
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

}