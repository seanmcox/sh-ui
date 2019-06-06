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
import java.util.HashSet;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.MutableComboBoxModel;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import com.shtick.apps.sh.core.QuizDesign;
import com.shtick.apps.sh.core.QuizDesignSubject;
import com.shtick.apps.sh.core.Subject;
import com.shtick.apps.sh.core.User;
import com.shtick.apps.sh.ui.gui.GUIDriver;

/**
 * @author Sean
 *
 */
public class QuizDesignDialog extends JDialog {
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

    private User user;
    private QuizDesign quizDesign;

	private JTable subjectsTable;
    private JTextField nameField;
    private JSpinner minSpinner;
    private JSpinner maxSpinner;
    private JComboBox<Subject> subjectCombo;

    private JButton addSubjectButton=new JButton("Add Subject");
    private JButton removeSubjectButton=new JButton("Remove Subject");
    private JButton cancelButton=new JButton("Cancel");
    private JButton applyButton=new JButton("Save & Close");
	private ArrayList<QuizDesignSubject> designSubjects = new ArrayList<>();
	private HashSet<TableModelListener> subjectsTableListeners=new HashSet<>();
	private Collection<Subject> subjects;
    
    /**
     * @param owner
     * @param user
     * @param quizDesign
     * @throws IOException 
     */
    public QuizDesignDialog(Frame owner, User user, QuizDesign quizDesign) throws IOException{
    	super(owner,"Designing Quiz for "+user.getName(),true);
    	this.user = user;
    	this.quizDesign = quizDesign;
    	subjects = new ArrayList<>(GUIDriver.getDriver().getAllSubjects());

    	setupUI(quizDesign);
        
        updateEnabling();
        setSize(600,350);
    }
    
    private void setupUI(QuizDesign quizDesign) {
        Container contentPane=getContentPane();
        contentPane.setLayout(new BorderLayout());
        
        JPanel topPanel = new JPanel(new GridLayout(4, 1));
        {
	        nameField = new JTextField();
	        nameField.setText(quizDesign.getTitle());
	        JPanel titlePanel = new JPanel(new BorderLayout());
	        titlePanel.add(new JLabel("Design Name:"),BorderLayout.LINE_START);
	        titlePanel.add(nameField,BorderLayout.CENTER);

	        minSpinner = new JSpinner();
	        minSpinner.setValue(quizDesign.getMinQuestions());
	        ((DefaultEditor)minSpinner.getEditor()).getTextField().setColumns(3);
	        JPanel minPanel = new JPanel();
	        minPanel.add(new JLabel("Minimum Quiz Questions:"));
	        minPanel.add(minSpinner);
	        
	        maxSpinner = new JSpinner();
	        maxSpinner.setValue(quizDesign.getMaxQuestions());
	        ((DefaultEditor)maxSpinner.getEditor()).getTextField().setColumns(3);
	        JPanel maxPanel = new JPanel();
	        maxPanel.add(new JLabel("Maximum Quiz Questions:"));
	        maxPanel.add(maxSpinner);

	    	subjectCombo = new JComboBox<Subject>(new SubjectComboBoxModel());
	        JPanel addSubjectPanel = new JPanel(new BorderLayout());
	        addSubjectPanel.add(new JLabel("Subject:"), BorderLayout.LINE_START);
	        addSubjectPanel.add(subjectCombo, BorderLayout.CENTER);
	        addSubjectPanel.add(addSubjectButton, BorderLayout.LINE_END);
	        
	        topPanel.add(titlePanel);
	        topPanel.add(minPanel);
	        topPanel.add(maxPanel);
	        topPanel.add(addSubjectPanel);
        }
        
        JPanel buttonPanel=new JPanel(new BorderLayout());
        {
	        JPanel bottomButtonPanel = new JPanel(new GridLayout(2,1));
	        bottomButtonPanel.add(cancelButton);
	        bottomButtonPanel.add(applyButton);

	        buttonPanel.add(removeSubjectButton,BorderLayout.PAGE_START);
	        buttonPanel.add(bottomButtonPanel,BorderLayout.PAGE_END);
        }
        
        subjectsTable=new JTable(new QuizDesignSubjectsTableModel());
        designSubjects.addAll(quizDesign.getSubjects());

        contentPane.add(topPanel, BorderLayout.PAGE_START);
        contentPane.add(new JScrollPane(subjectsTable), BorderLayout.CENTER);
        contentPane.add(buttonPanel, BorderLayout.LINE_END);

        subjectsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				updateEnabling();
			}
		});
        subjectCombo.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				updateEnabling();
			}
		});
        
        addSubjectButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object selectedSubject = subjectCombo.getSelectedItem();
				if(selectedSubject==null)
					return;
				designSubjects.add(new QuizDesignSubject((Subject)selectedSubject, 1, 1));
				TableModelEvent event=new TableModelEvent(subjectsTable.getModel(),designSubjects.size()-1,designSubjects.size()-1,TableModelEvent.ALL_COLUMNS,TableModelEvent.INSERT);
				for(TableModelListener listener:subjectsTableListeners)
					listener.tableChanged(event);
				subjectCombo.removeItem(selectedSubject);
				subjectCombo.setSelectedItem(null);
			}
		});
        removeSubjectButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int selectedRow = subjectsTable.getSelectedRow();
				if(selectedRow<0)
					return;
				Object selectedSubject = subjectsTable.getValueAt(selectedRow, 0);
				System.out.println("Tried to remove: "+selectedSubject);
				subjectCombo.addItem(new Subject(selectedSubject.toString()));
				designSubjects.remove(selectedRow);
				TableModelEvent event=new TableModelEvent(subjectsTable.getModel(),selectedRow,selectedRow,TableModelEvent.ALL_COLUMNS,TableModelEvent.DELETE);
				for(TableModelListener listener:subjectsTableListeners)
					listener.tableChanged(event);
			}
		});
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
    }
    
    private void updateEnabling(){
    	addSubjectButton.setEnabled(subjectCombo.getSelectedItem()!=null);
    	removeSubjectButton.setEnabled(subjectsTable.getSelectedRow()>=0);
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
    public QuizDesign getQuizDesign(){
		return new QuizDesign(quizDesign.getQuizDesignID(),user.getUserID(), nameField.getText(), new HashSet<>(designSubjects), (Integer)minSpinner.getValue(), (Integer)maxSpinner.getValue(),quizDesign.getTimeAdded());
    }
	
	private class SubjectComboBoxModel implements MutableComboBoxModel<Subject>{
		private LinkedList<ListDataListener> listeners = new LinkedList<>();
		private Subject selectedObject = null;
    	private LinkedList<Subject> unusedSubjects;
    	
    	public SubjectComboBoxModel() {
        	unusedSubjects = new LinkedList<>(subjects);
        	for(QuizDesignSubject subject:quizDesign.getSubjects())
        		unusedSubjects.remove(subject.getSubject());
		}
		
		/* (non-Javadoc)
		 * @see javax.swing.ListModel#getSize()
		 */
		@Override
		public int getSize() {
			return unusedSubjects.size();
		}

		/* (non-Javadoc)
		 * @see javax.swing.ListModel#getElementAt(int)
		 */
		@Override
		public Subject getElementAt(int index) {
			if((index<0)||(index>=unusedSubjects.size()))
				return null;
			return unusedSubjects.get(index);
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
			if (anItem instanceof Subject)
				selectedObject = (Subject)anItem;
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

		/* (non-Javadoc)
		 * @see javax.swing.MutableComboBoxModel#addElement(java.lang.Object)
		 */
		@Override
		public void addElement(Subject item) {
			if(!unusedSubjects.contains(item))
				unusedSubjects.add(item);
		}

		/* (non-Javadoc)
		 * @see javax.swing.MutableComboBoxModel#removeElement(java.lang.Object)
		 */
		@Override
		public void removeElement(Object obj) {
			unusedSubjects.remove(obj);
		}

		/* (non-Javadoc)
		 * @see javax.swing.MutableComboBoxModel#insertElementAt(java.lang.Object, int)
		 */
		@Override
		public void insertElementAt(Subject item, int index) {
			if(!unusedSubjects.contains(item))
				unusedSubjects.add(index,item);
		}

		/* (non-Javadoc)
		 * @see javax.swing.MutableComboBoxModel#removeElementAt(int)
		 */
		@Override
		public void removeElementAt(int index) {
			unusedSubjects.remove(index);
		}
	}
	
	private class QuizDesignSubjectsTableModel implements TableModel{
		
		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			synchronized(subjectsTable){
				if((rowIndex>=0)&&(rowIndex<designSubjects.size())){
					QuizDesignSubject quizDesignSubject = designSubjects.remove(rowIndex);
					designSubjects.add(rowIndex, new QuizDesignSubject(
							quizDesignSubject.getSubject(),
							((columnIndex==1)?Integer.parseInt(aValue.toString()):quizDesignSubject.getMinQuestions()),
							((columnIndex==2)?Integer.parseInt(aValue.toString()):quizDesignSubject.getMaxQuestions())
					));
					
					TableModelEvent event=new TableModelEvent(QuizDesignSubjectsTableModel.this,rowIndex,rowIndex,columnIndex,TableModelEvent.UPDATE);
					for(TableModelListener listener:subjectsTableListeners)
						listener.tableChanged(event);
				}
			}
		}
		
		@Override
		public void removeTableModelListener(TableModelListener l) {
			subjectsTableListeners.remove(l);
		}
		
		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			synchronized(subjectsTable){
				return ((rowIndex>=0)&&(rowIndex<=designSubjects.size())&&(columnIndex>0)&&(columnIndex<=2));
			}
		}
		
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			synchronized(subjectsTable){
				if((rowIndex>=0)&&(rowIndex<designSubjects.size())){
					QuizDesignSubject quizDesignSubject=designSubjects.get(rowIndex);
					if(columnIndex==0)
						return quizDesignSubject.getSubject().toString();
					if(columnIndex==1)
						return ""+quizDesignSubject.getMinQuestions();
					if(columnIndex==2)
						return ""+quizDesignSubject.getMaxQuestions();
				}
				return null;
			}
		}
		
		@Override
		public int getRowCount() {
			return designSubjects.size();
		}
		
		@Override
		public String getColumnName(int columnIndex) {
			if(columnIndex == 0)
				return "Subject";
			if(columnIndex == 1)
				return "Min";
			if(columnIndex == 2)
				return "Max";
			return null;
		}
		
		@Override
		public int getColumnCount() {
			return 3;
		}
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return String.class;
		}
		
		@Override
		public void addTableModelListener(TableModelListener l) {
			subjectsTableListeners.add(l);
		}
	}
}
