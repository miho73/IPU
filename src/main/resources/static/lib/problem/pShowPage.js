var timex = 0;
var timer;

function onload(specx) {
    let toAdd = gei('custom-tabs');
    let div = document.createElement('hr');
    div.classList.add('prob-hr');
    let divProb = document.createElement('hr');
    divProb.classList.add('prob-div-hr');
    spec = JSON.parse(specx);
    spec.forEach(spe => {
        let title = document.createElement('span');
        let cont = document.createElement('div');
        title.classList.add('prob-title');
        title.innerText = spe.name
        cont.classList.add('mathjax');
        cont.classList.add('prob-appl');
        cont.classList.add('ql-editor');
        cont.classList.add('ql-shower');
        cont.innerHTML = spe.content;
        toAdd.appendChild(title);
        toAdd.appendChild(div);
        toAdd.appendChild(cont);
        toAdd.appendChild(divProb);
    });
    timer = setInterval(()=>timex++, 1000);
}

function showHint() {
    gei('show-hint').style.display = 'none';
    gei('hint-view').style.display = 'block';
}

function markaswa(cod) {
    wa(cod);
    showAns();
    setTimeout(()=>gei('prob-control-nd').style.opacity = 0, 2);
}

function showAns() {
    clearInterval(timer);
    gei('show-ans-btn').disabled = true;
    gei('prob-control').style.display = 'none';
    gei('prob-control-nd').style.display = 'block';
    gei('after-sol').style.display = 'block';
    setTimeout(()=>gei('after-sol').style.transform = 'scaleY(1)', 2);
}

function wa(cod) {
    gei('ac-btn').disabled = true;
    gei('wa-btn').disabled = true;
    $.ajax({
        type: 'POST',
        url: '/problem/api/solrep',
        data: {
            code: cod,
            time: timex,
            res: 0
        },
        success: function() {
            activeFinal(false);
        },
        error: function(err) {
            let ec, suf = " 잠시 후 다시 시도해주세요";
            switch(err.responseText) {
                case "trans":
                    ec = "문제가 발생했습니다."+suf;
                    break;
                case "forb":
                    ec = "권한이 없습니다."+suf;
                    break;
                case "time":
                    ec = "제출은 1분마다 할 수 있습니다.";
                    break;
                default:
                    ec = "문제가 발생했습니다."+suf;
            }
            alert(ec);
        }
    });
}

function ac(cod) {
    gei('ac-btn').disabled = true;
    gei('wa-btn').disabled = true;
    $.ajax({
        type: 'POST',
        url: '/problem/api/solrep',
        data: {
            code: cod,
            time: timex,
            res: 1
        },
        success: function() {
            activeFinal(true);
        },
        error: function(err) {
            let ec, suf = " 잠시 후 다시 시도해주세요";
            switch(err.responseText) {
                case "trans":
                    ec = "문제가 발생했습니다."+suf;
                    break;
                case "forb":
                    ec = "권한이 없습니다."+suf;
                    break;
                case "time":
                    ec = "제출은 1분마다 할 수 있습니다.";
                    break;
                default:
                    ec = "문제가 발생했습니다."+suf;
            }
            alert(ec);
            gei('ac-btn').disabled = false;
            gei('wa-btn').disabled = false;
        }
    });
}

function activeFinal(cor) {
    if(cor) {
        gei('final-sol').style.color = '#099134';
    }
    else {
        gei('final-sol').style.color = '#b1230a';
    }
    gei('prob-control-nd').style.opacity = 0;
    setTimeout(()=>{
        gei('prob-control-nd').style.display = 'none';
        gei('final-sol').style.display = 'block';
        setTimeout(()=>{
            gei('final-sol').style.opacity=1;
        }, 5);
    }, 200);
    gei('sol-time').innerText = timex;
}