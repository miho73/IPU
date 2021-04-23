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
            alert(err);
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
            alert(err);
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