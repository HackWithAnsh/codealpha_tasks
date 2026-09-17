# Grand Horizon Hotel — Reservation System (Java)

A console-based hotel reservation system demonstrating OOP design with
file-based persistence (no external database required).

## Features
- Room categorization: `STANDARD`, `DELUXE`, `SUITE` (each with its own base price)
- Search available rooms by type and date range (correctly checks for
  overlapping bookings, not just a single "booked/free" flag)
- Book a room for a guest (guests are looked up/created by phone number)
- Cancel a reservation (auto-simulates a refund if it was paid)
- Simulated payment gateway (CARD / UPI / CASH), ~95% success rate, with
  failed-payment handling
- View one reservation's full details (+ latest payment) or all reservations
- All data is stored in plain CSV files under `data/` and reloaded on
  every run, so bookings survive restarts

## Project Structure
```
src/hotel/model/     Room, RoomType, Guest, Reservation, ReservationStatus,
                      Payment, PaymentStatus  — plain data classes
src/hotel/io/        FileStore — generic CSV load/save helper
src/hotel/service/   HotelService — all business logic (search, book,
                      cancel, pay), RoomNotAvailableException
src/hotel/app/       Main — console menu / entry point
data/                CSV "database" files, created automatically on first run
```

## How it works (OOP design)
- **Room / Guest / Reservation / Payment** are immutable-where-possible model
  classes, each responsible for its own CSV (de)serialization
  (`toCsv()` / `fromCsv()`).
- **RoomType** and **ReservationStatus** / **PaymentStatus** are enums, so
  invalid categories/states are impossible to represent.
- **HotelService** is the single source of truth: it holds the in-memory
  lists, enforces business rules (no overlapping bookings, date validation),
  and persists to disk after every mutation.
- **FileStore** is a small generic helper (`load` / `saveAll`) so the CSV
  read/write logic isn't duplicated four times.
- **Main** only handles console I/O and delegates everything else to
  `HotelService` — the UI layer knows nothing about file paths.

Room availability is computed dynamically: a room is "available" for a date
range if no `CONFIRMED` reservation for it overlaps that range. This means a
room booked Oct 1–5 is still bookable for Oct 5–10 — a common bug in
simpler implementations that only use a boolean flag.

## Build & Run

Requires JDK 17+ (uses enhanced `switch` expressions and `isBlank()`).

```bash
# From the project root:
mkdir -p bin
javac -d bin $(find src -name "*.java")
java -cp bin hotel.app.Main
```

On first run the app seeds 10 default rooms (5 Standard, 3 Deluxe, 2 Suite)
into `data/rooms.csv`. Delete the `data/` folder to reset the system to a
clean slate.

## Sample session
```
1. Search Available Rooms
2. Book a Room
3. Cancel a Reservation
4. View Reservation Details
5. View All Reservations
6. List All Rooms
0. Exit
Choose an option: 2
Guest name: Riya Sharma
Guest phone: 9876543210
Guest email: riya@example.com
Guest ID: G0001
Room type (STANDARD/DELUXE/SUITE, blank for any): DELUXE
Check-in date (yyyy-MM-dd): 2026-10-10
Check-out date (yyyy-MM-dd): 2026-10-13
Available rooms:
  Room #106  | DELUXE   | Rs.2500.00/night | In Service
  Room #107  | DELUXE   | Rs.2500.00/night | In Service
  Room #108  | DELUXE   | Rs.2500.00/night | In Service
Enter room number to book: 106

Reservation created:
Reservation [R00001]
  Guest ID   : G0001
  Room       : #106
  Check-in   : 2026-10-10
  Check-out  : 2026-10-13
  Nights     : 3
  Total      : Rs.7500.00
  Status     : CONFIRMED
  Payment ID : NONE

Proceed to payment now? (y/n): y
Amount due: Rs.7500.00
Payment method (CARD/UPI/CASH): CARD
Payment [P00001] Rs.7500.00 via CARD -> SUCCESS at 2026-09-17T10:15:22.123 (Reservation R00001)
Payment successful! Reservation is confirmed.
```

## Extending it further
Some natural next steps if you want to take this further:
- Add a `Bill`/invoice generator that formats a receipt from a `Reservation` + `Payment`
- Add an admin mode to add/remove rooms or mark a room `Out of Service`
- Swap the CSV `FileStore` for JDBC + a real database — `HotelService`
  wouldn't need to change, only `FileStore`'s implementation
- Add unit tests for `HotelService` (overlap detection is the trickiest part to get right)
