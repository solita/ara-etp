FROM node:20-alpine@sha256:09e2b3d9726018aecf269bd35325f46bf75046a643a66d28360ec71132750ec8

WORKDIR /usr/src/app

RUN apk --no-cache add curl

COPY ./package.json package-lock.json ./
RUN npm ci
COPY ./ ./

CMD [ "npm", "run", "dev" ]
