package Cinema.util;

import javafx.scene.control.Button;

// Lưu giá của từng ghế dựa trên loại ghế
public class SeatButton extends Button {
    private int seatPrice; // Giá của ghế

    public SeatButton(String text, int seatPrice) {
        super(text);
        this.seatPrice = seatPrice;
    }

    public int getSeatPrice() {
        return seatPrice;
    }
}
