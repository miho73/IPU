const auth = require('./auth');
const fs = require('fs');
const pg = require('pg');
const error = require('./error');
const prob = require('./problem');
const crypto = require('crypto');
const sanitizeHtml = require('sanitize-html');

const dbconfig_json = fs.readFileSync(__dirname+'/db_config.json');
const dbconfig_solv = JSON.parse(dbconfig_json).solv;
const SolveDb = new pg.Client(dbconfig_solv);
SolveDb.connect(err => {
    if (err) {
        console.log('Failed to connect to solve db(profile purpose): ' + err);
    }
    else {
        console.log('Connected to solve db(profile purpose)');
    }
});

String.prototype.string = function(len){var s = '', i = 0; while (i++ < len) { s += this; } return s;};
String.prototype.zf = function(len){return "0".string(len - this.length) + this;};
Number.prototype.zf = function(len){return this.toString().zf(len);};

function validateEmail(email) {
    const re = /^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    return re.test(String(email).toLowerCase());
}

module.exports = {
    profileRouter: function(app) {
        app.get('/profile', (req, res)=>{
            if(auth.checkIdentity(req)) {
                user = req.session.user.id;
                auth.allQueryId(user, (err, data)=>{
                    if(err) {
                        error.sendError(404, 'Not Found', res);
                        return;
                    }
                    let page = 0;
                    if(req.query.page != undefined && req.query.page >= 0) {
                        page = req.query.page;
                    }
                    if(auth.checkIdentity(req)) {
                        res.render("profile/user_profile.ejs", {
                            ylog: 'block',
                            nlog: 'none',
                            userid: user,
                            username: req.session.user.name,
                            usernameT: data['user_name'],
                            useridT: data['user_id'],
                            bioT: data['bio'],
                            experience: data['experience'],
                            pg: page
                        });
                    }
                    else {
                        res.render("profile/user_profile.ejs", {
                            ylog: 'block',
                            nlog: 'none',
                            userid: '',
                            username: '',
                            usernameT: data['user_name'],
                            useridT: data['user_id'],
                            bioT: data['bio'],
                            experience: data['experience'],
                            pg: page
                        });
                    }
                });
            }
            else {
                res.redirect('/login/?ret=profile');
            }
        });
        app.get('/profile/:user', (req, res)=>{
            auth.allQueryId(req.params.user, (err, data)=>{
                if(err) {
                    error.sendError(404, 'Not Found', res);
                    return;
                }
                let page = 0;
                if(req.query.page != undefined && req.query.page >= 0) {
                    page = req.query.page;
                }
                if(auth.checkIdentity(req)) {
                    res.render("profile/user_profile.ejs", {
                        ylog: 'block',
                        nlog: 'none',
                        userid: req.session.user.id,
                        username: req.session.user.name,
                        usernameT: data['user_name'],
                        useridT: data['user_id'],
                        bioT: data['bio'],
                        experience: data['experience'],
                        pg: page
                    });
                }
                else {
                    res.render("profile/user_profile.ejs", {
                        ylog: 'none',
                        nlog: 'block',
                        userid: '',
                        username: '',
                        usernameT: data['user_name'],
                        useridT: data['user_id'],
                        bioT: data['bio'],
                        experience: data['experience'],
                        pg: page
                    });
                }
            });
        });
        app.get('/settings', (req, res)=>{
            if(!auth.checkIdentity(req)) {
                res.redirect('/login/?ret=settings');
            }
            else {
                auth.allQueryId(req.session.user.id, (err, data)=>{
                    if(err) {
                        error.sendError(500, 'Internal Server Error', res);
                        return;
                    }
                    else {
                        let jd = new Date(data.joined), ll = new Date(data.last_login);
                        let emailx = '';
                        if(data.email != '' && data.email != undefined) {
                            const secretByte = Buffer.from(crypto.createHash('md5').update(req.session.user.pwd).digest('hex'));
                            const cipher = crypto.createDecipheriv('aes-256-cbc', secretByte, Buffer.from(data.aes_iv, 'hex'));
                            emailx = cipher.update(data.email, 'base64', 'utf8');
                            emailx += cipher.final('utf8');
                        }
                        res.render('profile/profile_settings.ejs', {
                            ylog: 'block',
                            nlog: 'none',
                            userid: req.session.user.id,
                            username: req.session.user.name,
                            bio: data.bio,
                            email: emailx,
                            joined: `${jd.getFullYear()}년 ${(jd.getMonth()+1).zf(2)}월 ${jd.getDate().zf(2)}일 ${jd.getHours().zf(2)}시 ${jd.getMinutes().zf(2)}분 UTC`,
                            last_login: `${ll.getFullYear()}년 ${(ll.getMonth()+1).zf(2)}월 ${ll.getDate().zf(2)}일 ${ll.getHours().zf(2)}시 ${ll.getMinutes().zf(2)}분 UTC`,
                        });
                    }
                });
            }
        });
        app.get('/settings/gbye', (req, res)=>{
            if(auth.checkIdentity(req)) {
                res.render('profile/goodbye.ejs', {
                    ylog: 'block',
                    nlog: 'none',
                    userid: req.session.user.id,
                    username: req.session.user.name,
                    fail: 'none',
                    whyfail: ''
                });
            }
            else {
                res.redirect('/login/?ret=settings/gbye');
            }
        });
        app.post('/settings', (req, res)=>{
            if(!auth.checkIdentity(req)) {
                error.sendError(403, "Forbidden", res);
            }
            auth.query('SELECT * FROM iden WHERE user_id=$1;', [req.session.user.id], (err, data) => {
                try {
                row = data.rows;
                if(row.length == 1) {
                    if(err || data.rowCount == 0) {
                        if(!sucess) {
                            error.sendError(403, "Forbidden", res);
                        }
                    }
                    else {
                        const buf = Buffer.from(row[0].user_salt, 'base64');
                        if(req.body.lpwd == undefined || !new RegExp('^[a-zA-Z0-9]{4,100}$').test(req.body.lpwd)) {
                            res.status(400).send('pwd');
                            return;
                        }
                        crypto.pbkdf2(req.body.lpwd, buf.toString('base64'), 12495, 64, 'sha512', (err, key) => {
                            if(row[0].user_password==key.toString('base64')) {
                                //Auth success
                                //name regex violent
                                let uname = req.body.name;
                                let bio = req.body.bio;
                                let email = req.body.email, ema, IV;
                                if(bio.len >= 50) {
                                    res.status(400).send('name');
                                    return;
                                }
                                if(bio.length >= 500) {
                                    res.status(400).send('bio');
                                    return;
                                }
                                if(!validateEmail(email) && email != '') {
                                    res.status(400).send('email');
                                    return;
                                }
                                if(email != '') {
                                    let secretByte;
                                    if(req.body.pwdC == 'true') {
                                        secretByte = Buffer.from(crypto.createHash('md5').update(req.body.npwd).digest('hex'));
                                    }
                                    else {
                                        secretByte = Buffer.from(crypto.createHash('md5').update(req.body.lpwd).digest('hex'));
                                    }
                                    const iv = crypto.randomBytes(16);
                                    const cipher = crypto.createCipheriv('aes-256-cbc', secretByte, iv);
                                    let enc = cipher.update(email, 'utf8', 'base64');
                                    enc += cipher.final('base64');
                                    ema = enc;
                                    IV = iv.toString('hex');
                                }
                                else {
                                    ema = '';
                                    IV = '';
                                }
                                if(req.body.pwdC == 'true') {
                                    //password regex violent
                                    if(req.body.npwd == undefined || !new RegExp('^[a-zA-Z0-9]{4,100}$').test(req.body.npwd)) {
                                        res.status(400).send('pwdf');
                                        return;
                                    }
                                    crypto.randomBytes(64, (errq, bufx) => {
                                        crypto.pbkdf2(req.body.npwd, bufx.toString('base64'), 12495, 64, 'sha512', (err, keyp) => {
                                            auth.query('UPDATE iden SET user_name=$1, bio=$2, email=$3, user_password=$4, user_salt=$5, aes_iv=$6 WHERE user_code=$7', [sanitizeHtml(uname), sanitizeHtml(bio), ema, keyp.toString('base64'), bufx.toString('base64'), IV, req.session.user.code], (err, resp)=>{
                                                if(err) {
                                                    res.status(500).send('dbupdate-pwd');
                                                }
                                                else {
                                                    req.session.user.name = uname;
                                                    res.sendStatus(200);
                                                }
                                            });
                                        });
                                    });
                                }
                                else {
                                    auth.query('UPDATE iden SET user_name=$1, bio=$2, email=$3, aes_iv=$4 WHERE user_code=$5', [uname, sanitizeHtml(bio), ema, IV, req.session.user.code], (err, resp)=>{
                                        if(err) {
                                            res.status(500).send('dbupdate-npwd');
                                        }
                                        else {
                                            req.session.user.name = uname;
                                            res.sendStatus(200);
                                        }
                                    });
                                }
                            }
                            else {
                                res.status(403).send('pwd');
                            }
                        });
                    }
                }
                else {
                    res.render("../views/auth/signin.ejs", {
                        'visible': 'block',
                        'ret': `${ret}`,
                        'capt_site': '6LeldLYaAAAAAF2LqYQgHiq_SwPTXIAvQPBvWGWc',
                        'capt2': 'none',
                    });
                }
                }
                catch {
                    res.sendStatus(500);
                }
            });
        });
        app.post('/settings/gbye', (req, res)=>{
            try{
            if(!auth.checkIdentity(req)) {
                error.sendError(403, 'Forbidden', res);
                return;
            }
            auth.query('SELECT * FROM iden WHERE user_code=$1;', [req.session.user.code], (err, data) => {
                row = data.rows;
                if(row.length == 1) {
                    if(err || data.rowCount == 0) {
                        res.render('profile/goodbye.ejs', {
                            ylog: 'block',
                            nlog: 'none',
                            userid: req.session.user.id,
                            username: req.session.user.name,
                            fail: 'block',
                            whyfail: '사용자를 찾을 수 없습니다.'
                        });
                    }
                    else {
                        const buf = Buffer.from(row[0].user_salt, 'base64');
                        crypto.pbkdf2(req.body.pwd, buf.toString('base64'), 12495, 64, 'sha512', (errp, key) => {
                            if(row[0].user_password==key.toString('base64')) {
                                if(!errp) {
                                    auth.query("DELETE FROM iden WHERE user_code=$1;", [req.session.user.code], (err, response)=>{
                                        SolveDb.query(`DROP TABLE u${req.session.user.code};`, [], (resxp)=>{
                                            if(resxp) {
                                                res.render('profile/goodbye.ejs', {
                                                    ylog: 'block',
                                                    nlog: 'none',
                                                    userid: req.session.user.id,
                                                    username: req.session.user.name,
                                                    fail: 'block',
                                                    whyfail: '계정 데이터를 삭제할 수 없습니다.'
                                                });
                                            }
                                            else {
                                                req.session.destroy(
                                                    function (err) {
                                                        if (err) {
                                                            error.sendError(500, 'INTERNAL SERVER ERROR', res);
                                                            return;
                                                        }
                                                    }
                                                );
                                                res.render('profile/realbye.ejs');
                                            }
                                        });
                                    });
                                }
                                else {
                                    res.render('profile/goodbye.ejs', {
                                        ylog: 'block',
                                        nlog: 'none',
                                        userid: req.session.user.id,
                                        username: req.session.user.name,
                                        fail: 'block',
                                        whyfail: '암호를 처리할 수 없습니다.'
                                    });
                                }
                            }
                            else {
                                res.render('profile/goodbye.ejs', {
                                    ylog: 'block',
                                    nlog: 'none',
                                    userid: req.session.user.id,
                                    username: req.session.user.name,
                                    fail: 'block',
                                    whyfail: '잘못된 암호입니다.'
                                });
                            }
                        });
                    }
                }
                else {
                    res.render('profile/goodbye.ejs', {
                        ylog: 'block',
                        nlog: 'none',
                        userid: req.session.user.id,
                        username: req.session.user.name,
                        fail: 'block',
                        whyfail: '사용자를 식별할 수 없습니다.'
                    });
                }
            });
            }
            catch {
                res.render('profile/goodbye.ejs', {
                    ylog: 'block',
                    nlog: 'none',
                    userid: req.session.user.id,
                    username: req.session.user.name,
                    fail: 'block',
                    whyfail: '요청을 처리할 수 없습니다. 잠시 후 다시 시도해주세요.'
                });
            }
        });
        app.post('/profile/api/get-solved', (req, res)=>{
            let from = req.body.frm;
            let length = req.body.len;
            let regex = new RegExp('^[0-9]{1,2}$');
            if(from <= 0 || length<=0 || length>50 || !regex.test(from) || !regex.test(length)) {
                error.sendError(400, 'Bad Request', res);
                return;
            }
            auth.queryId(req.body.id, 'user_code', (err, datax)=>{
                if(err) {
                    error.sendError(404, 'Not Found', res);
                    return;
                }
                SolveDb.query(`SELECT code, problem_code, solved_time, solving_time, correct `+
                              `FROM u${datax} `+
                              `WHERE code<=((SELECT code FROM u${datax} ORDER BY code DESc LIMIT 1)-$1) `+
                              `ORDER BY code DESC LIMIT $2;`, [from-1, length], (err, data)=>{
                    if(err) {
                        console.log("problem table query error: "+err);
                        error.sendError(500, 'Internal Server Error', res);
                    }
                    else if(data.rowCount == 0) {
                        res.send([]);
                    }
                    else {
                        let toget = [];
                        data.rows.forEach(ele=>{
                            toget.push(ele.problem_code);
                        });
                        prob.probQuery(toget, (datap)=>{
                            rows = data.rows;
                            let dataprob = {};
                            datap.forEach(elem=>{
                                dataprob[elem.problem_code] = elem;
                            });
                            let ret = [];
                            rows.forEach(row => {
                                ret.push({
                                    code: row.problem_code,
                                    name: dataprob[row['problem_code']]['problem_name'],
                                    sol: row.solved_time,
                                    solt: row.solving_time,
                                    cor: row.correct,
                                    tags: [
                                        {
                                            key:"cate",
                                            content: dataprob[row['problem_code']]['problem_category']
                                        },
                                        {
                                            key:"diff",
                                            content: dataprob[row['problem_code']]['problem_difficulty']
                                        }
                                    ]
                                });
                            });
                            res.send(ret);
                        });
                    }
                });
            });
        });
    },
    solvesQuery: function(query, paramenter, callback) {
        SolveDb.query(query, paramenter, (err, res)=>{
            callback(err, res);
        });
    }
}