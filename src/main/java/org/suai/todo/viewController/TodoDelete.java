package org.suai.todo.viewController;
import org.suai.todo.model.*;

import javax.servlet.http.*;
import javax.servlet.*;

import java.io.IOException;

public class TodoDelete extends TodoBase {
	@Override
	public void doGet(
			HttpServletRequest req, 
			HttpServletResponse res) 
	throws ServletException, IOException {
	
		Record record  = (Record)req.getAttribute("record");
		Integer parent = getParameterInt(req, "parent", 0);
		Integer page   = getParameterInt(req, "page", 1);
		
		Model.delete(record);
		
		redirect(res, "list?parent=" + parent + "&page=" + page);
	}
}
