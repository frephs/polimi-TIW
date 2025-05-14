import { User } from './prototypes.js';
import { makeXHRequest } from './requests.js';
import { controllerSwitcher } from './routing.js';

export function generateWelcomeMessage(user: User) {
    return `<h1>Hello <i>${user.name}</i></h1>`;
}

export function generateModalBox(content: string): HTMLElement {
    const modal = document.createElement('div');
    modal.className = 'modal';
    modal.id = 'modal';
    modal.innerHTML = `
        <div class="modal-content">
            <span class="close-button">
            
            </span>
            ${content}
            <div id="modal-content"></div>
        </div>
    `;

    modal.style.height = document.body.scrollHeight + 'px';

    const closeButton = modal.querySelector('.close-button') as HTMLElement;
    closeButton.onclick = function () {
        modal.style.display = 'none';
    };
    return modal;
}

export function showMessage(message: string) {
    const messageSection: HTMLElement | null = document.querySelector('section#message');

    if (messageSection) {
        messageSection.innerHTML = message;
        messageSection.style.display = 'block';

        messageSection.onclick = function () {
            hideMessage();
        };
    }
}

export function hideMessage() {
    const messageSection: HTMLElement | null = document.querySelector('section#message');

    if (messageSection) {
        messageSection.style.display = 'none';
    }
}

export function showBreadcrumb(breadcrumb: string) {
    const breadcrumbSection: HTMLElement | null = document.querySelector(
        'section#breadcrumb .breadcrumb',
    );
    if (breadcrumbSection) {
        breadcrumbSection.innerHTML = breadcrumb;
        breadcrumbSection.style.display = 'block';
    }
}

export function hideAllPages(pageSections: Map<string, HTMLElement>) {
    pageSections.forEach((section, _) => {
        section.style.display = 'none';
    });
}

export function showPage(page: string, pageSections: Map<string, HTMLElement>) {
    hideAllPages(pageSections);
    const pageSection = pageSections.get(page);
    if (pageSection) {
        pageSection.style.display = 'block';
    }
}

export function showModal(pageSection: HTMLElement, content: string) {
    pageSection.append(generateModalBox(content));
    const modal: HTMLElement | null = document.querySelector(
        'section#' + pageSection.id + ' .modal',
    );
    if (modal) {
        modal.style.display = 'block';
        window.location.href = window.location.href.split('#')[0] + `#${modal.id}`;
    }
}

export function processForms(pageSections: Map<string, HTMLElement>) {
    const forms: NodeListOf<HTMLFormElement> = document.querySelectorAll('form');
    forms.forEach((form) => {
        form.addEventListener('submit', function (event) {
            event.preventDefault();
            const formData = new FormData(form);
            const actionUrl = form.getAttribute('action');
            const method = form.getAttribute('method') || 'GET';
            if (actionUrl) {
                makeXHRequest(method, actionUrl, formData, controllerSwitcher, pageSections);
            }
        });
    });
}

export function processNavLinks(pageSections: Map<string, HTMLElement>) {
    const navLinks = document.querySelectorAll('nav a');
    console.log(navLinks);
    navLinks.forEach((link) => {
        console.log('Event substituted');
        link.addEventListener('click', function (event) {
            event.preventDefault();
            const href = link.getAttribute('href');
            if (href) {
                makeXHRequest('GET', href, new FormData(), controllerSwitcher, pageSections);
            }
        });
    });
}

export function processUpdateProductsForms(pageSections: Map<string, HTMLElement>) {
    document.querySelectorAll<HTMLButtonElement>('.update-product-btn').forEach((button) => {
        //make sure there aren't any other event listeners
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

            makeXHRequest(
                'post',
                `/yourauction/sell/product/edit/`,
                formData,
                controllerSwitcher,
                pageSections,
            );
        });
    });
}

export function processRedirects(pageSections: Map<string, HTMLElement>) {
    if (window.sessionStorage.getItem('from')) {
        makeXHRequest(
            'GET',
            '/yourauction/' + window.sessionStorage.getItem('from'),
            new FormData(),
            controllerSwitcher,
            pageSections,
        );

        window.sessionStorage.removeItem('from');
    } else {
        showPage('account-details', pageSections);
    }

    if (window.sessionStorage.getItem('ever_logged_in') === 'true') {
        makeXHRequest('GET', 'yourauction/sell', new FormData(), controllerSwitcher, pageSections);
    } else {
        makeXHRequest('GET', 'yourauction/buy', new FormData(), controllerSwitcher, pageSections);
    }

    window.sessionStorage.setItem('ever_logged_in', 'true');
}
