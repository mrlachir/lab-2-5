package com.microservice.product.util;
public final class DriverCleanup{
    private DriverCleanup(){}
    public static void shutdown(){
        try{Class.forName("com.mysql.cj.jdbc.AbandonedConnectionCleanupThread").getMethod("checkedShutdown").invoke(null);}
        catch(ReflectiveOperationException ignored){}
    }
}
