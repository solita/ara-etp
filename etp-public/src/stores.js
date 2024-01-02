import { readable, derived } from 'svelte/store';
import * as LaatijaApi from '@/api/laatija-api';
import * as GeoApi from '@/api/geo-api';
import { locale } from '@Localization/localization';
import * as LaatijaUtils from '@/utilities/laatija';

export const rawLaatijat = readable(null, set =>
  set(LaatijaApi.laatijat(fetch))
);

export const patevyydet = readable(null, set =>
  set(LaatijaApi.patevyydet(fetch))
);

export const postinumerot = readable(null, set =>
  set(GeoApi.postinumerot(fetch))
);

export const toimintaalueet = readable(null, set =>
  set(GeoApi.toimintaalueet(fetch))
);

export const kunnat = readable(null, set => set(GeoApi.kunnat(fetch)));

export const laatijat = derived(
  [rawLaatijat, patevyydet, toimintaalueet, locale],
  ([$laatijat, $patevyydet, $toimintaalueet, $locale]) =>
    Promise.all([$laatijat, $patevyydet, $toimintaalueet]).then(
      ([$laatijat, ...args]) =>
        $laatijat.map(LaatijaUtils.deserialize($locale, ...args))
    )
);
