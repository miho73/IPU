const auth = require('./auth');
const fs = require('fs');
const pg = require('pg');
const error = require('./error');
const prob = require('./problem');

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
                    if(auth.checkIdentity(req)) {
                        res.render("profile/user_profile.ejs", {
                            ylog: 'block',
                            nlog: 'none',
                            userid: user,
                            username: req.session.user.name,
                            usernameT: data['user_name'],
                            useridT: data['user_id'],
                            bioT: data['bio']
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
                            bioT: data['bio']
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
                if(auth.checkIdentity(req)) {
                    res.render("profile/user_profile.ejs", {
                        ylog: 'block',
                        nlog: 'none',
                        userid: req.session.user.id,
                        username: req.session.user.name,
                        usernameT: data['user_name'],
                        useridT: data['user_id'],
                        bioT: data['bio']
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
                        bioT: data['bio']
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
                        res.render('profile/profile_settings.ejs', {
                            ylog: 'block',
                            nlog: 'none',
                            userid: req.session.user.id,
                            username: req.session.user.name,
                            bio: data.bio,
                            email: data.email,
                            joined: `${jd.getFullYear()}년 ${(jd.getMonth()+1).zf(2)}월 ${jd.getDate().zf(2)}일 ${jd.getHours().zf(2)}시 ${jd.getMinutes().zf(2)}분`,
                            last_login: `${ll.getFullYear()}년 ${(ll.getMonth()+1).zf(2)}월 ${ll.getDate().zf(2)}일 ${ll.getHours().zf(2)}시 ${ll.getMinutes().zf(2)}분`,
                        });
                    }
                });
            }
        });
        app.post('/settings', (req, res)=>{
            if(!auth.checkIdentity(req)) {
                error.sendError(403, "Forbidden", res);
            }
            IdenDb.query('SELECT * FROM iden WHERE user_id=$1;', [req.session.user.id], (err, data) => {
                row = data.rows;
                if(row.length == 1) {
                    if(err || data.rowCount == 0) {
                        if(!sucess) {
                            error.sendError(403, "Forbidden", res);
                        }
                    }
                    else {
                        const buf = Buffer.from(row[0].user_salt, 'base64');
                        crypto.pbkdf2(req.body.lpwd, buf.toString('base64'), 12495, 64, 'sha512', (err, key) => {
                            if(row[0].user_password==key.toString('base64')) {
                                //Auth success
                                //name regex violent
                                if(!new RegExp('^[a-zA-Zㄱ-힣]{1,50}$').test(req.body.uname)) {
                                    res.render("../views/auth/signup.ejs", {
                                        'visible': 'inline-block',
                                        'why_failed': '이름은 한글과 영어의 조합이여야 합니다.'
                                    });
                                    return;
                                }
                                IdenDb.query('UPDATE iden ')
                            }
                            else {
                                error.sendError(403, "Forbidden", res);
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
            });
        });
        app.post('/profile/api/get-solved', (req, res)=>{
            let from = req.body.frm;
            let length = req.body.len;
            let regex = new RegExp('^[0-9]{1,2}$');
            if(from <= 0 || length<=0 || length>20 || !regex.test(from) || !regex.test(length)) {
                error.sendError(400, 'Bad Request', res);
                return;
            }
            auth.queryId(req.body.id, 'user_code', (err, datax)=>{
                if(err) {
                    error.sendError(404, 'Not Found', res);
                    return;
                }
                SolveDb.query(`SELECT code, problem_code, solved_time, solving_time, correct `+
                              `FROM (SELECT code, problem_code, solved_time, solving_time, correct FROM u${datax} ORDER BY code DESC) AS probs `+
                              `WHERE code>=$1 AND code<$2;`, [from, length+from], (err, data)=>{
                    if(err || data.rowCount == 0) {
                        console.log("problem table query error: "+err);
                        error.sendError(500, 'Internal Server Error', res);
                    }
                    else {
                        prob.probQuery([1, 5], (datap)=>{
                            rows = data.rows;
                            let dataprob = {};
                            datap.forEach(elem=>{
                                dataprob[elem.problem_code] = elem;
                            });
                            let ret = [];
                            rows.forEach(row => {
                                ret.push({
                                    code: row.code,
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
    }
}