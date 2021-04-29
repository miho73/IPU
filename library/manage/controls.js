function loaded() {
    invRef('GET');
}

function invRef(query) {
    pascal = document.getElementById('addInv');
    pascal.innerHTML = "";
    $.ajax({
        method: "POST",
        dataType: "json",
        url: "/mgr/api/get/inv",
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
        }
    });
}

function invSend() {
    cmd = document.getElementById('invQuery').value;
    invRef(cmd);
}

function sendPermQuery() {
    $.ajax({
        method: "POST",
        dataType: "text",
        url: "/mgr/api/perm",
        data: {
            q: document.getElementById('usrQuery').value
        },
        success: function(data) {
            document.getElementById('usrRes').innerText = data;
        },
        error: function(err) {
            let txt = "Response: Unknown"
            switch(err.responseText) {
                case "dbquery":
                    txt = "DB 쿼리 오류"
                    break;
                case "unk":
                    txt = "알 수 없는 명령"
                    break;
                case "perm":
                    txt = "권한 거부"
                    break;
                case "perm":
                    txt = "예기지 못한 오류 발생"
                    break;
                case "permFormat":
                    txt = "권한코드 형식 오류"
            }
            document.getElementById('usrRes').innerText = txt;
        }
    });
}