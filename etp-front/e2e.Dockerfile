FROM node:21-alpine@sha256:58c5c8d40911b57fef25117fd9bb162e6eb2d5c152006d3969fc46ca1b058665

WORKDIR /usr/src/app

RUN apk --no-cache add curl

COPY ./package.json package-lock.json ./
RUN npm ci
COPY ./ ./

CMD [ "npm", "run", "dev" ]
