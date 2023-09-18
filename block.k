main() {
   int i=10;
   {
      int j=20;
      {
         int k=30;
         outputint (i+j+k);
      }
      {
         int k=100;
         outputint (i+j+k);
      }
   }
}
   