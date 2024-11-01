FROM node:20-alpine@sha256:d504f23acdda979406cf3bdbff0dff7933e5c4ec183dda404ed24286c6125e60

WORKDIR /usr/src/app

RUN apk --no-cache add curl

COPY ./package.json package-lock.json ./
RUN npm ci
COPY ./ ./

CMD [ "npm", "run", "dev" ]
