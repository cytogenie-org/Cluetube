//
// $Id: GetdownApp.java,v 1.1 2015/03/02 17:18:39 beauheim.woodsidelogic Exp $
//
// Getdown - application installer, patcher and launcher
// Copyright (C) 2004-2010 Three Rings Design, Inc.
// http://code.google.com/p/getdown/
//
// Redistribution and use in source and binary forms, with or without modification, are permitted
// provided that the following conditions are met:
//
// 1. Redistributions of source code must retain the above copyright notice, this list of
//    conditions and the following disclaimer.
// 2. Redistributions in binary form must reproduce the above copyright notice, this list of
//    conditions and the following disclaimer in the documentation and/or other materials provided
//    with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES,
// INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
// PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT,
// INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
// TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
// LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

package com.threerings.getdown.launcher;


import static com.threerings.getdown.Log.log;

import java.awt.Container;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import com.samskivert.swing.util.SwingUtil;
import com.samskivert.util.ArrayUtil;
import com.samskivert.util.StringUtil;
/**
 * The main application entry point for Getdown.
 */
public class GetdownApp
{
	
	public static boolean isOnline() {
		try {
			URL url = new URL("http://www.google.com");
			URLConnection uconn = url.openConnection();
			uconn.setUseCaches(false);
    		uconn.getInputStream();
    		return true;
		}
		catch(Exception e) {
			return false;
		}
	}
	
	public static ImageIcon getImageIcon(final Class _class, final String subFolder, final String fileName, String fileExtension) {
    	if (!fileExtension.startsWith(".")){
    		fileExtension=concat(".", fileExtension.toLowerCase());
    	} else {
    		fileExtension=fileExtension.toLowerCase();
    	}
    	final String key=concat(_class.getName(), ":", subFolder, "/", fileName, fileExtension);
        ImageIcon icon = null;
        if (icon == null || icon.getIconWidth() == -1) {
            icon = loadImageIcon(
            		_class, concat(subFolder, "/", fileName, fileExtension.toLowerCase()));
            if (icon==null){
            	icon = loadImageIcon(
            			_class, concat(subFolder, "/", fileName, fileExtension.toUpperCase()));         	
            }
            if (icon == null && !fileName.equals("spacer")) {
                icon = getImageIcon(GetdownApp.class, subFolder, "spacer", ".gif");
            }
        }
        return icon;
    }

	public static String concat( final String arg1, final String arg2, final String ... args){  		
  		final StringBuilder sb= new StringBuilder();
  		sb.append(arg1);
  		sb.append(arg2);
  		for (final String arg:args){
  			sb.append(arg);
  		}
  		return sb.toString();
  	}
	
	public static ImageIcon loadImageIcon(Class cls, String name) {
		ImageIcon icon = null;
		URL url = cls.getResource(name);
		if (url != null) {
			icon = new ImageIcon(url);
		}
		return icon;
	}
	private static boolean isEmpty(final String inputString) {
        return inputString == null ||
          inputString.trim().length() == 0;
    }
	
    public static void main (String[] argArray)
    {
        // maybe they specified the appdir in a system property
        int aidx = 0;
        List<String> args = Arrays.asList(argArray);
        String adarg = System.getProperty("appdir");
        //boolean isMac = System.getProperty("os.name").indexOf("Mac OS") >= 0?true:false;
        /*if (args.size() > 1) {
        	//How does this work for Mac?
        	System.setProperty("user.dir", args.get(0));	
        }*/
        
        // if not, check for a command line argument
        if (StringUtil.isBlank(adarg)) {
            if (args.isEmpty()) {
                System.err.println("Usage: java -jar getdown.jar app_dir [app_id] [app args]");
                System.exit(-1);
            }
            adarg = args.get(aidx++);
        }
       /* String launchFile = null;
        if (args.size() > 1) {
        	launchFile = args.get(aidx++);            
        }
        log.info("Launch file: " + launchFile);*/
        
        if (adarg.indexOf(" ") != -1) {
        	adarg = fixSpecialCharsInFolderPath_old(adarg);
        	argArray[argArray.length-1] = adarg;
    		args = Arrays.asList(argArray);	
        }

        // look for a specific app identifier
        String appId = (aidx < args.size()) ? args.get(aidx++) : System.getProperty("appid");

        // pass along anything after that as app args
        String[] appArgs = (aidx < args.size()) ?
            args.subList(aidx, args.size()).toArray(ArrayUtil.EMPTY_STRING) : null;

        /*if (appArgs == null && !isEmpty(launchFile)) {
        	appArgs = new String[1];
        	appArgs[0] =launchFile; 
        }*/
        // ensure a valid directory was supplied
        File appDir = new File(adarg);
        if (!appDir.exists() || !appDir.isDirectory()) {
            log.warning("Invalid app_dir '" + adarg + "'.");
            System.exit(-1);
        }

        // pipe our output into a file in the application directory
        if (System.getProperty("no_log_redir") == null) {
            File logFile = new File(appDir, "launcher.log");
            try {
                PrintStream logOut = new PrintStream(
                    new BufferedOutputStream(new FileOutputStream(logFile, true)), true);
                System.setOut(logOut);
                System.setErr(logOut);
            } catch (IOException ioe) {
                log.warning("Unable to redirect output to '" + logFile + "': " + ioe);
            }
        }
        
    	//Check another copy of Cluetube runs
    	String line;
    	int resultsLineCount = 0;
    	try {
    		boolean isMac = System.getProperty("os.name").indexOf("Mac OS") >= 0?true:false;
    		if (!isMac) {
    			Process proc = Runtime.getRuntime().exec("wmic.exe");
        	    BufferedReader input = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        	    OutputStreamWriter oStream = new OutputStreamWriter(proc.getOutputStream());
        	    oStream .write("process where name='Cluetube.exe'");
        	    oStream .flush();
        	    oStream .close();
        	    
        	    while ((line = input.readLine()) != null) {
        	    	log.info(line);
        	    	if (line.trim().length() > 0) {
        	    		resultsLineCount++;	
        	    	}
        	        
        	    }
        	    input.close();         
        	    
        	    log.info("TOTAL WMIC LINES: " + resultsLineCount);
            	
            	if (resultsLineCount > 3) {
            		log.info("Found another copy of Cluetube, Bringing that one to front");                	
                	String activateScriptFile = ".\\..\\domains\\FACS\\CluetubeActivate.vbs";
            		/*List<Resource> ress = _app.getAllResources();
            		for (Resource re: ress) {
            			log.info("Resource: " + re.getLocal().getName() + "," + re.getLocal().getPath());
            			if (re.getLocal().getName().equals(activateScriptFile)) {
            				activateScriptFile = re.getLocal().getAbsolutePath();
            				break;
            			}
            		}*/
            		log.info("Running activation script: " + activateScriptFile);
            		String cmds[] = new String[]{"cmd.exe","/c", activateScriptFile};
                	final Process p=Runtime.getRuntime().exec(cmds);
        			String lsString;
        			BufferedReader br = new BufferedReader(
        					new InputStreamReader(p.getErrorStream()));
        			while ((lsString = br.readLine()) != null) {
        				log.info("ScriptOut: " + lsString);
        				System.out.println(lsString);
        			}								
        			p.waitFor();    			
        			log.info("Script Run, Existing from this");
        			System.exit(0);
            	}
    		}
    		else {
    			log.info("Checking for multiple copies of Cluetube");
    			String activateScriptFile = "./../domains/FACS/CluetubeActivate.scpt";
    			File checkFile = new File(activateScriptFile);
    			if (checkFile.exists()) {
    				final String []cmd={"osascript", activateScriptFile};
        			try {        				
        				final Process p=Runtime.getRuntime().exec(cmd);
        				String lsString;
        				BufferedReader br = new BufferedReader(
        						new InputStreamReader(p.getInputStream()));
        				boolean copyFound = false;
        				while ((lsString = br.readLine()) != null) {        					
        					log.info("Got this from the script:" + lsString);
        					if (lsString.equalsIgnoreCase("true")) {
        						copyFound = true;
        					}
        					break;
        				}
        				p.waitFor();
        				p.waitFor();
        				log.info("Script has run2...");
        				if (copyFound) {
        					log.info("Found another copy of Cluetube running, Now restoring it ...");
        					//final String []cmd1={"osascript", "-e", "'tell app \"Cluetube\" to reopen' -e \'tell app \"Cluetube\" to activate'"};
        					//String cmdStr="osascript -e 'tell app \"Cluetube\" to reopen' -e 'tell app \"Cluetube\" to activate'";        					            
        					final String []cmd1={"osascript", "-e", "tell app \"Cluetube\" to reopen"};
        					final Process p1 = Runtime.getRuntime().exec(cmd1);
        					BufferedReader bufferedReader = new BufferedReader(
        						    new InputStreamReader(p1.getErrorStream()));
						    while ((lsString = bufferedReader.readLine()) != null) {
						      System.out.println(lsString);
						      log.info(lsString);
						    }
        					p1.waitFor();
        					log.info("Restored it, Now bringing to front...");
        					final String []cmd2={"osascript", "-e", "tell app \"Cluetube\" to activate"};
        					final Process p2 = Runtime.getRuntime().exec(cmd2);
        					BufferedReader bufferedReader2 = new BufferedReader(
        						    new InputStreamReader(p2.getErrorStream()));
						    while ((lsString = bufferedReader2.readLine()) != null) {
						      System.out.println(lsString);
						      log.info(lsString);
						    }
        					p2.waitFor();
        					log.info("Now exiting from the current...");
        					System.exit(0);
        				}
        				log.info("No other copy of Cluetube running, Continuing with the current...");
        			} catch (IOException e) {
        				// TODO Auto-generated catch block
        				e.printStackTrace();
        				log.info("IOEX "+ e.getMessage());
        			} catch (InterruptedException e) {
        				// TODO Auto-generated catch block
        				e.printStackTrace();
        				log.info("IEOEX "+ e.getMessage());
        			}	
    			}
    			
    		}
    	} catch (Exception ioe) {
    	    ioe.printStackTrace();
    	    log.info("EEX "+ ioe.getMessage());
    	}

    	
        log.info("Getdown is starting");
        String osname = System.getProperty("os.name");
        if (osname.indexOf("Windows 7") != -1 || osname.indexOf("Windows Vista") != -1 || osname.indexOf("Windows NT (unknown)") != -1) {//At times windows 7 machine returns as windows vista?
        	final String userProfile = System.getenv("USERPROFILE");
        	log.info("Setting user profile as user home");
       		System.setProperty("user.home", userProfile); //Known issue with Windows 7 "user.home" is incorrectly set
        }

        // record a few things for posterity
        log.info("------------------ M/C Details ------------------");
        log.info("-- OS Name: " + System.getProperty("os.name"));
        log.info("-- OS Arch: " + System.getProperty("os.arch"));
        log.info("-- OS Vers: " + System.getProperty("os.version"));
        log.info("-- Java Vers: " + System.getProperty("java.version"));
        log.info("-- Java Home: " + System.getProperty("java.home"));
        log.info("-- User Name: " + System.getProperty("user.name"));
        log.info("-- User Home: " + System.getProperty("user.home"));
        log.info("-- Cur dir: " + System.getProperty("user.dir"));
        log.info("---------------------------------------------");

        try {
        	/*if (isOnline() && !AppAuthenticator.authenticate()) {
        		System.exit(1);
        	}*/
        	/*log.info("App directory & arguments: " + appDir);
        	if (appArgs != null && appArgs.length > 0) {
    			for (int i=0; i < appArgs.length; i++) {
    				log.info(appArgs[i]);
    			}	
    		}*/
            Getdown app = new Getdown(appDir, appId, null, null, appArgs) {
                @Override
                protected Container createContainer () {
                    // create our user interface, and display it
                    String title = StringUtil.isBlank(_ifc.name) ? "" : _ifc.name;
                    if (_frame == null) {
                        _frame = new JFrame(title);
                    	_frame.setIconImage(getImageIcon(GetdownApp.class, "images", "gating_small", ".jpg").getImage());                                        	
                        _frame.addWindowListener(new WindowAdapter() {
                            @Override
                            public void windowClosing (WindowEvent evt) {
                                handleWindowClose();
                            }
                        });
                        _frame.setResizable(false);
                    } else {
                        _frame.setTitle(title);
                        _frame.getContentPane().removeAll();
                    }

                    if (_ifc.iconImages != null) {
                        ArrayList<Image> icons = new ArrayList<Image>();
                        for (String path : _ifc.iconImages) {
                            Image img = loadImage(path);
                            if (img == null) {
                                log.warning("Error loading icon image", "path", path);
                            } else {
                                icons.add(img);
                            }
                        }
                        if (icons.isEmpty()) {
                            log.warning("Failed to load any icons", "iconImages", _ifc.iconImages);
                        } else {
                            SwingUtil.setFrameIcons(_frame, icons);
                        }
                    }

                    _frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                    return _frame.getContentPane();
                }
                @Override
                protected void showContainer () {
                    if (_frame != null) {
                        _frame.pack();
                        SwingUtil.centerWindow(_frame);
                        _frame.setVisible(true);
                    }
                }
                @Override
                protected void hideContainer () {
                    if (_frame != null) {
                        _frame.setVisible(false);
                    }
                }
                @Override
                protected void disposeContainer () {
                    if (_frame != null) {
                        _frame.dispose();
                        _frame = null;
                    }
                }
                @Override
                protected void exit (int exitCode) {
                    System.exit(exitCode);
                }
                protected JFrame _frame;
            };
            app.start();

        } catch (Exception e) {
            log.warning("main() failed.", e);
        }
    }

	private static String fixSpecialCharsInFolderPath_old(String adarg) {
		if (adarg.indexOf(" ") != -1) {
        	StringTokenizer tokens = new StringTokenizer(adarg, " ");
			String userHome = "";
			boolean skipNext = false;
			while (tokens.hasMoreTokens()) {
				String token = tokens.nextToken();
				if (skipNext)  {
					int lI = token.indexOf("\\");
					if (lI != -1) {
						userHome += token.substring(0,lI);
						userHome += "/";
						userHome += token.substring(lI);
					}
					skipNext = false;
					continue;
				}
				int lI = token.lastIndexOf("\\");
				if (lI != -1) {
					userHome += token.substring(0,lI+1);
					userHome += "/";
					userHome += token.substring(lI+1);
					userHome += " ";
				}
				skipNext = true;
			}
			return userHome;			
        }
		return adarg;
	}
    
    private static String fixSpecialCharsInFolderPath(String userHomePath) {
    	boolean isMac = System.getProperty("os.name").indexOf("Mac OS") >= 0?true:false;
		String home = userHomePath;
		log.info("\n");
		
		String uHome = "";
		if (!isMac) {
			StringTokenizer tokenizer = new StringTokenizer(home, "\\");
			while (tokenizer.hasMoreTokens()) {
				uHome = uHome + tokenizer.nextToken() + "\\\\";
			}	
		}
		
		if (uHome.length()==0) {
			uHome = home; 
		}
		String userHome = "";
		String r = "[\\|&()< >'`:;]";
		Pattern p = Pattern.compile(r);
		Matcher m1 = p.matcher(uHome);
		if (m1.find()) {
			log.info("User home has one or more special characters " + uHome);
        	StringTokenizer tokens = new StringTokenizer(uHome, File.separator);
			while (tokens.hasMoreTokens()) {
				String token = tokens.nextToken();
				log.info("Current token: " + token);
				Matcher m2 = p.matcher(token);
				if (m2.find()) {
					userHome += isMac?"\"":"/";
					userHome += token;
					userHome += isMac?"\"":"/";
					userHome += File.separator;
				}
				else {
					userHome += token;
					userHome += File.separator;
				}
			}
			log.info(userHome);
			return userHome;
		}
		return uHome;
	}
}
