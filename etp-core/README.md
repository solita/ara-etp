ARA - Energiatodistuspalvelu
===

The components of the system are:
- User interface for different stakeholders
- Backend / application layer
- Database (PostgreSQL)

Backend / Application layer
--------------
The backend contains the following components:
- [etp-backend](/etp-backend) - backend services for both the public and
  authorities -sections
- [etp-db](/etp-db) - database migration tool

User interface
---------------

Separate repositories at https://github.com/solita/etp-front and https://github.com/solita/etp-public

Installing the development environment
-----------------------------

### Java 17

For Ubuntu:

    sudo apt install openjdk-17-jdk

### Docker

Installation for Linux by following the instructions at the following URL:

https://docs.docker.com/install/linux/docker-ce/ubuntu/#install-using-the-repository

Remember to follow the
[Docker postinstall](https://docs.docker.com/install/linux/linux-postinstall/)
-guide. Then logout and login.

Docker-compose is also required:

https://docs.docker.com/compose/install/

### Clojure + CLI Tools

For Ubuntu:

https://clojure.org/guides/getting_started#_installation_on_linux

### LibreOffice
Install LibreOffice

For MacOS:

    brew install libreoffice
    ln -s /opt/homebrew/bin/soffice /opt/homebrew/bin/libreoffice

Starting the development environment
--------------------------------

Start [the required services](/docker) (database etc):

    cd docker
    ./start.sh

Start script starts docker-compose, creates template and dev databases
and runs migrations for both of them.

Docker-compose has a service for faking digital signatures, mpollux. It uses a self-signed certificate.
Visit https://127.0.0.1:53952/ and trust the certificate to get the digital signatures working.

Start [the backend](/etp-backend). Backend developers should start the REPL from
their IDE and start the services from there by calling the `reset` function.
The application can be also started from the command line with the following
command:

    cd etp-backend
    clojure -M:dev -m solita.etp.core

Tests can be run (parallel) with

    cd etp-backend
    clojure -M:dev:test

Test coverage report (without API layer) can be generated with

    cd etp-backend
    clojure -M:dev:coverage

Check dependencies for vulnerabilities

    cd etp-backend
    ./nvd.sh

Check outdated dependencies

    cd etp-backend
    clojure -M:outdated

Documentation
---
When the server is running, the API documentation can be found at
http://localhost:8080/api/documentation/index.html

MinIO
---
[MinIO](https://github.com/minio/minio) is used as a replacement for S3 in
local development environment. MinIO Console can be accessed through
http://localhost:9001/ (user: minio pw: minio123).


About database usage
--------------------

There are two users for the database:

 * ```etp```: has all privileges to ```postgres``` database.
 * ```etp_app```: can read and write to tables in ```postgres``` database.

In production and test environments the ```postgres``` database is used
normally. It needs to be created during instance setup and migrations are ran as
```etp``` user. ```etp_app``` user writes and reads data from the tables.

In development environment the ```postgres``` database is used as a template
that can be used for setting up new databases. The dockerized Postgres sets up
a second database ```etp_dev``` which should be used locally during
development. For convenience, [docker](/docker) directory contains script
[flyway.sh](/docker/flyway.sh) that can be used for migrating and cleaning
both databases with a single call.

Tests will utilize ```postgres``` database as template extensively as each test
will create their own database from it.

### Generating data

##### Manual performance testing
The  function `user/generate-energiatodistukset-for-performance-testing!`
can be used to add multiple signed energiatodistus into the database. For
performance testing purpose most strings are just randomly generated and
signing the documents is done by just directly inserting signed documents.

For example, to generate 2000 energiatodistus you can run:
```clojure
(user/generate-energiatodistukset-for-performance-testing! 2000)
```

Third-party licenses
--------------------

To generate a site of used licenses:

    cd etp-backend
    clojure -Spom
    # Now add Clojars to repositories in generated pom.xml
    mvn project-info-reports:dependencies

Other environments
---

### Uberjars

Both projects contain script ```build-docker-image.sh``` which can be
used to build the uberjars and related docker containers. The build containers
can be executed by running ```docker run [etp-db or etp-backend]```.
