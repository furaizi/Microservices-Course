CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE
);

INSERT INTO users (email, name) VALUES
('test@example.com', 'Test User'),
('admin@example.com', 'Admin User'),
('john.doe@email.com', 'John Doe'),
('jane.smith@sample.net', 'Jane Smith'),
('alex_k@domain.org', 'Alex Kowalski'),
('support@mycompany.io', 'Support Team'),
('peter.jones@another.com', 'Peter Jones');
