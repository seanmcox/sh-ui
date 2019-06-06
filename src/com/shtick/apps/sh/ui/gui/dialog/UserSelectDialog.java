/**
 * 
 */
package com.shtick.apps.sh.ui.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ListDataListener;

import com.shtick.apps.sh.core.User;
import com.shtick.apps.sh.ui.gui.GUIDriver;

/**
 * @author Sean
 *
 */
public class UserSelectDialog extends JDialog {
    /**
     * Return state value indicating that the action is to be applied as detailed by the user.
     */
    public static final int APPROVE_OPTION=0;
    /**
     * Return state value indicating that the action is not to be applied.
     */
    public static final int CANCEL_OPTION=1;
    /**
     * Return state value indicating that some error occurred.
     */
    public static final int ERROR_OPTION=2;

    private int returnValue=ERROR_OPTION;

    private List<LabeledItem<User>> users;

    private JLabel userLabel=new JLabel("Users: ");
    private JComboBox<LabeledItem<User>> userCombo;
    private JButton cancelButton=new JButton("Cancel");
    private JButton applyButton=new JButton("Select");
    
    /**
     * @param owner
     * @param stroke
     * @throws IOException
     */
    public UserSelectDialog(Frame owner) throws IOException{
    	super(owner,"Select User",true);
    	Collection<User> users = GUIDriver.getDriver().getUsers();
    	this.users = new ArrayList<LabeledItem<User>>(users.size());
    	for(User user:users)
    		this.users.add(new LabeledItem<User>(user.getName(),user));
    	users=null;
    	userCombo = new JComboBox<LabeledItem<User>>(new UserComboBoxModel());
        
        cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				returnValue = CANCEL_OPTION;
				setVisible(false);
			}
		});
        applyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				returnValue = APPROVE_OPTION;
				setVisible(false);
			}
		});

        Container contentPane=getContentPane();
        contentPane.setLayout(new BorderLayout());
        
        JPanel inputPanel=new JPanel(new GridLayout(1,2));
        inputPanel.add(userLabel);
        inputPanel.add(userCombo);
        contentPane.add(inputPanel, BorderLayout.NORTH);
        
        userCombo.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				updateEnabling();
			}
		});

        JPanel buttonPanel=new JPanel(new BorderLayout());
        buttonPanel.add(cancelButton, BorderLayout.WEST);
        buttonPanel.add(applyButton, BorderLayout.EAST);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);
        updateEnabling();
        setSize(200,100);
    }
    
    private void updateEnabling(){
    	applyButton.setEnabled(userCombo.getSelectedItem()!=null);
    }

    /**
     * @return The status result. One of APPROVE_OPTION, CANCEL_OPTION, or ERROR_OPTION.
     */
    public int showDialog(){
        addWindowListener(new WindowAdapter() {
                @Override
            public void windowClosing(WindowEvent e) {
                returnValue = CANCEL_OPTION;
            }
        });

        setLocation(getOwner().getX()+(getOwner().getWidth() - getWidth())/2, getOwner().getY()+(getOwner().getHeight() - getHeight())/2);
        setVisible(true);
        dispose(); // Free up native resources and any references to this dialog box.

        return returnValue;
    }
    
    /**
     * @return The User selected.
     */
    public User getUser(){
		Object selectedItem = userCombo.getSelectedItem();
		if(selectedItem==null)
			return null;
		return ((LabeledItem<User>)selectedItem).getItem();
    }
	
	private class UserComboBoxModel implements ComboBoxModel<LabeledItem<User>>{
		private LinkedList<ListDataListener> listeners = new LinkedList<>();
		private LabeledItem<?> selectedObject = null;
		
		/* (non-Javadoc)
		 * @see javax.swing.ListModel#getSize()
		 */
		@Override
		public int getSize() {
			return users.size();
		}

		/* (non-Javadoc)
		 * @see javax.swing.ListModel#getElementAt(int)
		 */
		@Override
		public LabeledItem<User> getElementAt(int index) {
			if((index<0)||(index>=users.size()))
				return null;
			return users.get(index);
		}

		/* (non-Javadoc)
		 * @see javax.swing.ListModel#addListDataListener(javax.swing.event.ListDataListener)
		 */
		@Override
		public void addListDataListener(ListDataListener l) {
			listeners.add(l);
		}

		/* (non-Javadoc)
		 * @see javax.swing.ListModel#removeListDataListener(javax.swing.event.ListDataListener)
		 */
		@Override
		public void removeListDataListener(ListDataListener l) {
			listeners.remove(l);
		}

		/* (non-Javadoc)
		 * @see javax.swing.ComboBoxModel#setSelectedItem(java.lang.Object)
		 */
		@Override
		public void setSelectedItem(Object anItem) {
			if(anItem instanceof LabeledItem)
				selectedObject = (LabeledItem<?>)anItem;
			else if (anItem instanceof User)
				selectedObject = new LabeledItem<User>(((User)anItem).getName(),(User)anItem);
			else
				selectedObject = null;
		}

		/* (non-Javadoc)
		 * @see javax.swing.ComboBoxModel#getSelectedItem()
		 */
		@Override
		public Object getSelectedItem() {
			return selectedObject;
		}
	}
	
	private class LabeledItem<I>{
		private String label;
		private I item;
		
		/**
		 * @param label
		 * @param item
		 */
		public LabeledItem(String label, I item) {
			super();
			this.label = label;
			this.item = item;
		}

		/**
		 * @return the label
		 */
		public String getLabel() {
			return label;
		}

		/**
		 * @return the item
		 */
		public I getItem() {
			return item;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return label;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return item.hashCode();
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if(!(obj instanceof LabeledItem))
				return false;
			return item.equals(((LabeledItem)obj).item);
		}
	}
}
