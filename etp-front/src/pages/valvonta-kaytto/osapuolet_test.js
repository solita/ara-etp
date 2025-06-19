import { expect, describe, it } from '@jest/globals';
import {
  toimitustapa,
  toimitustapaErrorKey
} from '@Pages/valvonta-kaytto/osapuolet';
import { Maybe } from '@Utility/maybe-utils';

describe('Toimitustapa', () => {
  const suomifi = {
    'toimitustapa-id': Maybe.Some(0)
  };
  const email = {
    'toimitustapa-id': Maybe.Some(1)
  };
  const other = {
    'toimitustapa-id': Maybe.Some(2)
  };
  it('Check suomifi', () => {
    expect(toimitustapa.suomifi(suomifi)).toBe(true);
    expect(toimitustapa.suomifi(email)).toBe(false);
    expect(toimitustapa.suomifi(other)).toBe(false);
  });
  it('Check email', () => {
    expect(toimitustapa.email(suomifi)).toBe(false);
    expect(toimitustapa.email(email)).toBe(true);
    expect(toimitustapa.email(other)).toBe(false);
  });
  it('Check other', () => {
    expect(toimitustapa.other(suomifi)).toBe(false);
    expect(toimitustapa.other(email)).toBe(false);
    expect(toimitustapa.other(other)).toBe(true);
  });
});

describe('ToimitustapaErrorKey', () => {
  describe('Valid values return None', () => {
    it('always for muu', () => {
      expect(
        toimitustapaErrorKey
          .yritys({ 'toimitustapa-id': Maybe.Some(2) })
          .isNone()
      ).toBe(true);
      expect(
        toimitustapaErrorKey
          .henkilo({ 'toimitustapa-id': Maybe.Some(2) })
          .isNone()
      ).toBe(true);
    });
    describe('for yritys', () => {
      it('when using suomifi', () => {
        const suomifi = {
          'toimitustapa-id': Maybe.Some(0),
          ytunnus: Maybe.Some('1234567-8'),
          jakeluosoite: Maybe.Some('Katu 1'),
          maa: Maybe.Some('Suomi')
        };
        expect(toimitustapaErrorKey.yritys(suomifi).isNone()).toBe(true);
      });
      it('when using email', () => {
        const email = {
          'toimitustapa-id': Maybe.Some(1),
          email: Maybe.Some('example@example.com')
        };
        expect(toimitustapaErrorKey.yritys(email).isNone()).toBe(true);
      });
    });
    describe('for henkilo', () => {
      it('when using suomifi', () => {
        const suomifi = {
          'toimitustapa-id': Maybe.Some(0),
          'rooli-id': Maybe.Some(0),
          henkilotunnus: Maybe.Some('123456-7890'),
          jakeluosoite: Maybe.Some('Katu 1'),
          maa: Maybe.Some('Suomi')
        };
        expect(toimitustapaErrorKey.henkilo(suomifi).isNone()).toBe(true);
      });
      it('when using email', () => {
        const email = {
          'toimitustapa-id': Maybe.Some(1),
          email: Maybe.Some('example@example.com')
        };
        expect(toimitustapaErrorKey.henkilo(email).isNone()).toBe(true);
      });
    });
  });
  describe('Invalid values return Some', () => {
    describe('for yritys', () => {
      it('when using suomifi', () => {
        const missingYTunnus = {
          'toimitustapa-id': Maybe.Some(0),
          ytunnus: Maybe.None(),
          jakeluosoite: Maybe.Some('Katu 1'),
          maa: Maybe.Some('Suomi')
        };
        const missingJakeluosoite = {
          'toimitustapa-id': Maybe.Some(0),
          ytunnus: Maybe.Some('1234567-8'),
          jakeluosoite: Maybe.None(),
          maa: Maybe.Some('Suomi')
        };
        const missingMaa = {
          'toimitustapa-id': Maybe.Some(0),
          ytunnus: Maybe.Some('1234567-8'),
          jakeluosoite: Maybe.Some('Katu 1'),
          maa: Maybe.None()
        };
        [missingYTunnus, missingJakeluosoite, missingMaa].forEach(invalid => {
          expect(toimitustapaErrorKey.yritys(invalid).some()).toEqual(
            'suomifi-yritys'
          );
        });
      });
      it('when using email', () => {
        const missingEmail = {
          'toimitustapa-id': Maybe.Some(1),
          email: Maybe.None()
        };
        expect(toimitustapaErrorKey.yritys(missingEmail).some()).toEqual(
          'email'
        );
      });
    });
    describe('for henkilo', () => {
      it('when using suomifi', () => {
        const missingHenkilotunnus = {
          'toimitustapa-id': Maybe.Some(0),
          'rooli-id': Maybe.Some(0),
          henkilotunnus: Maybe.None(),
          jakeluosoite: Maybe.Some('Katu 1'),
          maa: Maybe.Some('Suomi')
        };
        const missingJakeluosoite = {
          'toimitustapa-id': Maybe.Some(0),
          'rooli-id': Maybe.Some(0),
          henkilotunnus: Maybe.Some('123456-7890'),
          jakeluosoite: Maybe.None(),
          maa: Maybe.Some('Suomi')
        };
        const missingMaa = {
          'toimitustapa-id': Maybe.Some(0),
          'rooli-id': Maybe.Some(0),
          henkilotunnus: Maybe.Some('123456-7890'),
          jakeluosoite: Maybe.Some('Katu 1'),
          maa: Maybe.None()
        };
        [missingHenkilotunnus, missingJakeluosoite, missingMaa].forEach(
          invalid => {
            expect(toimitustapaErrorKey.henkilo(invalid).some()).toEqual(
              'suomifi-henkilo'
            );
          }
        );
        const notOmistaja = {
          'toimitustapa-id': Maybe.Some(0),
          'rooli-id': Maybe.Some(1), // Not omistaja
          henkilotunnus: Maybe.Some('123456-7890'),
          jakeluosoite: Maybe.Some('Katu 1'),
          maa: Maybe.Some('Suomi')
        };
        expect(toimitustapaErrorKey.henkilo(notOmistaja).some()).toEqual(
          'suomifi-henkilo-omistaja-required'
        );
      });
      it('when using email', () => {
        const missingEmail = {
          'toimitustapa-id': Maybe.Some(1),
          email: Maybe.None()
        };
        expect(toimitustapaErrorKey.henkilo(missingEmail).some()).toEqual(
          'email'
        );
      });
    });
  });
});
