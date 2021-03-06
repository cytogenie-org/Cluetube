//
// $Id: GetdownApplet.java,v 1.1 2015/03/02 17:18:38 beauheim.woodsidelogic Exp $
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

import java.awt.Container;
import java.awt.Image;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.JApplet;
import javax.swing.JPanel;

import static com.threerings.getdown.Log.log;

/**
 * An applet that can be used to launch a Getdown application (when signed and given privileges).
 */
public class GetdownApplet extends JApplet
    implements ImageLoader
{
    @Override // documentation inherited
    public void init ()
    {
        _config = new GetdownAppletConfig(this);

        try {

            try {
                // Check our permissions, download getdown.txt, etc.
                _config.init();
            } catch (Exception e) {
                _errmsg = e.getMessage();
            }

            // XXX getSigners() returns all certificates used to sign this applet which may allow
            // a third party to insert a trusted certificate. This should be replaced with
            // statically included trusted keys.
            _getdown = new Getdown(_config.appdir, null, GetdownApplet.class.getSigners(),
                                   _config.jvmargs, _config.appargs) {
                @Override
                protected Container createContainer () {
                    getContentPane().removeAll();
                    return getContentPane();
                }
                @Override
                protected RotatingBackgrounds getBackground () {
                    return _config.getBackgroundImages(GetdownApplet.this);
                }
                @Override
                protected void showContainer () {
                    ((JPanel)getContentPane()).revalidate();
                }
                @Override
                protected void hideContainer () {
                    ((JPanel)getContentPane()).hide();
                }
                @Override
                protected void disposeContainer () {
                    // nothing to do as we're in an applet
                }
                @Override
                protected boolean invokeDirect () {
                    return _config.invokeDirect;
                }
                @Override
                protected JApplet getApplet () {
                    return GetdownApplet.this;
                }
                @Override
                protected void exit (int exitCode) {
                    _app.releaseLock();
                    _config.redirect(false);
                }
            };

            // set up our user interface
            _config.config(_getdown);
            _getdown.preInit();

        } catch (Exception e) {
            // assume that if we already encountered an error, that is the root cause that we want
            // to report back to the user
            if (_errmsg == null) {
                _errmsg = e.getMessage();
            }
            log.warning("init() failed.", e);
        }
    }

    @Override // documentation inherited
    public void destroy ()
    {
        _config.redirect(true);
        super.destroy();
    }
    
    // implemented from ImageLoader
    public Image loadImage (String path)
    {
        try {
            return getImage(new URL(getDocumentBase(), path));
        } catch (MalformedURLException e) {
            log.warning("Failed to load background image", "path", path, e);
            return null;
        }
    }

    @Override // documentation inherited
    public void start ()
    {
        if (_errmsg != null) {
            _getdown.fail(_errmsg);
        } else {
            try {
                _getdown.start();
            } catch (Exception e) {
                log.warning("start() failed.", e);
            }
        }
    }

    @Override // documentation inherited
    public void stop ()
    {
        // Interrupt the getdown thread to tell it to kill its current downloading or verifying
        // before launching
        _getdown.interrupt();
        // release the lock if the applet window is closed or replaced
        _getdown._app.releaseLock();
    }

    /**
     * Creates the specified file and writes the supplied contents to it.
     */
    protected boolean writeToFile (File tofile, String contents)
    {
        try {
            PrintStream out = new PrintStream(new FileOutputStream(tofile));
            out.println(contents);
            out.close();
            return true;
        } catch (IOException ioe) {
            log.warning("Failed to create '" + tofile + "'.", ioe);
            return false;
        }
    }

    /** The Getdown configuration as pulled from the applet params */
    protected GetdownAppletConfig _config;

    /** Handles all the actual getting down. */
    protected Getdown _getdown;

    /** An error encountered during initialization. */
    protected String _errmsg;
}
