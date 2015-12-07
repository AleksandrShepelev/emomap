package innopolis.aleksandr.emomap;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.parse.Parse;

public class MapApplication extends Application {
    private static SharedPreferences preferences;
    @Override
    public void onCreate() {
        super.onCreate();
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "Nkg0yIuGnYL3VmATMWFfCIMFpM29dnQJa1KRUAwS", "LudVq2HA4UWpvxDII2m9PpZSsvfV0Yn34PB9WJsG");

        preferences = getSharedPreferences("innopolis.aleksandr.emomap", Context.MODE_PRIVATE);
    }

}
