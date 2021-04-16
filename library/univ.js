//Event Register
document.onfullscreenchange = FullscreenRelease;

//IE check
function msieversion() {

    var ua = window.navigator.userAgent;
    var msie = ua.indexOf("MSIE ");

    if (msie > 0 || !!navigator.userAgent.match(/Trident.*rv\:11\./))  // If Internet Explorer, return version number
    {
        alert(parseInt(ua.substring(msie + 5, ua.indexOf(".", msie))));
    }
    else  // If another browser, return 0
    {
        alert('otherbrowser');
    }

    return false;
}

function FullscreenRelease(event) {
    if(document.fullscreen) {
        document.getElementById('fullscr-txt').innerText = "exit fullscreen";
    }
    else {
        document.getElementById('fullscr-txt').innerText = "fullscreen";
    }
}

function Fullscreen() {
    if(document.fullscreen) {
        document.exitFullscreen();
    }
    else {
        document.getElementsByTagName('html')[0].requestFullscreen();
    }
}

function AboutDisplay() {
    document.getElementById('about-page').style.display = 'block';
    setTimeout(()=>{ 
        document.getElementById('about-page').classList.remove('about-hide-page');
        document.getElementById('about-page').classList.add('about-show-page');
        for(let i=1; i<=3; i++) {
            document.getElementById(`footer-up-${i}`).classList.add('inverse-footer-up');
        }
        document.getElementById('fullscr-txt').classList.add('inverse-footer-dwn');
        document.getElementById('header-title').classList.add('inverse-header');
    }, 100);
}
function HideAboutDisplay() {
    document.getElementById('about-page').classList.add('about-hide-page');
    document.getElementById('about-page').classList.remove('about-show-page');
    for(let i=1; i<=3; i++) {
        document.getElementById(`footer-up-${i}`).classList.remove('inverse-footer-up');
    }
    document.getElementById('fullscr-txt').classList.remove('inverse-footer-dwn');
    document.getElementById('header-title').classList.remove('inverse-header');
    setTimeout(()=>{ 
        document.getElementById('about-page').style.display = 'none';
    }, 350);
}
