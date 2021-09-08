const root = '#/valvonta/kaytto';

export const valvonta = valvonta => `${root}/${valvonta.id}/valvonta`;

export const kohde = valvonta => `${root}/${valvonta.id}/kohde`;

export const henkilo = (valvonta, henkilo) =>
  `${root}/${valvonta.id}/henkilo/${henkilo.id}`;

export const yritys = (valvonta, yritys) =>
  `${root}/${valvonta.id}/yritys/${yritys.id}`;

export const newHenkilo = (valvonta) =>
  `${root}/${valvonta.id}/henkilo/new`;

export const newYritys = (valvonta) =>
  `${root}/${valvonta.id}/yritys/new`;
