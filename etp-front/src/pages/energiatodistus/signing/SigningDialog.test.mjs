/**
 * @jest-environment jsdom
 */

import {
  afterEach,
  beforeAll,
  beforeEach,
  expect,
  jest,
  test
} from '@jest/globals';
import { fireEvent, render, screen } from '@testing-library/svelte';
import fetchMock from 'jest-fetch-mock';

import * as R from 'ramda';
import * as Maybe from '@Utility/maybe-utils';

import SigningDialog from './SigningDialog.svelte';
import { energiatodistus2018 } from '@Pages/energiatodistus/empty';
import { setupI18n } from '@Language/i18n';

beforeAll(() => {
  setupI18n();
});

beforeEach(() => {
  fetchMock.enableMocks();
});

afterEach(() => {
  jest.clearAllMocks();
  fetchMock.disableMocks();
});

const mockMpolluxConnectionExists = () => {
  fetchMock.mockIf('https://localhost:53952/version', async req => {
    return {
      status: 200,
      body: JSON.stringify({
        version: 'dummy',
        httpMethods: 'GET, POST',
        contentTypes: 'data, digest',
        signatureTypes: 'signature',
        selectorAvailable: true,
        hashAlgorithms: 'SHA1, SHA256, SHA384, SHA512'
      })
    };
  });
};

const mockMpolluxConnectionDoesNotExist = () => {
  fetchMock.mockIf('https://localhost:53952/version', async req => {
    return {
      status: 500
    };
  });
};

const mockSystemSignApiCallSuccess = () => {
  fetchMock.mockIf(
    /\/api\/private\/energiatodistukset\/2018\/1\/signature\/system\/pdf\/.*$/,
    async req => {
      const lang = req.url.endsWith('/fi') ? 'fi' : 'sv';
      return {
        status: 200,
        body: JSON.stringify(`energiatodistukset/energiatodistus-1-${lang}.pdf`)
      };
    }
  );
};

const mockSystemSignApiCallFailure = () => {
  fetchMock.mockIf(
    '/api/private/energiatodistukset/2018/1/signature/system/pdf/fi',
    async req => {
      return {
        status: 500
      };
    }
  );
};

const assertButtons = async closeDialogFn => {
  // Test that signing button exists
  const signButton = screen.getByRole('button', {
    name: /allekirjoita/i
  });
  expect(signButton).toBeInTheDocument();
  expect(signButton).toBeEnabled();

  // Test that sulje buttton exists and clicking it calls the reload function
  // passed to the component
  const cancelButton = screen.getByRole('button', { name: /Sulje/i });
  expect(cancelButton).toBeInTheDocument();
  await fireEvent.click(cancelButton);
  expect(closeDialogFn.mock.calls).toHaveLength(1);
};

const assertSystemSigninDialogContents = async closeDialogFn => {
  // Mpollux state was not checked as card signing is not selected
  expect(fetchMock.mock.calls).toHaveLength(0);

  const heading = screen.queryByText(/Allekirjoittaminen/u);
  expect(heading).toBeInTheDocument();
  expect(heading.tagName).toBe('H1');

  const systemSigningContent = await screen.findByText(
    /Allekirjoita ilman henkilökorttia/u
  );
  expect(systemSigningContent).toBeInTheDocument();

  await assertButtons(closeDialogFn);
};

const assertCardSigningDialogContents = async closeDialogFn => {
  const heading = screen.queryByText(/Allekirjoittaminen/u);
  expect(heading).toBeInTheDocument();
  expect(heading.tagName).toBe('H1');

  const statusText = await screen.findByText(
    /Energiatodistuksen tiedot ja yhteys mPolluxiin on tarkastettu./u
  );
  expect(statusText).toBeInTheDocument();

  await assertButtons(closeDialogFn);
};

const finnishTodistus = R.compose(
  R.assoc('id', 1),
  R.assocPath(['perustiedot', 'kieli'], Maybe.Some(0))
)(energiatodistus2018());

test('SigningDialog displays error message when default selection is card and there is no connection Mpollux', async () => {
  mockMpolluxConnectionDoesNotExist();

  const closeDialogFn = jest.fn();

  render(SigningDialog, {
    energiatodistus: finnishTodistus,
    reload: closeDialogFn,
    selection: 'card'
  });

  // Mpollux state was checked
  expect(fetchMock.mock.calls).toHaveLength(1);

  const heading = screen.queryByText(/Allekirjoittaminen/u);
  expect(heading).toBeInTheDocument();
  expect(heading.tagName).toBe('H1');

  const errorText = await screen.findByText(
    /Yhteyden avaus mPolluxiin epäonnistui./u
  );
  expect(errorText).toBeInTheDocument();

  // Signing button should not exist when Mpollux connection failed
  const signButton = screen.queryByRole('button', { name: /Allekirjoita/i });
  expect(signButton).not.toBeInTheDocument();

  // Test that sulje buttton exists and clicking it calls the reload function
  // passed to the component
  const cancelButton = screen.getByRole('button', { name: /Sulje/i });
  expect(cancelButton).toBeInTheDocument();
  await fireEvent.click(cancelButton);
  expect(closeDialogFn.mock.calls).toHaveLength(1);
});

test('SigningDialog renders correctly when default selection is card and there is connection to Mpollux', async () => {
  mockMpolluxConnectionExists();
  const closeDialogFn = jest.fn();

  render(SigningDialog, {
    energiatodistus: finnishTodistus,
    reload: closeDialogFn,
    selection: 'card'
  });

  // Mpollux state was checked
  expect(fetchMock.mock.calls).toHaveLength(1);

  await assertCardSigningDialogContents(closeDialogFn);
});

test('SigningDialog renders correctly when default selection is system and there is connection to Mpollux', async () => {
  mockMpolluxConnectionExists();

  const closeDialogFn = jest.fn();

  render(SigningDialog, {
    energiatodistus: finnishTodistus,
    reload: closeDialogFn,
    selection: 'system'
  });

  await assertSystemSigninDialogContents(closeDialogFn);
});

test('Signing method can be selected in SigningDialog when allowSelection is true', async () => {
  mockMpolluxConnectionExists();
  const closeDialogFn = jest.fn();

  // Render the dialog with card as the default selection
  render(SigningDialog, {
    energiatodistus: finnishTodistus,
    reload: closeDialogFn,
    selection: 'card',
    allowSelection: true
  });

  // Mpollux state was checked
  expect(fetchMock.mock.calls).toHaveLength(1);

  // Initial state of the view is as expected for card signing method
  await assertCardSigningDialogContents(closeDialogFn);

  // Reset mock calls so that they don't affect checks after switching the signing method
  fetchMock.resetMocks();
  closeDialogFn.mockReset();

  // Select system signing
  const selection = screen.getByText('Älä käytä henkilökorttia');
  expect(selection).toBeInTheDocument();
  await fireEvent.click(selection);

  await assertSystemSigninDialogContents(closeDialogFn);
});

test('Signing method can not be selected when allowSelection is false', async () => {
  const closeDialogFn = jest.fn();

  // Render the dialog with card as the default selection
  render(SigningDialog, {
    energiatodistus: finnishTodistus,
    reload: closeDialogFn,
    selection: 'card',
    allowSelection: false
  });

  // Options should not be available
  const selection = screen.queryByText('Älä käytä korttia');
  expect(selection).not.toBeInTheDocument();
});

test('Pressing sign button with system as signing method shows loading indicator', async () => {
  render(SigningDialog, {
    energiatodistus: finnishTodistus,
    reload: R.identity,
    selection: 'system'
  });

  const signButton = screen.getByRole('button', { name: /Allekirjoita/i });

  await fireEvent.click(signButton);

  const spinner = screen.getByTestId('spinner');

  expect(spinner).toBeInTheDocument();
});

test('When system sign fails, error is shown', async () => {
  // Mock the signing api call to return an error
  mockSystemSignApiCallFailure();

  render(SigningDialog, {
    energiatodistus: finnishTodistus,
    reload: R.identity,
    selection: 'system'
  });

  const signButton = screen.getByRole('button', { name: /Allekirjoita/i });

  await fireEvent.click(signButton);

  const spinner = screen.getByTestId('spinner');

  expect(spinner).toBeInTheDocument();

  const errorText = await screen.findByText(
    /Allekirjoittaminen keskeytyi tuntemattomasta syystä./u
  );
  expect(errorText).toBeInTheDocument();

  // Spinner has disappeared when request finished
  expect(spinner).not.toBeInTheDocument();
});

test('When system sign of energiatodistus in Finnish succeeds, success message and link to the pdf is shown', async () => {
  mockSystemSignApiCallSuccess();

  render(SigningDialog, {
    energiatodistus: finnishTodistus,
    reload: R.identity,
    selection: 'system'
  });

  const signButton = screen.getByRole('button', { name: /Allekirjoita/i });

  await fireEvent.click(signButton);

  const spinner = screen.getByTestId('spinner');

  expect(spinner).toBeInTheDocument();

  const statusText = await screen.findByText(
    /Suomenkielinen energiatodistus on allekirjoitettu onnistuneesti./u
  );

  expect(statusText).toBeInTheDocument();

  // Spinner has disappeared when request finished
  expect(spinner).not.toBeInTheDocument();

  // Download link for signed pdf exists
  const todistusLink = screen.getByTestId('energiatodistus-1-fi.pdf');
  expect(todistusLink).toBeInTheDocument();
  expect(todistusLink.getAttribute('href')).toBe(
    '/api/private/energiatodistukset/2018/1/pdf/fi/energiatodistus-1-fi.pdf'
  );
});

test('When system sign of energiatodistus in Swedish succeeds, success message and link to the pdf is shown', async () => {
  mockSystemSignApiCallSuccess();

  const todistus = R.compose(
    R.assoc('id', 1),
    R.assocPath(['perustiedot', 'kieli'], Maybe.Some(1))
  )(energiatodistus2018());

  render(SigningDialog, {
    energiatodistus: todistus,
    reload: R.identity,
    selection: 'system'
  });

  const signButton = screen.getByRole('button', { name: /Allekirjoita/i });

  await fireEvent.click(signButton);

  const spinner = screen.getByTestId('spinner');

  expect(spinner).toBeInTheDocument();

  const statusText = await screen.findByText(
    /Ruotsinkielinen energiatodistus on allekirjoitettu onnistuneesti./u
  );
  expect(statusText).toBeInTheDocument();

  // Spinner has disappeared when request finished
  expect(spinner).not.toBeInTheDocument();

  // Download link for signed pdf exists
  const todistusLink = screen.getByTestId('energiatodistus-1-sv.pdf');
  expect(todistusLink).toBeInTheDocument();
  expect(todistusLink.getAttribute('href')).toBe(
    '/api/private/energiatodistukset/2018/1/pdf/sv/energiatodistus-1-sv.pdf'
  );
});

test('When system signing of bilingual energiatodistus succeeds, success message and links to both pdfs are shown', async () => {
  mockSystemSignApiCallSuccess();

  const todistus = R.compose(
    R.assoc('id', 1),
    R.assocPath(['perustiedot', 'kieli'], Maybe.Some(2))
  )(energiatodistus2018());

  render(SigningDialog, {
    energiatodistus: todistus,
    reload: R.identity,
    selection: 'system'
  });

  const signButton = screen.getByRole('button', { name: /Allekirjoita/i });

  await fireEvent.click(signButton);

  const spinner = screen.getByTestId('spinner');

  expect(spinner).toBeInTheDocument();

  const statusText = await screen.findByText(
    /Kaksikielinen energiatodistus on allekirjoitettu onnistuneesti./u
  );
  expect(statusText).toBeInTheDocument();

  expect(fetchMock.mock.calls.length).toBe(2);

  // Spinner has disappeared when request finished
  expect(spinner).not.toBeInTheDocument();

  // Download link for signed Finnish pdf exists
  const todistusLinkFi = screen.getByTestId('energiatodistus-1-fi.pdf');
  expect(todistusLinkFi).toBeInTheDocument();
  expect(todistusLinkFi.getAttribute('href')).toBe(
    '/api/private/energiatodistukset/2018/1/pdf/fi/energiatodistus-1-fi.pdf'
  );

  // Download link for signed Swedish pdf exists
  const todistusLinkSv = screen.getByTestId('energiatodistus-1-sv.pdf');
  expect(todistusLinkSv).toBeInTheDocument();
  expect(todistusLinkSv.getAttribute('href')).toBe(
    '/api/private/energiatodistukset/2018/1/pdf/sv/energiatodistus-1-sv.pdf'
  );
});

test('Signing button is not visible after signing using system signing', async () => {
  mockSystemSignApiCallSuccess();

  render(SigningDialog, {
    energiatodistus: finnishTodistus,
    reload: R.identity,
    selection: 'system'
  });

  const signButton = screen.getByRole('button', { name: /Allekirjoita/i });

  await fireEvent.click(signButton);

  const statusText = await screen.findByText(
    /Suomenkielinen energiatodistus on allekirjoitettu onnistuneesti./u
  );
  expect(statusText).toBeInTheDocument();

  expect(signButton).not.toBeInTheDocument();
});
