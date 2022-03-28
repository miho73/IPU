var timex = 0;
var timer;

var judgeType;

var acwa;
var answer;

function load(_judgeType) {
    MathJax.Hub.Queue(["Typeset",MathJax.Hub]);
    timer = setInterval(()=>timex++, 1000);
    judgeType = _judgeType;
}

function submitSolution(code) {
    clearInterval(timer);

    if(judgeType == 1) {
        gei('answer').disabled = true;
    }
    else if(judgeType == 2) {
        gei('answer-frac1').disabled = true;
        gei('answer-frac2').disabled = true;
    }
    gei('show-ans-btn').disabled = true;

    gei('answer-context').style.display = 'block';
    setTimeout(()=>gei('answer-context').style.transform = 'scaleY(1)', 2);

    showFinalDisplay();

    if(judgeType == 0) {
        gei('self-judge').style.display = 'block';
    }
    else {
        gei('judge-result').style.display = 'block';
        switch(judgeType) {
            case 1:
                answer = gei('answer').value;
                break;
            case 2:
                answer = gei('answer-frac1').value + '/' + gei('answer-frac2').value;
                break;

        }
        $.ajax({
            method: 'POST',
            url: '/problem/api/solution/post',
            dataType: 'json',
            data: {
                code: code,
                time: timex,
                answer: answer
            },
            success: afterSubmitSuccess,
            error: afterSubmitFailure
        });
    }
}

function ac(code) {
    acwa = true;
    submitPerspective(code);
}
function wa(code) {
    acwa = false;
    submitPerspective(code);
}
function submitPerspective(code) {
    gei('wa-btn').disabled = true;
    gei('ac-btn').disabled = true;
    gei('self-judge').style.display = 'none';
    gei('judge-result').style.display = 'block';

    $.ajax({
        method: 'POST',
        url: '/problem/api/solution/post',
        dataType: 'json',
        data: {
            code: code,
            time: timex,
            answer: acwa
        },
        success: afterSubmitSuccess,
        error: afterSubmitFailure
    });
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
        gei('sol-comment').innerText = "문제가 생겨 정답 여부를 파악할 수 없었어요.";
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

function resubmit(code) {
    gei('resubmit').disabled = true;
    if(judgeType == 0) {
        $.ajax({
            method: 'POST',
            url: '/problem/api/solution/post',
            dataType: 'json',
            data: {
                code: code,
                time: timex,
                answer: acwa
            },
            success: function(res) {
                gei('cannot-submit').style.display = 'none';
                afterSubmitSuccess(res)
            },
            error: afterSubmitFailure,
            complete: function() {
                gei('resubmit').disabled = false;
            }
        });
    }
    else {
        $.ajax({
            method: 'POST',
            url: '/problem/api/solution/post',
            dataType: 'json',
            data: {
                code: code,
                time: timex,
                answer: answer
            },
            success: function(res) {
                gei('cannot-submit').style.display = 'none';
                afterSubmitSuccess(res)
            },
            error: afterSubmitFailure,
            complete: function() {
                gei('resubmit').disabled = false;
            }
        });
    }
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