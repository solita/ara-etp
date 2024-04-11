FROM node:20-alpine@sha256:5cf32127b55467ea639dc805a13c6f51b2facebc5eb11f9c5d49e3059f3c0aa4

WORKDIR /usr/src/app

RUN apk --no-cache add curl

COPY ./package.json package-lock.json ./
RUN npm ci
COPY ./ ./

CMD [ "npm", "run", "dev" ]
