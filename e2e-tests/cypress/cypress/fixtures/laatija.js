export const FIXTURES = {
  headers: {
    'x-amzn-oidc-accesstoken':
      'eyJraWQiOiJ0ZXN0LWtpZCIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJsYWF0aWphQHNvbGl0YS5maSIsInRva2VuX3VzZSI6ImFjY2VzcyIsInNjb3BlIjoib3BlbmlkIiwiYXV0aF90aW1lIjoxNTgzMjMwOTY5LCJpc3MiOiJodHRwczovL3Jhdy5naXRodWJ1c2VyY29udGVudC5jb20vc29saXRhL2V0cC1jb3JlL2ZlYXR1cmUvQUUtNDMtYXV0aC1oZWFkZXJzLWhhbmRsaW5nL2V0cC1iYWNrZW5kL3NyYy9tYWluL3Jlc291cmNlcyIsImV4cCI6MTg5MzQ1NjAwMCwiaWF0IjoxNjAxNTEwNDAwLCJ2ZXJzaW9uIjoyLCJqdGkiOiI1ZmRkN2EyOS03ZWVhLTRmM2QtYTdhNi1jMjI4NDI2ZjYxMmIiLCJjbGllbnRfaWQiOiJ0ZXN0LWNsaWVudF9pZCIsInVzZXJuYW1lIjoidGVzdC11c2VybmFtZSJ9.HAlEjQejKyvOoxHrORdnnTnfwiD5lUuEBMalTFKQtu_6luxqxJYfyn-etf2AkaoKWkqsT9g_-k3BV1hT-R1Y0gK3Xl21yT1MDk8QmEZlp1ztiOx4o5ufrX0t6C_Y-VKBxqQkRWLw8crSKfH2TpsTETDetA2gCReoHfHBt2_O63xL-y35glJIzHlc3egqlNFfXwduBZy8ON08h-hhZp0b8AtlaYVoY_OZY3jjfmA19jzf19rEUmK6qOJhJPr-Sgob_CDFf8G6KO4-lIF4FqdUYTvmiJoLiFmefRFscqVQTTurbsxIKmmz5JFY10vCvqp4uWcvO60O0-zgZEQcbV1Ltw',
    'x-amzn-oidc-identity': 'laatija@solita.fi',
    'x-amzn-oidc-data':
      'eyJ0eXAiOiJKV1QiLCJraWQiOiJ0ZXN0LWtpZCIsImFsZyI6IlJTMjU2IiwiaXNzIjoidGVzdC1pc3MiLCJjbGllbnQiOiJ0ZXN0LWNsaWVudCIsInNpZ25lciI6InRlc3Qtc2lnbmVyIiwiZXhwIjoxODkzNDU2MDAwfQ.eyJzdWIiOiJsYWF0aWphQHNvbGl0YS5maSIsImN1c3RvbTpGSV9uYXRpb25hbElOIjoiMDEwNDY5LTk5OVciLCJ1c2VybmFtZSI6InRlc3QtdXNlcm5hbWUiLCJleHAiOjE4OTM0NTYwMDAsImlzcyI6InRlc3QtaXNzIn0.Uk3DCz8fVTqgE_ge0ywVYpeFXnt5x6orlE3cC1e3lgs_2tzv7WHKCtLSbMWXYrcwOgZ-eOOuF_StNovq-IyMVjKAGxu1qaAR20Q2AYYg3JnOUNj1YPBpyA1nF5FYeNDolhlQKxrCj07hXmSBxBeIqNgOnepRJ0Rx9QEBoGbLvzT9mBf_m7CZncTcg2PCdtXiNeww5fx0R2ip53BcdI5nYcKz_LOae6Y707vfbmgfV_zDTFATDAqquwNuhtsqXbmc6D9smkJOl7CNPXY4riDuqyCbi62JMme90HlcHBRnMDLJXEIkTCaox3vdztxBlYVQYUwsaV3eOdQ7_v3wOal18w'
  },

  yritys: {
    1: {
      id: 1,
      ytunnus: '1111112-8',
      nimi: 'nimi',
      'vastaanottajan-tarkenne': null,
      jakeluosoite: 'jakeluosoite',
      postinumero: '12345',
      postitoimipaikka: 'postitoimipaikka',
      maa: 'FI',
      laskutuskieli: 0,
      verkkolaskuosoite: null,
      verkkolaskuoperaattori: null
    },
    2: {
      id: 2,
      ytunnus: '1111113-6',
      nimi: 'yritys2',
      'vastaanottajan-tarkenne': null,
      jakeluosoite: 'jakeluosoite2',
      postinumero: '54321',
      postitoimipaikka: 'postitoimipaikka2',
      maa: 'FI',
      laskutuskieli: 0,
      verkkolaskuosoite: null,
      verkkolaskuoperaattori: null
    }
  },
  get yritykset() {
    return [this.yritys[1], this.yritys[2]];
  },

  laatija: {
    yritykset: [
      {
        id: 1,
        'modifiedby-name': 'Specimen-Potex, Liisa',
        modifytime: '2021-03-11T10:32:36.778400Z',
        'tila-id': 1
      }
    ]
  },

  viesti: {
    id: 5000,
    vastaanottajat: [],
    'vastaanottajaryhma-id': 0,
    'energiatodistus-id': null,
    subject: 'Otsikko',
    viestit: [
      {
        from: {
          id: 2,
          'rooli-id': 0,
          sukunimi: 'Specimen-Potex',
          etunimi: 'Liisa'
        },
        'sent-time': '2021-03-18T08:25:16.735110Z',
        body: 'Viesti'
      }
    ]
  },
  get respondedViesti() {
    return {
      ...this.viesti,
      ...{
        viestit: [
          ...this.viesti.viestit,
          {
            from: {
              id: 2,
              'rooli-id': 0,
              sukunimi: 'Specimen-Potex',
              etunimi: 'Liisa'
            },
            'sent-time': '2021-03-18T10:01:35.810864Z',
            body: 'Vastaus'
          }
        ]
      }
    };
  },
  energiatodistukset: [
    {
      'laatija-fullname': 'Specimen-Potex, Liisa',
      'laatija-id': 2,
      'voimassaolo-paattymisaika': null,
      tulokset: {
        'e-luku': 190,
        'e-luokka': 'D',
        'kaytettavat-energiamuodot': {
          'fossiilinen-polttoaine': 0,
          sahko: 20000,
          kaukojaahdytys: 0,
          kaukolampo: 0,
          'uusiutuva-polttoaine': 5000
        }
      },
      'tila-id': 0,
      perustiedot: {
        havainnointikaynti: '2020-04-15',
        rakennustunnus: '101089527F',
        'katuosoite-sv': 'katuosoite-sv',
        'keskeiset-suositukset-fi':
          'Seuraavia toimenpiteitä voisi tehdä:\n\n * Toimenpide 1\n                                                                                                                       * Toimenpide 2',
        laatimisvaihe: 0,
        kieli: 0,
        nimi: 'Rakennuksen nimi',
        postinumero: '33100',
        yritys: {
          nimi: 'Yritys Oy',
          katuosoite: 'yrityksen katuosoite',
          postinumero: '33100',
          postitoimipaikka: 'tampere'
        },
        kayttotarkoitus: 'YAT',
        'katuosoite-fi': 'Katuosoite',
        'keskeiset-suositukset-sv': null,
        valmistumisvuosi: 2020
      },
      lahtotiedot: {
        'lammitetty-nettoala': 140,
        ilmanvaihto: {
          'tyyppi-id': 1,
          'kuvaus-fi': 'ilmanvaihdon kuvaus fi',
          'kuvaus-sv': null
        },
        lammitys: {
          'lammitysmuoto-1': { id: 0, 'kuvaus-fi': null, 'kuvaus-sv': null },
          'lammitysmuoto-2': {
            id: 9,
            'kuvaus-fi': 'Toissijaisen lämmitysjärjestelmän kuvaus',
            'kuvaus-sv': null
          },
          lammonjako: { id: 1, 'kuvaus-fi': null, 'kuvaus-sv': null }
        }
      },
      'korvaava-energiatodistus-id': null,
      id: 5,
      versio: 2018,
      allekirjoitusaika: null
    },
    {
      'laatija-fullname': 'Specimen-Potex, Liisa',
      'laatija-id': 2,
      'voimassaolo-paattymisaika': null,
      tulokset: {
        'e-luku': 101,
        'e-luokka': 'C',
        'kaytettavat-energiamuodot': {
          'fossiilinen-polttoaine': 0,
          sahko: 5000,
          kaukojaahdytys: 0,
          kaukolampo: 3000000,
          'uusiutuva-polttoaine': 7000
        }
      },
      'tila-id': 0,
      perustiedot: {
        havainnointikaynti: '2020-04-15',
        rakennustunnus: '101089527F',
        'katuosoite-sv': 'katuosoite-sv',
        'keskeiset-suositukset-fi':
          'Seuraavia toimenpiteitä voisi tehdä:\n\n * Toimenpide 1\n                                                                                                                       * Toimenpide 2',
        laatimisvaihe: 0,
        kieli: 0,
        nimi: 'Rakennuksen nimi',
        postinumero: '33100',
        yritys: {
          nimi: 'Yritys Oy',
          katuosoite: 'yrityksen katuosoite',
          postinumero: '33100',
          postitoimipaikka: 'tampere'
        },
        kayttotarkoitus: 'AK3',
        'katuosoite-fi': 'Katuosoite',
        'keskeiset-suositukset-sv': null,
        valmistumisvuosi: 2017
      },
      lahtotiedot: {
        'lammitetty-nettoala': 15000,
        ilmanvaihto: {
          'tyyppi-id': 1,
          'kuvaus-fi': 'ilmanvaihdon kuvaus fi',
          'kuvaus-sv': null
        },
        lammitys: {
          'lammitysmuoto-1': { id: 0, 'kuvaus-fi': null, 'kuvaus-sv': null },
          'lammitysmuoto-2': {
            id: 9,
            'kuvaus-fi': 'Toissijaisen lämmitysjärjestelmän kuvaus',
            'kuvaus-sv': null
          },
          lammonjako: { id: 1, 'kuvaus-fi': null, 'kuvaus-sv': null }
        }
      },
      'korvaava-energiatodistus-id': null,
      id: 4,
      versio: 2018,
      allekirjoitusaika: null
    },
    {
      'laatija-fullname': 'Specimen-Potex, Liisa',
      'laatija-id': 2,
      'voimassaolo-paattymisaika': null,
      tulokset: {
        'e-luku': 104,
        'e-luokka': 'B',
        'kaytettavat-energiamuodot': {
          'fossiilinen-polttoaine': 0,
          sahko: 10000000,
          kaukojaahdytys: 0,
          kaukolampo: 7000000,
          'uusiutuva-polttoaine': 7000
        }
      },
      'tila-id': 0,
      perustiedot: {
        havainnointikaynti: '2020-04-15',
        rakennustunnus: '101089527F',
        'katuosoite-sv': 'katuosoite-sv',
        'keskeiset-suositukset-fi':
          'Seuraavia toimenpiteitä voisi tehdä:\n\n * Toimenpide 1\n                                                                                                                         * Toimenpide 2',
        laatimisvaihe: 0,
        kieli: 0,
        nimi: 'Rakennuksen nimi',
        postinumero: '33100',
        yritys: {
          nimi: 'Yritys Oy',
          katuosoite: 'yrityksen katuosoite',
          postinumero: '33100',
          postitoimipaikka: 'tampere'
        },
        kayttotarkoitus: 'TT',
        'katuosoite-fi': 'Katuosoite',
        'keskeiset-suositukset-sv': null,
        valmistumisvuosi: 2018
      },
      lahtotiedot: {
        'lammitetty-nettoala': 150000,
        ilmanvaihto: {
          'tyyppi-id': 1,
          'kuvaus-fi': 'ilmanvaihdon kuvaus fi',
          'kuvaus-sv': null
        },
        lammitys: {
          'lammitysmuoto-1': { id: 0, 'kuvaus-fi': null, 'kuvaus-sv': null },
          'lammitysmuoto-2': {
            id: 9,
            'kuvaus-fi': 'Toissijaisen lämmitysjärjestelmän kuvaus',
            'kuvaus-sv': null
          },
          lammonjako: { id: 1, 'kuvaus-fi': null, 'kuvaus-sv': null }
        }
      },
      'korvaava-energiatodistus-id': null,
      id: 3,
      versio: 2018,
      allekirjoitusaika: null
    },
    {
      'laatija-fullname': 'Specimen-Potex, Liisa',
      'laatija-id': 2,
      'voimassaolo-paattymisaika': null,
      tulokset: {
        'e-luku': 50,
        'e-luokka': 'A',
        'kaytettavat-energiamuodot': {
          'fossiilinen-polttoaine': 0,
          sahko: 0,
          kaukojaahdytys: 0,
          kaukolampo: 0,
          'uusiutuva-polttoaine': 50000
        }
      },
      'tila-id': 0,
      perustiedot: {
        havainnointikaynti: '2020-04-15',
        rakennustunnus: '101089527F',
        'katuosoite-sv': 'katuosoite-sv',
        'keskeiset-suositukset-fi':
          'Seuraavia toimenpiteitä voisi tehdä:\n\n * Toimenpide 1\n                                                                                                                         * Toimenpide 2',
        laatimisvaihe: 0,
        kieli: 0,
        nimi: 'Rakennuksen nimi',
        postinumero: '33100',
        yritys: {
          nimi: 'Yritys Oy',
          katuosoite: 'yrityksen katuosoite',
          postinumero: '33100',
          postitoimipaikka: 'tampere'
        },
        kayttotarkoitus: 'PK',
        'katuosoite-fi': 'Katuosoite',
        'keskeiset-suositukset-sv': null,
        valmistumisvuosi: 2019
      },
      lahtotiedot: {
        'lammitetty-nettoala': 500,
        ilmanvaihto: {
          'tyyppi-id': 1,
          'kuvaus-fi': 'ilmanvaihdon kuvaus fi',
          'kuvaus-sv': null
        },
        lammitys: {
          'lammitysmuoto-1': { id: 0, 'kuvaus-fi': null, 'kuvaus-sv': null },
          'lammitysmuoto-2': {
            id: 9,
            'kuvaus-fi': 'Toissijaisen lämmitysjärjestelmän kuvaus',
            'kuvaus-sv': null
          },
          lammonjako: { id: 1, 'kuvaus-fi': null, 'kuvaus-sv': null }
        }
      },
      'korvaava-energiatodistus-id': null,
      id: 2,
      versio: 2018,
      allekirjoitusaika: null
    },
    {
      'laatija-fullname': 'Specimen-Potex, Liisa',
      'laatija-id': 2,
      'voimassaolo-paattymisaika': null,
      tulokset: {
        'e-luku': 322,
        'e-luokka': 'E',
        'kaytettavat-energiamuodot': {
          'fossiilinen-polttoaine': 550000,
          sahko: 5000,
          kaukojaahdytys: 0,
          kaukolampo: 0,
          'uusiutuva-polttoaine': 0
        }
      },
      'tila-id': 0,
      perustiedot: {
        havainnointikaynti: '2020-04-15',
        rakennustunnus: '101089527F',
        'katuosoite-sv': 'Katuosoite SV',
        'keskeiset-suositukset-fi':
          'Seuraavia toimenpiteitä voisi tehdä:\n\n * Toimenpide 1\n                                                                                                                      * Toimenpide 2',
        kieli: 2,
        nimi: 'Rakennuksen nimi',
        postinumero: '33100',
        yritys: {
          nimi: 'Yritys Oy',
          katuosoite: 'yrityksen katuosoite',
          postinumero: '33100',
          postitoimipaikka: 'tampere'
        },
        kayttotarkoitus: 'RK',
        'katuosoite-fi': 'Katuosoite',
        'keskeiset-suositukset-sv': 'Keskeiset suositukset SV',
        valmistumisvuosi: 2016
      },
      lahtotiedot: {
        'lammitetty-nettoala': 5000,
        ilmanvaihto: {
          'tyyppi-id': 0,
          'kuvaus-fi': 'ilmanvaihdon kuvaus fi',
          'kuvaus-sv': 'ilmanvaihdon kuvaus sv'
        },
        lammitys: {
          'lammitysmuoto-1': { id: 2, 'kuvaus-fi': null, 'kuvaus-sv': null },
          'lammitysmuoto-2': { id: 5, 'kuvaus-fi': null, 'kuvaus-sv': null },
          lammonjako: { id: 7, 'kuvaus-fi': null, 'kuvaus-sv': null }
        }
      },
      'korvaava-energiatodistus-id': null,
      id: 1,
      versio: 2013,
      allekirjoitusaika: null
    }
  ]
};
