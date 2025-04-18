package Cinema.controller;

import javafx.fxml.FXML;

public abstract class BaseEventController {
    protected Controller parentController;
    // Phương thức dùng lấy controller truyền cho biến để thông qua biến dùng các phương thức của controller khác
    public void setParentController(Controller parentController) {
        this.parentController = parentController;
    }

    @FXML
    protected void goBack() {
        if (parentController != null) {
            parentController.goBack(); //dùng phương thức goBack() ở Controller.java
        }
    }
}