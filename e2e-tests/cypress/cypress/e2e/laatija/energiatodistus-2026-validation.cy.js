import { FIXTURES } from '../../fixtures/laatija';
import { isEtp2026Enabled } from '../../support/featureFlags';

// This spec generates hundreds of it() blocks from CSV field definitions.
// Prevent Electron OOM by not keeping DOM snapshots of completed tests.
Cypress.config('numTestsKeptInMemory', 0);

/**
 * ET2026 form validation tests.
 *
 * Field definitions (dataCy, type, limits, validValue, …) live in the
 * companion CSV file  `energiatodistus-2026-fields.csv`  so that adding
 * or changing a field only requires editing one row in a spreadsheet-
 * friendly format.
 *
 * CSV columns:
 *   dbColumn, dataCy, type, required, validValue,
 *   warningMin, warningMax, errorMin, errorMax, testedInE2e, section,
 *   description
 *
 * The CSV is loaded at config time (see cypress.config.js) via
 * `Cypress.env('ET2026_FIELDS_CSV')` so that we can generate per-field
 * describe/it blocks synchronously.
 */

// ---------------------------------------------------------------------------
// CSV → field-object parser
// ---------------------------------------------------------------------------

const parseCsv = csv => {
  const lines = csv.trim().split('\n');
  const headers = lines[0].split(',');

  return lines.slice(1).map(line => {
    const cols = line.split(',');
    const row = {};
    headers.forEach((h, i) => (row[h] = cols[i]));

    const field = {
      dbColumn: row.dbColumn,
      dataCy: row.dataCy,
      type: row.type,
      required: row.required === 'true',
      validValue: row.validValue,
      testedInE2e: row.testedInE2e === 'true',
      section: row.section,
      description: row.description,
      maxLength: row.maxLength !== '' ? Number(row.maxLength) : null
    };

    const warnMin = row.warningMin !== '' ? Number(row.warningMin) : null;
    const warnMax = row.warningMax !== '' ? Number(row.warningMax) : null;
    const errMin = row.errorMin !== '' ? Number(row.errorMin) : null;
    const errMax = row.errorMax !== '' ? Number(row.errorMax) : null;

    if (errMin !== null && errMax !== null) {
      field.limits = { error: { min: errMin, max: errMax } };
    }
    if (warnMin !== null && warnMax !== null) {
      field.limits = {
        ...field.limits,
        warning: { min: warnMin, max: warnMax }
      };
    }

    return field;
  });
};

// ---------------------------------------------------------------------------
// Parse fields synchronously from env (loaded in cypress.config.js)
// ---------------------------------------------------------------------------

const csvString = Cypress.env('ET2026_FIELDS_CSV') || '';
const ALL_ROWS = csvString ? parseCsv(csvString) : [];
const ALL_FIELDS = ALL_ROWS.filter(f => f.testedInE2e);

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

const fillInput = (dataCy, value) => {
  cy.get(`[data-cy="${dataCy}"]`).clear().type(value).blur();
};

const clearInput = dataCy => {
  cy.get(`[data-cy="${dataCy}"]`).clear().blur();
};

const selectById = (id, optionToSelect) => {
  cy.get(`[id="${id}"]`)
    .click()
    .parent()
    .within(() => {
      cy.contains(optionToSelect).click();
    });
};

const fillField = field => {
  switch (field.type) {
    case 'input':
      fillInput(field.dataCy, field.validValue);
      break;
    case 'select':
      selectById(field.dataCy, field.validValue);
      break;
    case 'textarea':
      cy.get(`[data-cy="${field.dataCy}"]`)
        .clear()
        .type(field.validValue)
        .blur();
      break;
    case 'checkbox':
      cy.get(`[data-cy="${field.dataCy}"]`).check({ force: true });
      break;
  }
};

const assertValidationVisible = (dataCy, expectedText) => {
  const selector = `[data-cy="validation-label-for=${dataCy}"]`;
  if (Array.isArray(expectedText)) {
    cy.get(selector)
      .should('be.visible')
      .invoke('text')
      .should(text => {
        const match = expectedText.some(t => text.includes(t));
        expect(
          match,
          `Expected label to contain one of ${JSON.stringify(expectedText)}, but was "${text}"`
        ).to.be.true;
      });
  } else {
    cy.get(selector).should('be.visible').and('contain.text', expectedText);
  }
};

const assertNoValidation = dataCy => {
  cy.get(`[data-cy="validation-label-for=${dataCy}"]`).should('not.exist');
};

const clickSave = () => cy.get('[data-cy="save-button"]').click();

const clickAllekirjoita = () =>
  cy.get('[data-cy="allekirjoita-button"]').click();

const assertSaveSucceeds = () =>
  cy.get('[data-cy="form-alert-text"]').should('not.exist');

// ---------------------------------------------------------------------------
// Tests
// ---------------------------------------------------------------------------

const runTests = () => {
  describe('ET2026 form validation', { testIsolation: false }, () => {
    let etUrl;

    beforeEach(() => {
      cy.intercept(/\/api\/private/, req => {
        req.headers = { ...req.headers, ...FIXTURES.headers };
      });
    });

    before(() => {
      cy.intercept(/\/api\/private/, req => {
        req.headers = { ...req.headers, ...FIXTURES.headers };
      });

      // Create a single ET2026 draft that all tests reuse
      cy.visit('/#/energiatodistus/all');
      cy.get('[data-cy="create-new-et26"]').click();
      clickSave();
      cy.location('hash').should('match', /\/energiatodistus\/2026\/\d+/);
      cy.location('hash').then(hash => {
        etUrl = '/' + hash;
      });
      cy.get('[data-cy="save-button"]').should('be.visible');
    });

    // -------------------------------------------------------------------
    // Per-field tests — flat it() blocks so the page stays loaded.
    // -------------------------------------------------------------------

    ALL_FIELDS.forEach(field => {
      const label = `[${field.dataCy}] ${field.description || ''}`.trim();
      const hasErrorLimits = field.type === 'input' && field.limits?.error;
      const hasWarningLimits = field.type === 'input' && field.limits?.warning;
      const isRequired = field.required;

      if (hasErrorLimits) {
        it(`${label} — rejects values above error max`, () => {
          const overMax = field.limits.error.max + 1;
          fillInput(field.dataCy, String(overMax));
          assertValidationVisible(field.dataCy, `${field.limits.error.max}`);
          clearInput(field.dataCy);
        });

        if (field.limits.error.min > -9999999999) {
          it(`${label} — rejects values below error min`, () => {
            const underMin = field.limits.error.min - 1;
            fillInput(field.dataCy, String(underMin));
            const expectedTexts =
              underMin < 0
                ? [`${field.limits.error.min}`, 'Virheellinen']
                : `${field.limits.error.min}`;
            assertValidationVisible(field.dataCy, expectedTexts);
            clearInput(field.dataCy);
          });
        }

        it(`${label} — accepts a valid value without errors`, () => {
          fillInput(field.dataCy, field.validValue);
          assertNoValidation(field.dataCy);
          clearInput(field.dataCy);
        });
      }

      if (hasWarningLimits) {
        const { warning, error } = field.limits;
        if (warning.max < error.max) {
          it(`${label} — shows warning above warning max`, () => {
            const aboveWarning = +((warning.max + error.max) / 2).toFixed(4);
            fillInput(field.dataCy, String(aboveWarning));
            assertValidationVisible(field.dataCy, `${warning.max}`);
            clearInput(field.dataCy);
          });
        }

        if (warning.min > error.min) {
          it(`${label} — shows warning below warning min`, () => {
            const belowWarning = +((error.min + warning.min) / 2).toFixed(4);
            fillInput(field.dataCy, String(belowWarning));
            assertValidationVisible(field.dataCy, `${warning.min}`);
            clearInput(field.dataCy);
          });
        }
      }

      if (field.maxLength) {
        it(`${label} — rejects text exceeding max length (${field.maxLength})`, () => {
          const tooLong = 'a'.repeat(field.maxLength + 1);
          const selector = `[data-cy="${field.dataCy}"]`;
          // Set value directly (like a paste) — much faster than cy.type()
          // for long strings. Trigger 'input' so Svelte picks up the change.
          cy.get(selector)
            .clear()
            .invoke('val', tooLong)
            .trigger('input')
            .blur();
          assertValidationVisible(field.dataCy, `${field.maxLength}`);
          cy.get(selector).clear().blur();
        });
      }

      if (isRequired) {
        it(`${label} — is listed as missing when allekirjoita is clicked on empty form`, () => {
          clickAllekirjoita();

          cy.get('[data-cy="form-alert-text"]')
            .should('be.visible')
            .and('contain.text', 'Pakolliset tiedot puuttuvat:');

          // Dismiss the alert so it doesn't cover subsequent tests.
          cy.get('[data-cy="form-alert-text"]').parent().find('.close').click();
        });
      }
    });

    // -------------------------------------------------------------------
    // Structural / integration tests
    // -------------------------------------------------------------------

    it('invalid value blocks save and appears in error list', () => {
      // Reload to clear any leftover validation state.
      cy.visit(etUrl);
      cy.get('[data-cy="save-button"]').should('be.visible');

      fillInput('lahtotiedot.rakennusvaippa.ilmanvuotoluku', '51');

      clickSave();
      cy.get('[data-cy="form-alert-text"]').should(
        'contain.text',
        'Ilmanvuotoluku'
      );

      clearInput('lahtotiedot.rakennusvaippa.ilmanvuotoluku');
      clickSave();
      assertSaveSucceeds();
    });

    it('havainnointikaynti fields appear for "Olemassa oleva rakennus" and hide for "Rakennuslupa"', () => {
      selectById('perustiedot.laatimisvaihe', 'Olemassa oleva rakennus');
      cy.get('[data-cy="perustiedot.havainnointikaynti"]').should('exist');
      cy.get('[id="perustiedot.havainnointikayntityyppi-id"]').should('exist');

      selectById('perustiedot.laatimisvaihe', 'Rakennuslupa');
      cy.get('[data-cy="perustiedot.havainnointikaynti"]').should('not.exist');
      cy.get('[id="perustiedot.havainnointikayntityyppi-id"]').should(
        'not.exist'
      );

      selectById('perustiedot.laatimisvaihe', 'Olemassa oleva rakennus');
    });

    // -------------------------------------------------------------------
    // Signing test — fills all fields, saves, and signs (must be last)
    // -------------------------------------------------------------------

    it('fills all fields, saves, and signs the energiatodistus', () => {
      // Reload for a clean form.
      cy.visit(etUrl);
      cy.get('[data-cy="save-button"]').should('be.visible');

      cy.intercept(
        {
          method: 'PUT',
          pathname: /\/api\/private\/energiatodistukset\/2026\/*/
        },
        req => {
          req.headers = { ...req.headers, ...FIXTURES.headers };
        }
      ).as('save');

      ALL_FIELDS.forEach(fillField);

      // Laskutustiedot (billing info) must be set before signing.
      cy.selectInSelect('laskutusosoite-id', 'Henkilökohtaiset tiedot');

      clickSave();
      cy.wait('@save');
      assertSaveSucceeds();

      // Click Allekirjoita — this saves again (saveComplete) then opens
      // the signing dialog.
      clickAllekirjoita();

      cy.intercept(
        {
          method: 'POST',
          pathname:
            /\/api\/private\/energiatodistukset\/2026\/\d+\/signature\/system-sign/
        },
        req => {
          req.headers = { ...req.headers, ...FIXTURES.headers };
        }
      ).as('system-sign');

      // Click the "Start signing" button.
      cy.get('[data-cy="signing-pre-submit-button"]').click();

      // Confirm signing in the confirmation step.
      cy.get('[data-cy="signing-submit-button"]').click();

      cy.wait('@system-sign');

      // After signing, a link to the signed PDF should be visible and
      // downloadable.
      cy.get('[data-cy^="energiatodistus-"][data-cy$="-fi.pdf"]', {
        timeout: 30000
      })
        .should('be.visible')
        .should('have.attr', 'href')
        .then(href => cy.request({ url: href, headers: FIXTURES.headers }))
        .then(res => {
          expect(res.status).to.eq(200);
        });
    });
  });
};

(isEtp2026Enabled() ? runTests : describe.skip)(
  'ET2026 validation (skipped — feature flag off)',
  () => {}
);
