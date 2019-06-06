/**
 * 
 */
package com.shtick.apps.sh.ui.gui;

import java.awt.DisplayMode;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import javax.swing.JFrame;

import com.shtick.apps.sh.core.Driver;
import com.shtick.apps.sh.core.UIDriver;

/**
 * @author sean.cox
 *
 */
public class GUIDriver implements UIDriver {
	private static MainFrame MAIN_FRAME;
	private static Driver DRIVER;
	private static boolean EXIT = false;
	private static Font BASE_FONT = new Font("sansserif", Font.PLAIN, 18);

	/* (non-Javadoc)
	 * @see com.shtick.apps.sh.core.UIDriver#main(com.shtick.apps.sh.core.Driver)
	 */
	@Override
	public void main(Driver driver) {
		DRIVER=driver;
		MAIN_FRAME = new MainFrame();
		MAIN_FRAME.setLocation(0, 0);
		GraphicsDevice graphicsDevice=GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		DisplayMode displayMode = graphicsDevice.getDisplayMode();
		MAIN_FRAME.setSize(displayMode.getWidth()/2, displayMode.getHeight()/2);
		MAIN_FRAME.setExtendedState(JFrame.MAXIMIZED_BOTH);
		MAIN_FRAME.setVisible(true);
		synchronized(this){
			while(!EXIT){
				try{
					this.wait();
				}
				catch(InterruptedException t){
					t.printStackTrace();
				}
			}
		}
	}

	/**
	 * @return the main frame
	 */
	public static MainFrame getMainFrame() {
		return MAIN_FRAME;
	}

	/**
	 * @return the Driver
	 */
	public static Driver getDriver() {
		return DRIVER;
	}

	/**
	 * @return the base font for the application
	 */
	public static Font getBaseFont() {
		return BASE_FONT;
	}

	/**
	 * @param baseFont the new base font for the application
	 */
	public static void setBaseFont(Font baseFont) {
		BASE_FONT = baseFont;
	}

	/**
	 * 
	 */
	public static void exit() {
		MAIN_FRAME.setVisible(false);
		EXIT = true;
		System.exit(0);
	}

}
