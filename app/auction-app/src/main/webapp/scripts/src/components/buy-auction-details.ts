import { Auction, Bid, Product } from '../prototypes.js';

export function generateBuyAuctionDetails(auction: Auction, bids: Bid[]): string {
    return `
<section id="buy-auction-details">
    ${generateAuctionHeader(auction)}
    ${generateProductsSection(auction)}
    ${generateAuctionInfo(auction)}
    ${generateBidsSection(bids)}
    ${generatePlaceBidSection(auction)}
</section>
    `;
}

function generateAuctionHeader(auction: Auction): string {
    return `
<section>
    <h1>
        Auction
        <span>${auction.id}</span>
    </h1>
</section>
    `;
}

function generateProductsSection(auction: Auction): string {
    if (!auction) return '';
    return `
<section id="Products">
    <h2>Products</h2>
    <div class="flex-row">
        <fieldset style="max-height: 630px; padding-top: 20px; padding-bottom: 20px; min-width: 60%;">
            <legend>Product details</legend>
            <div style="overflow-y: scroll; max-height: 610px; max-width: 99%">
                <table style="max-width: calc(100% - 20px); margin-right: 20px">
                    <thead>
                        <tr>
                            <th>Name</th>
                            <th>Price</th>
                            <th>Description</th>
                            <th>Image</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${auction.products
                            .map((product: any) => generateProductRow(product))
                            .join('')}
                    </tbody>
                </table>
            </div>
        </fieldset>
    </div>
</section>
    `;
}

function generateProductRow(product: Product): string {
    return `
<tr>
    <td>${product.name}</td>
    <td>€${product.price}</td>
    <td>${product.description}</td>
    <td>
        <img src="/yourauction/image/${product.imageFilename}" alt="Product Image" style="max-width: 100px; max-height: 100px" />
    </td>
</tr>
    `;
}

function generateAuctionInfo(auction: Auction): string {
    if (!auction) return '';
    return `
<section id="bids">
    <h2>Info</h2>
    <ul>
        <li><b>Items:</b> ${auction.products.length}</li>
        <li><b>Starting price:</b> €${auction.getStartingPrice()}</li>
        <li><b>Minimum bid increase:</b> €${auction.minimumBidIncrement}</li>
        <li><b>Deadline:</b> ${auction.getFormattedEndTime()}</li>
    </ul>
    ${auction.isOpen() ? generateRemainingTime(auction) : generateAuctionClosed(auction)}
</section>
    `;
}

function generateRemainingTime(auction: Auction): string {
    const remainingTime = auction.getRemainingTimeString();
    return `
<div>
    <h3>Remaining time</h3>
    <div class="info">
        ${
            remainingTime !== '0d 0h'
                ? `
        <p>There is still ${remainingTime} until the auction ends.</p>
        `
                : `
        <p>The auction deadline has passed, but it is not closed yet. Bid just in time!</p>
        `
        }
    </div>
</div>
    `;
}

function generateAuctionClosed(auction: Auction): string {
    const winner = auction.currentHighestBid;
    return `
<div class="info">
    <p>The auction has been closed.</p>
</div>
<div>
    <h2>Winner</h2>
    <div class="winner" style="background:url('/images/confetti.gif');">
        <p>
            The winner of the auction is <b>@</b>
            <span style="font-weight: bold">${winner?.bidderUsername}</span>
            with a bid of <b>€</b>
            <span style="font-weight: bold">${winner?.bidAmount}</span>
            sent at <span style="font-weight: bold">${winner?.bidTimestamp}</span>
        </p>
    </div>
</div>
    `;
}

function generateBidsSection(bids: Bid[]): string {
    if (!bids || bids.length === 0) {
        return `
<section id="bids">
    <h2>Bids</h2>
    <div class="info">
        <p>No bids available for this auction.</p>
    </div>
</section>
        `;
    }
    return `
<section id="bids">
    <h2>Bids</h2>
    <h3>Current Bids</h3>
    <table>
        <thead>
            <tr>
                <th>Bidder</th>
                <th>Bid Amount (€)</th>
                <th>Bid Time</th>
            </tr>
        </thead>
        <tbody>
            ${bids.map((bid: Bid) => generateBidRow(bid)).join('')}
        </tbody>
    </table>
</section>
    `;
}

function generateBidRow(bid: Bid): string {
    return `
<tr>
    <td>${bid.bidderUsername}</td>
    <td>€${bid.bidAmount}</td>
    <td>${new Date(bid.bidTimestamp).toLocaleString()}</td>
</tr>
    `;
}

function generatePlaceBidSection(auction: Auction): string {
    if (!auction.isOpen()) return '';
    const minBid =
        auction.currentHighestBid == null || auction.currentHighestBid.bidAmount === 0
            ? auction.getStartingPrice()
            : auction.currentHighestBid?.bidAmount + auction.minimumBidIncrement;
    return `
<section id="place-bid">
    <h3>Place a Bid</h3>
    <form action="/yourauction/buy/auction/bid/" method="post" enctype="multipart/form-data">
        <label for="bid-amount">Your Bid (€):</label>
        <input type="hidden" name="auction-id" id="auction_id" value="${auction.id}" />
        <input type="number" id="bid-amount" name="bid-amount" required min="${minBid}" step="${auction.minimumBidIncrement}" value="${minBid}" />
        <button type="submit">Submit Bid</button>
    </form>
</section>
    `;
}
