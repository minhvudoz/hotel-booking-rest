package vn.ptit.hotel.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import javax.ws.rs.Consumes;
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
import vn.ptit.hotel.model.Booking;
import vn.ptit.hotel.model.Customer;
import vn.ptit.hotel.model.Page;
import vn.ptit.hotel.model.Paging;
import vn.ptit.hotel.security.Secured;
import vn.ptit.hotel.service.BookingService;
import vn.ptit.hotel.service.CustomerService;

@Path("/customers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Secured
@Tag(name = "Khách hàng")
public class CustomerResource {

    @GET
    @Operation(summary = "Tìm khách theo tên hoặc số điện thoại (phân trang)")
    public Page<Customer> search(@QueryParam("q") String q,
                                 @QueryParam("page") @DefaultValue("1") int page,
                                 @QueryParam("size") @DefaultValue("10") int size) {
        return CustomerService.search(q, Paging.of(page, size));
    }

    @GET
    @Path("/{id: \\d+}")
    @Operation(summary = "Chi tiết khách hàng")
    public Customer get(@PathParam("id") int id) {
        return CustomerService.get(id);
    }

    @GET
    @Path("/{id: \\d+}/bookings")
    @Operation(summary = "Lịch sử lưu trú của khách")
    public List<Booking> bookings(@PathParam("id") int id) {
        return BookingService.listByCustomer(id);
    }

    @POST
    @Operation(summary = "Thêm khách hàng")
    public Response create(Customer customer) {
        return Params.created(CustomerService.create(customer));
    }

    @PUT
    @Path("/{id: \\d+}")
    @Operation(summary = "Sửa thông tin khách hàng")
    public Customer update(@PathParam("id") int id, Customer customer) {
        return CustomerService.update(id, customer);
    }
}
