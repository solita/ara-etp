FROM mcr.microsoft.com/playwright:v1.43.0-jammy

WORKDIR /visual-tests

# TODO: Nämä jonnekin muualle?
RUN npm install -g concurrently http-server wait-on
COPY package-lock.json ./
COPY package.json ./

RUN npm ci

COPY ./ ./
RUN npm run storybook:build
