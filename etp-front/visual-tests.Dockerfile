FROM mcr.microsoft.com/playwright:v1.55.0-jammy

WORKDIR /visual-tests

COPY package-lock.json ./
COPY package.json ./

RUN npm ci

COPY ./ ./
RUN npm run storybook:build
