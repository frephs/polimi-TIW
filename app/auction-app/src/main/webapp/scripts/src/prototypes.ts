export class Address {
    public country: string;
    public zipCode: number;
    public city: string;
    public street: string;
    public streetNumber: number;

    constructor(
        country: string,
        zipCode: number,
        city: string,
        street: string,
        streetNumber: number,
    ) {
        this.country = country;
        this.zipCode = zipCode;
        this.city = city;
        this.street = street;
        this.streetNumber = streetNumber;
    }
}

export class User {
    public username: string;
    public name: string;
    public surname: string;
    public address: Address;

    constructor(username: string, name: string, surname: string, address: Address) {
        this.username = username;
        this.name = name;
        this.surname = surname;
        this.address = address;
    }
}

export class Bid {
    public auctionId: number;
    public bidderUsername: string;
    public bidAmount: number;
    public bidTimestamp: string; // ISO 8601 format

    constructor(
        auctionId: number,
        bidderUsername: string,
        bidAmount: number,
        bidTimestamp: string,
    ) {
        this.auctionId = auctionId;
        this.bidderUsername = bidderUsername;
        this.bidAmount = bidAmount;
        this.bidTimestamp = bidTimestamp;
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

    constructor(
        id: number,
        name: string,
        description: string,
        price: number,
        imageFilename: string,
        auctionId: number,
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageFilename = imageFilename;
        this.auctionId = auctionId;
    }

    public isAuctioned(): boolean {
        return this.auctionId !== 0;
    }

    public canChangeAuction(auctions: Auction[]): boolean {
        return auctions.some(
            (auction) =>
                auction.products.some((product) => product.id === this.id) &&
                auction.products.length > 2,
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

    constructor(
        id: number,
        sellerUsername: string,
        startTime: string,
        endTime: string,
        minimumBidIncrement: number,
        isClosed: boolean,
        currentHighestBid: Bid | null,
        products: Product[],
    ) {
        this.id = id;
        this.sellerUsername = sellerUsername;
        this.startTime = startTime;
        this.endTime = endTime;
        this.minimumBidIncrement = minimumBidIncrement;
        this.isClosed = isClosed;
        this.currentHighestBid = currentHighestBid;
        this.products = products;
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
            return '0d 0h';
        }

        const days = Math.floor(remainingMillis / (24 * 60 * 60 * 1000));
        const hours = Math.floor((remainingMillis / (60 * 60 * 1000)) % 24);

        return `${days}d ${hours}h`;
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
