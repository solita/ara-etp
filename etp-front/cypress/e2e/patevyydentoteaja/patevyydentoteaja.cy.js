const FIXTURES = {
  headers: {
    'x-amzn-oidc-accesstoken':
      'eyJraWQiOiJ0ZXN0LWtpZCIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJwYXRldnl5ZGVudG90ZWFqYUBzb2xpdGEuZmkiLCJ0b2tlbl91c2UiOiJhY2Nlc3MiLCJzY29wZSI6Im9wZW5pZCIsImF1dGhfdGltZSI6MTU4MzIzMDk2OSwiaXNzIjoiaHR0cHM6Ly9yYXcuZ2l0aHVidXNlcmNvbnRlbnQuY29tL3NvbGl0YS9ldHAtY29yZS9mZWF0dXJlL0FFLTQzLWF1dGgtaGVhZGVycy1oYW5kbGluZy9ldHAtYmFja2VuZC9zcmMvbWFpbi9yZXNvdXJjZXMiLCJleHAiOjE4OTM0NTYwMDAsImlhdCI6MTU4MzQxMzQyNCwidmVyc2lvbiI6MiwianRpIjoiNWZkZDdhMjktN2VlYS00ZjNkLWE3YTYtYzIyODQyNmY2MTJiIiwiY2xpZW50X2lkIjoidGVzdC1jbGllbnRfaWQiLCJ1c2VybmFtZSI6InRlc3RfdXNlcm5hbWUifQ.XXWQOcTxuz5NUs8E2oQqKBEyBHXdh7WbaUQfpKIKRRc8QDgeV2VfwJd4C8VnxyEunrTxNU2whQm_dFxAIVhg2gUVpdnrYwB023dSZHGOowlnbHQYW_xVcFplW-pX0UrWuhdvo2FU3ZTW4Y_QBZzrztSMvQU0bThk8VDkWke4NsuI-ImPr_iczd20fQAcvXsuY8fXdxYDnXMi_mjQMOl8CmuIi-08zUnkIdRf0fvD40UUsxAZqYsxHdJyIev6XUQiWvscVjZ5LfGmbnPzpwqBYb2rrwDT7yefvys44d9klzyEcNV9v9-p_LwYNiM4Uetsb8ElK_6InPRmSEuRtLHWGQ',
    'x-amzn-oidc-identity': 'patevyydentoteaja@solita.fi',
    'x-amzn-oidc-data':
      'eyJ0eXAiOiJKV1QiLCJraWQiOiJ0ZXN0LWtpZCIsImFsZyI6IlJTMjU2IiwiaXNzIjoidGVzdC1pc3MiLCJjbGllbnQiOiJ0ZXN0LWNsaWVudCIsInNpZ25lciI6InRlc3Qtc2lnbmVyIiwiZXhwIjoxODkzNDU2MDAwfQ.eyJzdWIiOiJwYXRldnl5ZGVudG90ZWFqYUBzb2xpdGEuZmkiLCJlbWFpbCI6InBhdGV2eXlkZW50b3RlYWphQHNvbGl0YS5maSIsInVzZXJuYW1lIjoidGVzdC11c2VybmFtZSIsImV4cCI6MTg5MzQ1NjAwMCwiaXNzIjoidGVzdC1pc3MifQ.UTDDu-r4dQWydb-IMe_HE3-B-Z3nHm3znYy1XOdhSP2gf3pFGiJ03QlVhCq54hH7bfGD6vdtrGXqBwcpznTtoECR5sTGz5N0fYW7Ff94NG8W_DlvE8XUGVV3eh3GreS1tPRaHLVXEdTb3m3ENdAAHBe-T9V1vOfj3S7Nd5JAkKBQnpzTPrxXUjSzA4gA2n0fm3nxvEitQW40UKoglA6jebaGu3_etnOMBbMP-y4TrnA9WvHbtl0YKP0QKY0dxNEJWPUvkfylwqvHWUHvfzi-GeBwBz3PkH7EPJegdoBU4gAr6zmubUf49OjWVBVw7dg9iXzwh0pUopcEZvQetI0KgA'
  },
  validFile: `FISE;Etunimi;Sukunimi;240301A921V;Jakeluosoite 1;12345;Postitoimipaikka;a@example.com;000000000;1;24.3.2021\nFISE;Etunimi2;Sukunimi2;090999-9431;Jakeluosoite 2;54321;Postitoimipaikka2;b@example.com;11111111;2;24.3.2021`,
  invalidFile: `FISE;Etunimi;Sukunimi;240301A921G;Jakeluosoite 1;12345;Postitoimipaikka;a@example.com;000000000;1;24.3.2021\nFISE;Etunimi2;Sukunimi2;090999-9431;Jakeluosoite 2;54321;Postitoimipaikka2;b@example.com;11111111;2;24.3.2021`
};

context('Patevyyden toteaja', () => {
  beforeEach(() => {
    cy.intercept('https://localhost:3000/api/private/', req => {
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
          'https://localhost:3000/#/laatija/all?search=&page=0&state='
        )
      );

      cy.get('[data-cy="laatija-row"]').should('have.length', 15);
    });

    it('should filter laatijat', () => {
      cy.visit('https://localhost:3000/#/laatija/all');
      cy.get('input[type="text"]')
        .as('input')
        .type('laat');

      cy.get('[data-cy="laatija-row"]').should('have.length', 9);
      cy.location().should(loc =>
        assert.equal(
          loc.toString(),
          'https://localhost:3000/#/laatija/all?search=laat&page=0&state='
        )
      );

      cy.get('@input').type('{selectall}{backspace}');

      cy.get('[data-cy="laatija-row"]').should('have.length', 15);
      cy.location().should(loc =>
        assert.equal(
          loc.toString(),
          'https://localhost:3000/#/laatija/all?search=&page=0&state='
        )
      );
    });
  });
});
