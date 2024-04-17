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
import { render, screen } from '@testing-library/svelte';
import fetchMock from 'jest-fetch-mock';

import * as R from 'ramda';
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

test('SigningDialog renders correctly when no connection Mpollux', async () => {
  fetchMock.mockIf('https://localhost:53952/version', async req => {
    return {
      status: 500
    };
  });
  render(SigningDialog, {
    energiatodistus: energiatodistus2018(),
    reload: R.identity
  });

  const heading = screen.queryByText(/Allekirjoittaminen/u);

  expect(heading).toBeInTheDocument();
  expect(heading.tagName).toBe('H1');

  const errorText = await screen.findByText(
    /Yhteyden avaus mPolluxiin epäonnistui./u
  );
  expect(errorText).toBeInTheDocument();
});

test('SigningDialog renders correctly when there is connection to Mpollux', async () => {
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

  render(SigningDialog, {
    energiatodistus: energiatodistus2018(),
    reload: R.identity
  });

  const heading = screen.queryByText(/Allekirjoittaminen/u);
  expect(heading).toBeInTheDocument();
  expect(heading.tagName).toBe('H1');

  const errorText = await screen.findByText(
    /Energiatodistuksen tiedot ja yhteys mPolluxiin on tarkastettu./u
  );
  expect(errorText).toBeInTheDocument();

  // TODO: Check that this is a button
  const signButton = screen.getByText('Allekirjoita');
  expect(signButton).toBeInTheDocument();
  expect(signButton).toBeEnabled();
});
