function loaded() {
    invRef('GET');
}

function invRef(query) {
    pascal = gei('addInv');
    pascal.innerHTML = "";
    $.ajax({
        method: "POST",
        dataType: "json",
        url: "/root/api/inv",
        data: {
            q: query
        },
        success: function(data) {
            cod = data.codes;
            cnt = 1;
            cod.forEach(ele => {
                let tr = document.createElement('tr');
                let number = document.createElement('td'); number.innerText = cnt;
                let code = document.createElement('td'); code.innerText = ele;
                tr.appendChild(number);
                tr.appendChild(code);
                pascal.appendChild(tr);
                cnt++;
            });
        },
        error: function(error) {
            let tr = document.createElement('tr');
            let number = document.createElement('td'); number.innerText = "ERROR!";
            let code = document.createElement('td'); code.innerText = "!"+error.responseText;
            tr.appendChild(number);
            tr.appendChild(code);
            pascal.appendChild(tr);
        },
        complete: function() {
            gei('invQuery').value = "";
        }
    });
}

function invSend() {
    cmd = gei('invQuery').value;
    invRef(cmd);
}

function sendPermQuery() {
    $.ajax({
        method: "POST",
        dataType: "text",
        url: "/root/api/perm",
        data: {
            q: gei('usrQuery').value
        },
        success: function(data) {
            gei('usrRes').innerText = data;
        },
        error: function(err) {
            let txt = "예기치 못한 오류"
            switch(err.responseText) {
                case "dbquery":
                    txt = "DB 쿼리 오류"
                    break;
                case "uk":
                    txt = "알 수 없는 명령"
                    break;
                case "unc":
                    txt = "불완전한 명령"
                    break;
                case "perm":
                    txt = "권한 거부"
                    break;
                case "permFormat":
                    txt = "권한코드 형식 오류"
                    break;
                case "self":
                    txt = "자기 자신의 권한은 수정할 수 없어요.";
                    break;
            }
            gei('usrRes').innerText = txt;
        }
    });
}

function sendUlogQuery() {
    $.ajax({
        method: "POST",
        dataType: "text",
        url: "/root/api/deauth",
        data: {
            id: gei('usrUnlog').value
        },
        success: function(data) {
            gei('uLogRes').innerText = data;
        },
        error: function(err) {
            let txt = "예기치 못한 오류"
            switch(err.responseText) {
                case "perm":
                    txt="권한 거부";
                    break;
                case "usr":
                    txt="세션을 찾을 수 없음";
                    break;
            }
            gei('uLogRes').innerText = txt;
        }
    });
}

problemElements = ['problem_code', 'problem_name', 'problem_category', 'problem_difficulty', 'problem_content', 'problem_solution', 'tags', 'active', 'author_name', 'added_at', 'last_modified'];
function pReq() {
    html = '';
    $.ajax({
        method: "POST",
        dataType: "json",
        url: "/root/api/pReq",
        data: {
            code: gei('pQueryCode').value
        },
        success: function(data) {
            cnt = 0;
            problemElements.forEach((ele)=>{
                html += '<td>'+data[ele]+'</td>';
            });
        },
        error: function(error) {
            html = error.responseText;
        },
        complete: function() {
            gei('pData').innerHTML = '<tr>'+html+'</tr>';
        }
    });
}