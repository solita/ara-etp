FROM node:20-alpine@sha256:658d0f63e501824d6c23e06d4bb95c71e7d704537c9d9272f488ac03a370d448

WORKDIR /usr/src/app

RUN apk --no-cache add curl

COPY ./package.json package-lock.json ./
RUN npm ci
COPY ./ ./

CMD [ "npm", "run", "dev" ]
