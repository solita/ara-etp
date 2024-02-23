const FIXTURES = {
  headers: {
    'x-amzn-oidc-accesstoken':
      'eyJraWQiOiJ0ZXN0LWtpZCIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJ0aWVkb3R0YXJrYXN0YW1hdHRhQGV4YW1wbGUuY29tIiwidG9rZW5fdXNlIjoiYWNjZXNzIiwic2NvcGUiOiJvcGVuaWQiLCJhdXRoX3RpbWUiOjE1ODMyMzA5NjksImlzcyI6Imh0dHBzOi8vcmF3LmdpdGh1YnVzZXJjb250ZW50LmNvbS9zb2xpdGEvZXRwLWNvcmUvZmVhdHVyZS9BRS00My1hdXRoLWhlYWRlcnMtaGFuZGxpbmcvZXRwLWJhY2tlbmQvc3JjL21haW4vcmVzb3VyY2VzIiwiZXhwIjoxODkzNDU2MDAwLCJpYXQiOjE2MDE1MTA0MDAsInZlcnNpb24iOjIsImp0aSI6IjVmZGQ3YTI5LTdlZWEtNGYzZC1hN2E2LWMyMjg0MjZmNjEyYiIsImNsaWVudF9pZCI6InRlc3QtY2xpZW50X2lkIiwidXNlcm5hbWUiOiJ0ZXN0LXVzZXJuYW1lIn0.daZ0jb24s66cLbGPeU5kaaV7HGNusZfzmTl_3Jsv3YLc3JlWIKUriluTg4hg0c8o5Ppik9bFOT-IJU81ZptmaPeyVh57Ph1Urx3ZMWWKgHzXSSJaQGbXX1Nd-cy5-Qd_WfHjB_EYjzdkTZwLOGOVOynTeLVfIFPHBfz-1FexOGQ0voBU_V-719OQTlEara1nbDAQqX-hxgSlh9jSEl92C9tKo8c3ljIuUQwSqKSfKTdQe87Q1zPOv-5CNp-n6CUUWLi7a7XCiVw4x0op5CTPZfYEXezTeWqtCJnsQycUa0Ovr14cg88s9jkhrMehPdbjvFuECS0UXnX6pZuRSZkTAg',
    'x-amzn-oidc-identity': 'tiedottarkastamatta@example.com',
    'x-amzn-oidc-data':
      'eyJ0eXAiOiJKV1QiLCJraWQiOiJ0ZXN0LWtpZCIsImFsZyI6IlJTMjU2IiwiaXNzIjoidGVzdC1pc3MiLCJjbGllbnQiOiJ0ZXN0LWNsaWVudCIsInNpZ25lciI6InRlc3Qtc2lnbmVyIiwiZXhwIjoxODkzNDU2MDAwfQ.eyJzdWIiOiJ0aWVkb3R0YXJrYXN0YW1hdHRhQGV4YW1wbGUuY29tIiwiY3VzdG9tOkZJX25hdGlvbmFsSU4iOiIwMTAxMDZBOTMxMSIsInVzZXJuYW1lIjoidGVzdC11c2VybmFtZSIsImV4cCI6MTg5MzQ1NjAwMCwiaXNzIjoidGVzdC1pc3MifQ.OxY7jXs1tKWQIf5FDeHSkBGSbOJTxIOuL1ATuwP3fGhouHAUlo6VOItilgjOfIWc7fpDk0iHmddD8JUmePJwiaMus5QeLMf6tZpRhQ4yFiqOReKCM_Bh87Tpm2rbc3MKsaeRwSfSSJeh6LgZv4IX6_REW_iGo39EZ8avxbQg0LRhczsC23Momyl8HrHbktyk7MeTuqUxJV9Znqv6GSFquHrGg0QRFX_u3MfYbXDCG3SL7WGMJ7bxgXM2hSrW1cyZnsNEbjMAZRGCueIIHUuROqByr15WDBCDVksDQR1KQ6usaFZGJR9w-XLg-JarOnR6sK8s4WkOp0owndcG6XnWVA'
  }
};

const baseUrl = Cypress.config('baseUrl');

context("User that hasn't verified their information is asked to do so", () => {
  beforeEach(() => {
    cy.intercept(/\/api\/private/, req => {
      req.headers = { ...req.headers, ...FIXTURES.headers };
    });
  });

  it('Navigating to main page should redirect to personal information page', () => {
    cy.visit('/');

    cy.location().should(loc =>
      assert.equal(loc.toString(), `${baseUrl}/#/kayttaja/21`)
    );
  });

  it('Navigating to other pages works as expected', () => {
    cy.visit('/#/energiatodistus/all');

    cy.location().should(loc =>
      assert.equal(loc.toString(), `${baseUrl}/#/energiatodistus/all`)
    );
  });

  it('After verifying the information redirect is to energiatodistukset instead', () => {
    cy.visit('/#/kayttaja/21');

    cy.get('[data-cy="verify-submit"]').click();

    cy.visit('/');
    cy.location().should(loc =>
      assert.equal(loc.toString(), `${baseUrl}/#/energiatodistus/all`)
    );
  });
});
