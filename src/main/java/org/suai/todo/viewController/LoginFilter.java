package org.suai.todo.viewController;
import org.suai.todo.model.*;

import javax.servlet.http.*;
import javax.servlet.*;

import java.io.PrintWriter;

import java.util.ArrayList;
import java.io.IOException;

public class LoginFilter implements Filter {
	private ServletContext servletContext = null;
	private ArrayList<String> acceptNotAuthPaths = null;
	
	@Override
	public void init(FilterConfig config) throws ServletException {
		servletContext = config.getServletContext();
		
		acceptNotAuthPaths = new ArrayList<String>();
		acceptNotAuthPaths.add("");
		acceptNotAuthPaths.add("/");
		acceptNotAuthPaths.add("/login");
		acceptNotAuthPaths.add("/register");

		Model.init(servletContext.getInitParameter("dbfilename"));
	}

    @Override
    public void doFilter(
    		ServletRequest  request, 
    		ServletResponse response, 
    		FilterChain     chain)
	throws ServletException, IOException {
        
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;

		try {
			req.setCharacterEncoding("UTF-8");
 		} catch (Exception err) {  
 			throw new ServletException(err);
 		}
		User user = null;
		HttpSession session = req.getSession(false);

		if (session != null) {
			Integer userId = (Integer)session.getAttribute("userid");
			user = Model.getUser(userId);
    			
		}
		req.setAttribute("user", user);

		String pathInfo = req.getRequestURI().substring(req.getContextPath().length());
		boolean acceptNotAuth = acceptNotAuthPaths.contains(pathInfo);
		
		if (user != null && acceptNotAuth) {
			res.sendRedirect("list?parent=0&page=1");
			return;
		}
		if (!acceptNotAuth) {
			if (user == null) {
				res.sendRedirect("login");
				return;
			}
			Integer parent = getParameterInt(req, "parent");
			Record parentRecord = Model.getTodo(user, parent);
			req.setAttribute("parentRecord", parentRecord);
			
			if (parent > 0 && parentRecord == null) {
				showForbidden(res);
				return;
			}
			Integer id = getParameterInt(req, "id");
			Record record = Model.getTodo(user, id);
			req.setAttribute("record", record);
			
			if (id > 0 && record == null) {
				showForbidden(res);
				return;
			}
		}
		chain.doFilter(request, response);
	}
	
	private Integer getParameterInt(HttpServletRequest req, String key) {
		String v = req.getParameter(key);
		
		if (v != null) {
			try {
				return Math.max(0, Integer.parseInt(v.trim()));
			} catch (NumberFormatException e) {}
        }
        return 0;
    }
    
    private void showForbidden(HttpServletResponse res) throws IOException {
    	res.setStatus(403);
    	res.setContentType("text/html; charset=utf-8");
		res.setHeader("Cache-Control", "no-cache");
		
		PrintWriter out = res.getWriter();
		out.print("<h1>403 Forbidden</h1>");
    }
}
