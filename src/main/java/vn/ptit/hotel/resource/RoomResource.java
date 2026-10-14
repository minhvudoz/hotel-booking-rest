package vn.ptit.hotel.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import vn.ptit.hotel.model.Page;
import vn.ptit.hotel.model.Paging;
import vn.ptit.hotel.model.Room;
import vn.ptit.hotel.model.RoomStatus;
import vn.ptit.hotel.security.Secured;
import vn.ptit.hotel.service.RoomService;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Secured
@Tag(name = "Phòng")
public class RoomResource {

    @GET
    @Operation(summary = "Danh sách phòng (lọc theo loại, phân trang)")
    public Page<Room> list(@QueryParam("type") String type,
                           @QueryParam("page") @DefaultValue("1") int page,
                           @QueryParam("size") @DefaultValue("10") int size) {
        return RoomService.list(Params.upper(type), Paging.of(page, size));
    }

    @GET
    @Path("/{id: \\d+}")
    @Operation(summary = "Chi tiết một phòng")
    public Room get(@PathParam("id") int id) {
        return RoomService.get(id);
    }

    @GET
    @Path("/available")
    @Operation(summary = "Phòng còn trống trong khoảng ngày [checkIn, checkOut)")
    public List<Room> available(@QueryParam("checkIn") String checkIn,
                                @QueryParam("checkOut") String checkOut,
                                @QueryParam("type") String type) {
        return RoomService.available(Params.date(checkIn, "checkIn"), Params.date(checkOut, "checkOut"), Params.upper(type));
    }

    @GET
    @Path("/status")
    @Operation(summary = "Trạng thái từng phòng trong một ngày (mặc định hôm nay)")
    public List<RoomStatus> status(@QueryParam("date") String date) {
        return RoomService.status(Params.date(date, "date"));
    }

    @POST
    @Secured("ADMIN")
    @Operation(summary = "Thêm phòng (chỉ admin)")
    public Response create(Room room) {
        return Params.created(RoomService.create(room));
    }

    @PUT
    @Path("/{id: \\d+}")
    @Secured("ADMIN")
    @Operation(summary = "Sửa phòng (chỉ admin)")
    public Room update(@PathParam("id") int id, Room room) {
        return RoomService.update(id, room);
    }

    @DELETE
    @Path("/{id: \\d+}")
    @Secured("ADMIN")
    @Operation(summary = "Xóa phòng chưa có đơn nào (chỉ admin)")
    public Response delete(@PathParam("id") int id) {
        RoomService.delete(id);
        return Response.noContent().build();
    }
}
