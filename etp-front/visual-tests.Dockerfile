FROM mcr.microsoft.com/playwright:v1.56.1-jammy

# Install Node.js 24 to match the project's .nvmrc
RUN apt-get update && apt-get install -y xz-utils && rm -rf /var/lib/apt/lists/*
RUN curl -fsSL https://nodejs.org/dist/v24.13.0/node-v24.13.0-linux-x64.tar.xz | tar -xJ -C /usr/local --strip-components=1

WORKDIR /visual-tests

COPY package-lock.json ./
COPY package.json ./

RUN npm ci

COPY ./ ./
RUN npm run storybook:build
