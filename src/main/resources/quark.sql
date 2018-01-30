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
