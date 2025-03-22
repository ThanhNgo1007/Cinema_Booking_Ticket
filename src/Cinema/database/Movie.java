package Cinema.database;

import java.io.InputStream;
import java.net.URL;
import javafx.scene.image.Image;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;

public class Movie {
	private String movieID, movieName, movieDescription, price;
	private String movieRating, movieTime;
	private String movieRealeseDate, movieGener;
	private String Actor, movieTrailer,director;
	private Image moviePoster;
	private String createDate, updateDate;
	private Integer status;
	
	public String getMovieName() {
		return movieName;
	}

	public void setMoviePosterFromBlob(InputStream imageStream) {
	    if (imageStream != null) {
	        this.moviePoster = new Image(imageStream);
	    } else {
	        this.moviePoster = null;
	    }
	}


	public Image getMoviePoster() {
		return moviePoster;
	}

	public void setMovieName(String movieName) {
		this.movieName = movieName;
	}

	public String getMovieDescription() {
		return movieDescription;
	}

	public void setMovieDescription(String movieDescription) {
		this.movieDescription = movieDescription;
	}

	public String getMovieRating() {
		return movieRating;
	}

	public void setMovieRating(String movieRating) {
		this.movieRating = movieRating;
	}


	public String getMovieGener() {
		return movieGener;
	}

	public void setMovieGener(String movieGener) {
		this.movieGener = movieGener;
	}

	public String getMovieActor() {
		return Actor;
	}

	public void setMovieActor(String Actor) {
		this.Actor = Actor;
	}

	public String getMovieTrailer() {
		return movieTrailer;
	}

	public void setMovieTrailer(String movieTrailer) {
		this.movieTrailer = movieTrailer;
	}

	public String getMovieTime() {
		return movieTime;
	}

	public void setMovieTime(String movieTime) {
		this.movieTime = movieTime;
	}

	public String getDirector() {
		return director;
	}

	public void setDirector(String director) {
		this.director = director;
	}

	public String getMovieID() {
		return movieID;
	}

	public void setMovieID(String movieID) {
		this.movieID = movieID;
	}

	public String getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(String updateDate) {
		this.updateDate = updateDate;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getCreateDate() {
		return createDate;
	}

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

	public String getMovieRealeseDate() {
		return movieRealeseDate;
	}

	public void setMovieRealeseDate(String movieRealeseDate) {
		this.movieRealeseDate = movieRealeseDate;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}
}
