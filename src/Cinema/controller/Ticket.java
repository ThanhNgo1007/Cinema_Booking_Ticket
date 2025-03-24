package Cinema.controller;

import java.time.LocalDateTime;

//Lớp đại diện cho một vé
public class Ticket {
    private String ticketId;
    private String movieName;
    private int totalPrice;
    private String seats;
    private String showtime;
    private LocalDateTime bookingDate;

    public Ticket(String ticketId, String movieName, int totalPrice, String seats, String showtime, LocalDateTime bookingDate) {
        this.ticketId = ticketId;
        this.movieName = movieName;
        this.totalPrice = totalPrice;
        this.seats = seats;
        this.showtime = showtime;
        this.bookingDate = bookingDate;
    }

    public String getTicketId() {
        return ticketId;
    }

    public String getMovieName() {
        return movieName;
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public String getSeats() {
        return seats;
    }

    public String getShowtime() {
        return showtime;
    }

    public LocalDateTime getBookingDate() {
        return bookingDate;
    }
}
