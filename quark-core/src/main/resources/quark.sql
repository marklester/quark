--CREATE DATABASE quark;

--USE quark;

CREATE TABLE IF NOT EXISTS orders (
	id VARCHAR(64) PRIMARY KEY,
	tradePairId INT,
	orderDate TIMESTAMP, 
	label VARCHAR(100), 
	price DECIMAL,
	amount DECIMAL, 
	total DECIMAL,
	orderType INT
);
CREATE INDEX IF NOT EXISTS date_idx ON orders(orderdate);
CREATE INDEX IF NOT EXISTS tp_idx on orders(tradepairid);
ALTER TABLE orders ALTER orderdate SET STATISTICS 1000;