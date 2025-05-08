import { Auction, Product } from '../prototypes';
import { generateWelcomeMessage } from '../utils';

export function generateSellSection(
    closed_auctions: Auction[],
    open_auctions: Auction[],
    products: Product[],
    shipping_addresses: Record<number, String>,
): void {
    document.querySelector('section#sell')!.innerHTML = `
        <section id="sell">
            ${generateSellWelcomeSection()}
            ${generateProductsSection(products, open_auctions)}
            ${generateAuctionsSection(open_auctions, closed_auctions, products, shipping_addresses)}
        </section>
    `;
}

function generateSellWelcomeSection(): string {
    return `
        <section>
            ${generateWelcomeMessage()}
            <p>In this page you can manage your products and auctions.</p>
        </section>
    `;
}

function generateProductsSection(products: Product[], auctions: Auction[]): string {
    return `
        <section id="Products">
            <h2>Products</h2>
            <div class="flex-row">
                ${generateAddProductForm()}
                ${generateEditProductsTable(products, auctions)}
            </div>
        </section>
    `;
}

function generateAddProductForm(): string {
    return `
        <form action="/sell/product/new" method="post" enctype="multipart/form-data">
            <fieldset style="display: block">
                <legend>Add a new product</legend>
                <label for="name">Product Name:</label>
                <input type="text" id="name" name="product-name" required placeholder="Enter product name" />
                <label for="description">Product Description:</label>
                <textarea id="description" name="product-description" placeholder="Enter product description" maxlength="500" style="background-color: #e2e1e1"></textarea>
                <label for="price">Product Price (€):</label>
                <input type="number" id="price" name="product-price" required min="0.01" step="0.01" value="1.00" />
                <label for="image">Product Image:</label>
                <input type="file" id="image" name="product-image" required accept="image/png, image/jpeg" />
                <button type="submit">Add Product</button>
            </fieldset>
        </form>
    `;
}

function generateEditProductsTable(products: Product[], auctions: Auction[]): string {
    // Replace with dynamic data fetching logic
    if (products.length === 0) {
        return '';
    }

    return `
        <fieldset style="max-height: 630px; padding-top: 20px; padding-bottom: 20px; min-width: 60%;">
            <legend>Edit product details</legend>
            <div style="overflow-y: scroll; max-height: 610px; max-width: 99%">
                <table style="max-width: calc(100% - 20px); margin-right: 20px">
                    <thead>
                        <th>Name</th>
                        <th>Price</th>
                        <th>Description</th>
                        <th>Auction_id</th>
                        <th>Image</th>
                        <th>Submit edit</th>
                    </thead>
                    <tbody>
                        ${products.map((p) => generateEditProductRow(p, auctions)).join('')}
                    </tbody>
                </table>
            </div>
        </fieldset>
    `;
}

function generateEditProductRow(product: Product, auctions: Auction[]): string {
    return `
        <tr id="product-${product.id}">
            <form action="/sell/product/edit/" enctype="multipart/form-data" method="post">
                <input type="hidden" name="product-id" value="${product.id}" />
                <td>
                    <input type="text" name="product-name" value="${product.name}" required />
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
                            ${!product.canChangePrice(auctions) ? 'disabled' : ''} 
                            title="${product.canChangePrice(auctions) ? 'Change price' : 'Price cannot be changed because the product is auctioned and there is already a bid on it'}"
                        />
                    </div>
                </td>
                <td>
                    <textarea name="product-description" required>${product.description}</textarea>
                </td>
                <td>
                    <select 
                        name="product-auction-id" 
                        ${product.isAuctioned() && !product.canChangeAuction(auctions) ? 'disabled' : ''} 
                        title="${product.isAuctioned() && !product.canChangeAuction(auctions) ? 'Product auction cannot be changed unless there are no bids placed and there are at least two other products' : ''}"
                    >
                        <option value="0">No auction</option>
                        ${generateAuctionOptions(product.auctionId, auctions)}
                    </select>
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
                        />
                    </div>
                </td>
                <td>
                    <button type="submit">Update</button>
                </td>
            </form>
        </tr>
    `;
}

function generateAuctionOptions(selectedAuctionId: number, auctions: Auction[]): string {
    // Replace with dynamic data fetching logic
    return auctions
        .map(
            (auction) => `
        <option value="${auction.id}" ${auction.id === selectedAuctionId ? 'selected' : ''}>Auct. ${auction.id}</option>
    `,
        )
        .join('');
}

function generateAuctionsSection(
    open_auctions: Auction[],
    closed_auctions: Auction[],
    products: Product[],
    shipping_addresses: Record<number, String>,
): string {
    return `
        <section id="Auctions">
            <h2>Auctions</h2>
            ${generateCreateAuctionForm(products)}
            ${generateOpenAuctionsTable(open_auctions)}
            ${generateClosedAuctionsTable(closed_auctions, shipping_addresses)}
        </section>
    `;
}

function generateCreateAuctionForm(products: Product[]): string {
    return `
        <div style="width: 100%">
            <h3>Create a new auction</h3>
            <form action="/sell/auction/new" method="post">
                <div class="flex-row">
                    <div>
                        <fieldset style="display: block">
                            <legend>Auction details</legend>
                            <label for="final-bid-submission-date">Bid submission date</label>
                            <input type="date" name="final-bid-submission-date" id="final-bid-submission-date" required />
                            <label for="final-bid-submission-time"></label>
                            <input type="time" name="final-bid-submission-time" id="final-bid-submission-time" required />
                            <label for="min-bid-increment">Minimum bid-increment (€)</label>
                            <input type="number" name="min-bid-increment" id="min-bid-increment" required step="1" min="1" value="1" />
                        </fieldset>
                    </div>
                    <div>
                        <fieldset>
                            <legend>Product selection</legend>
                            ${generateUnauctionedProductsTable(products)}
                        </fieldset>
                    </div>
                </div>
                <button style="float: right; margin-top: 20px" type="submit">Create Auction</button>
            </form>
        </div>
    `;
}

function generateUnauctionedProductsTable(products: Product[]): string {
    // Replace with dynamic data fetching logic
    if (products.length === 0) {
        return '<div class="info"><p>No products available for auction</p></div>';
    }

    return `
        <table>
            <thead>
                <tr>
                    <th>Select product</th>
                    <th>Name</th>
                    <th>Price</th>
                    <th>Description</th>
                    <th>Image</th>
                </tr>
            </thead>
            <tbody>
                ${products.map(generateUnauctionedProductRow).join('')}
            </tbody>
        </table>
    `;
}

function generateUnauctionedProductRow(product: Product): string {
    return `
        <tr>
            <td><input type="checkbox" name="product-ids" value="${product.id}" checked /></td>
            <td>${product.name}</td>
            <td>€ ${product.price}</td>
            <td>${product.description}</td>
            <td><img src="/image/${product.imageFilename}" alt="Product Image" style="max-width: 100px; max-height: 100px;" /></td>
        </tr>
    `;
}

function generateOpenAuctionsTable(auctions: Auction[]): string {
    // Replace with dynamic data fetching logic

    if (auctions.length === 0) {
        return '<div class="info"><p>No open auctions</p></div>';
    }

    return `
        <div>
            <h3>Open auctions</h3>
            <table>
                <thead>
                    <tr>
                        <th>Auction id</th>
                        <th>Current highest bid</th>
                        <th>Minimum bid increment</th>
                        <th>Products</th>
                        <th>End time</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    ${auctions.map(generateOpenAuctionRow).join('')}
                </tbody>
            </table>
        </div>
    `;
}

function generateOpenAuctionRow(auction: Auction): string {
    return `
        <tr>
            <td>${auction.id}</td>
            <td>${auction.currentHighestBid ? `€ ${auction.currentHighestBid}` : '<div class="info"> <p> No bids yet</p></div>'}</td>
            <td>€ ${auction.minimumBidIncrement}</td>
            <td>${auction.products.map((product: any) => `<a href="#product-${product.id}">${product.name}</a>`).join(', ')}</td>
            <td>${auction.endTime}</td>
            <td>
                <form action="/sell/auction/close/" method="post">
                    <input type="hidden" name="id" value="${auction.id}" />
                    <button 
                        type="submit" 
                        ${!auction.canBeClosed() ? 'disabled' : ''} 
                        title="${auction.canBeClosed() ? 'Close this auction' : 'The auction has no bids or deadline has not expired yet'}"
                    >
                        Close Auction
                    </button>
                </form>
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
                <form action="/sell/auction" method="get">
                    <input type="hidden" name="id" value="${auction.id}" />
                    <button type="submit" class="details-button">Auction details</button>
                </form>
            </td>
        </tr>
    `;
}

function generateClosedAuctionsTable(
    auctions: Auction[],
    shippingAddresses: Record<number, String>,
): string {
    // Replace with dynamic data fetching logic
    if (auctions.length === 0) {
        return '<div class="info"><p>No closed auctions</p></div>';
    }

    return `
        <div>
            <h3>Closed auctions</h3>
            <table>
                <thead>
                    <tr>
                        <th>Final bid</th>
                        <th>End time</th>
                        <th>Winner</th>
                        <th>Shipping address</th>
                        <th>Auction details</th>
                    </tr>
                </thead>
                <tbody>
                    ${auctions
                        .map((auction, index) => {
                            generateClosedAuctionRow(auction, shippingAddresses[auction.id]);
                        })
                        .join('')}
                </tbody>
            </table>
        </div>
    `;
}

function generateClosedAuctionRow(auction: Auction, shippingAddress: String | null): string {
    return `
        <tr>
            <td>€ ${auction.currentHighestBid}</td>
            <td>${auction.endTime}</td>
            <td>${auction.currentHighestBid?.bidderUsername}</td>
            <td>${shippingAddress || '<div class="info"> <p>No bids yet</p></div>'}</td>
            <td>
                <form action="/sell/auction" method="get">
                    <input type="hidden" name="id" value="${auction.id}" />
                    <button type="submit">Auction details</button>
                </form>
            </td>
        </tr>
    `;
}
