const e = require('express');
const multer = require('multer');
const auth = require('./auth');
const error = require('./error');
const upload = multer({dest: 'problem/lib'});
const sqlite3 = require('sqlite3').verbose();

let ProbDb = new sqlite3.Database('./db/problems.db', sqlite3.OPEN_READWRITE, (err) => {
    if (err) {
        console.error(err.message);
    } else {
        console.log('Connected to the PROBLEM database.');
    }
});
ProbDb.serialize(()=>{
    ProbDb.each('CREATE TABLE IF NOT EXISTS prob('+
                'problem_code INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,'+
                'problem_name TEXT NOT NULL,'+
                'problem_category TEXT NOT NULL,'+
                'problem_difficulty INTEGER NOT NULL,'+
                'problem_content TEXT NOT NULL,'+
                'problem_solution TEXT NOT NULL,'+
                'problem_answer TEXT NOT NULL,'+
                'problem_hint TEXT,'+
                'has_hint INTEGER NOT NULL,'+
                'author_name INTEGER NOT NULL,'+
                'added_at TEXT NOT NULL,'+
                'last_modified TEXT NOT NULL,'+
                'answers INTEGER NOT NULL,'+
                'extr_tabs TEXT NOT NULL);');
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
            ProbDb.run(`INSERT INTO prob(problem_name, problem_category, problem_difficulty, problem_content, problem_solution, problem_answer, problem_hint, author_name, added_at, last_modified, answers, extr_tabs, has_hint) `+
                       `values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, ?, ?)`,
                        [req.body.title, req.body.cate, req.body.difficult, req.body.cont, req.body.expl, req.body.answ, hint, req.session.user.id, now.toISOString(), now.toISOString(), req.body.extr, req.body.hashint ? 1 : 0]);
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
            ProbDb.all('SELECT problem_code, problem_name, problem_category, problem_difficulty, answers FROM prob WHERE problem_code>=? ORDER BY problem_code LIMIT ?;', [from, length], (err, rows)=>{
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
            ProbDb.get('SELECT problem_code, problem_name, problem_category, problem_difficulty, answers, problem_content, problem_hint, problem_solution, problem_answer, has_hint, extr_tabs FROM prob WHERE problem_code=?;', [code], (err, row)=>{
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