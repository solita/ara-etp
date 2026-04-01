/**
 * Label overrides for laatimisvaihe entries on the public site.
 *
 * For pre-2026 versions, IDs 0 and 1 should display the original shorter
 * labels instead of the 2026-era longer labels stored in the database.
 *
 * @param {number|string} version - The energiatodistus version (e.g. 2018, 2026)
 * @param {Object} entry - A laatimisvaihe classification entry
 * @returns {Object} entry with labels overridden as appropriate
 */

const pre2026LabelOverrides = {
  0: { 'label-fi': 'Rakennuslupa', 'label-sv': 'Bygglov' },
  1: { 'label-fi': 'Käyttöönotto', 'label-sv': 'Införandet' }
};

export const overrideLaatimisvaiheLabel = (version, entry) => {
  if (!entry || parseInt(version, 10) >= 2026) {
    return entry;
  }
  const override = pre2026LabelOverrides[entry.id];
  if (override) {
    return { ...entry, ...override };
  }
  return entry;
};
