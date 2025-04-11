CREATE TYPE payment_method AS ENUM ('BANK_TRANSFER', 'CREDIT_CARD');

ALTER TABLE payments
    ALTER COLUMN method TYPE payment_method USING method::payment_method;
