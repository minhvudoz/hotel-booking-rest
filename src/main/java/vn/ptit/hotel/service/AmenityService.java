package vn.ptit.hotel.service;

import java.util.List;
import vn.ptit.hotel.dao.AmenityDao;
import vn.ptit.hotel.exception.ApiException;
import vn.ptit.hotel.model.Amenity;

public final class AmenityService {
    private static final String NOT_FOUND = "Không tìm thấy dịch vụ";

    private AmenityService() {
    }

    public static List<Amenity> list() {
        return Tx.run(AmenityDao::list);
    }

    public static Amenity create(Amenity input) {
        Amenity amenity = validate(input);
        return Tx.run(c -> {
            if (AmenityDao.nameTaken(c, amenity.name(), -1)) {
                throw ApiException.conflict("Dịch vụ \"" + amenity.name() + "\" đã tồn tại");
            }
            return AmenityDao.findById(c, AmenityDao.insert(c, amenity)).orElseThrow();
        });
    }

    public static Amenity update(int id, Amenity input) {
        Amenity amenity = validate(input);
        return Tx.run(c -> {
            Check.found(AmenityDao.findById(c, id), NOT_FOUND);
            if (AmenityDao.nameTaken(c, amenity.name(), id)) {
                throw ApiException.conflict("Dịch vụ \"" + amenity.name() + "\" đã tồn tại");
            }
            AmenityDao.update(c, id, amenity);
            return AmenityDao.findById(c, id).orElseThrow();
        });
    }

    public static void delete(int id) {
        Tx.run(c -> {
            Amenity amenity = Check.found(AmenityDao.findById(c, id), NOT_FOUND);
            if (AmenityDao.isUsed(c, id)) {
                throw ApiException.conflict("Dịch vụ \"" + amenity.name() + "\" đã được sử dụng trong đơn đặt phòng nên không thể xóa");
            }
            AmenityDao.delete(c, id);
            return null;
        });
    }

    private static Amenity validate(Amenity a) {
        Check.that(a != null, "Thiếu dữ liệu dịch vụ");
        String name = Check.text(a.name(), "Tên dịch vụ");
        Check.that(a.unitPrice() != null && a.unitPrice() > 0, "Đơn giá phải lớn hơn 0");
        return new Amenity(null, name, a.unitPrice(), Check.text(a.unit(), "Đơn vị tính"));
    }
}
