package com.help;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Properties;
import java.util.Random;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.plaf.UIResource;

import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.DefaultLoadHandler;
import com.teamdev.jxbrowser.chromium.LoadParams;
import com.teamdev.jxbrowser.chromium.swing.BrowserView;

public class ClueTube {
	
    private java.util.Timer timerContextTracker = null;
    private static File HELP_HOME = new File(System.getProperty("user.home"), ".autoGate");
    private static String SERVER = "AutoGateTest";
    private static String HELP_PROPS_HOME_WIN_TEST = System.getProperty("user.home") + File.separator + "AppData" + File.separator + 
    		"Local" + File.separator + "AutoGateTest" + File.separator + "domains" + File.separator + "FACS";
    private static String HELP_PROPS_HOME_WIN_PROD = System.getProperty("user.home") + File.separator + "AppData" + File.separator + 
    		"Local" + File.separator + "domains" + File.separator + "FACS";
    private static String HELP_PROPS_HOME_MAC_TEST = "/Applications/AutoGateTest.app/Contents/Resources/Java/domains/FACS";
    private static String HELP_PROPS_HOME_MAC_PROD = "/Applications/AutoGate.app/Contents/Resources/domains/FACS/";
    private static String HELP_LINK = "helpLinks.properties";
    private static File CLUETUBE_HOME = new File(System.getProperty("user.home"), ".cluetube");
    private final static String HELP_CONTEXT = "currentTopic.txt";
   // private final static String HELP_FILEURL = System.getProperty("user.home") + File.separator + ".autoGate" + File.separator + "HelpContents.html";
    final static String HELP_FILEURL = "file://Users/bhavanichandramohan/Documents/Cluetube_IN_CVS/res/HelpContents.html";
    private Browser browser = null;
    private long helpContextLastModified = 0;
    private Properties helpProperties, helpLinks;
    private JFrame frame = null;
    private boolean exitOnRequest = false;
    private static Rectangle mainRec;// = new Rectangle(1920, 1080);
	private static final int RESOLUTION_WIDTH_2800 = 2800;
	private static final int RESOLUTION_WIDTH_2000 = 2000;
	private static final int RESOLUTION_WIDTH_1440 = 1440;
	private static float WIDTH_PERCENT = .36f;
	private static float HEIGHT_PERCENT = .29f;

	private JPanel addWikiPanel() {
		final JTextField addressURL = new JTextField(20);
		JButton go = new JButton("Go");
		JLabel addressBar = new JLabel("URL: ");
        Action goAction = new AbstractAction() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				browser.loadURL(addressURL.getText());
				
			}
		};
        go.addActionListener(goAction);
        JButton set = new JButton("Set");
        set.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		long fileName = new Random().nextLong();
        		takeScreenShot(mainRec.x, mainRec.y,  mainRec.width,  mainRec.width, CLUETUBE_HOME + File.separator + fileName + ".png");
        		linkHelpURL(String.valueOf(fileName), addressURL.getText() + "," + browser.getURL());
        		JOptionPane.showMessageDialog(frame, "Help link set!");
        	}
        });
        addressURL.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "submit"); 
        addressURL.getActionMap().put("submit", goAction); 
        JPanel northPanel = new JPanel(new FlowLayout());
        northPanel.add(addressBar);
        northPanel.add(addressURL);
        northPanel.add(go);
        northPanel.add(set);
        return northPanel;
	}
	
    private ClueTube(File home) {
		this.browser = new Browser();
		BrowserView browserView = new BrowserView(browser);
		browser.setLoadHandler(new DefaultLoadHandler() {
		    @Override
		    public boolean onLoad(LoadParams params) {
		        String url = params.getURL();
		        System.out.println("Openeing2: "+ url);
		        if (url.startsWith("http://stage4.cytogenie.org")) {
		            try {
		            	System.out.println("Openeing: "+ url);
		                Desktop.getDesktop().browse(URI.create(url));
		            } catch (IOException e) {
		                e.printStackTrace();
		            }
		            return true;
		        }
		        return super.onLoad(params);
		    }
		});
		this.frame = new JFrame("ClueTube");
		this.timerContextTracker = new java.util.Timer();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        //frame.add(addWikiPanel(), BorderLayout.NORTH);
       // browser.setSize(getRelativeDimension());
       // browserView.setSize(getRelativeDimension());
        frame.add(browserView, BorderLayout.CENTER);
        frame.setSize(getRelativeDimension());
      //  browser.setZoomLevel(getRelativeBrowserZoomLevel());
        setUIFont(new javax.swing.plaf.FontUIResource("Arial",Font.BOLD, getRelativeFontSize()));
        //frame.setLocationRelativeTo(null);
        topRight(frame);
        frame.setVisible(true);
        frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				exitOnRequest = true;
				unsetClueTubeRunning();
				if (frame != null) {
					frame.dispose();
				}
				if (clueTubeEventListener != null) {
					clueTubeEventListener.fireEvent(EVENT.EXIT);
				}
				System.exit(0);
				//setHelpText(!exitOnRequest?"LAUNCH":"HOME");
			}
		});
       /* if (isReachable("s6.serverscience.com", 22, 5500)) {
        	browser.loadURL("http://stage1.cytogenie.org/0aa.html");
        	// browser.loadHTML("<html><body><center><h2><br>AutoGate Help</h2><h3>Loading, Please wait</h3></center></body></html>");
            //browser.loadURL(HELP_BASEURL);
        }
        else {
        	browser.loadHTML(MSG_OFFLINE);
        }*/
        if (new File(HELP_FILEURL).exists()) {
        	//browser.loadURL("http://stage1.cytogenie.org/0aa.html");
        	//browser.loadHTML("<html><body><center><h2><br>AutoGate Help</h2><h3>Loading, Please wait</h3></center></body></html>");
            browser.loadURL(HELP_FILEURL);
        }
        else {
        	browser.loadHTML(MSG_OFFLINE);
        }
	}
    
    final String MSG_OFFLINE = "<html><body><h3>You appear to be offline or are failing to access our help server. We’re looking to correct any possible problems on our side. "
			+ "If the problem persists when you are online, please contact us at support@cytogenie.org</h3></body></html>";
    
    boolean isReachable(final String host, final int port,  int timeout) {
    	Socket socket = new Socket();
  		boolean online = false;
  		if (timeout==0){
  			timeout=9000;
  		}
  		try {
  			final SocketAddress sockaddr = new InetSocketAddress(host, port);  	  		
  			socket.connect(sockaddr, timeout);
  			online = true;
  		} catch (Exception ex) {
  			ex.printStackTrace();
  			online = false;
  		} finally {
  			try {
  				socket.close();
  			} catch (IOException ex) {
  				ex.printStackTrace();
  			}

  		}
  		return online;
    }
    
	private static void createFileIfNotExists(File file, String text) {
		if (file.exists() && file.isDirectory()) {
			file.delete();
		}
		if (!file.exists()) {
			System.out.println("Properties do not exist");
			try {
				file.createNewFile();
				BufferedWriter bw = null;
				try {
					bw = new BufferedWriter(new FileWriter(file));
					bw.write(text);
					bw.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				} finally {
					if (bw != null) {
			    		try {
			    			bw.close();	    			
			    		}
			    		catch(Exception e2) {}
			    	}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void linkHelpURL(String key, String value) {
		File helpLink = new File(CLUETUBE_HOME, HELP_LINK);
		
		BufferedWriter bw = null;
		try {
			if (!helpLink.exists()) {
				helpLink.createNewFile();
			}
			FileWriter writer = new FileWriter(helpLink, true);
			bw = new BufferedWriter(writer);
			bw.write(key +"="+value+"\n");
			bw.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			if (bw != null) {
	    		try {
	    			bw.close();	    			
	    		}
	    		catch(Exception e2) {}
	    	}
		}
	}
	
	public static String getHelpText() {
		File current = new File(HELP_HOME, HELP_CONTEXT);
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(current));
			return br.readLine();
		}catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			if (br != null) {
	    		try {
	    			br.close();   			
	    		}
	    		catch(Exception e2) {}
	    	}
		}
		return "";
		
	}
	public static void setHelpText(String text) {
		File current = new File(HELP_HOME, HELP_CONTEXT);
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(current);
			java.nio.channels.FileLock lock = out.getChannel().lock();
			try {
				out.write(text.getBytes());
			}
			finally {
				if (lock != null) {
					lock.release();
				}	
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			if (out != null) {
	    		try {
	    			out.close();   			
	    		}
	    		catch(Exception e2) {}
	    	}
		}
	}
	
	/*private static String getHelpPropertiesPath() {
		boolean isMac = System.getProperty("os.name").indexOf("Mac OS") >= 0?true:false;
		String helpProps = "help.properties";
		if (SERVER.equals("AutoGateTest")) {
			if (isMac) {
				helpProps = HELP_PROPS_HOME_MAC_TEST;
			}
			else {
				helpProps = HELP_PROPS_HOME_WIN_TEST;
			}
			
		}
		else {
			if (isMac) {
				helpProps = HELP_PROPS_HOME_MAC_PROD;
			}
			else {
				helpProps = HELP_PROPS_HOME_WIN_PROD;
			}
		}
		System.out.println("Help properties location: " + helpProps);
		return helpProps;
		
	}*/
	
	public static boolean isClueTubeRunning() {
		File ctRunning = new File(RUNS_FLAG);
		if (ctRunning.exists()) {
			return true;
		}
		return false;
	}
	
	public static void setClueTubeRunning() {
		File ctRunning = new File(RUNS_FLAG);
		if (!ctRunning.exists()) {
			try {
				ctRunning.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	static String RUNS_FLAG = System.getProperty("user.home") + File.separator + "ctRunsFlag";
	public static void unsetClueTubeRunning() {
		File ctRunning = new File(RUNS_FLAG);
		if (ctRunning.exists()) {
			ctRunning.delete();
		}
	}
	
	public static void checkAndstartCluetube() {
		if (!isClueTubeRunning()) {
			startCluetube();
			setClueTubeRunning();
		}
	}
	
	public static void startCluetube() {
		String path = getHelpResourceParentPath("cluetube.jar", SERVER);
		boolean isMac = System.getProperty("os.name").indexOf("Mac OS") >= 0?true:false;
		try {
			if (isMac) {
				String[] args = new String[2];
        		args[0] = "open";
        		args[1] = "java -jar "+ path;
				Runtime.getRuntime().exec(args[1]);
			}
			else {
				String cmd = "java -jar \"" + path +"\"";
				Runtime.getRuntime().exec(new String[]{"cmd", "/c", cmd});
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String a(String resource, String server) {
		boolean isMac = System.getProperty("os.name").indexOf("Mac OS") >= 0?true:false;
		String resourceParent = "help.properties";
		if (SERVER.equals("AutoGateTest")) {
			if (isMac) {
				resourceParent = HELP_PROPS_HOME_MAC_TEST;
			}
			else {
				resourceParent = HELP_PROPS_HOME_WIN_TEST;
			}
			
		}
		else {
			if (isMac) {
				resourceParent = HELP_PROPS_HOME_MAC_PROD;
			}
			else {
				resourceParent = HELP_PROPS_HOME_WIN_PROD;
			}
		}
		resourceParent = resourceParent + File.separator + resource;
		System.out.println("Help resource parent location: " + resourceParent);
		return resourceParent;
	}
	
	public static String getHelpResourceParentPath(String resource, String server) {
		return "/Users/bhavanichandramohan/Documents/Cluetube_IN_CVS/res/" +resource;
	}
	
	public static ClueTube Get() {
		HELP_HOME.mkdirs();
		CLUETUBE_HOME.mkdirs();
		File propsFile = new File(getHelpResourceParentPath("helpText.properties", SERVER));
		createFileIfNotExists(propsFile, "HOME=http://stage1.cytogenie.org/0aa.html");//
		//createFileIfNotExists(propsFile, "HOME=http://stage1.cytogenie.org/CT2aa.html");//http://cytogenie.org
		createFileIfNotExists(new File(HELP_HOME, HELP_CONTEXT), "LAUNCH");
		return new ClueTube(propsFile.getParentFile());
	}
	
	public static void setHelp(String text) {
		setHelpText(text);
	}
	
	enum EVENT {
		HOME, EXIT
	}
	interface ClueTubeEventListener {
		public void fireEvent(EVENT event);
	}
	ClueTubeEventListener clueTubeEventListener;
	public void setClueTubeEventListener(ClueTubeEventListener ctel) {
		this.clueTubeEventListener = ctel;
	}
	
	private void startTimer() {
		System.out.println("Starting cluetube listener");
		final String header=helpProperties.getProperty("header");
		final String footer=helpProperties.getProperty("footer");
		final String base=helpLinks.getProperty("base");
		timerContextTracker.schedule(new TimerTask() {
			public void run() {
				if (exitOnRequest) {
					timerContextTracker.cancel();
					unsetClueTubeRunning();
					if (frame != null) {
						frame.dispose();
					}
					if (clueTubeEventListener != null) {
						clueTubeEventListener.fireEvent(EVENT.EXIT);
					}
					System.exit(0);
				}
				BufferedReader br = null;
				try {
					File current = new File(HELP_HOME, HELP_CONTEXT);
					long lmnow = current.lastModified();
					if (lmnow != helpContextLastModified) {
						helpContextLastModified = lmnow;
						br = new BufferedReader(new FileReader(current));
						String helpKey = br.readLine();
						System.out.println("new helpkey: " + helpKey);
						boolean toFront = false;
						/*if (!isReachable("s6.serverscience.com", 22, 5500)) {
							return;
						}*/
						if (helpKey.trim().equalsIgnoreCase("EXIT")) {
							browser.loadURL("http://www.google.com"); 
							timerContextTracker.cancel();
							unsetClueTubeRunning();
							if (frame != null) {
								frame.dispose();
							}
							if (clueTubeEventListener != null) {
								clueTubeEventListener.fireEvent(EVENT.EXIT);
							}
							System.exit(0);
						}
						else if (helpKey.trim().equalsIgnoreCase("HOME")) {
							//browser.loadURL(HELP_BASEURL); 
							browser.loadURL("http://stage1.cytogenie.org/0aa.html"); 
							toFront = true;
						}
						else if (helpKey.trim().equalsIgnoreCase("SHOW")) {
							toFront = true;
						}
						else  {
							String helpUrl = helpProperties.getProperty(helpKey);
							System.out.println("Loading URL: " + helpUrl);
							System.out.println("Loading " + HELP_FILEURL + "#" + helpUrl);
							//System.out.println("http://stage1.cytogenie.org/" + helpUrl);
							//System.out.println("----------------------");
							if (helpUrl != null) {
								toFront = true;
								//browser.loadURL(HELP_BASEURL + helpUrl);
								//String prefixIt = helpUrl.startsWith("#")?"":"#";
								//prefixIt +=  helpUrl;
								String linkUrl = helpLinks.getProperty(helpKey);
								System.out.println("base:" + base);
								System.out.println("Index of: " + footer.indexOf("MORELINK"));
								String bottom="";
								if (linkUrl != null) {
									bottom=footer.replace("MORELINK",new StringBuilder(base).append(linkUrl).toString());
								}
								browser.loadHTML(new StringBuilder(header).append(helpUrl).append(bottom).toString());
								//browser.loadURL("file://"+HELP_FILEURL +  prefixIt); 
								//browser.loadURL("http://stage1.cytogenie.org/" + helpUrl);
								//addressURL.setText("http://stage1.cytogenie.org/" + helpUrl);
							}
							else {
								browser.loadURL("http://stage1.cytogenie.org/0aa.html"); 
							}
							
						}
						if (toFront) {
							frame.setAlwaysOnTop(true);
							frame.toFront();
							SwingUtilities.invokeLater(new Runnable(){
	    						public void run() {
	    							SwingUtilities.invokeLater(new Runnable(){
	    	    						public void run() {
	    	    							SwingUtilities.invokeLater(new Runnable(){
	    	    	    						public void run() {
	    	    	    							frame.setAlwaysOnTop(false);
	    	    	    				    }});	
	    	    				    }});	
	    				    }});	
							
						}
						br.close();
					}
				} 
				catch (final Exception e) {
					e.printStackTrace();
				}
				finally {
					if (br != null) {
			    		try {
			    			br.close();	    			
			    		}
			    		catch(Exception e2) {}
			    	}
				}
			}}, 1000, 1000);
	}
	
	private void buildProperties(File helpfile) {
	    FileInputStream fis = null; 
	    try {
	        helpProperties = new Properties();
	        fis = new FileInputStream (helpfile);
	        helpProperties.load (fis);
	        helpProperties.setProperty("HOME", "http://stage1.cytogenie.org/0aa.html");
	        helpProperties.setProperty("LAUNCH", "http://stage1.cytogenie.org/0aa.html");
	    } catch (Exception e){
	        e.printStackTrace();
	    }
	    finally {
	    	if (fis != null) {
	    		try {
	    			fis.close();	    			
	    		}
	    		catch(Exception e) {}
	    	}
	    }
	}
	
	private void buildLinks(File helpfile) {
	    FileInputStream fis = null; 
	    try {
	        helpLinks = new Properties();
	        fis = new FileInputStream (helpfile);
	        helpLinks.load (fis);
	        //helpLinks.setProperty("HOME", "http://stage1.cytogenie.org/0aa.html");
	        //helpLinks.setProperty("LAUNCH", "http://stage1.cytogenie.org/0aa.html");
	    } catch (Exception e){
	        e.printStackTrace();
	    }
	    finally {
	    	if (fis != null) {
	    		try {
	    			fis.close();	    			
	    		}
	    		catch(Exception e) {}
	    	}
	    }
	}
	
	
	private Dimension getScreenSizeMinusDockingBar(final Component c) {
		final Dimension d = c.getToolkit().getScreenSize();
		boolean isMac = System.getProperty("os.name").indexOf("Mac OS") >= 0?true:false;
		d.height -= (!isMac ? 48 : 30);
		return d;
	}

	private void topRight(final Component c) {
		topRight(c, 0, getScreenSizeMinusDockingBar(c));
		if (c instanceof Window) {
			boolean maximized = false;
			if (c instanceof Frame) {
				maximized = ((Frame) c).getExtendedState() == Frame.MAXIMIZED_BOTH;
			}
			// adjustToAvailableScreens((Window)c, maximized);
		}
	}

	private void topRight(final Component c, final int x,
			final Dimension size) {
		size.width -= 5;
		size.height -= 5;
		final Dimension componentSize = c.getSize();
		int xPos = x + (size.width - componentSize.width);
		xPos = Math.max(xPos, 0);
		c.setLocation(new Point(xPos, 0));
	}
	
	
	 public static void setUIFont (UIResource f){
	    	java.util.Enumeration keys = UIManager.getDefaults().keys();
	    	while (keys.hasMoreElements()) {
	    		Object key = keys.nextElement();
	    		Object value = UIManager.get (key);
	    		if (value != null && value instanceof javax.swing.plaf.FontUIResource)
	    			UIManager.put (key, f);
	    	}
	    }
	    
	    public static void setScreenRec(final int screenNumber) {
	    	if (mainRec == null) {
	    		final GraphicsDevice[] gs = GraphicsEnvironment.
						getLocalGraphicsEnvironment().
						getScreenDevices();
				Rectangle b=null;
				for (int i = 0; i < gs.length; i++) {
					GraphicsConfiguration gc = gs[i].getDefaultConfiguration();
					b = gc.getBounds();
				}
				if (screenNumber < gs.length){
					GraphicsConfiguration gc = gs[screenNumber].getDefaultConfiguration();
					b = gc.getBounds();
				}
				if (b == null) {
					for (int i = 0; i < gs.length; i++) {
						GraphicsConfiguration gc = gs[i].getDefaultConfiguration();
						b = gc.getBounds();
						if (b != null) {
							break;
						}
					}
				}
				mainRec=b;
	    	}
		}
	    
	    private static Dimension getRelativeDimension() {
	    	setScreenRec(0);
			Dimension dim = new Dimension();
			dim.width = (int) (WIDTH_PERCENT * mainRec.width);
			dim.height = (int) (HEIGHT_PERCENT * mainRec.height);
			return dim;
		}
	    
	    private static int getRelativeFontSize() {
			setScreenRec(0);
			int fs = 0;
			if (mainRec.width <= RESOLUTION_WIDTH_1440) {
				fs = 12;
			} else if (mainRec.width <= RESOLUTION_WIDTH_2000) {
				fs = 14;
			} else if (mainRec.width <= RESOLUTION_WIDTH_2800) {
				fs = 18;
			} else {
				fs = 28;
			}
			return fs;
		}
	    
	    private static int getRelativeBrowserZoomLevel() {
			setScreenRec(0);
			int zoom = 0;
			if (mainRec.width <= RESOLUTION_WIDTH_1440) {
				zoom = 100;
			} else if (mainRec.width <= RESOLUTION_WIDTH_2000) {
				zoom = 115;
			} else if (mainRec.width <= RESOLUTION_WIDTH_2800) {
				zoom = 130;
			} else {
				zoom = 145;
			}
			return zoom;
		}
		
		public static void launchClueTube(String url) {
			Browser browser = new Browser();
			BrowserView browserView = new BrowserView(browser);
	        JFrame frame = new JFrame("ClueTube");
	        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	        frame.add(browserView, BorderLayout.CENTER);
	        browser.setSize(getRelativeDimension());
	        browserView.setSize(getRelativeDimension());
	        frame.setSize(getRelativeDimension());
	       // browser.setZoomLevel(getRelativeBrowserZoomLevel());
	       // setUIFont(new javax.swing.plaf.FontUIResource("Arial",Font.BOLD, getRelativeFontSize()));
	        frame.setLocationRelativeTo(null);
	        frame.setVisible(true);
	        browser.loadURL(url);
		}
		
		public static void main(String a[]) {
			HELP_HOME.mkdirs();
			CLUETUBE_HOME.mkdirs();
			File propsFile = new File(getHelpResourceParentPath("helpText.properties", SERVER));
			File props2File = new File(getHelpResourceParentPath("helpLinks.properties", SERVER));
			createFileIfNotExists(propsFile, "HOME=http://stage1.cytogenie.org/0aa.html");//
			//createFileIfNotExists(propsFile, "HOME=http://stage1.cytogenie.org/CT2aa.html");//http://cytogenie.org
			createFileIfNotExists(new File(HELP_HOME, HELP_CONTEXT), "LAUNCH");
			ClueTube clueTube = new ClueTube(propsFile.getParentFile());
			if (ClueTube.getHelpText().equals("EXIT")) {
				ClueTube.setHelpText("HOME");
			}
			clueTube.buildProperties(propsFile);
			clueTube.buildLinks(props2File);
			ClueTube.setClueTubeRunning();
			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					ClueTube.unsetClueTubeRunning();
				}
			});
			clueTube.startTimer();
		}
		
		public static void takeScreenShot(int x, int y, int width, int height, String file) {
			
			if (file != null) {
				Toolkit toolkit = Toolkit.getDefaultToolkit();
				Dimension screenSize = toolkit.getScreenSize();
				Rectangle screenRect = new Rectangle(x, y, width, height);
				try {
					Robot robot = new Robot();
					BufferedImage image = robot.createScreenCapture(screenRect);
					ImageIO.write(image, "jpg", new File(file));
				} catch (Exception e) {
					e.printStackTrace();
				}	
			}
		}
		
}
