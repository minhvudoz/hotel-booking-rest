package vn.ptit.hotel.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import vn.ptit.hotel.model.Customer;
import vn.ptit.hotel.model.Paging;

public final class CustomerDao {
    private static final String SELECT = "SELECT id, full_name, phone, id_card, email, created_at FROM customers";
    private static final String SEARCH = " WHERE full_name ILIKE ? OR phone LIKE ?";

    private CustomerDao() {
    }

    private static Customer map(ResultSet rs) throws SQLException {
        return new Customer(rs.getInt("id"), rs.getString("full_name"), rs.getString("phone"), rs.getString("id_card"),
                rs.getString("email"), rs.getObject("created_at", LocalDateTime.class));
    }

    public static List<Customer> search(Connection c, String q, Paging p) throws SQLException {
        if (q == null) {
            return Jdbc.list(c, SELECT + " ORDER BY full_name, id LIMIT ? OFFSET ?", CustomerDao::map, p.size(), p.offset());
        }
        String like = "%" + q + "%";
        return Jdbc.list(c, SELECT + SEARCH + " ORDER BY full_name, id LIMIT ? OFFSET ?", CustomerDao::map, like, like, p.size(), p.offset());
    }

    public static long count(Connection c, String q) throws SQLException {
        if (q == null) {
            return Jdbc.count(c, "SELECT count(*) FROM customers");
        }
        String like = "%" + q + "%";
        return Jdbc.count(c, "SELECT count(*) FROM customers" + SEARCH, like, like);
    }

    public static Optional<Customer> findById(Connection c, int id) throws SQLException {
        return Jdbc.one(c, SELECT + " WHERE id = ?", CustomerDao::map, id);
    }

    public static boolean phoneTaken(Connection c, String phone, int excludeId) throws SQLException {
        return Jdbc.count(c, "SELECT count(*) FROM customers WHERE phone = ? AND id <> ?", phone, excludeId) > 0;
    }

    public static int insert(Connection c, Customer cu) throws SQLException {
        return Jdbc.insert(c, "INSERT INTO customers (full_name, phone, id_card, email) VALUES (?, ?, ?, ?)",
                cu.fullName(), cu.phone(), cu.idCard(), cu.email());
    }

    public static void update(Connection c, int id, Customer cu) throws SQLException {
        Jdbc.update(c, "UPDATE customers SET full_name = ?, phone = ?, id_card = ?, email = ? WHERE id = ?",
                cu.fullName(), cu.phone(), cu.idCard(), cu.email(), id);
    }
}
