const express = require('express');
const proxy = require('http-proxy-middleware');

const config = require('./proxy.config');

var app = express();

app.use('/api', proxy({target: `http://localhost:${config['api']}`, changeOrigin: true}));
app.use('/', proxy({target: `http://localhost:${config['static']}`, changeOrigin: true}));

app.listen(3000);