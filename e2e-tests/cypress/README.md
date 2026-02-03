# Cypress tests
### Running the tests

To run the tests you must first start the `e2e Docker stack`. To do so on systems
other than Windows, run the `start-e2e-env.sh` script. For Windows environments,
there is a similar `start-e2e-env.ps1` script that can be used instead.
```bash
./start-e2e-env.sh
```

After the e2e stack is running and you have remembered to run `npm install`, you can start cypress:

```bash
npm run cypress
```

or just run the tests:

```bash
npm run cypress:run
```

You can also narrow down the tests with `--spec`
```bash
npm run cypress:run -- --spec "cypress/e2e/laatija/energiatodistus-signing.cy.js","cypress/e2e/laatija/tiedot-tarkastamatta.cy.js"
```

### Docker stack

The `e2e Docker stack` includes the `dev Docker stack` from `etp-core/docker/` and adds
`frontend`, `backend` and `etp-db` services on top of it. The mapping of ports in the dev stack is
overridden by `override.yml` so that you can run it while also running the e2e stack.

### Writing more tests

> **NB:** The `e2e Docker stack` might not yet use all of the components (minio, etc.)
> correctly. Typically you can fix this by making their location (e.g. localhost:1234) configurable
> through an environment variable and setting it to the container's name (e.g. container:1234).

In `cypress.config.js` you can temporarily change the `baseUrl` to point to your local frontend so that you  don't
have to the image after every change. Then you might want to also comment out resetting the database and setting the
headers if your test does.

Best practises can be found at https://docs.cypress.io/guides/references/best-practices
