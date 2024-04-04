FROM node:20-alpine@sha256:eca3b795767baded421fb73ef18f2fdb47c87fd3e53d093148cbc8275e03c9fa

WORKDIR /usr/src/app

RUN apk --no-cache add curl

COPY ./package.json package-lock.json ./
RUN npm ci
COPY ./ ./

CMD [ "npm", "run", "dev" ]
