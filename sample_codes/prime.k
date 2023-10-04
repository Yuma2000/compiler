main() {
    int m,n, max;
    max = inputint;
    m=2;
    while (m<max) {
        n=2;
        while (!(m%n==0)) n = n + 1;
        if (m==n) outputint (m);
        m = m + 1;
    }
}
