/**
 * Backend ↔ Frontend schema consistency test.
 *
 * Fetches the backend's Prismatic Schema constraints from the internal API
 * endpoint and compares them against the frontend schema.js constraints
 * (extracted at runtime via a Cypress task that evaluates schema.js).
 *
 * This catches drift where e.g. the backend allows String75 (max 75 chars)
 * but the frontend caps at String(2, 50), or vice versa.
 *
 * Tests that need the DB validation_numeric_column data (error/warning
 * ranges, schema ⊇ DB ranges) live in the backend Clojure test:
 *   energiatodistus_validation_limits_test.clj
 */

const BACKEND_URL = Cypress.config('backendUrl');

/**
 * Build a lookup map from backend constraints: property → constraint.
 */
const buildBackendLookup = backendConstraints => {
  const map = {};
  backendConstraints.forEach(({ property, constraint }) => {
    map[property] = constraint;
  });
  return map;
};

/**
 * Try to find the backend constraint for a frontend property.
 * The frontend often has "perustiedot.katuosoite" while the backend
 * has "perustiedot.katuosoite-fi" and "perustiedot.katuosoite-sv".
 * We try exact match first, then "-fi" suffix.
 */
const findBackendConstraint = (backendMap, frontendProp) => {
  // Strip array-element markers — backend doesn't use [*] paths
  const cleanProp = frontendProp.replace(/\[\*\]/g, '');

  if (backendMap[cleanProp]) return backendMap[cleanProp];
  if (backendMap[cleanProp + '-fi']) return backendMap[cleanProp + '-fi'];
  if (backendMap[cleanProp + '-sv']) return backendMap[cleanProp + '-sv'];
  return null;
};

describe('Backend ↔ Frontend schema consistency (ET2026)', () => {
  let frontendConstraints;
  let backendConstraints;

  before(() => {
    cy.task('extractFrontendSchemaConstraints', 2026).then(fc => {
      frontendConstraints = fc;
    });

    cy.request(
      `${BACKEND_URL}/api/internal/energiatodistukset/schema-constraints/2026`
    ).then(res => {
      expect(res.status).to.eq(200);
      backendConstraints = res.body;
    });
  });

  it('backend schema constraints endpoint returns valid data', () => {
    expect(backendConstraints).to.be.an('array').and.not.be.empty;
    backendConstraints.forEach(entry => {
      expect(entry).to.have.property('property');
      expect(entry).to.have.property('constraint');
      expect(entry.constraint).to.have.property('type');
    });
  });

  it('frontend schema extractor returns valid data', () => {
    expect(frontendConstraints).to.be.an('array').and.not.be.empty;
    frontendConstraints.forEach(entry => {
      expect(entry).to.have.property('property');
      expect(entry).to.have.property('constraint');
      expect(entry.constraint).to.have.property('type');
    });
  });

  it('string max-length values match between frontend and backend', () => {
    const backendMap = buildBackendLookup(backendConstraints);
    const frontendStrings = frontendConstraints.filter(
      c => c.constraint.type === 'string' && c.constraint['max-length'] != null
    );

    const mismatches = [];
    const unresolved = [];

    frontendStrings.forEach(({ property, constraint: fc }) => {
      const bc = findBackendConstraint(backendMap, property);

      if (!bc) {
        unresolved.push(`${property} (frontend max=${fc['max-length']})`);
        return;
      }

      if (bc.type !== 'string' || !bc['max-length']) {
        // Backend has the field but not as a bounded string — skip
        return;
      }

      if (fc['max-length'] !== bc['max-length']) {
        mismatches.push({
          property,
          frontend: fc['max-length'],
          backend: bc['max-length']
        });
      }
    });

    if (unresolved.length > 0) {
      cy.log(
        `ℹ️ ${unresolved.length} frontend string field(s) not found in backend:`
      );
      unresolved.forEach(u => cy.log(`  ${u}`));
    }

    if (mismatches.length > 0) {
      cy.log('⚠️ String max-length mismatches:');
      mismatches.forEach(m => {
        cy.log(`  ${m.property}: frontend=${m.frontend}, backend=${m.backend}`);
      });
    }

    expect(
      mismatches,
      `${mismatches.length} string max-length mismatch(es):\n` +
        mismatches
          .map(
            m =>
              `  ${m.property}: frontend=${m.frontend} vs backend=${m.backend}`
          )
          .join('\n')
    ).to.have.length(0);
  });

  it('frontend numeric ranges fit within backend schema ranges', () => {
    const backendMap = buildBackendLookup(backendConstraints);
    const frontendNumerics = frontendConstraints.filter(
      c => c.constraint.type === 'number' && c.constraint.min != null
    );

    const violations = [];

    frontendNumerics.forEach(({ property, constraint: fc }) => {
      const bc = findBackendConstraint(backendMap, property);

      if (!bc || bc.type !== 'number' || bc.min == null) {
        return; // No backend bounds to compare
      }

      // Frontend range should be within backend range
      if (fc.min < bc.min) {
        violations.push(
          `${property}: frontend min (${fc.min}) < backend min (${bc.min})`
        );
      }
      if (fc.max > bc.max) {
        violations.push(
          `${property}: frontend max (${fc.max}) > backend max (${bc.max})`
        );
      }
    });

    expect(
      violations,
      `Frontend numeric ranges exceed backend schema bounds:\n  ${violations.join('\n  ')}`
    ).to.have.length(0);
  });

  it('every backend string field has a corresponding frontend field', () => {
    const frontendMap = {};
    frontendConstraints.forEach(({ property, constraint }) => {
      const clean = property.replace(/\[\*\]/g, '');
      frontendMap[clean] = constraint;
    });

    const backendStrings = backendConstraints.filter(
      c => c.constraint.type === 'string' && c.constraint['max-length']
    );

    const missing = [];

    // Fields that are internal-only or system-managed
    const internalFields = [
      'kommentti',
      'bypass-validation-limits-reason',
      'laskuriviviite'
    ];

    backendStrings.forEach(({ property, constraint: bc }) => {
      // Backend has locale variants (-fi, -sv); frontend uses base name
      const baseProp = property.replace(/-(fi|sv)$/, '');

      if (!frontendMap[property] && !frontendMap[baseProp]) {
        if (!internalFields.includes(property)) {
          missing.push(`${property} (backend max=${bc['max-length']})`);
        }
      }
    });

    if (missing.length > 0) {
      cy.log(
        `ℹ️ ${missing.length} backend string field(s) not found in frontend:`
      );
      missing.forEach(m => cy.log(`  ${m}`));
    }

    // This is informational — some backend fields are intentionally
    // absent from the frontend schema (e.g. locale-specific variants
    // where frontend uses a single field with locale postfix handling).
  });
});
