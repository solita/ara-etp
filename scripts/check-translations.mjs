#!/usr/bin/env node
// Checks that fi/sv translation catalogs are normalized:
//  - both files have exactly the same set of (flattened) keys
//  - keys appear in the same order (fi.json is the reference)
//  - values are non-empty strings (arrays of strings allowed)
//
// Usage:
//   node scripts/check-translations.mjs          # check, exit 1 on problems
//   node scripts/check-translations.mjs --fix    # normalize sv from fi
//
// --fix adds missing sv keys as "<fi value> (sv)", removes extra keys and
// reorders sv to match fi. Missing fi keys are never invented; they are
// reported as errors (fi is the source language).

import { readFileSync, writeFileSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const root = join(dirname(fileURLToPath(import.meta.url)), '..');

const catalogs = [
  {
    name: 'etp-front',
    fi: 'etp-front/src/language/fi.json',
    sv: 'etp-front/src/language/sv.json'
  },
  {
    name: 'etp-public',
    fi: 'etp-public/assets/fi.json',
    sv: 'etp-public/assets/sv.json'
  }
];

const fix = process.argv.includes('--fix');

const isObject = (x) => x !== null && typeof x === 'object' && !Array.isArray(x);

const flatten = (obj, prefix = '', out = new Map()) => {
  for (const [k, v] of Object.entries(obj)) {
    const key = prefix ? `${prefix}.${k}` : k;
    if (isObject(v)) flatten(v, key, out);
    else out.set(key, v);
  }
  return out;
};

const read = (path) => JSON.parse(readFileSync(join(root, path), 'utf8'));

// Rebuild sv with the structure and key order of fi.
const normalizeSv = (fi, sv) => {
  const out = {};
  for (const [k, v] of Object.entries(fi)) {
    if (isObject(v)) {
      out[k] = normalizeSv(v, isObject(sv?.[k]) ? sv[k] : {});
    } else if (sv && Object.hasOwn(sv, k) && !isObject(sv[k])) {
      out[k] = sv[k];
    } else {
      out[k] = typeof v === 'string' ? `${v} (sv)` : v;
    }
  }
  return out;
};

const emptyValue = (v) =>
  v === null ||
  v === undefined ||
  (typeof v === 'string' && v.trim() === '') ||
  (Array.isArray(v) && v.length === 0);

let failed = false;

for (const { name, fi: fiPath, sv: svPath } of catalogs) {
  let fi = read(fiPath);
  let sv = read(svPath);

  if (fix) {
    sv = normalizeSv(fi, sv);
    // Preserve the file's existing line endings and trailing newline.
    const raw = readFileSync(join(root, svPath), 'utf8');
    const eol = raw.includes('\r\n') ? '\r\n' : '\n';
    const trailing = /\r?\n$/.test(raw) ? eol : '';
    writeFileSync(
      join(root, svPath),
      JSON.stringify(sv, null, 2)
        .replace(/\u00AD/g, '\\u00AD') // keep soft hyphens visible
        .replace(/\n/g, eol) + trailing
    );
    console.log(`${name}: normalized ${svPath}`);
    sv = read(svPath);
  }

  const fiFlat = flatten(fi);
  const svFlat = flatten(sv);
  const errors = [];

  for (const key of fiFlat.keys())
    if (!svFlat.has(key)) errors.push(`missing in sv: ${key}`);
  for (const key of svFlat.keys())
    if (!fiFlat.has(key)) errors.push(`missing in fi (extra in sv): ${key}`);

  for (const [file, flat] of [
    [fiPath, fiFlat],
    [svPath, svFlat]
  ])
    for (const [key, value] of flat)
      if (emptyValue(value)) errors.push(`empty value in ${file}: ${key}`);

  if (errors.length === 0) {
    const fiKeys = [...fiFlat.keys()];
    const svKeys = [...svFlat.keys()];
    const i = fiKeys.findIndex((k, idx) => k !== svKeys[idx]);
    if (i !== -1)
      errors.push(
        `key order differs from fi.json (first difference at "${fiKeys[i]}" vs "${svKeys[i]}")`
      );
  }

  if (errors.length > 0) {
    failed = true;
    console.error(`\n${name}: ${errors.length} problem(s)`);
    for (const e of errors) console.error(`  - ${e}`);
  } else {
    console.log(`${name}: OK (${fiFlat.size} keys)`);
  }
}

if (failed) {
  console.error('\nTranslations are not normalized. Run: node scripts/check-translations.mjs --fix');
  process.exit(1);
}

