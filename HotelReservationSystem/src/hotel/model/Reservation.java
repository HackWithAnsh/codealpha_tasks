package hotel.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Reservation {
    private final String reservationId;
    private final String guestId;
    private final int roomNumber;
    private final LocalDate checkIn;
    private final LocalDate checkOut;
    private final double totalAmount;
    private ReservationStatus status;
    private String paymentId; // linked payment record, "NONE" until paid

    public Reservation(String reservationId, String guestId, int roomNumber,
                        LocalDate checkIn, LocalDate checkOut, double totalAmount,
                        ReservationStatus status, String paymentId) {
        this.reservationId = reservationId;
        this.guestId = guestId;
        this.roomNumber = roomNumber;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.totalAmount = totalAmount;
        this.status = status;
        this.paymentId = paymentId;
    }

    public long getNights() {
        return ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    public String getReservationId() {
        return reservationId;
    }

    public String getGuestId() {
        return guestId;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public LocalDate getCheckOut() {
        return checkOut;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    /** True if this reservation's date range overlaps the given range. */
    public boolean overlaps(LocalDate otherIn, LocalDate otherOut) {
        return checkIn.isBefore(otherOut) && otherIn.isBefore(checkOut);
    }

    public String toCsv() {
        return reservationId + "," + guestId + "," + roomNumber + "," + checkIn + "," +
                checkOut + "," + totalAmount + "," + status + "," + paymentId;
    }

    public static Reservation fromCsv(String line) {
        String[] p = line.split(",");
        return new Reservation(p[0], p[1], Integer.parseInt(p[2]), LocalDate.parse(p[3]),
                LocalDate.parse(p[4]), Double.parseDouble(p[5]), ReservationStatus.valueOf(p[6]), p[7]);
    }

    @Override
    public String toString() {
        return String.format(
                "Reservation [%s]%n  Guest ID   : %s%n  Room       : #%d%n  Check-in   : %s%n  Check-out  : %s%n  Nights     : %d%n  Total      : Rs.%.2f%n  Status     : %s%n  Payment ID : %s",
                reservationId, guestId, roomNumber, checkIn, checkOut, getNights(), totalAmount, status, paymentId);
    }
}
