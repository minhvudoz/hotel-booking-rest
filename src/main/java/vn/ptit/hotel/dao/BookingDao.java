package vn.ptit.hotel.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import vn.ptit.hotel.model.Booking;
import vn.ptit.hotel.model.Paging;

public final class BookingDao {
    private static final String FROM = " FROM bookings b JOIN rooms r ON r.id = b.room_id JOIN customers cu ON cu.id = b.customer_id";
    private static final String SELECT = "SELECT b.id, b.room_id, r.room_number, r.type AS room_type, b.customer_id,"
            + " cu.full_name AS customer_name, cu.phone AS customer_phone, b.check_in, b.check_out, b.status,"
            + " b.room_total, b.created_by, b.created_at" + FROM;

    private record Where(String sql, List<Object> params) {
    }

    private BookingDao() {
    }

    private static Booking map(ResultSet rs) throws SQLException {
        LocalDate checkIn = rs.getObject("check_in", LocalDate.class);
        LocalDate checkOut = rs.getObject("check_out", LocalDate.class);
        return new Booking(rs.getInt("id"), rs.getInt("room_id"), rs.getString("room_number"), rs.getString("room_type"),
                rs.getInt("customer_id"), rs.getString("customer_name"), rs.getString("customer_phone"),
                checkIn, checkOut, (int) ChronoUnit.DAYS.between(checkIn, checkOut),
                rs.getString("status"), rs.getLong("room_total"), rs.getString("created_by"),
                rs.getObject("created_at", LocalDateTime.class));
    }

    private static Where where(String status, String q) {
        List<String> conditions = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        if (status != null) {
            conditions.add("b.status = ?");
            params.add(status);
        }
        if (q != null) {
            conditions.add("(cu.full_name ILIKE ? OR cu.phone LIKE ? OR r.room_number ILIKE ?)");
            String like = "%" + q + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }
        return new Where(conditions.isEmpty() ? "" : " WHERE " + String.join(" AND ", conditions), params);
    }

    public static List<Booking> list(Connection c, String status, String q, Paging p) throws SQLException {
        Where w = where(status, q);
        List<Object> params = new ArrayList<>(w.params());
        params.add(p.size());
        params.add(p.offset());
        return Jdbc.list(c, SELECT + w.sql() + " ORDER BY b.check_in DESC, b.id DESC LIMIT ? OFFSET ?", BookingDao::map, params.toArray());
    }

    public static long count(Connection c, String status, String q) throws SQLException {
        Where w = where(status, q);
        return Jdbc.count(c, "SELECT count(*)" + FROM + w.sql(), w.params().toArray());
    }

    public static Optional<Booking> findById(Connection c, int id) throws SQLException {
        return Jdbc.one(c, SELECT + " WHERE b.id = ?", BookingDao::map, id);
    }

    public static Optional<Booking> lockById(Connection c, int id) throws SQLException {
        return Jdbc.one(c, SELECT + " WHERE b.id = ? FOR UPDATE OF b", BookingDao::map, id);
    }

    public static List<Booking> listByCustomer(Connection c, int customerId) throws SQLException {
        return Jdbc.list(c, SELECT + " WHERE b.customer_id = ? ORDER BY b.check_in DESC, b.id DESC", BookingDao::map, customerId);
    }

    /** Đơn còn hiệu lực trên cùng phòng có khoảng ngày giao với [checkIn, checkOut). */
    public static Optional<Booking> findConflict(Connection c, int roomId, LocalDate checkIn, LocalDate checkOut) throws SQLException {
        return Jdbc.one(c, SELECT + " WHERE b.room_id = ? AND b.status IN ('BOOKED', 'CHECKED_IN')"
                + " AND b.check_in < ? AND ? < b.check_out ORDER BY b.check_in LIMIT 1", BookingDao::map, roomId, checkOut, checkIn);
    }

    public static int insert(Connection c, int roomId, int customerId, LocalDate checkIn, LocalDate checkOut,
                             long roomTotal, String createdBy) throws SQLException {
        return Jdbc.insert(c, "INSERT INTO bookings (room_id, customer_id, check_in, check_out, status, room_total, created_by)"
                + " VALUES (?, ?, ?, ?, 'BOOKED', ?, ?)", roomId, customerId, checkIn, checkOut, roomTotal, createdBy);
    }

    public static void updateStatus(Connection c, int id, String status) throws SQLException {
        Jdbc.update(c, "UPDATE bookings SET status = ? WHERE id = ?", status, id);
    }
}
