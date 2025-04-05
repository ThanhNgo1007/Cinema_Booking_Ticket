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
    private HBox container;

    @FXML
    private ImageView img_side_left, img_side_right,zalo_banner;

    @FXML
    private ImageView event1, event2, event3, event4;

    private List<String> imagePaths;
    private int currentIndex = 0;
    private Timeline timeline;
    private Controller parentController;

    @FXML
    public void initialize() {
        // Danh sách hình ảnh
        imagePaths = new ArrayList<>();
        imagePaths.add("/Cinema/image/movie/980_x_488_4.png");
        imagePaths.add("/Cinema/image/movie/980x448_6__9.jpg");
        imagePaths.add("/Cinema/image/movie/980x448_6_-min.png");
        imagePaths.add("/Cinema/image/movie/cgv_980x448-min.png");
        imagePaths.add("/Cinema/image/movie/ff_rolling-banner.jpg");
        imagePaths.add("/Cinema/image/movie/yaame-min.jpg");
        imagePaths.add("/Cinema/image/movie/b_n_sao_c_a_980x448.png");
        imagePaths.add("/Cinema/image/movie/namaroll-min.png");
        imagePaths.add("/Cinema/image/movie/rolling-min.png");

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

        // Thêm sự kiện click cho các ImageView event
        event1.setOnMouseClicked(event -> loadEventContent("/Cinema/UI/Event1.fxml"));
        event2.setOnMouseClicked(event -> loadEventContent("/Cinema/UI/Event2.fxml"));
        event3.setOnMouseClicked(event -> loadEventContent("/Cinema/UI/Event3.fxml"));
        event4.setOnMouseClicked(event -> loadEventContent("/Cinema/UI/Event4.fxml"));
        img_side_left.setOnMouseClicked(event -> loadEventContent("/Cinema/UI/Event5.fxml"));
        img_side_right.setOnMouseClicked(event -> loadEventContent("/Cinema/UI/Event5.fxml"));
        zalo_banner.setOnMouseClicked(event -> loadEventContent("/Cinema/UI/Event5.fxml"));
    }
    
    private void loadEventContent(String fxmlFile) {
        if (parentController != null) {
            parentController.loadCenterContent(fxmlFile, null);
        } else {
            System.err.println("Parent controller is null in Home_Center_Controller");
        }
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
            if (!container.getChildren().contains(img_side_left)) {
                container.getChildren().add(0, img_side_left);
            }
            if (!container.getChildren().contains(img_side_right)) {
                container.getChildren().add(img_side_right);
            }
        } else {
            container.getChildren().remove(img_side_left);
            container.getChildren().remove(img_side_right);
        }
    }

    // Phương thức để thiết lập parentController
    public void setParentController(Controller parentController) {
        this.parentController = parentController;
    }
}