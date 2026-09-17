package hotel.model;

import java.time.LocalDateTime;

public class Payment {
    private final String paymentId;
    private final String reservationId;
    private final double amount;
    private final String method;
    private final PaymentStatus status;
    private final LocalDateTime timestamp;

    public Payment(String paymentId, String reservationId, double amount, String method,
                    PaymentStatus status, LocalDateTime timestamp) {
        this.paymentId = paymentId;
        this.reservationId = reservationId;
        this.amount = amount;
        this.method = method;
        this.status = status;
        this.timestamp = timestamp;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getReservationId() {
        return reservationId;
    }

    public double getAmount() {
        return amount;
    }

    public String getMethod() {
        return method;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String toCsv() {
        return paymentId + "," + reservationId + "," + amount + "," + method + "," + status + "," + timestamp;
    }

    public static Payment fromCsv(String line) {
        String[] p = line.split(",");
        return new Payment(p[0], p[1], Double.parseDouble(p[2]), p[3], PaymentStatus.valueOf(p[4]),
                LocalDateTime.parse(p[5]));
    }

    @Override
    public String toString() {
        return String.format("Payment [%s] Rs.%.2f via %s -> %s at %s (Reservation %s)",
                paymentId, amount, method, status, timestamp, reservationId);
    }
}
