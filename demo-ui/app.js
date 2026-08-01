const KEYCLOAK_TOKEN_URL = "http://localhost:8083/realms/power-outage/protocol/openid-connect/token";
const API_URL = "http://localhost:8000/api/v1/outage-reports";
let accessToken = "";
const connection = document.querySelector("#connection");
const loginButton = document.querySelector("#loginButton");
const submitButton = document.querySelector("#submitButton");
const refreshButton = document.querySelector("#refreshButton");
const reportForm = document.querySelector("#reportForm");
const formMessage = document.querySelector("#formMessage");
const reportRows = document.querySelector("#reportRows");

function setConnection(text, state) { connection.textContent = text; connection.className = `status ${state}`; }

async function login() {
  loginButton.disabled = true; setConnection("Connecting...", "waiting"); formMessage.textContent = "";
  try {
    const body = new URLSearchParams({grant_type: "password", client_id: "power-outage-api", username: "operator01", password: "operator123"});
    const response = await fetch(KEYCLOAK_TOKEN_URL, {method: "POST", headers: {"Content-Type": "application/x-www-form-urlencoded"}, body});
    if (!response.ok) throw new Error("Keycloak sign-in failed");
    accessToken = (await response.json()).access_token;
    setConnection("System connected", "connected"); loginButton.textContent = "Reconnect"; submitButton.disabled = false; refreshButton.disabled = false;
    await loadReports();
  } catch (error) {
    setConnection("Connection failed", "error"); formMessage.textContent = `${error.message}. Confirm that Docker services are running.`;
  } finally { loginButton.disabled = false; }
}

async function apiRequest(url, options = {}) {
  const response = await fetch(url, {...options, headers: {Authorization: `Bearer ${accessToken}`, ...(options.headers || {})}});
  const payload = await response.json().catch(() => ({}));
  if (!response.ok) throw new Error(payload.message || `Request failed (${response.status})`);
  return payload;
}

async function loadReports() {
  reportRows.innerHTML = '<tr><td colspan="5" class="empty">Loading...</td></tr>';
  try {
    const page = await apiRequest(`${API_URL}?page=0&size=20`); const reports = page.content || [];
    reportRows.innerHTML = reports.length ? reports.map(report => `<tr><td><strong>${report.reportCode}</strong></td><td>${report.customerCode}</td><td>${report.servicePointCode}</td><td><span class="badge">${report.status}</span></td><td>${new Date(report.createdAt).toLocaleString()}</td></tr>`).join("") : '<tr><td colspan="5" class="empty">No reports yet.</td></tr>';
  } catch (error) { reportRows.innerHTML = `<tr><td colspan="5" class="empty error-text">${error.message}</td></tr>`; }
}

reportForm.addEventListener("submit", async event => {
  event.preventDefault(); submitButton.disabled = true; formMessage.textContent = "Submitting...";
  try {
    const report = await apiRequest(API_URL, {method: "POST", headers: {"Content-Type": "application/json", "Idempotency-Key": crypto.randomUUID()}, body: JSON.stringify(Object.fromEntries(new FormData(reportForm).entries()))});
    formMessage.textContent = `Created successfully: ${report.reportCode}`; await loadReports();
  } catch (error) { formMessage.textContent = error.message; } finally { submitButton.disabled = false; }
});
loginButton.addEventListener("click", login); refreshButton.addEventListener("click", loadReports); login();
