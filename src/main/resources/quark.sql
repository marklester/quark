--CREATE DATABASE quark;

USE quark;

CREATE TABLE IF NOT EXISTS quark.orders (
	id STRING PRIMARY KEY,
	tradePairId INT,
	orderDate TIMESTAMP, 
	label STRING, 
	price DECIMAL,
	amount DECIMAL, 
	total DECIMAL,
	orderType INT
);
