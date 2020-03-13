import { writable } from 'svelte/store';
import * as Either from '@Utility/either-utils';

export const currentUserStore = writable();
export const errorStore = writable();
export const countryStore = writable(Either.Left('Not initialized'));
export const breadcrumbStore = writable([]);
