FROM mcr.microsoft.com/devcontainers/base:ubuntu-22.04

# Java 17 and other tools
RUN apt-get update \
    && apt-get install -y openjdk-17-jdk tmux jq \
    && rm -rf /var/lib/apt/lists/*

USER vscode

# Node.js 24 - https://nodejs.org/en/download
ENV NVM_DIR=/home/vscode/.nvm
RUN curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.4/install.sh | bash \
    && \. "$NVM_DIR/nvm.sh" \
    && nvm install 24

# Clojure - https://clojure.org/guides/install_clojure#_posix_instructions
RUN cd /tmp \
    && curl -L -O https://github.com/clojure/brew-install/releases/latest/download/posix-install.sh \
    && chmod +x posix-install.sh \
    && sudo ./posix-install.sh \
    && rm posix-install.sh
