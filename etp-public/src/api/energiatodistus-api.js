const baseUrl = '/api/public';

const energiatodistuksetUrl = `${baseUrl}/energiatodistukset`;
const energiatodistuksetCountUrl = `${energiatodistuksetUrl}/count`;

const fetchJson = (fetch, url) => fetch(url).then(response => response.json());

const zip = obj => {
  const result = [];
  for (const key in obj) {
    result.push([key, obj[key]]);
  }

  return result;
};

export const namespaceSearchParams = ([and]) => [
  [...and.map(([op, key, value]) => [op, `energiatodistus.${key}`, value])]
];

export const energiatodistukset = (fetch, opts) => {
  const filteredOpts = Object.keys(opts).reduce(
    (acc, item) => ({ ...acc, ...(opts[item] ? { [item]: opts[item] } : {}) }),
    {}
  );

  if (
    !Object.keys(filteredOpts).some(item => ['where', 'keyword'].includes(item))
  )
    return Promise.resolve([]);

  if (filteredOpts?.where)
    filteredOpts.where = JSON.stringify(
      namespaceSearchParams(filteredOpts.where)
    );

  const qs = encodeURI(
    zip(filteredOpts)
      .map(item => item.join('='))
      .join('&')
  );

  const url = `${energiatodistuksetUrl}${qs.length ? `?${qs}` : ''}`;

  return fetchJson(fetch, url);
};

export const energiatodistuksetCount = (fetch, opts) => {
  const filteredOpts = Object.keys(opts).reduce(
    (acc, item) => ({ ...acc, ...(opts[item] ? { [item]: opts[item] } : {}) }),
    {}
  );

  if (
    !Object.keys(filteredOpts).some(item => ['where', 'keyword'].includes(item))
  )
    return Promise.resolve([]);

  if (filteredOpts.where)
    filteredOpts.where = JSON.stringify(
      namespaceSearchParams(filteredOpts.where)
    );

  const qs = encodeURI(
    zip(filteredOpts)
      .map(item => item.join('='))
      .join('&')
  );

  const url = `${energiatodistuksetCountUrl}${qs.length ? `?${qs}` : ''}`;

  return fetchJson(fetch, url);
};
export const energiatodistuksetCountAll = fetch =>
  fetchJson(fetch, energiatodistuksetCountUrl);

export const kayttotarkoitusluokat = (fetch, versio) =>
  fetchJson(fetch, `${energiatodistuksetUrl}/kayttotarkoitusluokat/${versio}`);

export const alakayttotarkoitusluokat = (fetch, versio) =>
  fetchJson(
    fetch,
    `${energiatodistuksetUrl}/alakayttotarkoitusluokat/${versio}`
  );

export const laatimisvaiheet = fetch =>
  fetchJson(fetch, `${energiatodistuksetUrl}/laatimisvaiheet`);

export const lammonjako = fetch =>
  fetchJson(fetch, `${energiatodistuksetUrl}/lammonjako`);
export const lammitysmuoto = fetch =>
  fetchJson(fetch, `${energiatodistuksetUrl}/lammitysmuoto`);
export const ilmanvaihtotyyppi = fetch =>
  fetchJson(fetch, `${energiatodistuksetUrl}/ilmanvaihtotyyppi`);

export const kielisyys = fetch =>
  fetchJson(fetch, `${energiatodistuksetUrl}/kielisyys`);

export const eLuokka = (
  fetch,
  versio,
  alakayttotarkoitusluokka,
  nettoala,
  eLuku
) =>
  fetchJson(
    fetch,
    `${energiatodistuksetUrl}/e-luokka/${versio}/${alakayttotarkoitusluokka}/${nettoala}/${eLuku}`
  );
