#TODO: read fron .envrc?
#ARG NODE_VERSION
#FROM node:${NODE_VERSION}
FROM node:18

WORKDIR /usr/src/app

# Install some dependencies
# TODO: Fix using --legacy-peer-deps
# TODO: Fix conflicts with `npm install`
# COPY ./package.json ./
COPY ./ ./
RUN npm install --legacy-peer-deps

CMD [ "npm", "run", "dev" ]
# ENTRYPOINT ["npx", "webpack-dev-server", "--content-base public"]
