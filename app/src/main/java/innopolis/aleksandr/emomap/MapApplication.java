package innopolis.aleksandr.emomap;

import android.app.Application;

import com.parse.Parse;

public class MapApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "Nkg0yIuGnYL3VmATMWFfCIMFpM29dnQJa1KRUAwS", "LudVq2HA4UWpvxDII2m9PpZSsvfV0Yn34PB9WJsG");
    }
}
