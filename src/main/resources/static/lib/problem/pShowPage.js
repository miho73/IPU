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
        disableInput();

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
    gei('cannot-submit').remove();
    gei('judge-summary').style.display = 'block';
    gei('judge-results').style.display = 'table';
    jResult = res.result;
    if(jResult.total == jResult.corrects) {
        gei('judge-summary').classList.add('obj-ac');
        gei('judge-summary').innerText = `맞았습니다!!`;
        gei('judge-acwa').classList.add('obj-ac');
        gei('judge-acwa').innerText = `${jResult.corrects}/${jResult.corrects}`;
    }
    else if (jResult.corrects == 0) {
        gei('judge-summary').classList.add('obj-wa');
        gei('judge-summary').innerText = `틀렸습니다`;
        gei('judge-acwa').classList.add('obj-wa');
        gei('judge-acwa').innerText = `0/${jResult.corrects}`;
    }
    else {
        gei('judge-summary').classList.add('obj-ac');
        gei('judge-summary').innerText = `부분점수 (${jResult.corrects}/${jResult.total})`;
        gei('judge-acwa').classList.add('obj-ac');
        gei('judge-acwa').innerText = `${jResult.corrects}/${jResult.total}`;
    }
    jResult.result.forEach((judge, index) => {
        tds = gei(`j${index}`).children;
        if('yours' in judge && 'answer' in judge) {
            tds[1].innerText = judge.yours;
            tds[2].innerText = judge.answer;
        }
        else {
            if(judge.acwa) {
                tds[1].innerText = '정답';
            }
            else {
                tds[1].innerText = '오답';
            }
        }
        if(judge.acwa) {
            tds[3].innerText = '맞았습니다!!';
            tds[3].classList.add('obj-ac');
        }
        else {
            tds[3].innerText = '틀렸습니다';
            tds[3].classList.add('obj-wa');
        }
    });
    window.scrollTo(0, document.body.scrollHeight);
}

function afterSubmitFailure(error) {
    gei('judging').style.display = 'none';
    if(error.responseJSON == undefined) {
        gei('was-error-submit').innerText = '문제가 생겨서 채점하지 못했어요.';
        gei('cannot-submit').style.display = 'flex';
        return;
    }
    res = error.responseJSON['result'];
    var msg;
    switch(res) {
        case 'dis':
            msg = '문제가 비활성화되어 있기 때문에'
            break;
        case 'unkn':
            msg = '문제가 생겨서'
            break;
        case 'intr':
            msg = '마지막 제출 후 1분이 지나지 않았기 때문에';
            break;
        case 'tiov':
            msg = '풀이에 한 시간 이상이 걸렸기 때문에';
            break;
        case 'astl':
            msg = '답안이 너무 길어서';
            break;
        case 'forb':
            msg = '문제를 제출하려면 로그인해야 해요. 인증에 실패했기 때문에'
            break;
        case "dber":
            msg = '문제가 생겨서'
            break;
        case "pras":
            msg = '제대로 제출되지 않아서'
            break;
        default:
            msg = "문제가 생겨서"
    }
    gei('was-error-submit').innerText = msg + ' 채점하지 못했어요.';
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

function disableInput() {
    answers = gei('answer-field').children;
    for(answerDiv of answers) {
        inputs = answerDiv.getElementsByTagName('input');
        for(input of inputs) {
            if(input.type == 'text') {
                input.disabled = true;
            }
            else if(input.type == 'button') {
                input.disabled = false;
            }
        }
    }
    return true;
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
    });
}

function resubmit(code) {
    gei('resubmit').disabled = true;
    setTimeout(()=>{
        gei('resubmit').disabled = false;
    }, 3000)
    submitAll(code);
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