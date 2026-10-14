package vn.ptit.hotel.resource;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import vn.ptit.hotel.config.Database;

@Path("/health")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Hệ thống")
@OpenAPIDefinition(
        info = @Info(title = "Hotel Booking REST API", version = "1.0",
                description = "Dịch vụ web RESTful quản lý đặt phòng khách sạn, xây dựng bằng JAX-RS (Jersey) trên Tomcat"),
        servers = @Server(url = "/api"),
        security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class HealthResource {

    @GET
    @Operation(summary = "Kiểm tra tình trạng dịch vụ và CSDL")
    public Response health() {
        try (Connection c = Database.connection(); Statement st = c.createStatement()) {
            st.execute("SELECT 1");
            return Response.ok(Map.of("status", "UP", "database", "UP")).build();
        } catch (SQLException e) {
            return Response.status(503).entity(Map.of("status", "DOWN", "database", "DOWN")).build();
        }
    }
}
