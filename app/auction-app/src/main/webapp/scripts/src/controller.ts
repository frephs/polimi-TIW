import { processForms, processNavLinks, processRedirects } from './utils.js';

document.addEventListener('DOMContentLoaded', function () {
    const pageSections: Map<string, HTMLElement> = new Map();
    ['sell', 'buy', 'account-details'].forEach((section) => {
        const pageSection: HTMLElement | null = document.querySelector(`section#${section}`);
        if (pageSection) {
            pageSections.set(section, pageSection);
        }
    });

    processNavLinks(pageSections);
    processForms(pageSections);

    processRedirects(pageSections);
});
