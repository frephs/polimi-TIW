export function generateWelcomeMessage() {
    const userName = window.sessionStorage.getItem('user.name') || 'Guest';
    return `Hello <i>${userName}</i>`;
}

export function generateModalBox(content: string): HTMLElement {
    const modal = document.createElement('div');
    modal.className = 'modal';
    modal.innerHTML = `
        <div class="modal-content">
            <span class="close">&times;</span>
            ${content}
            <div id="modal-content"></div>
        </div>
    `;
    return modal;
}
