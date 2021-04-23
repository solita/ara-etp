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
