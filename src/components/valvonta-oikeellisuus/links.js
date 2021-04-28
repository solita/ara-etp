
const root = '#/valvonta/oikeellisuus';

export const valvonta = energiatodistus => `${root}/${energiatodistus.versio}/${energiatodistus.id}`;

export const toimenpide = (toimenpide, energiatodistus) => `${root}/${energiatodistus.versio}/${energiatodistus.id}/${toimenpide.id}`;

export const newToimenpide = (typeId, energiatodistus) => `${root}/${energiatodistus.versio}/${energiatodistus.id}/new/${typeId}`;