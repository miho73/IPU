function onload(spec) {
    console.log(spec);
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
}