function convertDiff(code) {
    switch(code) {
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

String.prototype.string = function(len){var s = '', i = 0; while (i++ < len) { s += this; } return s;};
String.prototype.zf = function(len){return "0".string(len - this.length) + this;};
Number.prototype.zf = function(len){return this.toString().zf(len);};