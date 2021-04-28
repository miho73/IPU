function onload() {
    $.ajax({
        type: 'POST',
        url: '/profile/api/get-solved',
        dataType: 'json',
        data: {
            frm: 1,
            len: 10,
            id: document.getElementById('uid').innerText
        },
        success: function(data) {
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
        }
    });
}

$(function () {
    $('[data-toggle="ac"]').tooltip();
    $('[data-toggle="wa"]').tooltip();
})