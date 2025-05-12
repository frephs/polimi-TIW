-- Select the database to use
DROP SCHEMA IF EXISTS `your-auction`;
CREATE SCHEMA `your-auction`;
USE `your-auction`;

CREATE TABLE `users` (
    `username` VARCHAR(20) PRIMARY KEY,
    `password` CHAR(128) NOT NULL,
    `name` VARCHAR(45) NOT NULL,
    `surname` VARCHAR(45) NOT NULL,
    `country` VARCHAR(45) NOT NULL,
    `zip_code` INTEGER NOT NULL,
    `city` VARCHAR(45) NOT NULL,
    `street` VARCHAR(45) NOT NULL,
    `street_number` INTEGER NOT NULL
);

CREATE TABLE `auctions` (
    `auction_id` INTEGER PRIMARY KEY AUTO_INCREMENT,
    `seller_username` VARCHAR(20) NOT NULL,
    `start_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `final_bid_submission_time` DATETIME NOT NULL,
    `min_bid_increment` INTEGER NOT NULL DEFAULT 1,
    `closed` BOOLEAN NOT NULL DEFAULT 0,
    FOREIGN KEY (`seller_username`) REFERENCES `users` (`username`) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE `products` (
    `product_id` INTEGER PRIMARY KEY AUTO_INCREMENT,
    `seller_username` VARCHAR(20) NOT NULL,
    `name` TEXT NOT NULL,
    `description` TEXT,
    `image_filename` TEXT NOT NULL,
    `price` DECIMAL(10, 2) NOT NULL,
    `auction_id` INTEGER DEFAULT NULL,
    FOREIGN KEY (`auction_id`) REFERENCES `auctions` (`auction_id`) ON UPDATE CASCADE ON DELETE
    SET
        NULL
);

CREATE TABLE `bids` (
    `auction_id` INTEGER NOT NULL,
    `bidder_username` VARCHAR(20),
    `bid_amount` DECIMAL(10, 2) NOT NULL,
    `bid_timestamp` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX (`auction_id` ASC, `bid_timestamp` DESC),
    FOREIGN KEY (`auction_id`) REFERENCES `auctions` (`auction_id`) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (`bidder_username`) REFERENCES `users` (`username`) ON UPDATE CASCADE ON DELETE
    SET
        NULL,
        CONSTRAINT `unique_bid_amount` UNIQUE (`auction_id`, `bid_amount`),
        CONSTRAINT `unique_bid_timestamp` UNIQUE (`auction_id`, `bidder_username`, `bid_timestamp`)
);

CREATE TABLE shipping_addresses (
    auction_id INTEGER NOT NULL,
    address TEXT NOT NULL,
    PRIMARY KEY (auction_id),
    FOREIGN KEY (auction_id) REFERENCES auctions (auction_id) ON UPDATE CASCADE ON DELETE CASCADE
);
