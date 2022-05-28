package com.threerings.getdown.launcher;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLEncoder;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;

public class AppAuthenticator extends Authenticator {

		String guestEmail = "guest@cytogenie.org";
		private String emailAddress;
		private final String originalEmailAddress, server;
		private String lastPassword;
		private String autoPassword;
		private String singleSignOnPassword;
		private char[] newPassword;
		private JCheckBox rememberCheckBox;
		private String topSecret;
		private JTextField emailField;
        private JFrame dummyFrame = new JFrame();
        private boolean okClicked = false;
        PasswordAuthentication value;

        private AppAuthenticator(final String email, final String webroot) {
           this.originalEmailAddress = email;
           this.emailAddress=email;
           this.server = webroot;
        }
        
        private AppAuthenticator(final String email, final String webroot,
                final String autoPassword) {
           this(email, webroot);
		   this.autoPassword = autoPassword;
        }

        private String getPasswordPropertyName() {
			return server + "." + emailAddress;
		}

        boolean cancelled = false;
		
        JPasswordField passwordField;
    @Override
        public PasswordAuthentication getPasswordAuthentication() {

        
        System.out.println ("getPasswordAuthentication() changed");
        
        if (autoPassword != null) {
    		newPassword = autoPassword.toCharArray();
			topSecret = autoPassword;
    		return new PasswordAuthentication(emailAddress, autoPassword.toCharArray());			
    	}

        if (singleSignOnPassword != null) {
    		newPassword = singleSignOnPassword.toCharArray();
			topSecret = singleSignOnPassword;
    		return new PasswordAuthentication(emailAddress, singleSignOnPassword.toCharArray());			
    	}

			boolean guestAccount = false;
			if(guestEmail.equals(originalEmailAddress)) {
				guestAccount = true;
			}
			final JDialog dlg = new JDialog(dummyFrame, " Login...", true);

            final JPanel main = new JPanel(new BorderLayout(2, 8));

            Color genieBlue = new Color (13, 57, 84);
			main.setBorder(BorderFactory.createMatteBorder (12,8,8,8,genieBlue));
            dlg.setAlwaysOnTop(true);
			dlg.getContentPane().add(main);
			final JPanel middle = new JPanel(new BorderLayout(4,4));

//			middle.add(new JLabel(applicationIcon), BorderLayout.WEST);
			final JPanel entries = new JPanel();
            
            GridLayout grid = new GridLayout (5,1);
       
            entries.setLayout (grid);
            
//            entries.setLayout (new BoxLayout (entries, BoxLayout.Y_AXIS));


            JPanel userlogin = new JPanel();
            JPanel pwdpanel = new JPanel();
            JPanel buttons = new JPanel();
            final Dimension dim1=new Dimension(340,30);
            userlogin.setPreferredSize(dim1);
            pwdpanel.setPreferredSize(dim1);
	
  			userlogin.setLayout(new BoxLayout(userlogin, BoxLayout.X_AXIS));
            JLabel userEmail = new JLabel("User email:");
            userEmail.setPreferredSize(new Dimension(90, 30));
            userlogin.add (userEmail);
            emailField = new JTextField(25);
			emailField.setText(originalEmailAddress);
			final Font f = emailField.getFont();
            Font font = new Font (f.getName(), Font.BOLD, f.getSize());
            System.out.println (f.getName() + "  " + f.getSize());
			emailField.setFont(font);
            userlogin.add (emailField);
            userEmail.setFont (font);

//            pwdpanel.setPreferredSize (dim3);
            pwdpanel.setLayout(new BoxLayout(pwdpanel, BoxLayout.X_AXIS));
            JLabel passwordLabel = new JLabel("Password:");
            passwordLabel.setPreferredSize(new Dimension(90, 30));
            pwdpanel.add(passwordLabel);
            passwordLabel.setFont (font);
            Dimension rigidArea = new Dimension (0, 8);
			buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
           
			   
			
			passwordField = new JPasswordField(25);
			if(guestAccount) {
				emailField.setMargin(new Insets(2,2,2,2));
				passwordField.setMargin(new Insets(2,2,2,2));
			}

			pwdpanel.add(passwordField);
            buttons.add(Box.createRigidArea(rigidArea));
            buttons.add(Box.createRigidArea(rigidArea));
            buttons.add(Box.createRigidArea(rigidArea));
            buttons.add(Box.createRigidArea(rigidArea));
            final Font smallFont=new Font(UIManager.getFont("Table.font").getName(), Font.PLAIN, 10);
			if (originalEmailAddress != null) {
				buttons.add(Box.createRigidArea(rigidArea));
			}
						
			final JButton ok = new JButton("Ok");
			ok.setText(" Ok ");
            
            System.out.println ("userlogin size = " + userlogin.getSize().toString());
            System.out.println ("pwd panel size = " + pwdpanel.getSize().toString());
            entries.add (Box.createRigidArea(rigidArea));
			entries.add(userlogin);
            entries.add(pwdpanel);
            JPanel rememberPass = new JPanel();
            rememberPass.setPreferredSize(dim1);
            rememberPass.setLayout(new BoxLayout(rememberPass, BoxLayout.X_AXIS));
            JLabel rememEmail = new JLabel("                      ");

            rememberPass.add (rememEmail);

            rememberCheckBox = new JCheckBox("Remember password!");
			rememberCheckBox.setMnemonic('r');
			rememberCheckBox.setFont(smallFont);
            //rememberPass.add (rememberCheckBox);
            entries.add (rememberPass);
            buttons.add (Box.createRigidArea (rigidArea) );
            buttons.add(rememberCheckBox);
            buttons.add (Box.createRigidArea (rigidArea));

            JPanel sidebyside = new JPanel ();
            sidebyside.setLayout (new BoxLayout (sidebyside, BoxLayout.X_AXIS));


            buttons.setAlignmentY (Component.TOP_ALIGNMENT);
            entries.setAlignmentY(Component.TOP_ALIGNMENT);
            sidebyside.add (entries);
            sidebyside.add(new JLabel("    "));
            sidebyside.add (buttons);
			final JLabel serverLabel = new JLabel("                   ");
            serverLabel.setBackground (Color.WHITE);

            JLabel title = new JLabel("  Login to your CytoGenie");
            title.setFont (new Font (f.getName(), Font.BOLD, f.getSize()+2));
            title.setHorizontalTextPosition(SwingConstants.CENTER);


            middle.add (title, BorderLayout.NORTH);
            middle.add (sidebyside, BorderLayout.CENTER);


            middle.add(serverLabel, BorderLayout.SOUTH);
            middle.add(new JPanel(), BorderLayout.EAST);
            middle.add(new JPanel(), BorderLayout.WEST);

			
			if(!guestAccount) {
				File file = new File(System.getProperty("user.home"), "cgpass.txt");
				if (file.exists()) {
					try {
						FileInputStream fos = new FileInputStream(file);
						byte[] b=new byte[1024];
						fos.read(b);
						fos.close();
						String upass = new String(b);
						String up[]=upass.split("=");
						emailField.setText(up[0]);
						passwordField.setText(up[1].trim());						
						rememberCheckBox.setSelected(true);
					}
					catch(Exception e) {
						e.printStackTrace();
					}
				}
				    				
    			
				if (lastPassword != null) {
					rememberCheckBox.setSelected(true);
					passwordField.setText(lastPassword);
				}
			}
            
			main.add(middle, BorderLayout.CENTER);
            JPanel geniePanel = new JPanel(new GridLayout(8,1));
            geniePanel.add (new JLabel ("         "));
            geniePanel.add (new JLabel ("         "));
            geniePanel.add (new JLabel ("         "));
            geniePanel.add (new JLabel ("         "));
            geniePanel.add (new JLabel ("         "));
            geniePanel.add (new JLabel ("         "));
            geniePanel.add (new JLabel ("         "));
            geniePanel.add (new JLabel ("         "));
            main.add(geniePanel, BorderLayout.WEST);

            JPanel geniePanel2 = new JPanel(new GridLayout(8,1));
            geniePanel2.add (new JLabel ("         "));
            geniePanel2.add (new JLabel ("         "));
            geniePanel2.add (new JLabel ("         "));
            geniePanel2.add (new JLabel ("         "));
            geniePanel2.add (new JLabel ("         "));
            geniePanel2.add (new JLabel ("         "));
            geniePanel2.add (new JLabel ("         "));
            geniePanel2.add (new JLabel ("         "));
            main.add(geniePanel2, BorderLayout.EAST);

			JPanel northPanel = new JPanel(new GridLayout(2,1));
			northPanel.add (new JLabel ("         "));
			northPanel.add (new JLabel ("         "));
			main.add(northPanel, BorderLayout.NORTH);

			
			final JPanel passwordMemory = new JPanel();
			if(!guestAccount) {
				final JButton changePassword = new JButton("Change password");
				changePassword.addActionListener(new ActionListener() {
					public void actionPerformed(final ActionEvent e) {
						changePassword();
					}
				});
				changePassword.setFont(smallFont);
				passwordMemory.add(changePassword);
			}
			final JPanel okCancel = new JPanel();
			JLabel emptyLabel2 = new JLabel("                ");
			emptyLabel2.setBounds(2,2,2,2);
			if(!guestAccount) {
				final JButton changePassword = new JButton("Forgot  password");
				changePassword.addActionListener(new ActionListener() {
					public void actionPerformed(final ActionEvent e) {
						changePassword();
					}
				});
			}
			else {
				okCancel.add(emptyLabel2);
			}
			final JButton cancel = new JButton("Cancel");
			cancel.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
                	if (!PROHIBIT_EXIT_ON_CANCEL){
                		cancelled = true;
                		System.exit(1);
                	}
				}
			});
			
			okCancel.add(ok);
            okCancel.add(cancel);

			final JPanel bottom = new JPanel(new BorderLayout(12, 1));
            bottom.setBorder (BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
			bottom.add(passwordMemory, BorderLayout.WEST);
			bottom.add(new JLabel("           "), BorderLayout.CENTER);
			bottom.add(okCancel, BorderLayout.EAST);

			main.add(bottom, BorderLayout.SOUTH);
			ok.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					okClicked = true;
					newPassword = passwordField.getPassword();
					emailAddress = emailField.getText();
					topSecret = String.copyValueOf(passwordField.getPassword());
			    	String requestURL  = server + "/CGBasicLogin?op=login&email="+emailAddress+"&password=" + new String(newPassword) + "&confirmLogin=yes&offline=true";
			    	try {    		
			    		URL url = new URL(requestURL);
			    		final HttpURLConnection ucon = (HttpURLConnection) url.openConnection();
			            ucon.setUseCaches(false);
			            ucon.setDoInput(true);
			            ucon.setRequestMethod("GET");
			            int len = ucon.getContentLength();
			           	InputStream is = ucon.getInputStream();
			           	BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF8"));
			            String result2 = br.readLine();
			            br.close();
			    		if (result2.equals("OK")) {
			    			File file = new File(System.getProperty("user.home"),  "cgpass.txt");
			    			if (rememberCheckBox.isSelected()) {
			    				FileOutputStream fos = new FileOutputStream(file);
			    				String upass= new String(emailAddress) + "=" + new String(newPassword);			    				
			    				fos.write(upass.getBytes());
			    				fos.close();    				
			    			}
			    			else {
			    				if (file.exists()) {
			    					file.delete();
			    				}
			    			}
			    			dlg.dispose();
			    			return;
			    		}
			    	}
			    	catch(Exception e1) {
			    		e1.printStackTrace();
			    	}
			    	JOptionPane.showMessageDialog(dlg, "Invalid credentials");
				}
			});
			
			
			dlg.getRootPane().setDefaultButton(ok);
			dlg.pack();
			/*if (originalEmailAddress != null) {
				passwordField.requestFocus();	
			}
			else {*/
				emailField.requestFocus();
			//}
			
            dlg.setResizable (false);
			dlg.setVisible(true);

			if (!okClicked) {
				value = null;
			} else {
				singleSignOnPassword = newPassword.toString();
				value = new PasswordAuthentication(emailAddress, newPassword);
			}
			return value;
		}

		protected void changePassword() {
			final JDialog dlg = new JDialog(dummyFrame, "Change Password...", true);
			dlg.setAlwaysOnTop(true);
	        final JTextField emailField = new JTextField(25);
	        final Font font = emailField.getFont();
	        emailField.setFont(new Font(font.getName(), Font.BOLD, font.getSize()));
	        //emailField.setEditable(false);
	        final JPasswordField oldPasswordField = new JPasswordField(20);
	        final JPasswordField newPasswordField = new JPasswordField(20);
	        final JPasswordField confirmNewPasswordField = new JPasswordField(20);
	          
	        final JLabel emailLabel = new JLabel("User email : ");
	        final JLabel oldPasswordLabel = new JLabel("Old Password : ");
	        final JLabel newPasswordLabel = new JLabel("New Password : ");
	        final JLabel confirmPasswordLabel = new JLabel("Confirm New Password : ");
	                 
	        emailLabel.setLabelFor(emailField);
	        oldPasswordLabel.setLabelFor(oldPasswordField);
	        newPasswordLabel.setLabelFor(newPasswordLabel);
	        confirmPasswordLabel.setLabelFor(confirmNewPasswordField);
	                 
	        JPanel textControlsPane = new JPanel();
	        GridBagLayout gridbag = new GridBagLayout();
	        GridBagConstraints c = new GridBagConstraints();
            textControlsPane.setLayout(gridbag);
            textControlsPane.setBorder(
            		BorderFactory.createCompoundBorder(
            				BorderFactory.createTitledBorder(""),
            				BorderFactory.createEmptyBorder(5,5,5,5)));
	                
	                 
	        JLabel labels[] = {emailLabel,oldPasswordLabel,newPasswordLabel,confirmPasswordLabel};
	        JTextField textFields[] = {emailField,oldPasswordField,newPasswordField,confirmNewPasswordField};
	                 
	        addLabelTextRows(labels, textFields, gridbag, textControlsPane);
	        ActionListener submitAction = new ActionListener(){

	 		public void actionPerformed(ActionEvent event) {
	 			URL url;
	 			final String codebase="http://cgworkspace.cytogenie.org/";
	 					
	            char []oldpassword=  oldPasswordField.getPassword();
	            final String oldPass = new String (oldpassword).trim();
	            char [] newpassword = newPasswordField.getPassword();
	 			final String newPass = new String (newpassword).trim();
                char[] confirmnew = confirmNewPasswordField.getPassword();
                final String confirmNewPass = new String(confirmnew).trim();
				try {
					if(emailField.getText() == null  ||emailField.getText().equals("")) {
						JOptionPane.showMessageDialog(dlg, "Email address cannot be empty");
						dlg.toFront();
					}
					else if(oldPass == null  ||newPass == null || confirmNewPass == null ) {
						JOptionPane.showMessageDialog(dlg, "Password cannot be empty");
						dlg.toFront();
					}
					else if(oldPass.equals("") ||newPass.equals("") || confirmNewPass.equals("")) {
						JOptionPane.showMessageDialog(dlg, "Password cannot be empty");
						dlg.toFront();
					}
					else if(!(newPass.equals(confirmNewPass))) {
						JOptionPane.showMessageDialog(dlg, "Passwords do not match");
						dlg.toFront();
					}
					else if(newPass.equals(oldPass)) {
						JOptionPane.showMessageDialog(dlg, "Old and new password cannot be same");
						dlg.toFront();
					}
					else {
						//String requestURL  = webRoot + "/CGBasicLogin?op=login&email="+appAuth.emailAddress+"&password=" + new String(appAuth.newPassword) + "&confirmLogin=yes&offline=true";
						url = new URL(
								codebase+"/cgaccs?op=savePasswd&email="
								+emailField.getText()+"&oldPassword="+URLEncoder.encode(oldPass,"UTF-8")+
								"&newPassword="+URLEncoder.encode(newPass,"UTF-8"));
						
		                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		                conn.setRequestMethod("GET");
						conn.setDoOutput(true);
						conn.setDoInput(true);
						InputStream is = conn.getInputStream();
						BufferedReader br =
			                  new BufferedReader(
			                    new InputStreamReader(is));
						
			                String line;
			                boolean clean = true;
			                while ((line = br.readLine()) != null) {
			                	//if(line.contains("mis-match")) {
			                	if(line.contains("mis-match")) {
			                		clean = false;
			                		JOptionPane.showMessageDialog(dlg, "Old password is not correct");
			                		dlg.toFront();
			                		break;
			                	}
			                }
			                if (clean) {
			                	JOptionPane.showMessageDialog(dlg, "Your password has been changed successfully");
		                		dlg.dispose();		                		
			                }
			                
						} 
					}catch (MalformedURLException e1) {
						e1.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					
				}
             	  
               };

             final JButton save = new JButton("Submit");
             save.addActionListener(submitAction);
             dlg.getRootPane().setDefaultButton(save);

             final JButton noButton = new JButton("Cancel");
             noButton.addActionListener(new ActionListener() {
                 public void actionPerformed(final ActionEvent e) {
                     dlg.dispose();
                   }});
             
             dlg.getRootPane().registerKeyboardAction(
                     new ActionListener() {
                       public void actionPerformed(final ActionEvent e) {
                           noButton.doClick(132);
                       }
                   }

                   ,

                   KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                   JComponent.WHEN_IN_FOCUSED_WINDOW);
             
             
             JPanel buttonPanel = new JPanel();
             buttonPanel.add(save);
             buttonPanel.add(noButton);
             
             buttonPanel.setBorder(
                     BorderFactory.createEtchedBorder()/*(5,5,5,5))*/);
             
             
             dlg.add(textControlsPane,"North");
             dlg.add(buttonPanel,"South");
             dlg.pack();
             dlg.setResizable(true);
             dlg.setLocationRelativeTo(null);
             dlg.setVisible(true); 
        }
	        
		
		public static void addLabelTextRows(JLabel[] labels, JTextField[] textFields,
		      GridBagLayout gridbag, Container container) {
		      GridBagConstraints c = new GridBagConstraints();
		      c.anchor = GridBagConstraints.EAST;
		      int numLabels = labels.length;

		      for (int i = 0; i < numLabels; i++) {
		          c.gridwidth = GridBagConstraints.RELATIVE; // next-to-last
		          c.fill = GridBagConstraints.NONE; // reset to default
		          c.weightx = 0.0; // reset to default
		          container.add(labels[i], c);

		          c.gridwidth = GridBagConstraints.REMAINDER; // end row
		          c.fill = GridBagConstraints.HORIZONTAL;
		          c.weightx = 1.0;
		          container.add(textFields[i], c);
		      }
		  } 
	


		public void savePasswordIfRequired() {
			if (rememberCheckBox != null) {
				if (rememberCheckBox.isSelected()) {
				} else {
				}
			}
       }
		
	public static void main(String a[]) {
		authenticate("lstempo@emory.edu");
	}
	public static boolean authenticate() {
		return authenticate("");
	}
	public static boolean authenticate(final String emailAddress) {
		String webRoot = "http://cgworkspace.cytogenie.org";
		AppAuthenticator appAuth = new AppAuthenticator(emailAddress, webRoot);
		Authenticator.setDefault(appAuth);
		appAuth.getPasswordAuthentication();
		if (appAuth.cancelled) {
			return false;
		}    	
		return true;    	
	}
	
    public static void authenticate_old(final String emailAddress) {
    	String webRoot = "http://cgworkspace.cytogenie.org";
    	AppAuthenticator appAuth = new AppAuthenticator(emailAddress, "http://cgworkspace.cytogenie.org");
		Authenticator.setDefault(appAuth);
    	appAuth.getPasswordAuthentication();
		String result = "";
		try {
			 URL url = new URL("http://cgworkspace.cytogenie.org//verifyUser.jsp?email=" + emailAddress);
			 final HttpURLConnection ucon = (HttpURLConnection) url.openConnection();
             ucon.setUseCaches(false);
             ucon.setDoInput(true);
             ucon.setRequestMethod("GET");
             int len = ucon.getContentLength();
             try {
            	 InputStream is = ucon.getInputStream();
            	 BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF8"));
            			 final StringWriter sw = new StringWriter();
                 final PrintWriter pw = new PrintWriter(sw);
                 try {
                     String line;
                     while ((line = br.readLine()) != null) {
                         pw.println(line);
                     }
                 } finally {
                    br.close();
                 }
             } 
             catch(Exception e) {
            	 e.printStackTrace();
             }
             finally {
    	 	   final BufferedReader br = new BufferedReader(new InputStreamReader(ucon.getErrorStream()));
               String feedBackFromServer = null;
               while ((feedBackFromServer = br.readLine()) != null) {
            	   String s = feedBackFromServer;
            	   String HIDDEN_ACESSS_TYPE =
            			      "<input type=\"hidden\" name=\"accessType\" value=\"";
            	   int idx = s.indexOf(HIDDEN_ACESSS_TYPE);
                   if (idx >= 0) {
                       int idx2 = s.lastIndexOf("\">");
                       if (idx2 >= 0) {
                           if (s.substring(idx + HIDDEN_ACESSS_TYPE.length(), idx2) != null) {
                        	   break;
                           }
                       }
                   }
               }
             }
			
		} catch (IOException e) { // code to catch 401 exception
			e.printStackTrace();
			if (e.getMessage().contains("HTTP response code: 401")
					|| e.getMessage().contains(
							"Server redirected too many")) {
				result = "FAILED";
			} else { // assuming it's a connectivity problem, allow
				// to continue
				result = "OFFLINE";
			}
		}

		if (result.equals("OK") || result.equals("OFFLINE")) {
			System.out.println("Authentication OK");
		} 

	

    }

    	public static boolean PROHIBIT_EXIT_ON_CANCEL=false;
	}





