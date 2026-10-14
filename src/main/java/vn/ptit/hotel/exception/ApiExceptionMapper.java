package vn.ptit.hotel.exception;

import java.util.Map;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ApiExceptionMapper implements ExceptionMapper<ApiException> {
    @Override
    public Response toResponse(ApiException e) {
        return json(e.status(), e.getMessage());
    }

    public static Response json(int status, String message) {
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON_TYPE.withCharset("UTF-8"))
                .entity(Map.of("error", message))
                .build();
    }
}
