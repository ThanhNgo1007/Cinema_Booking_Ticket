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
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MovieItemController {
	@FXML
    private Label setMovieGener;

    @FXML
    private Text setMovieName;

    @FXML
    private Label setMovieReleaseDate;

    @FXML
    private Label setMovieTime;
    
    @FXML
    private ImageView setMovieImg;
    
    private String movieID;
    
    @FXML
    private void click(MouseEvent e) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Cinema/UI/MovieStatus.fxml"));
        Parent root = loader.load();
        MovieStatusController controller = loader.getController();

        // Truyền dữ liệu phim
        controller.setMovieData(movieID, setMovieName.getText(), setMovieGener.getText(), setMovieTime.getText(), setMovieReleaseDate.getText());

        // Lấy stage hiện tại (cửa sổ gốc)
        Stage currentStage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        
        // Làm mờ cửa sổ gốc
        currentStage.getScene().getRoot().setEffect(new GaussianBlur(10));

        // Tạo cửa sổ mới
        Stage newStage = new Stage();
        Scene scene = new Scene(root);
        newStage.setScene(scene);
        newStage.initModality(Modality.APPLICATION_MODAL); // Chặn tương tác với cửa sổ cũ
        newStage.initStyle(StageStyle.UNDECORATED); // Ẩn thanh tiêu đề
        newStage.initOwner(currentStage); // Đặt cửa sổ cha
        newStage.show();
    }


//   set to display data to screen
	public void setData(Movie movie) {
		movieID = movie.getMovieID();
		Image image = movie.getMoviePoster();
		setMovieImg.setImage(image);
		setMovieName.setText(movie.getMovieName());
		setMovieTime.setText("Thời lượng: " + movie.getMovieTime());
		setMovieGener.setText("Thể loại: " + movie.getMovieGener());
		setMovieReleaseDate.setText("Ngày ra mắt: " + movie.getMovieRealeseDate());
	}
}
