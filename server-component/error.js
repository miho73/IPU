module.exports = {
    sendError: function (errCode, errName, res) {
        res.status(errCode).render("../views/error.ejs", {
            errorCode: errCode,
            errorExp: errName
        });
    }
}