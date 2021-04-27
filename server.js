'use strict';

const express = require('express');
const bodyParser = require('body-parser');
const session = require('express-session');
const favicon = require('serve-favicon');
const https = require('https');
const fs = require('fs');
const error = require('./server-component/error');
const pg = require('pg');

const auth = require('./server-component/auth');
const problem = require('./server-component/problem');
const profile = require('./server-component/profile');

const app = express();
app.use(favicon(__dirname + '/library/resources/favicon.ico'));
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

app.all('*', (req, res, next) => {
    let protocol = req.headers['x-forwarded-proto'] || req.protocol;
    if (protocol == 'https') next();
    else { let from = `${protocol}://${req.hostname}${req.url}`; 
        let to = `https://${req.hostname}${req.url}`;
        res.redirect(to); 
    }
});

app.get('/', (req, res)=>{
    if(auth.checkIdentity(req)) {
        res.render("main.ejs", {
            ylog: 'block',
            nlog: 'none',
            userid: req.session.user.id,
            username: req.session.user.name,
        });
    }
    else {
        res.render("main.ejs", {
            ylog: 'none',
            nlog: 'block',
            userid: '',
            username: '',
        });
    }
});

app.get('/.well-known/pki-validation/AE90BEC8EBFB3A7B8CD2825DA22282D3.txt', (req, res)=>{
    res.sendFile(__dirname+'/cert/AE90BEC8EBFB3A7B8CD2825DA22282D3.txt');
});

auth.authRouter(app, __dirname);
problem.problemRouter(app, __dirname);
profile.profileRouter(app);

app.use((req, res) => {
    error.sendError(404, 'Not Found', res);
});

var options = {
    key: fs.readFileSync('./cert/private.key'),
    cert: fs.readFileSync('./cert/certificate.crt')
};

app.listen(HTTP_PORT);
console.log("HTTP server listening on port " + HTTP_PORT);

https.createServer(options, app).listen(HTTPS_PORT, function() {
    console.log("HTTPS server listening on port " + HTTPS_PORT);
});