package Cinema.controller;

import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    private Label btn_menuback;

    @FXML
    private Label btn_menuback1;

    @FXML
    private AnchorPane menuPane;
    
    @FXML
    private Label btn_account;

    @FXML
    private Label btn_home;

    @FXML
    private Label btn_movie;

    @FXML
    private Label btn_support;

    @FXML
    private Label btn_ticket;
    
    @FXML
    private BorderPane mainPane; // Tham chiếu đến BorderPane trong FXML

    private boolean isMenuVisible = true; // Trạng thái menu (hiển thị ban đầu)
    private Label currentSelectedButton = null; // Lưu trạng thái nút hiện tại

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btn_menuback.setOnMouseClicked(event -> hideMenu());
        btn_menuback1.setOnMouseClicked(event -> showMenu());
        
        // Load nội dung mặc định vào center
        loadCenterContent("/Cinema/UI/Home_Center.fxml", btn_home);
        
     // Gán sự kiện đổi trang cho từng button
        btn_movie.setOnMouseClicked(event -> loadCenterContent("/Cinema/UI/FilmUI.fxml", btn_movie));
        btn_account.setOnMouseClicked(event -> loadCenterContent("/Cinema/UI/Account.fxml", btn_account));
        btn_home.setOnMouseClicked(event -> loadCenterContent("/Cinema/UI/Home_Center.fxml", btn_home));
        btn_support.setOnMouseClicked(event -> loadCenterContent("/Cinema/UI/Support.fxml", btn_support));
        btn_ticket.setOnMouseClicked(event -> loadCenterContent("/Cinema/UI/Ticket.fxml", btn_ticket));
    }

    private void hideMenu() {
        TranslateTransition slide = new TranslateTransition(Duration.seconds(0.4), menuPane);
        slide.setToX(-menuPane.getWidth()); // Dịch sang trái hoàn toàn
        slide.play();

        slide.setOnFinished(event -> {
            menuPane.setVisible(false);
            menuPane.setManaged(false);
            mainPane.setLeft(null); // Xóa menu khỏi layout
            btn_menuback.setVisible(false);
            btn_menuback1.setVisible(true);
        });
    }

    private void showMenu() {
        menuPane.setVisible(true);
        menuPane.setManaged(true);
        mainPane.setLeft(menuPane); // Đặt lại menu vào layout

        TranslateTransition slide = new TranslateTransition(Duration.seconds(0.4), menuPane);
        slide.setToX(0); // Trả menu về vị trí cũ
        slide.play();

        slide.setOnFinished(event -> {
            btn_menuback.setVisible(true);
            btn_menuback1.setVisible(false);
        });
    }

    
    private void loadCenterContent(String fxmlFile, Label selectedButton) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            mainPane.setCenter(loader.load()); // Đặt nội dung vào center
            
            // Cập nhật màu button
            updateButtonStyle(selectedButton);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void updateButtonStyle(Label selectedButton) {
        if (currentSelectedButton != null) {
            // Reset màu nền của nút trước đó
            currentSelectedButton.setStyle("-fx-background-color: transparent; -fx-text-fill: black;");
        }

        // Đổi màu nền và màu chữ của nút hiện tại
        selectedButton.setStyle("-fx-background-color: #ffcc00; -fx-text-fill: white;");
        currentSelectedButton = selectedButton; // Cập nhật trạng thái
    }

}
