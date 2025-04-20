package Cinema.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import Cinema.database.JSONUtility;

public class Controller implements Initializable {

    @FXML
    private Label btn_menuback, btn_menuback1, btn_account, btn_home, btn_movie, btn_support, btn_ticket, home_name, logout_btn, changePassword;
    @FXML
    private AnchorPane menuPane, settingPane;
    @FXML
    private BorderPane mainPane;
    @FXML
    private ImageView settings_icon;

    private Label currentSelectedButton = null;
    private Stage stage;
    private Scene scene;
    private Parent root;
    private boolean isSettingPaneVisible = false;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String firstName = JSONUtility.getUserFirstName();
        home_name.setText(firstName + " !");
          
        btn_menuback.setOnMouseClicked(event -> hideMenu());
        btn_menuback1.setOnMouseClicked(event -> showMenu());
        
        loadCenterContent("/Cinema/UI/Home_Center.fxml", btn_home);
        menuPane.setVisible(false);
        menuPane.setManaged(false);
        mainPane.setLeft(null);
        btn_menuback.setVisible(false);
        btn_menuback1.setVisible(true);
        
        btn_movie.setOnMouseClicked(event -> loadCenterContent("/Cinema/UI/FilmUI.fxml", btn_movie));
        btn_account.setOnMouseClicked(event -> loadCenterContent("/Cinema/UI/AccountSetting.fxml", btn_account));
        btn_home.setOnMouseClicked(event -> loadCenterContent("/Cinema/UI/Home_Center.fxml", btn_home));
        btn_support.setOnMouseClicked(event -> loadCenterContent("/Cinema/UI/ClientForm.fxml", btn_support));
        btn_ticket.setOnMouseClicked(event -> loadCenterContent("/Cinema/UI/UserTickets.fxml", btn_ticket));
        changePassword.setOnMouseClicked(event -> loadCenterContent("/Cinema/UI/ChangePassword.fxml", changePassword));
        if (settings_icon != null) {
            settings_icon.setOnMouseClicked(this::toggleSettingPane);
        } else {
            System.err.println("Warning: settings_icon is null. Check FXML file.");
        }

        if (settingPane != null) {
            settingPane.setTranslateY(-settingPane.getPrefHeight());
            settingPane.setTranslateX(mainPane.getPrefWidth() - settingPane.getPrefWidth() - 20);
        }

        mainPane.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                    if (isSettingPaneVisible) {
                        if (!settingPane.getBoundsInParent().contains(event.getX()-400, event.getY()+200) &&
                            !settings_icon.getBoundsInParent().contains(event.getX(), event.getY())) {
                            hideSettingPane();
                        }
                    }
                });
            }
        });
    }

    public void loadCenterContent(String fxmlFile, Label selectedButton) {
        try {
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent content = loader.load();
            mainPane.setCenter(content);
            
            if (selectedButton != null) {
                updateButtonStyle(selectedButton);
            }

            // Set parent controller for all child controllers
            Object controller = loader.getController();
            if (controller instanceof Home_Center_Controller) {
                ((Home_Center_Controller) controller).setParentController(this);
            }
            else if (controller instanceof BaseEventController) {
                ((BaseEventController) controller).setParentController(this);
            } else if (controller instanceof ChangePasswordController) {
            	((ChangePasswordController) controller).setParentController(this); 
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void goBack() {
           // load lại home_center nếu thực hiện
            loadCenterContent("/Cinema/UI/Home_Center.fxml", btn_home);
    }


    private void updateButtonStyle(Label selectedButton) {
        if (currentSelectedButton != null) {
            currentSelectedButton.setStyle("-fx-background-color: transparent; -fx-text-fill: black;");
        }
        if (selectedButton != null) {
            selectedButton.setStyle("-fx-background-color: #e8d15d; -fx-text-fill: white; -fx-font-weight:bold;");
            currentSelectedButton = selectedButton;
        }
    }

    private void hideMenu() {
        menuPane.setTranslateX(-menuPane.getWidth());
        menuPane.setVisible(false);
        menuPane.setManaged(false);
        mainPane.setLeft(null);
        btn_menuback.setVisible(false);
        btn_menuback1.setVisible(true);
    }

    private void showMenu() {
        menuPane.setVisible(true);
        menuPane.setManaged(true);
        mainPane.setLeft(menuPane);
        menuPane.setTranslateX(0);
        btn_menuback.setVisible(true);
        btn_menuback1.setVisible(false);
    }

    private void toggleSettingPane(MouseEvent event) {
        if (!isSettingPaneVisible) {
            // Show the SettingPanel
            settingPane.setVisible(true);

           
            double iconY = settings_icon.localToScene(settings_icon.getBoundsInLocal()).getMaxY();
            double targetY = iconY; // 100px below the icon

            // Set the position instantly (no animation)
            settingPane.setTranslateY(targetY);

            isSettingPaneVisible = true;
        } else {
            hideSettingPane();
        }
        event.consume(); // Prevent the scene-wide click from immediately hiding it
    }

    private void hideSettingPane() {
        // Hide the SettingPanel instantly
        settingPane.setTranslateY(-settingPane.getPrefHeight()); // Move back off-screen
        settingPane.setVisible(false);
        isSettingPaneVisible = false;
    }

    
 // Trong class Controller
    public void logOutButton(MouseEvent event) throws IOException {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        performLogout(stage);
    }

    public void performLogout(Stage stage) throws IOException {
        boolean isUserDataClearedSuccessfully = JSONUtility.removeValuesAndSave();
        if (isUserDataClearedSuccessfully) {
            root = FXMLLoader.load(getClass().getResource("/Cinema/UI/Login.fxml"));
            double currentWidth = stage.getWidth();
            double currentHeight = stage.getHeight();
            scene = new Scene(root, currentWidth, currentHeight);
            stage.setMaximized(true);
            stage.setScene(scene);
            stage.show();
        }
    }
    
}