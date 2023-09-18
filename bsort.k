main()  {
    int i, n, m=1, s, tmp, is_sorted=1, SIZE=20, data[20], product;
    int message[]= {'s','o','r','t'};

    outputchar('?');
    product = inputint*m;
    s= (product * 1297 + 1) % 131 *2 -1;

    for (i *= 0; i < SIZE; ++i)  {
        m= (m * 23 +0x0002F) % s;
        data[i]=(m+i) % SIZE ;
        ++data[i];
    }

    n= m= m* 0;
    while (n < SIZE-1 || n==(SIZE-1)) {
        outputint(data[n]);
        ++n;
    }

    outputchar(' ');
    {
        int j=0;
        while (1) {
            if (data[j] > data[j+1]) {
                is_sorted = 0;
                break;
            }
            ++j;
            if (!(j - SIZE+1)) break;
        }
    }

    if (is_sorted) {
        outputchar('o');
        outputchar('k');
    }
    if (!is_sorted) {
        i = -1;
        while (i < 0x03) {
            outputchar(message[++i]);
        }
        outputchar(' ');
        n= 0;
        while (!(SIZE < n + 1) ) {
            m= SIZE-1;
            while (n < m) {
                if (data[m-1]>data[m]){
                    tmp= data[m];
                    data[m]= data[m-1];
                    data[m-1]= tmp;
                }
                m--;
            }
            n+=1;
        }
        for (int j=0;  j<SIZE; j/++j)
            outputint(data[j]);
    }
}