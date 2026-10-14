package vn.ptit.hotel.model;

import java.time.LocalDateTime;

public record ServiceUsage(int id, int bookingId, int amenityId, String amenityName, String unit,
                           int quantity, long unitPrice, long amount, LocalDateTime usedAt) {
}
