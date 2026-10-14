INSERT INTO users (username, password_hash, full_name, role) VALUES
    ('admin', '$2a$10$yn3MbORy43P8YImuQEglL.QNx1IdYVtIMmfknuObEElUr.hfy1UXW', 'Quản trị viên', 'ADMIN'),
    ('letan', '$2a$10$IekSZQ0fSCxrVVBWxcMbIOX1rmC4nPbbALHDSNMluRmXRpiASBq9O', 'Lễ tân ca sáng', 'RECEPTIONIST');

INSERT INTO rooms (room_number, type, price_per_night, capacity, description) VALUES
    ('101', 'STANDARD',  500000, 2, 'Giường đôi, cửa sổ hướng phố'),
    ('102', 'STANDARD',  500000, 2, 'Hai giường đơn'),
    ('103', 'STANDARD',  550000, 2, 'Giường đôi, ban công nhỏ'),
    ('104', 'STANDARD',  550000, 3, 'Giường đôi và một giường đơn'),
    ('105', 'STANDARD',  500000, 2, 'Giường đôi, gần thang máy'),
    ('201', 'DELUXE',    850000, 2, 'Giường king, bồn tắm'),
    ('202', 'DELUXE',    850000, 2, 'Giường king, hướng hồ'),
    ('203', 'DELUXE',    900000, 3, 'Phòng rộng, sofa bed'),
    ('204', 'DELUXE',    900000, 3, 'Ban công lớn, hướng hồ'),
    ('301', 'SUITE',    1500000, 4, 'Phòng khách riêng, bếp nhỏ'),
    ('302', 'SUITE',    1600000, 4, 'Hai phòng ngủ, view toàn thành phố'),
    ('303', 'SUITE',    1800000, 4, 'Suite tổng thống, bồn sục');

INSERT INTO amenities (name, unit_price, unit) VALUES
    ('Bữa sáng buffet',  150000, 'suất'),
    ('Giặt là',           50000, 'kg'),
    ('Spa - massage',    400000, 'lượt'),
    ('Đưa đón sân bay',  300000, 'lượt'),
    ('Minibar',           30000, 'món'),
    ('Thuê xe máy',      150000, 'ngày');

INSERT INTO customers (full_name, phone, id_card, email) VALUES
    ('Nguyễn Văn An',    '0901234561', '001090000001', 'an.nguyen@example.com'),
    ('Trần Thị Bình',    '0901234562', '001090000002', 'binh.tran@example.com'),
    ('Lê Hoàng Cường',   '0901234563', '001090000003', NULL),
    ('Phạm Thu Dung',    '0901234564', '001090000004', 'dung.pham@example.com'),
    ('Hoàng Minh Đức',   '0901234565', '001090000005', NULL),
    ('Vũ Thị Hà',        '0901234566', '001090000006', 'ha.vu@example.com'),
    ('Đặng Quốc Huy',    '0901234567', '001090000007', NULL),
    ('Bùi Thanh Hương',  '0901234568', '001090000008', 'huong.bui@example.com'),
    ('Ngô Đức Khải',     '0901234569', '001090000009', NULL),
    ('Đỗ Thị Lan',       '0901234570', '001090000010', 'lan.do@example.com'),
    ('Hồ Văn Minh',      '0901234571', '001090000011', NULL),
    ('Dương Thị Ngọc',   '0901234572', '001090000012', 'ngoc.duong@example.com'),
    ('Lý Gia Phúc',      '0901234573', '001090000013', NULL),
    ('Mai Anh Quân',     '0901234574', '001090000014', 'quan.mai@example.com'),
    ('Trịnh Thu Trang',  '0901234575', '001090000015', NULL);

-- Lịch đặt phòng từ 90 ngày trước đến 14 ngày sau, tính theo CURRENT_DATE nên dashboard luôn có số liệu gần nhất
DO $$
DECLARE
    r          RECORD;
    d          DATE;
    nights     INT;
    i          INT;
    st         VARCHAR(20);
    bid        INT;
    services   BIGINT;
    cust_count INT;
BEGIN
    SELECT count(*) INTO cust_count FROM customers;
    FOR r IN SELECT id, price_per_night FROM rooms ORDER BY id LOOP
        d := CURRENT_DATE - 90 + (r.id % 4);
        i := 0;
        WHILE d <= CURRENT_DATE + 14 LOOP
            i := i + 1;
            nights := 1 + (r.id * 3 + i * 5) % 4;
            IF (r.id + i) % 9 = 0 THEN
                st := 'CANCELLED';
            ELSIF d + nights <= CURRENT_DATE THEN
                st := 'CHECKED_OUT';
            ELSIF d = CURRENT_DATE AND r.id % 3 = 0 THEN
                st := 'BOOKED';
            ELSIF d <= CURRENT_DATE THEN
                st := 'CHECKED_IN';
            ELSE
                st := 'BOOKED';
            END IF;

            INSERT INTO bookings (room_id, customer_id, check_in, check_out, status, room_total, created_by, created_at)
            VALUES (r.id, 1 + (r.id * 7 + i) % cust_count, d, d + nights, st, nights * r.price_per_night,
                    'letan', (d - 3)::timestamp + time '09:00')
            RETURNING id INTO bid;

            IF st IN ('CHECKED_IN', 'CHECKED_OUT') AND (r.id + i) % 2 = 0 THEN
                INSERT INTO booking_services (booking_id, amenity_id, quantity, unit_price, used_at)
                SELECT bid, a.id, 1 + i % 3, a.unit_price, d::timestamp + time '08:00'
                FROM amenities a WHERE a.id = 1 + (r.id + i) % 6;
            END IF;

            IF st = 'CHECKED_OUT' THEN
                SELECT COALESCE(SUM(quantity * unit_price), 0) INTO services FROM booking_services WHERE booking_id = bid;
                INSERT INTO payments (booking_id, amount, method, paid_at)
                VALUES (bid, nights * r.price_per_night + services,
                        CASE WHEN i % 2 = 0 THEN 'CASH' ELSE 'TRANSFER' END,
                        (d + nights)::timestamp + time '11:00');
            END IF;

            d := d + nights + (r.id + i * 2) % 4;
        END LOOP;
    END LOOP;
END $$;
