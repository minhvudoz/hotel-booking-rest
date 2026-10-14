package vn.ptit.hotel.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import java.util.Arrays;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;
import vn.ptit.hotel.exception.ApiException;
import vn.ptit.hotel.model.User;

@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthFilter implements ContainerRequestFilter {
    public static final String USER = "hotel.user";

    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext request) {
        String header = request.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            throw ApiException.unauthorized("Bạn cần đăng nhập để sử dụng chức năng này");
        }
        User user;
        try {
            user = JwtUtil.verify(header.substring("Bearer ".length()).trim());
        } catch (JWTVerificationException e) {
            throw ApiException.unauthorized("Phiên đăng nhập không hợp lệ hoặc đã hết hạn");
        }
        Secured rule = resourceInfo.getResourceMethod().getAnnotation(Secured.class);
        if (rule == null) {
            rule = resourceInfo.getResourceClass().getAnnotation(Secured.class);
        }
        if (!Arrays.asList(rule.value()).contains(user.role())) {
            throw ApiException.forbidden("Bạn không có quyền thực hiện thao tác này");
        }
        request.setProperty(USER, user);
    }
}
