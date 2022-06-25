function keyDwn() {
    if(event.keyCode == 13) {
        checkCode();
    }
}
function checkCode() {
    gei('code-check').disabled = true;
    $.ajax({
        url: "/api/invite/check",
        data: {"code": gei('invite').value},
        type: "POST",
        dataType: "json",
        success: function(data) {
            if(data.result == true) {
                gei('invite').style.opacity = 0;
                gei('errdisplay').style.display = "none";
                setTimeout(()=>{
                    gei('invite').style.display = 'none';
                    gei('name').style.display = 'inline-block';
                    gei('id').style.display = 'inline-block';
                    gei('pwd').style.display = 'inline-block';
                    gei('captcha').style.display = "block";
                    setTimeout(()=>{
                        gei('name').style.opacity = 1;
                        gei('name').focus();
                        gei('id').style.opacity = 1;
                        gei('pwd').style.opacity = 1;
                        gei('captcha').style.opacity = 1;
                        gei('code-check').remove()
                        gei('submit').style.display = "block";
                    }, 50);
                }, 300);
            }
            else {
                gei('errdisplay').innerText = "초대코드를 확인할 수 없어요.\n코드가 정확한가요?";
                gei('errdisplay').style.display = "inline-block";
                gei('invite').value = "";
                gei('code-check').disabled = false;
            };
        },
        error: function(err) {
            gei('errdisplay').innerText = "인증 중 문제가 발생했어요.\n잠시 후에 다시 시도해주세요.";
            gei('errdisplay').style.display = "inline-block";
            gei('invite').value = "";
            gei('code-check').disabled = false;
        }
    });
}

const idValidator = new RegExp('^(?=.*[A-Za-z])[A-Za-z0-9]{0,50}$');
const pwdValidator = new RegExp('^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d!"#$%&\'()*+,\\-./:;<=>?@\\[\\]^_`{|}~\\\\]{6,}$');

function create() {
    let id = gei("id").value, pwd = gei("pwd").value, name = gei("name").value;
    let ret = true;
    if(!idValidator.test(id)) {
        gei("id").classList.add("wrong-form");
        gei("ii").style.display="block";
        ret = false;
    }
    else {
        gei("id").classList.remove("wrong-form");
        gei("ii").style.display="none";
    }
    if(name.length>50) {
        gei("name").classList.add("wrong-form");
        gei("ni").style.display="block";
        ret = false;
    }
    else {
        gei("name").classList.remove("wrong-form");
        gei("ni").style.display="none";
    }
    if(!pwdValidator.test(pwd)) {
        gei("pwd").classList.add("wrong-form");
        gei("pi").style.display="block";
        ret = false;
    }
    else {
        gei("pwd").classList.remove("wrong-form");
        gei("pi").style.display="none";
    }
    captcha = gei('g-recaptcha-response').value;
    $.ajax({
        url: "/api/account/create",
        method: "POST",
        dataType: "json",
        data: {
            id: id,
            password: pwd,
            name: name,
            invite: gei('invite').value,
            gToken: captcha
        },
        success: function() {
            window.location.href = '/';
        },
        error: function(error) {
            if(error.responseJSON.status == 500) {
                gei('err').innerText = "계정을 만들지 못했어요. 잠시 후에 다시 시도해주세요.";
                return;
            }
            switch(error.responseJSON.result) {
                case "badform":
                    gei('err').innerText = "입력하신 정보가 규칙에 맞지 않아요. 다시 확인하고 시도해주세요.";
                    break;
                case "captcha":
                    gei('err').innerText = "CAPTCHA 인증에 실패했어요. 다시 시도해주세요.";
                    break;
                case "invite":
                    gei('err').innerText = "유효하지 않은 초대코드에요. 다시 시도해주세요.";
                    break;
                case "id_dupl":
                    gei('err').innerText = "이미 사용중인 ID에요. 조금 더 개성을 담아 ID를 정해주세요!";
                    break;

            }
            setCaptcha();
        }
    });			
}