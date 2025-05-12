import { generateModalBox, showBreadcrumb, showMessage } from './utils.js';
import { generateSellSection } from './components/sell.js';
import { Auction, Bid, Product, User } from './prototypes.js';
import { generateBuySection } from './components/buy.js';
import { updateUserDetails } from './components/account-details.js';
import { generateSellAuctionDetails } from './components/sell-auction-details.js';
import { generateBuyAuctionDetails } from './components/buy-auction-details.js';

document.addEventListener('DOMContentLoaded', function () {
    const pageSections: Map<string, HTMLElement> = new Map();

    processNavLinks();
    processForms();

    ['sell', 'buy', 'account-details'].forEach((section) => {
        const pageSection: HTMLElement | null = document.querySelector(`section#${section}`);
        if (pageSection) {
            pageSections.set(section, pageSection);
        }
    });

    function hideAllPages() {
        pageSections.forEach((section, _) => {
            section.style.display = 'none';
        });
    }

    function showPage(page: string) {
        hideAllPages();
        const pageSection = pageSections.get(page);
        if (pageSection) {
            pageSection.style.display = 'block';
        }

        processForms();
        processNavLinks();
    }

    function showModal(pageSection: HTMLElement, content: string) {
        pageSection.append(generateModalBox(content));
        const modal: HTMLElement | null = document.querySelector(
            'section#' + pageSection.id + ' .modal',
        );
        if (modal) {
            modal.style.display = 'block';
            window.location.href = window.location.href.split('#')[0] + `#${modal.id}`;
        }
    }

    function makeXHRequest(method: string, url: string, data: FormData) {
        const xhr = new XMLHttpRequest();

        let finalUrl = url;

        if (method.toUpperCase() === 'GET') {
            const params = new URLSearchParams();
            data.forEach((value, key) => {
                params.append(key, value.toString());
            });
            finalUrl += params.toString() ? `?${params.toString()}` : '';
        }

        xhr.open(method, finalUrl, true);
        xhr.setRequestHeader('X-Requested-With', 'XMLHttpRequest');

        xhr.onreadystatechange = function () {
            if (xhr.readyState === XMLHttpRequest.DONE) {
                if (
                    xhr.status >= 300 &&
                    xhr.status < 400 &&
                    !xhr.responseURL.endsWith('controller')
                ) {
                    makeXHRequest('GET', xhr.getResponseHeader('location') || '', new FormData());
                } else if (xhr.status >= 500 && xhr.status < 600) {
                    const parser = new DOMParser();
                    const doc = parser.parseFromString(xhr.response, 'text/html');
                    const newContent = doc.querySelector('body')?.innerHTML;
                    if (newContent) {
                        window.location.href = xhr.responseURL;
                        document.body.innerHTML = newContent;
                    }
                } else {
                    processXHRResponse(xhr);
                }
            }
        };
        xhr.send(data);
    }

    function processXHRResponse(xhr: XMLHttpRequest) {
        if (xhr.responseText.toLowerCase().startsWith('<!doctype html>')) {
            const parser = new DOMParser();
            const doc = parser.parseFromString(xhr.response, 'text/html');
            const newContent = doc.querySelector('body')?.innerHTML;
            if (newContent) {
                window.location.href = xhr.responseURL;
                document.body.innerHTML = newContent;
            }
            return;
        } else if (xhr.responseText.startsWith('{')) {
            const response = JSON.parse(xhr.response);
            controllerSwitcher(response);
        }
    }

    function controllerSwitcher(response: any) {
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
                showPage('sell');
                processUpdateProductsForms();
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
                );
                showPage('buy');
                break;
            case '/account/details':
                if (breadcrumb) {
                    showBreadcrumb(breadcrumb);
                }
                updateUserDetails(response.user);
                showPage('account-details');
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
                processUpdateProductsForms();

                break;
            case '/buy/auction-detail':
                showModal(
                    pageSections.get('buy') as HTMLElement,
                    generateBuyAuctionDetails(
                        new Auction(response.auction),
                        response.bids.map((bid: any) => new Bid(bid)),
                    ),
                );
                break;
            default:
                hideAllPages();
        }

        if (message) {
            showMessage(message);
            window.sessionStorage.removeItem('FLASH_message');
        }
    }

    function processForms() {
        const forms: NodeListOf<HTMLFormElement> = document.querySelectorAll('form');
        forms.forEach((form) => {
            form.addEventListener('submit', function (event) {
                event.preventDefault();
                const formData = new FormData(form);
                const actionUrl = form.getAttribute('action');
                const method = form.getAttribute('method') || 'GET';
                if (actionUrl) {
                    makeXHRequest(method, actionUrl, formData);
                }
            });
        });
    }

    function processNavLinks() {
        const navLinks = document.querySelectorAll('nav a');
        console.log(navLinks);
        navLinks.forEach((link) => {
            console.log('Event substituted');
            link.addEventListener('click', function (event) {
                event.preventDefault();
                const href = link.getAttribute('href');
                if (href) {
                    makeXHRequest('GET', href, new FormData());
                }
            });
        });
    }

    function processUpdateProductsForms() {
        document.querySelectorAll<HTMLButtonElement>('.update-product-btn').forEach((button) => {
            button.addEventListener('click', async () => {
                const row = button.closest('tr');
                const productIdFormElement: HTMLFormElement | null =
                    button.closest('input[type="hidden"]');
                const productId: number | null = productIdFormElement?.value;
                if (!row) {
                    console.error(`Row for product id ${productId} not found.`);
                    return;
                }

                const formData = new FormData();

                // Collect inputs from the row
                row.querySelectorAll('input, select, textarea').forEach((element) => {
                    if (element instanceof HTMLInputElement && element.type === 'file') {
                        if (element.files && element.files.length > 0) {
                            formData.append(element.name, element.files[0]);
                        }
                    } else if (
                        element instanceof HTMLInputElement ||
                        element instanceof HTMLSelectElement ||
                        element instanceof HTMLTextAreaElement
                    ) {
                        if (!element.disabled) {
                            formData.append(element.name, element.value);
                        }
                    }
                });

                makeXHRequest('post', `/yourauction/sell/product/edit/`, formData);
            });
        });
    }
});
