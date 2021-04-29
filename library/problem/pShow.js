$.ajax({
    type: 'POST',
    url: '/problem/api/get',
    dataType: 'json',
    data: {
        frm: 1,
        len: 10
    },
    success: function(data) {
        data.forEach(datum => {
            let tr = document.createElement('tr');
            let number = document.createElement('td'); number.innerText = datum.code;
            let name = document.createElement('td'); 
            name.innerHTML = `<a class="prob-href" href="/problem/${datum.code}">${datum.name}</a>`
            let info = document.createElement('td'); 
            let answers = document.createElement('td'); answers.innerText = datum.anss
            let cate = document.createElement('td'); cate.innerText = convertSubj(datum.cate);
            tr.appendChild(number);
            tr.appendChild(name);
            tr.appendChild(info);
            tr.appendChild(answers);
            tr.appendChild(cate);
            document.getElementById('addProb').appendChild(tr);
        });
    },
    error: function(error) {
        console.log(error);
    }
});