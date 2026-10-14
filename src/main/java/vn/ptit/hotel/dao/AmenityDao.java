package vn.ptit.hotel.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import vn.ptit.hotel.model.Amenity;

public final class AmenityDao {
    private static final String SELECT = "SELECT id, name, unit_price, unit FROM amenities";

    private AmenityDao() {
    }

    private static Amenity map(ResultSet rs) throws SQLException {
        return new Amenity(rs.getInt("id"), rs.getString("name"), rs.getLong("unit_price"), rs.getString("unit"));
    }

    public static List<Amenity> list(Connection c) throws SQLException {
        return Jdbc.list(c, SELECT + " ORDER BY name", AmenityDao::map);
    }

    public static Optional<Amenity> findById(Connection c, int id) throws SQLException {
        return Jdbc.one(c, SELECT + " WHERE id = ?", AmenityDao::map, id);
    }

    public static boolean nameTaken(Connection c, String name, int excludeId) throws SQLException {
        return Jdbc.count(c, "SELECT count(*) FROM amenities WHERE lower(name) = lower(?) AND id <> ?", name, excludeId) > 0;
    }

    public static boolean isUsed(Connection c, int id) throws SQLException {
        return Jdbc.count(c, "SELECT count(*) FROM booking_services WHERE amenity_id = ?", id) > 0;
    }

    public static int insert(Connection c, Amenity a) throws SQLException {
        return Jdbc.insert(c, "INSERT INTO amenities (name, unit_price, unit) VALUES (?, ?, ?)", a.name(), a.unitPrice(), a.unit());
    }

    public static void update(Connection c, int id, Amenity a) throws SQLException {
        Jdbc.update(c, "UPDATE amenities SET name = ?, unit_price = ?, unit = ? WHERE id = ?", a.name(), a.unitPrice(), a.unit(), id);
    }

    public static void delete(Connection c, int id) throws SQLException {
        Jdbc.update(c, "DELETE FROM amenities WHERE id = ?", id);
    }
}
