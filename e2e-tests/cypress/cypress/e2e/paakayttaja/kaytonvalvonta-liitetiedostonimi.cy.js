import paakayttajaHeaders from '../../fixtures/users/paakayttaja.json';
import {
  assertRejectedAttachment,
  assertSuccessfulAttachment,
  uploadAttachment
} from '../../support/attachment-test-helpers';

const backendUrl = Cypress.config('backendUrl');

const createValvonta = () =>
  cy
    .request({
      method: 'POST',
      url: `${backendUrl}/api/private/valvonta/kaytto`,
      headers: paakayttajaHeaders,
      body: {
        rakennustunnus: null,
        katuosoite: 'Liitetiedostonimen E2E-testi',
        postinumero: '90100',
        'ilmoituspaikka-id': null,
        'ilmoituspaikka-description': null,
        ilmoitustunnus: null,
        havaintopaiva: null,
        'valvoja-id': null
      }
    })
    .then(response => {
      expect(response.status).to.eq(201);
      return response.body.id;
    });

const openAttachments = valvontaId => {
  cy.visit(`/#/valvonta/kaytto/${valvontaId}/kohde`);
  cy.get('[data-cy="droparea"]').should('be.visible');
};

const assertRejectedUpload = (uploadUrl, options) => {
  uploadAttachment(uploadUrl, options).then(({ response }) => {
    assertRejectedAttachment(response, options.expectedType);
  });
};

context('Käytönvalvonnan liitetiedostonimi', () => {
  before(() => {
    cy.resetDb();
  });

  beforeEach(() => {
    cy.intercept(/\/api\/private/, req => {
      req.headers = { ...req.headers, ...paakayttajaHeaders };
    });
  });

  const withValvonta = test => {
    createValvonta().then(valvontaId => {
      const uploadUrl = `/api/private/valvonta/kaytto/${valvontaId}/liitteet/files`;
      openAttachments(valvontaId);
      test(uploadUrl);
    });
  };

  it('rejects an invalid filename with 400', () => {
    withValvonta(uploadUrl =>
      assertRejectedUpload(uploadUrl, {
        fileName: 'document?.pdf',
        expectedType: 'liite-invalid-filename'
      })
    );
  });

  it('rejects a forbidden extension with 400', () => {
    withValvonta(uploadUrl => {
      assertRejectedUpload(uploadUrl, {
        fileName: 'document.exe',
        expectedType: 'liite-forbidden-extension'
      });
      cy.contains('document.exe').should('not.exist');
    });
  });

  it('rejects executable content with 400', () => {
    withValvonta(uploadUrl =>
      assertRejectedUpload(uploadUrl, {
        contents: Cypress.Buffer.from([0x4d, 0x5a, 0x00, 0x00]),
        fileName: 'virus.dat',
        mimeType: 'application/octet-stream',
        expectedType: 'liite-executable'
      })
    );
  });

  it('rejects a content-type mismatch with 400', () => {
    withValvonta(uploadUrl =>
      assertRejectedUpload(uploadUrl, {
        contents: Cypress.Buffer.from([0x25, 0x50, 0x44, 0x46, 0x00, 0x00]),
        fileName: 'not-really-a-png.png',
        mimeType: 'image/png',
        expectedType: 'liite-content-type-mismatch'
      })
    );
  });

  it('rejects an extension mismatch with 400', () => {
    withValvonta(uploadUrl =>
      assertRejectedUpload(uploadUrl, {
        contents: Cypress.Buffer.from([0x25, 0x50, 0x44, 0x46, 0x00, 0x00]),
        fileName: 'document.txt',
        mimeType: 'application/pdf',
        expectedType: 'liite-extension-mismatch'
      })
    );
  });

  it('uploads a valid attachment successfully', () => {
    withValvonta(uploadUrl => {
      uploadAttachment(uploadUrl, { fileName: 'document.pdf' }).then(
        ({ response }) => {
          assertSuccessfulAttachment(
            response,
            'Liitetiedostot lisätty käytönvalvontaan.'
          );
        }
      );
      cy.contains('document.pdf').should('be.visible');
    });
  });
});
