/**
 * @module Geo
 */

/**
 * @typedef {Object} Country
 * @property {string} id
 * @property {string} label-fi
 * @property {string} label-sv
 * @property {boolean} valid
 */

/**
 * @typedef {Object} Toimintaalue
 * @property {number} id
 * @property {string} label-fi
 * @property {string} label-sv
 * @property {boolean} valid
 */

import * as Fetch from '@Utility/fetch-utils';

export const countries = Fetch.cached(fetch, '/countries/');
export const toimintaalueet = Fetch.cached(fetch, '/toimintaalueet/');
export const postinumerot = Fetch.cached(fetch, '/postinumerot');
