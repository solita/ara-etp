# Varke - Energiatodistuspalvelu frontend

Tämä on web sovellus energiatodistusten laadintaan
ja todistuksiin liittyvään viranomaistoimintaan.

This is a web application for managing energy performance certificates and related activities.
This application is for accredited certificate experts and public authorities.

## Dependencies

[Node.js](https://nodejs.org) has to be installed. At the moment of writing the required version is Node 20, but there
is a `.nvmrc` file that can be used with [node version manager](https://github.com/nvm-sh/nvm) to automatically
get the currently-required version.

[Modheader](https://modheader.com/) extension for the browser is not absolutely required, but it is
useful for quickly switching between various user accounts.


```bash
source ~/.nvm/nvm.sh
nvm use
```

## Get started

Install the NPM dependencies.

```bash
npm install
```

Naturally, the [backend](../etp-core) services need to be running locally.

In case of the private frontend in this directory, the backend also expects to receive requests
with JWT headers that associate the request with a known user. The useful header sets are specified
in the `modheaders.json` file, and there are two ways to use them:

1) Use the Modheader extension in the browser to set the headers. The extension can import the
   `modheaders.json` file and then lets you choose which set of headers to use.
2) Set the `ETP_DEV_FRONT_USER` environment variable to the email address of the user
   whose JWT headers will be injected in the development server's proxy. Optionally you can also set
   the `WEBPACK_PORT` environment variable to change the port the development server runs on,
   enabling you to run multiple instances of the frontend at the same time.

Set the environment variable or the correct Modheader profile as needed, then start  [webpack](https://webpack.js.org):

```bash
npm run dev

# Or with ETP 2026 feature flag enabled
ETP_2026=true npm run dev
```

Navigate to [https://localhost:3000](https://localhost:3000). You should see your app running. Edit a component file
in `src`, save it, the page should reload automatically.

Note: To activate the work-in-progress ETP 2026 features, set the ETP_2026 environment variable to
a non-emtpy string. This tells the webpack dev server to generate a frontend config.json with
the parameters required for that.

## Testing

### What to test and at what level?

* For code that is not UI related, have the code in JS file. Write unit tests with Jest to a file <filename>_test.js
  next to the implementation.
  Separate side effects like api calls from the code so those don't need to be mocked.
* Write a story for the component using Storybook. These give an example to other developers what components we have
  available in the project. Stories are then rendered in Storybook. Storybook is then built as tests
  using [Storybook test-runner](https://github.com/storybookjs/test-runner). This tests that the stories are rendered
  without errors and also compare that they are visually similar to what they should be. This should be the minimum
  level of tests for new UI components. These should be next to the implementation in a file <Component name>
  .stories.js.
* UI component tests can be written to test the components in isolation. Use Jest
  and [testing-library](https://testing-library.com/docs/), [Svelte testing library docs](https://testing-library.com/docs/svelte-testing-library/intro)
  to write component tests. These can either just render the component with the given props and then check what was
  rendered or also contain interactions. The most minimal rendering test, such
  as [H1.test.js](src/components/H/H1.test.js) provide the additional safety compared to Storybook story that it
  checks and warns against improper props passed to the component. These should be located next to the component in a
  file <Component name>.test.js.
* Write E2E tests to test the whole system through the user interface using Cypress. Use E2E tests when the integration
  of the user interface and backend need to be tested, or when a longer UI workflow needs to be tested. Cypress tests
  and their setup are located [in their own directory](../e2e-tests/cypress).

### Jest

Run unit tests with:

```bash
npm run test
```

or

```bash
npm run tdd
```

The latter command watches file changes and reruns tests upon saved changes.

### Storybook test-runner

Run in Docker container with

```bash
./run-visual-tests-in-docker.sh
```
(or in case of Windows):
```powershell
.\run-visual-tests-in-docker.ps1
```

If you run these locally outside Docker container, the snapshots will be different as browsers in different operating
systems render things slightly differently.

When there are intentional changes, update the image snapshots by running
```bash
./run-visual-tests-in-docker.sh update
```
(or in case of Windows):
```powershell
.\run-visual-tests-in-docker.ps1 update
```

### Cypress

See [e2e test readme](../e2e-tests/cypress/README.md)

## Building and running in production mode

To create a production optimised version of the app:

```bash
npm run build
```

## About the application

### Directory structure

#### ./src

Contains all the application code divided into subfolders for different purposes. The root contains internal riggin
required for scaffolding the application.

##### ./src/components

General components to be used in UI building e.g. inputs, buttons etc.
These components can be used in any page.

##### ./src/pages

All pages and their components. Pages are grouped by feature-basis.
Each feature contains:

* index (`index.svelte`)
* page components and specialized components used from these pages (`*.svelte`)
* api modules (`*-api.js`) to create backend futures
* datamodel validation schemas (`schema.js`)
* other functional utility modules (`*.js`)

Specialized components (used only from a specific place) are:

* header
* breadcrumb
* navigation
* footer
* valvonta (used from valvonta-oikeellisuus/valvonta-kaytto)

##### ./src/language

Translations, i18n riggings and some utilities for handling objects with fi/sv-label fields.

##### ./src/utils

General purpose pure function libraries.

#### ./assets

Static assets (images, pdfs, etc.)

### Best practices

#### Naming things

- Directories/files should be kebab-case
- Imported namespaces should be PascalCase
- Data models used with backend are kebab-case
- Otherwise, use camelCase

#### Monadic types

Always use proper monadic type for special cases.

- Instead of null, use Maybe.
- Everything that could fail (f.ex. parsing/validation) synchronously should be wrapped in Either where Left is error
  and Right is value.
- Asynchronous operations should be wrapped in futures.

#### Programming style

- In general prefer functional programming style.
- Variable reassignment should only happen with svelte components. Always declare variables with const.
- No mutations allowed
- Heavy utilization of Ramda
- Curry-functions and compositions
- Write pointfree/tacit code
- Only pure functions in utilities, side effects allowed inside svelte components
- Try to achieve good test coverage with unit tests.
- If you find 2 different ways to achieve the same result within codebase; default to the newer version for examples
  when writing new code.
