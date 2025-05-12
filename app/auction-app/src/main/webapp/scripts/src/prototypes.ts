export class Address {
    public country: string;
    public zipCode: number;
    public city: string;
    public street: string;
    public streetNumber: number;

    constructor(object: any) {
        this.country = object.country;
        this.zipCode = object.zipCode;
        this.city = object.city;
        this.street = object.street;
        this.streetNumber = object.streetNumber;
    }
}

export class User {
    public username: string;
    public name: string;
    public surname: string;
    public address: Address;

    constructor(object: any) {
        this.username = object.username;
        this.name = object.name;
        this.surname = object.surname;
        this.address = new Address(object.address);
    }
}

export class Bid {
    public auctionId: number;
    public bidderUsername: string;
    public bidAmount: number;
    public bidTimestamp: string; // ISO 8601 format

    constructor(object: any) {
        this.auctionId = object.auction_id;
        this.bidderUsername = object.bidder_username;
        this.bidAmount = object.bid_amount;
        this.bidTimestamp = object.bid_timestamp;
    }

    public getRoundedBidAmount(): number {
        return Math.round(this.bidAmount * 100) / 100;
    }

    public getFormattedBidTimestamp(): string {
        const date = new Date(this.bidTimestamp);
        return date.toISOString().replace('T', ' ').substring(0, 19);
    }
}

export class Product {
    public id: number;
    public name: string;
    public description: string;
    public price: number;
    public imageFilename: string;
    public auctionId: number;

    constructor(object: any) {
        this.id = object.id;
        this.name = object.name;
        this.description = object.description;
        this.price = object.price;
        this.imageFilename = object.image_filename;
        this.auctionId = object.auctionId
            ? object.auctionId
            : object.auction_id
              ? object.auction_id
              : null;
    }

    public isAuctioned(): boolean {
        return this.auctionId !== null;
    }

    public canChangeAuction(auctions: Auction[]): boolean {
        return (
            !this.isAuctioned() ||
            auctions.some(
                (auction) =>
                    auction.products.some((product) => product.id === this.id) &&
                    auction.products.length > 2,
            )
        );
    }

    public canChangePrice(auctions: Auction[]): boolean {
        if (!this.isAuctioned()) return true;
        return auctions.some(
            (auction) =>
                auction.products.some((product) => product.id === this.id) &&
                auction.currentHighestBid === null,
        );
    }
}

export class Auction {
    public id: number;
    public sellerUsername: string;
    public startTime: string; // ISO 8601 format
    public endTime: string; // ISO 8601 format
    public minimumBidIncrement: number;
    public isClosed: boolean;
    public currentHighestBid: Bid | null;
    public products: Product[];

    constructor(object: any) {
        this.id = object.id;
        this.sellerUsername = object.seller_username
            ? object.seller_username
            : object.sellerUsername
              ? object.sellerUsername
              : null;
        this.startTime = object.start_time;
        this.endTime = object.end_time;
        this.minimumBidIncrement = object.minimum_bid_increment;
        this.isClosed = object.is_closed;
        this.currentHighestBid = object.currentHighestBid
            ? new Bid(object.currentHighestBid)
            : null;
        this.products = object.products.map((p: any) => new Product(p));
    }

    public getFormattedEndTime(): string {
        const date = new Date(this.endTime);
        return date.toISOString().replace('T', ' ').substring(0, 19);
    }

    public getRemainingTimeString(): string {
        const currentTime = Date.now();
        const endTime = new Date(this.endTime).getTime();
        const remainingMillis = endTime - currentTime;

        if (remainingMillis <= 0) {
            return '0d 0h 0m';
        }

        const days = Math.floor(remainingMillis / (24 * 60 * 60 * 1000));
        const hours = Math.floor((remainingMillis / (60 * 60 * 1000)) % 24);
        const minutes = Math.floor((remainingMillis / (60 * 1000)) % 60);

        return `${days}d ${hours}h ${minutes}m`;
    }

    public canBeDeleted(): boolean {
        return this.currentHighestBid === null;
    }

    public canBeClosed(): boolean {
        const currentTime = Date.now();
        const endTime = new Date(this.endTime).getTime();
        return !this.isClosed && this.currentHighestBid !== null && endTime < currentTime;
    }

    public getStartingPrice(): number {
        return (
            Math.round(this.products.reduce((sum, product) => sum + product.price, 0) * 100) / 100
        );
    }

    public isOpen(): boolean {
        return !this.isClosed;
    }
}
