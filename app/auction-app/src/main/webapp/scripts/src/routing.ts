import {
    processForms,
    processUpdateProductsForms,
    showBreadcrumb,
    showMessage,
    showModal,
    showPage,
} from './utils.js';
import { generateSellSection } from './components/sell.js';
import { Auction, Bid, Product, User } from './prototypes.js';
import { generateBuySection } from './components/buy.js';
import { updateUserDetails } from './components/account-details.js';
import { generateSellAuctionDetails } from './components/sell-auction-details.js';
import { generateBuyAuctionDetails } from './components/buy-auction-details.js';

export function controllerSwitcher(response: any, pageSections: Map<string, HTMLElement>) {
    const breadcrumb = response.breadcrumb;
    const message = response.FLASH_message;

    switch (response.page) {
        case '/sell/index':
            if (breadcrumb) {
                showBreadcrumb(breadcrumb);
            }
            generateSellSection(
                response.closed_auctions.map((auction: any) => new Auction(auction)),
                response.open_auctions.map((auction: any) => new Auction(auction)),
                response.products.map((product: any) => new Product(product)),
                response.unauctioned_products.map((product: any) => new Product(product)),
                response.closed_auction_shipping_addresses,
                new User(response.user),
            );
            showPage('sell', pageSections);
            processForms(pageSections);
            processUpdateProductsForms(pageSections);
            break;
        case '/buy/index':
        case '/buy/search':
            if (breadcrumb) {
                showBreadcrumb(breadcrumb);
            }
            generateBuySection(
                response.auctions?.map((auction: any) => new Auction(auction)),
                response.wonAuctions.map((auction: any) => new Auction(auction)),
                response.closed_auction_shipping_addresses,
                new User(response.user),
                response.searchQuery,
            );
            showPage('buy', pageSections);
            processForms(pageSections);
            break;
        case '/account/details':
            if (breadcrumb) {
                showBreadcrumb(breadcrumb);
            }
            updateUserDetails(new User(response.user));
            showPage('account-details', pageSections);
            break;
        case '/sell/auction-detail':
            showModal(
                pageSections.get('sell') as HTMLElement,
                generateSellAuctionDetails(
                    new Auction(response.auction),
                    response.bids.map((bid: any) => new Bid(bid)),
                    response.closed_auction_shipping_address,
                ),
            );
            processForms(pageSections);
            processUpdateProductsForms(pageSections);

            break;
        case '/buy/auction/details':
            showModal(
                pageSections.get('buy') as HTMLElement,
                generateBuyAuctionDetails(
                    new Auction(response.auction),
                    response.bids.map((bid: any) => new Bid(bid)),
                ),
            );
            processForms(pageSections);
            break;
    }

    if (message) {
        showMessage(message);
        window.sessionStorage.removeItem('FLASH_message');
    }
}
