package hotel.app;

import hotel.model.*;
import hotel.service.HotelService;
import hotel.service.RoomNotAvailableException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final HotelService hotelService = new HotelService();

    public static void main(String[] args) {
        System.out.println("=======================================");
        System.out.println(" Welcome to the Grand Horizon Hotel");
        System.out.println(" Reservation Management System");
        System.out.println("=======================================");

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> searchRooms();
                case "2" -> bookRoom();
                case "3" -> cancelReservation();
                case "4" -> viewReservation();
                case "5" -> viewAllReservations();
                case "6" -> listAllRooms();
                case "0" -> {
                    running = false;
                    System.out.println("Thank you for using Grand Horizon Hotel system. Goodbye!");
                }
                default -> System.out.println("Invalid option, please try again.");
            }
        }
        scanner.close();
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("---------------------------------------");
        System.out.println("1. Search Available Rooms");
        System.out.println("2. Book a Room");
        System.out.println("3. Cancel a Reservation");
        System.out.println("4. View Reservation Details");
        System.out.println("5. View All Reservations");
        System.out.println("6. List All Rooms");
        System.out.println("0. Exit");
        System.out.println("---------------------------------------");
        System.out.print("Choose an option: ");
    }

    // -----------------------------------------------------------
    // Menu actions
    // -----------------------------------------------------------

    private static void searchRooms() {
        try {
            RoomType type = readOptionalRoomType();
            LocalDate checkIn = readDate("Check-in date (yyyy-MM-dd): ");
            LocalDate checkOut = readDate("Check-out date (yyyy-MM-dd): ");

            List<Room> available = hotelService.searchAvailableRooms(type, checkIn, checkOut);
            if (available.isEmpty()) {
                System.out.println("No rooms available for the selected criteria.");
                return;
            }
            System.out.println("\nAvailable rooms from " + checkIn + " to " + checkOut + ":");
            for (Room r : available) {
                System.out.println("  " + r);
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void bookRoom() {
        try {
            System.out.print("Guest name: ");
            String name = scanner.nextLine().trim();
            System.out.print("Guest phone: ");
            String phone = scanner.nextLine().trim();
            System.out.print("Guest email: ");
            String email = scanner.nextLine().trim();
            Guest guest = hotelService.registerOrGetGuest(name, phone, email);
            System.out.println("Guest ID: " + guest.getGuestId());

            RoomType type = readOptionalRoomType();
            LocalDate checkIn = readDate("Check-in date (yyyy-MM-dd): ");
            LocalDate checkOut = readDate("Check-out date (yyyy-MM-dd): ");

            List<Room> available = hotelService.searchAvailableRooms(type, checkIn, checkOut);
            if (available.isEmpty()) {
                System.out.println("No rooms available for those dates.");
                return;
            }
            System.out.println("Available rooms:");
            for (Room r : available) {
                System.out.println("  " + r);
            }
            System.out.print("Enter room number to book: ");
            int roomNumber = Integer.parseInt(scanner.nextLine().trim());

            Reservation reservation = hotelService.bookRoom(guest.getGuestId(), roomNumber, checkIn, checkOut);
            System.out.println("\nReservation created:");
            System.out.println(reservation);

            System.out.print("\nProceed to payment now? (y/n): ");
            if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
                processPayment(reservation);
            } else {
                System.out.println("You can pay later using the reservation ID: " + reservation.getReservationId());
            }
        } catch (RoomNotAvailableException e) {
            System.out.println("Booking failed: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void processPayment(Reservation reservation) {
        System.out.println("Amount due: Rs." + String.format("%.2f", reservation.getTotalAmount()));
        System.out.print("Payment method (CARD/UPI/CASH): ");
        String method = scanner.nextLine().trim();
        if (method.isBlank()) method = "CARD";

        Payment payment = hotelService.simulatePayment(reservation.getReservationId(), method);
        System.out.println(payment);
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            System.out.println("Payment successful! Reservation is confirmed.");
        } else {
            System.out.println("Payment failed. The reservation still holds the room — try paying again from the menu.");
        }
    }

    private static void cancelReservation() {
        System.out.print("Enter reservation ID to cancel: ");
        String id = scanner.nextLine().trim();
        try {
            hotelService.cancelReservation(id);
            System.out.println("Reservation " + id + " has been cancelled.");
        } catch (NoSuchElementException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void viewReservation() {
        System.out.print("Enter reservation ID: ");
        String id = scanner.nextLine().trim();
        try {
            Reservation reservation = hotelService.getReservationOrThrow(id);
            System.out.println(reservation);
            hotelService.getLatestPayment(id).ifPresent(p -> {
                System.out.println("Latest payment:");
                System.out.println("  " + p);
            });
        } catch (NoSuchElementException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void viewAllReservations() {
        List<Reservation> all = hotelService.getAllReservations();
        if (all.isEmpty()) {
            System.out.println("No reservations yet.");
            return;
        }
        for (Reservation r : all) {
            System.out.println(r);
            System.out.println("-----");
        }
    }

    private static void listAllRooms() {
        for (Room r : hotelService.getAllRooms()) {
            System.out.println("  " + r);
        }
    }

    // -----------------------------------------------------------
    // Input helpers
    // -----------------------------------------------------------

    private static LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return LocalDate.parse(input);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please use yyyy-MM-dd (e.g. 2026-10-05).");
            }
        }
    }

    private static RoomType readOptionalRoomType() {
        System.out.print("Room type (STANDARD/DELUXE/SUITE, blank for any): ");
        String input = scanner.nextLine().trim();
        if (input.isBlank()) return null;
        try {
            return RoomType.valueOf(input.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Unrecognized type, showing all types instead.");
            return null;
        }
    }
}
