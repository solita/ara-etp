context('Laatija', () => {
  beforeEach(() => {
    cy.intercept('https://localhost:3000/api/private/', req => {
      req.headers['x-amzn-oidc-accesstoken'] =
        'eyJraWQiOiJ0ZXN0LWtpZCIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJsYWF0aWphQHNvbGl0YS5maSIsInRva2VuX3VzZSI6ImFjY2VzcyIsInNjb3BlIjoib3BlbmlkIiwiYXV0aF90aW1lIjoxNTgzMjMwOTY5LCJpc3MiOiJodHRwczovL3Jhdy5naXRodWJ1c2VyY29udGVudC5jb20vc29saXRhL2V0cC1jb3JlL2ZlYXR1cmUvQUUtNDMtYXV0aC1oZWFkZXJzLWhhbmRsaW5nL2V0cC1iYWNrZW5kL3NyYy9tYWluL3Jlc291cmNlcyIsImV4cCI6MTg5MzQ1NjAwMCwiaWF0IjoxNjAxNTEwNDAwLCJ2ZXJzaW9uIjoyLCJqdGkiOiI1ZmRkN2EyOS03ZWVhLTRmM2QtYTdhNi1jMjI4NDI2ZjYxMmIiLCJjbGllbnRfaWQiOiJ0ZXN0LWNsaWVudF9pZCIsInVzZXJuYW1lIjoidGVzdC11c2VybmFtZSJ9.HAlEjQejKyvOoxHrORdnnTnfwiD5lUuEBMalTFKQtu_6luxqxJYfyn-etf2AkaoKWkqsT9g_-k3BV1hT-R1Y0gK3Xl21yT1MDk8QmEZlp1ztiOx4o5ufrX0t6C_Y-VKBxqQkRWLw8crSKfH2TpsTETDetA2gCReoHfHBt2_O63xL-y35glJIzHlc3egqlNFfXwduBZy8ON08h-hhZp0b8AtlaYVoY_OZY3jjfmA19jzf19rEUmK6qOJhJPr-Sgob_CDFf8G6KO4-lIF4FqdUYTvmiJoLiFmefRFscqVQTTurbsxIKmmz5JFY10vCvqp4uWcvO60O0-zgZEQcbV1Ltw';
      req.headers['x-amzn-oidc-identity'] = 'laatija@solita.fi';
      req.headers['x-amzn-oidc-data'] =
        'eyJ0eXAiOiJKV1QiLCJraWQiOiJ0ZXN0LWtpZCIsImFsZyI6IlJTMjU2IiwiaXNzIjoidGVzdC1pc3MiLCJjbGllbnQiOiJ0ZXN0LWNsaWVudCIsInNpZ25lciI6InRlc3Qtc2lnbmVyIiwiZXhwIjoxODkzNDU2MDAwfQ.eyJzdWIiOiJsYWF0aWphQHNvbGl0YS5maSIsImVtYWlsIjoibGFhdGlqYUBzb2xpdGEuZmkiLCJ1c2VybmFtZSI6InRlc3QtdXNlcm5hbWUiLCJleHAiOjE4OTM0NTYwMDAsImlzcyI6InRlc3QtaXNzIn0.HiEjH9svgiUxPmC__kag997di2cNy5PfRehCeNtxN55OMLQM8PcTJTO4_0sY6EXkedWRZCLFJ2jxqMXyOL_EXLK9DmjRyZw6RrVXwG8y2feoAvZWvJU2zcnH_j30rd5DuICNeUwx_l3ej3ZiX1JNmh3PAn_7j85aVzEs7-ipChG1K2qSJPvluXlyfEYyhmOv3U7kcQQRM2R-LipXCWXRVAuj6HOuQXF1jwdV2RTQHBWfNb2zyUQ3OoTDBH0lPy9FRww7KLOMzbDEz8fNACnZXZSGGkz2NI1YuLS0-IBjqlwewCR9RhuuU6NoIojTeJo9LMZzbPaYqXnNSDTZ_uB1Bw';
    });
  });

  it('should redirect to energiatodistukset', () => {
    cy.visit('https://localhost:3000');

    cy.location().should(loc =>
      assert.equal(
        loc.toString(),
        'https://localhost:3000/#/energiatodistus/all'
      )
    );
  });

  it('should navigate to yritykset', () => {
    cy.visit('https://localhost:3000');

    cy.contains('Yritykset').click();

    cy.location().should(loc =>
      assert.equal(
        loc.toString(),
        'https://localhost:3000/#/laatija/2/yritykset'
      )
    );
  });

  it('should navigate to viestit', () => {
    cy.visit('https://localhost:3000');

    cy.contains('Viestit').click();

    cy.location().should(loc =>
      assert.equal(loc.toString(), 'https://localhost:3000/#/viesti/all')
    );
  });

  it('should navigate to energiatodistus', () => {
    cy.visit('https://localhost:3000');

    cy.get('tbody tr')
      .first()
      .click();

    cy.location().should(loc =>
      assert.equal(
        loc.toString(),
        'https://localhost:3000/#/energiatodistus/2018/5'
      )
    );
  });

  it('should change laskutettava yritys', () => {
    cy.visit('https://localhost:3000/#/energiatodistus/2018/5');

    cy.get('#laskutettava-yritys-id').click();
    cy.contains('Paavon pesusieni').click();
    cy.get('[name="energiatodistus.laskutus.postiosoite"]').should(
      'have.value',
      'paavo, 12345 paavo'
    );
  });
});
