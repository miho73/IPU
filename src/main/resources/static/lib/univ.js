let isBurgerOpen = false;
function hamburger() {
    //Close operation
    if(isBurgerOpen) {
        document.getElementById('top-patty2').classList.remove('top-patty-active2');
        document.getElementById('bottom-patty2').classList.remove('bottom-patty-active2');
        document.getElementById('ham-content').style.transform = 'scaleX(0)';
        document.getElementById('hambuger-menu').style.opacity=0;
        document.getElementsByTagName('html')[0].style.height='unset';
        document.getElementsByTagName('html')[0].style.overflowY='unset';
        setTimeout(function () {
            document.getElementById('top-patty1').classList.remove('top-patty-active1');
            document.getElementById('bottom-patty1').classList.remove('bottom-patty-active1');
        },100);
        setTimeout(function () {
            document.getElementById('hambuger-menu').style.display = 'none';
        }, 200);
    }
    //Open operation
    else {
        document.getElementById('top-patty1').classList.add('top-patty-active1');
        document.getElementById('bottom-patty1').classList.add('bottom-patty-active1');
        document.getElementById('hambuger-menu').style.display = 'block';
        document.getElementsByTagName('html')[0].style.height='100%';
        document.getElementsByTagName('html')[0].style.overflowY='hidden';
        setTimeout(function() {
            document.getElementById('hambuger-menu').style.opacity=1;
            document.getElementById('ham-content').style.transform = 'scaleX(1)';
        }, 5);
        setTimeout(function() {
            document.getElementById('top-patty2').classList.add('top-patty-active2');
            document.getElementById('bottom-patty2').classList.add('bottom-patty-active2');
        },100);
    }
    isBurgerOpen = !isBurgerOpen;
}
