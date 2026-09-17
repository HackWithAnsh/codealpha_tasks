package hotel.model;

/**
 * Categorizes rooms and defines the base nightly price for each category.
 */
public enum RoomType {
    STANDARD(1500.0),
    DELUXE(2500.0),
    SUITE(4000.0);

    private final double basePrice;

    RoomType(double basePrice) {
        this.basePrice = basePrice;
    }

    public double getBasePrice() {
        return basePrice;
    }
}
