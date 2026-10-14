package vn.ptit.hotel.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {
    private static final Logger LOG = Logger.getLogger(GenericExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable e) {
        // Jersey có thể bọc lỗi của Jackson trong exception khác, nên phải lần theo chuỗi cause
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t instanceof JsonProcessingException) {
                return ApiExceptionMapper.json(400, "Dữ liệu JSON không hợp lệ hoặc sai định dạng");
            }
        }
        if (e instanceof WebApplicationException) {
            int status = ((WebApplicationException) e).getResponse().getStatus();
            return ApiExceptionMapper.json(status, switch (status) {
                case 404 -> "Không tìm thấy đường dẫn";
                case 405 -> "Phương thức HTTP không được hỗ trợ";
                case 415 -> "Content-Type phải là application/json";
                default -> "Yêu cầu không hợp lệ";
            });
        }
        LOG.log(Level.SEVERE, "Lỗi không mong muốn", e);
        return ApiExceptionMapper.json(500, "Đã xảy ra lỗi hệ thống, vui lòng thử lại");
    }
}
