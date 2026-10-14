package vn.ptit.hotel.model;

public record Paging(int page, int size) {
    public static Paging of(int page, int size) {
        return new Paging(Math.max(page, 1), Math.min(Math.max(size, 1), 100));
    }

    public int offset() {
        return (page - 1) * size;
    }
}
