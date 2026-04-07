# ARA - Energiatodistusrekisteri public frontend

*Note that you will need to have [Node.js](https://nodejs.org) installed.*

## Get started

Install the dependencies...

```bash
npm ci
```

The project includes the `sharp` native module, which requires build scripts to be executed.
Due to the `.npmrc` configuration that disables scripts during installation, you need to
manually rebuild `sharp` after installation:

```bash
npm rebuild sharp --foreground-scripts --ignore-scripts=false
```

You also need to start [backend](../etp-core) services


...then start [webpack](https://webpack.js.org)

```bash
npm run dev
```

Navigate to [localhost:5050](http://localhost:5050). You should see your app running. Edit a component file in `src`, save it, the page should reload automatically.

## Building and running in production mode

To create a production optimised version of the app:

```bash
npm run build
```
