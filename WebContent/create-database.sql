DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS categories CASCADE;
DROP TABLE IF EXISTS products CASCADE;
DROP TABLE IF EXISTS sales CASCADE;
DROP TABLE IF EXISTS states CASCADE;
DROP TABLE IF EXISTS cart_history CASCADE;
DROP TABLE IF EXISTS ordered CASCADE;
DROP TABLE IF EXISTS totals CASCADE;
DROP FUNCTION IF EXISTS orderfunc() CASCADE;
DROP FUNCTION IF EXISTS salesfunc() CASCADE;
DROP FUNCTION IF EXISTS totalsfunc() CASCADE;

/**table 0: [entity] states**/
CREATE TABLE states (
    id          SERIAL PRIMARY KEY,
    name        TEXT NOT NULL
);

INSERT INTO states (name) VALUES ('Alabama');
INSERT INTO states (name) VALUES ('Alaska');
INSERT INTO states (name) VALUES ('Arizona');
INSERT INTO states (name) VALUES ('Arkansas');
INSERT INTO states (name) VALUES ('California');
INSERT INTO states (name) VALUES ('Colorado');
INSERT INTO states (name) VALUES ('Connecticut');
INSERT INTO states (name) VALUES ('Delaware');
INSERT INTO states (name) VALUES ('Florida');
INSERT INTO states (name) VALUES ('Georgia');
INSERT INTO states (name) VALUES ('Hawaii');
INSERT INTO states (name) VALUES ('Idaho');
INSERT INTO states (name) VALUES ('Illinois');
INSERT INTO states (name) VALUES ('Indiana');
INSERT INTO states (name) VALUES ('Iowa');
INSERT INTO states (name) VALUES ('Kansas');
INSERT INTO states (name) VALUES ('Kentucky');
INSERT INTO states (name) VALUES ('Louisiana');
INSERT INTO states (name) VALUES ('Maine');
INSERT INTO states (name) VALUES ('Maryland');
INSERT INTO states (name) VALUES ('Massachusetts');
INSERT INTO states (name) VALUES ('Michigan');
INSERT INTO states (name) VALUES ('Minnesota');
INSERT INTO states (name) VALUES ('Mississippi');
INSERT INTO states (name) VALUES ('Missouri');
INSERT INTO states (name) VALUES ('Montana');
INSERT INTO states (name) VALUES ('Nebraska');
INSERT INTO states (name) VALUES ('Nevada');
INSERT INTO states (name) VALUES ('New Hampshire');
INSERT INTO states (name) VALUES ('New Jersey');
INSERT INTO states (name) VALUES ('New Mexico');
INSERT INTO states (name) VALUES ('New York');
INSERT INTO states (name) VALUES ('North Carolina');
INSERT INTO states (name) VALUES ('North Dakota');
INSERT INTO states (name) VALUES ('Ohio');
INSERT INTO states (name) VALUES ('Oklahoma');
INSERT INTO states (name) VALUES ('Oregon');
INSERT INTO states (name) VALUES ('Pennsylvania');
INSERT INTO states (name) VALUES ('Rhode Island');
INSERT INTO states (name) VALUES ('South Carolina');
INSERT INTO states (name) VALUES ('South Dakota');
INSERT INTO states (name) VALUES ('Tennessee');
INSERT INTO states (name) VALUES ('Texas');
INSERT INTO states (name) VALUES ('Utah');
INSERT INTO states (name) VALUES ('Vermont');
INSERT INTO states (name) VALUES ('Virginia');
INSERT INTO states (name) VALUES ('Washington');
INSERT INTO states (name) VALUES ('West Virginia');
INSERT INTO states (name) VALUES ('Wisconsin');
INSERT INTO states (name) VALUES ('Wyoming');


/**table 1: [entity] users**/
CREATE TABLE users (
    id          SERIAL PRIMARY KEY,
    name        TEXT NOT NULL UNIQUE CHECK (name <> ''),
    role        TEXT NOT NULL,
    age         INTEGER NOT NULL,
    state       INTEGER REFERENCES states (id) NOT NULL
);
INSERT INTO users (name, role, age, state) VALUES('CSE','owner',35,3);
INSERT INTO users (name, role, age, state) VALUES('David','customer',33,12);
INSERT INTO users (name, role, age, state) VALUES('Floyd','customer',27,14);
INSERT INTO users (name, role, age, state) VALUES('James','customer',55,1);
INSERT INTO users (name, role, age, state) VALUES('Ross','customer',24,5);


/**table 2: [entity] category**/
CREATE TABLE categories (
    id          SERIAL PRIMARY KEY,
    name        TEXT NOT NULL UNIQUE CHECK (name <> ''),
    description TEXT NOT NULL
);
INSERT INTO categories (name, description) VALUES('Computers','A computer is a general purpose device that can be programmed to carry out a set of arithmetic or logical operations automatically. Since a sequence of operations can be readily changed, the computer can solve more than one kind of problem.');
INSERT INTO categories (name, description) VALUES('Cell Phones','A mobile phone (also known as a cellular phone, cell phone, and a hand phone) is a phone that can make and receive telephone calls over a radio link while moving around a wide geographic area. It does so by connecting to a cellular network provided by a mobile phone operator, allowing access to the public telephone network.');
INSERT INTO categories (name, description) VALUES('Cameras','A camera is an optical instrument that records images that can be stored directly, transmitted to another location, or both. These images may be still photographs or moving images such as videos or movies.');
INSERT INTO categories (name, description) VALUES('Video Games','A video game is an electronic game that involves human interaction with a user interface to generate visual feedback on a video device..');

/**table 3: [entity] product**/
CREATE TABLE products (
    id          SERIAL PRIMARY KEY,
    cid         INTEGER REFERENCES categories (id) NOT NULL,
    name        TEXT NOT NULL,
    SKU         TEXT NOT NULL UNIQUE,
    price       INTEGER NOT NULL
);
INSERT INTO products (cid, name, SKU, price) VALUES(1, 'Apple MacBook',     '103001',   1200); /**1**/
INSERT INTO products (cid, name, SKU, price) VALUES(1, 'HP Laptop',         '106044',   480);
INSERT INTO products (cid, name, SKU, price) VALUES(1, 'Dell Laptop',       '109023',   399);/**3**/
INSERT INTO products (cid, name, SKU, price) VALUES(2, 'Iphone 5s',         '200101',   709);
INSERT INTO products (cid, name, SKU, price) VALUES(2, 'Samsung Galaxy S4', '208809',   488);/**5**/
INSERT INTO products (cid, name, SKU, price) VALUES(2, 'LG Optimus g',       '209937',  375);
INSERT INTO products (cid, name, SKU, price) VALUES(3, 'Sony DSC-RX100M',   '301211',   689);/**7**/
INSERT INTO products (cid, name, SKU, price) VALUES(3, 'Canon EOS Rebel T3',     '304545',  449);
INSERT INTO products (cid, name, SKU, price) VALUES(3, 'Nikon D3100',       '308898',   520);
INSERT INTO products (cid, name, SKU, price) VALUES(4, 'Xbox 360',          '405065',   249);/**10**/
INSERT INTO products (cid, name, SKU, price) VALUES(4, 'Nintendo Wii U ',    '407033',  430);
INSERT INTO products (cid, name, SKU, price) VALUES(4, 'Nintendo Wii',      '408076',   232);


-- should be removed for project 2.
CREATE TABLE cart_history (
    id          SERIAL PRIMARY KEY,
    uid         INTEGER REFERENCES users (id) NOT NULL
);

CREATE TABLE sales (
    id          SERIAL PRIMARY KEY,
    uid         INTEGER REFERENCES users (id) NOT NULL,
    cart_id     INTEGER REFERENCES cart_history (id) NOT NULL, -- should be removed for project 2.
    pid         INTEGER REFERENCES products (id) NOT NULL,
    quantity    INTEGER NOT NULL,
    price       INTEGER NOT NULL
);

CREATE TABLE ordered (
    id          SERIAL PRIMARY KEY,
    uid       	INTEGER REFERENCES states (id) NOT NULL,
    pid     	INTEGER REFERENCES products (id) NOT NULL,
    price       INTEGER NOT NULL
);

--CREATE FUNCTION orderfunc() RETURNS TRIGGER AS $order_table$
--    BEGIN
--	INSERT INTO ordered(uid, product, price) VALUES
--	((SELECT u.id AS uid FROM states AS s), NEW.ID, 0);
--        RETURN NEW;
--    END;
--$order_table$ LANGUAGE plpgsql;

--CREATE TRIGGER trg_products
--    AFTER INSERT OR UPDATE
--    ON products  
--    FOR EACH ROW
--EXECUTE PROCEDURE orderfunc();

CREATE FUNCTION salesfunc() RETURNS TRIGGER AS $orderS_table$
    BEGIN
	INSERT INTO ordered(uid, pid, price) VALUES
	(NEW.uid, NEW.pid, (NEW.price * NEW.quantity));
        RETURN NEW;
    END;
$orderS_table$ LANGUAGE plpgsql;

CREATE TRIGGER trg_sales
    BEFORE INSERT OR UPDATE
    ON sales
    FOR EACH ROW
EXECUTE PROCEDURE salesfunc();

CREATE TABLE totals (
    id          SERIAL PRIMARY KEY,
    state       INTEGER REFERENCES states (id) NOT NULL,
    total       INTEGER NOT NULL
);

INSERT INTO totals (state, total) VALUES (1, 0.0);
INSERT INTO totals (state, total) VALUES (2, 0.0);
INSERT INTO totals (state, total) VALUES (3, 0.0);
INSERT INTO totals (state, total) VALUES (4, 0.0);
INSERT INTO totals (state, total) VALUES (5, 0.0);
INSERT INTO totals (state, total) VALUES (6, 0.0);
INSERT INTO totals (state, total) VALUES (7, 0.0);
INSERT INTO totals (state, total) VALUES (8, 0.0);
INSERT INTO totals (state, total) VALUES (9, 0.0);
INSERT INTO totals (state, total) VALUES (10, 0.0);
INSERT INTO totals (state, total) VALUES (11, 0.0);
INSERT INTO totals (state, total) VALUES (12, 0.0);
INSERT INTO totals (state, total) VALUES (13, 0.0);
INSERT INTO totals (state, total) VALUES (14, 0.0);
INSERT INTO totals (state, total) VALUES (15, 0.0);
INSERT INTO totals (state, total) VALUES (16, 0.0);
INSERT INTO totals (state, total) VALUES (17, 0.0);
INSERT INTO totals (state, total) VALUES (18, 0.0);
INSERT INTO totals (state, total) VALUES (19, 0.0);
INSERT INTO totals (state, total) VALUES (20, 0.0);
INSERT INTO totals (state, total) VALUES (21, 0.0);
INSERT INTO totals (state, total) VALUES (22, 0.0);
INSERT INTO totals (state, total) VALUES (23, 0.0);
INSERT INTO totals (state, total) VALUES (24, 0.0);
INSERT INTO totals (state, total) VALUES (25, 0.0);
INSERT INTO totals (state, total) VALUES (26, 0.0);
INSERT INTO totals (state, total) VALUES (27, 0.0);
INSERT INTO totals (state, total) VALUES (28, 0.0);
INSERT INTO totals (state, total) VALUES (29, 0.0);
INSERT INTO totals (state, total) VALUES (30, 0.0);
INSERT INTO totals (state, total) VALUES (31, 0.0);
INSERT INTO totals (state, total) VALUES (32, 0.0);
INSERT INTO totals (state, total) VALUES (33, 0.0);
INSERT INTO totals (state, total) VALUES (34, 0.0);
INSERT INTO totals (state, total) VALUES (35, 0.0);
INSERT INTO totals (state, total) VALUES (36, 0.0);
INSERT INTO totals (state, total) VALUES (37, 0.0);
INSERT INTO totals (state, total) VALUES (38, 0.0);
INSERT INTO totals (state, total) VALUES (39, 0.0);
INSERT INTO totals (state, total) VALUES (40, 0.0);
INSERT INTO totals (state, total) VALUES (41, 0.0);
INSERT INTO totals (state, total) VALUES (42, 0.0);
INSERT INTO totals (state, total) VALUES (43, 0.0);
INSERT INTO totals (state, total) VALUES (44, 0.0);
INSERT INTO totals (state, total) VALUES (45, 0.0);
INSERT INTO totals (state, total) VALUES (46, 0.0);
INSERT INTO totals (state, total) VALUES (47, 0.0);
INSERT INTO totals (state, total) VALUES (48, 0.0);
INSERT INTO totals (state, total) VALUES (49, 0.0);
INSERT INTO totals (state, total) VALUES (50, 0.0);

CREATE FUNCTION totalsfunc() RETURNS TRIGGER AS $totals_table$
    BEGIN
	UPDATE totals
        SET total = 
            (SELECT SUM(o.price) FROM ordered AS o,totals AS t 
             WHERE NEW.id = o.id AND t.state = (SELECT t.state FROM totals AS t, users as u 
						WHERE NEW.uid = u.id AND u.state = t.state))
	WHERE  state = (SELECT t.state FROM totals AS t, users as u 
			  WHERE NEW.uid = u.id AND u.state = t.state);
        RETURN NEW;
    END;
$totals_table$ LANGUAGE plpgsql;

CREATE TRIGGER trg_totals
    AFTER INSERT
    ON ordered
    FOR EACH ROW
EXECUTE PROCEDURE totalsfunc();