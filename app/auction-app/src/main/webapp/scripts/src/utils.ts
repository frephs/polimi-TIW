import { User } from './prototypes.js';

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
