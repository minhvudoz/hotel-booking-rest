package vn.ptit.hotel.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import vn.ptit.hotel.config.Env;
import vn.ptit.hotel.dao.StatsDao;
import vn.ptit.hotel.model.DailyOccupancy;
import vn.ptit.hotel.model.MonthlyRevenue;
import vn.ptit.hotel.model.Overview;

public final class StatsService {
    private StatsService() {
    }

    public static Overview overview(LocalDate date) {
        LocalDate day = date == null ? Env.today() : date;
        return Tx.run(c -> StatsDao.overview(c, day, day.isBefore(Env.today())));
    }

    public static List<MonthlyRevenue> revenue(Integer year) {
        int y = year == null ? Env.today().getYear() : year;
        Check.that(y >= 2000 && y <= 2100, "Năm phải nằm trong khoảng 2000–2100");
        return Tx.run(c -> StatsDao.revenue(c, y));
    }

    public static List<DailyOccupancy> occupancy(LocalDate from, LocalDate to) {
        LocalDate end = to == null ? Env.today() : to;
        LocalDate start = from == null ? end.minusDays(29) : from;
        Check.that(!start.isAfter(end), "Ngày bắt đầu phải trước hoặc bằng ngày kết thúc");
        Check.that(ChronoUnit.DAYS.between(start, end) < 366, "Chỉ xem được tối đa 366 ngày");
        return Tx.run(c -> StatsDao.occupancy(c, start, end, Env.today()));
    }
}
