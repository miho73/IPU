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
        app.get('/etc/dday/:path', (req, res, next)=>{
            let dd, tit;
            switch(req.params.path) {
                case 'shs':
                    dd=1628913600;
                    tit='영재고 3차 캠프';
                    break;
                case 'sat':
                    dd=1637190000;
                    tit='2022학년도 대학수학능력평가';
                    break;
                default:
                    next();
                    return;
            }
            res.render('etc/dday/dday.ejs', {
                dd:dd,
                tit:tit
            });
        });
    }
}