FROM node:20-alpine@sha256:375518d70893d14665b99393079e77bd4947884f123a66ade28744eb8340d229

WORKDIR /usr/src/app

RUN apk --no-cache add curl

COPY ./package.json package-lock.json ./
RUN npm ci
COPY ./ ./

CMD [ "npm", "run", "dev" ]
