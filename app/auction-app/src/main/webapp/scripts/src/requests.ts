export function makeXHRequest(
    method: string,
    url: string,
    data: FormData,
    jsonParserCallback: Function,
    pageSections: Map<string, HTMLElement>,
) {
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
            if (xhr.status >= 300 && xhr.status < 400 && !xhr.responseURL.endsWith('controller')) {
                makeXHRequest(
                    'GET',
                    xhr.getResponseHeader('location') || '',
                    new FormData(),
                    jsonParserCallback,
                    pageSections,
                );
            } else if (xhr.status >= 500 && xhr.status < 600) {
                const parser = new DOMParser();
                const doc = parser.parseFromString(xhr.response, 'text/html');
                const newContent = doc.querySelector('body')?.innerHTML;
                if (newContent) {
                    window.location.href = xhr.responseURL;
                    document.body.innerHTML = newContent;
                }
            } else {
                processXHRResponse(xhr, jsonParserCallback, pageSections);
            }
        }
    };
    xhr.send(data);
}

export function processXHRResponse(
    xhr: XMLHttpRequest,
    jsonParserCallback: Function,
    pageSections: Map<string, HTMLElement>,
) {
    if (xhr.responseText.toLowerCase().startsWith('<!doctype html>')) {
        const parser = new DOMParser();
        const doc = parser.parseFromString(xhr.responseText, 'text/html');
        const newContent = doc.querySelector('body')?.innerHTML;
        if (newContent) {
            window.location.href = xhr.responseURL;
            document.body.innerHTML = newContent;
        }
        return;
    } else if (xhr.responseText.startsWith('{')) {
        const response = JSON.parse(xhr.responseText);
        jsonParserCallback(response, pageSections);
    }
}
