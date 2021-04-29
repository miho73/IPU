const fs = require('fs');
const auth = require('./auth');
const error = require('./error');

function pPermission(req, callback) {
    if(!auth.checkIdentity(req)) {
        callback(false);
        return;
    }
    auth.queryId(req.session.user.id, 'privilege', (err, dat)=>{
        if(err) callback(false);
        else if(dat.includes('p')) callback(true);
        else callback(false);
    });
}

module.exports = {
    manageRouter: function(app, rtp) {
        app.get('/manage', (req, res)=>{
            pPermission(req, (rex)=>{
                if(rex) {
                    res.render('management/control.ejs');
                }
                else {
                    error.sendError(404, 'Not Found', res);
                }
            });
        });
        app.post('/mgr/api/get/inv', (req, res)=>{
            pPermission(req, (rex)=>{
                if(!rex) return;
                query = req.body.q;
                if(query == 'GET') {
                    res.sendFile(rtp+'/invites.json');
                }
                else {
                    if(query == undefined) {
                        res.status(400).send("QueryUndefined");
                        return;
                    }
                    parsed = query.split(' ');
                    try {
                        fs.readFile(rtp+'/invites.json', (err, data)=>{
                            if(err) {
                                console.log("invite query error: "+err);
                                res.status(500).send("CannotLoadCurrent");
                                return;
                            }
                            let obj = JSON.parse(data).codes;
                            switch(parsed[0]) {
                                case "DELETE":
                                    key = parsed[1]+1;
                                    obj = obj.splice(key-1, 1);
                                    fs.writeFile(rtp+'/invites.json', JSON.stringify({codes: obj}), (err)=>{
                                        if(err) {
                                            res.status(500).send("CannotSaveChange");
                                            return;
                                        }
                                        res.sendFile(rtp+'/invites.json');
                                    });
                                    break;
                                case "APPEND":
                                    code = parsed[1];
                                    if(!new RegExp('^[a-zA-Z0-9]{1,50}$').test(code)) {
                                        res.status(400).send("InviteCodeNotInFormat");
                                        return;
                                    }
                                    obj.push(code);
                                    fs.writeFile(rtp+'/invites.json', JSON.stringify({codes: obj}), (err)=>{
                                        if(err) {
                                            res.status(500).send("CannotLoadCurrent");
                                            return;
                                        }
                                        res.sendFile(rtp+'/invites.json');
                                    });
                                    break;
                                case "UPDATE":
                                    code = parsed[2];
                                    key = parsed[1]-1;
                                    if(!new RegExp('^[a-zA-Z0-9]{1,50}$').test(code)) {
                                        res.status(400).send("InviteCodeNotInFormat");
                                        return;
                                    }
                                    if(obj.length <= key || key<0) {
                                        res.status(400).send("KeyOutOfRange");
                                        return;
                                    }
                                    obj[key] = code;
                                    fs.writeFile(rtp+'/invites.json', JSON.stringify({codes: obj}), (err)=>{
                                        if(err) {
                                            res.status(500).send("CannotLoadCurrent");
                                            return;
                                        }
                                        res.sendFile(rtp+'/invites.json');
                                    });
                                    break;
                                default:
                                    res.status(400).send('UnknownInstruction');
                            }
                        });
                    }
                    catch {
                        res.sendStatus(500);
                    }
                }
            });
        });
    }
}