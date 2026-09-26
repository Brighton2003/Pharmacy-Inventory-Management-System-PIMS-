-- =====================================================================
--  Pharmacy Inventory Management System (PIMS)
--  HealthFirst Pharmacy - MySQL database script
--  Run this file in MySQL Workbench / phpMyAdmin BEFORE starting the app.
-- =====================================================================

DROP DATABASE IF EXISTS pims_db;
CREATE DATABASE pims_db CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE pims_db;

-- ------------------------- Table 1: users ----------------------------
CREATE TABLE users (
    user_id    INT AUTO_INCREMENT PRIMARY KEY,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,          -- SHA-256 hash
    role       ENUM('Admin','Cashier') NOT NULL DEFAULT 'Cashier',
    full_name  VARCHAR(100) NOT NULL
) ENGINE=InnoDB;

-- ----------------------- Table 2: suppliers --------------------------
CREATE TABLE suppliers (
    supplier_id    INT AUTO_INCREMENT PRIMARY KEY,
    name           VARCHAR(100) NOT NULL,
    contact_person VARCHAR(100),
    phone          VARCHAR(20),
    email          VARCHAR(100),
    address        TEXT
) ENGINE=InnoDB;

-- ----------------------- Table 3: medicines --------------------------
CREATE TABLE medicines (
    medicine_id       INT AUTO_INCREMENT PRIMARY KEY,
    name              VARCHAR(150) NOT NULL,
    company           VARCHAR(100),
    medicine_type     VARCHAR(50),
    price             DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    quantity_in_stock INT NOT NULL DEFAULT 0,
    reorder_level     INT NOT NULL DEFAULT 10,
    expiry_date       DATE NOT NULL,
    supplier_id       INT,
    CONSTRAINT fk_med_supplier FOREIGN KEY (supplier_id)
        REFERENCES suppliers(supplier_id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ------------------------- Table 4: sales ----------------------------
CREATE TABLE sales (
    sale_id      INT AUTO_INCREMENT PRIMARY KEY,
    sale_date    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    user_id      INT NOT NULL,
    CONSTRAINT fk_sale_user FOREIGN KEY (user_id)
        REFERENCES users(user_id) ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ----------------------- Table 5: sale_items -------------------------
CREATE TABLE sale_items (
    sale_item_id  INT AUTO_INCREMENT PRIMARY KEY,
    sale_id       INT NOT NULL,
    medicine_id   INT NOT NULL,
    quantity_sold INT NOT NULL,
    price_at_sale DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_item_sale FOREIGN KEY (sale_id)
        REFERENCES sales(sale_id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_item_med FOREIGN KEY (medicine_id)
        REFERENCES medicines(medicine_id) ON UPDATE CASCADE
) ENGINE=InnoDB;

-- =====================================================================
--                          SAMPLE DATA
-- =====================================================================

-- Users (passwords are stored as SHA-256 hashes)
--   admin   / admin123
--   cashier / cash123
--   thabo   / thabo123
INSERT INTO users (username, password, role, full_name) VALUES
('admin',   SHA2('admin123',256), 'Admin',   'System Administrator'),
('cashier', SHA2('cash123',256),  'Cashier', 'Naledi Mokoena'),
('thabo',   SHA2('thabo123',256), 'Cashier', 'Thabo Dlamini');

-- Suppliers
INSERT INTO suppliers (name, contact_person, phone, email, address) VALUES
('MediSupply SA',        'Johan van Wyk', '011 555 0101', 'orders@medisupply.co.za',  '12 Industrial Rd, Germiston, Gauteng'),
('PharmaDirect Wholesale','Ayesha Patel',  '011 555 0202', 'sales@pharmadirect.co.za', '88 Commissioner St, Johannesburg'),
('Cape Health Distributors','Sipho Ndlovu','021 555 0303', 'info@capehealth.co.za',    '5 Dock Road, Cape Town'),
('Zenith Medical Supplies','Lerato Mashaba','012 555 0404','support@zenithmed.co.za',  '30 Church St, Pretoria');

-- Medicines (expiry dates are relative to today so the Expiry Report always works)
INSERT INTO medicines (name, company, medicine_type, price, quantity_in_stock, reorder_level, expiry_date, supplier_id) VALUES
('Panado 500mg',          'Adcock Ingram', 'Tablet',    24.99, 180,  40, DATE_ADD(CURDATE(), INTERVAL 400 DAY), 1),
('Grandpa Headache Powder','Adcock Ingram','Tablet',    18.50,  95,  30, DATE_ADD(CURDATE(), INTERVAL 250 DAY), 1),
('Amoxicillin 250mg',     'Aspen Pharma',  'Capsule',   89.00,  12,  25, DATE_ADD(CURDATE(), INTERVAL 180 DAY), 2),
('Benylin Cough Syrup',   'Johnson & Johnson','Syrup',  76.50,  48,  15, DATE_ADD(CURDATE(), INTERVAL  20 DAY), 2),
('Voltaren Emulgel',      'Novartis',      'Cream',    129.99,  22,  10, DATE_ADD(CURDATE(), INTERVAL 300 DAY), 3),
('Insulin Actrapid',      'Novo Nordisk',  'Injection',345.00,   6,  10, DATE_ADD(CURDATE(), INTERVAL  25 DAY), 3),
('Allergex 4mg',          'Aspen Pharma',  'Tablet',    39.95, 140,  35, DATE_ADD(CURDATE(), INTERVAL 500 DAY), 1),
('Betadine Ointment',     'Mundipharma',   'Ointment',  64.00,  31,  12, DATE_ADD(CURDATE(), INTERVAL 220 DAY), 4),
('Corenza C',             'Adcock Ingram', 'Tablet',    55.75,   8,  20, DATE_ADD(CURDATE(), INTERVAL  15 DAY), 1),
('Rehidrat Sachets',      'Pharma Dynamics','Syrup',     32.40,  70,  20, DATE_ADD(CURDATE(), INTERVAL 365 DAY), 4),
('Vitamin C 1000mg',      'Bettaway',      'Tablet',    45.00, 210,  50, DATE_ADD(CURDATE(), INTERVAL 600 DAY), 2),
('Eye Drops Optrex',      'Reckitt',       'Drops',     58.99,  17,  10, DATE_ADD(CURDATE(), INTERVAL  28 DAY), 3),
('Brufen 400mg',          'Abbott',        'Tablet',    67.20,  90,  25, DATE_ADD(CURDATE(), INTERVAL 330 DAY), 2),
('Bactroban Cream',       'GSK',           'Cream',    118.00,   9,  10, DATE_ADD(CURDATE(), INTERVAL 270 DAY), 4);

-- Sample sales so that the reports are not empty on first run
INSERT INTO sales (sale_date, total_amount, user_id) VALUES
(DATE_SUB(NOW(), INTERVAL 9 DAY), 299.93, 2),
(DATE_SUB(NOW(), INTERVAL 5 DAY), 421.45, 2),
(DATE_SUB(NOW(), INTERVAL 2 DAY), 168.94, 3),
(DATE_SUB(NOW(), INTERVAL 1 DAY), 690.00, 2);

INSERT INTO sale_items (sale_id, medicine_id, quantity_sold, price_at_sale) VALUES
(1, 1,  5,  24.99),
(1, 7,  2,  39.95),
(1, 10, 3,  32.40),
(2, 5,  2, 129.99),
(2, 13, 1,  67.20),
(2, 4,  1,  76.50),
(2, 2,  2,  18.50),
(3, 1,  2,  24.99),
(3, 11, 1,  45.00),
(3, 8,  1,  64.00),
(4, 6,  2, 345.00);

-- =====================================================================
--  Handy verification queries (optional)
-- =====================================================================
-- SELECT * FROM users;
-- SELECT * FROM medicines WHERE quantity_in_stock <= reorder_level;
-- SELECT * FROM medicines WHERE expiry_date <= DATE_ADD(CURDATE(), INTERVAL 1 MONTH);
