const auth = require('./auth');
const profile = require('./profile');

module.exports = {
    usrRouter: function(app) {
        app.get('/users', (req, res)=>{
            if(auth.checkIdentity(req)) {
                res.render('../views/users/users.ejs', {
                    ylog: "block",
                    nlog: "none",
                    userid: req.session.user.id,
                    username: req.session.user.name
                });
            }
            else {
                res.render('../views/users/users.ejs', {
                    ylog: "none",
                    nlog: "block",
                    userid: '',
                    username: ''
                });
            }
        });
        app.post('/users/api/rank', (req, res)=>{
            let length = req.body.len;
            let regex = new RegExp('^[0-9]{1,2}$');
            if(length<=0 || length>100 || !regex.test(length)) {
                error.sendError(400, 'Bad Request', res);
                return;
            }
            auth.query('SELECT user_id, user_name, bio, experience, user_code FROM iden ORDER BY experience DESC LIMIT $1', [length], (err, users)=>{
                if(err) {
                    console.log("users/api/rank get rank error: "+err3);;
                    res.status(500).send("uq");
                }
                rows = users.rows;
                let arr = [];
                rows.forEach(row=>{
                    arr.push({
                        uname: row.user_name,
                        bio: row.bio,
                        exp: row.experience,
                        id: row.user_id
                    });
                });
                res.send(arr);
            });
        });
    }
}