CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    sum NUMERIC(19, 2) NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO orders (description, sum, user_id) VALUES
('Laptop purchase', 1250.99, 3),        -- Заказ для John Doe (предположим)
('Office supplies', 85.40, 4),           -- Заказ для Jane Smith (предположим)
('Cloud service subscription', 49.99, 5),  -- Заказ для Alex Kowalski (предположим)
('Bulk paper order', 215.00, 6),           -- Заказ для Support Team (предположим)
('Keyboard and Mouse', 110.25, 3),        -- Еще один заказ для John Doe
('Software License Renewal', 499.00, 4),  -- Еще один заказ для Jane Smith
('Consulting services', 1500.00, 7),       -- Заказ для Peter Jones (предположим)
('Travel expenses reimbursement', 320.55, 3); -- Еще один заказ для John Doe