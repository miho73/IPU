function update() {
    let content = document.querySelector('#ipuac').value;
    render(document.getElementById('render'), content);
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
const font_size     = new RegExp('[{]{3}([+-]\\d+)\\s(.*?)[}]{3}', 'g');

const link_with_exp = new RegExp('[\\[]{2}(.*?)[|](.*?)[\\]]{2}', 'g');
const link          = new RegExp('[\\[]{2}(.*?)[\\]]{2}',         'g');
const image         = new RegExp('[\\[][{](.*?)[}][\\]]',         'g');
const imageWithArgs = new RegExp('[\\[][{](.*?)[|](.*?)[}][\\]]', 'g');
/********************/

function render(renderRoot, content) {
    const lines = content.split('\n');

    let finalDOM = [];

    let definitions = {};
    let quoteFlag = false;

    lines.forEach(line => {
        let html = '';
        let len = line.length;

        // 빈 줄(줄바꿈)인 경우 무시(인용은 끊어줌)
        if(line == '') {
            finalDOM[finalDOM.length-1] = finalDOM.at(-1)+'</blockquote>';
            quoteFlag = false;
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
                   .replaceAll('---', '<hr class="prob-hr">');

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

        // 텍스트 색상 적용
        matches = [...line.matchAll(color)];
        matches.forEach((bil_mat)=>{
            line = line.replace(bil_mat[0], `<span style="color: ${bil_mat[1]};">${bil_mat[2]}</span>`);
        });

        // 하이퍼링크 적용
        matches = [...line.matchAll(link_with_exp)];
        matches.forEach((bil_mat)=>{
            line = line.replace(bil_mat[0], `<a href="${bil_mat[1]}" class="ac-link">${bil_mat[2]}</a>`);
        });
        matches = [...line.matchAll(link)];
        matches.forEach((bil_mat)=>{
            line = line.replace(bil_mat[0], `<a href="${bil_mat[1]}" class="ac-link">${bil_mat[1]}</a>`);
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
        console.log(con);
        finalHTML=finalHTML+con;
    });

    renderRoot.innerHTML = finalHTML;
}