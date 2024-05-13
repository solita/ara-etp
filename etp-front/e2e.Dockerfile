FROM node:22-alpine@sha256:321a52b9fccde74c532796f832046124af1dd997a65410d6155accd40fbe6ab8

WORKDIR /usr/src/app

RUN apk --no-cache add curl

COPY ./package.json package-lock.json ./
RUN npm ci
COPY ./ ./

CMD [ "npm", "run", "dev" ]
