const PROBLEM_PER_PAGE = 50;

function load(pg) {
    let getLen = -1;
    $.ajax({
        type: 'POST',
        url: '/problem/api/get',
        dataType: 'json',
        data: {
            frm: pg*PROBLEM_PER_PAGE+1,
            len: PROBLEM_PER_PAGE
        },
        success: function(data) {
            getLen = data.length
            if(getLen == 0) {
                document.getElementById('not').style.display = 'block';
                return;
            }
            data.forEach(datum => {
                let tr = document.createElement('tr');
                let number = document.createElement('td'); number.innerText = datum.code;
                let name = document.createElement('td'); 
                name.innerHTML = `<a class="prob-href" href="/problem/${datum.code}">${datum.name}</a>`
                let info = document.createElement('td'); 
                //let answers = document.createElement('td'); answers.innerText = datum.anss
                let cate = document.createElement('td'); cate.innerText = convertSubj(datum.cate);
                tr.appendChild(number);
                tr.appendChild(name);
                tr.appendChild(info);
                //tr.appendChild(answers);
                tr.appendChild(cate);
                datum.tags.forEach(ele=>{
                    let tag = document.createElement('span');
                    tag.classList.add('tag');
                    tag.classList.add(`tag-${ele.key}`);
                    switch(ele.key) {
                        case "diff":
                            tag.innerText = convertDiff(ele.content);
                            tag.style.backgroundColor = convertDiffColor(ele.content);
                            break;
                        case "cate":
                            tag.innerText = convertSubj(ele.content);
                            break;
                        default:
                            tag.innerText = ele.content;
                    }
                    info.appendChild(tag);
                });
                document.getElementById('addProb').appendChild(tr);
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
            if(getLen < PROBLEM_PER_PAGE) {
                document.getElementById('next').style.display = 'none';
                document.getElementById('pnsep').style.display = 'none';
            }
            else {
                document.getElementById('next').setAttribute('href', `/problem/?page=${pg+1}`)
            }
        }
    });
}