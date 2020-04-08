const https = require('https');
const express = require('express');
const proxy = require('http-proxy-middleware');
const csp = require('helmet-csp');
const fs = require('fs');

const config = require('./proxy.config');

const key = fs.readFileSync('./keys/localhost.key');
const cert = fs.readFileSync('./keys/localhost.crt');

var app = express();

app.use(
  csp({
    directives: {
      defaultSrc: ["'self'"],
      scriptSrc: ["'self'", 'localhost:35729'],
      connectSrc: [
        "'self'",
        'wss://localhost:35729',
        'https://localhost:53952',
        'https://127.0.0.1:53952'
      ],
      styleSrc: ["'self'", "'unsafe-inline'", 'fonts.googleapis.com'],
      fontSrc: ["'self'", 'fonts.gstatic.com']
    }
  })
);

app.use(
  '/api',
  proxy({ target: `http://localhost:${config['api']}`, changeOrigin: true })
);
app.use(
  '/',
  proxy({ target: `http://localhost:${config['static']}`, changeOrigin: true })
);

const server = https.createServer({ key, cert }, app);

server.listen(3000, () =>
  console.log('Proxy listening in https://localhost:3000')
);
