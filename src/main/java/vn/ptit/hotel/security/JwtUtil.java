package vn.ptit.hotel.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.time.Duration;
import java.time.Instant;
import vn.ptit.hotel.config.Env;
import vn.ptit.hotel.model.User;

public final class JwtUtil {
    private static final String ISSUER = "hotel-booking-api";
    private static final Duration TTL = Duration.ofHours(8);
    private static final Algorithm ALGORITHM = Algorithm.HMAC256(Env.require("JWT_SECRET"));
    private static final JWTVerifier VERIFIER = JWT.require(ALGORITHM).withIssuer(ISSUER).build();

    private JwtUtil() {
    }

    public static String issue(User user) {
        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(user.username())
                .withClaim("uid", user.id())
                .withClaim("name", user.fullName())
                .withClaim("role", user.role())
                .withExpiresAt(Instant.now().plus(TTL))
                .sign(ALGORITHM);
    }

    public static User verify(String token) throws JWTVerificationException {
        DecodedJWT jwt = VERIFIER.verify(token);
        return new User(jwt.getClaim("uid").asInt(), jwt.getSubject(), jwt.getClaim("name").asString(), jwt.getClaim("role").asString());
    }
}
