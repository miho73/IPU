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