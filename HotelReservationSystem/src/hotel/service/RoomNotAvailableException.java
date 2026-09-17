package hotel.service;

/** Thrown when a room is requested for dates it is already booked for. */
public class RoomNotAvailableException extends Exception {
    public RoomNotAvailableException(String message) {
        super(message);
    }
}
