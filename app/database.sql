-- Select the database to use
DROP SCHEMA IF EXISTS `TIW - Auctions`;

CREATE SCHEMA `TIW - Auctions`;

USE `TIW - Auctions`;

CREATE TABLE `users` (
    `username` VARCHAR(20) PRIMARY KEY,
    `password` VARCHAR(45),
    `name` VARCHAR(45),
    `surname` VARCHAR(45),
    `street` VARCHAR(45),
    `postcode` INTEGER,
    `city` VARCHAR(45),
    `region` VARCHAR(45),
    `country` VARCHAR(45)
);

CREATE TABLE `auctions` (
    `auction_id` INTEGER PRIMARY KEY AUTO_INCREMENT,
    `seller_username` VARCHAR(20) NOT NULL,
    `start_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `final_bid_submission` DATETIME NOT NULL,
    `min_bid_increment` INTEGER NOT NULL DEFAULT 1,
    `closed` BOOLEAN NOT NULL DEFAULT 0,
    FOREIGN KEY (`seller_username`) REFERENCES `users` (`username`) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE `products` (
    `product_id` INTEGER PRIMARY KEY AUTO_INCREMENT,
    `seller_username` VARCHAR(20) NOT NULL,
    `name` TEXT NOT NULL,
    `description` TEXT,
    `image_url` TEXT NOT NULL,
    `price` DECIMAL(10, 2) NOT NULL,
    `auction_id` INTEGER DEFAULT NULL,
    FOREIGN KEY (`auction_id`) REFERENCES `auctions` (`auction_id`) ON UPDATE CASCADE ON DELETE
    SET
        NULL
);

CREATE TABLE `bids` (
    `auction_id` INTEGER NOT NULL,
    `bidder_username` VARCHAR(20),
    `bid_price` DECIMAL(10, 2) NOT NULL,
    `bid_timestamp` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX (`auction_id` ASC, `bid_timestamp` DESC),
    FOREIGN KEY (`auction_id`) REFERENCES `auctions` (`auction_id`) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (`bidder_username`) REFERENCES `users` (`username`) ON UPDATE CASCADE ON DELETE
    SET
        NULL,
        CONSTRAINT `unique_bid_price` UNIQUE (`auction_id`, `bid_price`),
        CONSTRAINT `unique_bid_timestamp` UNIQUE (`auction_id`, `bidder_username`, `bid_timestamp`)
);

CREATE VIEW `active_auctions` AS
SELECT
    a.auction_id,
    a.seller_username,
    a.start_time,
    a.final_bid_submission,
    a.min_bid_increment,
    a.closed,
    p.product_id,
    p.name,
    p.description,
    p.price,
    p.image_url
FROM
    auctions a
    LEFT JOIN products p ON a.auction_id = p.auction_id
WHERE
    a.closed = 0;

CREATE VIEW `active_bids` AS
SELECT
    b.auction_id,
    b.bidder_username,
    b.bid_price
FROM
    bids b
    JOIN auctions a ON b.auction_id = a.auction_id
    LEFT JOIN products p ON a.auction_id = p.auction_id
WHERE
    a.closed = 0
    AND b.bid_price = (
        SELECT
            MAX(b2.bid_price)
        FROM
            bids b2
        WHERE
            b2.auction_id = b.auction_id
    );

-- todo: check this trigger
DELIMITER `~;`;

CREATE TRIGGER prevent_insert_lower_bids BEFORE
INSERT
    ON bids FOR EACH ROW BEGIN IF EXISTS (
        SELECT
            1
        FROM
            bids b
        WHERE
            b.auction_id = NEW.auction_id
            AND b.bid_price >= NEW.bid_price
    ) THEN SIGNAL SQLSTATE '45000'
SET
    MESSAGE_TEXT = 'Bid price must be higher than the current highest bid';

END IF;

END ~;

-- todo: check this trigger
CREATE TRIGGER prevent_bids_lower_than_increment BEFORE
INSERT
    ON bids FOR EACH ROW BEGIN IF EXISTS (
        SELECT
            *
        FROM
            bids b
        WHERE
            b.auction_id = NEW.auction_id
            AND NEW.bid_price < (
                SELECT
                    MAX(b2.bid_price) + a.min_bid_increment
                FROM
                    bids b2 NATURAL
                    JOIN auctions a
                WHERE
                    b2.auction_id = NEW.auction_id
                    AND a.closed = 0
            )
    ) THEN SIGNAL SQLSTATE '45000'
SET
    MESSAGE_TEXT = "Bid price is lower than the minimum bid increment";

END IF;

END ~;

-- todo: check this trigger
-- if the auction has no products, the auction is deleted
CREATE TRIGGER delete_auction_if_no_products
AFTER
    DELETE ON products FOR EACH ROW BEGIN
DELETE FROM
    auctions
WHERE
    auction_id = OLD.auction_id
    AND NOT EXISTS (
        SELECT
            1
        FROM
            products
        WHERE
            auction_id = OLD.auction_id
    );

END ~;

-- todo: check this trigger
-- if the auction is closed, the product auction_id cannot be changed
CREATE TRIGGER auction_closed_no_product_changes BEFORE
UPDATE
    ON products FOR EACH ROW BEGIN IF EXISTS (
        SELECT
            *
        FROM
            auctions a
        WHERE
            a.auction_id = OLD.auction_id
            AND a.closed = 1
    ) THEN SIGNAL SQLSTATE '45000'
SET
    MESSAGE_TEXT = "Cannot change auction_id of a closed auction";

END IF;

END ~;

-- Prevent deletion of a user if they are the highest bidder in any auction
-- todo: check this trigger
CREATE TRIGGER prevent_delete_highest_bidder BEFORE DELETE ON users FOR EACH ROW BEGIN IF EXISTS (
    SELECT
        *
    FROM
        bids b1
    WHERE
        b1.bidder_username = OLD.username
        AND b1.bid_price = (
            SELECT
                MAX(b2.bid_price)
            FROM
                bids b2
            WHERE
                b2.auction_id = b1.auction_id
        )
        AND EXISTS (
            SELECT
                *
            FROM
                auctions a
            WHERE
                a.auction_id = b1.auction_id
                AND a.closed = 0
        )
) THEN SIGNAL SQLSTATE '45000'
SET
    MESSAGE_TEXT = "Cannot delete user who is the highest bidder in an active auction";

END IF;

END ~;