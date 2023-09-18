main()  {           
    int n=0, m=1, s, tmp, SIZE=20, data[20];

    outputchar('?');
    s= (inputint * 1297 + 1) % 131 *2 -1;

    while (n < SIZE)  {
        m= (m * 23 +0x002F) % s;
        data[n]=(m+n) % SIZE + 1;
        n= n+1;
    }

    n= 0;
    while (n < SIZE)  {
        outputint(data[n]);
        n=n+1;
    }

    outputchar(' ');
    n= 0;
    while (n < SIZE) {
        m= SIZE-1;
        while (n < m) {
            if (data[m-1] > data[m]){
                tmp= data[m];
                data[m]= data[m-1];
                data[m-1]= tmp;
            }
            m=m-1;
        }
        n=n+1; 
    }

    outputint(data[0]);
    n= 1;
    while (n < SIZE) {
        outputint(data[n]);
        n=n+1;
    }   
}
