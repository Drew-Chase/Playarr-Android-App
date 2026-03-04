const PLEX_PRODUCT = "Playarr";
const PLEX_CLIENT_ID = "playarr-tv-" + getOrCreateClientId();

function getOrCreateClientId(): string {
    const key = "playarr-client-id";
    let id = localStorage.getItem(key);
    if (!id) {
        id = crypto.randomUUID();
        localStorage.setItem(key, id);
    }
    return id;
}

const plexHeaders: HeadersInit = {
    "Accept": "application/json",
    "X-Plex-Product": PLEX_PRODUCT,
    "X-Plex-Client-Identifier": PLEX_CLIENT_ID,
};

async function requestPin(): Promise<{ id: number; code: string }> {
    const resp = await fetch("https://plex.tv/api/v2/pins", {
        method: "POST",
        headers: {
            ...plexHeaders,
            "Content-Type": "application/x-www-form-urlencoded",
        },
        body: "strong=false",
    });
    if (!resp.ok) throw new Error(`Failed to request PIN: ${resp.status}`);
    const data = await resp.json();
    return {id: data.id, code: data.code};
}

async function pollPin(id: number): Promise<string | null> {
    const resp = await fetch(`https://plex.tv/api/v2/pins/${id}`, {
        method: "GET",
        headers: plexHeaders,
    });
    if (!resp.ok) throw new Error(`Failed to poll PIN: ${resp.status}`);
    const data = await resp.json();
    return data.authToken || null;
}

async function complete(serverUrl: string, plex_auth_token: string) {
    const url = new URL("/complete", window.location.origin);
    url.searchParams.set("plex_auth_token", plex_auth_token);
    url.searchParams.set("serverUrl", serverUrl);
    await fetch(url.toString());
}

const plexCodeEl = document.querySelector<HTMLElement>("#plex-code")!;
const plexLinkButton = document.querySelector<HTMLButtonElement>("#plex-link-button")!;
const completeButton = document.querySelector<HTMLButtonElement>("#complete-button")!;
let plexAuthToken: string | null = null;
let pollInterval: ReturnType<typeof setInterval> | undefined;
let pollTimeout: ReturnType<typeof setTimeout> | undefined;

// Request a PIN immediately on load and display it
async function initPin() {
    plexCodeEl.textContent = "...";
    try {
        const {id, code} = await requestPin();
        plexCodeEl.textContent = code;
        startPolling(id);
    } catch {
        plexCodeEl.textContent = "Error";
    }
}

function startPolling(pinId: number) {
    clearInterval(pollInterval);
    clearTimeout(pollTimeout);

    pollInterval = setInterval(async () => {
        try {
            const token = await pollPin(pinId);
            if (token) {
                clearInterval(pollInterval);
                clearTimeout(pollTimeout);
                plexAuthToken = token;
                plexLinkButton.textContent = "Linked!";
                plexLinkButton.classList.add("linked");
                plexLinkButton.disabled = true;
                completeButton.disabled = false;
            }
        } catch {
            // continue polling
        }
    }, 2000);

    pollTimeout = setTimeout(() => {
        clearInterval(pollInterval);
        // Request a fresh PIN after timeout
        initPin();
    }, 300_000);
}

// Tap code to copy
const copyTextEl = document.querySelector<HTMLElement>(".copy-text")!;
plexCodeEl.addEventListener("click", () => {
    const code = plexCodeEl.textContent?.trim();
    if (!code || code === "..." || code === "Error") return;

    navigator.clipboard.writeText(code);

    // Pop animation on the code
    plexCodeEl.classList.remove("copied");
    void plexCodeEl.offsetWidth; // force reflow to restart animation
    plexCodeEl.classList.add("copied");

    // Flash the label to "Copied!"
    copyTextEl.textContent = "Copied!";
    copyTextEl.style.color = "var(--primary-color)";
    setTimeout(() => {
        plexCodeEl.classList.remove("copied");
        copyTextEl.textContent = "Tap to Copy";
        copyTextEl.style.color = "";
    }, 1200);
});

// Button opens plex.tv/link in a new tab
plexLinkButton.addEventListener("click", () => {
    if (!plexAuthToken) {
        window.open("https://plex.tv/link", "_blank");
    }
});

// Complete button
completeButton.addEventListener("click", async () => {
    const serverUrl = document.querySelector<HTMLInputElement>("#server-url")!.value;
    if (!serverUrl) {
        alert("Please enter a server URL");
        return;
    }
    if (!plexAuthToken) return;
    completeButton.disabled = true;
    completeButton.textContent = "Completing...";
    await complete(serverUrl, plexAuthToken);
});

initPin();