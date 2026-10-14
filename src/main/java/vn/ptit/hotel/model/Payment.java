package vn.ptit.hotel.model;

import java.time.LocalDateTime;

public record Payment(int id, int bookingId, long amount, String method, LocalDateTime paidAt) {
}
