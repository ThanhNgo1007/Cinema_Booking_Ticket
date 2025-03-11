package Cinema.controller;

import Cinema.database.Movie;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MovieItemController {
	@FXML
    private Label setMovieGener;

    @FXML
    private Label setMovieName;

    @FXML
    private Label setMovieReleaseDate;

    @FXML
    private Label setMovieTime;
    
    @FXML
    private ImageView setMovieImg;
    
    @FXML
	private void click(MouseEvent e) throws Exception {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("MovieStatus.fxml"));
		Parent root = loader.load();
		MovieStatusController controller = loader.getController();

		// Pass the movie details to MovieStatusController
		 controller.setMovieData(setMovieName.getText(), setMovieGener.getText(), setMovieTime.getText(),
	                setMovieReleaseDate.getText() );


		 // Lấy stage hiện tại (cửa sổ cũ)
	        Stage currentStage = (Stage) ((Node) e.getSource()).getScene().getWindow();
	        
	        // Làm mờ cửa sổ cũ
	        currentStage.getScene().getRoot().setEffect(new GaussianBlur(10));

	        // Tạo cửa sổ mới
	        Stage newStage = new Stage();
	        Scene scene = new Scene(root);
	        newStage.setScene(scene);
	        newStage.initModality(Modality.APPLICATION_MODAL); // Chặn tương tác với cửa sổ cũ
	        newStage.show();
	}

//   set to display data to screen
	public void setData(Movie movie) {
		Image image = movie.getMoviePoster();
		setMovieImg.setImage(image);
		setMovieName.setText(movie.getMovieName());
		setMovieTime.setText(movie.getMovieTime());
		setMovieGener.setText(movie.getMovieGener());
		setMovieReleaseDate.setText(movie.getMovieRealeseDate());
	}
}
