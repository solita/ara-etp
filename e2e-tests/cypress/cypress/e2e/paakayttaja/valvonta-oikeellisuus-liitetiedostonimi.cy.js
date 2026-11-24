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

const uploadAttachment = (
  toimenpideId,
  {
    fileName,
    contents = Cypress.Buffer.from('attachment test'),
    mimeType = 'text/plain'
  }
) => {
  cy.intercept(
    'POST',
    `/api/private/valvonta/oikeellisuus/${energiatodistusId}/toimenpiteet/${toimenpideId}/liitteet/files`
  ).as('uploadAttachment');

  cy.get('[data-cy="droparea"] input[type="file"]').selectFile(
    {
      contents,
      fileName,
      mimeType
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

  it('rejects an invalid filename with 400', () => {
    createDraftToimenpide().then(toimenpideId => {
      openToimenpide(toimenpideId);

      uploadAttachment(toimenpideId, { fileName: 'document?.pdf' }).then(
        ({ response }) => {
          assertRejectedAttachment(response, 'liite-invalid-filename');
        }
      );
    });
  });

  it('rejects a forbidden extension with 400', () => {
    createDraftToimenpide().then(toimenpideId => {
      openToimenpide(toimenpideId);

      uploadAttachment(toimenpideId, { fileName: 'document.exe' }).then(
        ({ response }) => {
          assertRejectedAttachment(response, 'liite-forbidden-extension');
        }
      );

      cy.get('[data-cy="droparea"]').should('be.visible');
      cy.contains('document.exe').should('not.exist');
    });
  });

  it('rejects executable content with 400', () => {
    createDraftToimenpide().then(toimenpideId => {
      openToimenpide(toimenpideId);

      uploadAttachment(toimenpideId, {
        contents: Cypress.Buffer.from([0x4d, 0x5a, 0x00, 0x00]),
        fileName: 'virus.dat',
        mimeType: 'application/octet-stream'
      }).then(({ response }) => {
        assertRejectedAttachment(response, 'liite-executable');
      });
    });
  });

  it('rejects a content-type mismatch with 400', () => {
    createDraftToimenpide().then(toimenpideId => {
      openToimenpide(toimenpideId);

      uploadAttachment(toimenpideId, {
        contents: Cypress.Buffer.from([0x25, 0x50, 0x44, 0x46, 0x00, 0x00]),
        fileName: 'not-really-a-png.png',
        mimeType: 'image/png'
      }).then(({ response }) => {
        assertRejectedAttachment(response, 'liite-content-type-mismatch');
      });
    });
  });

  it('rejects an extension mismatch with 400', () => {
    createDraftToimenpide().then(toimenpideId => {
      openToimenpide(toimenpideId);

      uploadAttachment(toimenpideId, {
        contents: Cypress.Buffer.from([0x25, 0x50, 0x44, 0x46, 0x00, 0x00]),
        fileName: 'document.txt',
        mimeType: 'application/pdf'
      }).then(({ response }) => {
        assertRejectedAttachment(response, 'liite-extension-mismatch');
      });
    });
  });

  it('uploads a valid attachment successfully', () => {
    createDraftToimenpide().then(toimenpideId => {
      openToimenpide(toimenpideId);

      uploadAttachment(toimenpideId, { fileName: 'document.pdf' }).then(
        ({ response }) => {
          expect(response.statusCode).to.eq(201);
        }
      );

      cy.contains('document.pdf').should('be.visible');
    });
  });
});
