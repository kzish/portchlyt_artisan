package globals;



public class globals {

    //online settings
    final static String mqtt_server = "tcp://etixng.com:1883";
    public static String base_url="http://etixng.com:1000/apiArtisan";//live
    //public static String base_url="http://192.168.100.5:1111/apiArtisan";//local
    public static String artisan_blog_base_url="http://etixng.com:8083/wp-json/wp/v2";


    //offline settings
//    final static String mqtt_server = "tcp://192.168.4.1:1883";
//    public static String base_url="http://192.168.4.1:1111/apiArtisan";
//    public static String artisan_blog_base_url="http://192.168.4.1:70/wp-json/wp/v2";






    //get currency
    public static String formatCurrency(double amount) {
        String amount_currency = "";

        if (amount < 1000) {
            amount_currency = "₦" + String.format("%.2f", amount);
        }
        else {
            int exp = (int) (Math.log(amount) / Math.log(1000));
            amount_currency = String.format("%.2f %c", amount / Math.pow(1000, exp), "kMGTPE".charAt(exp - 1));
        }

        return amount_currency;
    }



    //get currency exact
    public static String formatCurrencyExact(double amount) {
        String amount_currency = "";
            amount_currency = "₦" + String.format("%.2f", amount);
        return amount_currency;
    }


    public static String numberCalculation(long number) {
        if (number < 1000)
            return "" + number;
        int exp = (int) (Math.log(number) / Math.log(1000));
        return String.format("%.1f %c", number / Math.pow(1000, exp), "kMGTPE".charAt(exp-1));
    }





}
