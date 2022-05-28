//
// $Id: HTTPDownloader.java,v 1.1 2015/03/02 17:18:41 beauheim.woodsidelogic Exp $
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

package com.threerings.getdown.net;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.JOptionPane;

import com.samskivert.io.StreamUtil;

import com.threerings.getdown.data.Resource;

import static com.threerings.getdown.Log.log;

/**
 * Implements downloading files over HTTP
 */
public class HTTPDownloader extends Downloader
{
    public HTTPDownloader (List<Resource> resources, Observer obs)
    {
        super(resources, obs);
    }

    @Override
    protected long checkSize (Resource rsrc)
        throws IOException
    {
        URLConnection conn = rsrc.getRemote().openConnection();
        try {
            // if we're accessing our data via HTTP, we only need a HEAD request
            if (conn instanceof HttpURLConnection) {
                HttpURLConnection hcon = (HttpURLConnection)conn;
                hcon.setRequestMethod("HEAD");
                hcon.connect();
                // make sure we got a satisfactory response code
                if (hcon.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    throw new IOException("Unable to check up-to-date for " +
                                          rsrc.getRemote() + ": " + hcon.getResponseCode());
                }
            }
            if (!needDownload(rsrc, conn, false)) {
            	return 0L;
            }
            return conn.getContentLength();

        } finally {
            // let it be known that we're done with this connection
            conn.getInputStream().close();
        }
    }

    public static void updateTimeStamp(Resource rsrc, URLConnection connection)  {
    	log.info("Updating timestamp for file: " + rsrc.getLocal().getName());
    	try {
    		final SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmss");
    		File localTimeStampFile =  new File(rsrc.getLocal().getParentFile(), rsrc.getLocal().getName() + "_servertimestamp.txt");
    		Date lastModified = new Date(connection.getLastModified());
            String serverTimeStamp = dateFormat.format(lastModified).trim();
            FileWriter fw = new FileWriter(localTimeStampFile);
    		fw.write(serverTimeStamp);
    		fw.close();	
    	}
    	catch(Exception e) {
    		log.info("Exception Updating: " + e.getMessage());
    		e.printStackTrace();
    	}
    	log.info("Updated");
    }
    
    public static boolean needDownload(Resource rsrc, URLConnection connection, boolean updateTimeStamp)  {
    	try {
			final File preExistingFile = rsrc.getLocal();
			String localTimeStamp = "";
			final SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmss");
			File localTimeStampFile =  new File(rsrc.getLocal().getParentFile(), rsrc.getLocal().getName() + "_servertimestamp.txt");
			if (preExistingFile.exists()) {
				if (localTimeStampFile.exists()) {
					BufferedReader br = new BufferedReader(new FileReader(localTimeStampFile));
					String s = br.readLine();
					if (s!= null) {					
						localTimeStamp = s;
					}
				}
			} 
	        Date lastModified = new Date(connection.getLastModified());
	        String serverTimeStamp = dateFormat.format(lastModified).trim();
			if (localTimeStamp.trim().equals(serverTimeStamp)) {
				log.info("Timestamp matches, Not to download." + rsrc.getLocal().getName());
				return false;
			}
			else if (updateTimeStamp){				
				log.info("Timestamp does not match, Need to download." + rsrc.getLocal().getName());
				return true;
			}
		}  
    	catch(Exception e) {e.printStackTrace();}
    	finally {
			// avoid massive memory leak
		}
    	log.info("Impossible return, Need to download."+ rsrc.getLocal().getName());
    	return true;
	}
    
    @Override
    protected void doDownload (Resource rsrc)
        throws IOException
    {
        // download the resource from the specified URL
        URLConnection conn = rsrc.getRemote().openConnection();
        conn.connect();

        // make sure we got a satisfactory response code
        if (conn instanceof HttpURLConnection) {
            HttpURLConnection hcon = (HttpURLConnection)conn;
            if (hcon.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException("Unable to download resource " + rsrc.getRemote() + ": " +
                                      hcon.getResponseCode());
            }
        }

        if (!needDownload(rsrc, conn, true)) {
        	return;
        }
        long actualSize = conn.getContentLength();
        log.info("Downloading resource : ", "url", rsrc.getRemote(), "size", actualSize);
        InputStream in = null;
        FileOutputStream out = null;
        long currentSize = 0L;
        String localFileName = rsrc.getLocal().getAbsolutePath();
        boolean selfUpdate = false;
        try {
            in = conn.getInputStream();
            /*if (localFileName.endsWith("getdown.jar")) {
            	//Self update, Copy to temporary file, 
            	selfUpdate = true;
            	out = new FileOutputStream(new File(localFileName + ".new"));	
            }
            else {
            	out = new FileOutputStream(rsrc.getLocal());	
            }*/
            out = new FileOutputStream(rsrc.getLocal());
            int read;

            // TODO: look to see if we have a download info file
            // containing info on potentially partially downloaded data;
            // if so, use a "Range: bytes=HAVE-" header.

            // read in the file data
            while ((read = in.read(_buffer)) != -1) {
                // write it out to our local copy
                out.write(_buffer, 0, read);

                // if we have no observer, then don't bother computing download statistics
                if (_obs == null) {
                    continue;
                }

                // note that we've downloaded some data
                currentSize += read;
                updateObserver(rsrc, currentSize, actualSize);
            }
            updateTimeStamp(rsrc, conn);
        } finally {
            StreamUtil.close(in);
            StreamUtil.close(out);
        }
        boolean isMac = false;
        if (System.getProperty("os.name").indexOf("Mac OS") >= 0) {
        	isMac = true;
        }
        if (isMac && localFileName.endsWith(".tar")) {
        	log.info("Extracting tar file: " + localFileName);
        	log.info("cmd: " + "tar xvf " + localFileName);
        	Runtime.getRuntime().exec("tar xvf " + localFileName);
        	
        }
        else if (localFileName.endsWith(".zip")) {
        	log.info("Extracting zip file.");
        }
        if (selfUpdate) {
        	selfUpdate = false;
        	JOptionPane.showMessageDialog(null, "Critical updates are made, please relaunch the application");
        	log.info("Renaming getdownjar file: " + localFileName);
        	if (isMac) {
        		String command = "mv " + localFileName + ".new " + localFileName;
        		log.info("cmd: " + command);
        		Runtime.getRuntime().exec(command);
        	}        	
        	else {
        		String command = "ren " + localFileName + ".new " + localFileName;
        		log.info("cmd: " + command);
        		Runtime.getRuntime().exec(new String[]{"cmd", "/c", command});
        	}
        	
        	System.exit(0);
        }
        else {
        	log.info("Self update has been turned off.");
        }
        	
        
    }
}
