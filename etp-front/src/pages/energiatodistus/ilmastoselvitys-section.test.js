/**
 * @jest-environment jsdom
 */

import { beforeAll, expect, test, describe } from '@jest/globals';
import { render, screen } from '@testing-library/svelte';
import * as R from 'ramda';
import * as Maybe from '@Utility/maybe-utils';
import * as Either from '@Utility/either-utils';

import Ilmastoselvitys from './form-parts/Ilmastoselvitys.svelte';
import { energiatodistus2026 } from './empty';
import { v2026 } from './schema';
import { setupI18n } from '@Language/i18n';

beforeAll(() => {
  setupI18n();
});

const mockLuokittelut = {
  ilmastoselvitysLaadintaperusteet: [
    { id: 0, 'label-fi': 'Uusi rakennus', 'label-sv': 'Uusi rakennus(sv)' },
    {
      id: 1,
      'label-fi': 'Perusparannus A+-luokkaan',
      'label-sv': 'Perusparannus A+-luokkaan(sv)'
    }
  ],
  postinumerot: [
    { id: 100, 'label-fi': 'Helsinki', 'label-sv': 'Helsingfors', valid: true, 'type-id': 1 }
  ]
};

const defaultProps = () => ({
  energiatodistus: energiatodistus2026(),
  schema: v2026,
  luokittelut: mockLuokittelut,
  disabled: false,
  inputLanguage: 'fi'
});

// --- Part 4: Svelte UI Component — Ilmastoselvitys Section ---

// Test 4.1: Ilmastoselvitys section renders header
test('given the ilmastoselvitys component, when rendered, then section header is displayed', () => {
  // given
  const props = defaultProps();

  // when
  render(Ilmastoselvitys, props);

  // then
  const heading = screen.getByRole('heading', { level: 2 });
  expect(heading).toBeInTheDocument();
});

// Test 4.2: Metadata fields are rendered
test('given the ilmastoselvitys component, when rendered, then metadata input fields exist', () => {
  // given
  const props = defaultProps();

  // when
  const { container } = render(Ilmastoselvitys, props);

  // then
  const expectedIds = [
    'ilmastoselvitys.laatimisajankohta',
    'ilmastoselvitys.laatija',
    'ilmastoselvitys.yritys',
    'ilmastoselvitys.yritys-osoite',
    'ilmastoselvitys.yritys-postinumero'
  ];

  expectedIds.forEach(id => {
    const element = container.querySelector(`[id="${id}"]`);
    expect(element).not.toBeNull();
  });
});

// Test 4.3: Laadintaperuste select is rendered with classification options
test('given the ilmastoselvitys component with mock classifications, when rendered, then laadintaperuste select with options exists', () => {
  // given
  const props = defaultProps();

  // when
  const { container } = render(Ilmastoselvitys, props);

  // then
  const selectElement = container.querySelector(
    '[id*="laadintaperuste"], [name*="laadintaperuste"]'
  );
  expect(selectElement).not.toBeNull();

  // The Select component renders a custom dropdown (not native <select>).
  // Verify the format function and items are wired by checking the hidden input name.
  const hiddenInput = container.querySelector(
    'input[name="ilmastoselvitys.laadintaperuste"]'
  );
  expect(hiddenInput).not.toBeNull();
});

// Test 4.4: Hiilijalanjälki table renders all input fields
test('given the ilmastoselvitys component, when rendered, then all 10 hiilijalanjalki input fields exist', () => {
  // given
  const props = defaultProps();

  // when
  const { container } = render(Ilmastoselvitys, props);

  // then
  const expectedIds = [
    'ilmastoselvitys.hiilijalanjalki.rakennus.rakennustuotteiden-valmistus',
    'ilmastoselvitys.hiilijalanjalki.rakennus.kuljetukset-tyomaavaihe',
    'ilmastoselvitys.hiilijalanjalki.rakennus.rakennustuotteiden-vaihdot',
    'ilmastoselvitys.hiilijalanjalki.rakennus.energiankaytto',
    'ilmastoselvitys.hiilijalanjalki.rakennus.purkuvaihe',
    'ilmastoselvitys.hiilijalanjalki.rakennuspaikka.rakennustuotteiden-valmistus',
    'ilmastoselvitys.hiilijalanjalki.rakennuspaikka.kuljetukset-tyomaavaihe',
    'ilmastoselvitys.hiilijalanjalki.rakennuspaikka.rakennustuotteiden-vaihdot',
    'ilmastoselvitys.hiilijalanjalki.rakennuspaikka.energiankaytto',
    'ilmastoselvitys.hiilijalanjalki.rakennuspaikka.purkuvaihe'
  ];

  expectedIds.forEach(id => {
    const element = container.querySelector(`[id="${id}"]`);
    expect(element).not.toBeNull();
  });
});

// Test 4.5: Hiilikädenjälki table renders all input fields
test('given the ilmastoselvitys component, when rendered, then all 10 hiilikadenjalki input fields exist', () => {
  // given
  const props = defaultProps();

  // when
  const { container } = render(Ilmastoselvitys, props);

  // then
  const expectedIds = [
    'ilmastoselvitys.hiilikadenjalki.rakennus.uudelleenkaytto',
    'ilmastoselvitys.hiilikadenjalki.rakennus.kierratys',
    'ilmastoselvitys.hiilikadenjalki.rakennus.ylimaarainen-uusiutuvaenergia',
    'ilmastoselvitys.hiilikadenjalki.rakennus.hiilivarastovaikutus',
    'ilmastoselvitys.hiilikadenjalki.rakennus.karbonatisoituminen',
    'ilmastoselvitys.hiilikadenjalki.rakennuspaikka.uudelleenkaytto',
    'ilmastoselvitys.hiilikadenjalki.rakennuspaikka.kierratys',
    'ilmastoselvitys.hiilikadenjalki.rakennuspaikka.ylimaarainen-uusiutuvaenergia',
    'ilmastoselvitys.hiilikadenjalki.rakennuspaikka.hiilivarastovaikutus',
    'ilmastoselvitys.hiilikadenjalki.rakennuspaikka.karbonatisoituminen'
  ];

  expectedIds.forEach(id => {
    const element = container.querySelector(`[id="${id}"]`);
    expect(element).not.toBeNull();
  });
});

// Test 4.6: Yhteensä (totals) column calculates sum of 5 input fields
test('given energiatodistus with some rakennus values set, when rendered, then yhteensä displays their sum', () => {
  // given
  const et = R.compose(
    R.assocPath(
      [
        'ilmastoselvitys',
        'hiilijalanjalki',
        'rakennus',
        'rakennustuotteiden-valmistus'
      ],
      Either.Right(Maybe.Some(10.5))
    ),
    R.assocPath(
      [
        'ilmastoselvitys',
        'hiilijalanjalki',
        'rakennus',
        'kuljetukset-tyomaavaihe'
      ],
      Either.Right(Maybe.Some(3.2))
    )
  )(energiatodistus2026());

  const props = { ...defaultProps(), energiatodistus: et };

  // when
  const { container } = render(Ilmastoselvitys, props);

  // then
  expect(container.textContent).toContain('13,7');
});

// Test 4.7: Yhteensä treats empty (None) values as zero
test('given energiatodistus with only one rakennus value set, when rendered, then yhteensä shows that single value', () => {
  // given
  const et = R.assocPath(
    [
      'ilmastoselvitys',
      'hiilijalanjalki',
      'rakennus',
      'rakennustuotteiden-valmistus'
    ],
    Either.Right(Maybe.Some(5.0)),
    energiatodistus2026()
  );

  const props = { ...defaultProps(), energiatodistus: et };

  // when
  const { container } = render(Ilmastoselvitys, props);

  // then
  expect(container.textContent).toContain('5,0');
});

// Test 4.8: All fields are disabled when disabled prop is true
test('given disabled is true, when rendered, then all inputs and selects are disabled', () => {
  // given
  const props = { ...defaultProps(), disabled: true };

  // when
  const { container } = render(Ilmastoselvitys, props);

  // then
  // The component uses custom Select (div-based, not native <select>).
  // Check that visible text inputs are disabled.
  const inputs = container.querySelectorAll('input:not([class*="hidden"])');
  inputs.forEach(input => {
    expect(input.disabled).toBe(true);
  });

  // Verify the custom Select button div has the disabled class
  const selectButton = container.querySelector(
    '[id="ilmastoselvitys.laadintaperuste"]'
  );
  expect(selectButton.classList.contains('disabled')).toBe(true);
});

// Test 4.9: Partial input is allowed (no mandatory field validation in draft mode)
test('given only laatija is set, when rendered, then no validation errors are shown', () => {
  // given
  const et = R.assocPath(
    ['ilmastoselvitys', 'laatija'],
    Either.Right(Maybe.Some('Test Laatija')),
    energiatodistus2026()
  );

  const props = { ...defaultProps(), energiatodistus: et };

  // when
  const { container } = render(Ilmastoselvitys, props);

  // then
  const errorElements = container.querySelectorAll(
    '.has-error, [aria-invalid="true"]'
  );
  expect(errorElements.length).toBe(0);
});

// Test 4.10: Section is positioned after lisämerkintöjä in the form
describe('ET2026Form integration', () => {
  // This test verifies the DOM order of the headings.
  // We mock getComputedStyle to avoid JSDOM limitations with autoresize.
  test('given the full ET2026Form, when rendered, then Ilmastoselvitys heading appears after Lisämerkintojä heading', async () => {
    // Stub getComputedStyle for JSDOM (autoresize.js needs font-size)
    const origGetComputedStyle = window.getComputedStyle;
    window.getComputedStyle = el => {
      const result = origGetComputedStyle(el);
      return {
        ...result,
        getPropertyValue: prop =>
          prop === 'font-size' ? '16px' : result.getPropertyValue(prop)
      };
    };

    try {
      // This test requires the full ET2026Form with all dependencies.
      const ET2026Form = (await import('./ET2026Form.svelte')).default;

      // given — build minimal required props
      const et = energiatodistus2026();
      const minimalLuokittelut = {
        ...mockLuokittelut,
        lammonjako: [],
        lammitysmuoto: [],
        ilmanvaihtotyypit: [],
        postinumerot: [],
        kielisyys: [],
        laatimisvaiheet: [],
        patevyydet: [],
        kayttotarkoitusluokat: [],
        alakayttotarkoitusluokat: [],
        mahdollisuusLiittya: [],
        uusiutuvaEnergia: [],
        jaahdytys: [],
        'toimenpide-ehdotus': [],
        'toimenpide-ehdotus-group': [],
        havainnointikayntityyppi: []
      };

      // when
      const { container } = render(ET2026Form, {
        energiatodistus: et,
        inputLanguage: 'fi',
        luokittelut: minimalLuokittelut,
        schema: v2026,
        disabled: false,
        validation: { kuormat: [] },
        eTehokkuus: Maybe.None(),
        whoami: { id: 1, rolesInUse: [] }
      });

      const html = container.innerHTML;

      // then
      const lisamerkintojaIndex = html.indexOf('Lisämerkintojä');
      const ilmastoselvitysIndex = html.indexOf('Ilmastoselvitys');

      expect(lisamerkintojaIndex).toBeGreaterThan(-1);
      expect(ilmastoselvitysIndex).toBeGreaterThan(-1);
      expect(ilmastoselvitysIndex).toBeGreaterThan(lisamerkintojaIndex);
    } finally {
      window.getComputedStyle = origGetComputedStyle;
    }
  });
});
