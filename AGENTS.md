# Project Guidelines

## Build and Test

### Frontend (etp-front)

Run all tests:

```bash
cd etp-front
npm run test
```

Run tests in watch mode (re-runs on file changes):

```bash
cd etp-front
npm run tdd
```

Run a single test file:

```bash
cd etp-front
npm run test -- src/components/Pagination/pagination-utils_test.js
```

Run a single test case by name (uses Jest's `-t` flag to match test name):

```bash
cd etp-front
npm run test -- src/components/Pagination/pagination-utils_test.js -t "test name pattern"
```

Test files are located next to the implementation with the naming convention `<filename>_test.js` or `<Component>.test.js`.

### Backend (etp-backend)

The backend uses Clojure with eftest. The Docker environment (database etc.) must be running before tests can be executed.

Run all tests:

```bash
cd etp-core/etp-backend
clojure -M:dev:test
```

Run a single test namespace:

```bash
cd etp-core/etp-backend
clojure -M:dev -e "(require 'solita.etp.jwt-test) (let [{:keys [fail error]} (clojure.test/run-tests 'solita.etp.jwt-test)] (System/exit (if (and (zero? fail) (zero? error)) 0 1)))"
```

Run a single test var:

```bash
cd etp-core/etp-backend
clojure -M:dev -e "(require 'solita.etp.jwt-test) (run-test #'solita.etp.jwt-test/some-test-name) (System/exit 0)"
```

Test files are located in `etp-core/etp-backend/src/test/clj/` and follow the `<namespace>_test.clj` naming convention.
