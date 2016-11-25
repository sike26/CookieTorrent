package GraphicInterface;

import CookieClient.Actions;
import Cookie.CookieFile;
import Cookie.SharedFiles;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import java.awt.Dimension;
import javax.swing.JProgressBar;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.BoxLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;
import javax.swing.ImageIcon;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import java.util.Formatter;

@SuppressWarnings("serial")
/**
 * <b>The buttons bar</b>
 */
public class FenetreAvecBouton extends JFrame {

    /**
     * The main panel
     */
    private JPanel panel;
    
    /**
     * The top panel (for the buttons)
     */
    private JPanel panelTop;

    /**
     * The center panel (for the download bars)
     */
    private JPanel panelCenter;

    /**
     * An action object
     */
    private Actions A;

    /**
     * the CookieTorrect main window
     */
    private JFrame frame;

    /**
     * <b>The FenetreAvecBouton constructor</b>
     * @param A
     *        an action object
     * @param frame
     *        The CookieTorrent main window
     */
    public FenetreAvecBouton (Actions A, JFrame frame) {
	this.A = A;
	this.frame = frame;
    }

    ActionListener ActionBouton = new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        Object source = event.getActionCommand();
	ImageIcon imgQuestion = new ImageIcon("Interface/images/question.png");
	if (source.equals("Start")) {
	} 
	else if (source.equals("Stop")) {	
	}
	else if (source.equals("Supprimer")) {
	    String response = (String)JOptionPane.showInputDialog(frame, null, "Entrez un nom de fichier", JOptionPane.QUESTION_MESSAGE, imgQuestion, null, null);	    
	    if (response != null) {
		A.delSharedFile(response);
	    }
	}
      }
    };

    /**
     * The paneltop builder.
     * @return the panel with button start, stop and supprimer
     */
    public JPanel buildContentPane(){

	panel = new JPanel();
	panel.setLayout(new BorderLayout());

	panelTop = new JPanel();
	panelTop.setLayout(new FlowLayout());

	panelCenter = new JPanel(new GridLayout(20,1));
	
	JButton bouton = new JButton("Start");
	bouton.addActionListener(ActionBouton);
	panelTop.add(bouton);
	
	JButton bouton2 = new JButton("Stop");
	bouton2.addActionListener(ActionBouton);
	panelTop.add(bouton2);
	
	JButton bouton3 = new JButton("Supprimer");
	bouton3.addActionListener(ActionBouton);
	panelTop.add(bouton3);

	panelTop.add(new JSeparator(SwingConstants.HORIZONTAL));

	panel.add(panelTop, BorderLayout.NORTH);
	panel.add(panelCenter, BorderLayout.CENTER);

	return panel;
    }

    /**
     * Refresh the downloads bar and download and upload speeds
     * @param files
     *        The files shared by the user
     */
    public void refreshContentPane(SharedFiles files){

	ArrayList<CookieFile> f = files.getFiles();
	panelCenter.removeAll();
	float down = 0;
	float up = 0;
	for (int i = 0; i < f.size(); i++) {
	    down = f.get(i).getDownloadSpeed();
	    up = f.get(i).getUploadSpeed();
	    String s = String.format(" | down: %.1fko/s | up: %.1fko/s", down, up);
	    JLabel label = new JLabel(f.get(i).getFilename()+s);
	    label.setHorizontalAlignment(SwingConstants.CENTER);
	    panelCenter.add(label);
	    JProgressBar progressBar = new JProgressBar(0, 100);
	    if (f.get(i).isComplete())
		progressBar.setValue(100);
	    else 
		progressBar.setValue(f.get(i).getSizePercent());
    
	    progressBar.setStringPainted(true);
	    panelCenter.add(progressBar);
	}

    
	panelCenter.repaint();
	panelCenter.validate();

    }

}
