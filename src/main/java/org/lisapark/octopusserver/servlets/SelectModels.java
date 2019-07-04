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
package org.lisapark.octopusserver.servlets;

import com.db4o.ObjectServer;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.lisapark.octopus.core.ProcessingModel;
import org.lisapark.octopus.repository.OctopusRepository;
import org.lisapark.octopus.repository.RepositoryException;
import org.lisapark.octopus.repository.db4o.OctopusDb4oRepository;
import org.openide.util.Exceptions;

/**
 *
 * @author alex
 */
public class SelectModels extends HttpServlet {

    private static final String SEARCH_PARAM = "search";
    private static final String NAME_PARAM = "name";

    protected void processRequest(ObjectServer server, HttpServletRequest request,
            HttpServletResponse response) throws IOException, RepositoryException {

        OctopusRepository repository = new OctopusDb4oRepository(server);

        String search = request.getParameter(SEARCH_PARAM);

        if (search == null) {
            search = "";
        }

        List<ProcessingModel> modelList = repository.getProcessingModelsByName(search);

        List<String> stringList = Lists.newArrayList();
        
        for(ProcessingModel model : modelList){
            
            stringList.add(model.toJson());
            
        }

        String json = new Gson().toJson(stringList, List.class);
        
        PrintWriter out = response.getWriter();
        
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        out.print(json);
        out.flush();

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

            processRequest(server, request, response);

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

        try {
            processRequest(server, request, response);
        } catch (RepositoryException ex) {
            Exceptions.printStackTrace(ex);
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
