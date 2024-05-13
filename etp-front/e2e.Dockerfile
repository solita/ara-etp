FROM node:20-alpine@sha256:f3d945e503328d76a54533ce1cbe8d6de03df16b00002f2ec1a890a28d1d6bd6

WORKDIR /usr/src/app

RUN apk --no-cache add curl

COPY ./package.json package-lock.json ./
RUN npm ci
COPY ./ ./

CMD [ "npm", "run", "dev" ]
