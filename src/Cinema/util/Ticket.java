package Cinema.util;

import java.time.LocalDateTime;

public class Ticket {
    private int ticketId;
    private int userId;
    private String movieName;
    private double totalPrice;
    private String seats;
    private String showtime;
    private String bookingDate; // Thêm bookingDate kiểu String
    private LocalDateTime bookingDateTime; // Lưu giá trị LocalDateTime để sử dụng trong TicketDetailController
    private int status;
    private String userName;
    private String userEmail;

    public Ticket(int ticketId, int userId, String movieName, double totalPrice, String seats,
                  String showtime, String bookingDate, LocalDateTime bookingDateTime, int status, String userName, String userEmail) {
        this.ticketId = ticketId;
        this.userId = userId;
        this.movieName = movieName;
        this.totalPrice = totalPrice;
        this.seats = seats;
        this.showtime = showtime;
        this.bookingDate = bookingDate;
        this.bookingDateTime = bookingDateTime;
        this.status = status;
        this.userName = userName;
        this.userEmail = userEmail;
    }

    // Getters và setters
    public int getTicketId() {
        return ticketId;
    }

    public void setTicketId(int ticketId) {
        this.ticketId = ticketId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getMovieName() {
        return movieName;
    }

    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getSeats() {
        return seats;
    }

    public void setSeats(String seats) {
        this.seats = seats;
    }

    public String getShowtime() {
        return showtime;
    }

    public void setShowtime(String showtime) {
        this.showtime = showtime;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(String bookingDate) {
        this.bookingDate = bookingDate;
    }

    public LocalDateTime getBookingDateTime() {
        return bookingDateTime;
    }

    public void setBookingDateTime(LocalDateTime bookingDateTime) {
        this.bookingDateTime = bookingDateTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}