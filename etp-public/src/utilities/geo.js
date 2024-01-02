export const isLabelMatch = haku => item =>
  item['label-fi'].toUpperCase() === haku.toUpperCase() ||
  item['label-sv'].toUpperCase() === haku.toUpperCase();

export const findToimintaalue = (toimintaalueet, toimintaalueId) =>
  toimintaalueet.find(toimintaalue => toimintaalue.id === toimintaalueId);

export const findToimintaalueetByMaakunta = (maakunta, toimintaalueet) =>
  new Set(
    toimintaalueet
      .filter(isLabelMatch(maakunta))
      .map(toimintaalue => toimintaalue.id)
  );

export const findToimintaalueIdsByKuntaIds = (kuntaIds, kunnat) =>
  new Set(
    kunnat
      .filter(kunta => kuntaIds.has(kunta.id))
      .map(kunta => kunta['toimintaalue-id'])
  );

export const findToimintaalueIdsByKunta = (kunta, kunnat) =>
  findToimintaalueIdsByKuntaIds(
    new Set(kunnat.filter(isLabelMatch(kunta)).map(k => k.id)),
    kunnat
  );

export const findKuntaIdsByPostitoimipaikka = (
  postitoimipaikka,
  postinumerot
) =>
  new Set(
    postinumerot
      .filter(isLabelMatch(postitoimipaikka))
      .map(numero => numero['kunta-id'])
  );

export const findKuntaIdsByPostinumero = (postinumero, postinumerot) =>
  new Set(
    postinumerot
      .filter(numero => numero.id === postinumero)
      .map(numero => numero['kunta-id'])
  );

export const findToimintaalueIds = (
  haku,
  toimintaalueet,
  kunnat,
  postinumerot
) => {
  const postinumero = parseInt(haku);
  if (!isNaN(postinumero)) {
    return findToimintaalueIdsByKuntaIds(
      findKuntaIdsByPostinumero(postinumero, postinumerot),
      kunnat
    );
  }

  return new Set([
    ...findToimintaalueIdsByKuntaIds(
      findKuntaIdsByPostitoimipaikka(haku, postinumerot),
      kunnat
    ),
    ...findToimintaalueIdsByKunta(haku, kunnat),
    ...findToimintaalueetByMaakunta(haku, toimintaalueet)
  ]);
};

export const padPostinumero = postinumero =>
  `00000${postinumero}`.split('').reverse().slice(0, 5).reverse().join('');
