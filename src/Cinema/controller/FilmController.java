package Cinema.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

public class FilmController implements Initializable  {
    @FXML
    private TextField input_search;

    @FXML
    private ImageView search_icon;

    public void handleClickIcon() {
    	input_search.requestFocus();
    }
    
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub
		
	}
    
}
