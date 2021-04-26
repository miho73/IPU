function activePwd() {
    console.log("sdf");
    document.getElementById('changepwd').style.display = 'none';
    document.getElementById('pwdreg').style.display = 'block';
    setTimeout(()=>document.getElementById('pwdreg').style.transform = 'scaleY(1)', 2);
}

function upload() {
    let uname = document.getElementById('name').value;
    let bio = document.getElementById('bios').value;
    let ema = document.getElementById('email').value;
    if(!new RegExp('^[a-zA-Zㄱ-힣]{1,50}$').test(uname) || uname == '') {
        document.getElementById('name').classList.add('formthis');
        window.location.hash = "name";
    }
    else document.getElementById('name').classList.remove('formthis');
    if(bio.length >= 100) {
        document.getElementById('bios').classList.add('formthis');
        window.location.hash = "bios";
    }
    else document.getElementById('bios').classList.remove('formthis');
    if(!validateEmail(ema) && ema != '') {
        document.getElementById('email').classList.add('formthis');
        window.location.hash = "email";
    }
    else document.getElementById('email').classList.remove('formthis');
    $.ajax({
        type: 'POST',
        url: '/settings',
        data: {
            //Next job: setup sending format and implement server side database control
        },
        success: function(data) {
            activeFinal(false);
        },
        error: function(err) {
            alert(err);
        }
    });
}

function validateEmail(email) {
    const re = /^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    return re.test(String(email).toLowerCase());
}