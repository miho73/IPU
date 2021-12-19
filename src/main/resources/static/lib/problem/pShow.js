const PROBLEM_PER_PAGE = 30;

EXCLUDE_LIST = ["cate"]

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
                gei('not').style.display = 'block';
                return;
            }
            const ppr = gei('addProb');
            data.forEach(datum => {
                let tr = document.createElement('tr');
                let number = document.createElement('td'); number.innerText = datum.code;
                let name = document.createElement('td');
                name.innerHTML = `<a class="prob-href" href="/problem/${datum.code}">${datum.name}</a>`;
                let infox = document.createElement('td'); infox.classList.add('tag-con');
                let info = document.createElement('div'); infox.appendChild(info);
                tr.appendChild(number);
                tr.appendChild(name);
                tr.appendChild(infox);
                datum.tags.forEach(ele=>{
                    if(EXCLUDE_LIST.includes(ele.key)) return;
                    let tag = document.createElement('span');
                    tag.classList.add('tag');
                    tag.classList.add(`tag-${ele.key}`);
                    switch(ele.key) {
                        case "diff":
                            tag.innerText = convertDiff(ele.content);
                            tag.style.backgroundColor = convertDiffColor(ele.content);
                            break;
                        default:
                            tag.innerText = ele.content;
                            if(ele.hasOwnProperty('back')) tag.style.backgroundColor = `#${ele.back}`;
                            else tag.style.backgroundColor = '#111';
                            if(ele.hasOwnProperty('color')) tag.style.color = `#${ele.color}`;
                            else tag.style.color = '#fff';
                    }
                    info.appendChild(tag);
                });
                if(!datum.active) {
                    let tag = document.createElement('span');
                    tag.classList.add('tag');
                    tag.classList.add(`tag-cannot-solve`);
                    tag.innerText = '제출 불가';
                    info.appendChild(tag);
                }
                ppr.appendChild(tr);
            });
        },
        error: function(error) {
            console.log(error);
        },
        complete: function() {
            if(getLen == -1) return;
            if(pg==0) {
                gei('prev').style.display = 'none';
                gei('pnsep').style.display = 'none';
            }
            else {
                gei('prev').setAttribute('href', `/problem/?page=${pg-1}`);
            }
            if(getLen < PROBLEM_PER_PAGE) {
                gei('next').style.display = 'none';
                gei('pnsep').style.display = 'none';
            }
            else {
                gei('next').setAttribute('href', `/problem/?page=${pg+1}`)
            }
        }
    });
}