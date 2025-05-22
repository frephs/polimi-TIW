import { Auction, Product, User } from '../prototypes.js';
import { generateWelcomeMessage } from '../utils.js';

export function generateBuySection(
    auctions: Auction[],
    wonAuctions: Auction[],
    closedAuctionShippingAddresses: Record<number, string>,
    user: User,
    searchQuery: string,
) {
    const container = document.querySelector('section#buy') as HTMLElement;

    container.innerHTML = '';
    container.appendChild(generateWelcomeSection(user));
    container.appendChild(generateSearchResultSection(auctions, searchQuery));
    container.appendChild(createWonAuctionsSection(wonAuctions, closedAuctionShippingAddresses));

    return container;
}

function generateWelcomeSection(user: User): HTMLElement {
    const welcomeSection = document.createElement('section');
    welcomeSection.id = 'buy.welcome';
    welcomeSection.innerHTML = `
    <p>${generateWelcomeMessage(user)}</p>
    <p>Welcome to yourAuction platform. Search for products, bid on items, and find great deals!</p>
        <form style="display: flex; align-items: center; align-content: center;" action="/yourauction/buy/search" method="get">
            <div style="margin: 50px 200px; display: flex; align-items: center; align-content: center; flex-direction: row;">
                <input type="text" style="margin-top: 10px;" name="q" id="buy-search-button" placeholder="Search for auctions and items...">
                <button type="submit">Search</button>
            </div>
        </form>
    `;
    return welcomeSection;
}

function generateSearchResultSection(auctions: Auction[], searchQuery: string): HTMLElement {
    if (!auctions || auctions.length === 0) {
        const noAuctionsWarning = document.createElement('div');
        noAuctionsWarning.className = !searchQuery ? 'info' : 'warning';
        noAuctionsWarning.innerHTML = !searchQuery
            ? '<p>Use the search bar to look for products!</p>'
            : '<p>No auctions found for your search query.</p>';
        return noAuctionsWarning;
    }

    const auctionsSection = document.createElement('section');
    auctionsSection.id = 'auctions';

    const auctionList = auctions.map((auction) => createAuctionCard(auction, searchQuery)).join('');

    auctionsSection.innerHTML = `
        ${createPreviousSearchResultsSection(
            JSON.parse(window.sessionStorage.getItem('searchedProducts') || '[]').map(
                (product: any) => new Product(product),
            ),
        )}
        
        <h2 style="margin-top: 20px">Search Results</h2>
        <div id="auction-list" style="display: grid; width: 100%; grid-template-columns: 1fr 1fr 1fr; gap: 20px; align-content: center; align-items: start;">
            ${auctionList}
        </div>
    `;

    return auctionsSection;
}

function createPreviousSearchResultsSection(products: Product[]): string {
    return products
        ? `
        <h2 style="margin-bottom: 15px">Products you viewed in the past</h2>
        ${products.map((product) => {
            return `
                <form action="/yourauction/buy/search" method="get" style="display: inline">
                    <button type="submit" class="no-style-button">
                        <div class="auction-card-tag" style="padding:20px">${product.name}</div>
                        <input type="hidden" name="q" value="${product.name}">
                    </button>
                </form>
            `;
        })}
    `
        : ``;
}

function createAuctionCard(auction: Auction, searchQuery: string): string {
    const searchedProducts: Product[] = JSON.parse(
        window.sessionStorage.getItem('searchedProducts') || '[]',
    );

    if (searchQuery != '') {
        auction.products
            .filter(
                (product: Product) =>
                    !searchedProducts.some(
                        (previousProduct: Product) => previousProduct.name === product.name,
                    ),
            )
            .forEach((product) => searchedProducts.push(product));
    }

    window.sessionStorage.setItem('searchedProducts', JSON.stringify(searchedProducts));

    const productImages = auction.products
        .map(
            (product: Product) =>
                `<img src="/yourauction/image/${product.imageFilename}" alt="Product Image">`,
        )
        .join('');

    const productTags = auction.products
        .map(
            (product: Product) => `
                <form action="/yourauction/buy/search" method="get" style="display: inline">
                    <button type="submit" class="no-style-button">
                        <div class="auction-card-tag">${product.name}</div>
                        <input type="hidden" name="q" value="${product.name}">
                    </button>
                </form>
            `,
        )
        .join('');

    return `
        <div class="auction-card">
            <div class="auction-card-author" style="margin-left: 10px;">
                Auction by <button class="no-style-button"> <div class="auction-card-tag user">${auction.sellerUsername}</div></button>
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
            <div class="auction-card-tags">${productTags}</div>
            <form action="/yourauction/buy/auction" method="get" style="width: calc(100%);" enctype="multipart/form-data">
                <button style="width: calc(100% - 25px);" type="submit" class="bid-button" >Bid</button>
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
        <h2>Won Auctions</h2>
        <div class="winner" style="background: url('/yourauction/images/confetti.gif')">
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
            (product: Product) =>
                `<img src="/yourauction/image/${product.imageFilename}" alt="Product Image">`,
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
