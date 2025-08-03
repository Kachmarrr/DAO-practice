CREATE DATABASE bankSystem;

GRANT ALL PRIVILEGES ON DATABASE bankSystem TO postgres;

CREATE SEQUENCE customer_seq start with 10000;

CREATE TABLE bank
(
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

CREATE TABLE customer
(
    id         BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(50),
    last_name  VARCHAR(50),
    email      VARCHAR(50),
    balance    DECIMAL(10, 2),
    bank_id    BIGINT,
    FOREIGN KEY (bank_id) REFERENCES bank (id)
);

CREATE TABLE transaction
(
    id          BIGSERIAL PRIMARY KEY,
    amount      DECIMAL(10, 2) NOT NULL,
    customer_id BIGINT         NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES customer (id)
);

Insert INTO bank (id, name)
values (1, 'monobank'),
       (2, 'oshchadbank');

INSERT INTO customer (first_name, last_name, email, balance, bank_id)
VALUES ('Andrii', 'Kachmar', 'andrii.kachmar@example.com', 1500.50, 1),
       ('Olha', 'Shevchenko', 'olha.shevchenko@example.com', 2450.75, 2),
       ('Maksym', 'Koval', 'maksym.koval@example.com', 320.00, 2),
       ('Iryna', 'Bondar', 'iryna.bondar@example.com', 5000.00, 2),
       ('Taras', 'Melnyk', 'taras.melnyk@example.com', 890.20, 2),
       ('Oleh', 'Petrenko', 'oleh.petrenko@example.com', 1200.00, 2),
       ('Kateryna', 'Vasylenko', 'kateryna.vasylenko@example.com', 780.50, 2),
       ('Yurii', 'Danylenko', 'yurii.danylenko@example.com', 950.10, 2),
       ('Viktoria', 'Horbach', 'viktoria.horbach@example.com', 4300.00, 1),
       ('Serhii', 'Moroz', 'serhii.moroz@example.com', 1050.75, 1),
       ('Dmytro', 'Tkachenko', 'dmytro.tkachenko@example.com', 500.00, 1),
       ('Anastasiia', 'Kravchenko', 'anastasiia.kravchenko@example.com', 2700.20, 1),
       ('Roman', 'Lysenko', 'roman.lysenko@example.com', 150.00, 1),
       ('Nadiia', 'Savchenko', 'nadiia.savchenko@example.com', 1999.99, 1),
       ('Oleksandr', 'Kuzmenko', 'oleksandr.kuzmenko@example.com', 3500.50, 1),
       ('Svitlana', 'Rudenko', 'svitlana.rudenko@example.com', 120.40, 1),
       ('Petro', 'Levchenko', 'petro.levchenko@example.com', 640.00, 1),
       ('Halyna', 'Boiko', 'halyna.boiko@example.com', 870.60, 1),
       ('Volodymyr', 'Klymenko', 'volodymyr.klymenko@example.com', 2150.00, 1),
       ('Lesia', 'Martynenko', 'lesia.martynenko@example.com', 990.00, 1);



-- INSERT INTO user (id, first_name, last_name, email, balance, bank_id)
-- values

