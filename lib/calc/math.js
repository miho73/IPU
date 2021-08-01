/**
 * Calculate a^b mod n
 * @param {base} a 
 * @param {exponent} b 
 * @param {modulo} m 
 */
function powerMod(a, b, n) {
    let r=1, m;
    y=a%n;
	while(1) {
		if(b%2==1) {
			b=(b-1)/2;
			r=(r*y)%n;	
		}
		else b/=2;	
		if(b==0) break;
		y=(y*y)%n;
	}
	return r;
}