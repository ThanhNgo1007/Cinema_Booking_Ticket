package Cinema.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import Cinema.database.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
	
public class AddMovieController {
	
	    @FXML private TextField movieIdField, titleField, directorField, trailerField, dateRelease, basePrice, durationField ;
	    @FXML private TextArea actorField, genreField, descriptionField;
	    @FXML private ComboBox<String> ageRatingComboBox;
	    @FXML private Button uploadImageButton, addMovieButton;
	    @FXML private Label titleLabel, dateLabel, timeLabel, directorLabel, actorLabel, genreLabel, ratedLabel, descriptionLabel;
	    @FXML private ImageView movieImageView;
	    private byte[] imageBytes; // Lưu dữ liệu ảnh dưới dạng byte[]

		    
	    private String imagePath;  // Lưu đường dẫn hình ảnh
	    
	    private static final String DB_URL = "jdbc:mysql://localhost:3306/cinema_db";
	    private static final String DB_USER = "root";
	    private static final String DB_PASSWORD = "";

	    @FXML
	    private void initialize() {
	        // Lắng nghe sự thay đổi trong các trường nhập liệu để cập nhật label song song
	        titleField.textProperty().addListener((obs, oldVal, newVal) -> titleLabel.setText("Title: " + newVal));
	        dateRelease.textProperty().addListener((obs, oldVal, newVal) -> dateLabel.setText("Date: " + newVal));
	        durationField.textProperty().addListener((obs, oldVal, newVal) -> timeLabel.setText("Time: " + newVal));
	        directorField.textProperty().addListener((obs, oldVal, newVal) -> directorLabel.setText("Director: " + newVal));
	        actorField.textProperty().addListener((obs, oldVal, newVal) -> actorLabel.setText("Actor/Actress: " + newVal));
	        genreField.textProperty().addListener((obs, oldVal, newVal) -> genreLabel.setText("Genre: " + newVal));
	        ageRatingComboBox.valueProperty().addListener((obs, oldVal, newVal) -> ratedLabel.setText("Rated: " + newVal));
	        descriptionField.textProperty().addListener((obs, oldVal, newVal) -> descriptionLabel.setText(newVal));
	        
	        ObservableList<String> ageRatings = FXCollections.observableArrayList(
	                "T13",
	                "T16",
	                "T18",
	                "P",
	                "K"
	            );
	            
	            ageRatingComboBox.setItems(ageRatings);

	            // Chọn mặc định (tuỳ chọn)
	            ageRatingComboBox.setValue("Select Rate");
	        }
	
	    @FXML
	    public void handleUploadImage() {
	        FileChooser fileChooser = new FileChooser();
	        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
	        File file = fileChooser.showOpenDialog(null);

	        if (file != null) {
	            try {
	                FileInputStream fis = new FileInputStream(file);
	                imageBytes = fis.readAllBytes(); // Chuyển ảnh thành byte[]
	                fis.close();

	                // Hiển thị ảnh đã chọn lên ImageView
	                Image image = new Image(file.toURI().toString());
	                movieImageView.setImage(image);
	                uploadImageButton.setText("Image Selected");
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
	    }

	
	    @FXML
	    private void handleAddMovie() {
	        String movieId = movieIdField.getText();
	        String title = titleField.getText();
	        String director = directorField.getText();
	        String trailer = trailerField.getText();
	        String actors = actorField.getText();
	        String genre = genreField.getText();
	        String description = descriptionField.getText();
	        String duration = durationField.getText();
	        String releaseDate = dateRelease.getText();
	        String price = basePrice.getText();
	        String trailerPath = trailerField.getText();
	        String ageRating = (ageRatingComboBox.getValue() != null) ? ageRatingComboBox.getValue() : "";

	        if (movieId.isEmpty() || title.isEmpty() || director.isEmpty() || releaseDate == null || imageBytes == null) {
	            showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all required fields and select an image!");
	            return;
	        }

	        try (Connection conn = mysqlconnect.ConnectDb(DB_URL, DB_USER, DB_PASSWORD)) {
	            String sql = "INSERT INTO movies (id, name, gener, actorList, director, description, duration, ratings, releaseDate, basePrice, posterImage, trailer) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	            PreparedStatement stmt = conn.prepareStatement(sql);
	            stmt.setString(1, movieId);
	            stmt.setString(2, title);
	            stmt.setString(3, genre);
	            stmt.setString(4, actors);
	            stmt.setString(5, director);
	            stmt.setString(6, description);
	            stmt.setString(7, duration);
	            stmt.setString(8, ageRating);
	            stmt.setString(9, releaseDate);
	            stmt.setString(10, price);
	            stmt.setBytes(11, imageBytes); // Lưu ảnh dưới dạng BLOB
	            stmt.setString(12, trailerPath);

	            int rowsInserted = stmt.executeUpdate();
	            if (rowsInserted > 0) {
	                showAlert(Alert.AlertType.INFORMATION, "Success", "Movie added successfully!");
	                clearFields();
	            }
	            if (adminPanelController != null) {
			        adminPanelController.loadMoviesFromDatabase();
			    }
	            
	        } catch (SQLException e) {
	            e.printStackTrace();
	            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to add movie.");
	        }
	    }
	    
	    public void handleBackBtnClicked(MouseEvent event) throws IOException {
			// Lấy cửa sổ hiện tại (cửa sổ MovieStatus)
		    Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
		    
		    // Đóng cửa sổ hiện tại
		    currentStage.close();
		}
	
	    private void clearFields() {
	        movieIdField.clear();
	        titleField.clear();
	        directorField.clear();
	        trailerField.clear();
	        actorField.clear();
	        genreField.clear();
	        descriptionField.clear();
	        durationField.clear();
	        dateRelease.clear();
	        ageRatingComboBox.setValue(null);
	        imagePath = null;
	    }
	
	    private void showAlert(Alert.AlertType alertType, String title, String message) {
	        Alert alert = new Alert(alertType);
	        alert.setTitle(title);
	        alert.setHeaderText(null);
	        alert.setContentText(message);
	        alert.showAndWait();
	    }
	    
	    private AdminPanelController adminPanelController;

	    public void setAdminPanelController(AdminPanelController controller) {
	    	this.adminPanelController = controller;
	    }
	      
}
