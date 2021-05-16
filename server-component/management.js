const fs = require('fs');
const auth = require('./auth');
const error = require('./error');
const perm = require('./permission');

module.exports = {
    manageRouter: function(app, rtp) {
        app.get('/manage', (req, res)=>{
            perm.checkPrivilege(req, ['m'], (rex)=>{
                if(rex) {
                    res.render('management/control.ejs');
                }
                else {
                    error.sendError(403, 'Forbidden', res);
                }
            });
        });
        app.post('/mgr/api/get/inv', (req, res)=>{
            perm.checkPrivilege(req, ['m'], (rex)=>{
                if(!rex) {
                    res.status(403).send("Permission");
                    return;
                }
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
                                    if(obj.length <= key || key<0) {
                                        res.status(400).send("KeyOutOfRange");
                                        return;
                                    }
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
        app.post('/mgr/api/perm', (req, res)=>{
            try {
                perm.checkPrivilege(req, [], (rex)=>{
                    if(rex) {
                        parsed = req.body.q.split(' ');
                        switch(parsed[0]) {
                            case "QUERY":
                                auth.queryId(parsed[1], 'privilege', (err, data)=>{
                                    if(err) {
                                        res.status(500).send('dbquery');
                                    }
                                    else {
                                        res.send("Privilege of user "+parsed[1]+" is \'"+data+"\'");
                                    }
                                })
                                break;
                            case "UPDATE":
                                if(parsed[1].length != 1) res.status(400).send('permFormat');
                                else {
                                    if(parsed[2] == req.session.user.id) {
                                        res.status(400).send('self');
                                        return;
                                    }
                                    auth.query('UPDATE iden SET privilege=$1 WHERE user_id=$2', [parsed[1], parsed[2]], (err314, data)=>{
                                        if(err314 || data.rowCount == 0) {
                                            res.status(500).send('dbquery');
                                        }
                                        else {
                                            res.send('Updated');
                                        }
                                    });
                                }
                                break;
                            default:
                                res.status(400).send('unk');
                        }
                    }
                    else {
                        res.status(403).send('perm');
                    }
                });
            }
            catch {
                res.status(500).send('cat');
            }
        });
    }
}