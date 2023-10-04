main () {
    int i1, i2, i3, i, sum=0;
    i1= inputint;
    i2= inputint;
    i3= inputint;
    if (i1 && i2 && i3) {
        i = i1 * i2 * i3;
        while (i) {
            sum=sum+i;
            i=i-1;
        }
    }
    outputint(sum);
    if (i1 || i2 || i3) {
      outputint(i1 + i2 + i3);
    }
}
