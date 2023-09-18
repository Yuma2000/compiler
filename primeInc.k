main() {
    int m,n, max;
    max = inputint;
    m=2;
    while (m<max) {
        n=2;
        while (!(m%n==0)) ++n; 
        if (m==n) outputint (m);
        m++;
    }
}
