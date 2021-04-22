const e = require('express');
const multer = require('multer');
const auth = require('./auth');
const error = require('./error');
const upload = multer({dest: 'problem/lib'});
const pg = require('pg');
const fs = require('fs');

const dbconfig_json = fs.readFileSync(__dirname+'/db_config.json');
const dbconfig = JSON.parse(dbconfig_json).prob;

const ProbDb = new pg.Client(dbconfig);
ProbDb.connect(err => {
    if (err) {
        console.log('Failed to connect to problem db: ' + err);
    }
    else {
        console.log('Connected to problem db');
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
                res.render('../views/problem/mk_problem.ejs', {
                    ylog: "block",
                    nlog: "none",
                    username: req.session.user.name
                });
            }
            else {
                res.redirect('/login/?ret=problem/make');
            }
        });
        app.post('/problem/make/upload', upload.single('img'),(req, res)=>{
            res.json(req.file.filename);
        });
        app.post('/problem/make/register', (req, res)=>{
            if(!auth.checkIdentity(req)) {
                res.status(403).send('forbidden');
                return;
            }
            let now = new Date();
            let hint = req.body.hint;
            if(!req.body.hashint) {
                hint = undefined;
            }
            //TODO: XSS filter maybe?
            ProbDb.query(`INSERT INTO prob(problem_name, problem_category, problem_difficulty, problem_content, problem_solution, problem_answer, problem_hint, author_name, added_at, last_modified, answers, extr_tabs, has_hint) `+
                         `values ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, 0, $11, $12)`,
                        [req.body.title, req.body.cate, req.body.difficult, req.body.cont, req.body.expl, req.body.answ, hint, req.session.user.id, now.toISOString(), now.toISOString(), req.body.extr, (req.body.hashint ? 1 : 0)], (err, res)=>{
                            if(err) {
                                console.log('Problem insert sql failure: '+err);
                                error.sendError(500, 'Internal Server Error', res);
                            }
                        });
            res.send('/problem');
        });
        app.post('/problem/api/get', (req, res)=>{
            let from = req.body.frm;
            let length = req.body.len;
            let regex = new RegExp('^[0-9]{1,2}$');
            if(from <= 0 || length<=0 || length>20 || !regex.test(from) || !regex.test(length)) {
                error.sendError(400, 'Bad Request', res);
                return;
            }
            ProbDb.query('SELECT problem_code, problem_name, problem_category, problem_difficulty, answers FROM prob WHERE problem_code>=$1 ORDER BY problem_code LIMIT $2;', [from, length], (err, data)=>{
                rows = data.rows;
                if(err || rows == undefined) {
                    console.log("problem table query error: "+err);
                    error.sendError(500, 'Internal Server Error', res);
                }
                else {
                    let ret = [];
                    rows.forEach(row => {
                        ret.push({
                            code: row.problem_code,
                            name: row.problem_name,
                            cate: row.problem_category,
                            diff: row.problem_difficulty,
                            anss: row.answers
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
            
        });
        app.get('/problem', (req, res)=>{
            if(auth.checkIdentity(req)) {
                res.render('../views/problem/problem.ejs', {
                    ylog: "block",
                    nlog: "none",
                    username: req.session.user.name
                });
            }
            else {
                res.render('../views/problem/problem.ejs', {
                    ylog: "none",
                    nlog: "block",
                    username: ""
                });
            }
        });
        app.get('/problem/:code', (req, res)=>{
            let regex = new RegExp('^[0-9]{1,2}$');
            let code = req.params.code;
            if(!regex.test(code)) {
                error.sendError(400, 'Bad Request', res);
                return;
            }
            ProbDb.query('SELECT problem_code, problem_name, problem_category, problem_difficulty, answers, problem_content, problem_hint, problem_solution, problem_answer, has_hint, extr_tabs FROM prob WHERE problem_code=$1;', [code], (err, data)=>{
                row = data.rows[0];
                if(err || row == undefined) {
                    console.log("problem table query error: "+err);
                    error.sendError(500, 'Internal Server Error', res);
                }
                else {
                    if(!auth.checkIdentity(req)) {
                        res.redirect(`/login/?ret=problem/${req.params.code}`);
                    }
                    else {
                        let hashintx='none', hintx='';
                        if(row.has_hint == 1) {
                            hashintx = 'block';
                            hintx = row.problem_hint;
                        }
                        res.render('../views/problem/problem_page.ejs', {
                            ylog: "block",
                            nlog: "none",
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
    }
}