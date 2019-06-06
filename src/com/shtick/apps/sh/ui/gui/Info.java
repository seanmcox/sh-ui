/**
 * 
 */
package com.shtick.apps.sh.ui.gui;

import java.util.prefs.Preferences;

/**
 * @author sean.cox
 *
 */
public class Info {
	/**
	 * The application name.
	 */
	public static final String NAME = "Super Helpful Quiz";
	
	/**
	 * The version of the application.
	 */
	public static final String VERSION = "2019.May.27";
	
	/**
	 * Author of the application.
	 */
	public static final String AUTHOR = "Sean M. Cox";
	
	/**
	 * A website to associate with the application by way of information.
	 */
	public static final String WEBSITE_URL = "http://www.theshtick.org/";

    private static final String PREF_NODE_NAME="/com/shtick/apps/sh";
    private static Preferences userPrefs =  Preferences.userRoot().node(PREF_NODE_NAME);
    
    /**
     * @return The preferences object which this application will use as a
     *         registry for user preferences and other such internal
     *         parameters.
     */
    public static Preferences getPreferences(){
    	return userPrefs;
    }
}
