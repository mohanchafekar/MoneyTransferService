INSERT INTO users (first_name, last_name, contact_number, address, unique_id)
VALUES ('Anna', 'Schmidt', '+31611111111', 'Amsterdam', 'UID-1001');

INSERT INTO users (first_name, last_name, contact_number, address, unique_id)
VALUES ('Ben', 'Bakker', '+31622222222', 'Utrecht', 'UID-1002');

INSERT INTO accounts (user_id, account_type, currency, balance)
VALUES (1, 'CHECKING', 'EUR', 1000.00);

INSERT INTO accounts (user_id, account_type, currency, balance)
VALUES (2, 'SAVINGS', 'USD', 500.00);