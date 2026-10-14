package vn.ptit.hotel.model;

import java.time.LocalDate;

public record Overview(LocalDate date, int totalRooms, int available, int occupied, int arrivals, int departures) {
}
