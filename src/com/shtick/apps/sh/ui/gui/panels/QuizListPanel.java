package com.shtick.apps.sh.ui.gui.panels;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import com.shtick.apps.components.ErrorDialog;
import com.shtick.apps.sh.core.Answer;
import com.shtick.apps.sh.core.Question;
import com.shtick.apps.sh.core.Quiz;
import com.shtick.apps.sh.core.QuizID;
import com.shtick.apps.sh.core.User;
import com.shtick.apps.sh.ui.gui.GUIDriver;

/**
 * @author sean.cox
 *
 */
public class QuizListPanel extends JPanel {
	private JTable userList;
	private JButton reviewButton = new JButton("Review Quiz");
	private JButton mainMenuButton = new JButton("Return to Menu");
	private ArrayList<Quiz> quizes = new ArrayList<>();
	private HashSet<TableModelListener> userListListeners=new HashSet<>();
	private HashMap<QuizID,String> quizScores=new HashMap<>();

	/**
	 * @param user 
	 * 
	 */
	public QuizListPanel(User user) {
		super(new BorderLayout());
		

		try{
			quizes = new ArrayList<>(GUIDriver.getDriver().getUserQuizes(user.getUserID()));
			quizScores = new HashMap<>(quizes.size());
		}
		catch(IOException t){
			ErrorDialog.showError(GUIDriver.getMainFrame(), t);
		}
		
		userList=new JTable(new QuizTableModel());

		{
			JLabel title = new JLabel("Manage Users");
			title.setFont(this.getFont().deriveFont(this.getFont().getSize()*2.0f));
			add(title, BorderLayout.NORTH);
		}
		add(new JScrollPane(userList), BorderLayout.CENTER);
		{
			JPanel buttonPanel = new JPanel();
			buttonPanel.add(reviewButton);
			buttonPanel.add(mainMenuButton);
			add(buttonPanel, BorderLayout.EAST);
		}
		
		mainMenuButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GUIDriver.getMainFrame().openApp(new UserPortalPanel(user));
			}
		});
		reviewButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				synchronized(userList){
					int row = userList.getSelectedRow();
					if(row<0)
						return;
					Quiz quiz = quizes.get(row);
					try{
						GUIDriver.getMainFrame().openApp(new QuizQuestionPanel(quiz,false));
					}
					catch(IOException t){
						ErrorDialog.showError(GUIDriver.getMainFrame(), t);
					}
				}
			}
		});
		updateEnabling();
		userList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				updateEnabling();
			}
		});
	}
	
	private void updateEnabling(){
		reviewButton.setEnabled(userList.getSelectedRow()>=0);
	}
	
	private class QuizTableModel implements TableModel{
		
		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		}
		
		@Override
		public void removeTableModelListener(TableModelListener l) {
			userListListeners.remove(l);
		}
		
		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}
		
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			synchronized(userList){
				if((rowIndex>=0)&&(rowIndex<quizes.size())){
					Quiz quiz=quizes.get(rowIndex);
					if(columnIndex==0)
						return quiz.getTimeAdded().format(DateTimeFormatter.ISO_DATE_TIME);
					if(columnIndex==1)
						return getQuizScore(quiz.getQuizID());
				}
				return null;
			}
		}
		
		@Override
		public int getRowCount() {
			return quizes.size();
		}
		
		@Override
		public String getColumnName(int columnIndex) {
			if(columnIndex == 0)
				return "Date/Time Taken";
			if(columnIndex == 1)
				return "Score";
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
		
		private String getQuizScore(QuizID quizID){
			if(!quizScores.containsKey(quizID)){
				try{
					Collection<Question> questions = GUIDriver.getDriver().getQuizQuestions(quizID);
					Answer answer;
					int totalPoints = 0;
					int earnedPoints = 0;
					for(Question question:questions){
						totalPoints+=question.getPoints();
						answer = GUIDriver.getDriver().getLatestAnswer(question.getQuestionID());
						if(answer!=null)
							earnedPoints+=answer.getPoints();
					}
					quizScores.put(quizID, earnedPoints+"/"+totalPoints);
				}
				catch(IOException t){
					return "IOException";
				}
			}
			return quizScores.get(quizID);
		}
	}
}
