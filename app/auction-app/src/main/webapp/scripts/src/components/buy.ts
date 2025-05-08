import { Auction, Product } from '../prototypes';
import { generateWelcomeMessage } from '../utils';

export function generateBuySection(
    auctions: Auction[],
    wonAuctions: Auction[],
    closedAuctionShippingAddresses: Record<number, string>,
) {
    const container = document.querySelector('section#buy') as HTMLElement;

    container.appendChild(generateWelcomeSection());
    container.appendChild(generateSearchResultSection(auctions));
    container.appendChild(createWonAuctionsSection(wonAuctions, closedAuctionShippingAddresses));

    return container;
}

function generateWelcomeSection(): HTMLElement {
    const welcomeSection = document.createElement('section');
    welcomeSection.id = 'buy.welcome';
    welcomeSection.innerHTML = `
    <p>${generateWelcomeMessage()}</p>
    <p>Welcome to yourAuction platform. Search for products, bid on items, and find great deals!</p>
        <form style="display: flex; align-items: center; align-content: center;" action="/buy/search" method="get">
            <div style="margin: 50px 200px; display: flex; align-items: center; align-content: center; flex-direction: row;">
                <input type="text" style="margin-top: 10px;" name="q" id="buy-search-button" placeholder="Search for auctions and items...">
                <button type="submit">Search</button>
            </div>
        </form>
    `;
    return welcomeSection;
}

function generateSearchResultSection(auctions: Auction[]): HTMLElement {
    if (!auctions || auctions.length === 0) {
        const noAuctionsWarning = document.createElement('div');
        noAuctionsWarning.className = 'warning';
        noAuctionsWarning.innerHTML = '<p>No auctions found for your search query.</p>';
        return noAuctionsWarning;
    }

    const auctionsSection = document.createElement('section');
    auctionsSection.id = 'auctions';

    const auctionList = auctions.map(createAuctionCard).join('');

    auctionsSection.innerHTML = `
        <h2>Search Results</h2>
        <div id="auction-list" style="display: grid; width: 100%; grid-template-columns: 1fr 1fr 1fr; gap: 20px; align-content: center; align-items: start;">
            ${auctionList}
        </div>
    `;

    return auctionsSection;
}

function createAuctionCard(auction: Auction): string {
    const productImages = auction.products
        .map(
            (product: Product) => `<img src="/image/${product.imageFilename}" alt="Product Image">`,
        )
        .join('');

    return `
        <div class="auction-card">
            <div class="auction-card-author" style="margin-left: 10px;">
                Auction by <button class="no-style-button">${auction.sellerUsername}</button>
            </div>
            <div class="auction-card-detail">
                <table style="margin-top: 10px;">
                    <thead>
                        <tr>
                            <th>Starting at:</th>
                            <th>Bid increase:</th>
                            <th>Time left:</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td>€${auction.currentHighestBid?.bidAmount || auction.getStartingPrice()}</td>
                            <td>€${auction.minimumBidIncrement}</td>
                            <td>${auction.getFormattedEndTime()}</td>
                        </tr>
                    </tbody>
                </table>
            </div>
            <span style="margin-top: 10px;"><b>Gallery</b></span>
            <div class="photo-stack">${productImages}</div>
            <form action="/buy/auction" method="get" style="width: calc(100%);">
                <button style="width: calc(100% - 25px);" type="submit">Bid</button>
                <input type="hidden" name="id" value="${auction.id}">
            </form>
        </div>
    `;
}

function createWonAuctionsSection(
    wonAuctions: Auction[],
    closedAuctionShippingAddresses: Record<number, string>,
): HTMLElement {
    if (!wonAuctions || wonAuctions.length === 0) {
        return document.createElement('div'); // Empty section if no won auctions
    }

    const wonAuctionsSection = document.createElement('section');
    wonAuctionsSection.id = 'won-auctions';

    const wonAuctionList = wonAuctions
        .map((auction) => createWonAuctionCard(auction, closedAuctionShippingAddresses[auction.id]))
        .join('');

    wonAuctionsSection.innerHTML = `
        <h2>Won Auctions</h2>2
        <div class="winner">
            <p>All the items in these sections are yours! Await the shipment and enjoy your new products!</p>
        </div>
        <div id="won-auction-list" style="display: grid; width: 100%; grid-template-columns: 1fr 1fr 1fr; gap: 20px; align-content: center; align-items: start;">
            ${wonAuctionList}
        </div>
    `;

    return wonAuctionsSection;
}

function createWonAuctionCard(auction: Auction, shippingAddress: String): string {
    const productRows = auction.products
        .map((product: Product) => `<tr><td>${product.name}</td></tr>`)
        .join('');
    const shippingAddressRow = shippingAddress
        ? `<tr><td><b>Shipping: address:</b> ${shippingAddress}</td></tr>`
        : '';
    const productImages = auction.products
        .map(
            (product: Product) => `<img src="/image/${product.imageFilename}" alt="Product Image">`,
        )
        .join('');

    return `
        <div class="auction-card">
            <div class="auction-card-author">
                Auction by <span>@${auction.sellerUsername}</span>
            </div>
            <div class="auction-card-detail">
                <table style="margin-top: 10px;">
                    <thead>
                        <tr>
                            <th>Product</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${productRows}
                        ${shippingAddressRow}
                        <tr><td><b>Final price:</b> €${auction.currentHighestBid?.bidAmount}</td></tr>
                    </tbody>
                </table>
            </div>
            <div class="photo-stack">${productImages}</div>
        </div>
    `;
}
