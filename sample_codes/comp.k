main () {
    int i, sum;
    sum=0;
    i= inputint;
    if (i>0 && i<10) {
        while (0<i) {
            sum=sum+i;
            i=i-1;
        }
    }
    outputint(sum);
}