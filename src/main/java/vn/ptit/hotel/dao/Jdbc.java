package vn.ptit.hotel.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class Jdbc {
    @FunctionalInterface
    public interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    private Jdbc() {
    }

    public static <T> List<T> list(Connection c, String sql, RowMapper<T> mapper, Object... params) throws SQLException {
        try (PreparedStatement ps = prepare(c, sql, params); ResultSet rs = ps.executeQuery()) {
            List<T> rows = new ArrayList<>();
            while (rs.next()) {
                rows.add(mapper.map(rs));
            }
            return rows;
        }
    }

    public static <T> Optional<T> one(Connection c, String sql, RowMapper<T> mapper, Object... params) throws SQLException {
        List<T> rows = list(c, sql, mapper, params);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    public static long count(Connection c, String sql, Object... params) throws SQLException {
        return one(c, sql, rs -> rs.getLong(1), params).orElse(0L);
    }

    public static int update(Connection c, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = prepare(c, sql, params)) {
            return ps.executeUpdate();
        }
    }

    public static int insert(Connection c, String sql, Object... params) throws SQLException {
        return one(c, sql + " RETURNING id", rs -> rs.getInt(1), params).orElseThrow();
    }

    private static PreparedStatement prepare(Connection c, String sql, Object[] params) throws SQLException {
        PreparedStatement ps = c.prepareStatement(sql);
        try {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            return ps;
        } catch (SQLException e) {
            ps.close();
            throw e;
        }
    }
}
