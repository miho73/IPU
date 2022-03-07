const REV = Math.PI * 2;
const deg150 = 2*Math.PI/360 *150;

var canvas;
var angleSec = 0;
var RADIUS = 0;

var ctx;
var x, y;

var R, r;

function loadGraphics() {
    canvas = document.getElementById('round-clock');

    var now = new Date();
    h = now.getHours();
    m = now.getMinutes();
    if(h >= 12) h -= 12;
    angleSec = h / 12 * REV;
    angleSec += m * 0.5 * REV / 360;

    ctx = canvas.getContext('2d');

    setInterval(()=>{
        angleSec += REV/43200;
        updateRender();
    }, 1000);

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

    delta = R/r * angleSec - deg150;

    ctx.lineWidth = 2;
    ctx.arc(x/2, y/2, R, 0, REV);
    ctx.closePath();
    //ctx.moveTo(x/2 + R-2*r, y/2);
    //ctx.arc(x/2, y/2, R-2*r, 0, REV);
    //ctx.closePath();
    ctx.stroke();

    ctx.lineWidth = 1;
    ctx.moveTo(s+60, c);
    ctx.arc(s, c, r, 0, REV);
    ctx.closePath();

    ctx.moveTo(s + r * Math.sin(delta), c + r * Math.cos(delta));
    ctx.lineTo(s - r * Math.sin(delta), c - r * Math.cos(delta));
    ctx.closePath();
    ctx.stroke();
}