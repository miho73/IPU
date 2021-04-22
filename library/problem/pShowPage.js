var timex = 0;

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
    setInterval(()=>timex++, 1000);
}

function showHint() {
    document.getElementById('show-hint').style.display = 'none';
    document.getElementById('hint-view').style.display = 'block';
}

function markaswa(cod) {
    wa(cod);
    showAns();
}

function showAns() {
    document.getElementById('prob-control').style.display = 'none';
    document.getElementById('prob-control-nd').style.display = 'block';
    document.getElementById('after-sol').style.display = 'block';
    setTimeout(()=>document.getElementById('after-sol').style.transform = 'scaleY(1)', 2);

}

function wa(cod) {
    $.ajax({
        type: 'POST',
        url: '/problem/api/solrep',
        dataType: 'json',
        data: {
            code: cod,
            time: timex,
            res: 0
        },
        success: function(data) {

        },
        error: function(error) {

        }
    });
}

function ac() {
    $.ajax({
        type: 'POST',
        url: '/problem/api/solrep',
        dataType: 'json',
        data: {
            code: cod,
            time: timex,
            res: 1
        },
        success: function(data) {

        },
        error: function(error) {
            
        }
    });
}