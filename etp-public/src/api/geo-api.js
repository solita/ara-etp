const baseUrl = '/api/public';

const toimintaalueetUrl = `${baseUrl}/toimintaalueet/`;
const countriesUrl = `${baseUrl}/toimintaalueet/`;
const postinumerotUrl = `${baseUrl}/postinumerot`;
const kunnatUrl = `${baseUrl}/kunnat`;

const fetchJson = (fetch, url) => fetch(url).then(response => response.json());

export const toimintaalueet = fetch => fetchJson(fetch, toimintaalueetUrl);
export const countries = fetch => fetchJson(fetch, countriesUrl);
export const postinumerot = fetch => fetchJson(fetch, postinumerotUrl);
export const kunnat = fetch => fetchJson(fetch, kunnatUrl);
