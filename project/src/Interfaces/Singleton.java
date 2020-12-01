package Interfaces;

import settings.Settings;

public abstract class Singleton {

    private static Singleton instance;

    public static Singleton getInstance() {
        return instance;
    };

}
