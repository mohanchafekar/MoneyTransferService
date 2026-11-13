DROP TABLE IF EXISTS transfers;
DROP TABLE IF EXISTS accounts;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
                       user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       first_name VARCHAR(50) NOT NULL,
                       last_name VARCHAR(50) NOT NULL,
                       contact_number VARCHAR(20),
                       address VARCHAR(255),
                       unique_id VARCHAR(50) UNIQUE
);

CREATE TABLE accounts (
                          account_number BIGINT AUTO_INCREMENT PRIMARY KEY,
                          user_id BIGINT NOT NULL,
                          account_type VARCHAR(20) NOT NULL,
                          currency VARCHAR(3) NOT NULL,
                          balance DECIMAL(15, 4) NOT NULL,
                          CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE transfers (
                           transfer_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           source_account_number BIGINT NOT NULL,
                           target_account_number BIGINT NOT NULL,
                           amount_sent DECIMAL(15, 4) NOT NULL,
                           amount_sent_currency VARCHAR(3) NOT NULL,
                           amount_received DECIMAL(15, 4) NOT NULL,
                           amount_received_currency VARCHAR(3) NOT NULL,
                           exchange_rate DECIMAL(15, 6),
                           status VARCHAR(20) NOT NULL,
                           failure_reason VARCHAR(255),
                           date_of_transfer TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           CONSTRAINT fk_source_account FOREIGN KEY (source_account_number) REFERENCES accounts(account_number),
                           CONSTRAINT fk_target_account FOREIGN KEY (target_account_number) REFERENCES accounts(account_number)
);