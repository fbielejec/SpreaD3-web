FROM ubuntu:18.04
MAINTAINER "Filip Bielejec"

# install nginx
RUN apt-get update -y \
    && apt-get install --no-install-recommends -y \
    -q nginx

# replace nginx config
ADD nginx.conf /etc/nginx/nginx.conf

# setup error page
ADD X0X.html /usr/share/nginx/html/X0X.html

# replace default server
ADD default /etc/nginx/sites-available/default

# setup server config
ADD api.spread.nodrama.io /etc/nginx/sites-available/api.spread.nodrama.io
RUN ln -s -f /etc/nginx/sites-available/api.spread.nodrama.io /etc/nginx/sites-enabled/api.spread.nodrama.io

EXPOSE 80
EXPOSE 443
CMD ["nginx", "-g", "daemon off;"]
