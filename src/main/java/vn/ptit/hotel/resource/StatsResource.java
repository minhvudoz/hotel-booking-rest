package vn.ptit.hotel.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import vn.ptit.hotel.model.DailyOccupancy;
import vn.ptit.hotel.model.MonthlyRevenue;
import vn.ptit.hotel.model.Overview;
import vn.ptit.hotel.security.Secured;
import vn.ptit.hotel.service.StatsService;

@Path("/stats")
@Produces(MediaType.APPLICATION_JSON)
@Secured("ADMIN")
@Tag(name = "Thống kê")
public class StatsResource {

    @GET
    @Path("/overview")
    @Operation(summary = "Phòng trống, đang ở, sắp nhận, sắp trả trong ngày (mặc định hôm nay)")
    public Overview overview(@QueryParam("date") String date) {
        return StatsService.overview(Params.date(date, "date"));
    }

    @GET
    @Path("/revenue")
    @Operation(summary = "Doanh thu 12 tháng của một năm (mặc định năm nay)")
    public List<MonthlyRevenue> revenue(@QueryParam("year") Integer year) {
        return StatsService.revenue(year);
    }

    @GET
    @Path("/occupancy")
    @Operation(summary = "Công suất phòng theo ngày (mặc định 30 ngày gần nhất)")
    public List<DailyOccupancy> occupancy(@QueryParam("from") String from, @QueryParam("to") String to) {
        return StatsService.occupancy(Params.date(from, "from"), Params.date(to, "to"));
    }
}
