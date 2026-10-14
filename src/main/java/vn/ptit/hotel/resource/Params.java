package vn.ptit.hotel.resource;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import vn.ptit.hotel.exception.ApiException;
import vn.ptit.hotel.model.User;
import vn.ptit.hotel.security.AuthFilter;

final class Params {
    private Params() {
    }

    static LocalDate date(String value, String name) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException e) {
            throw ApiException.badRequest("Tham số " + name + " phải có dạng yyyy-MM-dd");
        }
    }

    static String upper(String value) {
        return value == null || value.isBlank() ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    static User user(ContainerRequestContext request) {
        return (User) request.getProperty(AuthFilter.USER);
    }

    static Response created(Object entity) {
        return Response.status(Response.Status.CREATED).entity(entity).build();
    }
}
