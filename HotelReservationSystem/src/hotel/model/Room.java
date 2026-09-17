package hotel.model;

/**
 * Represents a physical hotel room. Availability for a specific date range
 * is computed by the service layer (based on overlapping reservations),
 * not stored as a single boolean here — a room can be free next week even
 * if it is occupied tonight.
 */
public class Room {
    private final int roomNumber;
    private final RoomType type;
    private double pricePerNight;
    private boolean active; // false = room taken out of service (maintenance etc.)

    public Room(int roomNumber, RoomType type, double pricePerNight) {
        this.roomNumber = roomNumber;
        this.type = type;
        this.pricePerNight = pricePerNight;
        this.active = true;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public RoomType getType() {
        return type;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(double pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String toCsv() {
        return roomNumber + "," + type + "," + pricePerNight + "," + active;
    }

    public static Room fromCsv(String line) {
        String[] p = line.split(",");
        Room r = new Room(Integer.parseInt(p[0]), RoomType.valueOf(p[1]), Double.parseDouble(p[2]));
        r.setActive(Boolean.parseBoolean(p[3]));
        return r;
    }

    @Override
    public String toString() {
        return String.format("Room #%-4d | %-8s | Rs.%-8.2f/night | %s",
                roomNumber, type, pricePerNight, active ? "In Service" : "Out of Service");
    }
}
