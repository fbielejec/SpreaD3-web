FROM ubuntu:18.04
MAINTAINER "Filip Bielejec"

# install nginx
RUN apt-get update -y \
    && apt-get install --no-install-recommends -y \
    -q nginx

# replace nginx config
ADD nginx.conf /etc/nginx/nginx.conf 

# replace default server
ADD default /etc/nginx/sites-available/default

# setup server config
ADD ipfs.gateway /etc/nginx/sites-available/ipfs.gateway
RUN ln -s -f /etc/nginx/sites-available/ipfs.gateway /etc/nginx/sites-enabled/ipfs.gateway

EXPOSE 5001
CMD ["nginx", "-g", "daemon off;"]
