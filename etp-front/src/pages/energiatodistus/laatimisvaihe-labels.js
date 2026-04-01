/**
 * Label overrides for laatimisvaihe entries.
 *
 * For pre-2026 versions, IDs 0 and 1 should display the original shorter
 * labels instead of the 2026-era longer labels stored in the database.
 *
 * @param {number} version - The energiatodistus version (e.g. 2018, 2026)
 * @param {Array} entries - Array of laatimisvaihe classification entries
 * @returns {Array} entries with labels overridden as appropriate
 */

const pre2026LabelOverrides = {
  0: { 'label-fi': 'Rakennuslupa', 'label-sv': 'Bygglov' },
  1: { 'label-fi': 'Käyttöönotto', 'label-sv': 'Införandet' }
};

export const overrideLabels = (version, entries) => {
  if (version >= 2026) {
    return entries;
  }
  return entries.map(entry => {
    const override = pre2026LabelOverrides[entry.id];
    if (override) {
      return { ...entry, ...override };
    }
    return entry;
  });
};
