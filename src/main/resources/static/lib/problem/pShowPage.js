var timex = 0;
var timer;

function onload(code) {
    MathJax.Hub.Queue(["Typeset",MathJax.Hub]);
    timer = setInterval(()=>timex++, 1000);
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
            let ec, suf = " 잠시 후에 다시 해보면 어떨까요?";
            switch(err.responseText) {
                case "trans":
                    ec = "문제가 발생했어요. "+suf;
                    break;
                case "forb":
                    ec = "권한이 없어요.";
                    break;
                case "time":
                    ec = "제출은 1분마다 할 수 있어요. 빨리 푸는데요?!";
                    break;
                default: 
                    ec = "제출하지 못했어요."+suf;
            }
            alert(ec);
            gei('ac-btn').disabled = false;
            gei('wa-btn').disabled = false;
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
            let ec, suf = " 잠시 후에 다시 해보면 어떨까요?";
            switch(err.responseText) {
                case "trans":
                    ec = "문제가 발생했어요. "+suf;
                    break;
                case "forb":
                    ec = "권한이 없어요.";
                    break;
                case "time":
                    ec = "제출은 1분마다 할 수 있어요. 빨리 푸는데요?!";
                    break;
                default:
                    ec = "제출하지 못했어요. "+suf;
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