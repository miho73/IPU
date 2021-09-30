function activePwd() {
    gei('changepwd').style.display = 'none';
    gei('pwdreg').style.display = 'block';
    gei('pwdChange').checked = true;
    setTimeout(()=>gei('pwdreg').style.transform = 'scaleY(1)', 2);
}

const idValidator = new RegExp('^(?=.*[A-Za-z])[A-Za-z0-9]{0,50}$');
const pwdValidator = new RegExp('^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d!"#$%&\'()*+,\\-./:;<=>?@\\[\\]^_`{|}~\\\\\\)]{6,}$');

function upload() {
    let uname = gei('name').value;
    let bio = gei('bios').value;
    let ema = gei('email').value;
    let lpwd = gei('lpwd').value;
    let success = true;
    if(uname.length > 50 || uname == "") {
        gei('name').classList.add('formthis');
        window.location.hash = "name";
        success = false;
    }
    else gei('name').classList.remove('formthis');
    if(bio.length >= 500) {
        gei('bios').classList.add('formthis');
        window.location.hash = "bios";
        success = false;
    }
    else gei('bios').classList.remove('formthis');
    if(!validateEmail(ema) && ema != '') {
        gei('email').classList.add('formthis');
        window.location.hash = "email";
        success = false;
    }
    else gei('email').classList.remove('formthis');
    if(!pwdValidator.test(lpwd)) {
        gei('lpwd').classList.add('formthis');
        window.location.hash = "lpwd";
        success = false;
    }
    else gei('lpwd').classList.remove('formthis');
    if(!success) return;
    $.ajax({
        type: 'POST',
        url: '/settings',
        data: {
            name: uname,
            bio: bio,
            email: ema,
            pwdC: gei('pwdChange').checked,
            npwd: gei('npwd').value,
            lpwd: lpwd
        },
        success: function(data) {
            window.location.reload();
        },
        error: function(err) {
            gei('errdis').style.display = "block";
            switch(err.responseText) {
                case "pwd":
                    gei('errdis').innerText = "인증 실패";
                    break;
                case "form":
                    gei('errdis').innerText = "알맞지 않은 형식입니다.";
                    break;
                case "fpwd":
                    gei('errdis').innerText = "프로필은 업데이트되었지만 암호는 바꾸지 못했습니다.";
                    break;
            }
            window.location.hash = "errdis";
        }
    });
}

function validateEmail(email) {
    const re = /^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    return re.test(String(email).toLowerCase());
}