package com.example.factory;

import com.example.proxy.DatabaseProxy;
import com.example.config.DatabaseConfig;

public class DatabaseProxyFactory {
    private static DatabaseProxy instance;

    public static DatabaseProxy getProxy() {//crea un istanza di databaseProxy gia configurata
        if (instance == null) {
            instance = new DatabaseProxy( DatabaseConfig.getServerHost(), DatabaseConfig.getServerPort());
        }
        return instance;
    }
}