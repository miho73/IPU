function convertDiff(code) {
    switch(code) {
        case "unse":
            return "Unset";
        case "unra":
            return "Unrated";
        case "broz":
            return "Bronze";
        case "silv":
            return "Silver";
        case "gold":
            return "Gold";
        case "sapp":
            return "Sapphire";
        case "ruby":
            return "Ruby";
        case "diam":
            return "Diamond";
        default:
            return "Unratable";
    }
}

function convertDiffColor(code) {
    switch(code) {
        case "unse":
            return "#000";
        case "unra":
            return "#555";
        case "broz":
            return "#987b43";
        case "silv":
            return "#C0C0C0";
        case "gold":
            return "#FFD700";
        case "sapp":
            return "#0F52BA";
        case "ruby":
            return "#e0115f";
        case "diam":
            return "#7BC3D4";
        default:
            return "#000";
    }
}

cutTable = [0, 3000, 15000, 40000, 80000, 150000, 200000, 300000]

codeTableRl = {
    1: 'Unrated',
    2: 'Bronze',
    3: 'Silver',
    4: 'Gold',
    5: 'Sapphire',
    6: 'Ruby',
    7: 'Diamond',
    8: 'Infinity'
}

codeTable = {
    1: 'unra',
    2: 'broz',
    3: 'silv',
    4: 'gold',
    5: 'sapp',
    6: 'ruby',
    7: 'diam',
    8: 'redd'
}

const Subj = {
    alge: "대수",
    numb: "정수",
    comb: "조합",
    geom: "기하",
    phys: "물리",
    chem: "화학",
    biol: "생물",
    eart: "지구"
}
function convertSubj(code) {
    if(Subj.hasOwnProperty(code)) return Subj[code];
    else return "미분류";
}

function getLevelCode(exp) {
    let levelCode = 8;
        for(let i=1; i<=7; i++) {
        if(exp < cutTable[i]) {
            levelCode = i;
            break;
        }
    }
    return levelCode;
}

String.prototype.string = function(len){var s = '', i = 0; while (i++ < len) { s += this; } return s;};
String.prototype.zf = function(len){return "0".string(len - this.length) + this;};
Number.prototype.zf = function(len){return this.toString().zf(len);};