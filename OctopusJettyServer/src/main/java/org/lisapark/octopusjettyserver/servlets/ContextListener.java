/* 
 * Copyright (C) 2013 Lisa Park, Inc. (www.lisa-park.net)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.lisapark.octopusjettyserver.servlets;

import com.db4o.ObjectServer;
import com.db4o.config.ConfigScope;
import com.db4o.cs.Db4oClientServer;
import com.db4o.cs.config.ServerConfiguration;
import com.db4o.ta.DeactivatingRollbackStrategy;
import com.db4o.ta.TransparentActivationSupport;
import com.db4o.ta.TransparentPersistenceSupport;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 *
 * @author alex
 */
public class ContextListener  implements ServletContextListener {
    
    public static final String KEY_DB4O_FILE_NAME   = "db4oFileName";
    public static final String KEY_DB4O_SERVER      = "db4oServer";
    public static final String KEY_DB4O_SERVER_PORT = "db4oPort";
    
    public static final String KEY_DB4O_UID         = "db4oUid";
    public static final String KEY_DB4O_PSW         = "db4oPsw";
    public final static String KEY_MODEL_NAME_PARAM     = "modelname";
    public final static String KEY_PARAM_NAME_PARAM     = "prefixname";
    
    private ObjectServer server = null;
    
    /**
     * 
     * @param event 
     */
    @Override
    public void contextInitialized(ServletContextEvent event) {

        close();

        ServletContext context = event.getServletContext();

        String filePath = context.getInitParameter(KEY_DB4O_FILE_NAME);
        Integer db4oPort = Integer.parseInt(context.getInitParameter(KEY_DB4O_SERVER_PORT));
        
        String uid = context.getInitParameter(KEY_DB4O_UID);
        String psw = context.getInitParameter(KEY_DB4O_PSW);
        
        ServerConfiguration config = Db4oClientServer.newServerConfiguration();
        config.common().add(new TransparentActivationSupport());
        config.common().add(new TransparentPersistenceSupport(new DeactivatingRollbackStrategy()));
        config.file().generateUUIDs(ConfigScope.GLOBALLY);
        config.file().generateCommitTimestamps(true);
        
        server = Db4oClientServer.openServer(config, filePath, db4oPort);
        
        server.grantAccess(uid, psw);
        
        context.setAttribute(KEY_DB4O_SERVER, server);
        context.log("db4o startup on " + filePath);
    }

    /**
     * 
     * @param event 
     */
    @Override
    public void contextDestroyed(ServletContextEvent event) {
        ServletContext context = event.getServletContext();
        context.removeAttribute(KEY_DB4O_SERVER);

        close();

        context.log("db4o shutdown");
    }

    /**
     * 
     */
    private void close() {
        if (server != null) {
            server.close();
        }
        server = null;
    }
}
