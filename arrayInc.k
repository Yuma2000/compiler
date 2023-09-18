main() {
   int i=7, a[7];

   while (i)
      a[--i] = 10*i;

   outputint (a[0]);
   while (i<6)
      outputint (a[++i]);

   while (i>4)
      --a[--i];

   while (i>1)
      ++a[--i];

   a[i *= 0] += 5;

   outputint (a[0]);
   while (i<6)
      outputint (a[++i]);
}
