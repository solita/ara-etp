const FIXTURES = {
  paakayttajaHeaders: {
    'x-amzn-oidc-accesstoken':
      'eyJraWQiOiJ0ZXN0LWtpZCIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJwYWFrYXl0dGFqYUBzb2xpdGEuZmkiLCJ0b2tlbl91c2UiOiJhY2Nlc3MiLCJzY29wZSI6Im9wZW5pZCIsImF1dGhfdGltZSI6MTU4MzIzMDk2OSwiaXNzIjoiaHR0cHM6Ly9yYXcuZ2l0aHVidXNlcmNvbnRlbnQuY29tL3NvbGl0YS9ldHAtY29yZS9mZWF0dXJlL0FFLTQzLWF1dGgtaGVhZGVycy1oYW5kbGluZy9ldHAtYmFja2VuZC9zcmMvbWFpbi9yZXNvdXJjZXMiLCJleHAiOjE4OTM0NTYwMDAsImlhdCI6MTU4MzQxMzQyNCwidmVyc2lvbiI6MiwianRpIjoiNWZkZDdhMjktN2VlYS00ZjNkLWE3YTYtYzIyODQyNmY2MTJiIiwiY2xpZW50X2lkIjoidGVzdC1jbGllbnRfaWQiLCJ1c2VybmFtZSI6InRlc3QtdXNlcm5hbWUifQ.PY5_jWcdxhCyn2EpFpss7Q0R3_xH1PvHi4mxDLorpppHnciGT2kFLeutebi7XeLtTYwmttTxxg2tyUyX0_UF7zj_P-tdq-kZQlud1ENmRaUxLXO5mTFKXD7zPb6BPFNe0ewRQ7Uuv3lDk_IxOf-6i86VDYB8luyesEXq7ra4S4l8akFodW_QYBSZQnUva_CVyzsTNcmgGTyrz2NI6seT1x6Pt1uFdYI97FHKlCCWVL1Z042omfujfta8j8XkTWdhKf3dfsHRWjrw31xqOkgD7uwPKcrC0U-wIj3U0uX0Rz2Tk4T-kIq4XTkKttYpkJqOmMFAYuhk6MDjfRkPWBZhUA',
    'x-amzn-oidc-identity': 'paakayttaja@solita.fi',
    'x-amzn-oidc-data':
      'eyJ0eXAiOiJKV1QiLCJraWQiOiJ0ZXN0LWtpZCIsImFsZyI6IlJTMjU2IiwiaXNzIjoidGVzdC1pc3MiLCJjbGllbnQiOiJ0ZXN0LWNsaWVudCIsInNpZ25lciI6InRlc3Qtc2lnbmVyIiwiZXhwIjoxODkzNDU2MDAwfQ.eyJzdWIiOiJwYWFrYXl0dGFqYUBzb2xpdGEuZmkiLCJjdXN0b206VklSVFVfbG9jYWxJRCI6InZ2aXJrYW1pZXMiLCJjdXN0b206VklSVFVfbG9jYWxPcmciOiJ0ZXN0aXZpcmFzdG8uZmkiLCJ1c2VybmFtZSI6InRlc3QtdXNlcm5hbWUiLCJleHAiOjE4OTM0NTYwMDAsImlzcyI6InRlc3QtaXNzIn0.BfuDVOFUReiJd6N05Re6affps_47AA0F5o-g6prmXgAnk4lB1S3k9RpovCFU3-R5Zn0p38QTiwi5dENHCHaj1A6MGHHKeYd7vBZK0VquuBxlIQH-4k1MWLvpYnkK3yuEvfmbRb3jYspCA_4N-AF21cCyjd15RiuIawLCEM0Km1DRgLhXIBta6XCGSRwaRmrT7boDRMp7hUkYPpoakCahMC70sjyuvLE0pjAy1_S09g4SkboentI7WhfsfN4uAHbKy6ViVMfsnwVVvKsM8dXav_a-6PoNGywuUbi8nHt8c20KiB_AzAEYSqxbRX1YBd0UHlYS16LbLtMBTOctCBLDMg'
  },
  vanhaHetuHeaders: {
    'x-amzn-oidc-accesstoken':
      'eyJraWQiOiJ0ZXN0LWtpZCIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJ2YW5oYWhldHVAZXhhbXBsZS5jb20iLCJ0b2tlbl91c2UiOiJhY2Nlc3MiLCJzY29wZSI6Im9wZW5pZCIsImF1dGhfdGltZSI6MTU4MzIzMDk2OSwiaXNzIjoiaHR0cHM6Ly9yYXcuZ2l0aHVidXNlcmNvbnRlbnQuY29tL3NvbGl0YS9ldHAtY29yZS9mZWF0dXJlL0FFLTQzLWF1dGgtaGVhZGVycy1oYW5kbGluZy9ldHAtYmFja2VuZC9zcmMvbWFpbi9yZXNvdXJjZXMiLCJleHAiOjE4OTM0NTYwMDAsImlhdCI6MTYwMTUxMDQwMCwidmVyc2lvbiI6MiwianRpIjoiNWZkZDdhMjktN2VlYS00ZjNkLWE3YTYtYzIyODQyNmY2MTJiIiwiY2xpZW50X2lkIjoidGVzdC1jbGllbnRfaWQiLCJ1c2VybmFtZSI6InRlc3QtdXNlcm5hbWUifQ.NnX76tSOF82bEn8AWMU1Cu-WxX5Not54DKpQTVI_SHq-tRQTZZ62dJJa26vxggh38O0XOCAKgGYEDvj31ZCnFxS166Mo-5hhjf1kXextlejsWOts2P95dzQasQPVcD7wpZAbE0NrWsL7htIeDCUl63p6CYx0sfm5_IDVX1cbGs1QqFHYFdCSsN3njxtmEpPOakQDITjOE-sQYxZDCGVPPj1jh5D6Rq5Y3v0hoKilruUYvozJ9aAz8v9G_Iih3hT0TtaVGXyXOEWBMDwqxpU7eGNqbBMHgNhRxJsR5AMqk8uwmJ2TOGtUOGK0pcLwLcGFDuc0r8ep9SEBKaZ5A2gRkA',
    'x-amzn-oidc-identity': 'vanhahetu@example.com',
    'x-amzn-oidc-data':
      'eyJ0eXAiOiJKV1QiLCJraWQiOiJ0ZXN0LWtpZCIsImFsZyI6IlJTMjU2IiwiaXNzIjoidGVzdC1pc3MiLCJjbGllbnQiOiJ0ZXN0LWNsaWVudCIsInNpZ25lciI6InRlc3Qtc2lnbmVyIiwiZXhwIjoxODkzNDU2MDAwfQ.eyJzdWIiOiJ2YW5oYWhldHVAZXhhbXBsZS5jb20iLCJjdXN0b206RklfbmF0aW9uYWxJTiI6IjEwMDE5MC05OTlQIiwidXNlcm5hbWUiOiJ0ZXN0LXVzZXJuYW1lIiwiZXhwIjoxODkzNDU2MDAwLCJpc3MiOiJ0ZXN0LWlzcyJ9.c8iFOMh4phlDlBppJTmTZd5pSIcB8ZTKicaVi_MPyHhSkcqfqoPtB8lPjYsXWi_0W1bDl-ZgaKJoXI9GWeAygv4uYhRU4x0XPR-0UJECrk0sEpduRXVOlwVxGvyect9PU_jhuZYfTMNu4quHXQ9KOV9I67Le4cgu6CiXEZmWHGYeKWpymn_t1xdx2bYRCfg5hWGk9Bizlr0j7qXb65hWoEvL5DWOmy1giEZ_XAZs8IyVIgswZU9huyKaY7KxgcyaecZNPSSBQvdNq15HOEynu9h-Ax6C1Ptxt3BShLbMSEeixdWOAPDBBxgb4jzbclfa2rTWbU2Z835B3_yGt1TDWA'
  },
  uusiHetuHeaders: {
    'x-amzn-oidc-accesstoken':
      'eyJraWQiOiJ0ZXN0LWtpZCIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJ1dXNpaGV0dUBleGFtcGxlLmNvbSIsInRva2VuX3VzZSI6ImFjY2VzcyIsInNjb3BlIjoib3BlbmlkIiwiYXV0aF90aW1lIjoxNTgzMjMwOTY5LCJpc3MiOiJodHRwczovL3Jhdy5naXRodWJ1c2VyY29udGVudC5jb20vc29saXRhL2V0cC1jb3JlL2ZlYXR1cmUvQUUtNDMtYXV0aC1oZWFkZXJzLWhhbmRsaW5nL2V0cC1iYWNrZW5kL3NyYy9tYWluL3Jlc291cmNlcyIsImV4cCI6MTg5MzQ1NjAwMCwiaWF0IjoxNjAxNTEwNDAwLCJ2ZXJzaW9uIjoyLCJqdGkiOiI1ZmRkN2EyOS03ZWVhLTRmM2QtYTdhNi1jMjI4NDI2ZjYxMmIiLCJjbGllbnRfaWQiOiJ0ZXN0LWNsaWVudF9pZCIsInVzZXJuYW1lIjoidGVzdC11c2VybmFtZSJ9.H0qBnhAVi9duwxx9NyhaYKJyxsigjeWYQwbKJJqevjhAu0A-caBEKwSXs_bwco23InmkjXtUDvEtiAH5d8bJaood3-0tWoBYHRpXz8yOtF8iCVw_nAqzwbd-GW289B95SKbDSAyhFq5MCNWMuookx5J8dchviBCUImdU98Rr1sK2t-T8bNF0hd2Yutn2m6SLp_8rLxmuXipae_tqRtTuslltDnI2zoF6QM2eGntkyEHVo07Bz90lnonBjB5Ql6GdH8l-G8NHa2bGdIozOFO5bmjyvv1ap10w9ch9oNDFFmGhGX9txA1HjNFxLvFpNdwKJRM0yjjTGsnzcYb45691cQ',
    'x-amzn-oidc-identity': 'uusihetu@example.com',
    'x-amzn-oidc-data':
      'eyJ0eXAiOiJKV1QiLCJraWQiOiJ0ZXN0LWtpZCIsImFsZyI6IlJTMjU2IiwiaXNzIjoidGVzdC1pc3MiLCJjbGllbnQiOiJ0ZXN0LWNsaWVudCIsInNpZ25lciI6InRlc3Qtc2lnbmVyIiwiZXhwIjoxODkzNDU2MDAwfQ.eyJzdWIiOiJ1dXNpaGV0dUBleGFtcGxlLmNvbSIsImN1c3RvbTpGSV9uYXRpb25hbElOIjoiMTAwMTkwWTk5OVAiLCJ1c2VybmFtZSI6InRlc3QtdXNlcm5hbWUiLCJleHAiOjE4OTM0NTYwMDAsImlzcyI6InRlc3QtaXNzIn0.O8P6zAmPezSO8DRydAaPPvBHX9-SskXjxx4AyiVNceJW-Zm6y0T8cg1Zsw9JICxdmUOkowQ4kzNlYANpcNorIHt9RZ-mtIdpLP12VdkB4kEAUgrPc8_T-rGoumd91z2n1a6aTaGNBeaf97ep0Xk7F0AtfGDAXSnEwyeHOe_dd_CPI0GTlY-b9ewdlqZlBiNeMcop89kxKHVFkmWLGulZv1CoxCyahYRUGAodBp1MmuYwNzfMIxUmDjOYR7JZlm3vrV_HJ7exaPJIjA_LbWbJFvuKTDYwICTcK5UTHvC2Pynnye7BLBROaYZ5Jn9rShxxRY07Rf9xNAJQQ5FcpKzJKw'
  }
};

const baseUrl = Cypress.config('baseUrl');

describe('Henkilotunnus', () => {
  before(() => {
    cy.resetDb();
  });

  context('Given user with vanha hetu does not exist in the system', () => {
    beforeEach(() => {
      cy.intercept(/\/api\/private/, req => {
        req.headers = { ...req.headers, ...FIXTURES.vanhaHetuHeaders };
      });
    });

    describe('When trying to login with vanha hetu creds', () => {
      it('Then can not login', () => {
        cy.visit('/');
        cy.get('[data-cy="error"]').contains('Puutteelliset käyttöoikeudet');
      });
    });
  });

  context('Given user with uusi hetu does not exist in the system', () => {
    beforeEach(() => {
      cy.intercept(/\/api\/private/, req => {
        req.headers = { ...req.headers, ...FIXTURES.uusiHetuHeaders };
      });
    });

    describe('When trying to login with uusi hetu creds', () => {
      it('Then can not login', () => {
        cy.visit('/');
        cy.get('[data-cy="error"]').contains('Puutteelliset käyttöoikeudet');
      });
    });
  });

  context("Given the users-to-be-added don't exist in the system", () => {
    beforeEach(() => {
      cy.intercept(/\/api\/private/, req => {
        req.headers = { ...req.headers, ...FIXTURES.paakayttajaHeaders };
      });
    });
    describe('When pääkäyttäjä tries to add them', () => {
      beforeEach(() => {
        cy.intercept(/\/api\/private/, req => {
          req.headers = { ...req.headers, ...FIXTURES.paakayttajaHeaders };
        });
      });
      it('Then pääkäyttäjä can add user with vanha hetu', () => {
        cy.visit('/#/kayttaja/new');

        cy.get('[data-cy="etunimi"]').type('Vanha');

        cy.get('[data-cy="sukunimi"]').type('Hetu');

        cy.get('[data-cy="email"]').type('vanhahetu@example.com');

        cy.get('[data-cy="puhelin"]').type('12321');

        cy.get('[data-cy="henkilotunnus"]').type('100190-999P');

        cy.get('[data-cy="rooli-select"]')
          .click()
          .parent()
          .within(() => {
            cy.contains('Pääkäyttäjä').click();
          });

        cy.get('[data-cy="kayttaja-form-submit"]').click();
      });

      it('Then pääkäyttäjä can add user with uusi hetu', () => {
        cy.visit('/#/kayttaja/new');

        cy.get('[data-cy="etunimi"]').type('Uusi');

        cy.get('[data-cy="sukunimi"]').type('Hetu');

        cy.get('[data-cy="email"]').type('uusihetu@example.com');

        cy.get('[data-cy="puhelin"]').type('12321');

        cy.get('[data-cy="henkilotunnus"]').type('100190Y999P');

        cy.get('[data-cy="rooli-select"]')
          .click()
          .parent()
          .within(() => {
            cy.contains('Pääkäyttäjä').click();
          });

        cy.get('[data-cy="kayttaja-form-submit"]').click();
      });
    });
  });

  context(
    'Given the users with vanha and uusi hetu are added to the system',
    () => {
      describe('When user with vanha hetu tries to login', () => {
        beforeEach(() => {
          cy.intercept(/\/api\/private/, req => {
            req.headers = { ...req.headers, ...FIXTURES.vanhaHetuHeaders };
          });
        });
        it('Then should be able to login', () => {
          cy.visit('/');

          cy.get('[data-cy="fullname-in-header"]').contains('Vanha Hetu');
        });
      });
      describe('When user with uusi hetu tries to login', () => {
        beforeEach(() => {
          cy.intercept(/\/api\/private/, req => {
            req.headers = { ...req.headers, ...FIXTURES.uusiHetuHeaders };
          });
        });
        it('Then should be able to login', () => {
          cy.visit('/');

          cy.get('[data-cy="fullname-in-header"]').contains('Uusi Hetu');
        });
      });
    }
  );
});
