let isBurgerOpen = false;
function hamburger() {
    //Close operation
    if(isBurgerOpen) {
        gei('top-patty2').classList.remove('top-patty-active2');
        gei('bottom-patty2').classList.remove('bottom-patty-active2');
        gei('hambuger-menu').style.maxHeight = '0';
        setTimeout(function () {
            gei('top-patty1').classList.remove('top-patty-active1');
            gei('bottom-patty1').classList.remove('bottom-patty-active1');
        },100);
        setTimeout(function () {
            gei('hambuger-menu').style.display = 'none';
        },300);
    }
    //Open operation
    else {
        gei('top-patty1').classList.add('top-patty-active1');
        gei('bottom-patty1').classList.add('bottom-patty-active1');
        gei('hambuger-menu').style.display = 'block';
        setTimeout(function() {
            gei('hambuger-menu').style.maxHeight = '200px';
        }, 5);
        setTimeout(function() {
            gei('top-patty2').classList.add('top-patty-active2');
            gei('bottom-patty2').classList.add('bottom-patty-active2');
        },100);
    }
    isBurgerOpen = !isBurgerOpen;
    updateFooter()
}

window.addEventListener('load', ()=>{
    updateFooterOnInit()
    gei('footer').style.opacity = 1;
    document.body.addEventListener('resize', ()=>{
        setTimeout(function() {
            updateFooter()
        }, 10);
    });
});
window.addEventListener('resize', ()=>{
    updateFooter()
});

function updateFooter() {
    if ($(document.body).height() < $(window).height()-81.2) {
        $('#footer').attr('style', 'position: fixed; bottom: 0px;opacity: 1;');
    }
    else {
        $('#footer').attr('style', 'opacity: 1;');
    }
}
function updateFooterOnInit() {
    if ($(document.body).height() < $(window).height()-81.2) {
        $('#footer').attr('style', 'position: fixed; bottom: 0px;');
    }
}

function gei(id) {
    return document.getElementById(id);
}
