package com.shtick.apps.sh.ui.gui.panels;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JPanel;

import com.shtick.apps.components.ErrorDialog;
import com.shtick.apps.sh.core.User;
import com.shtick.apps.sh.ui.gui.GUIDriver;
import com.shtick.apps.sh.ui.gui.dialog.UserSelectDialog;

/**
 * @author sean.cox
 *
 */
public class HomePanel extends JPanel {
	private JButton manageUsersButton = new JButton("Manage Users");
	private JButton selectUserButton = new JButton("Enter User Portal");
	private JButton exitButton = new JButton("Exit");

	/**
	 * 
	 */
	public HomePanel() {
		super(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		add(manageUsersButton, gbc);
		add(selectUserButton, gbc);
		add(exitButton, gbc);
		
		manageUsersButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GUIDriver.getMainFrame().openApp(new ManageUsersPanel());
			}
		});
		selectUserButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Open dialog to allow user to be selected.
				UserSelectDialog dialog;
				try{
					dialog = new UserSelectDialog(GUIDriver.getMainFrame());
				}
				catch(IOException t){
					ErrorDialog.showError(GUIDriver.getMainFrame(), t);
					return;
				}
				int result = dialog.showDialog();
				if(result != UserSelectDialog.APPROVE_OPTION)
					return;
				User user = dialog.getUser();
				GUIDriver.getMainFrame().openApp(new UserPortalPanel(user));
			}
		});
		exitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GUIDriver.exit();
			}
		});
	}
	
}
