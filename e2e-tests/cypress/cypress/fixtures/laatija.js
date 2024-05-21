export const FIXTURES = {
  headers: {
    'x-amzn-oidc-accesstoken':
      'eyJraWQiOiJ0ZXN0LWtpZCIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJsYWF0aWphQHNvbGl0YS5maSIsInRva2VuX3VzZSI6ImFjY2VzcyIsInNjb3BlIjoib3BlbmlkIiwiYXV0aF90aW1lIjoxNTgzMjMwOTY5LCJpc3MiOiJodHRwczovL3Jhdy5naXRodWJ1c2VyY29udGVudC5jb20vc29saXRhL2V0cC1jb3JlL2ZlYXR1cmUvQUUtNDMtYXV0aC1oZWFkZXJzLWhhbmRsaW5nL2V0cC1iYWNrZW5kL3NyYy9tYWluL3Jlc291cmNlcyIsImV4cCI6MTg5MzQ1NjAwMCwiaWF0IjoxNjAxNTEwNDAwLCJ2ZXJzaW9uIjoyLCJqdGkiOiI1ZmRkN2EyOS03ZWVhLTRmM2QtYTdhNi1jMjI4NDI2ZjYxMmIiLCJjbGllbnRfaWQiOiJ0ZXN0LWNsaWVudF9pZCIsInVzZXJuYW1lIjoidGVzdC11c2VybmFtZSJ9.HAlEjQejKyvOoxHrORdnnTnfwiD5lUuEBMalTFKQtu_6luxqxJYfyn-etf2AkaoKWkqsT9g_-k3BV1hT-R1Y0gK3Xl21yT1MDk8QmEZlp1ztiOx4o5ufrX0t6C_Y-VKBxqQkRWLw8crSKfH2TpsTETDetA2gCReoHfHBt2_O63xL-y35glJIzHlc3egqlNFfXwduBZy8ON08h-hhZp0b8AtlaYVoY_OZY3jjfmA19jzf19rEUmK6qOJhJPr-Sgob_CDFf8G6KO4-lIF4FqdUYTvmiJoLiFmefRFscqVQTTurbsxIKmmz5JFY10vCvqp4uWcvO60O0-zgZEQcbV1Ltw',
    'x-amzn-oidc-identity': 'laatija@solita.fi',
    'x-amzn-oidc-data':
      'eyJ0eXAiOiJKV1QiLCJraWQiOiJ0ZXN0LWtpZCIsImFsZyI6IlJTMjU2IiwiaXNzIjoidGVzdC1pc3MiLCJjbGllbnQiOiJ0ZXN0LWNsaWVudCIsInNpZ25lciI6InRlc3Qtc2lnbmVyIiwiZXhwIjoxODkzNDU2MDAwfQ.eyJzdWIiOiJsYWF0aWphQHNvbGl0YS5maSIsImN1c3RvbTpGSV9uYXRpb25hbElOIjoiMDEwNDY5LTk5OVciLCJ1c2VybmFtZSI6InRlc3QtdXNlcm5hbWUiLCJleHAiOjE4OTM0NTYwMDAsImlzcyI6InRlc3QtaXNzIn0.Uk3DCz8fVTqgE_ge0ywVYpeFXnt5x6orlE3cC1e3lgs_2tzv7WHKCtLSbMWXYrcwOgZ-eOOuF_StNovq-IyMVjKAGxu1qaAR20Q2AYYg3JnOUNj1YPBpyA1nF5FYeNDolhlQKxrCj07hXmSBxBeIqNgOnepRJ0Rx9QEBoGbLvzT9mBf_m7CZncTcg2PCdtXiNeww5fx0R2ip53BcdI5nYcKz_LOae6Y707vfbmgfV_zDTFATDAqquwNuhtsqXbmc6D9smkJOl7CNPXY4riDuqyCbi62JMme90HlcHBRnMDLJXEIkTCaox3vdztxBlYVQYUwsaV3eOdQ7_v3wOal18w'
  },

  mpollux: {
    version: {
      version: 'dummy',
      httpMethods: 'GET, POST',
      contentTypes: 'data, digest',
      signatureTypes: 'signature',
      selectorAvailable: true,
      hashAlgorithms: 'SHA1, SHA256, SHA384, SHA512'
    },
    digest:
      'MVwwDwYJKoZIhvcvAQEIMQIwADAYBgkqhkiG9w0BCQMxCwYJKoZIhvcNAQcBMC8GCSqGSIb3DQEJBDEiBCAnW1v6zdcqQEaGYaAbsNhmMdEJA4SnFXRvj6wSytNDpg==',
    sign: {
      version: '1.1',
      signatureAlgorithm: 'SHA256withRSA',
      signatureType: 'signature',
      signature:
        'IAMcrYCDqC0nprcI2aKTZGAqHurktQYjw6IBh4gDrvl5FKrKczRlE07x8iwWd66O11J/LXuWj3xdNz3UTcPzvUBurT0VH4KDy9oGxeMbMLrJoWmD3gvzUrrRox/oA8/wKuTnqo/PIkJzkZFxty3zeh5ahNQAZEqXnUP+oBi524WlPNcSXA4EnTNlTm7FfJlWIUw8Ljo1ZqaFgOw7omTEeYJgBLiYAZgTSxeNkDMTogAqQA9jXnukUMDu7s/0APsZpFEU/kbYZM3Sz5XfgGHbq4p/zUzskTKqeMDfiJg8hGkMXfViLwS4cyriW/VyCm87WCBqkOeHD7whOv4KyVA/cw==',
      chain: [
        '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'
      ],
      status: 'ok',
      reasonCode: 200,
      reasonText: 'Signature generated'
    },
    pdf: n => `energiatodistukset/energiatodistus-${n}-fi.pdf`
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
