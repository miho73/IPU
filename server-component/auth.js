const sqlite3 = require('sqlite3').verbose();
const crypto = require('crypto');

let IdenDb = new sqlite3.Database('./db/iden.db', sqlite3.OPEN_READWRITE, (err) => {
    if (err) {
        console.error(err.message);
    } else {
        console.log('Connected to the IDENTIFICATION database.');
    }
});
IdenDb.serialize(()=>{
    IdenDb.each('CREATE TABLE IF NOT EXISTS iden('+
                'user_code INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,'+
                'user_id TEXT NOT NULL,'+
                'user_name TEXT NOT NULL,'+
                'user_password TEXT NOT NULL,'+
                'user_salt TEXT NOT NULL,'+
                'invite_code TEXT NOT NULL,'+
                'privilege TEXT NOT NULL);');
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
            if(req.body.u == undefined || req.body.p == undefined) {
                res.render("../views/auth/signin.ejs", {
                    'visible': 'block'
                });
                return;
            }
            if(!new RegExp('^[a-zA-Z0-9]{1,50}$').test(req.body.u)) {
                res.render("../views/auth/signin.ejs", {
                    'visible': 'block'
                });
                return;
            }
            var sucess = false;
            IdenDb.all('SELECT * FROM iden WHERE user_id=?;', [req.body.u], (err, row) => {
                if(row.length == 1) {
                    if(err) {
                        if(!sucess) {
                            res.render("../views/auth/signin.ejs", {
                                'visible': 'block'
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
                                res.redirect("/");
                                sucess = true;
                            }
                            else {
                                if(!sucess) {
                                    res.render("../views/auth/signin.ejs", {
                                        'visible': 'block'
                                    });
                                }
                            }
                        });
                    }
                }
                else {
                    res.render("../views/auth/signin.ejs", {
                        'visible': 'block'
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
                res.send(`{"status":1,"secret":"123"}`);
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
                IdenDb.run(`INSERT INTO iden(user_id, user_name, user_password, user_salt, invite_code, privilege) `+
                           `values (?, ?, ?, ?, ?, "usr")`,
                           [req.body.id, req.body.name, key.toString('base64'), buf.toString('base64'), req.body.invite]);
                });
            });
            res.redirect('/');
        });
        app.get('/login', (req, res)=>{
            if(CheckIdentity(req)) {
                res.redirect('/');
            }
            else {
                res.render("../views/auth/signin.ejs", {
                    'visible': 'none'
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