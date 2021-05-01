const fs = require('fs');
const auth = require('./auth');
const error = require('./error');

module.exports = {
    docsRouter: function(app, rtp) {
        app.get('/docs/:type', (req, res)=>{
            try {
                if(auth.checkIdentity(req)) {
                    res.render(`docs/${req.params.type}.ejs`, {
                        ylog: 'block',
                        nlog: 'none',
                        ylogi: 'inline-block',
                        nlogi: 'none',
                        userid: req.session.user.id,
                        username: req.session.user.name,
                    });
                }
                else {
                    let exi = fs.existsSync(rtp + `/views/docs/${req.params.type}.ejs`);
                    if(exi) {
                        res.render(`docs/${req.params.type}.ejs`, {
                            ylog: 'none',
                            nlog: 'block',
                            ylogi: 'none',
                            nlogi: 'inline-block',
                            userid: '',
                            username: '',
                        });
                    }
                    else {
                        error.sendError(404, 'Not Found', res);        
                    }
                }
            }
            catch {
                error.sendError(404, 'Not Found', res);
            }
        });
    }
}