package com.shtick.apps.components;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

/**
 * @author Sean M. Cox
 *
 */
public class ErrorDialog extends JDialog implements ActionListener {
    /**
     * Indicates that the OK button was clicked.
     */
    public static final int APPROVE_OPTION=0;
    /**
     * Indicates that the window was closed using another mechanism.
     */
    public static final int CANCEL_OPTION=1;
    /**
     * Indicates that there was some error.
     */
    public static final int ERROR_OPTION=2;
    
    private int returnValue=ERROR_OPTION;

    private JButton okButton;

    private Throwable throwable;
    
    private String debugData;

    /**
     * @param owner
     * @param t
     * @param debugData 
     * @throws HeadlessException
     */
    public ErrorDialog(Frame owner,Throwable t, String debugData) throws HeadlessException {
        this(owner,"Error",t,debugData);
    }

    /**
     * @param owner
     * @param title
     * @param t
     * @param debugData 
     * @throws HeadlessException
     */
    public ErrorDialog(Frame owner,String title, Throwable t, String debugData) throws HeadlessException {
        super(owner,title,true);
        throwable=t;
        this.debugData = debugData;
        errorDialogInit();
    }
    /**
     * @param owner
     * @param t
     * @param debugData 
     * @throws HeadlessException
     */
    public ErrorDialog(Dialog owner,Throwable t, String debugData) throws HeadlessException {
        this(owner,"Error",t,debugData);
    }

    /**
     * @param owner
     * @param title
     * @param t
     * @param debugData 
     * @throws HeadlessException
     */
    public ErrorDialog(Dialog owner,String title, Throwable t, String debugData) throws HeadlessException {
        super(owner,title,true);
        throwable=t;
        this.debugData = debugData;
        errorDialogInit();
    }

    protected void errorDialogInit(){
        okButton=new JButton("OK");
        okButton.addActionListener(this);

        JLabel errorLabel=new JLabel(throwable.toString(), SwingConstants.CENTER);

        JTextArea detailText=new JTextArea();
        StringWriter errorDetailStream=new StringWriter();
        throwable.printStackTrace(new PrintWriter(errorDetailStream));
        if((debugData!=null)&&(debugData.length()>0)) {
        	errorDetailStream.write("\n\nDebug Data:\n");
        	errorDetailStream.write(debugData);
        }
        detailText.setText(errorDetailStream.toString());
        detailText.setEditable(false);
        detailText.setEnabled(true);
        JScrollPane detailPane=new JScrollPane(detailText);
        detailPane.setBorder(BorderFactory.createEtchedBorder());

        Container contentPane=getContentPane();
        contentPane.add(errorLabel, BorderLayout.NORTH);
        contentPane.add(detailPane, BorderLayout.CENTER);
        contentPane.add(okButton, BorderLayout.SOUTH);
        setSize(400,250);
    }

    /**
     * Constructs and displays a frame reporting an error.
     * @param owner 
     * @param t The error that the new frame will report.
     * @return The exit code from the ErrorDialog one of
     *         APPROVE_OPTION, CANCEL_OPTION, or ERROR_OPTION
     */
    public static int showError(Frame owner,Throwable t){
        ErrorDialog dialog=new ErrorDialog(owner,t,null);
        return dialog.showErrorDialog();
    }

    /**
     * Constructs and displays a frame reporting an error.
     * @param owner 
     * @param t The error that the new frame will report.
     * @param debugData supplemental data to give the error context. 
     * @return The exit code from the ErrorDialog one of
     *         APPROVE_OPTION, CANCEL_OPTION, or ERROR_OPTION
     */
    public static int showError(Frame owner,Throwable t,String debugData){
        ErrorDialog dialog=new ErrorDialog(owner,t,debugData);
        return dialog.showErrorDialog();
    }

    /**
     * Shows the ErrorDialog and returns a status value when it is closed.
     * @return exit code
     */
    public int showErrorDialog(){
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                returnValue = CANCEL_OPTION;
            }
        });

        setVisible(true);
        dispose(); // Free up native resources and any references to this dialog box.

        return returnValue;
    }

    /**
     * @return the throable
     */
    public Throwable getThrowable() {
        return throwable;
    }

    @Override
	public void actionPerformed(ActionEvent e) {
        returnValue = APPROVE_OPTION;
        setVisible(false);
    }
}
