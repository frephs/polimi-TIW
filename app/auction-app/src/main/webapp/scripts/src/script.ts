import { generateModalBox } from './utils';
import { generateSellAuctionDetails } from './components/sell-auction-details';
import { generateBuyAuctionDetails } from './components/buy-auction-details';
import { generateSellSection } from './components/sell';
import { generateBuySection } from './components/buy';
import { updateUserDetails } from './components/account-details';

document.addEventListener('DOMContentLoaded', function () {
    const messageSection: HTMLElement | null = document.querySelector('section#message');
    const breadcrumbSection: HTMLElement | null = document.querySelector('section#breadcrumb');
    const pageSections: Map<string, HTMLElement> = new Map();

    [
        'sell',
        'buy',
        'account-details',
        'sell .modal#sell-auction-details',
        'buy .modal#buy-auction-details',
    ].forEach((section) => {
        const pageSection: HTMLElement | null = document.querySelector(`section#${section}`);
        if (pageSection) {
            pageSections.set(section, pageSection);
        }
    });

    function showMessage(message: string) {
        if (messageSection) {
            messageSection.innerHTML = message;
            messageSection.style.display = 'block';
        }
    }

    function hideMessage() {
        if (messageSection) {
            messageSection.style.display = 'none';
        }
    }

    function showBreadcrumb(breadcrumb: string) {
        if (breadcrumbSection) {
            breadcrumbSection.innerHTML = breadcrumb;
            breadcrumbSection.style.display = 'block';
        }
    }

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
    }

    function makeXHRequest(method: string, url: string, data: FormData) {
        const xhr = new XMLHttpRequest();
        xhr.open(method, url, true);
        xhr.setRequestHeader('X-Requested-With', 'XMLHttpRequest');
        xhr.onreadystatechange = function () {
            if (xhr.status >= 300 && xhr.status < 400) {
                makeXHRequest('GET', xhr.getResponseHeader('Location') || '', new FormData());
            } else {
                processXHRResponse(xhr);
            }
        };
        xhr.send(data);
    }

    function processXHRResponse(xhr: XMLHttpRequest) {
        const message = window.sessionStorage.getItem('FLASH_message');
        const breadcrumb = xhr.response.breadcrumb;
        let page = xhr.response.page;

        if (message) {
            showMessage(message);
            window.sessionStorage.removeItem('FLASH_message');
        }

        if (breadcrumb) {
            showBreadcrumb(breadcrumb);
        }

        if (page === 'controller') {
            page = 'buy';
        }

        if (page) {
            let modal;
            switch (page) {
                case 'sell':
                    generateSellSection(
                        xhr.response.closed_auctions,
                        xhr.response.open_auctions,
                        xhr.response.products,
                        xhr.response.closed_auction_shipping_addresses,
                    );
                    showPage('sell');
                    break;
                case 'buy':
                    generateBuySection(
                        xhr.response.auctions,
                        xhr.response.won_auctions,
                        xhr.response.closed_auction_shipping_address,
                    );
                    showPage('buy');
                    break;
                case 'account-details':
                    updateUserDetails(xhr.response.user);
                    showPage('account-details');
                    break;
                case 'sell-auction-details':
                    modal = generateModalBox(
                        generateSellAuctionDetails(
                            xhr.response.auction,
                            xhr.response.bids,
                            xhr.response.closed_auction_shipping_address,
                        ),
                    );
                    pageSections.get('buy')?.append(modal);
                    showPage('buy .modal#buy-auction-details');
                    break;
                case 'buy-auction-details':
                    modal = generateModalBox(
                        generateBuyAuctionDetails(xhr.response.auction, xhr.response.bids),
                    );
                    pageSections.get('buy')?.append(modal);
                    showPage('buy .modal#buy-auction-details');
                    break;
                default:
                    hideAllPages();
            }
        }
    }

    function processForms() {
        const forms = document.querySelectorAll('form');
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
});
