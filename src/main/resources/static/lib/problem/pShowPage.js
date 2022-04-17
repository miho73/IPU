var timex = 0;
var timer;

function load() {
    MathJax.Hub.Queue(["Typeset",MathJax.Hub]);
    timer = setInterval(()=>timex++, 1000);
}

submitAcvited = false;
function submitSolution(code) {
    if(!submitAcvited) {
        clearInterval(timer);
        gei('show-ans-btn').disabled = true;

        gei('answer-context').style.display = 'block';
        setTimeout(()=>gei('answer-context').style.transform = 'scaleY(1)', 2);
        submitAcvited = true;
    }

    if(checkAllSubmitable()) {
        showFinalDisplay();
        gei('judge-result').style.display = 'block';
        submitAll(code);
    }
    else {
        gei('show-ans-btn').disabled = false;
    }
}

function afterSubmitSuccess(res) {
    gei('judging').style.display = 'none';
    if(res.result == true) {
        gei('ac').style.display = 'block';
    }
    else if (res.result == false) {
        gei('wa').style.display = 'block';
    }
    else {
        gei('sol-comment').innerText = "문제가 생겨 채점결과를 받지 못했어요. 채점은 되었으니 안심하세요.";
        gei('solve-time-div').style.color = '#fec72e';
    }    
}

function afterSubmitFailure(error) {
    gei('judging').style.display = 'none';
    res = error.responseJSON['result'];
    var msg;
    switch(res) {
        case 'dis':
            msg = '문제가 비활성화되어 있기 때문에'
            break;
        case 'ueaw':
            msg = '정해진 형식의 답이 제출되지 않았기 때문에';
            break;
        case 'unkj':
            msg = '채점 형식이 잘못 설정되어 있어서'
            break;
        case 'unkn':
            msg = '알 수 없는 이유로 '
            break;
        case 'intr':
            msg = '마지막 제출 후 1분이 지나지 않았기 때문에';
            break;
        case 'forb':
            msg = '문제를 제출하려면 로그인해야 해요. 인증에 실패했기 때문에'
            break;
    }
    gei('was-error-submit').innerText = msg + ' 제출하지 못했어요.';
    gei('cannot-submit').style.display = 'flex';
}

function showFinalDisplay() {
    gei('sol-time').innerText = timex;
    gei('finish-solve').style.display = 'none';
    gei('solve-time-div').style.display = 'block';
    setTimeout(()=>{
        gei('solve-time-div').style.opacity = 1;
    }, 5);
}

function checkAllSubmitable() {
    answers = gei('answer-field').children;
    idx = 0;
    for(answerDiv of answers) {
        inputs = answerDiv.getElementsByTagName('input');

        if(inputs.length == 0) continue;
        if(inputs[0].id == `a${idx}sa` && inputs.length != 1) {
            return false;
        }
        idx++;
    }
    return true;
}

function ac(idx) {
    if(!submitAcvited) return;
    gei(`a${idx}sa`).disabled = true;
    gei(`a${idx}sw`).disabled = true;
    o = gei(`a${idx}sw`);
    o.style.opacity = 0;
    setTimeout(()=>{
        o.remove();
    }, 100);
}
function wa(idx) {
    if(!submitAcvited) return;
    gei(`a${idx}sa`).disabled = true;
    gei(`a${idx}sw`).disabled = true;
    o = gei(`a${idx}sa`);
    o.style.opacity = 0;
    setTimeout(()=>{
        o.remove();
        gei(`a${idx}sw`).style.transform = 'translate(53px, 0)'
    }, 220);
}

function submitAll(code) {
    answerJson = [];

    answers = gei('answer-field').children;
    idx = 0;
    for(answerDiv of answers) {
        inputs = answerDiv.getElementsByTagName('input');

        if(inputs.length == 0) continue;
        if(inputs[0].id == `a${idx}sa`) {
            answerJson.push(0);
        }
        else if(inputs[0].id == `a${idx}sw`) {
            answerJson.push(1);
        }
        else if(inputs[0].id == `a${idx}`) {
            answerJson.push(inputs[0].value);
        }
        else if(inputs[0].id == `a${idx}f1`) {
            answerJson.push(`${inputs[0].value}/${inputs[1].value}`);
        }
        idx++;
    }
    $.ajax({
        method: 'POST',
        url: '/problem/api/solution/post',
        dataType: 'json',
        data: {
            code: code,
            time: timex,
            answer: JSON.stringify(answerJson)
        },
        success: afterSubmitSuccess,
        error: afterSubmitFailure
    })
    console.log(answerJson)
}

function resubmit(code) {
    gei('resubmit').disabled = true;
    submitAll();
}

function changeStar(code) {
    gei('star-icon').classList.add('loading');
    gei('star-progress').style.display = 'block';
    gei('star').style.display = 'none';
    $.ajax({
        method: 'PATCH',
        url: '/problem/api/change-star',
        data: 'code='+code,
        dataType: 'json',
        success: function(result) {
            if(result.result.stared == 1) {
                gei('star-icon').classList.add("stared")
            }
            if(result.result.stared == 0) {
                gei('star-icon').classList.remove("stared")
            }
        },
        error: function() {},
        complete: function() {
            gei('star-icon').classList.remove('loading');
            gei('star-progress').style.display = 'none';
            gei('star').style.display = 'block';
        }
    });
}