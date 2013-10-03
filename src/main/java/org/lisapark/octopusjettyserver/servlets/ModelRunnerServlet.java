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
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.common.base.Preconditions;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.lisapark.octopus.ModelRunner;
import org.lisapark.octopus.core.ModelBean;
import org.lisapark.octopus.core.ProcessingModel;
import org.lisapark.octopus.repository.OctopusRepository;
import org.lisapark.octopus.repository.RepositoryException;
import org.lisapark.octopus.repository.db4o.OctopusDb4oRepository;
import org.openide.util.Exceptions;

/**
 *
 * @author alex
 */
public class ModelRunnerServlet extends HttpServlet {

    /**
     * ModelRunnerServlet allows to run Octopus models on any http server. In order to
     * run models the following conditions should be met:
     *  - the running model should be presented in server side db4o database;
     *  - the request for execution of this model should include at least one parameter with
     *    model name. Name of this parameter is in the http server octopus.properties file;
     * 
     * All parameter names for the executable model should be prefixed with a standard prefix, 
     * it is also provided in octopus.properties file. Prefix and parameter name are separated 
     * by period (.).
     * 
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     */
    
     /**
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(ObjectServer server, String modelName, 
            Map<String, String> paramMap, HttpServletResponse response) throws IOException, RepositoryException {

        OctopusRepository repository = new OctopusDb4oRepository(server);

        ProcessingModel currentProcessingModel;

        currentProcessingModel = repository.getProcessingModelByName(modelName);

        ModelRunner modelRunner = new ModelRunner(currentProcessingModel, paramMap, paramMap);

        modelRunner.runModel();
        
        response.setStatus(200);
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            /*
             * TODO output your page here. You may use following sample code.
             */
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet ModelRunner</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet ModelRunner has run Model: " + modelName + "</h1>");
            out.println("</body>");
            out.println("</html>");
            
        } finally {
            out.close();
        }
    }
    
    protected void processRequest(ObjectServer server, String modelName, 
            ModelBean modelBean, HttpServletResponse response) throws IOException, RepositoryException {

        OctopusRepository repository = new OctopusDb4oRepository(server);

        ProcessingModel currentProcessingModel;

        currentProcessingModel = repository.getProcessingModelByName(modelBean.getModelName());

        ModelRunner modelRunner = new ModelRunner(currentProcessingModel, modelBean);

        modelRunner.runModel();
        
        response.setStatus(200);
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            /*
             * TODO output your page here. You may use following sample code.
             */
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet ModelRunner</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet ModelRunner has run Model: " + modelBean.getModelName() + "</h1>");
            out.println("</body>");
            out.println("</html>");
            
        } finally {
            out.close();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        ServletContext servletContext = this.getServletContext();
        ObjectServer server = (ObjectServer) servletContext.getAttribute(ContextListener.KEY_DB4O_SERVER);

        try {
            if (request.getCharacterEncoding() == null) {
                request.setCharacterEncoding("UTF-8");
            }

            String modelNameParam = (String) servletContext.getInitParameter(ContextListener.KEY_MODEL_NAME_PARAM);
            
            String modelName = request.getParameter(modelNameParam);

            Map<String, String> paramMap = Maps.newHashMap();
            
            Enumeration<String> attrList = request.getParameterNames();
            while (attrList.hasMoreElements()) {
                String attrName = (String) attrList.nextElement();                
                paramMap.put(attrName, request.getParameterValues(attrName)[0]);
            }
            
            processRequest(server, modelName, paramMap, response);

        } catch (RepositoryException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        ServletContext servletContext = this.getServletContext();
        ObjectServer server = (ObjectServer) servletContext.getAttribute(ContextListener.KEY_DB4O_SERVER);
        
        StringBuilder jsonBuilder = new StringBuilder();
        String line;
        
        try {
            
            if (request.getCharacterEncoding() == null) {
                request.setCharacterEncoding("UTF-8");
            }
            
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
            
            Map<String, String> requestParamMap = Maps.newHashMap();

            if (jsonBuilder == null && jsonBuilder.toString().isEmpty()) {
                
            } else {
                requestParamMap = new Gson().fromJson(jsonBuilder.toString(), Map.class);
            }
            
            String modelNameParam = (String)servletContext.getInitParameter(ContextListener.KEY_MODEL_NAME_PARAM);
            String paramNameParam = (String)servletContext.getInitParameter(ContextListener.KEY_PARAM_NAME_PARAM);
            String modelJsonParam = (String)servletContext.getInitParameter(ContextListener.KEY_MODEL_JSON_PARAM);
            
            String modelName = requestParamMap.get(modelNameParam);
            String paramName = requestParamMap.get(paramNameParam);
            String modelJson = requestParamMap.get(modelJsonParam);
            
            Preconditions.checkNotNull(modelName, "Model name cannot be null.");
            
            

            if (modelJson != null && !modelJson.isEmpty()) {
                
                ModelBean modelBean = new Gson().fromJson(modelJson, ModelBean.class);
                processRequest(server, modelName, modelBean, response);
                
            } else if (paramName != null && !paramName.isEmpty()) {
                
                Map<String, String> paramMap = new Gson().fromJson(paramName, Map.class);
                processRequest(server, modelName, paramMap, response);
                
            } else {
                ModelBean modelBean = null;
                processRequest(server, modelName, modelBean, response);
                
            }
            
        } catch (RepositoryException ex) {
            Exceptions.printStackTrace(ex);
        } catch (Exception e) { 
            /*report an error*/         
        } 
        
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
