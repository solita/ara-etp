import '@testing-library/jest-dom';
import 'cross-fetch/polyfill';
import { configure } from '@testing-library/svelte';

configure({ testIdAttribute: 'data-cy' });
