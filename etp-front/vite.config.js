import { defineConfig } from 'vite';
import { svelte } from '@sveltejs/vite-plugin-svelte';
import basicSsl from '@vitejs/plugin-basic-ssl';
import path from 'path';
import { format } from 'date-fns/format';
import { readFileSync } from 'fs';

// Load mock headers for dev proxy
const mockHeaders = JSON.parse(
  readFileSync(new URL('./modheaders.json', import.meta.url))
);

const headersForUser = username => {
  if (!username) {
    return [];
  }
  const conf = mockHeaders.find(header => header.title === username);
  return conf ? conf.headers.filter(h => h.enabled) : [];
};

// Plugin to generate version.json (build) and serve it (dev)
const versionPlugin = () => ({
  name: 'version-json',
  configureServer(server) {
    server.middlewares.use((req, res, next) => {
      const url = req.url?.split('?')[0]; // Remove query string
      if (url === '/version.json') {
        res.setHeader('Content-Type', 'application/json');
        res.end(
          JSON.stringify({
            version: `dev - ${format(Date.now(), 'yyyy-MM-dd-HH-mm')}`
          })
        );
        return;
      }
      next();
    });
  },
  generateBundle() {
    this.emitFile({
      type: 'asset',
      fileName: 'version.json',
      source: JSON.stringify({
        version: `build - ${format(Date.now(), 'yyyy-MM-dd-HH-mm')}`
      })
    });
  }
});

// Plugin to serve config.json in dev mode
const configPlugin = () => ({
  name: 'config-json',
  configureServer(server) {
    server.middlewares.use((req, res, next) => {
      const url = req.url?.split('?')[0]; // Remove query string
      if (url === '/config.json') {
        res.setHeader('Content-Type', 'application/json');
        res.end(
          JSON.stringify({
            isDev: true,
            environment: 'dev',
            publicSiteUrl: 'https://localhost:3000',
            isEtp2026: !!process.env.ETP_2026
          })
        );
        return;
      }
      next();
    });
  }
});

export default defineConfig(({ mode }) => {
  const prod = mode === 'production';

  return {
    plugins: [
      versionPlugin(),
      configPlugin(),
      svelte({
        compilerOptions: {
          immutable: true
        }
      }),
      basicSsl()
    ],
    publicDir: 'static',
    resolve: {
      extensions: ['.mjs', '.js', '.svelte', '.ts', '.jsx', '.tsx', '.json'],
      alias: {
        '@Pages': path.resolve(__dirname, 'src/pages'),
        '@Component': path.resolve(__dirname, 'src/components'),
        '@Utility': path.resolve(__dirname, 'src/utils'),
        '@Language': path.resolve(__dirname, 'src/language'),
        '@': path.resolve(__dirname, 'src')
      }
    },
    build: {
      outDir: 'public',
      emptyOutDir: true,
      target: 'es2020',
      rollupOptions: {
        output: {
          entryFileNames: '[name].[hash].js',
          chunkFileNames: '[name].[hash].js',
          assetFileNames: '[name].[hash][extname]'
        }
      }
    },
    server: {
      port: process.env.WEBPACK_PORT ? parseInt(process.env.WEBPACK_PORT) : 3000,
      host: process.env.WEBPACK_HOST || undefined,
      headers: {
        'Content-Security-Policy':
          "default-src 'self'; script-src 'self'; connect-src 'self' localhost:53952 ws://localhost:3000 wss://localhost:3000; style-src 'self' 'unsafe-inline' https://fonts.googleapis.com https://cdn.quilljs.com; font-src 'self' https://fonts.gstatic.com; img-src 'self' data:"
      },
      proxy: {
        '/api': {
          target: process.env.WEBPACK_PROXY_TARGET || 'http://localhost:8080',
          secure: false,
          changeOrigin: true,
          configure: proxy => {
            proxy.on('proxyReq', proxyReq => {
              const headers = headersForUser(process.env.ETP_DEV_FRONT_USER);
              for (const { name, value } of headers) {
                proxyReq.setHeader(name, value);
              }
            });
          }
        }
      }
    }
  };
});
