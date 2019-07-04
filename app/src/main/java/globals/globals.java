package globals;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

import de.jonasrottmann.realmbrowser.helper.Utils;
import io.realm.Realm;

public class globals {
    public static String mqtt_server="tcp://192.168.138.1";
    public static String base_url="http://192.168.138.1/portchlytAPI/apiArtisan";




    //handle realm
    public static Realm getDB()
    {
        Realm db = Realm.getDefaultInstance();
        return db;
    }



    //get currency
    public static String formatCurrency(double amount) {
        String amount_currency = "₦"+String.format("%.2f",amount);
        return amount_currency;
    }

}
