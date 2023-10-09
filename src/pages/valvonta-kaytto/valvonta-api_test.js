import { assert } from 'chai';
import { serializeOsapuoliSpecificData } from '@Pages/valvonta-kaytto/valvonta-api';
import { Maybe } from '@Utility/maybe-utils';
import * as R from 'ramda';
import * as Toimenpiteet from '@Pages/valvonta-kaytto/toimenpiteet';

describe('Valvonta API test', () => {
  describe('Serialize osapuoli specific data', () => {
    it('Empty data is returned as is', () => {
      const input = [];
      const expected = [];
      const result = serializeOsapuoliSpecificData(input);
      assert.deepEqual(result, expected);
    });

    it('Empty object is returned as is', () => {
      const input = [{}];
      const expected = [{}];
      const result = serializeOsapuoliSpecificData(input);
      assert.deepEqual(result, expected);
    });

    describe('When modified fields do not exist, the original object is returned', () => {
      [true, false].forEach(document => {
        it(`Document is ${document}`, () => {
          const input = [{ document: document }];
          const expected = [{ document: document }];
          const result = serializeOsapuoliSpecificData(input);
          assert.deepEqual(result, expected);
        });
      });
    });

    it('When document is set to false, hallinto-oikeus-id is set to null', () => {
      const input = [
        {
          document: false,
          'hallinto-oikeus-id': Maybe.of('dropped')
        }
      ];
      const expected = [
        {
          document: false,
          'hallinto-oikeus-id': null
        }
      ];
      const result = serializeOsapuoliSpecificData(input);
      assert.deepEqual(result, expected);
    });

    it('When document is set to true, hallinto-oikeus-id is unwrapped from Maybe', () => {
      const data = [
        {
          document: true,
          'hallinto-oikeus-id': Maybe.of('unwrapped')
        }
      ];
      const expected = [
        {
          document: true,
          'hallinto-oikeus-id': 'unwrapped'
        }
      ];
      const result = serializeOsapuoliSpecificData(data);
      assert.deepEqual(result, expected);
    });

    it('When document is set to false, karajaoikeus-id is set to null', () => {
      const input = [
        {
          document: false,
          'karajaoikeus-id': Maybe.of('dropped')
        }
      ];
      const expected = [
        {
          document: false,
          'karajaoikeus-id': null
        }
      ];
      const result = serializeOsapuoliSpecificData(input);
      assert.deepEqual(result, expected);
    });

    it('When document is set to true, karajaoikeus-id is unwrapped from Maybe', () => {
      const data = [
        {
          document: true,
          'karajaoikeus-id': Maybe.of('unwrapped')
        }
      ];
      const expected = [
        {
          document: true,
          'karajaoikeus-id': 'unwrapped'
        }
      ];
      const result = serializeOsapuoliSpecificData(data);
      assert.deepEqual(result, expected);
    });

    it('for käskypäätös / varsinainen päätös', () => {
      const toimenpide = Toimenpiteet.emptyToimenpide(8, [], {
        osapuoliIds: [1]
      });

      assert.deepEqual(
        serializeOsapuoliSpecificData(
          R.path(['type-specific-data', 'osapuoli-specific-data'], toimenpide)
        ),
        [
          {
            'osapuoli-id': 1,
            'hallinto-oikeus-id': null,
            'recipient-answered': false,
            'answer-commentary-fi': null,
            'answer-commentary-sv': null,
            'statement-fi': null,
            'statement-sv': null,
            document: true
          }
        ]
      );
    });

    it('for käskypäätös / varsinainen päätös when recipient-answered is false but fields depending on it have data in them', () => {
      const osapuoliSpecificData = [
        {
          'osapuoli-id': 1,
          'hallinto-oikeus-id': Maybe.None(),
          'recipient-answered': false,
          'answer-commentary-fi': Maybe.Some('answer-commentary-fi'),
          'answer-commentary-sv': Maybe.Some('answer-commentary-sv'),
          'statement-fi': Maybe.Some('statement-fi'),
          'statement-sv': Maybe.Some('statement-sv'),
          document: true
        }
      ];

      // answer-commentary-fi, answer-commentary-sv, statement-fi and statement-sv have been set to null
      assert.deepEqual(serializeOsapuoliSpecificData(osapuoliSpecificData), [
        {
          'osapuoli-id': 1,
          'hallinto-oikeus-id': null,
          'recipient-answered': false,
          'answer-commentary-fi': null,
          'answer-commentary-sv': null,
          'statement-fi': null,
          'statement-sv': null,
          document: true
        }
      ]);
    });

    it('for käskypäätös / varsinainen päätös when recipient-answered is true but fields depending on it have data in them', () => {
      const osapuoliSpecificData = [
        {
          'osapuoli-id': 1,
          'hallinto-oikeus-id': Maybe.None(),
          'recipient-answered': true,
          'answer-commentary-fi': Maybe.Some('answer-commentary-fi'),
          'answer-commentary-sv': Maybe.Some('answer-commentary-sv'),
          'statement-fi': Maybe.Some('statement-fi'),
          'statement-sv': Maybe.Some('statement-sv'),
          document: true
        }
      ];

      // answer-commentary-fi, answer-commentary-sv, statement-fi and statement-sv have been set to null
      assert.deepEqual(serializeOsapuoliSpecificData(osapuoliSpecificData), [
        {
          'osapuoli-id': 1,
          'hallinto-oikeus-id': null,
          'recipient-answered': true,
          'answer-commentary-fi': 'answer-commentary-fi',
          'answer-commentary-sv': 'answer-commentary-sv',
          'statement-fi': 'statement-fi',
          'statement-sv': 'statement-sv',
          document: true
        }
      ]);
    });
  });
});
