package vn.ptit.hotel.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;
import vn.ptit.hotel.config.Env;
import vn.ptit.hotel.model.Payment;

public final class PaymentDao {
    private PaymentDao() {
    }

    public static Optional<Payment> findByBooking(Connection c, int bookingId) throws SQLException {
        return Jdbc.one(c, "SELECT id, booking_id, amount, method, paid_at FROM payments WHERE booking_id = ?",
                rs -> new Payment(rs.getInt("id"), rs.getInt("booking_id"), rs.getLong("amount"), rs.getString("method"),
                        rs.getObject("paid_at", LocalDateTime.class)),
                bookingId);
    }

    public static void insert(Connection c, int bookingId, long amount, String method) throws SQLException {
        Jdbc.insert(c, "INSERT INTO payments (booking_id, amount, method, paid_at) VALUES (?, ?, ?, ?)", bookingId, amount, method, Env.now());
    }
}
