package Cinema.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.List;
import java.net.URL;
import java.util.ArrayList;

public class Home_Center_Controller {

    @FXML
    private ImageView imageView;
    
    @FXML
    private Button prevButton, nextButton;
    
    @FXML
    private StackPane stackPane;
    
    @FXML
    private HBox container; // HBox chính chứa nội dung

    @FXML
    private ImageView img_side_left, img_side_right; // Hai hình ảnh hai bên

    private List<String> imagePaths;
    private int currentIndex = 0;
    private Timeline timeline;

    @FXML
    public void initialize() {
        // Danh sách hình ảnh (có thể thay bằng đường dẫn thực tế)
        imagePaths = new ArrayList<>();
        imagePaths.add("/Cinema/image/movie/rolling_banner_21_-_23_2_-min.jpg");
        imagePaths.add("/Cinema/image/movie/980wx448h_1_18.jpg");
        imagePaths.add("/Cinema/image/movie/980wx448h_1__19.jpg");
        imagePaths.add("/Cinema/image/movie/980wx448h_63.jpg");
        imagePaths.add("/Cinema/image/movie/atsh_itscupontime_980x448.jpg");

        URL imageUrl = getClass().getResource(imagePaths.get(currentIndex));
        if (!imagePaths.isEmpty()) {
        	imageView.setImage(new Image(imageUrl.toExternalForm()));
        }

        // Tự động chuyển ảnh
        timeline = new Timeline(new KeyFrame(Duration.seconds(5), e -> showNextImage()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        
     // Lắng nghe sự kiện thay đổi kích thước cửa sổ
        container.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Stage stage = (Stage) newScene.getWindow();
                stage.widthProperty().addListener((obs2, oldWidth, newWidth) -> adjustSideImages(newWidth.doubleValue()));
            }
        });
    }

    public void showNextImage() {
        currentIndex = (currentIndex + 1) % imagePaths.size();
        imageView.setImage(new Image(imagePaths.get(currentIndex)));
    }

    public void showPrevImage() {
        currentIndex = (currentIndex - 1 + imagePaths.size()) % imagePaths.size();
        imageView.setImage(new Image(imagePaths.get(currentIndex)));
    }
    
    private void adjustSideImages(double width) {
        if (width > 1300) { 
            // Nếu cửa sổ rộng >1300px, hiển thị và thêm lại vào HBox
            if (!container.getChildren().contains(img_side_left)) {
                container.getChildren().add(0, img_side_left); // Thêm ảnh trái
            }
            if (!container.getChildren().contains(img_side_right)) {
                container.getChildren().add(img_side_right); // Thêm ảnh phải
            }
        } else {
            // Nếu cửa sổ nhỏ hơn 1300px, xoá khỏi HBox để không chiếm diện tích
            container.getChildren().remove(img_side_left);
            container.getChildren().remove(img_side_right);
        }
    }
    
}
