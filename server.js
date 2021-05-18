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
const mgr = require('./server-component/management');
const docs = require('./server-component/docs');
const usr = require('./server-component/users');
const etc = require('./server-component/etc');

const app = express();
app.use(favicon(__dirname + '/library/resources/favicon.ico'));
app.set("view engine", "ejs");
app.use('/lib', express.static('./library'));
app.use(bodyParser.urlencoded({extended: false}));
app.use(bodyParser.json());
app.use(session({
    secret: auth.randomString(20),
    httpOnly: true,
    resave: false,
    saveUninitialized: true,
    cookie: {
        httpOnly: true,
        secure: true
    },
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
            ylogi: 'inline-block',
            nlogi: 'none',
            userid: req.session.user.id,
            username: req.session.user.name,
        });
    }
    else {
        res.render("main.ejs", {
            ylog: 'none',
            nlog: 'block',
            ylogi: 'none',
            nlogi: 'inline-block',
            userid: '',
            username: '',
        });
    }
});

app.get('/robots.txt', (req, res)=>{
    res.sendFile(__dirname+'/robots.txt');
});

try {
    auth.authRouter(app, __dirname);
    problem.problemRouter(app, __dirname);
    profile.profileRouter(app);
    mgr.manageRouter(app, __dirname);
    docs.docsRouter(app, __dirname);
    etc.etcRouter(app);
    usr.usrRouter(app);
}
catch(error) {
   console.log("Global Exception Catch: "+error)
}

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