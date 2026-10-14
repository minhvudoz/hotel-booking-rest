package vn.ptit.hotel.model;

import java.time.LocalDate;

public record DailyOccupancy(LocalDate date, int occupiedRooms, int totalRooms, double rate) {
}
