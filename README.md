# ARA - Energiatodistuspavelu

_Note that you will need to have [Node.js](https://nodejs.org) installed._

## Get started

Install the dependencies...

```bash
npm install
```

You also need to start [backend](https://github.com/solita/etp-core) services found in different repository.

...then start [webpack](https://webpack.js.org)

```bash
npm run dev
```

Navigate to [https://localhost:3000](https://localhost:3000). You should see your app running. Edit a component file in `src`, save it, the page should reload automatically.

## Testing

### Mocha

Run unit tests with:

```bash
npm run test
```

or

```bash
npm run tdd
```

The latter command watches file changes and reruns tests upon saved changes.

### Cypress

Make sure you installed optional dependencies from npm. Then just use the command:

```bash
npm run cypress
```

## Building and running in production mode

To create a production optimised version of the app:

```bash
npm run build
```


## About the application

### Directory structure

#### ./src

Contains all the application code divided into subfolders for different purposes. The root contains internal riggin required for scaffolding the application.

##### ./src/components

General components to be used in UI building

##### ./src/pages

All specialized pages and their components. Somewhat grouped by feature-basis.

##### ./src/language

Translations, i18n riggings and some utilities for handling objects with fi/sv-label fields.

#### ./assets

Static assets (images, pdfs, etc.)

#### ./cypress

Cypress tests


### Best practices

#### Naming things

- Directories/files should be kebab-case
- Imported namespaces should be pascal-case 
- Otherwise use camel-case.

#### Monadic types

Always use proper monadic type for special cases.
- Instead of null, use Maybe.
- Everything that could fail (f.ex. parsing/validation) synchronously should be wrapped in Either where Left is error and Right is value.
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
- If you find 2 different ways to achieve the same result within codebase; default to the newer version for examples when writing new code.