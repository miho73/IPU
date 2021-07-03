function activePwd() {
    document.getElementById('changepwd').style.display = 'none';
    document.getElementById('pwdreg').style.display = 'block';
    document.getElementById('pwdChange').checked = true;
    setTimeout(()=>document.getElementById('pwdreg').style.transform = 'scaleY(1)', 2);
}

function upload() {
    let uname = document.getElementById('name').value;
    let bio = document.getElementById('bios').value;
    let ema = document.getElementById('email').value;
    let lpwd = document.getElementById('lpwd').value;
    let success = true;
    if(uname.length >= 50 || uname == '') {
        document.getElementById('name').classList.add('formthis');
        window.location.hash = "name";
        success = false;
    }
    else document.getElementById('name').classList.remove('formthis');
    if(bio.length >= 500) {
        document.getElementById('bios').classList.add('formthis');
        window.location.hash = "bios";
        success = false;
    }
    else document.getElementById('bios').classList.remove('formthis');
    if(!validateEmail(ema) && ema != '') {
        document.getElementById('email').classList.add('formthis');
        window.location.hash = "email";
        success = false;
    }
    else document.getElementById('email').classList.remove('formthis');
    if(lpwd == '') {
        document.getElementById('lpwd').classList.add('formthis');
        window.location.hash = "lpwd";
        success = false;
    }
    else document.getElementById('lpwd').classList.remove('formthis');
    if(!success) return;
    $.ajax({
        type: 'POST',
        url: '/settings',
        data: {
            name: uname,
            bio: bio,
            email: ema,
            pwdC: document.getElementById('pwdChange').checked,
            npwd: document.getElementById('npwd').value,
            lpwd: lpwd
        },
        success: function(data) {
            window.location.reload();
        },
        error: function(err) {
            document.getElementById('errdis').style.display = "block";
            switch(err.responseText) {
                case "pwd":
                    document.getElementById('errdis').innerText = "인증 실패";
                    return;
                case "pwdf":
                    document.getElementById('errdis').innerText = "암호는 4자리 이상, 100자리 이하여야 하며, 글자(a-z, A-Z)와 숫자(0-9)로 만 이루어져 있어야 합니다.";
                    return;
                case "name":
                    document.getElementById('errdis').innerText = "이름은 50자 이내이여야 합니다.";
                    return;
                case "bio":
                    document.getElementById('errdis').innerText = "상태메시지는 최대 500자입니다.";
                    return;
                case "email":
                    document.getElementById('errdis').innerText = "올바른 이메일을 입력하세요.";
                    return;
                case "dbupdate-pwd":
                    document.getElementById('errdis').innerText = "업데이트할 수 없습니다.";
                    return;
                case "dbupdate-pwd":
                    document.getElementById('errdis').innerText = "업데이트할 수 없습니다.ㄴ";
                    return;
            }
        }
    });
}

function validateEmail(email) {
    const re = /^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    return re.test(String(email).toLowerCase());
}