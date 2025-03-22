package Cinema.controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;

import Cinema.database.Movie;
import Cinema.database.mysqlconnect;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class UpdateMovieController implements Initializable {
    @FXML
    private TextArea actorsField;

    @FXML
    private Button applyButton;

    @FXML
    private TextField dateField;

    @FXML
    private TextArea descriptionField;

    @FXML
    private TextField directorField;

    @FXML
    private TextField durationField;

    @FXML
    private TextField idField;

    @FXML
    private ImageView imagePoster;

    @FXML
    private Button inputImageButton;

    @FXML
    private TextField priceField;

    @FXML
    private ComboBox<String> rateComboBox;

    @FXML
    private TextField titleField;

    @FXML
    private TextField trailerField;

    @FXML
    private RadioButton valueFalse;

    @FXML
    private RadioButton valueTrue;

    private Movie selectedMovie;
    private byte[] selectedImageBytes;

    
    private static final String URL = "jdbc:mysql://localhost/Cinema_DB";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        // Khởi tạo giá trị cho ComboBox (rating)
        rateComboBox.getItems().addAll("T13", "T16", "T18", "K", "P");

        // Gán sự kiện cho nút Apply
        applyButton.setOnAction(event -> handleApplyUpdate());
        ToggleGroup group = new ToggleGroup();
        valueTrue.setToggleGroup(group);
        valueFalse.setToggleGroup(group);
    }

    public void setMovieData(Movie movie) {
        this.selectedMovie = movie;

        idField.setText(movie.getMovieID());
        titleField.setText(movie.getMovieName());
        directorField.setText(movie.getDirector());
        actorsField.setText(movie.getMovieActor());
        dateField.setText(movie.getMovieRealeseDate());
        descriptionField.setText(movie.getMovieDescription());
        durationField.setText(movie.getMovieTime());
        priceField.setText(movie.getPrice());
        rateComboBox.setValue(movie.getMovieRating());
        trailerField.setText(movie.getMovieTrailer());


        // Xử lý trạng thái phim
        if (movie.getStatus().equals(1)) {
            valueTrue.setSelected(true);
        } else {
            valueFalse.setSelected(true);
        }

        // Load ảnh poster nếu có
        if (movie.getMoviePoster() != null) {
            Image image = movie.getMoviePoster();
            imagePoster.setImage(image);
        }
    }

    @FXML

    private void handleApplyUpdate() {
        if (selectedMovie == null) {
            System.out.println("No movie selected.");
            return;
        }

        try (Connection conn = mysqlconnect.ConnectDb(URL, USER, PASSWORD)) {
            if (selectedImageBytes == null) {
                String getImageQuery = "SELECT posterImage FROM movies WHERE id = ?";
                PreparedStatement getImageStmt = conn.prepareStatement(getImageQuery);
                getImageStmt.setString(1, selectedMovie.getMovieID());
                ResultSet rs = getImageStmt.executeQuery();
                if (rs.next()) {
                    selectedImageBytes = rs.getBytes("posterImage");
                }
            }

            String updateQuery = "UPDATE movies SET name = ?, director = ?, actorList = ?, releaseDate = ?, updateDate = NOW(), " +
                    "status = ?, ratings = ?, duration = ?, basePrice = ?, description = ?, trailer = ?, posterImage = ? WHERE id = ?";

            PreparedStatement stmt = conn.prepareStatement(updateQuery);
            stmt.setString(1, titleField.getText());
            stmt.setString(2, directorField.getText());
            stmt.setString(3, actorsField.getText());
            stmt.setString(4, dateField.getText());
            stmt.setBoolean(5, valueTrue.isSelected());
            stmt.setString(6, rateComboBox.getValue());
            stmt.setString(7, durationField.getText());
            stmt.setString(8, priceField.getText());
            stmt.setString(9, descriptionField.getText());
            stmt.setString(10, trailerField.getText());
            stmt.setBytes(11, selectedImageBytes);
            stmt.setString(12, selectedMovie.getMovieID());

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Movie updated successfully!");

                // Cập nhật TableView trong AdminPanelController
                if (adminPanelController != null) {
                    adminPanelController.loadMoviesFromDatabase();
                } else {
                    System.out.println("AdminPanelController is null!");
                }

                // Đóng cửa sổ sau khi cập nhật thành công
                Stage stage = (Stage) applyButton.getScene().getWindow();
                stage.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void handleCancelButton(MouseEvent event) {
    	Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        
        stage.close();
    }

    
    @FXML
    private void handleSelectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File file = fileChooser.showOpenDialog((Stage) inputImageButton.getScene().getWindow());

        if (file != null) {
            try {
                // Đọc ảnh thành byte array
                FileInputStream fis = new FileInputStream(file);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    bos.write(buffer, 0, bytesRead);
                }
                fis.close();
                byte[] selectedImageBytes = bos.toByteArray();

                // Hiển thị ảnh đã chọn
                Image image = new Image(file.toURI().toString());
                imagePoster.setImage(image);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private AdminPanelController adminPanelController;

    public void setAdminPanelController(AdminPanelController controller) {
    	this.adminPanelController = controller;
    }

}
