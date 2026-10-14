package vn.ptit.hotel.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import vn.ptit.hotel.model.ServiceUsage;

public final class ServiceUsageDao {
    private static final String SELECT = "SELECT s.id, s.booking_id, s.amenity_id, a.name, a.unit, s.quantity, s.unit_price, s.used_at"
            + " FROM booking_services s JOIN amenities a ON a.id = s.amenity_id";

    private ServiceUsageDao() {
    }

    private static ServiceUsage map(ResultSet rs) throws SQLException {
        int quantity = rs.getInt("quantity");
        long unitPrice = rs.getLong("unit_price");
        return new ServiceUsage(rs.getInt("id"), rs.getInt("booking_id"), rs.getInt("amenity_id"), rs.getString("name"),
                rs.getString("unit"), quantity, unitPrice, quantity * unitPrice, rs.getObject("used_at", LocalDateTime.class));
    }

    public static List<ServiceUsage> listByBooking(Connection c, int bookingId) throws SQLException {
        return Jdbc.list(c, SELECT + " WHERE s.booking_id = ? ORDER BY s.used_at, s.id", ServiceUsageDao::map, bookingId);
    }

    public static Optional<ServiceUsage> findById(Connection c, int id) throws SQLException {
        return Jdbc.one(c, SELECT + " WHERE s.id = ?", ServiceUsageDao::map, id);
    }

    public static int insert(Connection c, int bookingId, int amenityId, int quantity, long unitPrice) throws SQLException {
        return Jdbc.insert(c, "INSERT INTO booking_services (booking_id, amenity_id, quantity, unit_price) VALUES (?, ?, ?, ?)",
                bookingId, amenityId, quantity, unitPrice);
    }

    public static void delete(Connection c, int id) throws SQLException {
        Jdbc.update(c, "DELETE FROM booking_services WHERE id = ?", id);
    }

    public static long sumByBooking(Connection c, int bookingId) throws SQLException {
        return Jdbc.count(c, "SELECT COALESCE(SUM(quantity * unit_price), 0) FROM booking_services WHERE booking_id = ?", bookingId);
    }
}
