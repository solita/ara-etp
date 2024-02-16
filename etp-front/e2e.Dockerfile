FROM node:18.19.1@sha256:256406951729eec0e9f15266bf77ec0b07260c2087cba2f5ca98951c099c0154

WORKDIR /usr/src/app

COPY ./package.json package-lock.json ./
RUN npm ci
COPY ./ ./

CMD [ "npm", "run", "dev" ]
