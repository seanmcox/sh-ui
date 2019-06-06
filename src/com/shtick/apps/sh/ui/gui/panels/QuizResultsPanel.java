package com.shtick.apps.sh.ui.gui.panels;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.shtick.apps.components.ErrorDialog;
import com.shtick.apps.sh.core.Answer;
import com.shtick.apps.sh.core.Question;
import com.shtick.apps.sh.core.Quiz;
import com.shtick.apps.sh.ui.gui.GUIDriver;

/**
 * @author sean.cox
 *
 */
public class QuizResultsPanel extends JPanel {
	private JButton exitButton = new JButton("Exit Quiz");
	private JButton reviewButton = new JButton("< Review Quiz");
	private Quiz quiz;
	private Question[] questions;

	/**
	 * A constructor for initially loading the panel with the first question.
	 * @param quiz 
	 * @param allowAnswering 
	 * @throws IOException 
	 * 
	 */
	public QuizResultsPanel(Quiz quiz) throws IOException{
		this(quiz,GUIDriver.getDriver().getQuizQuestions(quiz.getQuizID()).toArray(new Question[0]));
		
	}

	/**
	 * Loads the panel with the given quiz and the given questions, with the ith question being presented.
	 * 
	 * @param quiz
	 * @param questions
	 * @param i
	 * @param allowAnswering 
	 * @throws IOException 
	 */
	public QuizResultsPanel(Quiz quiz, Question[] questions) throws IOException {
		super(new BorderLayout());
		
		this.quiz = quiz;
		this.questions = questions;

		int quizPoints=0;
		int earnedPoints=0;
		Answer answer;
		boolean quizComplete=true;
		for(Question question:questions){
			quizPoints+=question.getPoints();
			answer = GUIDriver.getDriver().getLatestAnswer(question.getQuestionID());
			if(answer==null)
				quizComplete=false;
			else
				earnedPoints+=answer.getPoints();
		}

		JLabel title = new JLabel("Quiz Results");
		{
			title.setFont(title.getFont().deriveFont(title.getFont().getSize2D()*2));
		}
		JPanel buttonPanel = new JPanel(new BorderLayout());
		{
			buttonPanel.add(reviewButton,BorderLayout.LINE_START);
			buttonPanel.add(exitButton,BorderLayout.CENTER);
		}
		
		add(title, BorderLayout.NORTH);
		add(new ResultPanel(quizPoints,earnedPoints, quizComplete), BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);

		reviewButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GUIDriver.getMainFrame().openApp(new QuizQuestionPanel(quiz, questions,0,false));
			}
		});
		exitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try{
					GUIDriver.getMainFrame().openApp(new UserPortalPanel(GUIDriver.getDriver().getUser(quiz.getUserID())));
				}
				catch(IOException t){
					ErrorDialog.showError(GUIDriver.getMainFrame(), t);
					GUIDriver.getMainFrame().closeApp(QuizResultsPanel.this);
				}
			}
		});
	}
	
	private class ResultPanel extends JPanel{

		/**
		 * @param quizPoints 
		 * @param earnedPoints 
		 * @param quizComplete 
		 */
		public ResultPanel(int quizPoints, int earnedPoints, boolean quizComplete) {
			super(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			
			String percent = ""+(earnedPoints*1000/quizPoints);
			percent = ((percent.length()==1)?"0":"")+percent.substring(0, percent.length()-1)+"."+percent.substring(percent.length()-1);
			JLabel label;
			for(String str:new String[]{
					quizComplete?"Quiz Complete":"Quiz Incomplete",
					"Points: "+earnedPoints+"/"+quizPoints,
					percent+"%"
			}){
				label = new JLabel(str);
				label.setFont(label.getFont().deriveFont(label.getFont().getSize2D()*2));
				this.add(label, gbc);
			}
		}
	}
}
