package vn.ptit.hotel.model;

import java.util.List;

public record Invoice(Booking booking, long roomTotal, List<ServiceUsage> services, long servicesTotal, long total, Payment payment) {
}
