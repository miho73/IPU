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
app.disable('x-powered-by');
app.use(bodyParser.urlencoded({extended: false}));
app.use(bodyParser.json());
app.use(session({
    secret: auth.randomString(20),
    resave: false,
    name: 'authIdentifier',
    saveUninitialized: true,
    cookie: {
        httpOnly: true,
        secure: true,
        sameSite: true
    },
}));

const HTTP_PORT = 8800;
const HTTPS_PORT = 4400;
/*
app.all('*', (req, res, next) => {
    let protocol = req.headers['x-forwarded-proto'] || req.protocol;
    if (protocol == 'https') next();
    else { let from = `${protocol}://${req.hostname}${req.url}`; 
        let to = `https://ipu.r-e.kr${req.url}`;
        res.redirect(to); 
    }
});
*/
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
app.get('/sitemap.xml', (req, res)=>{
    res.setHeader('Content-Type', 'text/xml; chatset=utf-8').sendFile(__dirname+'/sitemap.xml');
});

try {
    auth.authRouter(app, __dirname);
    problem.problemRouter(app, __dirname);
    profile.profileRouter(app);
    mgr.manageRouter(app, __dirname);
    docs.docsRouter(app, __dirname);
    etc.etcRouter(app);
    usr.usrRouter(app);
    app.get('/noie', (req, res)=>{
        error.sendError(412, 'IE는 지원되지 않습니다.', res);
    })
}
catch(error) {
   console.log("Global Exception Catch: "+error)
}

app.use((req, res) => {
    error.sendError(404, 'Not Found', res);
});

/*
var options = {
    ca: fs.readFileSync('/etc/letsencrypt/live/ipu.r-e.kr/fullchain.pem'),
    key: fs.readFileSync('/etc/letsencrypt/live/ipu.r-e.kr/privkey.pem'),
    cert: fs.readFileSync('/etc/letsencrypt/live/ipu.r-e.kr/cert.pem')
};
*/

app.listen(HTTP_PORT);
console.log("HTTP server listening on port " + HTTP_PORT);
/*
https.createServer(options, app).listen(HTTPS_PORT, function() {
    console.log("HTTPS server listening on port " + HTTPS_PORT);
});
*/