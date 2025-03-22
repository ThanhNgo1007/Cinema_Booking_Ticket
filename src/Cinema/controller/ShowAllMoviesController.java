package Cinema.controller;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import Cinema.database.DBUtility;
import Cinema.database.Movie;
import Cinema.database.mysqlconnect;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;

public class ShowAllMoviesController implements Initializable {

    @FXML
    private HBox HBoxpane;

    @FXML
    private VBox anchorPane;

    @FXML
    private TextField getMovieSearchInput;

    @FXML
    private GridPane grid;

    @FXML
    private ImageView movieSearchBtn;

    @FXML
    private Button newReleaseBtn, searchBtn;

    @FXML
    private ScrollPane scrollBar;

    @FXML
    private Button trendingMoviesBtn;

    @FXML
    private Button upcomingsMoviesBtn;

    private static final String DB_URL = "jdbc:mysql://localhost/Cinema_DB";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";

    private List<Movie> movieList = new ArrayList<>();

    // ⭐ Fetch data from DB and display in movie cards
    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        List<Movie> moviesFromDatabase = readMoviesDate();
        movieList.addAll(moviesFromDatabase);
        refreshGrid(movieList);
        getMovieSearchInput.setFocusTraversable(false);
    }

    public void handleClickIcon() {
        getMovieSearchInput.requestFocus();
    }

    // ⭐ Connect to DB and fetch movie list
    @FXML
    List<Movie> readMoviesDate() {
        List<Movie> movieNames = new ArrayList<>();
        try (Connection con = mysqlconnect.ConnectDb(DB_URL, USERNAME, PASSWORD);
             PreparedStatement ps = con.prepareStatement("SELECT * FROM movies WHERE status = 1");
             ResultSet rs = ps.executeQuery()) {

            DBUtility.getMoviesData(rs, movieNames);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return movieNames;
    }

    // ⭐ Search movies
    @FXML
    void searchMovies(KeyEvent event) {
        String searchQuery = getMovieSearchInput.getText().trim();
        List<Movie> searchResults = searchMoviesInDatabase(searchQuery);
        refreshGrid(searchResults);
    }

    List<Movie> searchMoviesInDatabase(String searchQuery) {
        List<Movie> movieNames = new ArrayList<>();
        try (Connection con = mysqlconnect.ConnectDb(DB_URL, USERNAME, PASSWORD);
             PreparedStatement ps = con.prepareStatement("SELECT * FROM movies WHERE (name LIKE ? OR gener LIKE ?) AND status = 1")) {

            ps.setString(1, "%" + searchQuery + "%");
            ps.setString(2, "%" + searchQuery + "%");
            try (ResultSet rs = ps.executeQuery()) {
                DBUtility.getMoviesData(rs, movieNames);
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return movieNames;
    }

    void refreshGrid(List<Movie> movieData) {
        grid.getChildren().clear();  // Xóa tất cả item cũ
        grid.getRowConstraints().clear(); // Xóa RowConstraints cũ để tránh xung đột

        int col = 0, row = 0;
        try {
            for (Movie movie : movieData) {
                FXMLLoader fxmlloder = new FXMLLoader();
                fxmlloder.setLocation(getClass().getResource("/Cinema/UI/MovieItemUI.fxml"));
                VBox movieCard = fxmlloder.load();

                MovieItemController cardController = fxmlloder.getController();
                cardController.setData(movie);

                // Thêm movie card vào GridPane
                grid.add(movieCard, col, row);
                GridPane.setMargin(movieCard, new Insets(20)); // Đặt margin 20px cho mỗi card

                col++;
                if (col == 4) { // Khi đủ 4 cột, xuống hàng mới
                    col = 0;
                    row++;
                }
            }

            // Không cần thêm RowConstraints thủ công, để GridPane tự điều chỉnh
            // Đảm bảo ScrollPane có thể cuộn bằng cách đặt chiều cao tối thiểu
            grid.setMinHeight(row * 350); // Ước lượng chiều cao dựa trên số hàng (350px mỗi hàng)

        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    // ⭐ Refresh Grid Content
    @FXML
    void refreshContent(MouseEvent event) {
        grid.getChildren().clear();
        initialize(null, null);
    }
}
