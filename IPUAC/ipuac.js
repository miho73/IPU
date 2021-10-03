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

    let definitions = {};

    lines.forEach(line => {
        let html = '';
        const len = line.length;

        // Preprocessor
        if(line[0] == '#') {
            if(line == '#') {
                html=html+"<br>";
                finalDOM=finalDOM+html;
                return;
            }
            const inst = line.substr(1, 3);
            const prop = line.substr(5, len-5).split('=');
            switch(inst) {
                case 'def':
                    definitions[prop[0]] = prop[1];
                    break;
                default:
                    html=`<p class="error">IPUAC parsing error: Preprocessor ${inst} is not defined`;
                    break;
            }
            return;
        }

        // If line is paragraph
        if(section.test(line)) {
            const sub = tps(line, len);
            html = `<span class="prob-title">${sub}</span><hr class="prob-hr">`;
        }
        else if(line == '---') {
            html = '<hr class="prob-hr">'
        }
        else if(line == '') {
            return;
        }
        // If line is content
        else {
            let escapeFlag = false;
            let lastBrac = '-1';
            let start = 0;

            if(line.substr(0, 2)=='\\1') {
                html = '<p class="ac-left">';
                start = 2;
            }
            else if(line.substr(0, 2)=='\\2') {
                html = '<p class="ac-center">';
                start = 2;
            }
            else if(line.substr(0, 2)=='\\3') {
                html = '<p class="ac-right">';
                start = 2;
            }
            else if(line.substr(0, 2)=='\\4') {
                html = '<p class="ac-stretch">';
                start = 2;
            }
            else html = '<p>';

            if(definitions.hasOwnProperty('text-alignment')) {
                switch(definitions['text-alignment']) {
                    case '1':
                        html = '<p class="ac-left">';
                        break;
                    case '2':
                        html = '<p class="ac-center">';
                        break;
                    case '3':
                        html = '<p class="ac-right">';
                        break;
                    case '4':
                        html = '<p class="ac-stretch">';
                        break;
                    default:
                        html = '<p>';
                }
            }

            for(let i=start; i<len; i++) {
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
                            case '@':
                                if(args.length == 1) {
                                    html = html+`<img src="https://ipu.r-e.kr/problem/lib/${param}">`;
                                }
                                else if(args.length == 2) {
                                    html = html+`<img src="https://ipu.r-e.kr/problem/lib/${args[0]}" alt="${args[1]}">`;
                                }
                                else if(args.length == 4) {
                                    html = html+`<img src="https://ipu.r-e.kr/problem/lib/${args[0]}" alt="${args[1]}" style="width: ${args[2]}; height: ${args[3]};">`;
                                }
                                else {
                                    html=`<p class="error">IPUAC parsing error: function 'image' takes one, two or four argument(s).`;
                                }
                                break;
                            default:
                                html=`<p class="error">IPUAC parsing error: function is not defined.`;
                        }
                        lastBrac = -1;
                    }
                }
                else if(line[i]!='\\' && lastBrac<0) {
                    if(line[i]=='<') {
                        html=html+'&lt;';
                    }
                    else if(line[i]=='>') {
                        html=html+'&gt;';
                    }
                    else if(line[i]=='&') {
                        html=html+'&amp;';
                    }
                    else html=html+line[i];
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