package Cinema.util;

public class Showtime {
    private String id;
    private String movieId;
    private String showDate;
    private String showTime;
    private Integer totalNumberSeats;
    private Integer bookedSeatsCount;
    private Integer screen;

    public Showtime(String id, String movieId, String showDate, String showTime, Integer totalNumberSeats, Integer bookedSeatsCount, Integer screen) {
        this.id = id;
        this.movieId = movieId;
        this.showDate = showDate;
        this.showTime = showTime;
        this.totalNumberSeats = totalNumberSeats;
        this.bookedSeatsCount = bookedSeatsCount;
        this.setScreen(screen);
    }

    // Getter và Setter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public String getShowDate() {
        return showDate;
    }

    public void setShowDate(String showDate) {
        this.showDate = showDate;
    }

    public String getShowTime() {
        return showTime;
    }

    public void setShowTime(String showTime) {
        this.showTime = showTime;
    }

    public Integer getTotalNumberSeats() {
        return totalNumberSeats;
    }

    public void setTotalNumberSeats(Integer totalNumberSeats) {
        this.totalNumberSeats = totalNumberSeats;
    }

    public Integer getBookedSeatsCount() {
        return bookedSeatsCount;
    }

    public void setBookedSeatsCount(Integer bookedSeatsCount) {
        this.bookedSeatsCount = bookedSeatsCount;
    }

	public Integer getScreen() {
		return screen;
	}

	public void setScreen(Integer screen) {
		this.screen = screen;
	}
}