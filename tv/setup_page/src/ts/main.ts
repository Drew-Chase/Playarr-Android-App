// Fetch PIN and client ID from the TV's setup server
interface PinData {
    clientId: string;
    pinId: number | null;
    code: string | null;
}

async function fetchPinFromTV(): Promise<PinData> {
    const resp = await fetch("/api/pin");
    if (!resp.ok) throw new Error(`Failed to fetch PIN: ${resp.status}`);
    return await resp.json();
}

async function pollPlexPin(clientId: string, pinId: number): Promise<string | null> {
    const resp = await fetch(`https://plex.tv/api/v2/pins/${pinId}`, {
        method: "GET",
        headers: {
            "Accept": "application/json",
            "X-Plex-Product": "Playarr",
            "X-Plex-Client-Identifier": clientId,
        },
    });
    if (!resp.ok) throw new Error(`Failed to poll PIN: ${resp.status}`);
    const data = await resp.json();
    return data.authToken || null;
}

async function submitSetup(serverUrl: string, authToken: string) {
    await fetch("/api/setup", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({serverUrl, authToken}),
    });
}

const plexCodeEl = document.querySelector<HTMLElement>("#plex-code")!;
const plexLinkButton = document.querySelector<HTMLButtonElement>("#plex-link-button")!;
const completeButton = document.querySelector<HTMLButtonElement>("#complete-button")!;
const copyTextEl = document.querySelector<HTMLElement>(".copy-text")!;

let plexAuthToken: string | null = null;
let pollInterval: ReturnType<typeof setInterval> | undefined;
let pollTimeout: ReturnType<typeof setTimeout> | undefined;

async function init() {
    plexCodeEl.textContent = "...";

    try {
        // Wait for the TV to generate the PIN (may take a moment on first load)
        let pinData: PinData;
        for (let i = 0; i < 15; i++) {
            pinData = await fetchPinFromTV();
            if (pinData.code && pinData.pinId) break;
            await new Promise(r => setTimeout(r, 1000));
        }

        pinData = await fetchPinFromTV();
        if (!pinData.code || !pinData.pinId) {
            plexCodeEl.textContent = "Error";
            return;
        }

        plexCodeEl.textContent = pinData.code;
        startPolling(pinData.clientId, pinData.pinId);
    } catch {
        plexCodeEl.textContent = "Error";
    }
}

function startPolling(clientId: string, pinId: number) {
    clearInterval(pollInterval);
    clearTimeout(pollTimeout);

    pollInterval = setInterval(async () => {
        try {
            const token = await pollPlexPin(clientId, pinId);
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

    // Timeout after 5 minutes - reload to get a fresh PIN
    pollTimeout = setTimeout(() => {
        clearInterval(pollInterval);
        init();
    }, 300_000);
}

// Tap code to copy
plexCodeEl.addEventListener("click", () => {
    const code = plexCodeEl.textContent?.trim();
    if (!code || code === "..." || code === "Error") return;

    navigator.clipboard.writeText(code);

    plexCodeEl.classList.remove("copied");
    void plexCodeEl.offsetWidth;
    plexCodeEl.classList.add("copied");

    copyTextEl.textContent = "Copied!";
    copyTextEl.style.color = "var(--primary-color)";
    setTimeout(() => {
        plexCodeEl.classList.remove("copied");
        copyTextEl.textContent = "Tap to Copy";
        copyTextEl.style.color = "";
    }, 1200);
});

// Link button opens plex.tv/link
plexLinkButton.addEventListener("click", () => {
    if (!plexAuthToken) {
        window.open("https://plex.tv/link", "_blank");
    }
});

// Complete button submits setup
completeButton.addEventListener("click", async () => {
    const serverUrl = document.querySelector<HTMLInputElement>("#server-url")!.value;
    if (!serverUrl) {
        alert("Please enter a server URL");
        return;
    }
    if (!plexAuthToken) return;
    completeButton.disabled = true;
    completeButton.textContent = "Completing...";
    await submitSetup(serverUrl, plexAuthToken);
    completeButton.textContent = "Done!";
});

init();
