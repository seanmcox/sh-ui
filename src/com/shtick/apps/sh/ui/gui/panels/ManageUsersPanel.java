package com.shtick.apps.sh.ui.gui.panels;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import com.shtick.apps.components.ErrorDialog;
import com.shtick.apps.sh.core.QuizDesign;
import com.shtick.apps.sh.core.QuizDesignID;
import com.shtick.apps.sh.core.QuizDesignSubject;
import com.shtick.apps.sh.core.Subject;
import com.shtick.apps.sh.core.User;
import com.shtick.apps.sh.core.UserID;
import com.shtick.apps.sh.ui.gui.GUIDriver;
import com.shtick.apps.sh.ui.gui.dialog.QuizDesignDialog;

/**
 * @author sean.cox
 *
 */
public class ManageUsersPanel extends JPanel {
	private JTable userList;
	private JButton editButton = new JButton("Edit Quiz");
	private JButton deleteButton = new JButton("Delete");
	private JButton mainMenuButton = new JButton("Return to Menu");
	private ArrayList<User> users = new ArrayList<>();
	private HashSet<TableModelListener> userListListeners=new HashSet<>();

	/**
	 * 
	 */
	public ManageUsersPanel() {
		super(new BorderLayout());
		

		try{
			users = new ArrayList<>(GUIDriver.getDriver().getUsers());
		}
		catch(IOException t){
			ErrorDialog.showError(GUIDriver.getMainFrame(), t);
		}
		
		userList=new JTable(new UsersTableModel());

		{
			JLabel title = new JLabel("Manage Users");
			title.setFont(this.getFont().deriveFont(this.getFont().getSize()*2.0f));
			add(title, BorderLayout.NORTH);
		}
		add(new JScrollPane(userList), BorderLayout.CENTER);
		{
			JPanel buttonPanel = new JPanel();
			buttonPanel.add(editButton);
			buttonPanel.add(deleteButton);
			buttonPanel.add(mainMenuButton);
			add(buttonPanel, BorderLayout.EAST);
		}
		
		mainMenuButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GUIDriver.getMainFrame().closeApp(ManageUsersPanel.this);
			}
		});
		updateEnabling();
		userList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				updateEnabling();
			}
		});
		editButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				synchronized(userList){
					int row = userList.getSelectedRow();
					if(row<0)
						return;
					User user = users.get(row);
					Collection<QuizDesign> userQuizDesigns;
					try {
						userQuizDesigns = GUIDriver.getDriver().getQuizDesigns(user.getUserID());
						// TODO Update UI to handle management of multiple designs.
						QuizDesign quizDesign;
						if(userQuizDesigns.size()==0) {
							// Define a default.
							HashSet<QuizDesignSubject> subjects = new HashSet<>();
							QuizDesign design = new QuizDesign(user.getUserID(), "Default Math Quiz", subjects, 5, 5);
							QuizDesignID quizDesignID = GUIDriver.getDriver().createQuizDesign(design);
							quizDesign = new QuizDesign(quizDesignID, user.getUserID(), "Default Math Quiz", subjects, 5, 5,null);
							
						}
						else {
							quizDesign = userQuizDesigns.iterator().next();
						}
						QuizDesignDialog dialog = new QuizDesignDialog(JOptionPane.getFrameForComponent(ManageUsersPanel.this), user, quizDesign);
						int result = dialog.showDialog();
						if(result==QuizDesignDialog.APPROVE_OPTION) {
							GUIDriver.getDriver().updateQuizDesign(dialog.getQuizDesign());
						}
						else if(result==QuizDesignDialog.ERROR_OPTION) {
							JOptionPane.showMessageDialog(ManageUsersPanel.this, "Error ", "Error", JOptionPane.ERROR_MESSAGE);;
						}
					}
					catch(Exception t) {
						ErrorDialog.showError(JOptionPane.getFrameForComponent(ManageUsersPanel.this), t);
					}
				}
			}
		});
		deleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				synchronized(userList){
					int row = userList.getSelectedRow();
					if(row<0)
						return;
					User user = users.get(row);
					try{
						GUIDriver.getDriver().deleteUser(user.getUserID());
						users.remove(row);
						TableModelEvent event=new TableModelEvent(userList.getModel(),row,row,TableModelEvent.ALL_COLUMNS,TableModelEvent.DELETE);
						for(TableModelListener listener:userListListeners)
							listener.tableChanged(event);
					}
					catch(IOException t){
						ErrorDialog.showError(GUIDriver.getMainFrame(), t);
					}
				}
			}
		});
	}
	
	private void updateEnabling(){
		deleteButton.setEnabled((userList.getSelectedRow()>=0)&&(userList.getSelectedRow()<users.size()));
		editButton.setEnabled((userList.getSelectedRow()>=0)&&(userList.getSelectedRow()<users.size()));
	}
	
	private class UsersTableModel implements TableModel{
		
		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if(columnIndex==0){
				synchronized(userList){
					TableModelEvent event=null;
					try{
						if((rowIndex>=0)&&(rowIndex<users.size())){
							User user = users.get(rowIndex);
							GUIDriver.getDriver().updateUser(user.getUserID(), aValue.toString());
							user = GUIDriver.getDriver().getUser(user.getUserID());
							users.set(rowIndex, user);
							event=new TableModelEvent(UsersTableModel.this,rowIndex);
						}
						else if(rowIndex==users.size()){
							UserID newID = GUIDriver.getDriver().createUser(aValue.toString());
							users.add(GUIDriver.getDriver().getUser(newID));
							// Create default quiz design.
							HashSet<QuizDesignSubject> subjects = new HashSet<>();
							subjects.add(new QuizDesignSubject(new Subject("com.shtick.math.3rd"),5,5));
							QuizDesign design = new QuizDesign(newID, "Default Math Quiz", subjects, 5, 5);
							GUIDriver.getDriver().createQuizDesign(design);
							event=new TableModelEvent(UsersTableModel.this,rowIndex,rowIndex,TableModelEvent.ALL_COLUMNS,TableModelEvent.INSERT);
						}
						for(TableModelListener listener:userListListeners)
							listener.tableChanged(event);
					}
					catch(IOException t){
						ErrorDialog.showError(GUIDriver.getMainFrame(), t);
					}
				}
			}
		}
		
		@Override
		public void removeTableModelListener(TableModelListener l) {
			userListListeners.remove(l);
		}
		
		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			synchronized(userList){
				return ((rowIndex>=0)&&(rowIndex<=users.size())&&(columnIndex==0));
			}
		}
		
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			synchronized(userList){
				if((rowIndex>=0)&&(rowIndex<users.size())){
					User user=users.get(rowIndex);
					if(columnIndex==0)
						return user.getName();
					if(columnIndex==1)
						return user.getTimeAdded().format(DateTimeFormatter.ISO_DATE_TIME);
				}
				return null;
			}
		}
		
		@Override
		public int getRowCount() {
			return users.size()+1;
		}
		
		@Override
		public String getColumnName(int columnIndex) {
			if(columnIndex == 0)
				return "Username";
			if(columnIndex == 1)
				return "Date Created";
			return null;
		}
		
		@Override
		public int getColumnCount() {
			return 2;
		}
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return String.class;
		}
		
		@Override
		public void addTableModelListener(TableModelListener l) {
			userListListeners.add(l);
		}
	}
	
}
