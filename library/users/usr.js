const ACC_PER_PAGE = 50;

function load(pg) {
    let getLen = -1;
    $.ajax({
        type: 'POST',
        url: '/users/api/rank',
        dataType: 'json',
        data: {
            frm: pg*ACC_PER_PAGE+1,
            len: ACC_PER_PAGE
        },
        success: function(data) {
            getLen = data.length
            if(getLen == 0) {
                document.getElementById('not').style.display = 'block';
                return;
            }
            let cnt = 1;
            data.forEach(datum => {
                let tr = document.createElement('tr');
                let number = document.createElement('td'); number.innerText = cnt; cnt++;
                let name = document.createElement('td');
                name.innerHTML = `<a class="prob-href" href="/profile/${datum.id}">${datum.uname}</a>`
                let bio = document.createElement('td'); bio.innerText = datum.bio;
                let exp = document.createElement('td'); exp.innerText = datum.exp;
                lvcode = getLevelCode(datum.exp);
                if(lvcode == 8) {
                    exp.classList.add('rainbow');
                }
                else exp.style.color = convertDiffColor(codeTable[lvcode]);
                tr.appendChild(number);
                tr.appendChild(name);
                tr.appendChild(bio);
                tr.appendChild(exp);
                document.getElementById('addUsr').appendChild(tr);
            });
        },
        error: function(error) {
            console.log(error);
        },
        complete: function() {
            if(getLen == -1) return;
            if(pg==0) {
                document.getElementById('prev').style.display = 'none';
                document.getElementById('pnsep').style.display = 'none';
            }
            else {
                document.getElementById('prev').setAttribute('href', `/problem/?page=${pg-1}`);
            }
            if(getLen < ACC_PER_PAGE) {
                document.getElementById('next').style.display = 'none';
                document.getElementById('pnsep').style.display = 'none';
            }
            else {
                document.getElementById('next').setAttribute('href', `/problem/?page=${pg+1}`)
            }
        }
    });
}