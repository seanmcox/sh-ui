/**
 * 
 */
package com.shtick.apps.sh.ui.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.HeadlessException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.shtick.apps.sh.ui.gui.panels.HomePanel;

/**
 * @author sean.cox
 *
 */
public class MainFrame extends JFrame {
	private HomePanel homePanel = new HomePanel();
	private JPanel currentPanel = null;

	/**
	 * @param title
	 * @throws HeadlessException
	 */
	public MainFrame() throws HeadlessException {
		super(Info.NAME+" - "+Info.VERSION);
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
		this.setDefaultCloseOperation(MainFrame.EXIT_ON_CLOSE);
		openApp(homePanel);
	}

	/**
	 * 
	 * @param app
	 */
	public void closeApp(JPanel app){
		Runnable runnable=new Runnable() {
			@Override
			public void run() {
				synchronized(homePanel){
					if((currentPanel!=null)&&(currentPanel==app))
						getContentPane().remove(app);
					currentPanel = homePanel;
					getContentPane().add(currentPanel,BorderLayout.CENTER);
					invalidate();
					validate();
					repaint();
				}
			}
		};
		SwingUtilities.invokeLater(runnable);
	}
	
	/**
	 * 
	 * @param app
	 */
	public void openApp(JPanel app){
		Runnable runnable=new Runnable() {
			@Override
			public void run() {
				synchronized(homePanel){
					if(currentPanel!=app){
						if(currentPanel!=null)
							getContentPane().remove(currentPanel);
						currentPanel = app;
						getContentPane().add(currentPanel,BorderLayout.CENTER);
						invalidate();
						validate();
						repaint();
					}
				}
			}
		};
		SwingUtilities.invokeLater(runnable);
	}
}
