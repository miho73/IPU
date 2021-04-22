FROM node:14
WORKDIR /usr/src/app

RUN npm install

RUN apt-get update
RUN apt-get install postgresql postgresql-contrib

COPY . .

EXPOSE 8080
EXPOSE 4433

CMD [ "node", "server.js" ]