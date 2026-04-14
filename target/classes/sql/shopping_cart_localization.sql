
CREATE DATABASE IF NOT EXISTS shopping_cart_localization
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE shopping_cart_localization;

CREATE TABLE IF NOT EXISTS cart_records (
    id INT AUTO_INCREMENT PRIMARY KEY,
    total_items INT NOT NULL,
    total_cost DOUBLE NOT NULL,
    language VARCHAR(10),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS cart_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    cart_record_id INT,
    item_number INT NOT NULL,
    price DOUBLE NOT NULL,
    quantity INT NOT NULL,
    subtotal DOUBLE NOT NULL,
    FOREIGN KEY (cart_record_id) REFERENCES cart_records(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS localization_strings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    `key` VARCHAR(100) NOT NULL,
    value VARCHAR(255) NOT NULL,
    language VARCHAR(10) NOT NULL
);

-- Seed UI strings (languages: en, sv, fi, ja, ar)

INSERT INTO localization_strings (`key`, value, language) VALUES
('headerTitle', 'Shopping Cart', 'en'),
('menuAddItem', 'Add item', 'en'),
('menuDisplayTotalPrice', 'Display total price', 'en'),
('menuExit', 'Exit', 'en'),
('enterPrice', 'Enter Price', 'en'),
('enterQuantity', 'Enter Quantity', 'en'),
('invalidInputError', 'Invalid Price or quantity, price must be greater or equal to zero, and quantity must be greater or equal to zero', 'en'),
('totalPrice', 'Total Price', 'en'),
('lineTotal', 'Line Total', 'en');

INSERT INTO localization_strings (`key`, value, language) VALUES
('headerTitle', 'Varukorg', 'sv'),
('menuAddItem', 'Lägg till artikel', 'sv'),
('menuDisplayTotalPrice', 'Visa totalpris', 'sv'),
('menuExit', 'Avsluta', 'sv'),
('enterPrice', 'Ange pris', 'sv'),
('enterQuantity', 'Ange antal', 'sv'),
('invalidInputError', 'Ogiltigt pris eller antal. Pris och antal måste vara större än eller lika med noll', 'sv'),
('totalPrice', 'Totalpris', 'sv'),
('lineTotal', 'Radsumma', 'sv');

INSERT INTO localization_strings (`key`, value, language) VALUES
('headerTitle', 'Ostoskori', 'fi'),
('menuAddItem', 'Lisää tuote', 'fi'),
('menuDisplayTotalPrice', 'Näytä kokonaishinta', 'fi'),
('menuExit', 'Poistu', 'fi'),
('enterPrice', 'Syötä hinta', 'fi'),
('enterQuantity', 'Syötä määrä', 'fi'),
('invalidInputError', 'Virheellinen hinta tai määrä. Hinnan ja määrän on oltava suurempi tai yhtä suuri kuin nolla', 'fi'),
('totalPrice', 'Kokonaishinta', 'fi'),
('lineTotal', 'Rivin summa', 'fi');

INSERT INTO localization_strings (`key`, value, language) VALUES
('headerTitle', 'ショッピングカート', 'ja'),
('menuAddItem', '商品を追加', 'ja'),
('menuDisplayTotalPrice', '合計金額を表示', 'ja'),
('menuExit', '終了', 'ja'),
('enterPrice', '価格を入力', 'ja'),
('enterQuantity', '数量を入力', 'ja'),
('invalidInputError', '価格または数量が無効です。価格は0以上、数量は1以上である必要があります', 'ja'),
('totalPrice', '合計金額', 'ja'),
('lineTotal', '行合計', 'ja');

INSERT INTO localization_strings (`key`, value, language) VALUES
('headerTitle', 'سلة التسوق', 'ar'),
('menuAddItem', 'إضافة عنصر', 'ar'),
('menuDisplayTotalPrice', 'عرض إجمالي السعر', 'ar'),
('menuExit', 'خروج', 'ar'),
('enterPrice', 'أدخل السعر', 'ar'),
('enterQuantity', 'أدخل الكمية', 'ar'),
('invalidInputError', 'سعر أو كمية غير صالحين، يجب أن يكون السعر أكبر من أو يساوي الصفر، ويجب أن تكون الكمية أكبر من الصفر', 'ar'),
('totalPrice', 'إجمالي السعر', 'ar'),
('lineTotal', 'إجمالي السطر', 'ar');
