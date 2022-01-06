function selectLocalImage(forwhat) {
    const input = document.createElement('input');
    input.setAttribute('type', 'file');
    input.click();
    input.onchange = function() {
        const fd = new FormData();
        const file = $(this)[0].files[0];
        fd.append('img', file);
        $.ajax({
            type: 'POST',
            enctype: 'multipart/form-data',
            url: '/problem/make/upload',
            dataType: 'json',
            data: fd,
            processData: false,
            contentType: false,
            success: function(data) {
                gei('image-upload-result').innerText = `이미지를 업로드했어요. 코드=${data.result}`;
            },
            error: function(err) {
                switch(err.responseJSON.result) {
                    case "file too large":
                        gei('image-upload-result').innerText = '업로드 가능한 크기는 최대 5MB에요. 크기를 줄여주세요.';
                    case "database error":
                        gei('image-upload-result').innerText = '이미지를 등록하지 못했어요. 잠시 후에 다시 시도해주세요.';
                    default:
                        gei('image-upload-result').innerText = '이미지를 등록하지 못했어요. 잠시 후에 다시 시도해주세요.';
                }
            }
        });
    };
}

function preview() {
    payload = [];
    payload[0] = gei('content').value;
    payload[1] = gei('solution').value;
    $.ajax({
       type: 'POST',
       url: '/problem/api/ipuac-translation',
       data: {
           code: JSON.stringify(payload)
       },
       success: function(data) {
           translated = JSON.parse(data);
           content = translated[0],
           exp = translated[1];
           namex = gei('name').value;
           gei('preview-container').style.display = "block";
           setTimeout(()=>{
               gei('preview-container').style.opacity = 1;
           }, 10);
           gei('preview-name').innerText = namex;
           gei('prev-content').innerHTML = content;
           gei('prev-solution').innerHTML = exp;
           MathJax.Hub.Queue(["Typeset",MathJax.Hub]);
           location.href = '#confirm'
       },
       error: function(err) {
           alert(err.responseText+". IPUAC 번역에 실패했어요.");
       }
    });
}
function confirme() {
    let proceed = confirm("문제를 등록할까요?");
    let precond = true;
    if(!proceed) return;
    gei('confirm').disabled = true;
    content = gei('content').value;
    exp = gei('solution').value;
    namep = gei('name').value;
    if(namep == "" || namep == undefined) {
        gei('name').classList.add('formthis');
        location.href = "#name";
        precond = false;
    }
    else {
        gei('name').classList.remove('formthis');
    }
    cat = gei('category');
    if(cat.selectedIndex == 0) {
        gei('category').classList.add('formthis');
        location.href = "#category";
        precond = false;
    }
    else {
        gei('category').classList.remove('formthis');
    }
    dif = gei('diffi');
    if(dif.selectedIndex == 0) {
        gei('diffi').classList.add('formthis');
        location.href = "#diffi";
        precond = false;
    }
    else {
        gei('diffi').classList.remove('formthis');
    }
    if(!precond) {
        gei('confirm').disabled = false;
        return;
    }
    $.ajax({
        type: 'POST',
        url: '/problem/register',
        data: {
            name: namep,
            cate: cat.value,
            diff: dif.value,
            cont: content,
            solu: exp,
            tags: JSON.stringify(tag),
            active: gei('pActive').checked
        },
        success: function(data) {
            window.onbeforeunload = undefined;
            window.location.href = "/problem";
        },
        error: function(err) {
            if(err.status == 403) {
                let x = confirm('문제를 추가하려면 로그인해야 해요.');
                if(x) {
                    window.location.href = `/login/?ret=problem/make`;
                }
            }
            gei('confirm').disabled = false;
        }
    });
}

let tag = [];
function tagHandle() {
    c = confirm("태그를 추가할까요?");
    if(!c) return;
    let ne = {
        key: "custom",
        content: gei('tag-input').value,
        color: gei('tag-fore').value,
        back: gei('tag-back').value
    };
    gei('tag-input').value = '';
    tag.push(ne);
    let ntag = document.createElement('span');
    ntag.classList.add('tag');
    ntag.classList.add(`tag-${ne.key}`);
    ntag.innerText = ne.content;
    ntag.style.backgroundColor = `#${ne.back}`;
    ntag.style.color = `#${ne.color}`;
    gei('tags-cont').appendChild(ntag);
    gei('edit-tags').value = JSON.stringify(tag);
}

function applyTagsJSONtoVar() {
    let json = gei('edit-tags').value;
    if(json == '') json = '';
    tag = JSON.parse(json);
    gei('tags-cont').innerHTML = '';
    tag.forEach(ne => {
        let ntag = document.createElement('span');
        ntag.classList.add('tag');
        ntag.classList.add(`tag-${ne.key}`);
        ntag.innerText = ne.content;
        ntag.style.backgroundColor = `#${ne.back}`;
        ntag.style.color = `#${ne.color}`;
        gei('tags-cont').appendChild(ntag);
    });
}

function updateColor(id) {
    gei(id).style.backgroundColor = `#${gei(id).value}`;
}

window.onbeforeunload = function (e) {
    e = e || window.event;
    if (e) {
        e.returnValue = '문제가 저장되지 않아요!. 정말 닫을까요?';
    }
    return '문제가 저장되지 않아요!. 정말 닫을까요?';
};