main() {
   int i;

   for (i=1; i<11; i++) {
      int n=0;
      for (int j=1; j<i+1; ++j) {
         n += j;
      }
      outputint (n);
   }
}
