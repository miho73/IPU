'use strict';

const express = require('express');
const bodyParser = require('body-parser');

const app = express();
app.use('/lib', express.static('./library'));
app.use(bodyParser.urlencoded({extended: false}));
app.use(bodyParser.json());

const HTTP_PORT = 8888;

app.get('/', (req, res)=>{
    res.sendFile(__dirname + "/views/main.html");
});

app.use(function(req, res) {
    res.send("404");
});

app.listen(HTTP_PORT);
console.log("HTTP server listening on port " + HTTP_PORT);