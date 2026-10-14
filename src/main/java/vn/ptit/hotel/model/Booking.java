package vn.ptit.hotel.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record Booking(int id, int roomId, String roomNumber, String roomType,
                      int customerId, String customerName, String customerPhone,
                      LocalDate checkIn, LocalDate checkOut, int nights,
                      String status, long roomTotal, String createdBy, LocalDateTime createdAt) {
}
