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
package org.lisapark.octopusjettyserver;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.lisapark.octopusjettyserver.servlets.ContextListener;
import org.lisapark.octopusjettyserver.servlets.ModelRunnerServlet;
import org.lisapark.octopusjettyserver.servlets.SelectModels;
import org.lisapark.octopusjettyserver.servlets.Test;

/**
 * ModelRunner Servlet
 *
 */
public class JettyModelRunner {    
    
    static String port = "8080";

    public static void main(String[] args) throws Exception {
        
        if (args.length != 1) {
            System.err.printf("Usage: JettyModelRunner propertyFile\n");
            System.exit(-1);
        }

        Properties properties = parseProperties(args[0]);
        
        String repositoryDir = properties.getProperty("octopus.repository.dir");
        
        if (repositoryDir == null || repositoryDir.length() == 0) {
            System.err.printf("The property file %s is missing the octopus.repository.dir property", args[0]);
            System.exit(-1);
        }
        
        String repositoryFile = properties.getProperty("octopus.repository.file");
        
        if (repositoryFile == null || repositoryFile.length() == 0) {
            System.err.printf("The property file %s is missing the octopus.repository.file property", args[0]);
            System.exit(-1);
        } else {
            repositoryFile = repositoryDir + "/" + repositoryFile;
        }
        
        String jettyServerPort = properties.getProperty("jetty.server.port");
        
        if (jettyServerPort == null || jettyServerPort.length() == 0) {
            System.err.printf("The property file %s is missing the jetty.server.port property", args[0]);
            System.exit(-1);
        }

         
        String db4oServerPort = properties.getProperty("db4o.server.port");
        
        if (db4oServerPort == null || db4oServerPort.length() == 0) {
            System.err.printf("The property file %s is missing the jetty.server.port property", args[0]);
            System.exit(-1);
        }

        String db4oUid = properties.getProperty("db4o.server.uid");
        String db4oPsw = properties.getProperty("db4o.server.psw");
        String modelname = properties.getProperty("model.name.param");
        String prefixname = properties.getProperty("prefix.name.param");
        String modeljson = properties.getProperty("model.json.param");
        
        Server server = new Server(Integer.parseInt(jettyServerPort));
        server.setStopAtShutdown(true);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.setResourceBase(".");

        context.addServlet(new ServletHolder(new SelectModels()), "/search/*");
        context.addServlet(new ServletHolder(new ModelRunnerServlet()), "/run/*");
        
        context.addEventListener(new ContextListener());

        context.setInitParameter(ContextListener.KEY_DB4O_FILE_NAME, repositoryFile);
        context.setInitParameter(ContextListener.KEY_DB4O_SERVER_PORT, db4oServerPort);
        context.setInitParameter(ContextListener.KEY_DB4O_UID, db4oUid);
        context.setInitParameter(ContextListener.KEY_DB4O_PSW, db4oPsw);
        context.setInitParameter(ContextListener.KEY_MODEL_NAME_PARAM, modelname);
        context.setInitParameter(ContextListener.KEY_PARAM_NAME_PARAM, prefixname);
        context.setInitParameter(ContextListener.KEY_MODEL_JSON_PARAM, modeljson);

        server.setHandler(context);

        server.start();
        server.join();

    }
    
    
    private static Properties parseProperties(String propertyFileName) throws IOException {
        InputStream fin = null;
        Properties properties = null;
        try {
            fin = JettyModelRunner.class.getResourceAsStream("/" + propertyFileName);

            properties = new Properties();
            properties.load(fin);

        } finally {
            IOUtils.closeQuietly(fin);
        }

        return properties;
    }
}
