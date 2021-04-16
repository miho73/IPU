const multer = require('multer');
const auth = require('./auth');
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
                'author_id INTEGER NOT NULL,'+
                'added_at TEXT NOT NULL,'+
                'last_modified TEXT NOT NULL,'+
                'answers INTEGER NOT NULL);');
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
                res.render('../views/problem/mk_problem.ejs', {
                    ylog: "none",
                    nlog: "block",
                    username: ""
                });
            }
        });
        app.post('/problem/make/upload', upload.single('img'),(req, res)=>{
            res.json(req.file.filename);
        });
        app.post('/problem/make/register', (req, res)=>{
            if(!auth.checkIdentity(req)) {
                res.sendStatus(401);
                return;
            }
            let now = new Date();
            let hint = req.body.hint;
            if(!req.body.hashint) {
                hint = undefined;
            }
            ProbDb.run(`INSERT INTO prob(problem_name, problem_category, problem_difficulty, problem_content, problem_solution, problem_answer, problem_hint, author_id, added_at, last_modified, answers) `+
                       `values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)`,
                        [req.body.title, req.body.cate, req.body.difficult, req.body.cont, req.body.expl, req.body.answ, hint, req.session.user.id, now.toISOString(), now.toISOString()]);
            res.redirect('/problem');
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
        app.get('/problem/lib/:path', (req, res)=>{
            res.sendFile(dirname+`/problem/lib/${req.params.path}`);
        });
    }
}