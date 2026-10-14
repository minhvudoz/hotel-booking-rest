CREATE TABLE users (
    id            SERIAL PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    full_name     VARCHAR(100) NOT NULL,
    role          VARCHAR(20)  NOT NULL CHECK (role IN ('ADMIN', 'RECEPTIONIST'))
);

CREATE TABLE rooms (
    id              SERIAL PRIMARY KEY,
    room_number     VARCHAR(10) NOT NULL UNIQUE,
    type            VARCHAR(20) NOT NULL CHECK (type IN ('STANDARD', 'DELUXE', 'SUITE')),
    price_per_night BIGINT      NOT NULL CHECK (price_per_night > 0),
    capacity        INT         NOT NULL CHECK (capacity > 0),
    description     VARCHAR(500)
);

CREATE TABLE customers (
    id         SERIAL PRIMARY KEY,
    full_name  VARCHAR(100) NOT NULL,
    phone      VARCHAR(20)  NOT NULL UNIQUE,
    id_card    VARCHAR(20),
    email      VARCHAR(100),
    created_at TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE amenities (
    id         SERIAL PRIMARY KEY,
    name       VARCHAR(100) NOT NULL UNIQUE,
    unit_price BIGINT       NOT NULL CHECK (unit_price > 0),
    unit       VARCHAR(20)  NOT NULL
);

CREATE TABLE bookings (
    id          SERIAL PRIMARY KEY,
    room_id     INT         NOT NULL REFERENCES rooms (id),
    customer_id INT         NOT NULL REFERENCES customers (id),
    check_in    DATE        NOT NULL,
    check_out   DATE        NOT NULL,
    status      VARCHAR(20) NOT NULL CHECK (status IN ('BOOKED', 'CHECKED_IN', 'CHECKED_OUT', 'CANCELLED')),
    room_total  BIGINT      NOT NULL,
    created_by  VARCHAR(50) NOT NULL,
    created_at  TIMESTAMP   NOT NULL DEFAULT now(),
    CHECK (check_out > check_in)
);
CREATE INDEX idx_bookings_room_dates ON bookings (room_id, check_in, check_out);
CREATE INDEX idx_bookings_customer ON bookings (customer_id);

CREATE TABLE booking_services (
    id         SERIAL PRIMARY KEY,
    booking_id INT       NOT NULL REFERENCES bookings (id),
    amenity_id INT       NOT NULL REFERENCES amenities (id),
    quantity   INT       NOT NULL CHECK (quantity > 0),
    unit_price BIGINT    NOT NULL,
    used_at    TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE payments (
    id         SERIAL PRIMARY KEY,
    booking_id INT         NOT NULL UNIQUE REFERENCES bookings (id),
    amount     BIGINT      NOT NULL,
    method     VARCHAR(20) NOT NULL CHECK (method IN ('CASH', 'TRANSFER')),
    paid_at    TIMESTAMP   NOT NULL DEFAULT now()
);
