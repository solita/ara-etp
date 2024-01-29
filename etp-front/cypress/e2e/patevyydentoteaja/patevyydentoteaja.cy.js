const FIXTURES = {
  headers: {
    'x-amzn-oidc-accesstoken':
      'eyJraWQiOiJ0ZXN0LWtpZCIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJwYXRldnl5ZGVudG90ZWFqYUBzb2xpdGEuZmkiLCJ0b2tlbl91c2UiOiJhY2Nlc3MiLCJzY29wZSI6Im9wZW5pZCIsImF1dGhfdGltZSI6MTU4MzIzMDk2OSwiaXNzIjoiaHR0cHM6Ly9yYXcuZ2l0aHVidXNlcmNvbnRlbnQuY29tL3NvbGl0YS9ldHAtY29yZS9mZWF0dXJlL0FFLTQzLWF1dGgtaGVhZGVycy1oYW5kbGluZy9ldHAtYmFja2VuZC9zcmMvbWFpbi9yZXNvdXJjZXMiLCJleHAiOjE4OTM0NTYwMDAsImlhdCI6MTU4MzQxMzQyNCwidmVyc2lvbiI6MiwianRpIjoiNWZkZDdhMjktN2VlYS00ZjNkLWE3YTYtYzIyODQyNmY2MTJiIiwiY2xpZW50X2lkIjoidGVzdC1jbGllbnRfaWQiLCJ1c2VybmFtZSI6InRlc3RfdXNlcm5hbWUifQ.XXWQOcTxuz5NUs8E2oQqKBEyBHXdh7WbaUQfpKIKRRc8QDgeV2VfwJd4C8VnxyEunrTxNU2whQm_dFxAIVhg2gUVpdnrYwB023dSZHGOowlnbHQYW_xVcFplW-pX0UrWuhdvo2FU3ZTW4Y_QBZzrztSMvQU0bThk8VDkWke4NsuI-ImPr_iczd20fQAcvXsuY8fXdxYDnXMi_mjQMOl8CmuIi-08zUnkIdRf0fvD40UUsxAZqYsxHdJyIev6XUQiWvscVjZ5LfGmbnPzpwqBYb2rrwDT7yefvys44d9klzyEcNV9v9-p_LwYNiM4Uetsb8ElK_6InPRmSEuRtLHWGQ',
    'x-amzn-oidc-identity': 'patevyydentoteaja@solita.fi',
    'x-amzn-oidc-data':
      'eyJ0eXAiOiJKV1QiLCJraWQiOiJ0ZXN0LWtpZCIsImFsZyI6IlJTMjU2IiwiaXNzIjoidGVzdC1pc3MiLCJjbGllbnQiOiJ0ZXN0LWNsaWVudCIsInNpZ25lciI6InRlc3Qtc2lnbmVyIiwiZXhwIjoxODkzNDU2MDAwfQ.eyJzdWIiOiJwYXRldnl5ZGVudG90ZWFqYUBzb2xpdGEuZmkiLCJjdXN0b206RklfbmF0aW9uYWxJTiI6IjAxMDI4MC05NTJMIiwidXNlcm5hbWUiOiJ0ZXN0LXVzZXJuYW1lIiwiZXhwIjoxODkzNDU2MDAwLCJpc3MiOiJ0ZXN0LWlzcyJ9.W4v0fhxmHsHWM-mH0DEwuyp_JEHFl2qFEAg8L2l7R9oL3CuqfSnV19cSECCjDARtAMfePpCE_2G4M7SgmhQUpH7LORvcyloBT2BoWfNKPDeH-8UzrGSDLRWpDg69vP87qxmMMdGyigj1t7laoeT7OUErKvsy6HNLRTtrYXZu0JgLnCJ9t-OGDkm4WswQJvxUO9pciFATvyrkcQnGb51Z0Uf6K4rZlFxb1tapWx_R3sHFGdPpBYdEgK2L7zhGKQXJ9TU96cUJEnRwvI_c5SzXhOW28xMeLL0rMm8eLeV8Y4QMj5bm5Vss_b2hrH-wsCpMcPYTdub_40lvv0FZAcRBoQ'
  },
  validFile: `FISE;Etunimi;Sukunimi;240301A921V;Jakeluosoite 1;12345;Postitoimipaikka;a@example.com;000000000;1;24.3.2021\nFISE;Etunimi2;Sukunimi2;090999-9431;Jakeluosoite 2;54321;Postitoimipaikka2;b@example.com;11111111;2;24.3.2021`,
  invalidFile: `FISE;Etunimi;Sukunimi;240301A921G;Jakeluosoite 1;12345;Postitoimipaikka;a@example.com;000000000;1;24.3.2021\nFISE;Etunimi2;Sukunimi2;090999-9431;Jakeluosoite 2;54321;Postitoimipaikka2;b@example.com;11111111;2;24.3.2021`
};

context('Patevyyden toteaja', () => {
  beforeEach(() => {
    cy.intercept(/\/api\/private/, req => {
      req.headers = { ...req.headers, ...FIXTURES.headers };
    });
  });

  it('should redirect to laatijoidentuonti', () => {
    cy.visit('https://localhost:3000');

    cy.location().should(loc =>
      assert.equal(
        loc.toString(),
        'https://localhost:3000/#/laatija/laatijoidentuonti'
      )
    );
  });

  describe('laatija upload', () => {
    it('should highlight and unhighlight on dragevents', () => {
      cy.visit('https://localhost:3000/#/laatija/laatijoidentuonti');
      cy.get('[data-cy="droparea"]').as('upload');

      cy.get('@upload').should('exist');
      cy.get('@upload').should('not.have.class', 'highlight');

      cy.get('@upload').trigger('dragenter');
      cy.get('@upload').should('have.class', 'highlight');

      cy.get('@upload').trigger('dragleave');
      cy.get('@upload').should('not.have.class', 'highlight');
    });

    it('should read and submit file', () => {
      const files = [new File(FIXTURES.validFile.split(''), 'laatijat.txt')];
      cy.visit('https://localhost:3000/#/laatija/laatijoidentuonti');

      cy.get('[data-cy="droparea"]').trigger('drop', {
        dataTransfer: { files }
      });

      cy.get('[data-cy="laatija-upload-row"]').should('have.length', 2);

      cy.get('[data-cy="laatija-upload-submit"]')
        .as('submit')
        .should('be.enabled');

      cy.intercept(
        {
          method: 'PUT',
          pathname: /\/api\/private\/laatijat$/
        },
        {
          statusCode: 200,
          body: [20, 21]
        }
      ).as('putLaatijat');

      cy.get('@submit').click();

      cy.wait('@putLaatijat');

      cy.get('.alert.success').should('exist');
    });

    it('should read invalid file', () => {
      const files = [new File(FIXTURES.invalidFile.split(''), 'laatijat.txt')];
      cy.visit('https://localhost:3000/#/laatija/laatijoidentuonti');

      cy.get('[data-cy="droparea"]').trigger('drop', {
        dataTransfer: { files }
      });

      cy.get('[data-cy="laatija-upload-row"]').should('have.length', 2);

      cy.get('[data-cy="laatija-upload-submit"]').should('be.disabled');
      cy.contains('240301A921G').should('have.class', 'invalid');
      cy.get('.alert.error').should('exist');
    });
  });

  describe('laatijat', () => {
    it('should navigate to laatijat', () => {
      cy.visit('https://localhost:3000');
      cy.contains('Laatijat').click();

      cy.location().should(loc =>
        assert.equal(
          loc.toString(),
          'https://localhost:3000/#/laatija/all?page=1'
        )
      );

      cy.get('[data-cy="laatija-row"]').should('have.length', 10);
    });

    it('should filter laatijat', () => {
      cy.visit('https://localhost:3000/#/laatija/all');
      cy.get('[data-cy="keyword-search"]')
        .as('input')
        .type('laat');

      cy.get('[data-cy="laatija-row"]').should('have.length', 9);
      cy.location().should(loc =>
        assert.equal(
          loc.toString(),
          'https://localhost:3000/#/laatija/all?search=laat&page=1'
        )
      );

      cy.get('@input').type('{selectall}{backspace}');

      cy.get('[data-cy="laatijat-results-header"]').contains('15');
      cy.location().should(loc =>
        assert.equal(
          loc.toString(),
          'https://localhost:3000/#/laatija/all?page=1'
        )
      );
    });
  });
});
