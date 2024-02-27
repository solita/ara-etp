FROM node:20-alpine@sha256:f4c96a28c0b2d8981664e03f461c2677152cd9a756012ffa8e2c6727427c2bda

WORKDIR /usr/src/app

RUN apk --no-cache add curl

COPY ./package.json package-lock.json ./
RUN npm ci
COPY ./ ./

CMD [ "npm", "run", "dev" ]
