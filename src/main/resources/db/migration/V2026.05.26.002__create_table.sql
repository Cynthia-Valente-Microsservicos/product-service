CREATE TABLE products.products (
    id UUID PRIMARY KEY,
    name VARCHAR(256) NOT NULL,
    price REAL NOT NULL,
    unit VARCHAR(50)
)