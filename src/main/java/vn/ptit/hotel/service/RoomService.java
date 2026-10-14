package vn.ptit.hotel.service;

import java.time.LocalDate;
import java.util.List;
import vn.ptit.hotel.config.Env;
import vn.ptit.hotel.dao.RoomDao;
import vn.ptit.hotel.exception.ApiException;
import vn.ptit.hotel.model.Page;
import vn.ptit.hotel.model.Paging;
import vn.ptit.hotel.model.Room;
import vn.ptit.hotel.model.RoomStatus;

public final class RoomService {
    static final List<String> TYPES = List.of("STANDARD", "DELUXE", "SUITE");
    private static final String NOT_FOUND = "Không tìm thấy phòng";

    private RoomService() {
    }

    public static Page<Room> list(String type, Paging paging) {
        if (type != null) {
            Check.oneOf(type, TYPES, "Loại phòng");
        }
        return Tx.run(c -> Page.of(RoomDao.list(c, type, paging), paging, RoomDao.count(c, type)));
    }

    public static Room get(int id) {
        return Tx.run(c -> Check.found(RoomDao.findById(c, id), NOT_FOUND));
    }

    public static List<Room> available(LocalDate checkIn, LocalDate checkOut, String type) {
        Check.that(checkIn != null && checkOut != null, "Cần nhập checkIn và checkOut");
        Check.that(checkOut.isAfter(checkIn), "Ngày trả phòng phải sau ngày nhận phòng");
        if (type != null) {
            Check.oneOf(type, TYPES, "Loại phòng");
        }
        return Tx.run(c -> RoomDao.available(c, checkIn, checkOut, type));
    }

    public static List<RoomStatus> status(LocalDate date) {
        LocalDate day = date == null ? Env.today() : date;
        return Tx.run(c -> RoomDao.statusOn(c, day, day.isBefore(Env.today())));
    }

    public static Room create(Room input) {
        Room room = validate(input);
        return Tx.run(c -> {
            if (RoomDao.numberTaken(c, room.roomNumber(), -1)) {
                throw ApiException.conflict("Số phòng " + room.roomNumber() + " đã tồn tại");
            }
            return RoomDao.findById(c, RoomDao.insert(c, room)).orElseThrow();
        });
    }

    public static Room update(int id, Room input) {
        Room room = validate(input);
        return Tx.run(c -> {
            Check.found(RoomDao.findById(c, id), NOT_FOUND);
            if (RoomDao.numberTaken(c, room.roomNumber(), id)) {
                throw ApiException.conflict("Số phòng " + room.roomNumber() + " đã tồn tại");
            }
            RoomDao.update(c, id, room);
            return RoomDao.findById(c, id).orElseThrow();
        });
    }

    public static void delete(int id) {
        Tx.run(c -> {
            Room room = Check.found(RoomDao.findById(c, id), NOT_FOUND);
            if (RoomDao.hasBookings(c, id)) {
                throw ApiException.conflict("Phòng " + room.roomNumber() + " đã có lịch sử đặt phòng nên không thể xóa");
            }
            RoomDao.delete(c, id);
            return null;
        });
    }

    private static Room validate(Room r) {
        Check.that(r != null, "Thiếu dữ liệu phòng");
        String number = Check.text(r.roomNumber(), "Số phòng");
        String type = Check.oneOf(r.type() == null ? null : r.type().trim().toUpperCase(), TYPES, "Loại phòng");
        Check.that(r.pricePerNight() != null && r.pricePerNight() > 0, "Giá phòng phải lớn hơn 0");
        Check.that(r.capacity() != null && r.capacity() > 0, "Sức chứa phải lớn hơn 0");
        return new Room(null, number, type, r.pricePerNight(), r.capacity(), Check.optional(r.description()));
    }
}
