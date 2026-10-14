package vn.ptit.hotel.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import vn.ptit.hotel.model.LoginRequest;
import vn.ptit.hotel.model.LoginResponse;
import vn.ptit.hotel.model.User;
import vn.ptit.hotel.security.Secured;
import vn.ptit.hotel.service.AuthService;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Xác thực")
public class AuthResource {

    @POST
    @Path("/login")
    @Operation(summary = "Đăng nhập và nhận JWT (hạn 8 giờ)")
    public LoginResponse login(LoginRequest request) {
        return AuthService.login(request);
    }

    @GET
    @Path("/me")
    @Secured
    @Operation(summary = "Thông tin người đang đăng nhập")
    public User me(@Context ContainerRequestContext request) {
        return Params.user(request);
    }
}
