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
}

window.addEventListener('load', ()=>{
    if ($(document.body).height() < $(window).height()) {
        gei('footer').setAttribute('style', 'position: fixed!important; bottom: 0px;');
    }
    gei('footer').style.opacity = 1;
});

if ($(document.body).height() < $(window).height()) {
    $('#footer').attr('style', 'position: fixed!important; bottom: 0px;');
}

function gei(id) {
    return document.getElementById(id);
}
