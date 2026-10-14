package vn.ptit.hotel.model;

import java.time.LocalDateTime;

public record Customer(Integer id, String fullName, String phone, String idCard, String email, LocalDateTime createdAt) {
}
