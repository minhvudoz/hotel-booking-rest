package vn.ptit.hotel.model;

public record RoomStatus(Room room, String status, Integer bookingId, String guestName) {
}
