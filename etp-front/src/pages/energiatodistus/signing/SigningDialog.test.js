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

const systemSignSuccessResponse = {
  status: 200,
  body: JSON.stringify(`Ok`)
};

const systemSignFailureResponse = {
  status: 500
};

const systemSignUrl =
  '/api/private/energiatodistukset/2018/1/signature/system-sign';

const validateSessionUrl = '/api/private/energiatodistukset/validate-session';
const signingAllowedResponse = allowed => {
  return {
    status: 200,
    body: JSON.stringify({
      'signing-allowed': allowed
    })
  };
};

const setupFetchMocks = mocks => {
  fetchMock.mockIf(/.*/, async req => {
    return mocks[req.url];
  });
};

const assertButtons = async closeDialogFn => {
  // Test that signing button exists
  const signButton = await screen.findByRole('button', {
    name: /allekirjoita/i
  });
  expect(signButton).toBeInTheDocument();
  expect(signButton).toBeEnabled();

  // Test that sulje buttton exists and clicking it calls the reload function
  // passed to the component
  const closeButton = await screen.findByRole('button', { name: /Sulje/i });
  expect(closeButton).toBeInTheDocument();
  await fireEvent.click(closeButton);
  expect(closeDialogFn.mock.calls).toHaveLength(1);
};

const assertSystemSigninDialogContents = async closeDialogFn => {
  const heading = screen.getByRole('heading', { name: /Allekirjoittaminen/u });
  expect(heading).toBeInTheDocument();
  expect(heading.tagName).toBe('H1');

  await assertButtons(closeDialogFn);
};

const assertInProgress = async () => {
  const spinner = screen.getByTestId('spinner');
  expect(spinner).toBeInTheDocument();
  const statusText = await screen.queryByText(
    'Energiatodistuksen allekirjoittaminen käynnissä'
  );
  expect(statusText).toBeInTheDocument();
};

const assertNotInProgress = async () => {
  const spinner = screen.queryByTestId('spinner');
  expect(spinner).not.toBeInTheDocument();
  const statusText = await screen.queryByText(
    'Energiatodistuksen allekirjoittaminen käynnissä'
  );
  expect(statusText).not.toBeInTheDocument();
};

const assertInstructionsTextIsVisible = () => {
  const infoText = screen.getByTestId('signing-instructions');
  expect(infoText).toBeInTheDocument();
};

const assertInstructionsTextIsNotVisible = () => {
  const infoText = screen.queryByTestId('signing-instructions');
  expect(infoText).not.toBeInTheDocument();
};

const assertSigningMethodSelectionIsNotVisible = () => {
  const options = screen.queryAllByRole('radio');
  expect(options).toHaveLength(0);
};

const finnishTodistus = R.compose(
  R.assoc('id', 1),
  R.assocPath(['perustiedot', 'kieli'], Maybe.Some(0))
)(energiatodistus2018());

test('When system sign fails, error is shown', async () => {
  // Mock the signing api call to return an error
  setupFetchMocks({
    [validateSessionUrl]: signingAllowedResponse(true),
    [systemSignUrl]: systemSignFailureResponse
  });

  render(SigningDialog, {
    energiatodistus: finnishTodistus,
    reload: R.identity,
    selection: 'system'
  });

  const signButton = await screen.findByRole('button', {
    name: /Allekirjoita/i
  });

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
  setupFetchMocks({
    [validateSessionUrl]: signingAllowedResponse(true),
    [systemSignUrl]: systemSignSuccessResponse
  });

  render(SigningDialog, {
    energiatodistus: finnishTodistus,
    reload: R.identity,
    selection: 'system'
  });

  // Before signing
  assertInstructionsTextIsVisible();
  await assertNotInProgress();

  const signButton = await screen.findByRole('button', {
    name: /Allekirjoita/i
  });
  await fireEvent.click(signButton);

  // During signing
  assertInstructionsTextIsNotVisible();
  await assertInProgress();

  // After signing
  const statusText = await screen.findByText(
    /Suomenkielinen energiatodistus on allekirjoitettu onnistuneesti./u
  );
  expect(statusText).toBeInTheDocument();

  expect(fetchMock.mock.calls.length).toBe(2);

  assertInstructionsTextIsNotVisible();
  await assertNotInProgress();

  // Download link for signed pdf exists
  const todistusLink = screen.getByTestId('energiatodistus-1-fi.pdf');
  expect(todistusLink).toBeInTheDocument();
  expect(todistusLink.getAttribute('href')).toBe(
    '/api/private/energiatodistukset/2018/1/pdf/fi/energiatodistus-1-fi.pdf'
  );
});

test('When system sign of energiatodistus in Swedish succeeds, success message and link to the pdf is shown', async () => {
  setupFetchMocks({
    [validateSessionUrl]: signingAllowedResponse(true),
    [systemSignUrl]: systemSignSuccessResponse
  });

  const todistus = R.compose(
    R.assoc('id', 1),
    R.assocPath(['perustiedot', 'kieli'], Maybe.Some(1))
  )(energiatodistus2018());

  render(SigningDialog, {
    energiatodistus: todistus,
    reload: R.identity,
    selection: 'system'
  });

  // Before signing
  assertInstructionsTextIsVisible();
  await assertNotInProgress();

  const signButton = await screen.findByRole('button', {
    name: /Allekirjoita/i
  });
  await fireEvent.click(signButton);

  // During signing
  assertInstructionsTextIsNotVisible();
  await assertInProgress();

  // After signing
  const statusText = await screen.findByText(
    /Ruotsinkielinen energiatodistus on allekirjoitettu onnistuneesti./u
  );
  expect(statusText).toBeInTheDocument();

  expect(fetchMock.mock.calls.length).toBe(2);

  assertInstructionsTextIsNotVisible();
  await assertNotInProgress();

  // Download link for signed pdf exists
  const todistusLink = screen.getByTestId('energiatodistus-1-sv.pdf');
  expect(todistusLink).toBeInTheDocument();
  expect(todistusLink.getAttribute('href')).toBe(
    '/api/private/energiatodistukset/2018/1/pdf/sv/energiatodistus-1-sv.pdf'
  );
});

test('When system signing of bilingual energiatodistus succeeds, success message and links to both pdfs are shown', async () => {
  setupFetchMocks({
    [validateSessionUrl]: signingAllowedResponse(true),
    [systemSignUrl]: systemSignSuccessResponse
  });

  const todistus = R.compose(
    R.assoc('id', 1),
    R.assocPath(['perustiedot', 'kieli'], Maybe.Some(2))
  )(energiatodistus2018());

  render(SigningDialog, {
    energiatodistus: todistus,
    reload: R.identity,
    selection: 'system'
  });

  // Before signing
  assertInstructionsTextIsVisible();
  await assertNotInProgress();

  const signButton = await screen.findByRole('button', {
    name: /Allekirjoita/i
  });
  await fireEvent.click(signButton);

  // During signing
  assertSigningMethodSelectionIsNotVisible();
  assertInstructionsTextIsNotVisible();
  await assertInProgress();

  // After signing
  const statusText = await screen.findByText(
    /Kaksikielinen energiatodistus on allekirjoitettu onnistuneesti./u
  );
  expect(statusText).toBeInTheDocument();

  expect(fetchMock.mock.calls.length).toBe(2);

  assertInstructionsTextIsNotVisible();
  await assertNotInProgress();

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
  setupFetchMocks({
    [validateSessionUrl]: signingAllowedResponse(true),
    [systemSignUrl]: systemSignSuccessResponse
  });

  render(SigningDialog, {
    energiatodistus: finnishTodistus,
    reload: R.identity,
    selection: 'system'
  });

  const signButton = await screen.findByRole('button', {
    name: /Allekirjoita/i
  });

  await fireEvent.click(signButton);

  const statusText = await screen.findByText(
    /Suomenkielinen energiatodistus on allekirjoitettu onnistuneesti./u
  );
  expect(statusText).toBeInTheDocument();

  expect(signButton).not.toBeInTheDocument();
});
