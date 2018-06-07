package com.rssheap.utilities;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;

public class TypefaceUtil {
	/**
     * Using reflection to override default typeface
     * NOTICE: DO NOT FORGET TO SET TYPEFACE FOR APP THEME AS DEFAULT TYPEFACE WHICH WILL BE OVERRIDDEN
     * @param context to work with assets
     * @param defaultFontNameToOverride for example "monospace"
     * @param customFontFileNameInAssets file name of the font from assets
     */
    public static void overrideFont(Context context, Typeface font) {
        try {
 
            final java.lang.reflect.Field defaultFontTypefaceField = Typeface.class.getDeclaredField("SERIF");
            defaultFontTypefaceField.setAccessible(true);
            defaultFontTypefaceField.set(null, font);
        } catch (Exception e) {
            Log.e("Can not set custom font", null, e);
        }
    }
    
    public static class Fonts {
    	private static Typeface robotoRegular = null;
    	private static Typeface robotoBold = null;
    	private static Typeface fontAwesome = null;
    	
    	public static Typeface getFontAwesome(Context context) {
    		if(fontAwesome == null) {
    			fontAwesome = Typeface.createFromAsset(context.getAssets(), "fonts/fontawesome-webfont.ttf");
    		}
    		return fontAwesome;
    	}
    	
    	public static Typeface getRobotoRegular(Context context) {
    		if(robotoRegular == null) {
    			robotoRegular = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Regular.ttf");
    		}
    		return robotoRegular;
    	}
    	
    	public static Typeface getRobotoBold(Context context) {
    		if(robotoBold == null) {
    			robotoBold = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Bold.ttf");
    		}
    		return robotoBold;
    	}
    }
}


