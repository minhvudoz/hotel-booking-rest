package vn.ptit.hotel.service;

import java.sql.Connection;
import java.sql.SQLException;
import vn.ptit.hotel.config.Database;
import vn.ptit.hotel.exception.ApiException;

public final class Tx {
    @FunctionalInterface
    public interface Work<T> {
        T run(Connection c) throws SQLException;
    }

    private Tx() {
    }

    public static <T> T run(Work<T> work) {
        try (Connection c = Database.connection()) {
            c.setAutoCommit(false);
            try {
                T result = work.run(c);
                c.commit();
                return result;
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw translate(e);
        }
    }

    private static RuntimeException translate(SQLException e) {
        if ("23505".equals(e.getSQLState())) {
            return ApiException.conflict("Dữ liệu bị trùng với bản ghi đã có");
        }
        if ("23503".equals(e.getSQLState())) {
            return ApiException.conflict("Dữ liệu đang được sử dụng ở nơi khác");
        }
        return new IllegalStateException("Lỗi truy cập CSDL", e);
    }
}
