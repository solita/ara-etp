import * as Maybe from '@Utility/maybe-utils';

export const emptyKetju = _ => ({
  'kayttajat': [],
  'kayttajarooli-id': Maybe.None(),
  'kayttajaryhma-id': Maybe.None(),
  'energiatodistus-id': Maybe.None(),
  subject: '',
  body: ''
});