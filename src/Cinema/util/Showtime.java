package Cinema.util;

import java.time.LocalDate;
import java.time.LocalDate;
import java.time.LocalTime;

public class Showtime {
    private String id_lichchieu;
    private String date;
    private String time;
    private String id_movie;
    private int bookedSeatsCount;
    private int totalNumberSeats;
    private int screen; // Thêm thuộc tính screen
    private String end_time; // Thêm thuộc tính end_time

    public Showtime(String id_lichchieu, String date, String time, String id_movie, int bookedSeatsCount, 
                    int totalNumberSeats, int screen, String end_time) {
        this.id_lichchieu = id_lichchieu;
        this.date = date;
        this.time = time;
        this.id_movie = id_movie;
        this.bookedSeatsCount = bookedSeatsCount;
        this.totalNumberSeats = totalNumberSeats;
        this.screen = screen; // Khởi tạo screen
        this.end_time = end_time; // Khởi tạo end_time
    }
    public Showtime(String id_lichchieu, String date, String time, String id_movie, int bookedSeatsCount, 
            int totalNumberSeats) {
    		this.id_lichchieu = id_lichchieu;
    		this.date = date;
    		this.time = time;
    		this.id_movie = id_movie;
    		this.bookedSeatsCount = bookedSeatsCount;
    		this.totalNumberSeats = totalNumberSeats;
}
    public Showtime(String id_lichchieu,  String id_movie, String date, String time, int totalNumberSeats, int bookedSeatsCount, 
            int screen) {
    	this.id_lichchieu = id_lichchieu;
    	this.date = date;
    	this.time = time;
    	this.id_movie = id_movie;
    	this.bookedSeatsCount = bookedSeatsCount;
    	this.totalNumberSeats = totalNumberSeats;
    	this.screen = screen; // Khởi tạo screen
}

    public String getId_lichchieu() {
        return id_lichchieu;
    }

    public String getDate() { // Thay đổi kiểu trả về thành String
        return date;
    }

    public void setDate(String date) { // Thay đổi kiểu tham số thành String
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public int getAvailableSeats() {
        return totalNumberSeats - bookedSeatsCount;
    }

    public int getTotalNumberSeats() {
        return totalNumberSeats;
    }

    public String getId_movie() {
        return id_movie;
    }

    public void setId_movie(String id_movie) {
        this.id_movie = id_movie;
    }

    public int getScreen() {
        return screen;
    }

    public void setScreen(int screen) {
        this.screen = screen;
    }

    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }
}