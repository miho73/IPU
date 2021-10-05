function update(id, ipuac) {;
    render(gei(id), ipuac);
}

/**   LINE REGEX   **/
const section       = new RegExp('^==\\s+.*\\s+==$');
const bold_n_italic = new RegExp('[\']{4}(.*?)[\']{4}',   'g');
const italic        = new RegExp('[\']{3}(.*?)[\']{3}',   'g');
const bold          = new RegExp('[\']{2}(.*?)[\']{2}',   'g');
const underline     = new RegExp('[_]{2}(.*?)[_]{2}',     'g');
const strike        = new RegExp('[-]{2}(.*?)[-]{2}',     'g');
const superscript   = new RegExp('[\\^]{2}(.*?)[\\^]{2}', 'g');
const subscript     = new RegExp('[,]{2}(.*?)[,]{2}',     'g');

const color         = new RegExp('[{]{3}(.*?)\\s(.*?)[}]{3}',      'g');
const bgcolor       = new RegExp('[\\[]{3}(.*?)\\s(.*?)[\\]]{3}', 'g');
const font_size     = new RegExp('[{]{3}([+-]\\d+)\\s(.*?)[}]{3}', 'g');

const link_with_exp = new RegExp('[\\[]{2}(.*?)[|](.*?)[\\]]{2}', 'g');
const link          = new RegExp('[\\[]{2}(.*?)[\\]]{2}',         'g');
const image         = new RegExp('[\\[][{](.*?)[}][\\]]',         'g');
const imageWithArgs = new RegExp('[\\[][{](.*?)[|](.*?)[}][\\]]', 'g');

const func          = new RegExp('[\\[]([a-zA-Z]+?)[(](.*?)[)][\\]]', 'g');
const tableElement  = new RegExp('^[(]([|-]\\d+)[)](.*)$', 'g')
/********************/

function render(renderRoot, content) {
    const lines = content.split('\n');

    let finalDOM = [];

    let definitions = {};
    let quoteFlag = false, tableFlag = false;
    let hide_code = 0;

    lines.forEach(line => {
        let html = '';
        let len = line.length;

        // 빈 줄(줄바꿈)인 경우 무시(인용, 테이블은 끊어줌)
        if(line == '') {
            if(quoteFlag) {
                finalDOM[finalDOM.length-1] = finalDOM.at(-1)+'</blockquote>';
                quoteFlag = false;
            }
            if(tableFlag) {
                finalDOM[finalDOM.length-1] = finalDOM.at(-1)+'</tbody></table>';
                tableFlag = false;
            }
            return;
        }

        // 지시문 처리
        if(line[0] == '#') {
            const inst = line.substr(1, 3);
            const prop = line.substr(5, len-5).split('=');
            switch(inst) {
                case 'def':
                    definitions[prop[0]] = prop[1];
                    break;
                default:
                    html=`<p class="error">IPUAC 오류: 지시문 '${inst}'을 이해할 수 없습니다.</p>`;
                    finalDOM.push(html);
                    break;
            }
            return;
        }

        //지시문이 아닌 경우
        let matches = [];

        // HTML Injection 방지
        line = line.replaceAll('<', '&#60;')
                   .replaceAll('>', '&#62;');

        //매크로 치환
        line = line.replaceAll('[lf]', '<br>')
                   .replaceAll('---', '<hr class="prob-div-hr">');

        // 텍스트 스타일 Regex로 검색 -> 치환
        matches = [...line.matchAll(bold_n_italic)];
        matches.forEach((bil_mat)=>{
            line = line.replace(bil_mat[0], `<span class="ac-bold ac-italic">${bil_mat[1]}</span>`);
        });
        matches = [...line.matchAll(italic)];
        matches.forEach((bil_mat)=>{
            line = line.replace(bil_mat[0], `<span class="ac-italic">${bil_mat[1]}</span>`);
        });
        matches = [...line.matchAll(bold)];
        matches.forEach((bil_mat)=>{
            line = line.replace(bil_mat[0], `<span class="ac-bold">${bil_mat[1]}</span>`);
        });
        matches = [...line.matchAll(underline)];
        matches.forEach((bil_mat)=>{
            line = line.replace(bil_mat[0], `<span class="ac-underline">${bil_mat[1]}</span>`);
        });
        matches = [...line.matchAll(strike)];
        matches.forEach((bil_mat)=>{
            line = line.replace(bil_mat[0], `<span class="ac-strike">${bil_mat[1]}</span>`);
        });
        matches = [...line.matchAll(superscript)];
        matches.forEach((bil_mat)=>{
            line = line.replace(bil_mat[0], `<sup class="ac-super">${bil_mat[1]}</sup>`);
        });
        matches = [...line.matchAll(subscript)];
        matches.forEach((bil_mat)=>{
            line = line.replace(bil_mat[0], `<sub class="ac-sub">${bil_mat[1]}</sub>`);
        });

        //텍스트 크기 적용
        matches = [...line.matchAll(font_size)];
        matches.forEach((bil_mat)=>{
            line = line.replace(bil_mat[0], `<span style="font-size: ${Math.floor(parseInt(bil_mat[1])+11)/10}em;">${bil_mat[2]}</span>`);
        });

        // 텍스트 & 배경 색상 적용
        matches = [...line.matchAll(color)];
        matches.forEach((bil_mat)=>{
            line = line.replace(bil_mat[0], `<span style="color: ${bil_mat[1]};">${bil_mat[2]}</span>`);
        });
        matches = [...line.matchAll(bgcolor)];
        matches.forEach((bil_mat)=>{
            line = line.replace(bil_mat[0], `<span style="background-color: ${bil_mat[1]};">${bil_mat[2]}</span>`);
        });

        // 하이퍼링크 적용
        matches = [...line.matchAll(link_with_exp)];
        matches.forEach((bil_mat)=>{
            line = line.replace(bil_mat[0], `<a href="${bil_mat[1]}" class="ac-link" target="_blank">${bil_mat[2]}</a>`);
        });
        matches = [...line.matchAll(link)];
        matches.forEach((bil_mat)=>{
            line = line.replace(bil_mat[0], `<a href="${bil_mat[1]}" class="ac-link" target="_blank">${bil_mat[1]}</a>`);
        });

        // 이미지 적용
        matches = [...line.matchAll(imageWithArgs)];
        matches.forEach((bil_mat)=>{
            const lstCfg = bil_mat[2].split('&');
            if(bil_mat[1].substr(0, 4)=='img:') {
                line = line.replace(bil_mat[0], `<img src="https://ipu.r-e.kr/problem/lib/${bil_mat[1].substr(4, bil_mat[1].length-4)}" class="ac-img" ${lstCfg.join(' ')}>`);
            }
            else {
                line = line.replace(bil_mat[0], `<img src="${bil_mat[1]}" class="ac-img" ${lstCfg.join(' ')}>`);
            }
        });
        matches = [...line.matchAll(image)];
        matches.forEach((bil_mat)=>{
            if(bil_mat[1].substr(0, 4)=='img:') {
                line = line.replace(bil_mat[0], `<img src="https://ipu.r-e.kr/problem/lib/${bil_mat[1].substr(4, bil_mat[1].length-4)}" class="ac-img">`);
            }
            else {
                line = line.replace(bil_mat[0], `<img src="${bil_mat[1]}" class="ac-img">`);
            }
        });

        // 함수 적용
        matches = [...line.matchAll(func)];
        matches.forEach((bil_mat)=>{
            const arg = bil_mat[2];
            switch(bil_mat[1].toLowerCase()) {
                case "math":
                    line = line.replace(bil_mat[0], `$${arg}$`);
                    break;
                case "ytp":
                    const argsy = arg.split(',');
                    line = line.replace(bil_mat[0], `<iframe src="https://www.youtube.com/embed/${argsy[0]}" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen class="ac-ytp" ${argsy.join(' ')}></iframe>`);
                    break;
                case "embed":
                    const argse = arg.split(',');
                    line = line.replace(bil_mat[0], `<iframe src="${argse[0]}" class="ac-embed" ${argse.join(' ')}></iframe>`);
                    break;
                // hide 함수는 바로 적용
                case "hide":
                    const argsh = arg.split(',');
                    line = `<button class="ac-hidden" onclick="open(${hide_code})" id="ac-open-${hide_code}">${argsh[0]}</button><div id="ac-hidden-${hide_code}" class="ac-hidden-content">${argsh[1]}</div>`;
                    hide_code++;
                    break;
            }
        });

        // 테이블 바로 적용
        if(line.substr(0, 2) == '||') {
            html = '';
            //테이블이 처음 시작된 경우 테이블 열기
            if(!tableFlag) {
                html = '<table class="ac-table"><tbody class="ac-tbody">';
            }
            switch(definitions['table-align']) {
                case '1':
                    html = html+'<tr class="ac-left">';
                    break;
                case '2':
                    html = html+'<tr class="ac-center">';
                    break;
                case '3':
                    html = html+'<tr class="ac-right">';
                    break;
                case '4':
                    html = html+'<tr class="ac-stretch">';
                    break;
                default:
                    html = html+'<tr>';
            }
            const inTable = line.substr(2, line.length-4).split('||');
            inTable.forEach((td)=>{
                const c = [...td.matchAll(tableElement)][0];
                if(c == undefined) {
                    html = html+`<td>${td}</td>`;
                }
                else {
                    const inst = c[1];
                    if(inst[0]=='-') {
                        html = html+`<td colspan="${inst.substr(1, inst.length-1)}">${c[2]}</td>`;
                    }
                    else if(inst[0]=='|') {
                        html = html+`<td rowspan="${inst.substr(1, inst.length-1)}">${c[2]}</td>`;
                    }
                    else html = html+`<td>${td}</td>`;
                }
            });
            html = html+'</tr>';
            tableFlag = true;
            finalDOM.push(html);
            return;
        }
        // 이번줄은 테이블이 아니지만 저번줄이 테이블이면 테이블 닫기
        else if(tableFlag) {
            finalDOM[finalDOM.length-1] = finalDOM.at(-1)+'</tbody></table>';
            tableFlag = false;
        }

        // 인용문 바로 적용
        if(line[0] == '!') {
            if(quoteFlag) {
                html = `<br>${line.substr(1, line.length-1)}`;
            }
            else html = `<blockquote class="ac-quote">${line.substr(1, line.length-1)}`;
            quoteFlag = true;
            finalDOM.push(html);
            return;
        }
        // 이번줄은 인용이 아니지만 저번줄이 인용이면 저번 인용 닫기
        else if(quoteFlag) {
            finalDOM[finalDOM.length-1] = finalDOM.at(-1)+'</blockquote>';
            quoteFlag = false;
        }

        // IPUAC -> HTML 교체 후 길이 업데이트
        len = line.length;

        // 섹션인 경우 바로 적용
        if(section.test(line)) {
            html = `<span class="prob-title">${line.substr(3, len-6)}</span><hr class="prob-hr">`;
            finalDOM.push(html);
            return;
        }

        if(line[0] == '\\') {
            switch(line[1]) {
                case '1':
                    html = `<p class="ac-left">${line.substr(2, len-2)}</p>`;
                    break;
                case '2':
                    html = `<p class="ac-center">${line.substr(2, len-2)}</p>`;
                    break;
                case '3':
                    html = `<p class="ac-right">${line.substr(2, len-2)}</p>`;
                    break;
                case '4':
                    html = `<p class="ac-stretch">${line.substr(2, len-2)}</p>`;
                    break;
                default:
                    html = `<p>${line}</p>`;   
            }
        }
        else if(definitions.hasOwnProperty('text-align')) {
            switch(definitions['text-align']) {
                case '1':
                    html = `<p class="ac-left">${line}</p>`;
                    break;
                case '2':
                    html = `<p class="ac-center">${line}</p>`;
                    break;
                case '3':
                    html = `<p class="ac-right">${line}</p>`;
                    break;
                case '4':
                    html = `<p class="ac-stretch">${line}</p>`;
                    break;
                default:
                    html = `<p>${line}</p>`;
            }
        }
        else html = `<p>${line}</p>`;
        finalDOM.push(html);
    });

    let finalHTML = '';
    finalDOM.forEach((con)=>{
        finalHTML=finalHTML+con;
    });

    renderRoot.innerHTML = finalHTML;
}

// 가림 열기
function open(code) {
    gei(`ac-open-${code}`).style.display = 'none';
    gei(`ac-hidden-${code}`).style.display = 'block';
}

function gei(id) {
    return document.getElementById(id);
}