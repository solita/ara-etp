import * as R from 'ramda';
import * as Kayttaja from '@Utility/kayttajat';
import * as EtUtils from '@Component/Energiatodistus/energiatodistus-utils';

export const modules = [
  'tyojono',
  'save',
  'sign',
  'copy',
  'preview',
  'download',
  'discard',
  'undodiscard',
  'delete'
];

export const module = R.compose(R.map(parseInt), R.invertObj)(modules);

export const paakayttajaFields = tila => {
  switch (tila) {
    case EtUtils.tila['draft']:
    case EtUtils.tila['in-signing']:
      return [module.save, module.preview];
    case EtUtils.tila['signed']:
      return [module.tyojono, module.save, module.download, module.discard];
    case EtUtils.tila['discarded']:
      return [module.download, module.undodiscard];
    case EtUtils.tila['replaced']:
    case EtUtils.tila['deleted']:
      return [module.download];
    default:
      return [];
  }
};

export const laatijaFields = tila => {
  switch (tila) {
    case EtUtils.tila['draft']:
    case EtUtils.tila['in-signing']:
      return [module.save, module.sign, module.preview, module.delete];
    case EtUtils.tila['signed']:
      return [module.save, module.download];
    case EtUtils.tila['discarded']:
      return [module.preview, module.download, module.undodiscard];
    case EtUtils.tila['replaced']:
    case EtUtils.tila['deleted']:
      return [module.download];
    default:
      return [];
  }
};

export const toolbarFields = R.curry((user, tila) => {
  if (Kayttaja.isPaakayttaja(user)) {
    return paakayttajaFields(tila);
  }

  if (Kayttaja.isLaatija(user)) {
    return laatijaFields(tila);
  }

  return [];
});
