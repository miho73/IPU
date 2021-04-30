codetable = {
    unra: 1000,
    brozz: 2000,
    silv: 4000,
    gold: 8000,
    sapp: 16000,
    ruby: 32000,
    diam: 64000
}

wa_div = {
    unra: 2,
    brozz: 3,
    silv: 4,
    gold: 5,
    sapp: 6,
    ruby: 7,
    diam: 8
}

powtable = {
    0: 1,
    1: 0.5,
    2: 0.25,
    3: 0.125,
    4: 0.0625,
    5: 0.03125,
    6: 0.015625,
    7: 0.0078125,
    8: 0.0039063,
    9: 0.0019531,
    10:0.0009766
}

function getPow(nos) {
    if(nos>=1 && nos<=10) return powtable[nos];
    else return 0;
}

function getBaseScore(diff) {
    if(codetable.hasOwnProperty(diff)) return codetable[diff];
    else return 0;
}

module.exports = {
    calculate_score: function(difficulty, number_of_solve) {
        return Math.ceil(getBaseScore(difficulty) * getPow(parseInt(number_of_solve)+1));
    },
    trans_wa: function(original, difficulty) {
        return Math.ceil(original/wa_div[difficulty]);
    }
}