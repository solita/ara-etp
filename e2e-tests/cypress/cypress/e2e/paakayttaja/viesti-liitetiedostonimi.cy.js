import paakayttajaHeaders from '../../fixtures/users/paakayttaja.json';
import {
  assertRejectedAttachment,
  assertSuccessfulAttachment,
  uploadAttachment
} from '../../support/attachment-test-helpers';

const backendUrl = Cypress.config('backendUrl');

const createDraftViestiketju = () =>
  cy
    .request({
      method: 'GET',
      url: `${backendUrl}/api/private/viestit/osapuolet`,
      headers: paakayttajaHeaders
    })
    .then(({ body: osapuolet }) =>
      cy
        .request({
          method: 'POST',
          url: `${backendUrl}/api/private/viestit`,
          headers: paakayttajaHeaders,
          body: {
            vastaanottajat: [osapuolet[0].id],
            'vastaanottajaryhma-id': null,
            'energiatodistus-id': null,
            subject: 'Liitetiedostonimen E2E-testi',
            body: 'Viestin liitteen E2E-testi'
          }
        })
        .then(response => {
          expect(response.status).to.eq(201);
          return response.body.id;
        })
    );

const openAttachments = viestiketjuId => {
  cy.visit(`/#/viesti/${viestiketjuId}/liitteet`);
  cy.get('[data-cy="droparea"]').should('be.visible');
};

const assertRejectedUpload = (uploadUrl, options) => {
  uploadAttachment(uploadUrl, options).then(({ response }) => {
    assertRejectedAttachment(response, options.expectedType);
  });
};

context('Viestin liitetiedostonimi', () => {
  before(() => {
    cy.resetDb();
  });

  beforeEach(() => {
    cy.intercept(/\/api\/private/, req => {
      req.headers = { ...req.headers, ...paakayttajaHeaders };
    });
  });

  const withDraftThread = test => {
    createDraftViestiketju().then(viestiketjuId => {
      const uploadUrl = `/api/private/viestit/${viestiketjuId}/liitteet/files`;
      openAttachments(viestiketjuId);
      test(uploadUrl);
    });
  };

  it('rejects an invalid filename with 400', () => {
    withDraftThread(uploadUrl =>
      assertRejectedUpload(uploadUrl, {
        fileName: 'document?.pdf',
        expectedType: 'liite-invalid-filename'
      })
    );
  });

  it('rejects a forbidden extension with 400', () => {
    withDraftThread(uploadUrl => {
      assertRejectedUpload(uploadUrl, {
        fileName: 'document.exe',
        expectedType: 'liite-forbidden-extension'
      });
      cy.contains('document.exe').should('not.exist');
    });
  });

  it('rejects executable content with 400', () => {
    withDraftThread(uploadUrl =>
      assertRejectedUpload(uploadUrl, {
        contents: Cypress.Buffer.from([0x4d, 0x5a, 0x00, 0x00]),
        fileName: 'virus.dat',
        mimeType: 'application/octet-stream',
        expectedType: 'liite-executable'
      })
    );
  });

  it('rejects a content-type mismatch with 400', () => {
    withDraftThread(uploadUrl =>
      assertRejectedUpload(uploadUrl, {
        contents: Cypress.Buffer.from([0x25, 0x50, 0x44, 0x46, 0x00, 0x00]),
        fileName: 'not-really-a-png.png',
        mimeType: 'image/png',
        expectedType: 'liite-content-type-mismatch'
      })
    );
  });

  it('rejects an extension mismatch with 400', () => {
    withDraftThread(uploadUrl =>
      assertRejectedUpload(uploadUrl, {
        contents: Cypress.Buffer.from([0x25, 0x50, 0x44, 0x46, 0x00, 0x00]),
        fileName: 'document.txt',
        mimeType: 'application/pdf',
        expectedType: 'liite-extension-mismatch'
      })
    );
  });

  it('uploads a valid attachment successfully', () => {
    withDraftThread(uploadUrl => {
      uploadAttachment(uploadUrl, { fileName: 'document.pdf' }).then(
        ({ response }) => {
          assertSuccessfulAttachment(
            response,
            'Liitetiedostot lisätty viestiin.'
          );
        }
      );
      cy.contains('document.pdf').should('be.visible');
    });
  });
});
