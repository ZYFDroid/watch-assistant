package com.tomoon.extensions.bluessh;
import android.app.Application;
public class Main extends Application
{

	    @Override  
	    public void onCreate() {  
		        super.onCreate();  
		        CrashHandler crashHandler = CrashHandler.getInstance();  
		        crashHandler.init(getApplicationContext());  
				}
}

