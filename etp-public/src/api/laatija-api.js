const baseUrl = '/api/public';

const laatijatUrl = `${baseUrl}/laatijat`;
const laatijatCountUrl = `${laatijatUrl}/count`;
const patevyydetUrl = `${baseUrl}/patevyydet`;

export const laatijat = fetch =>
  fetch(laatijatUrl).then(response => response.json());

export const laatijatCount = fetch =>
  fetch(laatijatCountUrl).then(response => response.json());

export const patevyydet = fetch =>
  fetch(patevyydetUrl).then(response => response.json());
