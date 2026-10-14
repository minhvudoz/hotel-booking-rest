package vn.ptit.hotel.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import vn.ptit.hotel.model.User;

public final class UserDao {
    public record Account(User user, String passwordHash) {
    }

    private UserDao() {
    }

    public static Optional<Account> findByUsername(Connection c, String username) throws SQLException {
        return Jdbc.one(c, "SELECT id, username, full_name, role, password_hash FROM users WHERE username = ?",
                rs -> new Account(new User(rs.getInt("id"), rs.getString("username"), rs.getString("full_name"), rs.getString("role")),
                        rs.getString("password_hash")),
                username);
    }
}
