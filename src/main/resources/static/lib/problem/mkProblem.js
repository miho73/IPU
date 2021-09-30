const toolbar_conf = 
{
    toolbar: [
        ['bold','italic','underline','strike'],
        [{script: 'sub'},{script: 'super'}],
        [{header: '1'},{header: '2'},'blockquote',{align: []}],
        ['link','image'],
        [{list: 'ordered'},{list: 'bullet'}],
        [{color: []},{ background: []}],
    ],
    table: true
};

const contedit = new Quill('#editor-cont', {
    placeholder: '문제 내용',
    modules: toolbar_conf,
    theme: 'snow'
});
const expedit = new Quill('#editor-exp', {
    placeholder: '문제 풀이',
    modules: toolbar_conf,
    theme: 'snow'
});
const ansedit = new Quill('#editor-ans', {
    placeholder: '정답',
    modules: toolbar_conf,
    theme: 'snow'
});
const hintedit = new Quill('#editor-hint', {
    placeholder: '힌트',
    modules: toolbar_conf,
    theme: 'snow'
});

contedit.getModule('toolbar').addHandler('image', function() {
    selectLocalImage(contedit);
});
expedit.getModule('toolbar').addHandler('image', function() {
    selectLocalImage(expedit);
});
ansedit.getModule('toolbar').addHandler('image', function() {
    selectLocalImage(ansedit);
});
hintedit.getModule('toolbar').addHandler('image', function() {
    selectLocalImage(hintedit);
});
const conttable = contedit.getModule('table');
const exptable = expedit.getModule('table');
const anstable = ansedit.getModule('table');
const hinttable = hintedit.getModule('table');

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
            data: fd,
            processData: false,
            contentType: false,
            success: function(data) {
                const range = forwhat.getSelection();
                forwhat.insertEmbed(range.index, 'image', '/problem/lib/'+data);
            },
            error: function(err) {
                switch(err.responseText) {
                    case "size":
                        alert("최대 5MB까지 올릴 수 있습니다.");
                }
            }
        });
    };
}

function ctrlTable(code) {
    gei(`tab-${code}`).style.display = 'flex';
    gei(`tbtn-${code}`).style.display = 'none';
}

function addTable(table) {
    table.insertTable(1,1);
}
function addRowAbove(table) {
    table.insertRowAbove();
}
function addRowBelow(table) {
    table.insertRowBelow();
}
function addColumnLeft(table) {
    table.insertColumnLeft();
}
function addColumnRight(table) {
    table.insertColumnRight();
}
function deleteTable(table) {
    table.deleteTable();
}
function deleteRow(table) {
    table.deleteRow();
}
function deleteColumn(table) {
    table.deleteColumn();
}

function preview() {
    namex = gei('name').value;
    content = gei('editor-cont').getElementsByClassName('ql-editor')[0].innerHTML;
    exp = gei('editor-exp').getElementsByClassName('ql-editor')[0].innerHTML;
    ans = gei('editor-ans').getElementsByClassName('ql-editor')[0].innerHTML;
    hint = gei('editor-hint').getElementsByClassName('ql-editor')[0].innerHTML;
    gei('preview-container').style.display = "block";
    setTimeout(()=>{
        gei('preview-container').style.opacity = 1;
    }, 10);
    $('#preview-name').text(namex);
    $('#preview-content').html(content);
    $('#preview-exp').html(exp);
    $('#preview-ans').html(ans);
    $('#preview-hint').html(hint);
    let toAdd = gei('custon-tabs');
    let div = document.createElement('hr');
    div.classList.add('prob-hr');
    let divProb = document.createElement('hr');
    divProb.classList.add('prob-div-hr');
    toAdd.innerHTML = '';
    for(let i=0; i<extrTabCnt; i++) {
        let title = document.createElement('span');
        let cont = document.createElement('div');
        title.classList.add('prob-title');
        title.innerText = gei(`extr-tab-name-${i}`).value;
        cont.classList.add('mathjax');
        cont.classList.add('prob-appl');
        cont.classList.add('ql-editor');
        cont.classList.add('ql-shower');
        cont.innerHTML = gei(`extr-tab-edit-${i}`).getElementsByClassName('ql-editor')[0].innerHTML;
        toAdd.appendChild(title);
        toAdd.appendChild(div);
        toAdd.appendChild(cont);
        toAdd.appendChild(divProb);
    }
    MathJax.Hub.Queue(["Typeset",MathJax.Hub]);
    location.href = '#confirm'
}
function confirme() {
    let proceed = confirm("문제를 등록할까요?");
    let precond = true;
    if(!proceed) return;
    gei('confirm').disabled = true;
    content = gei('editor-cont').getElementsByClassName('ql-editor')[0].innerHTML;
    exp = gei('editor-exp').getElementsByClassName('ql-editor')[0].innerHTML;
    ans = gei('editor-ans').getElementsByClassName('ql-editor')[0].innerHTML;
    hintx = gei('editor-hint').getElementsByClassName('ql-editor')[0].innerHTML;
    namep = gei('name').value;
    tabs = Array(extrTabCnt);
    for(let i=0; i<extrTabCnt; i++) {
        if(gei(`extr-tab-name-${i}`) == '') {
            proceed = false;
            break;
        }
        tabs[i] = {
            name: gei(`extr-tab-name-${i}`).value,
            content: gei(`extr-tab-edit-${i}`).getElementsByClassName('ql-editor')[0].innerHTML
        };
    }
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
            answ: ans,
            hint: hintx,
            hasH: gei('hashint').checked,
            extr: JSON.stringify(tabs),
            tags: JSON.stringify(tag)
        },
        success: function(data) {
            window.onbeforeunload = undefined;
            window.location.href = "/problem";
        },
        error: function(err) {
            if(err.status == 403) {
                let x = confirm('문제를 추가하려면 로그인해야 합니다.');
                if(x) {
                    window.location.href = `/login/?ret=problem/make`;
                }
            }
            gei('confirm').disabled = false;
        }
    });
}
function hintSH() {
    if(gei('hashint').checked) {
        gei('hint-edit-container').style.display = "block";
        setTimeout(()=>{
            gei('hint-edit-container').style.maxHeight = "500px"
        }, 5);
        gei('hint-prev').style.display = "block";
        setTimeout(()=>{
            gei('hint-prev').style.maxHeight = "500px"
        }, 5);
    }
    else {
        gei('hint-edit-container').style.maxHeight = "0"
        setTimeout(()=>{
            gei('hint-edit-container').style.display = "none";
        }, 500);
        gei('hint-prev').style.maxHeight = "0"
        setTimeout(()=>{
            gei('hint-prev').style.display = "none";
        }, 500);
    }
}
var extrTabCnt = 0;
var extrTableArr = [];
function addExtr() {
    let conf = confirm('새 탭을 추가할까요?');
    if(!conf) return;
    const toAdd = gei('extr-edit-cont');
    const rt_div = document.createElement('div');
    const name = document.createElement('input');
    const edit_div = document.createElement('div');
    const controls_div = document.createElement('div'); 
    const table_control = document.createElement('div');
    const open_table_ctrl = document.createElement('button');
    const table_menu = document.createElement('div');
    const button = [];
    for(let i=0; i<8; i++) {
        button.push(document.createElement('button'));
        button[i].classList.add('table-btn');
        table_menu.appendChild(button[i]);
    }
    let priCode = extrTabCnt+5;
    rt_div.id = `extr-tab-cont-${extrTabCnt}`;
    edit_div.id = `extr-tab-edit-${extrTabCnt}`;
    name.type = 'textfield';
    controls_div.classList.add('extr-ctrl-div');
    name.placeholder = '탭 이름';
    name.classList.add('extr-tab-name');
    name.id = `extr-tab-name-${extrTabCnt}`;
    table_control.classList.add('table-controller');
    controls_div.appendChild(name);
    open_table_ctrl.id = `tbtn-${priCode}`;
    open_table_ctrl.classList.add('ctrl-table');
    open_table_ctrl.setAttribute('onclick',`ctrlTable(${priCode})`);
    open_table_ctrl.innerText='> Open table editor'
    table_menu.classList.add('table-menu');
    table_menu.id = `tab-${priCode}`;
    table_control.appendChild(open_table_ctrl);
    table_control.appendChild(table_menu);
    rt_div.classList.add('extr-tab-edit-div');
    rt_div.appendChild(controls_div);
    rt_div.appendChild(edit_div);
    rt_div.append(table_control);
    toAdd.appendChild(rt_div);
    var extr = new Quill(`#extr-tab-edit-${extrTabCnt}`, {
        placeholder: '추가 탭',
        modules: toolbar_conf,
        theme: 'snow'
    });
    extr.getModule('toolbar').addHandler('image', function() {
        selectLocalImage(extr);
    });
    extrTableArr.push(extr.getModule('table'));
    button[0].setAttribute('onclick', `addTable      (extrTableArr[${extrTabCnt}])`); button[0].innerText='테이블 추가';
    button[1].setAttribute('onclick', `addColumnLeft (extrTableArr[${extrTabCnt}])`); button[1].innerText='왼쪽 헹 추가';
    button[2].setAttribute('onclick', `addColumnRight(extrTableArr[${extrTabCnt}])`); button[2].innerText='오른쪽 행 추가';
    button[3].setAttribute('onclick', `addRowAbove   (extrTableArr[${extrTabCnt}])`); button[3].innerText='위에 열 추가';
    button[4].setAttribute('onclick', `addRowBelow   (extrTableArr[${extrTabCnt}])`); button[4].innerText='아래 열 추가';
    button[5].setAttribute('onclick', `deleteColumn  (extrTableArr[${extrTabCnt}])`); button[5].innerText='행 삭제';
    button[6].setAttribute('onclick', `deleteRow     (extrTableArr[${extrTabCnt}])`); button[6].innerText='열 삭제';
    button[7].setAttribute('onclick', `deleteTable   (extrTableArr[${extrTabCnt}])`); button[7].innerText='테이블 삭제';
    extrTabCnt++;
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
}

function updateColor(id) {
    gei(id).style.backgroundColor = `#${gei(id).value}`;
}

window.onbeforeunload = function (e) {
    e = e || window.event;
    if (e) {
        e.returnValue = '편집사항이 저장되지 않습니다. 정말 닫을까요?';
    }
    return '편집사항이 저장되지 않습니다. 정말 닫을까요?';
};