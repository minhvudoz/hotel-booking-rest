package vn.ptit.hotel.model;

public record Room(Integer id, String roomNumber, String type, Long pricePerNight, Integer capacity, String description) {
}
