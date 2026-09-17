package hotel.model;

public class Guest {
    private String guestId;
    private String name;
    private String phone;
    private String email;

    public Guest(String guestId, String name, String phone, String email) {
        this.guestId = guestId;
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    public String getGuestId() {
        return guestId;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String toCsv() {
        return guestId + "," + name + "," + phone + "," + email;
    }

    public static Guest fromCsv(String line) {
        String[] p = line.split(",", -1);
        return new Guest(p[0], p[1], p[2], p[3]);
    }

    @Override
    public String toString() {
        return String.format("%s (ID: %s, Phone: %s, Email: %s)", name, guestId, phone, email);
    }
}
