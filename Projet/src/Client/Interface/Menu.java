package GraphicInterface;

import CookieClient.Actions;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import javax.swing.ImageIcon;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.File;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledFuture;
import java.util.InputMismatchException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import java.util.Iterator;

@SuppressWarnings("serial")
/**
 * <b>The menu.</b>
 */
public class Menu extends JMenuBar {
    
    /**
     * An action instance
     */
    final private Actions A;

    /**
     * The main CookieTorrent window
     */
    final private JFrame frame;

    /**
     * <b>The Menu constructor</b>
     * @param A
     *        The action instance
     * @param frame
     *        The main CookieTorrent window
     */
    public Menu(final Actions A, final JFrame frame) {
	
	this.A = A;
	this.frame = frame;
	// Listener
	ActionListener afficherMenuListener = new ActionListener() {
		public void actionPerformed(ActionEvent event) {
		    Object source = event.getActionCommand();
		    ImageIcon imgQuestion = new ImageIcon("Interface/images/question.png");
		    if (source.equals("Par nom de fichier")) {
			String response = (String)JOptionPane.showInputDialog(frame, null, "Entrez un nom de fichier", JOptionPane.QUESTION_MESSAGE, imgQuestion, null, null);
			if (response != null) {
			    if (A.look(response, 1))
				A.getFile();
			}		
		    } 
		    else if (source.equals("Par taille de fichier")) {
			String response = (String)JOptionPane.showInputDialog(frame, null, "Entrez un critère (ex: '> 0'):", JOptionPane.QUESTION_MESSAGE, imgQuestion, null, null);
			if (response != null) {
			    if (A.look(response, 2))
				A.getFile();
			}
		    }
		    else if (source.equals("Ajouter")) {
			String response = (String)JOptionPane.showInputDialog(frame, null,"Nom du fichier à ajouter", JOptionPane.QUESTION_MESSAGE, imgQuestion, null, null);
			if (response != null) {
			    A.addSharedFile(response);
			}
		    }
		    else if (source.equals("Adresse du tracker")) {
			String response = JOptionPane.showInputDialog(frame, "", "Adresse du tracker", JOptionPane.PLAIN_MESSAGE);
			if (response != null) {
			    A.changeAdress(response);
			}
		    }
		    else if (source.equals("Port du tracker")) {
			String response = JOptionPane.showInputDialog(frame, "", "Port d'écoute du tracker", JOptionPane.PLAIN_MESSAGE);
			if (response != null) {
			    A.changePort(response);
			}
		    }
		    else if (source.equals("Cookie")) {
			ImageIcon img = new ImageIcon("Interface/images/cookie.gif");
			JOptionPane.showMessageDialog(frame, "", "Ceci est un cookie !", JOptionPane.INFORMATION_MESSAGE, img);
		    }
		    else if (source.equals("Quitter")) {
			System.exit(1);
		    }
		}
	    };
	
	// Création du menu Fichier
	JMenu fichierMenu = new JMenu("Fichier");
	ImageIcon icon = new ImageIcon("Interface/images/rechercher.png");
	JMenuItem sousMenu = new JMenu("Rechercher");
		
	
	JMenuItem item = new JMenuItem("Par nom de fichier", icon);
	sousMenu.add(item);
	item.addActionListener(afficherMenuListener);

	KeyStroke cntrlSKey = KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK);
	item.setAccelerator(cntrlSKey);

	item = new JMenuItem("Par taille de fichier", icon);
	sousMenu.add(item);
	item.addActionListener(afficherMenuListener);	
	fichierMenu.add(sousMenu);
	
	icon = new ImageIcon("Interface/images/ajouter.png");
	item = new JMenuItem("Ajouter", icon);
	item.addActionListener(afficherMenuListener);
	fichierMenu.insertSeparator(1);
	fichierMenu.add(item);

	KeyStroke cntrlAKey = KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK);
	item.setAccelerator(cntrlAKey);
	
	icon = new ImageIcon("Interface/images/quitter.png");
	item = new JMenuItem("Quitter", icon);
	item.addActionListener(afficherMenuListener);
	fichierMenu.add(item);

	KeyStroke cntrlCKey = KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK);
	item.setAccelerator(cntrlCKey);
	
	// Création du menu Preferences
	JMenu preferencesMenu = new JMenu("Préférences");
	JMenu sousMenuConfig = new JMenu("Configurer");
	icon = new ImageIcon("Interface/images/configuration.png");

	item = new JMenuItem("Adresse du tracker", icon);
	sousMenuConfig.add(item);
	item.addActionListener(afficherMenuListener);
	item = new JMenuItem("Port du tracker", icon);
	sousMenuConfig.add(item);
	item.addActionListener(afficherMenuListener);	
	preferencesMenu.add(sousMenuConfig);

	
	// Création menu bonus !
	JMenu cookiesMenu = new JMenu("A Propos");
	icon = new ImageIcon("Interface/images/cookie.png");
	item = new JMenuItem("Cookie", icon);
	item.addActionListener(afficherMenuListener);
	cookiesMenu.insertSeparator(1);
	cookiesMenu.add(item);

	KeyStroke cntrlBKey = KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK);
	item.setAccelerator(cntrlBKey);

	// ajout des menus à la barre de menus
	add(fichierMenu);
	add(preferencesMenu);
	add(cookiesMenu);
    }
}
