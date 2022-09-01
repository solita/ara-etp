import * as R from 'ramda';

/**
 * @sig Template -> boolean
 * @description Tests if a käytönvalvonta document template is sent tiedoksi
 */
export const sendTiedoksi = R.prop('tiedoksi');
