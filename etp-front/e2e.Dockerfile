FROM node:18

WORKDIR /usr/src/app

COPY ./package.json package-lock.json ./
RUN npm install --legacy-peer-deps
COPY ./ ./

CMD [ "npm", "run", "dev" ]
