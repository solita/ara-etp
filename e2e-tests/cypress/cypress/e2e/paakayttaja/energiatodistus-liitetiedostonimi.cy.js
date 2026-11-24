import paakayttajaHeaders from '../../fixtures/users/paakayttaja.json';
import {
  assertRejectedAttachment,
  assertSuccessfulAttachment,
  uploadAttachment
} from '../../support/attachment-test-helpers';

const backendUrl = Cypress.config('backendUrl');
const energiatodistusId = 7;
const energiatodistusVersio = 2018;
const uploadUrl = `/api/private/energiatodistukset/${energiatodistusVersio}/${energiatodistusId}/liitteet/files`;

const openAttachments = () => {
  cy.visit(
    `/#/energiatodistus/${energiatodistusVersio}/${energiatodistusId}/liitteet`
  );
  cy.get('[data-cy="droparea"]').should('be.visible');
};

const assertRejectedUpload = options => {
  uploadAttachment(uploadUrl, options).then(({ response }) => {
    assertRejectedAttachment(response, options.expectedType);
  });
};

context('Energiatodistuksen liitetiedostonimi', () => {
  before(() => {
    cy.resetDb();
  });

  beforeEach(() => {
    cy.intercept(/\/api\/private/, req => {
      req.headers = { ...req.headers, ...paakayttajaHeaders };
    });
  });

  it('rejects an invalid filename with 400', () => {
    openAttachments();
    assertRejectedUpload({
      fileName: 'document?.pdf',
      expectedType: 'liite-invalid-filename'
    });
  });

  it('rejects a forbidden extension with 400', () => {
    openAttachments();
    assertRejectedUpload({
      fileName: 'document.exe',
      expectedType: 'liite-forbidden-extension'
    });
    cy.contains('document.exe').should('not.exist');
  });

  it('rejects executable content with 400', () => {
    openAttachments();
    assertRejectedUpload({
      contents: Cypress.Buffer.from([0x4d, 0x5a, 0x00, 0x00]),
      fileName: 'virus.dat',
      mimeType: 'application/octet-stream',
      expectedType: 'liite-executable'
    });
  });

  it('rejects a content-type mismatch with 400', () => {
    openAttachments();
    assertRejectedUpload({
      contents: Cypress.Buffer.from([0x25, 0x50, 0x44, 0x46, 0x00, 0x00]),
      fileName: 'not-really-a-png.png',
      mimeType: 'image/png',
      expectedType: 'liite-content-type-mismatch'
    });
  });

  it('rejects an extension mismatch with 400', () => {
    openAttachments();
    assertRejectedUpload({
      contents: Cypress.Buffer.from([0x25, 0x50, 0x44, 0x46, 0x00, 0x00]),
      fileName: 'document.txt',
      mimeType: 'application/pdf',
      expectedType: 'liite-extension-mismatch'
    });
  });

  it('uploads a valid attachment successfully', () => {
    openAttachments();
    uploadAttachment(uploadUrl, { fileName: 'document.pdf' }).then(
      ({ response }) => {
        assertSuccessfulAttachment(
          response,
          'Energiatodistuksen liitteet lisätty onnistuneesti.'
        );
      }
    );
    cy.contains('document.pdf').should('be.visible');
  });
});
