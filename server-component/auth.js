const sqlite3 = require('sqlite3').verbose();
const crypto = require('crypto');
const pg = require('pg');
const fs = require('fs');

const dbconfig_json = fs.readFileSync(__dirname+'/db_config.json');
const dbconfig = JSON.parse(dbconfig_json).auth;

const IdenDb = new pg.Client(dbconfig);
IdenDb.connect(err => {
    if (err) {
        console.log('Failed to connect to identification db: ' + err);
    }
    else {
        console.log('Connected to identification db');
    }
});

IdenDb.query('CREATE TABLE IF NOT EXISTS iden('+
             'user_code BIGSERIAL NOT NULL PRIMARY KEY,'+
             'user_id TEXT NOT NULL,'+
             'user_name TEXT NOT NULL,'+
             'user_password TEXT NOT NULL,'+
             'user_salt TEXT NOT NULL,'+
             'invite_code TEXT NOT NULL,'+
             'bio TEXT,'+
             'privilege TEXT NOT NULL);', (err, data)=>{
                if(err) {
                    console.log('Failed to create table to identification database: '+err);
                }
             });

function CheckIdentity(req) {
    if(req.session == undefined) return false;
    else if(req.session.user) return true;
    else return false;
}

module.exports = {
    randomString: function randomString(length) {
        var result           = [];
        var characters       = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
        var charactersLength = characters.length;
        for ( var i = 0; i < length; i++ ) {
          result.push(characters.charAt(Math.floor(Math.random() * charactersLength)));
       }
       return result.join('');
    },
    checkIdentity: CheckIdentity,
    authRouter: function(app) {
        //Release login Request Handler
        app.get('/login/deauth', (req, res)=>{
            if (CheckIdentity(req)) {
                req.session.destroy(
                    function (err) {
                        if (err) {
                            sendError(500, 'INTERNAL SERVER ERROR', res);
                            return;
                        }
                    }
                );
            }
            res.redirect("/");
        });
        //Login Post Handler; u, p
        app.post('/login', (req, res)=>{
            let ret = '';
            if(req.query.ret != undefined) ret = req.query.ret;
            if(req.body.u == undefined || req.body.p == undefined) {
                res.render("../views/auth/signin.ejs", {
                    'visible': 'block',
                    'ret': `/${ret}`
                });
                return;
            }
            if(!new RegExp('^[a-zA-Z0-9]{1,50}$').test(req.body.u)) {
                res.render("../views/auth/signin.ejs", {
                    'visible': 'block',
                    'ret': `/${ret}`
                });
                return;
            }
            var sucess = false;
            IdenDb.query('SELECT * FROM iden WHERE user_id=$1;', [req.body.u], (err, data) => {
                row = data.rows;
                if(row.length == 1) {
                    if(err) {
                        if(!sucess) {
                            res.render("../views/auth/signin.ejs", {
                                'visible': 'block',
                                'ret': `/${ret}`
                            });
                        }
                    }
                    else {
                        const buf = Buffer.from(row[0].user_salt, 'base64');
                        crypto.pbkdf2(req.body.p, buf.toString('base64'), 12495, 64, 'sha512', (err, key) => {
                            if(row[0].user_password==key.toString('base64')) {
                                req.session.user = {
                                    id: row[0].user_id,
                                    pwd: req.body.p,
                                    name: row[0].user_name,
                                    prev: row[0].privilege,
                                    auth: true
                                };
                                res.redirect(`${req.body.ret}`);
                                sucess = true;
                            }
                            else {
                                if(!sucess) {
                                    res.render("../views/auth/signin.ejs", {
                                        'visible': 'block',
                                        'ret': `/${ret}`
                                    });
                                }
                            }
                        });
                    }
                }
                else {
                    res.render("../views/auth/signin.ejs", {
                        'visible': 'block',
                        'ret': `/${ret}`
                    });
                }
            });
        });
        function inviteCodeCheck(code) {
            //TODO: implement
            if(code == "1234") return true;
            return false
        }
        //Invite code checker
        app.post('/auth/invite', (req, res)=>{
            if(req.body.code == undefined) res.send(`{"status":0}`);
            if(inviteCodeCheck(req.body.code)) {
                res.send(`{"status":1}`);
            }
            else {
                res.send(`{"status":0}`);
            }
        })
        //Signup Post Handler; id, password, name, invite
        app.post('/signup', (req, res)=>{
            //invalid invite code
            if(!inviteCodeCheck(req.body.invite)) {
                res.render("../views/auth/signup.ejs", {
                    'visible': 'inline-block'
                });
            }
            //id regex violent
            if(!new RegExp('^[a-zA-Z0-9]{1,50}$').test(req.body.id)) {
                res.render("../views/auth/signup.ejs", {
                    'visible': 'inline-block'
                });
                return;
            }
            //password regex violent
            if(!new RegExp('^[a-zA-Z0-9]{4,50}$').test(req.body.password)) {
                res.render("../views/auth/signup.ejs", {
                    'visible': 'inline-block'
                });
                return;
            }
            //name regex violent
            if(!new RegExp('^[a-zA-Z가-힣]{1,50}$').test(req.body.name)) {
                res.render("../views/auth/signup.ejs", {
                    'visible': 'inline-block'
                });
                return;
            }
            crypto.randomBytes(64, (err, buf) => {
                crypto.pbkdf2(req.body.password, buf.toString('base64'), 12495, 64, 'sha512', (err, key) => {
                IdenDb.query(`INSERT INTO iden(user_id, user_name, user_password, user_salt, invite_code, bio, privilege) `+
                             `values ($1, $2, $3, $4, $5, '', 'u')`,
                             [req.body.id, req.body.name, key.toString('base64'), buf.toString('base64'), req.body.invite], (err, res)=>{
                                 if(err) {
                                    res.render("../views/auth/signup.ejs", {
                                        'visible': 'inline-block'
                                    });
                                 }
                                 else {
                                    res.redirect('/');
                                 }
                             });
                });
            });
        });
        app.get('/login', (req, res)=>{
            if(CheckIdentity(req)) {
                res.redirect('/');
            }
            else {
                let ret = ''
                if(req.query.ret != undefined) ret = req.query.ret;
                res.render("../views/auth/signin.ejs", {
                    'visible': 'none',
                    'ret': `/${ret}`
                });
            }            
        });
        app.get('/signup', (req, res)=>{
            res.render("../views/auth/signup.ejs", {
                'visible': 'none'
            });
        });
    }
}