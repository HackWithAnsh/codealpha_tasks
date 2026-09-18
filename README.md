# 🏨 Hotel Reservation System

A Java-based console application for managing hotel room reservations.

## ✨ Features

- View available rooms
- Search rooms by room type
- Make a reservation
- View reservation details
- Cancel reservations
- Check room availability
- Date validation
- Exception handling
- CSV-based room data

## 🛠️ Technologies Used

- Java
- Object-Oriented Programming (OOP)
- ArrayList / Collections
- Exception Handling
- File Handling
- Java Date & Time API
- CSV
- Git & GitHub

## 📁 Project Structure

```text
HotelReservationSystem/
│
├── src/
│   └── hotel/
│       ├── app/
│       │   └── Main.java
│       │
│       ├── model/
│       │   ├── Reservation.java
│       │   ├── ReservationStatus.java
│       │   ├── Room.java
│       │   └── RoomType.java
│       │
│       └── service/
│           ├── HotelService.java
│           └── RoomNotAvailableException.java
│
├── data/
│   └── rooms.csv
│
├── .gitignore
└── README.md