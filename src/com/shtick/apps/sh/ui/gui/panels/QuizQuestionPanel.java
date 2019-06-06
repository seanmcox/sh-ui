package com.shtick.apps.sh.ui.gui.panels;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.svg.SVGDocument;

import com.shtick.apps.components.ErrorDialog;
import com.shtick.apps.sh.core.Answer;
import com.shtick.apps.sh.core.Question;
import com.shtick.apps.sh.core.Quiz;
import com.shtick.apps.sh.core.content.Choice;
import com.shtick.apps.sh.core.content.Marshal;
import com.shtick.apps.sh.core.content.MultipleChoice;
import com.shtick.apps.sh.ui.gui.GUIDriver;
import com.shtick.utils.data.json.JSONDecoder;
import com.shtick.utils.data.json.JSONEncoder;

/**
 * @author sean.cox
 *
 */
public class QuizQuestionPanel extends JPanel {
	private JButton nextButton = new JButton("Next >");
	private JButton previousButton = new JButton("< Previous");
	private JButton exitButton = new JButton("Exit Quiz");
	private Quiz quiz;
	private Question[] questions;
	private int questionIndex;
	private ZonedDateTime questionAskedTime;
	private Answer oldAnswer = null;
	private boolean allowAnswering;
	private ImageIcon checkIcon;
	private ImageIcon xIcon;
	private ImageIcon arrowIcon;

	/**
	 * A constructor for initially loading the panel with the first question.
	 * @param quiz 
	 * @param allowAnswering 
	 * @throws IOException 
	 * 
	 */
	public QuizQuestionPanel(Quiz quiz, boolean allowAnswering) throws IOException{
		this(quiz,GUIDriver.getDriver().getQuizQuestions(quiz.getQuizID()).toArray(new Question[0]),0, allowAnswering);
		
	}

	/**
	 * Loads the panel with the given quiz and the given questions, with the ith question being presented.
	 * 
	 * @param quiz
	 * @param questions
	 * @param i
	 * @param forAnswering If false, then this will display as a question review.
	 */
	public QuizQuestionPanel(Quiz quiz, Question[] questions, int i, boolean forAnswering) {
		super(new BorderLayout());
		this.quiz = quiz;
		this.questions = questions;
		this.questionIndex = i;
		this.questionAskedTime = ZonedDateTime.now();
		this.allowAnswering = forAnswering;
		
		JLabel title = new JLabel("Question "+(i+1)+" of "+questions.length);
		{
			title.setFont(title.getFont().deriveFont(title.getFont().getSize2D()*2));
		}
		QuestionPanel questionPanel = new QuestionPanel(questions[i]);
		JPanel buttonPanel = new JPanel(new BorderLayout());
		{
			buttonPanel.add(previousButton,BorderLayout.LINE_START);
			buttonPanel.add(exitButton,BorderLayout.CENTER);
			buttonPanel.add(nextButton,BorderLayout.LINE_END);
		}
		
		add(title, BorderLayout.NORTH);
		add(questionPanel, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);

		if((i+1)==questions.length)
			nextButton.setText("Done >");
		previousButton.setEnabled(i>0);

		previousButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try{
					questionPanel.saveAnswer();
				}
				catch(IOException t){
					ErrorDialog.showError(GUIDriver.getMainFrame(), t);
					return;
				}
				GUIDriver.getMainFrame().openApp(new QuizQuestionPanel(quiz,questions,i-1,forAnswering));
			}
		});
		nextButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try{
					questionPanel.saveAnswer();
				}
				catch(IOException t){
					ErrorDialog.showError(GUIDriver.getMainFrame(), t);
					return;
				}
				if((i+1)==questions.length){
					try{
						if(allowAnswering){
							boolean allAnswered=true;
							Answer answer;
							for(int i=0;i<questions.length;i++){
								answer = GUIDriver.getDriver().getLatestAnswer(questions[i].getQuestionID());
								if(answer==null){
									allAnswered = false;
									break;
								}
							}
							if(!allAnswered){
								int result = JOptionPane.showConfirmDialog(QuizQuestionPanel.this, "Not all questions have been answered. Do you really want to finish the quiz?", "Confirm Done", JOptionPane.YES_NO_OPTION);
								if(result==JOptionPane.NO_OPTION)
									return;
							}
						}
						GUIDriver.getMainFrame().openApp(new QuizResultsPanel(quiz,questions));
					}
					catch(IOException t){
						ErrorDialog.showError(GUIDriver.getMainFrame(), t);
						return;
					}
				}
				else{
					GUIDriver.getMainFrame().openApp(new QuizQuestionPanel(quiz,questions,i+1,forAnswering));
				}
			}
		});
		exitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try{
					questionPanel.saveAnswer();
				}
				catch(IOException t){
					int result = JOptionPane.showConfirmDialog(GUIDriver.getMainFrame(), "Error saving answer:\n"+t.toString()+"\nExit the quiz anyway?");
					if(result!=JOptionPane.YES_OPTION)
						return;
				}
				try{
					GUIDriver.getMainFrame().openApp(new UserPortalPanel(GUIDriver.getDriver().getUser(quiz.getUserID())));
				}
				catch(IOException t){
					ErrorDialog.showError(GUIDriver.getMainFrame(), t);
					GUIDriver.getMainFrame().closeApp(QuizQuestionPanel.this);
				}
			}
		});
	}
	
	private class QuestionPanel extends JPanel{
		private Question question;
		private ValueSource valueSource;

		/**
		 * @param question
		 */
		public QuestionPanel(Question question) {
			super(new BorderLayout(0,15));
			this.question = question;
			Component questionPromptArea;
			try {
				questionPromptArea = getMediaComponent(question.getPromptType(), question.getPrompt());
			}
			catch(IllegalArgumentException t) {
				StringWriter strOut = new StringWriter();
				t.printStackTrace(new PrintWriter(strOut));
				String message = strOut.toString();
				System.out.println(message);
				JTextArea promptArea=new JTextArea(message);
				promptArea.setFont(GUIDriver.getBaseFont());
				promptArea.setEditable(false);
				questionPromptArea = promptArea;
			}
			JScrollPane scrollPane = new JScrollPane(questionPromptArea);
			scrollPane.setMinimumSize(new Dimension(100, Math.max(100,questionPromptArea.getPreferredSize().height+20)));
			this.add(scrollPane, BorderLayout.NORTH);
			JPanel answerPanel;
			try {
				if("choice/radio".equals(question.getAnswerPromptType())) {
					MultipleChoice multipleChoice = Marshal.unmarshalMultipleChoice(question.getAnswerPrompt());
					answerPanel = getRadioAnswerPanel(multipleChoice);
				}
				else if("choice/checkbox".equals(question.getAnswerPromptType())) {
					MultipleChoice multipleChoice = Marshal.unmarshalMultipleChoice(question.getAnswerPrompt());
					answerPanel = getCheckboxAnswerPanel(multipleChoice);
				}
				else{
					answerPanel = getTextAnswerPanel(question.getAnswerPrompt());
				}
			}
			catch(IllegalArgumentException t) {
				StringWriter strOut = new StringWriter();
				t.printStackTrace(new PrintWriter(strOut));
				String message = strOut.toString();
				System.out.println(message);
				JTextArea promptArea=new JTextArea(message);
				promptArea.setFont(GUIDriver.getBaseFont());
				promptArea.setEditable(false);

				answerPanel = new JPanel(new GridBagLayout());
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridwidth = GridBagConstraints.REMAINDER;
				gbc.fill = GridBagConstraints.HORIZONTAL;

				answerPanel.add(new JScrollPane(promptArea), gbc);
			}
			this.add(answerPanel, BorderLayout.CENTER);
		}
		
		private JPanel getTextAnswerPanel(String promptSpecification) throws IllegalArgumentException{
			Component answerPromptArea;
			answerPromptArea = getMediaComponent(question.getAnswerPromptType(), question.getAnswerPrompt()); 

			JPanel answerPanel = new JPanel(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			gbc.fill = GridBagConstraints.HORIZONTAL;

			answerPanel.add(new JScrollPane(answerPromptArea), gbc);
			final JTextField valueField = new JTextField(15);
			{ // Grab old answer if there is one.
				try{
					oldAnswer = GUIDriver.getDriver().getLatestAnswer(question.getQuestionID());
				}
				catch(IOException t){
					ErrorDialog.showError(GUIDriver.getMainFrame(), t);
				}
				if(oldAnswer!=null)
					valueField.setText(oldAnswer.getValue());
				valueField.setFont(GUIDriver.getBaseFont());
			}
			answerPanel.add(valueField, gbc);
			if(!allowAnswering){
				valueField.setEditable(false);
				JLabel feedbackLabel;
				if(oldAnswer==null){
					feedbackLabel = new JLabel("No answer given. Expected answer: "+question.getAnswerValue());
				}
				else if(!oldAnswer.getValue().equals(question.getAnswerValue())){
					valueField.setForeground(Color.RED);
					feedbackLabel = new JLabel("Wrong answer. Expected answer: "+question.getAnswerValue());
				}
				else{
					valueField.setForeground(Color.GREEN);
					feedbackLabel = new JLabel("Correct Answer!");
				}
				feedbackLabel.setFont(GUIDriver.getBaseFont());
				answerPanel.add(feedbackLabel, gbc);
			}
			valueSource = new ValueSource() {
				@Override
				public String getValue() {
					return valueField.getText();
				}
			};
			return answerPanel;
		}
		
		private JPanel getCheckboxAnswerPanel(MultipleChoice multipleChoice) {
			Component answerPromptArea = getMediaComponent(multipleChoice.getType(), multipleChoice.getContent());
			List<Choice> choices = multipleChoice.getChoices();
			ArrayList<JPanel> choicePanels = new ArrayList<>(choices.size());
			final HashMap<ButtonModel,String> buttonValues = new HashMap<>(choices.size());
			List<Object> choiceValuesSelected;
			List<Object> choiceValuesCorrect;
			try{
				oldAnswer = GUIDriver.getDriver().getLatestAnswer(question.getQuestionID());
			}
			catch(IOException t){
				ErrorDialog.showError(GUIDriver.getMainFrame(), t);
			}
			if(oldAnswer!=null){
				Object jsonDecoded = JSONDecoder.decode(oldAnswer.getValue(), null);
				if(!(jsonDecoded instanceof List))
					choiceValuesSelected = new LinkedList<>();
				else
					choiceValuesSelected = (List<Object>)jsonDecoded;
			}
			else {
				choiceValuesSelected = new LinkedList<>();
			}
			if(!allowAnswering) {
				Object jsonDecoded = JSONDecoder.decode(oldAnswer.getValue(), null);
				if(!(jsonDecoded instanceof List))
					choiceValuesCorrect = new LinkedList<>();
				else
					choiceValuesCorrect = (List<Object>)jsonDecoded;
			}
			else {
				choiceValuesCorrect = new LinkedList<>();
			}
			for(Choice choice:choices) {
				Component choiceComponent = getMediaComponent(choice.getType(), choice.getContent());
				JPanel choicePanel = new JPanel(new FlowLayout());
				JCheckBox checkbox = new JCheckBox();
				checkbox.getModel().setSelected(choiceValuesSelected.contains(choice.getValue()));
				if(!allowAnswering) {
					if(choiceValuesSelected.contains(choice.getValue())) {
						if(choiceValuesCorrect.contains(choice.getValue()))
							choicePanel.add(new JLabel(getCheckIcon()));
						else
							choicePanel.add(new JLabel(getXIcon()));
					}
					else {
						if(choiceValuesCorrect.contains(choice.getValue()))
							choicePanel.add(new JLabel(getArrowIcon()));
					}
				}
				buttonValues.put(checkbox.getModel(), choice.getValue());
				choicePanel.add(checkbox);
				choicePanel.add(choiceComponent);
				choicePanels.add(choicePanel);
			}

			if(!allowAnswering)
				for(ButtonModel buttonModel:buttonValues.keySet())
					buttonModel.setEnabled(false);

			JPanel answerPanel = new JPanel(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			
			answerPanel.add(new JScrollPane(answerPromptArea), gbc);
			for(Component choicePanel:choicePanels)
				answerPanel.add(choicePanel, gbc);
			valueSource = new ValueSource() {
				@Override
				public String getValue() {
					LinkedList<String> valueList = new LinkedList<>();
					for(ButtonModel buttonModel:buttonValues.keySet()) {
						if(buttonModel.isSelected())
							valueList.add(buttonValues.get(buttonModel));
					}
					return JSONEncoder.encode(valueList.toArray());
				}
			};
			return answerPanel;
		}
		
		private JPanel getRadioAnswerPanel(MultipleChoice multipleChoice) {
			Component answerPromptArea = getMediaComponent(multipleChoice.getType(), multipleChoice.getContent());
			List<Choice> choices = multipleChoice.getChoices();
			ArrayList<JPanel> choicePanels = new ArrayList<>(choices.size());
			final HashMap<ButtonModel,String> buttonValues = new HashMap<>(choices.size());
			final ButtonGroup radioGroup = new ButtonGroup();
			ButtonModel selectedButton = null;
			try{
				oldAnswer = GUIDriver.getDriver().getLatestAnswer(question.getQuestionID());
			}
			catch(IOException t){
				ErrorDialog.showError(GUIDriver.getMainFrame(), t);
			}
			for(Choice choice:choices) {
				Component choiceComponent = getMediaComponent(choice.getType(), choice.getContent());
				JPanel choicePanel = new JPanel(new FlowLayout());
				JRadioButton radio = new JRadioButton();
				radioGroup.add(radio);
				if((oldAnswer!=null)&&(oldAnswer.getValue().equals(choice.getValue()))) {
					selectedButton = radio.getModel();
				}
				if(!allowAnswering) {
					if((oldAnswer!=null)&&(oldAnswer.getValue().equals(choice.getValue()))) {
						if(choice.getValue().equals(question.getAnswerValue()))
							choicePanel.add(new JLabel(getCheckIcon()));
						else
							choicePanel.add(new JLabel(getXIcon()));
					}
					else {
						if(choice.getValue().equals(question.getAnswerValue()))
							choicePanel.add(new JLabel(getArrowIcon()));
					}
				}
				buttonValues.put(radio.getModel(), choice.getValue());
				choicePanel.add(radio);
				choicePanel.add(new JScrollPane(choiceComponent));
				choicePanels.add(choicePanel);
			}
			if(selectedButton!=null)
				selectedButton.setSelected(true);

			if(!allowAnswering)
				for(ButtonModel buttonModel:buttonValues.keySet())
					buttonModel.setEnabled(false);
			
			JPanel answerPanel = new JPanel(new BorderLayout());
			JPanel choicesPanel = new JPanel(new GridLayout(choicePanels.size(),1));
			
			answerPanel.add(new JScrollPane(answerPromptArea), BorderLayout.PAGE_START);
			for(Component choicePanel:choicePanels) {
				choicesPanel.add(choicePanel);
			}
			answerPanel.add(choicesPanel,BorderLayout.CENTER);
			valueSource = new ValueSource() {
				@Override
				public String getValue() {
					ButtonModel model = radioGroup.getSelection();
					if(model==null)
						return null;
					return buttonValues.get(model);
				}
			};
			return answerPanel;
		}
		
		/**
		 * 
		 * @param type
		 * @param value
		 * @return The component initialized with the given value.
		 */
		private Component getMediaComponent(String type, String value) throws IllegalArgumentException{
			Component retval = null;
			if("text/plain".equals(type)){
				JTextArea promptArea=new JTextArea(value);
				promptArea.setLineWrap(true);
				promptArea.setWrapStyleWord(true);
				promptArea.setFont(GUIDriver.getBaseFont());
				promptArea.setEditable(false);
				promptArea.setRows(5);
				promptArea.setColumns(50);
				retval = promptArea;
			}
			else if("image/svg+xml".equals(type)){
				JSVGCanvas promptArea = new JSVGCanvas();
                String parser = XMLResourceDescriptor.getXMLParserClassName();
                SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
                SVGDocument doc = null;
                try {
                	doc = (SVGDocument)f.createDocument("Question", new StringReader(value));
                }
                catch(IOException t){
					ErrorDialog.showError(GUIDriver.getMainFrame(), t, value);
                }
				promptArea.setSVGDocument(doc);
				retval = promptArea;
			}
			else {
				throw new IllegalArgumentException("Unrecognized media type: "+type);
			}
			return retval;
		}
		
		/**
		 * Save the answer. Does not generate a new answer record if the answer has not changed or if this panel was called to present without allowing answering.
		 * 
		 * @throws IOException
		 */
		public void saveAnswer() throws IOException{
			if(!allowAnswering)
				return;
			String value = null;
			if(valueSource!=null)
				value = valueSource.getValue();
			if(value==null)
				return;
			if(oldAnswer==null){
				if(value.length()==0)
					return;
			}
			else if(oldAnswer.getValue().equals(value)){
				return;
			}
			GUIDriver.getDriver().saveAnswer(question, valueSource.getValue(), questionAskedTime);
		}
	}
	
	private ImageIcon getCheckIcon() {
		if(checkIcon==null) {
			BufferedImage image = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = image.createGraphics();
			g.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0.0f));
			g.setColor(Color.GREEN);
			g.drawLine(3, 11, 7, 15);
			g.drawLine(7, 15, 17, 5);
			checkIcon = new ImageIcon(image);
		}
		return checkIcon;
	}
	
	private ImageIcon getXIcon() {
		if(xIcon==null) {
			BufferedImage image = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = image.createGraphics();
			g.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0.0f));
			g.setColor(Color.RED);
			g.drawLine(2, 2, 17, 17);
			g.drawLine(2, 17, 17, 2);
			xIcon = new ImageIcon(image);
		}
		return xIcon;
	}
	
	private ImageIcon getArrowIcon() {
		if(arrowIcon==null) {
			BufferedImage image = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = image.createGraphics();
			g.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0.0f));
			g.setColor(Color.GREEN);
			g.drawLine(2, 10, 17, 10);
			g.drawLine(13, 6, 17, 10);
			g.drawLine(13, 14, 17, 10);
			arrowIcon = new ImageIcon(image);
		}
		return arrowIcon;
	}
	
	private interface ValueSource{
		String getValue();
	}
}
