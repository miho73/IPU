const auth = require('./auth');

module.exports = {
    checkPrivilege(req, privilege, callback) {
        if(!auth.checkIdentity(req)) {
            callback(false);
            return;
        }
        auth.queryId(req.session.user.id, 'privilege', (err, dat)=>{
            if(err) callback(false);
            else if(privilege.includes(dat) || dat == 's') callback(true);
            else callback(false);
        });
    }
}