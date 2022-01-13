function editTitle() {
    gei('issue-title').style.display = 'none';
    gei('edit-issue-title').style.display = 'block';
    setTimeout(()=>{
        gei('edit-issue-title').focus();
    }, 10);
}

function updateTitle(iCode) {
    $.ajax({
        method: 'PATCH',
        dataType: 'json',
        url: '/issue/api/name/update',
        data: {
            'issue-code': iCode,
            'new-name': gei('edit-issue-title').value
        },
        success: function() {
            gei('issue-title').innerText = gei('edit-issue-title').value;
            gei('issue-title').style.display = 'block';
            gei('edit-issue-title').style.display = 'none';
        },
        error: function() {
            gei('edit-issue-title').value = gei('issue-title').innerText;
            gei('issue-title').style.display = 'block';
            gei('edit-issue-title').style.display = 'none';
        }
    })
}

function nameEditCancel() {
    if(gei('edit-issue-title').value == gei('issue-title').innerText) {
        gei('issue-title').style.display = 'block';
        gei('edit-issue-title').style.display = 'none';
    }
}