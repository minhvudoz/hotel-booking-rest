package vn.ptit.hotel.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import vn.ptit.hotel.model.Amenity;
import vn.ptit.hotel.security.Secured;
import vn.ptit.hotel.service.AmenityService;

@Path("/amenities")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Secured
@Tag(name = "Dịch vụ")
public class AmenityResource {

    @GET
    @Operation(summary = "Danh mục dịch vụ")
    public List<Amenity> list() {
        return AmenityService.list();
    }

    @POST
    @Secured("ADMIN")
    @Operation(summary = "Thêm dịch vụ (chỉ admin)")
    public Response create(Amenity amenity) {
        return Params.created(AmenityService.create(amenity));
    }

    @PUT
    @Path("/{id: \\d+}")
    @Secured("ADMIN")
    @Operation(summary = "Sửa dịch vụ (chỉ admin)")
    public Amenity update(@PathParam("id") int id, Amenity amenity) {
        return AmenityService.update(id, amenity);
    }

    @DELETE
    @Path("/{id: \\d+}")
    @Secured("ADMIN")
    @Operation(summary = "Xóa dịch vụ chưa được sử dụng (chỉ admin)")
    public Response delete(@PathParam("id") int id) {
        AmenityService.delete(id);
        return Response.noContent().build();
    }
}
