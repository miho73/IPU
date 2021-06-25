const crypto = require('crypto');
const pg = require('pg');
const fs = require('fs');
const request = require('request');

const error = require('./error');

const dbconfig_json = fs.readFileSync(__dirname+'/db_config.json');
const dbconfig = JSON.parse(dbconfig_json).auth;
const dbconfig_solv = JSON.parse(dbconfig_json).solv;

const IdenDb = new pg.Client(dbconfig);
const SolveDb = new pg.Client(dbconfig_solv);
IdenDb.connect(err => {
    if (err) {
        console.log('Failed to connect to identification db: ' + err);
    }
    else {
        console.log('Connected to identification db');
    }
});
SolveDb.connect(err => {
    if (err) {
        console.log('Failed to connect to solve db(auth purpose): ' + err);
    }
    else {
        console.log('Connected to solve db(auth purpose)');
    }
});

IdenDb.query('CREATE TABLE IF NOT EXISTS iden('+
             'user_code BIGSERIAL NOT NULL PRIMARY KEY,'+
             'user_id TEXT NOT NULL,'+
             'user_name TEXT NOT NULL,'+
             'user_password TEXT NOT NULL,'+//to be hashed
             'user_salt TEXT NOT NULL,'+//to be hashed
             'invite_code TEXT NOT NULL,'+
             'bio TEXT,'+
             'privilege TEXT NOT NULL,'+
             'email TEXT,'+
             'joined TEXT NOT NULL,'+
             `experience INTEGER NOT NULL,`+
             `aes_iv TEXT,`+
             `last_solve TEXT,`+
             `last_login TEXT);`, (err, data)=>{
                if(err) {
                    console.log('Failed to create table to identification database: '+err);
                }
             });

function CheckIdentity(req) {
    if(req.session == undefined) return false;
    else if(req.session.user) return true;
    else return false;
}

function QueryId(id, toget, callback) {
    IdenDb.query("SELECT * FROM iden WHERE user_id=$1;", [id], (err, data)=>{
        if(err || data.rowCount == 0) {
            console.log('queryId error: '+err);
            callback(true, undefined);
        }
        else {
            callback(err, data.rows[0][toget]);
        }
    });
}

function AllQueryId(id, callback) {
    IdenDb.query("SELECT * FROM iden WHERE user_id=$1;", [id], (err, data)=>{
        if(err || data.rowCount == 0) {
            console.log('allQueryId error: '+err);
            callback(true, undefined);
        }
        else {
            callback(err, data.rows[0]);
        }
    });
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
    authRouter: function(app, rtd) {
        function inviteCodeCheck(code, callback) {
            try {
                fs.readFile(rtd+'/invites.json', (err, data)=>{
                    if(err) {
                        console.log("invite code error: "+err);
                        callback(false);
                    }
                    else {
                        let obj = JSON.parse(data);
                        if(obj.codes.includes(code)) callback(true);
                        else callback(false);
                    }
                });
            }
            catch(error) {
                console.log("invites code checker error(catch): "+error);
                callback(false);
            }
        }
        //Release login Request Handler
        app.get('/login/deauth', (req, res)=>{
            if (CheckIdentity(req)) {
                req.session.destroy(
                    function (err) {
                        if (err) {
                            error.sendError(500, 'INTERNAL SERVER ERROR', res);
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
                res.render("auth/signin.ejs", {
                    'visible': 'block',
                    'ret': `${ret}`,
                    'capt_site': '6LefxLwaAAAAAOUYGq_6QLMbRMmGJZlQUQAcw-7u',
                    'capt2': 'none',
                });
                return;
            }
            let secret_key;
            let token;
            if(req.body.gVers == 'block') {
                secret_key = fs.readFileSync(__dirname+'/private/recaptcha_secret_v2.key');
                token = req.body['g-recaptcha-response'];
            }
            else {
                secret_key = fs.readFileSync(__dirname+'/private/recaptcha_secret.key');
                token = req.body['gtoken'];
            }
            const url = `https://www.google.com/recaptcha/api/siteverify?secret=${secret_key}&response=${token}`;
            request(url, function(err, resp, body) {
                if(err) {
                    res.render("auth/signin.ejs", {
                        'visible': 'block',
                        'ret': `${ret}`,
                        'capt_site': '6LefxLwaAAAAAOUYGq_6QLMbRMmGJZlQUQAcw-7u',
                        'capt2': 'none',
                    });
                    console.log('CAPTCHA ERROR:'+err);
                    return;
                }
                pbody = JSON.parse(body);
                if(!pbody.success) {
                    res.render("auth/signin.ejs", {
                        'visible': 'block',
                        'ret': `${ret}`,
                        'capt_site': '6LeoxLwaAAAAABC-oJu76Dt36Yb5K12Eu7a0pjD8',
                        'capt2': 'block',
                    });
                    return;
                }
                if(!new RegExp('^[a-zA-Z0-9]{1,50}$').test(req.body.u)) {
                    res.render("auth/signin.ejs", {
                        'visible': 'block',
                        'ret': `${ret}`,
                        'capt_site': '6LefxLwaAAAAAOUYGq_6QLMbRMmGJZlQUQAcw-7u',
                        'capt2': 'none',
                    });
                    return;
                }
                var sucess = false;
                IdenDb.query('SELECT * FROM iden WHERE user_id=$1;', [req.body.u], (err, data) => {
                    row = data.rows;
                    if(row.length == 1) {
                        if(err || data.rowCount == 0) {
                            if(!sucess) {
                                res.render("auth/signin.ejs", {
                                    'visible': 'block',
                                    'ret': `${ret}`,
                                    'capt_site': '6LefxLwaAAAAAOUYGq_6QLMbRMmGJZlQUQAcw-7u',
                                    'capt2': 'none',
                                });
                            }
                        }
                        else {
                            const buf = Buffer.from(row[0].user_salt, 'base64');
                            crypto.pbkdf2(req.body.p, buf.toString('base64'), 12495, 64, 'sha512', (err, key) => {
                                if(row[0].user_password==key.toString('base64')) {
                                    IdenDb.query('UPDATE iden SET last_login=$1 WHERE user_code=$2', [new Date().toISOString(), row[0].user_code], (err, datre) =>{
                                        if(err) {
                                            res.render("auth/signin.ejs", {
                                                'visible': 'block',
                                                'ret': `${ret}`,
                                                'capt_site': '6LefxLwaAAAAAOUYGq_6QLMbRMmGJZlQUQAcw-7u',
                                                'capt2': 'none',
                                            });
                                        }
                                        else {
                                            req.session.user = {
                                                code: row[0].user_code,
                                                id: row[0].user_id,
                                                name: row[0].user_name,
                                                auth: true
                                            };
                                            res.redirect(`/${req.body.ret}`);
                                            sucess = true;
                                        }
                                    });
                                }
                                else {
                                    if(!sucess) {
                                        res.render("auth/signin.ejs", {
                                            'visible': 'block',
                                            'ret': `${ret}`,
                                            'capt_site': '6LefxLwaAAAAAOUYGq_6QLMbRMmGJZlQUQAcw-7u',
                                            'capt2': 'none',
                                        });
                                    }
                                }
                            });
                        }
                    }
                    else {
                        res.render("auth/signin.ejs", {
                            'visible': 'block',
                            'ret': `${ret}`,
                            'capt_site': '6LefxLwaAAAAAOUYGq_6QLMbRMmGJZlQUQAcw-7u',
                            'capt2': 'none',
                        });
                    }
                });
            });
        });
        //Invite code checker
        app.post('/auth/invite', (req, res)=>{
            if(req.body.code == undefined) res.send(`{"status":0}`);
            inviteCodeCheck(req.body.code, (resq)=>{
                if(resq) {
                    res.send(`{"status":1}`);
                }
                else {
                    res.send(`{"status":0}`);
                }
            });
        });
        //Signup Post Handler; id, password, name, invite
        app.post('/signup', (req, res)=>{
            const secret_key = fs.readFileSync(__dirname+'/private/recaptcha_secret_v2.key');
            const token = req.body['g-recaptcha-response'];
            const url = `https://www.google.com/recaptcha/api/siteverify?secret=${secret_key}&response=${token}`;
            request(url, function(err, resp, body) {
                if(err) {
                    res.render("auth/signup.ejs", {
                        'visible': 'inline-block',
                        'why_failed': '일시적으로 reCAPTCHA 서버에 접속할 수 없습니다.'
                    });
                    return;
                }
                pbody = JSON.parse(body);
                if(!pbody.success) {
                    res.render("auth/signup.ejs", {
                        'visible': 'inline-block',
                        'why_failed': '다시 시도해주세요.'
                    });
                    return;
                }
                //invalid invite code
                inviteCodeCheck(req.body.invite, (qwer)=>{
                    if(!qwer) {
                        res.render("auth/signup.ejs", {
                            'visible': 'inline-block',
                            'why_failed': '초대코드를 확인할 수 없습니다.'
                        });
                        return;
                    }
                    //id regex violent
                    if(!new RegExp('^[a-zA-Z0-9]{1,50}$').test(req.body.id)) {
                        res.render("auth/signup.ejs", {
                            'visible': 'inline-block',
                            'why_failed': 'ID는 글자(a-z, A-Z)와 숫자(0-9)로만 이루어져 있어야 합니다.'
                        });
                        return;
                    }
                    //password regex violent
                    if(!new RegExp('^[a-zA-Z0-9]{4,100}$').test(req.body.password)) {
                        res.render("auth/signup.ejs", {
                            'visible': 'inline-block',
                            'why_failed': '암호는 4자리 이상, 100자리 이하여야 하며, 글자(a-z, A-Z)와 숫자(0-9)로 만 이루어져 있어야 합니다.'
                        });
                        return;
                    }
                    //name regex violent
                    if(!new RegExp('^[a-zA-Zㄱ-힣]{1,50}$').test(req.body.name)) {
                        res.render("auth/signup.ejs", {
                            'visible': 'inline-block',
                            'why_failed': '이름은 한글과 영어의 조합이여야 합니다.'
                        });
                        return;
                    }
                    //check if id is duplicated
                    IdenDb.query('SELECT user_code FROM iden WHERE user_id=$1', [req.body.id], (err, data)=>{
                        if(err || data.rowCount != 0) {
                            res.render("auth/signup.ejs", {
                                'visible': 'inline-block',
                                'why_failed': '해당 ID는 이미 사용중입니다.'
                            });
                        }
                        else {
                            crypto.randomBytes(64, (errq, buf) => {
                                crypto.pbkdf2(req.body.password, buf.toString('base64'), 12495, 64, 'sha512', (err, key) => {
                                    IdenDb.query('BEGIN', (err1, res1)=>{
                                        IdenDb.query(`INSERT INTO iden(user_id, user_name, user_password, user_salt, invite_code, bio, privilege, joined, experience) `+
                                                 `values ($1, $2, $3, $4, $5, '', 'u', $6, 0)`,
                                                 [req.body.id, req.body.name, key.toString('base64'), buf.toString('base64'), req.body.invite, new Date().toISOString()], (err, resx)=>{
                                            if(errq) {
                                                IdenDb.query('ROLLBACK');
                                                res.render("auth/signup.ejs", {
                                                    'visible': 'inline-block',
                                                    'why_failed': '지금은 서비스를 사용할 수 없습니다. 잠시 후 다시 시도해주세요.'
                                                });
                                            }
                                            else {
                                                QueryId(req.body.id, 'user_code', (errx, datre) =>{
                                                    if(errx) {
                                                        IdenDb.query('ROLLBACK');
                                                        res.render("auth/signup.ejs", {
                                                            'visible': 'inline-block',
                                                            'why_failed': '지금은 서비스를 사용할 수 없습니다. 잠시 후 다시 시도해주세요.'
                                                        });
                                                        return;
                                                    }
                                                    SolveDb.query(`CREATE TABLE IF NOT EXISTS u${datre}(`+
                                                                  'code BIGSERIAL NOT NULL PRIMARY KEY,'+
                                                                  'problem_code INTEGER NOT NULL,'+
                                                                  'solved_time TEXT NOT NULL,'+
                                                                  'solving_time TEXT NOT NULL,'+
                                                                  'correct BOOLEAN NOT NULL);', (errp, resx)=>{
                                                        if(errp) {
                                                            console.log('Failed to create solves table for user id: '+req.session.user.code+'. '+err);
                                                            IdenDb.query('ROLLBACK');
                                                            error.sendError(500, 'Internal Server Error', res);
                                                        }
                                                        else {
                                                            IdenDb.query('COMMIT');
                                                            res.redirect('/login');
                                                        }
                                                    });
                                                });
                                            }
                                        });
                                    });
                                });
                            });
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
                res.render("auth/signin.ejs", {
                    'visible': 'none',
                    'ret': `${ret}`,
                    'capt_site': '6LefxLwaAAAAAOUYGq_6QLMbRMmGJZlQUQAcw-7u',
                    'capt2': 'none'
                });
            }            
        });
        app.get('/signup', (req, res)=>{
            res.render("auth/signup.ejs", {
                'visible': 'none',
                'why_failed': ''
            });
        });
    },
    queryId: QueryId,
    allQueryId: AllQueryId,
    query: function(q, p, callback) {
        IdenDb.query(q, p, callback);
    }
}