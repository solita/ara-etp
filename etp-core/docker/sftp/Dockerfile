FROM alpine:3.18

RUN apk add --upgrade \
    bash \
    shadow \
    openssh \
    openssh-sftp-server \
    && addgroup -S etp && adduser -S etp -G etp --shell /bin/bash \
    && echo "etp:etp" | chpasswd 

COPY ssh /etc/ssh
RUN chmod -Rv og-rwx /etc/ssh/

EXPOSE 22
CMD ["/usr/sbin/sshd", "-D"]
