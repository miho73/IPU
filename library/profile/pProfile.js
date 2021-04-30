const PROBLEM_PER_PAGE = 30;

cutTable = [0, 3000, 10000, 20000, 35000, 60000, 90000, 150000]

codeTableRl = {
    1: 'Unrated',
    2: 'Bronze',
    3: 'Silver',
    4: 'Gold',
    5: 'Sapphire',
    6: 'Ruby',
    7: 'Diamond',
    8: 'Infinity'
}

codeTable = {
    1: 'unra',
    2: 'broz',
    3: 'silv',
    4: 'gold',
    5: 'sapp',
    6: 'ruby',
    7: 'diam',
    8: 'redd'
}

function load(pg) {
    let getLen = -1;
    $.ajax({
        type: 'POST',
        url: '/profile/api/get-solved',
        dataType: 'json',
        data: {
            frm: pg*PROBLEM_PER_PAGE+1,
            len: PROBLEM_PER_PAGE,
            id: document.getElementById('uid').innerText
        },
        success: function(data) {
            getLen = data.length;
            if(getLen == 0) {
                document.getElementById('not').style.display = 'block';
                return;
            }
            data.forEach(datum => {
                let tr = document.createElement('tr');
                let number = document.createElement('td'); number.innerText = datum.code;
                let name = document.createElement('td'); name.innerHTML = `<a class="prob-href" href="/problem/${datum.code}">${datum.name}</a>`
                let info = document.createElement('td');
                let solved = document.createElement('td'); solved.innerText = datum.solt+"초";
                let nowt = new Date(datum.sol);
                let solving = document.createElement('td'); solving.innerText = `${nowt.getFullYear()}년 ${(nowt.getMonth()+1).zf(2)}월 ${nowt.getDate().zf(2)}일 ${nowt.getHours().zf(2)}시 ${nowt.getMinutes().zf(2)}분`;
                tr.appendChild(number);
                tr.appendChild(name);
                tr.appendChild(info);
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
                document.getElementById('solveds').appendChild(tr);
            });
        },
        error: function(error) {
            $('html').html(error.responseText);
        },
        complete: function() {
            if(getLen == -1) return;
            if(pg==0) {
                document.getElementById('prev').style.display = 'none';
                document.getElementById('pnsep').style.display = 'none';
            }
            else {
                document.getElementById('prev').setAttribute('href', `/profile/${document.getElementById('uid').innerText}/?page=${pg-1}`);
            }
            if(getLen < PROBLEM_PER_PAGE) {
                document.getElementById('next').style.display = 'none';
                document.getElementById('pnsep').style.display = 'none';
            }
            else {
                document.getElementById('next').setAttribute('href', `/profile/${document.getElementById('uid').innerText}/?page=${pg+1}`)
            }
            let exp = document.getElementById('experi-visib').innerText;
            let levelCode = 8;
            for(let i=1; i<=7; i++) {
                if(exp < cutTable[i]) {
                    levelCode = i;
                    break;
                }
            }
            let lv_name = codeTable[levelCode];
            document.getElementById('exp-name').innerText = codeTableRl[levelCode];
            if(levelCode == 8) {
                document.getElementById('next-lv').innerText = `YOU HAVE HIGHEST`;
            }
            else {
                if(levelCode == 7) {
                    document.getElementById('next-lv').innerText = `Ultimate 승급까지 ${cutTable[levelCode]-exp}`;
                }
                else {
                    document.getElementById('next-lv').innerText = `${codeTableRl[levelCode+1]} 승급까지 ${cutTable[levelCode]-exp}`;
                }
            }
            setTimeout(()=>{
                if(levelCode == 8) {
                    document.getElementById('experi-visib').style.width = `100%`;
                }
                else document.getElementById('experi-visib').style.width = `${(exp-cutTable[levelCode-1])/(cutTable[levelCode]-cutTable[levelCode-1])*100}%`;
            }, 200);
            document.getElementById('experi-visib').classList.add(`prog-exp-${lv_name}`);
        }
    });
}

$(function () {
    $('[data-toggle="ac"]').tooltip();
    $('[data-toggle="wa"]').tooltip();
})