FROM node:22-alpine@sha256:348b3e6ff4eb6b9ac7c9cc5324b90bf8fc2b7b97621ca1e9e985b7c80f7ce6b3

WORKDIR /usr/src/app

RUN apk --no-cache add curl

COPY ./package.json package-lock.json ./
RUN npm ci
COPY ./ ./

CMD [ "npm", "run", "dev" ]
