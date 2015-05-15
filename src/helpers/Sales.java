package helpers;

public class Sales {
   
   private String purchaser;//can be a user or state
   
   private double price;
   
   private String product;
   
   /**
    * @param purchaser
    * @param price
    * @param product
    */
   public Sales(String purchaser, double price, String product) {
       this.purchaser = purchaser;
       this.price = price;
       this.product = product;
   }
   
   /**
    * @return the user
    */
   public String getPurchaser() {
       return purchaser;
   }

   /**
    * @return the product
    */
   public String getProduct() {
       return product;
   }

   /**
    * @return the price
    */
   public double getPrice() {
       return price;
   }
}
