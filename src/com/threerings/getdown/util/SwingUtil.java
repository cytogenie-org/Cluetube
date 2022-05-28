package com.threerings.getdown.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import java.awt.Window;

public class SwingUtil {

	private static ImageIcon getImageGifIcon(String fileName) {
		ImageIcon icon = null;
		URL url = SwingUtil.class.getResource("images/" + fileName +".gif");
		if (url != null) {
			icon = new ImageIcon(url);
		}
		return icon;
	}
	
	private static JPanel getMoreLessPanel(
			  final JDialog dlg,
			  final String briefMsg,
			  final String detailedMsg,
	          final int optionPaneMessageType){
		    final JPanel msgPanel = new JPanel();
		    final JLabel msgLabel = new JLabel(briefMsg);
		    final JPanel iconPanel = new JPanel(new GridLayout(1,1));
		    final JLabel iconLabel = new JLabel(getImageGifIcon("facs"));
		    iconPanel.add(iconLabel);
		    msgPanel.add(iconPanel);
		    msgPanel.add(msgLabel);
		    
		    msgPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
			final JButton details = new JButton("<html><small><u>More</u></small></html>");
			details.setIcon(getImageGifIcon("great"));
			details.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ImageIcon icon = null;					
					if (msgLabel.getText().equals(briefMsg)) {
						msgLabel.setText(detailedMsg);
						details.setText("<html><small><u>Less</u></small></html>");						
						details.setIcon(getImageGifIcon("less"));
					} else {
						msgLabel.setText(briefMsg);
						details.setText("<html><small><u>More</u></small></html>");
						details.setIcon(getImageGifIcon("great"));
					}
					stylizeAsHyperLink(details);
					dlg.pack();
				}
			});
			msgPanel.add(new JLabel("   "));
			msgPanel.add(details);
		    stylizeAsHyperLink(details);
		    dlg.pack();
			return msgPanel;
	  
	}
	
	public static void alertWithMoreOrLess(final Window owner, final String less, final String more, final String title, final boolean isError) {
		JDialog dlg = new JDialog(owner);
		show(owner, false, dlg, getMoreLessPanel(dlg, less, more, isError ? JOptionPane.ERROR_MESSAGE :
            JOptionPane.INFORMATION_MESSAGE), title);
	}
			
	  public static void show(final Window owner, final boolean needScrollBar, final JDialog dlg,
				final JPanel topPanel, final String title) {
		  	//ToolTipOnDemand.hideManagerWindow();
			final JPanel cp = new JPanel();
			dlg.getContentPane().add(cp);
			cp.setLayout(new BorderLayout());
			
			final JPanel middlePanel = new JPanel();
			JButton btnContinue = new JButton("Ok");
			btnContinue.setMnemonic('o');
			btnContinue.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					dlg.dispose();
				}
			});
			middlePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
			middlePanel.add(btnContinue);			
			middlePanel
					.setBorder(BorderFactory.createEmptyBorder(0, 4, 10, 10));
			if (!needScrollBar) {
				cp.add(topPanel, BorderLayout.NORTH);
				cp.add(middlePanel, BorderLayout.CENTER);
			} else {
				if (needScrollBar) {
					cp.add(new JScrollPane(topPanel), BorderLayout.CENTER);
					dlg.getContentPane().setPreferredSize(new Dimension(500, 600));
				} else {
					cp.add(topPanel, BorderLayout.NORTH);

				}
				cp.add(middlePanel, BorderLayout.SOUTH);
			}
			dlg.setTitle(title);
			dlg.getRootPane().setDefaultButton(btnContinue);
			dlg.setModal(true);
			dlg.pack();			
			dlg.getRootPane().setDefaultButton(btnContinue);
			center(owner, dlg);
			dlg.setVisible(true);
		}
	  
	  public static void center(final Window parent, final Window w){
		  final Dimension d=w.getSize();
		  Dimension parentSize =null;
		  if (parent==null){
			  Toolkit tk = Toolkit.getDefaultToolkit();
			  parentSize = tk.getScreenSize();
		  }else{
			  parentSize=parent.getSize();
		  }
		  int y = (parentSize.height/2)-(d.height/2);
		  int x = (parentSize.width/2)-(d.width/2);
		  if (parent !=null){
			  final Point p=parent.getLocation();
			  //System.out.println("parent size="+parentSize+", parent xy="+p);
			  //System.out.println("e1 x="+x+", y="+y);
			  y+=p.y;
			  x+=p.x;
//			  System.out.println("e2 x="+x+", y="+y);
		  }
		  if (x<0){
			  x=0;
		  }
		  if (y<0){
			  y=0;
		  }
		  //System.out.println("e3 x="+x+", y="+y);
		  w.setLocation(x, y);
	  }
	  
	  private static void stylizeAsHyperLink(final AbstractButton b){
	    	final String text=b.getText();
	    	setToolBarStyle(b);
	    	b.setText("<html><u>" + text + "</u></html>");
	    	b.setForeground(Color.blue);
	    }
	  
	  private static AbstractButton setToolBarStyle(final AbstractButton b) {
	    	b.setMargin(new Insets(0,0,0,0));
	    	b.setIconTextGap(0);
	    	b.setBorderPainted(false);
	    	b.setBorder(null);
	    	b.setText(null);
	    	b.setOpaque(false);
	        b.setBackground(UIManager.getColor("Panel.background"));
	        b.setFocusPainted(false);
	        return b;
	    }

}
