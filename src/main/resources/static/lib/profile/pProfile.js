const PROBLEM_PER_PAGE = 30;

function load(pg) {
    let getLen = -1;
    $.ajax({
        type: 'POST',
        url: '/api/get-solved',
        dataType: 'json',
        data: {
            frm: pg*PROBLEM_PER_PAGE+1,
            len: PROBLEM_PER_PAGE,
            id: gei('uid').innerText
        },
        success: function(data) {
            getLen = data.length;
            if(getLen == 0) {
                gei('not').style.display = 'block';
                return;
            }
            let cnt = 0;
            data.forEach(datum => {
                let tr = document.createElement('tr');
                let number = document.createElement('td'); number.innerText = pg*PROBLEM_PER_PAGE+cnt+1; cnt++;
                let name = document.createElement('td'); name.innerHTML = `<a class="prob-href" href="/problem/${datum.code}">${datum.name}</a>`
                let infox = document.createElement('td'); infox.classList.add('tag-con');
                let info = document.createElement('div'); infox.appendChild(info);
                let solved = document.createElement('td'); solved.innerText = datum.solt+"초";
                let nowt = new Date(datum.sol);
                let solving = document.createElement('td'); solving.innerText = `${nowt.getFullYear()}년 ${(nowt.getMonth()+1).zf(2)}월 ${nowt.getDate().zf(2)}일 ${nowt.getHours().zf(2)}시 ${nowt.getMinutes().zf(2)}분`;
                tr.appendChild(number);
                tr.appendChild(name);
                tr.appendChild(infox);
                tr.appendChild(solved);
                tr.appendChild(solving);
                let tagc = document.createElement('span');
                if(datum.cor) {
                    tagc.classList.add('tag');
                    tagc.innerText = "AC";
                    tagc.classList.add(`tag-ac`);
                    tagc.setAttribute('data-toggle', 'ac');
                    tagc.setAttribute('title', 'Accepted');
                    tagc.setAttribute('data-placement', 'bottom');
                }
                else {
                    tagc.classList.add('tag');
                    tagc.innerText = "WA";
                    tagc.classList.add(`tag-wa`);
                    tagc.setAttribute('data-toggle', 'wa');
                    tagc.setAttribute('title', 'Wrong');
                    tagc.setAttribute('data-placement', 'bottom');
                }
                info.appendChild(tagc);
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
                gei('solveds').appendChild(tr);
            });
        },
        error: function(error) {
            $('html').html(error.responseText);
        },
        complete: function() {
            if(getLen == -1) return;
            if(pg==0) {
                gei('prev').style.display = 'none';
                gei('pnsep').style.display = 'none';
            }
            else {
                gei('prev').setAttribute('href', `/profile/${gei('uid').innerText}/?page=${pg-1}`);
            }
            if(getLen < PROBLEM_PER_PAGE) {
                gei('next').style.display = 'none';
                gei('pnsep').style.display = 'none';
            }
            else {
                gei('next').setAttribute('href', `/profile/${gei('uid').innerText}/?page=${pg+1}`)
            }
            let exp = gei('experi-visib').innerText;
            let levelCode = getLevelCode(exp);
            let lv_name = codeTable[levelCode];
            gei('exp-name').innerText = codeTableRl[levelCode];
            if(levelCode == 8) {
                gei('next-lv').innerText = `YOU HAVE THE HIGHEST`;
            }
            else {
                if(levelCode == 7) {
                    gei('next-lv').innerText = `Ultimate 승급까지 ${cutTable[levelCode]-exp}`;
                }
                else {
                    gei('next-lv').innerText = `${codeTableRl[levelCode+1]} 승급까지 ${cutTable[levelCode]-exp}`;
                }
            }
            setTimeout(()=>{
                if(levelCode == 8) {
                    gei('experi-visib').style.width = `100%`;
                }
                else gei('experi-visib').style.width = `${(exp-cutTable[levelCode-1])/(cutTable[levelCode]-cutTable[levelCode-1])*100}%`;
            }, 200);
            gei('experi-visib').classList.add(`prog-exp-${lv_name}`);
        }
    });
}

$(function () {
    $('[data-toggle="ac"]').tooltip();
    $('[data-toggle="wa"]').tooltip();
})