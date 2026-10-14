package vn.ptit.hotel.service;

import java.util.regex.Pattern;
import vn.ptit.hotel.dao.CustomerDao;
import vn.ptit.hotel.exception.ApiException;
import vn.ptit.hotel.model.Customer;
import vn.ptit.hotel.model.Page;
import vn.ptit.hotel.model.Paging;

public final class CustomerService {
    private static final Pattern PHONE = Pattern.compile("^0\\d{9,10}$");
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final String NOT_FOUND = "Không tìm thấy khách hàng";

    private CustomerService() {
    }

    public static Page<Customer> search(String q, Paging paging) {
        String query = Check.optional(q);
        return Tx.run(c -> Page.of(CustomerDao.search(c, query, paging), paging, CustomerDao.count(c, query)));
    }

    public static Customer get(int id) {
        return Tx.run(c -> Check.found(CustomerDao.findById(c, id), NOT_FOUND));
    }

    public static Customer create(Customer input) {
        Customer customer = validate(input);
        return Tx.run(c -> {
            if (CustomerDao.phoneTaken(c, customer.phone(), -1)) {
                throw ApiException.conflict("Số điện thoại " + customer.phone() + " đã thuộc về khách hàng khác");
            }
            return CustomerDao.findById(c, CustomerDao.insert(c, customer)).orElseThrow();
        });
    }

    public static Customer update(int id, Customer input) {
        Customer customer = validate(input);
        return Tx.run(c -> {
            Check.found(CustomerDao.findById(c, id), NOT_FOUND);
            if (CustomerDao.phoneTaken(c, customer.phone(), id)) {
                throw ApiException.conflict("Số điện thoại " + customer.phone() + " đã thuộc về khách hàng khác");
            }
            CustomerDao.update(c, id, customer);
            return CustomerDao.findById(c, id).orElseThrow();
        });
    }

    private static Customer validate(Customer cu) {
        Check.that(cu != null, "Thiếu dữ liệu khách hàng");
        String name = Check.text(cu.fullName(), "Họ tên");
        String phone = Check.text(cu.phone(), "Số điện thoại");
        Check.that(PHONE.matcher(phone).matches(), "Số điện thoại phải bắt đầu bằng 0 và có 10–11 chữ số");
        String email = Check.optional(cu.email());
        Check.that(email == null || EMAIL.matcher(email).matches(), "Email không hợp lệ");
        return new Customer(null, name, phone, Check.optional(cu.idCard()), email, null);
    }
}
