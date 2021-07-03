let isBurgerOpen = false;
function hamburger() {
    if(isBurgerOpen) {
        document.getElementById('top-patty2').classList.remove('top-patty-active2');
        document.getElementById('bottom-patty2').classList.remove('bottom-patty-active2');
        setTimeout(function () {
            document.getElementById('top-patty1').classList.remove('top-patty-active1');
        document.getElementById('bottom-patty1').classList.remove('bottom-patty-active1');
        },100);
    }
    else {
        document.getElementById('top-patty1').classList.add('top-patty-active1');
        document.getElementById('bottom-patty1').classList.add('bottom-patty-active1');
        setTimeout(function() {
            document.getElementById('top-patty2').classList.add('top-patty-active2');
            document.getElementById('bottom-patty2').classList.add('bottom-patty-active2');
        },100);
    }
    isBurgerOpen = !isBurgerOpen;
}
