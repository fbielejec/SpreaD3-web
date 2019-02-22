[![Build Status](https://travis-ci.org/fbielejec/spread-server.svg?branch=master)](https://travis-ci.org/fbielejec/spread-server) [![codecov.io](https://codecov.io/gh/fbielejec/spread-server/coverage.svg?branch=master)](https://codecov.io/gh/fbielejec/spread-server?branch=master)

.:: SpreaD3-web ::.
===================

*Spatial Phylogenetic Reconstruction of Evolutionary Dynamics - REST api* <br />
Version: 0.1.0, 2019 <br />
Author: Filip Bielejec <br />

## PURPOSE
REST Web Services to SpreaD3, capable of generating links with on-line visualisations.

## LICENSE
  This is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation; either version 3
  of the License, or (at your option) any later version.

   This software is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   "GNU Lesser General Public License": http://www.gnu.org/licenses/lgpl.html for more details.

## Test

* run tests once
```bash
mvn test
```

* rerun tests on code changes
```bash
mvn fizzed-watcher:run
```

## Dev

Run with default config:
```bash
mvn spring-boot:run
```

Override config values:
```bash
mvn spring-boot:run -Drun.arguments=--server.port=6300,--app.logging.level=INFO
```

### Embedded database console (url: jdbc:h2:~/test username: test password: test) ######

http://localhost:4000/h2-console/

## Production

mvn clean package -DskipTests && java -jar target/spread-server.jar --spring.config.location=file:/home/$USER/configs/spread.properties

### Docker

docker-compose up
