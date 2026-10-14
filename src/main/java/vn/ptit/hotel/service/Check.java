package vn.ptit.hotel.service;

import java.util.List;
import java.util.Optional;
import vn.ptit.hotel.exception.ApiException;

final class Check {
    private Check() {
    }

    static void that(boolean ok, String message) {
        if (!ok) {
            throw ApiException.badRequest(message);
        }
    }

    static String text(String value, String label) {
        that(value != null && !value.isBlank(), label + " không được để trống");
        return value.trim();
    }

    static String optional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    static String oneOf(String value, List<String> allowed, String label) {
        that(value != null && allowed.contains(value), label + " phải là một trong: " + String.join(", ", allowed));
        return value;
    }

    static <T> T found(Optional<T> value, String message) {
        return value.orElseThrow(() -> ApiException.notFound(message));
    }
}
