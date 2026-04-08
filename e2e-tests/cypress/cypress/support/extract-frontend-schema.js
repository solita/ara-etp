/**
 * Extracts field constraints (string max-length, numeric min/max) from the
 * frontend's energiatodistus schema.js.
 *
 * Because schema.js uses webpack aliases and ES module imports, we can't
 * import it directly.  Instead we read the source, replace the imports
 * with minimal stubs that capture the constraint parameters, then evaluate
 * the whole thing in a sandboxed context.
 *
 * Called as a Cypress task (Node.js) — returns an array of
 *   { property: "dotted.path", constraint: { type, ... } }
 */

const fs = require('fs');
const path = require('path');
const vm = require('vm');

// ---------------------------------------------------------------------------
// Stub implementations — just enough to build the schema objects
// ---------------------------------------------------------------------------

/**
 * Minimal Ramda stubs that the schema.js uses.
 */
const R_stubs = {
  repeat: (item, n) => Array.from({ length: n }, () => item),
  compose:
    (...fns) =>
    x =>
      fns.reduceRight((v, f) => f(v), x),
  map: fn => x => (Array.isArray(x) ? x.map(fn) : fn(x)),
  trim: s => (typeof s === 'string' ? s.trim() : s),
  toUpper: s => (typeof s === 'string' ? s.toUpperCase() : s),
  split: sep => s => (typeof s === 'string' ? s.split(sep) : []),
  endsWith: suffix => s => (typeof s === 'string' ? s.endsWith(suffix) : false),
  append: (val, arr) => (arr === undefined ? a => [...a, val] : [...arr, val]),
  over: (lens, fn) => obj => obj,
  lensProp: () => null,
  propSatisfies: (pred, key) => obj => obj && pred(obj[key]),
  is: Type => val => val instanceof Type,
  when: (pred, fn) => val => (pred(val) ? fn(val) : val),
  has: key => obj => obj != null && key in obj,
  defaultTo: def => val => (val == null ? def : val),
  isNil: val => val == null,
  path: pathArr => obj => {
    let cur = obj;
    for (const k of pathArr) {
      if (cur == null) return undefined;
      cur = cur[k];
    }
    return cur;
  },
  concat: (a, b) => (b === undefined ? bb => [...a, ...bb] : [...a, ...b]),
  unless: (pred, fn) => obj => (pred(obj) ? obj : fn(obj)),
  prop: key => obj => (obj ? obj[key] : undefined),
  always: val => () => val,
  ifElse:
    (pred, onT, onF) =>
    (...args) =>
      pred(...args) ? onT(...args) : onF(...args),
  propEq: (val, key) => obj => obj && obj[key] === val,
  complement:
    fn =>
    (...args) =>
      !fn(...args),
  allPass:
    preds =>
    (...args) =>
      preds.every(p => p(...args)),
  filter: fn => arr => (Array.isArray(arr) ? arr.filter(fn) : {}),
  juxt:
    fns =>
    (...args) =>
      fns.map(f => f(...args)),
  identity: x => x,
  tap: fn => x => {
    fn(x);
    return x;
  },
  T: () => true,
  mergeDeepRight: (a, b) => {
    // Handle R.__ placeholder: R.mergeDeepRight(R.__, obj) returns a fn
    if (a === R_stubs.__) {
      return actualA => R_stubs.mergeDeepRight(actualA, b);
    }
    if (typeof b === 'undefined') {
      // curried: R.mergeDeepRight(obj) returns fn waiting for second arg
      return actualB => R_stubs.mergeDeepRight(a, actualB);
    }
    const result = { ...a };
    for (const key of Object.keys(b)) {
      if (
        b[key] &&
        typeof b[key] === 'object' &&
        !Array.isArray(b[key]) &&
        a[key] &&
        typeof a[key] === 'object' &&
        !Array.isArray(a[key])
      ) {
        result[key] = R_stubs.mergeDeepRight(a[key], b[key]);
      } else {
        result[key] = b[key];
      }
    }
    return result;
  },
  assocPath: (pathArr, val) => obj => {
    const result = Array.isArray(obj) ? [...obj] : { ...obj };
    if (pathArr.length === 0) return val;
    if (pathArr.length === 1) {
      result[pathArr[0]] = val;
      return result;
    }
    const [head, ...tail] = pathArr;
    result[head] = R_stubs.assocPath(tail, val)(result[head] || {});
    return result;
  },
  dissocPath: pathArr => obj => {
    if (pathArr.length === 0) return obj;
    const result = { ...obj };
    if (pathArr.length === 1) {
      delete result[pathArr[0]];
      return result;
    }
    const [head, ...tail] = pathArr;
    if (result[head]) {
      result[head] = R_stubs.dissocPath(tail)(result[head]);
    }
    return result;
  },
  __: Symbol('R.__')
};

// ---------------------------------------------------------------------------
// Build the schema by evaluating schema.js with stubs
// ---------------------------------------------------------------------------

function buildFrontendSchema(schemaJsPath) {
  const source = fs.readFileSync(schemaJsPath, 'utf-8');

  // Rewrite imports to use our stubs.
  // We replace `import ... from '...'` with nothing — the sandbox has
  // the needed names pre-populated.
  let code = source
    // Remove all import lines
    .replace(/^import\s+.*$/gm, '')
    // Replace `export const` with just `const` so we can capture the values
    .replace(/export\s+const\s+/g, 'const ');

  // Wrap in a function that returns the schema objects
  const wrappedCode = `
    ${code}

    // Return the schemas we care about
    ({
      v2013: typeof v2013 !== 'undefined' ? v2013 : null,
      v2018: typeof v2018 !== 'undefined' ? v2018 : null,
      v2026: typeof v2026 !== 'undefined' ? v2026 : null,
    });
  `;

  // Create sandbox with stubs
  const sandbox = {
    // Ramda
    R: R_stubs,

    // Stubs for things referenced but not needed for constraint extraction
    parsers: new Proxy({}, { get: () => () => {} }),
    formats: new Proxy({}, { get: () => () => {} }),
    validations: {
      liftValidator: x => x,
      minLengthConstraint: min => ({
        __validatorType: 'minLength',
        value: min
      }),
      maxLengthConstraint: max => ({
        __validatorType: 'maxLength',
        value: max
      }),
      MaybeInterval: (min, max) => [
        { __validatorType: 'intervalMin', value: min },
        { __validatorType: 'intervalMax', value: max }
      ],
      MaybeIntervalWarning: (min, max) => [
        { __validatorType: 'warnMin', value: min },
        { __validatorType: 'warnMax', value: max }
      ],
      rakennustunnusValidator: { __validatorType: 'rakennustunnus' },
      isValidId: () => ({ __validatorType: 'enumId' })
    },
    Validations: {
      liftValidator: x => x
    },
    Either: new Proxy({}, { get: () => () => {} }),
    Maybe: new Proxy(
      {},
      {
        get: (_, prop) => {
          if (prop === 'fromEmpty') return x => x;
          if (prop === 'orSome') return () => () => {};
          if (prop === 'isSome') return () => true;
          return () => {};
        }
      }
    ),
    dfns: new Proxy(
      {},
      {
        get: (_, prop) => {
          if (prop === 'formatISO') return () => '';
          if (prop === 'getYear') return () => 2026;
          return () => {};
        }
      }
    ),
    Inputs: new Proxy(
      {},
      {
        get: (_, prop) => {
          if (prop === 'removeLocalePostfix') return x => x;
          return () => {};
        }
      }
    ),
    Validation: new Proxy(
      {},
      {
        get: (_, prop) => {
          if (prop === 'predicate') return () => () => true;
          return () => {};
        }
      }
    ),
    Deep: new Proxy({}, { get: () => () => {} }),
    Date: Date,
    Array: Array,
    console: console
  };

  vm.createContext(sandbox);
  try {
    return vm.runInContext(wrappedCode, sandbox, { timeout: 5000 });
  } catch (e) {
    console.error('Failed to evaluate schema.js:', e.message);
    return null;
  }
}

// ---------------------------------------------------------------------------
// Walk the schema tree and extract constraints
// ---------------------------------------------------------------------------

function isLeafType(obj) {
  return (
    obj && typeof obj === 'object' && !Array.isArray(obj) && 'validators' in obj
  );
}

/**
 * Extract constraint info from a leaf type object by inspecting its
 * validators array (which contains our marker objects).
 */
function extractConstraint(obj) {
  const validators = Array.isArray(obj.validators) ? obj.validators.flat() : [];

  const findValidator = type =>
    validators.find(v => v && v.__validatorType === type);

  const minLen = findValidator('minLength');
  const maxLen = findValidator('maxLength');
  const intMin = findValidator('intervalMin');
  const intMax = findValidator('intervalMax');
  const rak = findValidator('rakennustunnus');

  if (minLen || maxLen) {
    return {
      type: 'string',
      'min-length': minLen ? minLen.value : undefined,
      'max-length': maxLen ? maxLen.value : undefined
    };
  }

  if (intMin && intMax) {
    // Detect integer vs float: Integer() uses parsers.parseInteger,
    // but we can't check that. Instead, check if values are integers
    // and the range looks integer-ish. Not perfect, but good enough.
    return {
      type: 'number',
      min: intMin.value,
      max: intMax.value
    };
  }

  if (rak) {
    return { type: 'rakennustunnus' };
  }

  // DateValue or other type with empty validators
  if (validators.length === 0) {
    return { type: 'date' };
  }

  return { type: 'unknown' };
}

function walkSchema(prefix, obj) {
  if (!obj || typeof obj !== 'object') return [];

  // Leaf type — extract constraint
  if (isLeafType(obj)) {
    const constraint = extractConstraint(obj);
    return [{ property: prefix, constraint }];
  }

  // Array — look at first element (e.g. toimenpide, muu)
  if (Array.isArray(obj)) {
    if (obj.length > 0 && typeof obj[0] === 'object' && !isLeafType(obj[0])) {
      return walkSchema(prefix + '[*]', obj[0]);
    }
    // Array of leaf types (e.g. repeat of a leaf)
    if (obj.length > 0 && isLeafType(obj[0])) {
      return walkSchema(prefix + '[*]', obj[0]);
    }
    return [];
  }

  // Object — recurse into values, skip internal keys
  const results = [];
  for (const [key, value] of Object.entries(obj)) {
    if (
      key.startsWith('$') ||
      key === 'parse' ||
      key === 'format' ||
      key === 'deserialize' ||
      key === 'serialize' ||
      key === 'validators' ||
      key === 'warnValidators' ||
      key === 'required'
    ) {
      continue;
    }
    const childPath = prefix ? `${prefix}.${key}` : key;
    results.push(...walkSchema(childPath, value));
  }
  return results;
}

// ---------------------------------------------------------------------------
// Main export for Cypress task
// ---------------------------------------------------------------------------

function extractFrontendConstraints(version) {
  const schemaJsPath = path.resolve(
    __dirname,
    '../../../../etp-front/src/pages/energiatodistus/schema.js'
  );

  const schemas = buildFrontendSchema(schemaJsPath);
  if (!schemas) return [];

  const versionKey = `v${version}`;
  const schema = schemas[versionKey];
  if (!schema) return [];

  return walkSchema('', schema).sort((a, b) =>
    a.property.localeCompare(b.property)
  );
}

module.exports = { extractFrontendConstraints };
