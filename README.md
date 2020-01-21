# ARA - Energiatodistuspavelu

*Note that you will need to have [Node.js](https://nodejs.org) installed.*

## Get started

Install the dependencies...

```bash
npm install
```

...then start [Rollup](https://rollupjs.org)

```bash
npm run dev
```

Navigate to [localhost:3000](http://localhost:3000). You should see your app running. Edit a component file in `src`, save it, the page should reload automatically via livereload.

This command launches an express-server that acts as a proxy and a sirv server to serve static files. The proxy is configurable in ./proxy/proxy.config.js file. The purpose of this is to enable developers to develop against some backend-environment using /api -route.

## Building and running in production mode

To create an optimised version of the app:

```bash
npm run build
```

You can run the newly built app with `npm run start`. This spins up a static file server with [sirv](https://github.com/lukeed/sirv) (using --single flag for single-page applications) that is accessible in [localhost:5000](http://localhost:5000). If you want to run the production build against some environment, you can spin up a proxy in another terminal with `npm run proxy` that works exactly like in development mode. The proxy responds in [localhost:3000](http://localhost:3000).