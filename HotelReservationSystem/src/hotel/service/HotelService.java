package hotel.service;

import hotel.io.FileStore;
import hotel.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Central service that owns all in-memory state and keeps it synced to disk.
 * Every mutating operation persists immediately so the "database" (plain
 * CSV files under data/) always reflects the current state.
 */
public class HotelService {

    private static final String DATA_DIR = "data";
    private static final String ROOMS_FILE = DATA_DIR + "/rooms.csv";
    private static final String GUESTS_FILE = DATA_DIR + "/guests.csv";
    private static final String RESERVATIONS_FILE = DATA_DIR + "/reservations.csv";
    private static final String PAYMENTS_FILE = DATA_DIR + "/payments.csv";

    private final List<Room> rooms = new ArrayList<>();
    private final List<Guest> guests = new ArrayList<>();
    private final List<Reservation> reservations = new ArrayList<>();
    private final List<Payment> payments = new ArrayList<>();

    private final Random random = new Random();

    public HotelService() {
        loadAll();
        if (rooms.isEmpty()) {
            seedDefaultRooms();
            saveRooms();
        }
    }

    // ---------------------------------------------------------------
    // Loading / seeding
    // ---------------------------------------------------------------

    private void loadAll() {
        rooms.addAll(FileStore.load(ROOMS_FILE, Room::fromCsv));
        guests.addAll(FileStore.load(GUESTS_FILE, Guest::fromCsv));
        reservations.addAll(FileStore.load(RESERVATIONS_FILE, Reservation::fromCsv));
        payments.addAll(FileStore.load(PAYMENTS_FILE, Payment::fromCsv));
    }

    private void seedDefaultRooms() {
        int roomNumber = 101;
        for (int i = 0; i < 5; i++) rooms.add(new Room(roomNumber++, RoomType.STANDARD, RoomType.STANDARD.getBasePrice()));
        for (int i = 0; i < 3; i++) rooms.add(new Room(roomNumber++, RoomType.DELUXE, RoomType.DELUXE.getBasePrice()));
        for (int i = 0; i < 2; i++) rooms.add(new Room(roomNumber++, RoomType.SUITE, RoomType.SUITE.getBasePrice()));
    }

    // ---------------------------------------------------------------
    // Persistence
    // ---------------------------------------------------------------

    private void saveRooms() {
        FileStore.saveAll(ROOMS_FILE, rooms, Room::toCsv);
    }

    private void saveGuests() {
        FileStore.saveAll(GUESTS_FILE, guests, Guest::toCsv);
    }

    private void saveReservations() {
        FileStore.saveAll(RESERVATIONS_FILE, reservations, Reservation::toCsv);
    }

    private void savePayments() {
        FileStore.saveAll(PAYMENTS_FILE, payments, Payment::toCsv);
    }

    // ---------------------------------------------------------------
    // Room search
    // ---------------------------------------------------------------

    public List<Room> getAllRooms() {
        return Collections.unmodifiableList(rooms);
    }

    /** Rooms of the given type (or all types if null) that are free for the whole stay. */
    public List<Room> searchAvailableRooms(RoomType type, LocalDate checkIn, LocalDate checkOut) {
        validateDateRange(checkIn, checkOut);
        return rooms.stream()
                .filter(Room::isActive)
                .filter(r -> type == null || r.getType() == type)
                .filter(r -> isRoomFree(r.getRoomNumber(), checkIn, checkOut, null))
                .collect(Collectors.toList());
    }

    private boolean isRoomFree(int roomNumber, LocalDate checkIn, LocalDate checkOut, String ignoreReservationId) {
        return reservations.stream()
                .filter(r -> r.getRoomNumber() == roomNumber)
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED)
                .filter(r -> !r.getReservationId().equals(ignoreReservationId))
                .noneMatch(r -> r.overlaps(checkIn, checkOut));
    }

    private void validateDateRange(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("Check-out date must be after check-in date.");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Check-in date cannot be in the past.");
        }
    }

    // ---------------------------------------------------------------
    // Guests
    // ---------------------------------------------------------------

    public Guest registerOrGetGuest(String name, String phone, String email) {
        Optional<Guest> existing = guests.stream()
                .filter(g -> g.getPhone().equalsIgnoreCase(phone))
                .findFirst();
        if (existing.isPresent()) {
            return existing.get();
        }
        String guestId = "G" + String.format("%04d", guests.size() + 1);
        Guest guest = new Guest(guestId, name, phone, email);
        guests.add(guest);
        saveGuests();
        return guest;
    }

    public Optional<Guest> findGuest(String guestId) {
        return guests.stream().filter(g -> g.getGuestId().equals(guestId)).findFirst();
    }

    // ---------------------------------------------------------------
    // Booking
    // ---------------------------------------------------------------

    public Reservation bookRoom(String guestId, int roomNumber, LocalDate checkIn, LocalDate checkOut)
            throws RoomNotAvailableException {
        validateDateRange(checkIn, checkOut);

        Room room = rooms.stream()
                .filter(r -> r.getRoomNumber() == roomNumber && r.isActive())
                .findFirst()
                .orElseThrow(() -> new RoomNotAvailableException("Room #" + roomNumber + " does not exist or is out of service."));

        if (!isRoomFree(roomNumber, checkIn, checkOut, null)) {
            throw new RoomNotAvailableException("Room #" + roomNumber + " is already booked for part of that date range.");
        }

        long nights = java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
        double total = nights * room.getPricePerNight();

        String reservationId = "R" + String.format("%05d", reservations.size() + 1);
        Reservation reservation = new Reservation(reservationId, guestId, roomNumber, checkIn, checkOut,
                total, ReservationStatus.CONFIRMED, "NONE");
        reservations.add(reservation);
        saveReservations();
        return reservation;
    }

    public void cancelReservation(String reservationId) throws NoSuchElementException {
        Reservation reservation = getReservationOrThrow(reservationId);
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new IllegalStateException("Reservation " + reservationId + " is already cancelled.");
        }
        reservation.setStatus(ReservationStatus.CANCELLED);
        saveReservations();

        // Simulate a refund if a successful payment exists.
        payments.stream()
                .filter(p -> p.getReservationId().equals(reservationId) && p.getStatus() == PaymentStatus.SUCCESS)
                .findFirst()
                .ifPresent(p -> {
                    Payment refund = new Payment("P" + String.format("%05d", payments.size() + 1),
                            reservationId, p.getAmount(), p.getMethod(), PaymentStatus.REFUNDED, LocalDateTime.now());
                    payments.add(refund);
                    savePayments();
                });
    }

    // ---------------------------------------------------------------
    // Payment simulation
    // ---------------------------------------------------------------

    /** Simulates charging a payment method. ~95% chance of success, like a real gateway call. */
    public Payment simulatePayment(String reservationId, String method) {
        Reservation reservation = getReservationOrThrow(reservationId);
        boolean success = random.nextInt(100) < 95;
        PaymentStatus status = success ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;

        String paymentId = "P" + String.format("%05d", payments.size() + 1);
        Payment payment = new Payment(paymentId, reservationId, reservation.getTotalAmount(), method,
                status, LocalDateTime.now());
        payments.add(payment);
        savePayments();

        if (success) {
            reservation.setPaymentId(paymentId);
            saveReservations();
        }
        return payment;
    }

    // ---------------------------------------------------------------
    // Lookups
    // ---------------------------------------------------------------

    public Reservation getReservationOrThrow(String reservationId) {
        return reservations.stream()
                .filter(r -> r.getReservationId().equals(reservationId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No reservation found with ID " + reservationId));
    }

    public List<Reservation> getAllReservations() {
        return Collections.unmodifiableList(reservations);
    }

    public List<Reservation> getReservationsForGuest(String guestId) {
        return reservations.stream().filter(r -> r.getGuestId().equals(guestId)).collect(Collectors.toList());
    }

    public Optional<Payment> getLatestPayment(String reservationId) {
        List<Payment> matches = payments.stream()
                .filter(p -> p.getReservationId().equals(reservationId))
                .collect(Collectors.toList());
        return matches.isEmpty() ? Optional.empty() : Optional.of(matches.get(matches.size() - 1));
    }

    public Optional<Room> findRoom(int roomNumber) {
        return rooms.stream().filter(r -> r.getRoomNumber() == roomNumber).findFirst();
    }
}
