let isBurgerOpen = false;
function hamburger() {
    //Close operation
    if(isBurgerOpen) {
        gei('top-patty2').classList.remove('top-patty-active2');
        gei('bottom-patty2').classList.remove('bottom-patty-active2');
        gei('hambuger-menu').style.height = '0';
        setTimeout(function () {
            gei('top-patty1').classList.remove('top-patty-active1');
            gei('bottom-patty1').classList.remove('bottom-patty-active1');
            gei('hambuger-menu').style.display = 'none';
        },100);
        setTimeout(function () {
            gei('hambuger-menu').style.display = 'none';
        },200);
    }
    //Open operation
    else {
        gei('top-patty1').classList.add('top-patty-active1');
        gei('bottom-patty1').classList.add('bottom-patty-active1');
        gei('hambuger-menu').style.display = 'block';
        setTimeout(function() {
            gei('hambuger-menu').style.height = '138px';
        }, 5);
        setTimeout(function() {
            gei('top-patty2').classList.add('top-patty-active2');
            gei('bottom-patty2').classList.add('bottom-patty-active2');
        },100);
    }
    isBurgerOpen = !isBurgerOpen;
}

function gei(id) {
    return document.getElementById(id);
}