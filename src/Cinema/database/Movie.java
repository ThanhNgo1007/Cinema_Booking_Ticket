package Cinema.database;

import java.io.InputStream;
import java.net.URL;
import javafx.scene.image.Image;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;

public class Movie {
	private String movieID, movieName, movieDescription;
	private String movieRating, movieTime;
	private String movieRealeseDate, movieGener;
	private String Actor, movieTrailer,director;
	private Image moviePoster;
	
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

	public String getMovieRealeseDate() {
		return movieRealeseDate;
	}

	public void setMovieRealeseDate(String movieRealeseDate) {
		this.movieRealeseDate = movieRealeseDate;
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
}
