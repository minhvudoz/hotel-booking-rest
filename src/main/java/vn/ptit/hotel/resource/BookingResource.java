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
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import vn.ptit.hotel.model.Booking;
import vn.ptit.hotel.model.BookingRequest;
import vn.ptit.hotel.model.CheckoutRequest;
import vn.ptit.hotel.model.Invoice;
import vn.ptit.hotel.model.Page;
import vn.ptit.hotel.model.Paging;
import vn.ptit.hotel.model.ServiceUsage;
import vn.ptit.hotel.model.ServiceUsageRequest;
import vn.ptit.hotel.security.Secured;
import vn.ptit.hotel.service.BookingService;

@Path("/bookings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Secured
@Tag(name = "Đặt phòng")
public class BookingResource {

    @GET
    @Operation(summary = "Danh sách đơn (lọc trạng thái; q tìm theo tên/SĐT khách hoặc số phòng)")
    public Page<Booking> list(@QueryParam("status") String status,
                              @QueryParam("q") String q,
                              @QueryParam("page") @DefaultValue("1") int page,
                              @QueryParam("size") @DefaultValue("10") int size) {
        return BookingService.list(Params.upper(status), q, Paging.of(page, size));
    }

    @GET
    @Path("/{id: \\d+}")
    @Operation(summary = "Chi tiết đơn đặt phòng")
    public Booking get(@PathParam("id") int id) {
        return BookingService.get(id);
    }

    @POST
    @Operation(summary = "Đặt phòng (chặn trùng lịch)")
    public Response create(BookingRequest request, @Context ContainerRequestContext ctx) {
        return Params.created(BookingService.create(request, Params.user(ctx)));
    }

    @PUT
    @Path("/{id: \\d+}/cancel")
    @Operation(summary = "Hủy đơn đang ở trạng thái Đã đặt")
    public Booking cancel(@PathParam("id") int id) {
        return BookingService.cancel(id);
    }

    @PUT
    @Path("/{id: \\d+}/check-in")
    @Operation(summary = "Nhận phòng")
    public Booking checkIn(@PathParam("id") int id) {
        return BookingService.checkIn(id);
    }

    @PUT
    @Path("/{id: \\d+}/check-out")
    @Operation(summary = "Trả phòng và thanh toán (CASH hoặc TRANSFER), trả về hóa đơn")
    public Invoice checkOut(@PathParam("id") int id, CheckoutRequest request) {
        return BookingService.checkOut(id, request);
    }

    @GET
    @Path("/{id: \\d+}/services")
    @Operation(summary = "Dịch vụ khách đã dùng trong đơn")
    public List<ServiceUsage> services(@PathParam("id") int id) {
        return BookingService.services(id);
    }

    @POST
    @Path("/{id: \\d+}/services")
    @Operation(summary = "Ghi dịch vụ phát sinh (khách đang ở)")
    public Response addService(@PathParam("id") int id, ServiceUsageRequest request) {
        return Params.created(BookingService.addService(id, request));
    }

    @DELETE
    @Path("/{id: \\d+}/services/{serviceId: \\d+}")
    @Operation(summary = "Xóa dịch vụ ghi nhầm")
    public Response removeService(@PathParam("id") int id, @PathParam("serviceId") int serviceId) {
        BookingService.removeService(id, serviceId);
        return Response.noContent().build();
    }

    @GET
    @Path("/{id: \\d+}/invoice")
    @Operation(summary = "Hóa đơn: tiền phòng, dịch vụ, tổng cộng, thanh toán")
    public Invoice invoice(@PathParam("id") int id) {
        return BookingService.invoice(id);
    }
}
