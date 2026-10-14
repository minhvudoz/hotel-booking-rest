package vn.ptit.hotel.config;

import java.time.LocalDate;
import java.time.ZoneId;

public final class Env {
    public static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private Env() {
    }

    public static String require(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Thiếu biến môi trường " + name);
        }
        return value;
    }

    public static LocalDate today() {
        return LocalDate.now(ZONE);
    }
}
