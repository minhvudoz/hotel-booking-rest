package vn.ptit.hotel.model;

import java.util.List;

public record Page<T>(List<T> items, int page, int size, long total) {
    public static <T> Page<T> of(List<T> items, Paging paging, long total) {
        return new Page<>(items, paging.page(), paging.size(), total);
    }
}
