'use strict';

const express = require('express');
const bodyParser = require('body-parser');
const session = require('express-session');
const favicon = require('serve-favicon');
const pg = require('pg');

const auth = require('./server-component/auth');
const problem = require('./server-component/problem');

const app = express();
//app.use(favicon(__dirname + '/library/imgs/fav.ico')); TODO: favicon=maybe later
app.set("view engine", "ejs");
app.use('/lib', express.static('./library'));
app.use(bodyParser.urlencoded({extended: false}));
app.use(bodyParser.json());
app.use(session({
    secret: auth.randomString(20),
    resave: false,
    saveUninitialized:true
}));

const HTTP_PORT = 8888;
const HTTPS_PORT = 4444;

//postgresql setup

app.get('/', (req, res)=>{
    if(auth.checkIdentity(req)) {
        res.render("main.ejs", {
            'ylog': 'block',
            'nlog': 'none',
            'username': req.session.user.name
        });
    }
    else {
        res.render("main.ejs", {
            'ylog': 'none',
            'nlog': 'block',
            'username': ''
        });
    }
});

auth.authRouter(app);
problem.problemRouter(app, __dirname);

app.use(function(req, res) {
    res.status(404).send("404");
});

app.listen(HTTP_PORT);
console.log("HTTP server listening on port " + HTTP_PORT);