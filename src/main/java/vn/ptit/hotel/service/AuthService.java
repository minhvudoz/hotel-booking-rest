package vn.ptit.hotel.service;

import org.mindrot.jbcrypt.BCrypt;
import vn.ptit.hotel.dao.UserDao;
import vn.ptit.hotel.exception.ApiException;
import vn.ptit.hotel.model.LoginRequest;
import vn.ptit.hotel.model.LoginResponse;
import vn.ptit.hotel.security.JwtUtil;

public final class AuthService {
    private AuthService() {
    }

    public static LoginResponse login(LoginRequest request) {
        Check.that(request != null && request.username() != null && request.password() != null,
                "Vui lòng nhập tên đăng nhập và mật khẩu");
        UserDao.Account account = Tx.run(c -> UserDao.findByUsername(c, request.username().trim())).orElse(null);
        if (account == null || !BCrypt.checkpw(request.password(), account.passwordHash())) {
            throw ApiException.unauthorized("Sai tên đăng nhập hoặc mật khẩu");
        }
        return new LoginResponse(JwtUtil.issue(account.user()), account.user());
    }
}
