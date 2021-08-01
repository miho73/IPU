const error = require('./error');
const fs = require('fs');

module.exports = {
    calcRouter: function(app, dirname) {
        app.get('/calc', (req, res)=>{
            res.send("Calculator");
        });
        app.get('/calc/:path', (req, res, next)=>{
            if(!fs.existsSync(dirname+`/views/calc/${req.params.path}.ejs`)) {
                error.sendError(404, 'Not Found', res);
            }
            else res.render(`calc/${req.params.path}.ejs`);
        });
    }
}