FROM node:18.19.0@sha256:04f22bd900cb2b1c82c62315248085aa821b929f4c7af9ed89e01c76f9191df8

WORKDIR /usr/src/app

COPY ./package.json package-lock.json ./
RUN npm install --legacy-peer-deps
COPY ./ ./

CMD [ "npm", "run", "dev" ]
