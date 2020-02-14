const express = require('express');
const proxy = require('http-proxy-middleware');
const csp = require('helmet-csp');

const config = require('./proxy.config');

var app = express();

app.use(
  csp({
    directives: {
      defaultSrc: ["'self'"],
      scriptSrc: ["'self'", 'localhost:35729'],
      connectSrc: ["'self'", 'ws://localhost:35729'],
      styleSrc: ["'self'", 'fonts.googleapis.com'],
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

app.listen(3000, () => console.log('Proxy listening in http://localhost:3000'));
