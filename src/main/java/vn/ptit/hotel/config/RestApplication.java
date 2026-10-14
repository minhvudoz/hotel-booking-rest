package vn.ptit.hotel.config;

import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import java.util.Set;
import javax.ws.rs.ApplicationPath;
import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

@ApplicationPath("/api")
public class RestApplication extends ResourceConfig {
    public RestApplication() {
        // Tắt auto-discovery để Jackson không tự đăng ký exception mapper trả lỗi dạng text
        property(CommonProperties.FEATURE_AUTO_DISCOVERY_DISABLE, true);
        property(ServerProperties.WADL_FEATURE_DISABLE, true);
        packages("vn.ptit.hotel.resource", "vn.ptit.hotel.security", "vn.ptit.hotel.exception");
        register(JacksonConfig.class);
        register(JacksonFeature.withoutExceptionMappers());
        register(new OpenApiResource().resourcePackages(Set.of("vn.ptit.hotel.resource")));
    }
}
