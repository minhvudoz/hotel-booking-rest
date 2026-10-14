package vn.ptit.hotel.model;

import java.time.LocalDate;

public record BookingRequest(Integer roomId, Integer customerId, LocalDate checkIn, LocalDate checkOut) {
}
