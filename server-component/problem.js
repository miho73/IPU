const e = require('express');
const multer = require('multer');
const auth = require('./auth');
const error = require('./error');
const perm = require('./permission');
const experi = require('./experience');
const upload = multer({dest: 'problem/lib'});
const pg = require('pg');
const fs = require('fs');

const dbconfig_json = fs.readFileSync(__dirname+'/db_config.json');
const dbconfig_prob = JSON.parse(dbconfig_json).prob;
const dbconfig_solv = JSON.parse(dbconfig_json).solv;

const ProbDb = new pg.Client(dbconfig_prob);
const SolveDb = new pg.Client(dbconfig_solv);
ProbDb.connect(err => {
    if (err) {
        console.log('Failed to connect to problem db: ' + err);
    }
    else {
        console.log('Connected to problem db');
    }
});
SolveDb.connect(err => {
    if (err) {
        console.log('Failed to connect to solve db(problem solving purpose): ' + err);
    }
    else {
        console.log('Connected to solve db (problem solving purpose)');
    }
});
ProbDb.query('CREATE TABLE IF NOT EXISTS prob('+
             'problem_code BIGSERIAL NOT NULL PRIMARY KEY,'+
             'problem_name TEXT NOT NULL,'+
             'problem_category TEXT NOT NULL,'+
             'problem_difficulty TEXT NOT NULL,'+
             'problem_content TEXT NOT NULL,'+
             'problem_solution TEXT NOT NULL,'+
             'problem_answer TEXT NOT NULL,'+
             'problem_hint TEXT,'+
             'has_hint INTEGER NOT NULL,'+
             'author_name TEXT NOT NULL,'+
             'added_at TEXT NOT NULL,'+
             'last_modified TEXT NOT NULL,'+
             'answers INTEGER NOT NULL,'+
             'extr_tabs TEXT NOT NULL);', (err, data)=>{
                if(err) {
                    console.log('Failed to create table to problem database: '+err);
                }
             });

module.exports = {
    problemRouter: function(app, dirname) {
        app.get('/problem/make', (req, res)=>{
            if(auth.checkIdentity(req)) {
                perm.checkPrivilege(req, ['p', 'm'], (rex)=>{
                    if(rex) {
                        res.render('../views/problem/mk_problem.ejs', {
                            ylog: "block",
                            nlog: "none",
                            userid: req.session.user.id,
                            username: req.session.user.name,
                        });
                    }
                    else {
                        error.sendError(403, 'Forbidden', res);
                    }
                })
            }
            else {
                res.redirect('/login/?ret=problem/make');
            }
        });
        app.get('/problem/:code/edit', (req, res)=>{
            perm.checkPrivilege(req, ['p', 'm'], (rex)=>{
                if(rex) {
                    let regex = new RegExp('^[0-9]{1,4}$');
                    let code = req.params.code;
                    if(!regex.test(code)) {
                        error.sendError(400, 'Bad Request', res);
                        return;
                    }
                    res.render('../views/problem/edit_problem.ejs', {
                        ylog: "block",
                        nlog: "none",
                        userid: req.session.user.id,
                        username: req.session.user.name,
                        prob_code: code,
                    });
                }
                else {
                    res.redirect(`/problem/${req.params.code}`);
                }
            });
        });
        app.post('/problem/edit/getd', (req, res)=>{
            const code = req.body.code;
            if(code == undefined) {
                res.status(400).send('cu');
                return;
            }
            ProbDb.query('SELECT * FROM prob WHERE problem_code=$1', [code], (err1, data)=>{
                if(err1) {
                    console.log("problem/:code/edit get problem_data error(on loading phase): "+err1);
                    res.status(500).send('db');
                    return;
                }
                row = data.rows[0];
                let hashintx=false, hintx='';
                if(row.has_hint == 1) {
                    hashintx = true;
                    hintx = row.problem_hint;
                }
                res.send({
                    prob_cont: row.problem_content,
                    prob_exp: row.problem_solution,
                    prob_ans: row.problem_answer,
                    prob_hint: hintx,
                    prob_name: row.problem_name,
                    cate: row.problem_category,
                    diff: row.problem_difficulty,
                    hashint: hashintx,
                    spec: row.extr_tabs
                });
            });
        });
        app.post('/problem/make/upload', upload.single('img'),(req, res)=>{
            res.json(req.file.filename);
        });
        app.post('/problem/make/register', (req, res)=>{
            if(!auth.checkIdentity(req)) {
                res.status(403).send('forbidden');
                return;
            }
            perm.checkPrivilege(req, ['p', 'm'], (rex)=>{
                if(rex) {
                    let now = new Date();
                    let hint = req.body.hint;
                    if(!req.body.hashint) {
                        hint = undefined;
                    }
                    ProbDb.query(`INSERT INTO prob(problem_name, problem_category, problem_difficulty, problem_content, problem_solution, problem_answer, problem_hint, author_name, added_at, last_modified, answers, extr_tabs, has_hint) `+
                                 `values ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, 0, $11, $12)`,
                                [req.body.title, req.body.cate, req.body.difficult, req.body.cont, req.body.expl, req.body.answ, hint, req.session.user.id, now.toISOString(), now.toISOString(), req.body.extr, (req.body.hashint=="true" ? 1 : 0)], (err, resx)=>{
                        if(err) {
                            console.log('Problem insert sql failure: '+err);
                            error.sendError(500, 'Internal Server Error', res);
                        }
                        else {
                            res.send('/problem');
                        }
                    });
                }
                else {
                    res.sendStatus(403);
                }
            });
        });
        app.post('/problem/make/update', (req, res)=>{
            if(!auth.checkIdentity(req)) {
                res.status(403).send('forbidden');
                return;
            }
            perm.checkPrivilege(req, ['p', 'm'], (rex)=>{
                if(rex) {
                    let now = new Date();
                    let hint = req.body.hint;
                    if(!req.body.hashint) {
                        hint = undefined;
                    }
                    ProbDb.query(`UPDATE prob SET problem_name=$1, problem_category=$2, problem_difficulty=$3, problem_content=$4, problem_solution=$5, problem_answer=$6, problem_hint=$7, author_name=$8, last_modified=$9, extr_tabs=$10, has_hint=$11 WHERE problem_code=$12`,
                                [req.body.title, req.body.cate, req.body.difficult, req.body.cont, req.body.expl, req.body.answ, hint, req.session.user.id, now.toISOString(), req.body.extr, (req.body.hashint=="true" ? 1 : 0), req.body.codex], (err, resx)=>{
                        if(err) {
                            console.log('problem/make/update sql failure: '+err);
                            error.sendError(500, 'Internal Server Error', res);
                        }
                        else {
                            res.send('/problem/'+req.body.codex);
                        }
                    });
                }
                else {
                    res.sendStatus(403);
                }
            });
        });
        app.post('/problem/api/get', (req, res)=>{
            let from = req.body.frm;
            let length = req.body.len;
            let regex = new RegExp('^[0-9]{1,2}$');
            if(from <= 0 || length<=0 || length>100 || !regex.test(from) || !regex.test(length)) {
                error.sendError(400, 'Bad Request', res);
                return;
            }
            ProbDb.query('SELECT problem_code, problem_name, problem_category, problem_difficulty, answers FROM prob WHERE problem_code>=$1 ORDER BY problem_code LIMIT $2;', [from, length], (err, data)=>{
                if(err) {
                    console.log("problem table query error: "+err);
                    error.sendError(500, 'Internal Server Error', res);
                }
                else {
                    rows = data.rows;
                    let ret = [];
                    rows.forEach(row => {
                        ret.push({
                            code: row.problem_code,
                            name: row.problem_name,
                            cate: row.problem_category,
                            diff: row.problem_difficulty,
                            tags: [
                                {
                                    key:"cate",
                                    content: row.problem_category
                                },
                                {
                                    key:"diff",
                                    content: row.problem_difficulty
                                }
                            ]
                        });
                    });
                    res.send(ret);
                }
            });
        });
        app.post('/problem/api/solrep', (req, res)=>{
            if(!auth.checkIdentity(req)) {
                res.status(403).send('forbidden');
                return;
            }
            let now = new Date();
            auth.queryId(req.session.user.id, 'last_solve', (errqw, ls)=>{
                if(errqw) {
                    res.status(500).send('usrQ');
                    return;
                }
                let last = new Date(ls);
                if((new Date().getTime()-last.getTime())/1000 < 60) {
                    res.status(403).send('time');
                    return;
                }
                SolveDb.query('BEGIN', (err1)=>{
                    if(err1) {
                        res.status(500).send('trans');
                        return;
                    }
                    SolveDb.query(`INSERT INTO u${req.session.user.code}(problem_code, solved_time, solving_time, correct) VALUES ($1, $2, $3, $4);`, [req.body.code, now.toISOString(), req.body.time, req.body.res], (err2)=>{
                        if(err2) {
                            res.status(500).send('sdb');
                            return;
                        }
                        SolveDb.query(`SELECT COUNT(*) AS count FROM u${req.session.user.code} WHERE problem_code=$1;`, [req.body.code], (err3, nos)=>{
                            if(err3) {
                                console.log("problem/api/solrep get NOS error: "+err3);
                                SolveDb.query('ROLLBACK');
                                res.status(500).send('expUpd');
                                return;
                            }
                            ProbDb.query('SELECT problem_difficulty AS dif FROM prob WHERE problem_code=$1;', [req.body.code], (err4, diff)=>{
                                if(err4) {
                                    console.log("problem/api/solrep get difficulty error: "+err4);
                                    SolveDb.query('ROLLBACK');
                                    res.status(500).send('expUpd');
                                    return;
                                }
                                let addExp = experi.calculate_score(diff.rows[0].dif, nos.rows[0].count);
                                if(req.body.res == '0') addExp = experi.trans_wa(addExp, diff.rows[0].dif);
                                auth.query('UPDATE iden SET experience=((SELECT experience FROM iden WHERE user_code=$1)+$2), last_solve=$3 WHERE user_code=$1;', [req.session.user.code, addExp, new Date().toISOString()], (err3)=>{
                                    if(err3) {
                                        console.log("problem/api/solrep update exp error: "+err4);
                                        SolveDb.query('ROLLBACK');
                                        res.status(500).send('expUpd');
                                        return;
                                    }
                                    else {
                                        SolveDb.query('COMMIT');
                                        res.sendStatus(200);
                                    }
                                });
                            });
                        });
                    });
                });
            });
        });
        app.get('/problem', (req, res)=>{
            let page = 0;
            if(req.query.page != undefined && req.query.page >= 0) {
                page = req.query.page;
            }
            if(auth.checkIdentity(req)) {
                res.render('../views/problem/problem.ejs', {
                    ylog: "block",
                    nlog: "none",
                    userid: req.session.user.id,
                    username: req.session.user.name,
                    pg: page
                });
            }
            else {
                res.render('../views/problem/problem.ejs', {
                    ylog: "none",
                    nlog: "block",
                    userid: '',
                    username: '',
                    pg: page
                });
            }
        });
        app.get('/problem/:code', (req, res)=>{
            let regex = new RegExp('^[0-9]{1,4}$');
            let code = req.params.code;
            if(!regex.test(code)) {
                error.sendError(400, 'Bad Request', res);
                return;
            }
            ProbDb.query('SELECT problem_code, problem_name, problem_category, problem_difficulty, answers, problem_content, problem_hint, problem_solution, problem_answer, has_hint, extr_tabs FROM prob WHERE problem_code=$1;', [code], (err, data)=>{
            if(err || data.rowCount != 1) {
                console.log("problem table query error: "+err);
                error.sendError(500, 'Internal Server Error', res);
            }
            else {
                row = data.rows[0];
                    let hashintx='none', hintx='';
                    if(row.has_hint == 1) {
                        hashintx = 'block';
                        hintx = row.problem_hint;
                    }
                    if(!auth.checkIdentity(req)) {
                        res.render('../views/problem/problem_page.ejs', {
                            ylog: "none",
                            nlog: "block",
                            userid: '',
                            username: '',
                            problem_code: row.problem_code,
                            problem_name: row.problem_name,
                            prob_cont: row.problem_content,
                            prob_exp: row.problem_solution,
                            prob_ans: row.problem_answer,
                            hashint: hashintx,
                            prob_hint: hintx,
                            spec: row.extr_tabs
                        });
                    }
                    else {
                        res.render('../views/problem/problem_page.ejs', {
                            ylog: "block",
                            nlog: "none",
                            userid: req.session.user.id,
                            username: req.session.user.name,
                            problem_code: row.problem_code,
                            problem_name: row.problem_name,
                            prob_cont: row.problem_content,
                            prob_exp: row.problem_solution,
                            prob_ans: row.problem_answer,
                            hashint: hashintx,
                            prob_hint: hintx,
                            spec: row.extr_tabs
                        });
                    }
                }
            });
        });
        app.get('/problem/lib/:path', (req, res)=>{
            res.sendFile(dirname+`/problem/lib/${req.params.path}`);
        });
    },
    probQuery: function(requiredCode, callback) {
        let condition = "(";
        requiredCode.forEach(v => {
            condition += "problem_code="+v+" OR ";
        });
        condition = condition.substr(0, condition.length-4);
        condition += ")";
        ProbDb.query(`SELECT * FROM prob WHERE ${condition}`, (err, data)=>{
            if(err || data.rowCount == 0) {
                console.log('Problem query api error: '+err);
                return;
            }
            else {
                callback(data.rows);
            }
        });
    },
    probNonquery: function(query, param, callback) {
        ProbDb.query(query, param, (err)=>{
            callback(err);
        });
    },
    query: function(query, parameter, callback) {
        ProbDb.query(query, parameter, (err, res)=>{
            callback(err, res);
        });
    }
}