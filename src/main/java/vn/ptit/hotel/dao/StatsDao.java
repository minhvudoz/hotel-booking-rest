package vn.ptit.hotel.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import vn.ptit.hotel.model.DailyOccupancy;
import vn.ptit.hotel.model.MonthlyRevenue;
import vn.ptit.hotel.model.Overview;

public final class StatsDao {
    private StatsDao() {
    }

    /** pastDate = true thì đơn CHECKED_OUT cũng được tính là đã chiếm phòng trong ngày đó. */
    public static Overview overview(Connection c, LocalDate d, boolean pastDate) throws SQLException {
        return Jdbc.one(c, """
                SELECT (SELECT count(*) FROM rooms) AS total,
                  count(DISTINCT room_id) FILTER (WHERE check_in <= ? AND ? < check_out
                      AND (status IN ('BOOKED', 'CHECKED_IN') OR (status = 'CHECKED_OUT' AND ?))) AS taken,
                  count(DISTINCT room_id) FILTER (WHERE check_in <= ? AND ? < check_out
                      AND (status = 'CHECKED_IN' OR (status = 'CHECKED_OUT' AND ?))) AS occupied,
                  count(*) FILTER (WHERE status = 'BOOKED' AND check_in = ?) AS arrivals,
                  count(*) FILTER (WHERE status = 'CHECKED_IN' AND check_out = ?) AS departures
                FROM bookings""",
                rs -> new Overview(d, rs.getInt("total"), rs.getInt("total") - rs.getInt("taken"), rs.getInt("occupied"),
                        rs.getInt("arrivals"), rs.getInt("departures")),
                d, d, pastDate, d, d, pastDate, d, d).orElseThrow();
    }

    public static List<MonthlyRevenue> revenue(Connection c, int year) throws SQLException {
        Map<Integer, Long> byMonth = Jdbc.list(c, """
                SELECT EXTRACT(MONTH FROM paid_at)::int AS month, SUM(amount) AS revenue
                FROM payments WHERE paid_at >= ? AND paid_at < ? GROUP BY 1""",
                rs -> Map.entry(rs.getInt("month"), rs.getLong("revenue")),
                LocalDate.of(year, 1, 1).atStartOfDay(), LocalDate.of(year + 1, 1, 1).atStartOfDay())
                .stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return IntStream.rangeClosed(1, 12).mapToObj(m -> new MonthlyRevenue(m, byMonth.getOrDefault(m, 0L))).toList();
    }

    public static List<DailyOccupancy> occupancy(Connection c, LocalDate from, LocalDate to, LocalDate today) throws SQLException {
        int total = (int) Jdbc.count(c, "SELECT count(*) FROM rooms");
        return Jdbc.list(c, """
                SELECT d::date AS day, count(DISTINCT b.room_id) AS occupied
                FROM generate_series(?::date, ?::date, interval '1 day') AS d
                LEFT JOIN bookings b ON b.check_in <= d::date AND d::date < b.check_out
                     AND (b.status IN ('BOOKED', 'CHECKED_IN') OR (b.status = 'CHECKED_OUT' AND d::date < ?))
                GROUP BY d ORDER BY d""",
                rs -> {
                    int occupied = rs.getInt("occupied");
                    double rate = total == 0 ? 0 : Math.round(occupied * 1000.0 / total) / 10.0;
                    return new DailyOccupancy(rs.getObject("day", LocalDate.class), occupied, total, rate);
                },
                from, to, today);
    }
}
