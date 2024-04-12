FROM mcr.microsoft.com/playwright:v1.43.1-jammy

WORKDIR /visual-tests

COPY package-lock.json ./
COPY package.json ./

RUN npm ci

COPY ./ ./
RUN npm run storybook:build
