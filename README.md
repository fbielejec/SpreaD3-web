[![Build Status](https://travis-ci.org/phylogeography/SpreaD3-web.svg?branch=master)](https://travis-ci.org/phylogeography/SpreaD3-web) [![codecov.io](https://codecov.io/gh/phylogeography/SpreaD3-web/coverage.svg?branch=master)](https://codecov.io/gh/phylogeography/SpreaD3-web?branch=master)

.:: SpreaD3-web ::.
===================

*Spatial Phylogenetic Reconstruction of Evolutionary Dynamics - web service* <br />
Version: 0.0.1, 2017 <br />
Authors: Filip Bielejec, Guy Baele, Andrew Rambaut, Marc A. Suchard & Philippe 'The Wise' Lemey <br />

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


## CONTRIBUTING
Very much welcomed. Contact for details.

## Profiles

mvn -Pproduction

mvn -Ptest

## Dev

mvn spring-boot:run

## migration

mvn -Pproduction liquibase:update

## Demo

mvn clean package && java -jar target/spread3-web-0.0.1.jar

