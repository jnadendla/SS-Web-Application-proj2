package helpers;

public class Sales {
   
   private String user;
   
   private double price;
   
   private String product;
   
   /**
    * @param user
    * @param price
    * @param product
    */
   public Sales(String user, double price, String product) {
       this.user = user;
       this.price = price;
       this.product = product;
   }
   
   /**
    * @return the user
    */
   public String getUser() {
       return user;
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
