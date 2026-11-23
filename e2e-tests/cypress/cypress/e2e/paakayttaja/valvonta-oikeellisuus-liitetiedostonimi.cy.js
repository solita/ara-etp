import paakayttajaHeaders from '../../fixtures/users/paakayttaja.json';

const backendUrl = Cypress.config('backendUrl');
const energiatodistusId = 7;
const energiatodistusVersio = 2018;

const createDraftToimenpide = () =>
  cy
    .request({
      method: 'POST',
      url: `${backendUrl}/api/private/valvonta/oikeellisuus/${energiatodistusId}/toimenpiteet`,
      headers: paakayttajaHeaders,
      body: {
        // 4 = rfi-reply, a draft toimenpide that does not publish or send email.
        'type-id': 4,
        'deadline-date': null,
        'template-id': null,
        description: 'Liitetiedostonimen E2E-testi',
        virheet: [],
        'severity-id': null,
        tiedoksi: []
      }
    })
    .then(response => {
      expect(response.status).to.eq(201);
      return response.body.id;
    });

const uploadAttachment = (toimenpideId, fileName) => {
  cy.intercept(
    'POST',
    `/api/private/valvonta/oikeellisuus/${energiatodistusId}/toimenpiteet/${toimenpideId}/liitteet/files`
  ).as('uploadAttachment');

  cy.get('[data-cy="droparea"] input[type="file"]').selectFile(
    {
      contents: Cypress.Buffer.from('attachment test'),
      fileName,
      mimeType: 'text/plain'
    },
    { force: true }
  );

  return cy.wait('@uploadAttachment');
};

const openToimenpide = toimenpideId => {
  cy.visit(
    `/#/valvonta/oikeellisuus/${energiatodistusVersio}/${energiatodistusId}/${toimenpideId}`
  );
};

const assertRejectedAttachment = (response, expectedType) => {
  expect(response.statusCode).to.eq(400);
  expect(response.body.type).to.eq(expectedType);
};

context('Oikeellisuuden valvonnan liitetiedostonimi', () => {
  before(() => {
    cy.resetDb();
  });

  beforeEach(() => {
    cy.intercept(/\/api\/private/, req => {
      req.headers = { ...req.headers, ...paakayttajaHeaders };
    });
  });

  it('rejects a forbidden extension with 400', () => {
    createDraftToimenpide().then(toimenpideId => {
      openToimenpide(toimenpideId);

      uploadAttachment(toimenpideId, 'document.exe').then(({ response }) => {
        assertRejectedAttachment(response, 'liite-forbidden-extension');
      });

      cy.get('[data-cy="droparea"]').should('be.visible');
      cy.contains('document.exe').should('not.exist');
    });
  });

  it('uploads a valid attachment successfully', () => {
    createDraftToimenpide().then(toimenpideId => {
      openToimenpide(toimenpideId);

      uploadAttachment(toimenpideId, 'document.pdf').then(({ response }) => {
        expect(response.statusCode).to.eq(201);
      });

      cy.contains('document.pdf').should('be.visible');
    });
  });
});
