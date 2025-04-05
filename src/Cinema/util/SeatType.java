package Cinema.util;

public enum SeatType {
    PREMIUM(50000),  // Top tier, higher price
    VIP(30000),      // Middle tier, swapped with NORMAL
    NORMAL(0);       // Base tier, swapped with VIP

    private final int priceOffset;

    SeatType(int priceOffset) {
        this.priceOffset = priceOffset;
    }

    public int getPriceOffset() {
        return priceOffset;
    }
}
