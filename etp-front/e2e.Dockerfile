FROM node:20-alpine@sha256:121edf6661770d20483818426b32042da33323b6fd30fc1ad4cd6890a817e240

WORKDIR /usr/src/app

RUN apk --no-cache add curl

COPY ./package.json package-lock.json ./
RUN npm ci
COPY ./ ./

CMD [ "npm", "run", "dev" ]
