function update() {
    let content = document.querySelector('#ipuac').value;
    render(document.getElementById('render'), content);
}

const section       = new RegExp('^==.*==$');

function cei(tag) {
    return document.createElement(tag);
}
function tps(text, len) {
    return text.substr(2, len-4);
}

function getInF(text) {
    const len = text.length;
    return sub = text.substr(2, len-3);
}

function argsF(text) {
    if(text == '') return [];
    return text.split('|');
}

function render(renderRoot, content) {
    const lines = content.split('\n');

    let finalDOM = '';

    lines.forEach(line => {
        let html = '';
        const len = line.length;

        // If line is paragraph
        if(section.test(line)) {
            const sub = tps(line, len);
            html = `<span class="prob-title">${sub}</span><hr class="prob-hr">`;
        }
        else if(line == '---') {
            html = '<hr class="prob-hr">'
        }
        else if(line == '') {
            html = "<br>"
        }
        // If line is content
        else {
            html = '<p>';

            let escapeFlag = false;
            let lastBrac = '-1';
            for(let i=0; i<len; i++) {
                if(line[i]=='[') {
                    if(escapeFlag) html=html+"[";
                    else {
                        if(lastBrac >= 0) {
                            html=`<p class="error">IPUAC parsing error: '[' and ']' must make pair.`;
                            break;
                        }
                        lastBrac = i;
                    }
                }
                else if(line[i]==']') {
                    if(escapeFlag) html=html+"]";
                    else {
                        if(lastBrac < 0) {
                            html=`<p class="error">IPUAC parsing error: '[' and ']' must make pair.`;
                            break;
                        }
                        const f = line.substr(lastBrac+1, i-lastBrac-1);
                        const inst = f[0];
                        const param = getInF(f);
                        const args = argsF(param);
                        switch(inst) {
                            case '=':
                                if(args.length == 2) {
                                    html = html+`<a href="${args[0]}" target="_blank">${args[1]}</a>`;
                                }
                                else if(args.length == 1) {
                                    html = html+`<a href="${args[0]}" target="_blank">${args[0]}</a>`;
                                }
                                else {
                                    html=`<p class="error">IPUAC parsing error: function 'hyperlink' takes one or two argument(s).`;
                                }
                                break;
                            case '$':
                                html=html+`$${param}$`;
                                break;
                            case '*':
                                if(args.length == 1) {
                                    html=html+`<iframe src=${param} title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>`
                                }
                                else if(args.length == 4) {
                                    html=html+`<iframe src=${args[0]} style="width: ${args[2]}; height: ${args[3]};"title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen>${args[1]}</iframe>`
                                }
                                else {
                                    html=`<p class="error">IPUAC parsing error: function 'embed web' takes one or three argument(s).`;
                                }
                                break;
                            default:
                                html=`<p class="error">IPUAC parsing error: function is not defined.`;
                        }
                        lastBrac = -1;
                    }
                }
                else if(line[i]!='\\' && lastBrac<0) {
                    html=html+line[i];
                }
                if(line[i]=='\\') {
                    // Insert if two escape in a row
                    if(escapeFlag) {
                        html=html+"&#92;";
                        escapeFlag = false;
                    }
                    else escapeFlag = true
                }
                else escapeFlag = false;
            }
            html=html+'</p>';
        }
        finalDOM=finalDOM+html;
    });

    renderRoot.innerHTML = finalDOM;
}