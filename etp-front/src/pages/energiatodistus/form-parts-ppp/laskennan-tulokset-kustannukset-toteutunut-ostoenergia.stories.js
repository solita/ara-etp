import KustannuksetToteutunutOstoenergia from './laskennan-tulokset-kustannukset-toteutunut-ostoenergia.svelte';

import * as empty from '@Pages/energiatodistus/empty';
import * as schema from '@Pages/energiatodistus/schema';
import * as Maybe from '@Utility/maybe-utils';
import * as Either from '@Utility/either-utils';
import * as R from 'ramda';

export default {
  title:
    'Pages/Energiatodistus/PPP Laskennan Tulokset Kustannukset Toteutunut Ostoenergia',
  component: KustannuksetToteutunutOstoenergia
};

const Template = args => ({
  Component: KustannuksetToteutunutOstoenergia,
  props: args
});

// Mock energiatodistus with toteutunut-ostoenergiankulutus data
const mockEnergiatodistus = R.compose(
  R.assocPath(
    [
      'toteutunut-ostoenergiankulutus',
      'ostettu-energia',
      'kaukolampo-vuosikulutus'
    ],
    Either.Right(Maybe.Some(18000))
  ),
  R.assocPath(
    [
      'toteutunut-ostoenergiankulutus',
      'ostettu-energia',
      'kokonaissahko-vuosikulutus'
    ],
    Either.Right(Maybe.Some(12000))
  ),
  R.assocPath(
    [
      'toteutunut-ostoenergiankulutus',
      'ostettu-energia',
      'kaukojaahdytys-vuosikulutus'
    ],
    Either.Right(Maybe.Some(400))
  ),
  R.assocPath(
    [
      'toteutunut-ostoenergiankulutus',
      'ostetut-polttoaineet',
      'kevyt-polttooljy'
    ],
    Either.Right(Maybe.Some(800))
  ),
  R.assocPath(
    [
      'toteutunut-ostoenergiankulutus',
      'ostetut-polttoaineet',
      'pilkkeet-havu-sekapuu'
    ],
    Either.Right(Maybe.Some(5))
  ),
  R.assocPath(
    [
      'toteutunut-ostoenergiankulutus',
      'ostetut-polttoaineet',
      'pilkkeet-koivu'
    ],
    Either.Right(Maybe.Some(3))
  ),
  R.assocPath(
    ['toteutunut-ostoenergiankulutus', 'ostetut-polttoaineet', 'puupelletit'],
    Either.Right(Maybe.Some(500))
  )
)(empty.energiatodistus2018(1));

const mockPerusparannuspassi = R.compose(
  R.assocPath(['tulokset', 'kaukolampo-hinta'], Either.Right(Maybe.Some(8.5))),
  R.assocPath(['tulokset', 'sahko-hinta'], Either.Right(Maybe.Some(15.2))),
  R.assocPath(
    ['tulokset', 'uusiutuvat-pat-hinta'],
    Either.Right(Maybe.Some(6.0))
  ),
  R.assocPath(
    ['tulokset', 'fossiiliset-pat-hinta'],
    Either.Right(Maybe.Some(10.0))
  ),
  R.assocPath(
    ['tulokset', 'kaukojaahdytys-hinta'],
    Either.Right(Maybe.Some(5.5))
  )
)(empty.perusparannuspassi(1));

export const Default = Template.bind({});
Default.args = {
  perusparannuspassi: mockPerusparannuspassi,
  energiatodistus: mockEnergiatodistus
};
