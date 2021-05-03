var timex = 0;
var timer;

function onload(spec) {
    let toAdd = document.getElementById('custom-tabs');
    let div = document.createElement('hr');
    div.classList.add('prob-hr');
    let divProb = document.createElement('hr');
    divProb.classList.add('prob-div-hr');
    spec.forEach(spe => {
        let title = document.createElement('span');
        let cont = document.createElement('div');
        title.classList.add('prob-title');
        title.innerText = spe.name
        cont.classList.add('mathjax');
        cont.innerHTML = spe.content;
        toAdd.appendChild(title);
        toAdd.appendChild(div);
        toAdd.appendChild(cont);
        toAdd.appendChild(divProb);
    });
    timer = setInterval(()=>timex++, 1000);
}

function showHint() {
    document.getElementById('show-hint').style.display = 'none';
    document.getElementById('hint-view').style.display = 'block';
}

function markaswa(cod) {
    wa(cod);
    showAns();
    setTimeout(()=>document.getElementById('prob-control-nd').style.opacity = 0, 2);
}

function showAns() {
    clearInterval(timer);
    document.getElementById('prob-control').style.display = 'none';
    document.getElementById('prob-control-nd').style.display = 'block';
    document.getElementById('after-sol').style.display = 'block';
    setTimeout(()=>document.getElementById('after-sol').style.transform = 'scaleY(1)', 2);
}

function wa(cod) {
    $.ajax({
        type: 'POST',
        url: '/problem/api/solrep',
        data: {
            code: cod,
            time: timex,
            res: 0
        },
        success: function(data) {
            activeFinal(false);
        },
        error: function(err) {
            let ec, suf = " 잠시 후 다시 시도해주세요";
            switch(err.responseText) {
                case "trans":
                    ec = "데이터베이스 트랜잭션을 개시하는데 실패했습니다."+suf;
                    break;
                case "forbidden":
                    ec = "권한이 없습니다."+suf;
                    break;
                case "sdb":
                    ec = "풀이를 기록할 수 없습니다."+suf;
                    break;
                case "expUpd":
                    ec = "경험치를 업데이트할 수 없습니다."+suf;
                    break;
                case "usrQ":
                    ec = "사용자 정보를 불러올 수 없습니다. "+suf;
                    break;
                case "time":
                    ec = "제출은 1분마다 할 수 있습니다.";
                    break;
                default:
                    ec = "예기치 못한 오류가 발생했습니다. "+suf;
            }
            alert(ec);
        }
    });
}

function ac(cod) {
    $.ajax({
        type: 'POST',
        url: '/problem/api/solrep',
        data: {
            code: cod,
            time: timex,
            res: 1
        },
        success: function(data) {
            activeFinal(true);
        },
        error: function(err) {
            let ec, suf = " 잠시 후 다시 시도해주세요";
            switch(err.responseText) {
                case "trans":
                    ec = "데이터베이스 트랜잭션을 개시하는데 실패했습니다."+suf;
                    break;
                case "forbidden":
                    ec = "권한이 없습니다."+suf;
                    break;
                case "sdb":
                    ec = "풀이를 기록할 수 없습니다."+suf;
                    break;
                case "expUpd":
                    ec = "경험치를 업데이트할 수 없습니다."+suf;
                    break;
                case "usrQ":
                    ec = "사용자 정보를 불러올 수 없습니다. "+suf;
                    break;
                case "time":
                    ec = "제출은 1분마다 할 수 있습니다.";
                    break;
                default:
                    ec = "예기치 못한 오류가 발생했습니다. "+suf;
            }
            alert(ec);
        }
    });
}

function activeFinal(cor) {
    if(cor) {
        document.getElementById('final-sol').style.color = '#099134 ';
    }
    else {
        document.getElementById('final-sol').style.color = '#b1230a';
    }
    document.getElementById('prob-control-nd').style.opacity = 0;
    setTimeout(()=>{
        document.getElementById('prob-control-nd').style.display = 'none';
        document.getElementById('final-sol').style.display = 'block';
        setTimeout(()=>{
            document.getElementById('final-sol').style.opacity=1;
        }, 5);
    }, 200);
    document.getElementById('sol-time').innerText = timex;
}