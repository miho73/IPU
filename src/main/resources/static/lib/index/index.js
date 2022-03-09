const REV = Math.PI * 2;

var canvas;
var angleSec = 0;
var RADIUS = 0;

var ctx;
var x, y;

var R, r;

function loadGraphics() {
    canvas = document.getElementById('round-clock');

    angleSec = 0;

    ctx = canvas.getContext('2d');

    setInterval(()=>{
        angleSec += REV/6000;
        updateRender();
    }, 10);

    document.addEventListener('resize', ()=>{
        updateRender();
    })

    updateRender();
}

function updateRender() {
    x = canvas.width = canvas.getBoundingClientRect().width;
    y = canvas.height = canvas.getBoundingClientRect().height;

    RADIUS = x/2-80;
    s = RADIUS * Math.sin(angleSec) + x/2;
    c = y/2 - RADIUS * Math.cos(angleSec);

    R = x/2-20;
    r = 60;

    delta = R/r * angleSec;

    ctx.lineWidth = 2;
    ctx.arc(x/2, y/2, R, 0, REV);
    ctx.closePath();
    ctx.stroke();

    ctx.lineWidth = 1;
    ctx.moveTo(s+60, c);
    ctx.arc(s, c, r, 0, REV);
    ctx.closePath();

    sd = Math.sin(delta) * r;
    cd = Math.cos(delta) * r;

    ctx.moveTo(s + sd, c + cd);
    ctx.lineTo(s - sd, c - cd);
    ctx.closePath();

    l = R - 2*r;
    for(i=0; i<12; i++) {
        ctx.moveTo(x/2, y/2);
        ctx.lineTo(x/2 + l * sincos[i][0], y/2 - l * sincos[i][1]);
    }
    ctx.stroke();
}

sincos = [
    // sin cos
    [0, 1], // 0
    [0.5, 0.86602540378443864676372317075294], // 30
    [0.86602540378443864676372317075294, 0.5], // 60
    [1, 0], // 90
    [0.86602540378443864676372317075294, -0.5], // 120
    [0.5, -0.86602540378443864676372317075294], // 150
    [0, -1], // 180
    [-0.5, -0.86602540378443864676372317075294], // 210
    [-0.86602540378443864676372317075294, -0.5], // 240
    [-1, 0], // 270
    [-0.5, 0.86602540378443864676372317075294], // 300
    [-0.86602540378443864676372317075294, 0.5], // 330
]