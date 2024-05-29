FROM node:20-alpine@sha256:389ae25ab30b4739d863c6116a686901ae99e9ccaaed524a0db9481531266f82

WORKDIR /usr/src/app

RUN apk --no-cache add curl

COPY ./package.json package-lock.json ./
RUN npm ci
COPY ./ ./

CMD [ "npm", "run", "dev" ]
