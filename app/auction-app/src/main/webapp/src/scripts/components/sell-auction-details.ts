import { Auction, Bid, Product } from '../prototypes';

export function generateSellAuctionDetails(
    auction: Auction,
    bids: Bid[],
    closedAuctionShippingAddress: string | null,
): string {
    return `
        <section id="sell-auction-details">
            ${generateAuctionHeader(auction)}
            ${generateProductsSection(auction)}
            ${generateBidsSection(auction, bids, closedAuctionShippingAddress)}
            ${generateAuctionActionsSection(auction)}
        </section>
    `;
}

function generateAuctionHeader(auction: any): string {
    return `
        <section>
            <h1>
                Auction
                <span>${auction.getId()}</span>
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
                    <legend>
                        <span>${auction.isOpen() ? 'Edit product details' : 'Product details'}</span>
                    </legend>
                    <div style="overflow-y: scroll; max-height: 610px; max-width: 99%">
                        <table style="max-width: calc(100% - 20px); margin-right: 20px">
                            <thead>
                                <th>Name</th>
                                <th>Price</th>
                                <th>Description</th>
                                <th>Image</th>
                                <th>Submit edit</th>
                            </thead>
                            <tbody>
                                ${auction.products.map((product: any) => generateProductRow(product, auction)).join('')}
                            </tbody>
                        </table>
                    </div>
                </fieldset>
            </div>
        </section>
    `;
}

function generateProductRow(product: Product, auction: Auction): string {
    return `
        <tr>
            <form action="/sell/product/edit/" enctype="multipart/form-data" method="post">
                <td>
                    <input type="hidden" name="id" value="${product.id}" />
                    <input type="text" name="product-name" value="${product.name}" required ${!auction.isOpen() ? 'disabled' : ''} />
                </td>
                <td>
                    <div style="display: flex; flex-direction: row; align-items: center; align-content: center;">
                        <div style="margin-right: 10px">€</div>
                        <input
                            style="display: inline-block"
                            type="number"
                            name="product-price"
                            value="${product.price}"
                            required
                            min="0.01"
                            step="0.01"
                            ${auction.currentHighestBid || !auction.isOpen() ? 'disabled' : ''}
                            title="${auction.currentHighestBid == null && auction.isOpen() ? 'Change price' : 'Price cannot be changed because the product is auctioned and there is already a bid on it or the auction is closed'}"
                        />
                    </div>
                </td>
                <td>
                    <textarea name="product-description" required ${!auction.isOpen() ? 'disabled' : ''}>${product.description}</textarea>
                </td>
                <td>
                    <div style="display: flex; flex-direction: column; align-items: center; align-content: center;">
                        <img src="/image/${product.imageFilename}" alt="Product Image" style="max-width: 100px; max-height: 100px;" />
                        <input
                            type="file"
                            style="border: none; padding: 10px; box-shadow: none; background: none;"
                            name="product-image"
                            accept="image/png, image/jpeg"
                            class="change"
                            ${!auction.isOpen() ? 'disabled' : ''}
                        />
                    </div>
                </td>
                <td>
                    <button type="submit" ${!auction.isOpen() ? 'disabled' : ''}>Update</button>
                </td>
            </form>
        </tr>
    `;
}

function generateBidsSection(
    auction: Auction,
    bids: Bid[],
    closedAuctionShippingAddress: string | null,
): string {
    if (!auction) return '';
    return `
        <section id="bids">
            <h2>Info</h2>
            <ul>
                <li><b>Items:</b> <span>${auction.products.length}</span></li>
                <li><b>Starting price:</b> €<span>${auction.getStartingPrice()}</span></li>
                <li><b>Minimum bid increase:</b> €<span>${auction.minimumBidIncrement}</span></li>
                <li><b>Deadline:</b> <span>${auction.getFormattedEndTime()}</span></li>
            </ul>
            ${generateAuctionStatus(auction, bids)}
            ${generateWinnerSection(auction, closedAuctionShippingAddress)}
            ${generateBidsTable(bids)}
        </section>
    `;
}

function generateAuctionStatus(auction: Auction, bids: Bid[]): string {
    if (auction.isOpen() && !auction.canBeClosed()) {
        return `
            <div>
                <h3>Remaining time</h3>
                <div class="info">
                    <p>${
                        auction.getRemainingTimeString() === '0d 0h' && bids.length === 0
                            ? 'Although the deadline has passed, the auction will only be closable once the first bid is placed.'
                            : `There is still ${auction.getRemainingTimeString()} until the auction ends.`
                    }
                    </p>
                </div>
            </div>
        `;
    } else if (auction.canBeClosed()) {
        return `<div class="info"><p>The auction has ended and can be closed.</p></div>`;
    } else if (!auction.isOpen()) {
        return `<div class="info"><p>The auction has been closed.</p></div>`;
    }
    return '';
}

function generateWinnerSection(
    auction: Auction,
    closedAuctionShippingAddress: string | null,
): string {
    if (!auction.isOpen()) {
        return `
            <div>
                <h2>Winner</h2>
                <div class="winner" style="background:url('/images/confetti.gif');">
                    <p>
                        The winner of the auction is <b>@</b>
                        <span style="font-weight: bold">${auction.currentHighestBid?.bidderUsername}</span>
                        with a bid of <b>€</b>
                        <span style="font-weight: bold">${auction.currentHighestBid?.bidAmount}</span>
                        sent at <span style="font-weight: bold">${auction.currentHighestBid?.getFormattedBidTimestamp()}</span>
                    </p>
                    ${
                        closedAuctionShippingAddress
                            ? `<p>Items will be sent to <span>${closedAuctionShippingAddress}</span></p>`
                            : ''
                    }
                </div>
            </div>
        `;
    }
    return '';
}

function generateBidsTable(bids: Bid[]): string {
    if (bids.length > 0) {
        return `
            <h2>Bids</h2>
            <table>
                <thead>
                    <tr>
                        <th>Bidder</th>
                        <th>Bid Amount (€)</th>
                        <th>Bid Time</th>
                    </tr>
                </thead>
                <tbody>
                    ${bids
                        .map(
                            (bid) => `
                        <tr>
                            <td>${bid.bidderUsername}</td>
                            <td>€${bid.bidAmount}</td>
                            <td>${new Date(bid.bidTimestamp).toLocaleString()}</td>
                        </tr>
                    `,
                        )
                        .join('')}
                </tbody>
            </table>
        `;
    }
    return `<div class="info"><p>No bids available for this auction.</p></div>`;
}

function generateAuctionActionsSection(auction: Auction): string {
    if (!auction.isOpen()) return '';
    return `
        <section id="auction-actions">
            <h2>Actions</h2>
            <div class="flex-row">
                ${generateCloseAuctionForm(auction)}
                ${generateDeleteAuctionForm(auction)}
            </div>
        </section>
    `;
}

function generateCloseAuctionForm(auction: Auction): string {
    return `
        <fieldset style="max-width: 50%">
            <legend>Close auction</legend>
            <p>An auction can be closed once the deadline has passed and at least a bid has been placed.</p>
            <p>Once the auction is closed, it cannot be reopened.</p>
            <form action="/sell/auction/close/" method="post">
                <input type="hidden" name="id" value="${auction.id}" />
                <button
                    name="auction-close-id"
                    ${!auction.canBeClosed() ? 'disabled' : ''}
                    type="submit"
                    title="${auction.canBeClosed() ? 'Close this auction' : 'The auction has no bids or deadline has not expired yet'}"
                >
                    Close Auction
                </button>
            </form>
        </fieldset>
    `;
}

function generateDeleteAuctionForm(auction: Auction): string {
    return `
        <fieldset style="max-width: 50%" class="destructive">
            <legend>Delete auction</legend>
            <p>An auction can be deleted up until there are no bids placed.</p>
            <p>Once an auction is deleted, it cannot be recovered.</p>
            <form action="/sell/auction/delete/" method="post">
                <input type="hidden" name="id" value="${auction.id}" />
                <button
                    type="submit"
                    class="destructive"
                    ${!auction.canBeDeleted() ? 'disabled' : ''}
                    title="${auction.canBeDeleted() ? 'Delete this auction' : 'The auction has some bids already'}"
                >
                    Delete Auction
                </button>
            </form>
        </fieldset>
    `;
}
