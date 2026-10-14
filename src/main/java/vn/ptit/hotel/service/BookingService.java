package vn.ptit.hotel.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import vn.ptit.hotel.config.Env;
import vn.ptit.hotel.dao.AmenityDao;
import vn.ptit.hotel.dao.BookingDao;
import vn.ptit.hotel.dao.CustomerDao;
import vn.ptit.hotel.dao.PaymentDao;
import vn.ptit.hotel.dao.RoomDao;
import vn.ptit.hotel.dao.ServiceUsageDao;
import vn.ptit.hotel.exception.ApiException;
import vn.ptit.hotel.model.Amenity;
import vn.ptit.hotel.model.Booking;
import vn.ptit.hotel.model.BookingRequest;
import vn.ptit.hotel.model.CheckoutRequest;
import vn.ptit.hotel.model.Invoice;
import vn.ptit.hotel.model.Page;
import vn.ptit.hotel.model.Paging;
import vn.ptit.hotel.model.Room;
import vn.ptit.hotel.model.ServiceUsage;
import vn.ptit.hotel.model.ServiceUsageRequest;
import vn.ptit.hotel.model.User;

public final class BookingService {
    static final List<String> STATUSES = List.of("BOOKED", "CHECKED_IN", "CHECKED_OUT", "CANCELLED");
    private static final Map<String, Set<String>> NEXT = Map.of(
            "BOOKED", Set.of("CHECKED_IN", "CANCELLED"),
            "CHECKED_IN", Set.of("CHECKED_OUT"));
    private static final DateTimeFormatter VN_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String NOT_FOUND = "Không tìm thấy đơn đặt phòng";

    private BookingService() {
    }

    public static Page<Booking> list(String status, String q, Paging paging) {
        if (status != null) {
            Check.oneOf(status, STATUSES, "Trạng thái");
        }
        String query = Check.optional(q);
        return Tx.run(c -> Page.of(BookingDao.list(c, status, query, paging), paging, BookingDao.count(c, status, query)));
    }

    public static Booking get(int id) {
        return Tx.run(c -> find(c, id));
    }

    public static List<Booking> listByCustomer(int customerId) {
        return Tx.run(c -> {
            Check.found(CustomerDao.findById(c, customerId), "Không tìm thấy khách hàng");
            return BookingDao.listByCustomer(c, customerId);
        });
    }

    public static Booking create(BookingRequest req, User user) {
        Check.that(req != null && req.roomId() != null && req.customerId() != null && req.checkIn() != null && req.checkOut() != null,
                "Cần nhập đủ roomId, customerId, checkIn, checkOut");
        Check.that(req.checkOut().isAfter(req.checkIn()), "Ngày trả phòng phải sau ngày nhận phòng");
        Check.that(!req.checkIn().isBefore(Env.today()), "Ngày nhận phòng không được ở quá khứ");
        return Tx.run(c -> {
            // Khóa dòng phòng để hai yêu cầu đặt cùng phòng không thể cùng vượt qua bước kiểm tra trùng lịch
            Room room = Check.found(RoomDao.lockById(c, req.roomId()), "Không tìm thấy phòng");
            Check.found(CustomerDao.findById(c, req.customerId()), "Không tìm thấy khách hàng");
            Optional<Booking> clash = BookingDao.findConflict(c, room.id(), req.checkIn(), req.checkOut());
            if (clash.isPresent()) {
                throw ApiException.conflict("Phòng " + room.roomNumber() + " đã có khách đặt từ "
                        + VN_DATE.format(clash.get().checkIn()) + " đến " + VN_DATE.format(clash.get().checkOut()));
            }
            long nights = ChronoUnit.DAYS.between(req.checkIn(), req.checkOut());
            int id = BookingDao.insert(c, room.id(), req.customerId(), req.checkIn(), req.checkOut(),
                    nights * room.pricePerNight(), user.username());
            return find(c, id);
        });
    }

    public static Booking cancel(int id) {
        return Tx.run(c -> {
            move(c, lock(c, id), "CANCELLED", "Chỉ hủy được đơn đang ở trạng thái Đã đặt");
            return find(c, id);
        });
    }

    public static Booking checkIn(int id) {
        return Tx.run(c -> {
            Booking b = lock(c, id);
            LocalDate today = Env.today();
            if ("BOOKED".equals(b.status()) && (today.isBefore(b.checkIn()) || !today.isBefore(b.checkOut()))) {
                throw ApiException.conflict("Chỉ nhận phòng được từ ngày " + VN_DATE.format(b.checkIn())
                        + " đến trước ngày " + VN_DATE.format(b.checkOut()));
            }
            move(c, b, "CHECKED_IN", "Chỉ nhận phòng được với đơn đang ở trạng thái Đã đặt");
            return find(c, id);
        });
    }

    public static Invoice checkOut(int id, CheckoutRequest req) {
        String method = Check.oneOf(req == null ? null : req.paymentMethod(), List.of("CASH", "TRANSFER"), "Hình thức thanh toán");
        return Tx.run(c -> {
            Booking b = lock(c, id);
            move(c, b, "CHECKED_OUT", "Chỉ trả phòng được khi khách đang ở");
            PaymentDao.insert(c, id, b.roomTotal() + ServiceUsageDao.sumByBooking(c, id), method);
            return invoice(c, id);
        });
    }

    public static List<ServiceUsage> services(int id) {
        return Tx.run(c -> {
            find(c, id);
            return ServiceUsageDao.listByBooking(c, id);
        });
    }

    public static ServiceUsage addService(int id, ServiceUsageRequest req) {
        Check.that(req != null && req.amenityId() != null, "Cần chọn dịch vụ (amenityId)");
        Check.that(req.quantity() != null && req.quantity() > 0, "Số lượng phải lớn hơn 0");
        return Tx.run(c -> {
            requireStaying(lock(c, id));
            Amenity amenity = Check.found(AmenityDao.findById(c, req.amenityId()), "Không tìm thấy dịch vụ");
            int usageId = ServiceUsageDao.insert(c, id, amenity.id(), req.quantity(), amenity.unitPrice());
            return ServiceUsageDao.findById(c, usageId).orElseThrow();
        });
    }

    public static void removeService(int id, int usageId) {
        Tx.run(c -> {
            requireStaying(lock(c, id));
            ServiceUsage usage = Check.found(ServiceUsageDao.findById(c, usageId).filter(u -> u.bookingId() == id),
                    "Không tìm thấy dịch vụ này trong đơn");
            ServiceUsageDao.delete(c, usage.id());
            return null;
        });
    }

    public static Invoice invoice(int id) {
        return Tx.run(c -> invoice(c, id));
    }

    private static Invoice invoice(Connection c, int id) throws SQLException {
        Booking b = find(c, id);
        List<ServiceUsage> services = ServiceUsageDao.listByBooking(c, id);
        long servicesTotal = services.stream().mapToLong(ServiceUsage::amount).sum();
        return new Invoice(b, b.roomTotal(), services, servicesTotal, b.roomTotal() + servicesTotal,
                PaymentDao.findByBooking(c, id).orElse(null));
    }

    private static Booking find(Connection c, int id) throws SQLException {
        return Check.found(BookingDao.findById(c, id), NOT_FOUND);
    }

    private static Booking lock(Connection c, int id) throws SQLException {
        return Check.found(BookingDao.lockById(c, id), NOT_FOUND);
    }

    private static void requireStaying(Booking b) {
        if (!"CHECKED_IN".equals(b.status())) {
            throw ApiException.conflict("Chỉ ghi dịch vụ khi khách đang ở (đã nhận phòng)");
        }
    }

    private static void move(Connection c, Booking b, String target, String message) throws SQLException {
        if (!NEXT.getOrDefault(b.status(), Set.of()).contains(target)) {
            throw ApiException.conflict(message);
        }
        BookingDao.updateStatus(c, b.id(), target);
    }
}
