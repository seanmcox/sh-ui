package com.shtick.apps.sh.ui.gui.panels;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.shtick.apps.components.ErrorDialog;
import com.shtick.apps.sh.core.Quiz;
import com.shtick.apps.sh.core.QuizDesign;
import com.shtick.apps.sh.core.User;
import com.shtick.apps.sh.ui.gui.GUIDriver;

/**
 * @author sean.cox
 *
 */
public class UserPortalPanel extends JPanel {
	private JButton takeQuizButton = new JButton("Take a New Quiz");
	private JButton manageQuizDesignButton = new JButton("Manage Quiz Designs *");
	private JButton reviewOldQuizesButton = new JButton("Review Old Quizes");
	private JButton subjectAnalysisButton = new JButton("Subject Analysis *");
	private JButton exitButton = new JButton("Exit User Portal");
	private User user;
	private Collection<QuizDesign> quizDesigns=null;

	/**
	 * @param user 
	 * 
	 */
	public UserPortalPanel(User user) {
		super(new GridBagLayout());
		this.user = user;
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		JLabel title = new JLabel("User Portal for "+user.getName());
		title.setFont(title.getFont().deriveFont(title.getFont().getSize2D()*2));
		add(title, gbc);
		add(takeQuizButton, gbc);
		add(manageQuizDesignButton, gbc);
		add(reviewOldQuizesButton, gbc);
		add(subjectAnalysisButton, gbc);
		add(exitButton, gbc);
		
		try{
			quizDesigns=GUIDriver.getDriver().getQuizDesigns(user.getUserID());
			takeQuizButton.setEnabled(quizDesigns.size()>0);
		}
		catch(IOException t){
			quizDesigns = new LinkedList<>();
			ErrorDialog.showError(GUIDriver.getMainFrame(), t);
		}
		takeQuizButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				QuizDesign quizDesign;
				if(quizDesigns.size()>1){
					// TODO Create user interface to handle this case.
					quizDesign=null;
				}
				else{
					quizDesign = quizDesigns.iterator().next();
				}
				Quiz quiz;
				try{
					quiz = GUIDriver.getDriver().generateQuiz(quizDesign.getQuizDesignID());
				}
				catch(IOException t){
					ErrorDialog.showError(GUIDriver.getMainFrame(), t);
					return;
				}
				
				// Open UI for navigating quiz.
				try{
					GUIDriver.getMainFrame().openApp(new QuizQuestionPanel(quiz,true));
				}
				catch(IOException t){
					ErrorDialog.showError(GUIDriver.getMainFrame(), t);
				}
			}
		});
		reviewOldQuizesButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GUIDriver.getMainFrame().openApp(new QuizListPanel(user));
			}
		});
		exitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GUIDriver.getMainFrame().closeApp(UserPortalPanel.this);
			}
		});
	}

}
