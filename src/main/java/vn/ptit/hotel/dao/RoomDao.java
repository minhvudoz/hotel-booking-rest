package vn.ptit.hotel.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import vn.ptit.hotel.model.Paging;
import vn.ptit.hotel.model.Room;
import vn.ptit.hotel.model.RoomStatus;

public final class RoomDao {
    private static final String SELECT = "SELECT r.id, r.room_number, r.type, r.price_per_night, r.capacity, r.description FROM rooms r";

    private RoomDao() {
    }

    static Room map(ResultSet rs) throws SQLException {
        return new Room(rs.getInt("id"), rs.getString("room_number"), rs.getString("type"),
                rs.getLong("price_per_night"), rs.getInt("capacity"), rs.getString("description"));
    }

    public static List<Room> list(Connection c, String type, Paging p) throws SQLException {
        return type == null
                ? Jdbc.list(c, SELECT + " ORDER BY r.room_number LIMIT ? OFFSET ?", RoomDao::map, p.size(), p.offset())
                : Jdbc.list(c, SELECT + " WHERE r.type = ? ORDER BY r.room_number LIMIT ? OFFSET ?", RoomDao::map, type, p.size(), p.offset());
    }

    public static long count(Connection c, String type) throws SQLException {
        return type == null
                ? Jdbc.count(c, "SELECT count(*) FROM rooms")
                : Jdbc.count(c, "SELECT count(*) FROM rooms WHERE type = ?", type);
    }

    public static Optional<Room> findById(Connection c, int id) throws SQLException {
        return Jdbc.one(c, SELECT + " WHERE r.id = ?", RoomDao::map, id);
    }

    public static Optional<Room> lockById(Connection c, int id) throws SQLException {
        return Jdbc.one(c, SELECT + " WHERE r.id = ? FOR UPDATE", RoomDao::map, id);
    }

    public static boolean numberTaken(Connection c, String roomNumber, int excludeId) throws SQLException {
        return Jdbc.count(c, "SELECT count(*) FROM rooms WHERE room_number = ? AND id <> ?", roomNumber, excludeId) > 0;
    }

    public static boolean hasBookings(Connection c, int id) throws SQLException {
        return Jdbc.count(c, "SELECT count(*) FROM bookings WHERE room_id = ?", id) > 0;
    }

    public static int insert(Connection c, Room r) throws SQLException {
        return Jdbc.insert(c, "INSERT INTO rooms (room_number, type, price_per_night, capacity, description) VALUES (?, ?, ?, ?, ?)",
                r.roomNumber(), r.type(), r.pricePerNight(), r.capacity(), r.description());
    }

    public static void update(Connection c, int id, Room r) throws SQLException {
        Jdbc.update(c, "UPDATE rooms SET room_number = ?, type = ?, price_per_night = ?, capacity = ?, description = ? WHERE id = ?",
                r.roomNumber(), r.type(), r.pricePerNight(), r.capacity(), r.description(), id);
    }

    public static void delete(Connection c, int id) throws SQLException {
        Jdbc.update(c, "DELETE FROM rooms WHERE id = ?", id);
    }

    public static List<Room> available(Connection c, LocalDate checkIn, LocalDate checkOut, String type) throws SQLException {
        String sql = SELECT + " WHERE NOT EXISTS (SELECT 1 FROM bookings b WHERE b.room_id = r.id"
                + " AND b.status IN ('BOOKED', 'CHECKED_IN') AND b.check_in < ? AND ? < b.check_out)";
        return type == null
                ? Jdbc.list(c, sql + " ORDER BY r.room_number", RoomDao::map, checkOut, checkIn)
                : Jdbc.list(c, sql + " AND r.type = ? ORDER BY r.room_number", RoomDao::map, checkOut, checkIn, type);
    }

    /** pastDate = true thì đơn CHECKED_OUT cũng được tính là phòng có khách (xem lại lịch sử). */
    public static List<RoomStatus> statusOn(Connection c, LocalDate date, boolean pastDate) throws SQLException {
        return Jdbc.list(c, """
                SELECT DISTINCT ON (r.room_number) r.id, r.room_number, r.type, r.price_per_night, r.capacity, r.description,
                       b.id AS booking_id, b.status AS booking_status, cu.full_name AS guest_name
                FROM rooms r
                LEFT JOIN bookings b ON b.room_id = r.id AND b.check_in <= ? AND ? < b.check_out
                     AND (b.status IN ('BOOKED', 'CHECKED_IN') OR (b.status = 'CHECKED_OUT' AND ?))
                LEFT JOIN customers cu ON cu.id = b.customer_id
                ORDER BY r.room_number, CASE b.status WHEN 'CHECKED_IN' THEN 0 WHEN 'BOOKED' THEN 1 ELSE 2 END""",
                rs -> {
                    String bookingStatus = rs.getString("booking_status");
                    String status = bookingStatus == null ? "AVAILABLE" : "BOOKED".equals(bookingStatus) ? "BOOKED" : "OCCUPIED";
                    return new RoomStatus(map(rs), status, (Integer) rs.getObject("booking_id"), rs.getString("guest_name"));
                },
                date, date, pastDate);
    }
}
