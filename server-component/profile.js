const auth = require('./auth');

module.exports = {
    profileRouter: function(app) {
        app.get('/profile/:user', (req, res)=>{
            res.render("profile/user_profile.ejs", {
                ylog: 'block',
                nlog: 'none',
                username: req.params.user,
                bio: auth.queryId(req.params.user, 'bio')
            });
        });
    }
}