module.exports = {
    etcRouter: function(app) {
        app.get("/sounds", (req, res)=>{
            res.render("sounds/sounds.ejs");
        });
        app.get("/sounds/:code", (req, res)=>{
            cd = "F-o1wmQ1AKA";
            cname = "Sounds";
            switch(req.params.code) {
                case "hogwarts":
                    cd = "yBmPDPCd_ls";
                    cname = "Hogwarts"
                    break;
                case "kang":
                    cd = "Vzv5HHehLg8";
                    cname = "강성태: 공부할 때 듣는 음악"
                    break;
                case "ani":
                    cd = "nhgm9pryBsY";
                    cname="OST from Animations";
                    break;
            }
            res.render('sounds/ytp.ejs', {
                name: cname,
                code: cd
            });
        });
    }
}