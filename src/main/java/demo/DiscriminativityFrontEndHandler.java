package demo;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Scanner;

public class DiscriminativityFrontEndHandler extends AbstractHandler {
    private final String indexFolderLocation;
    public DiscriminativityFrontEndHandler(String indexFolderLocation){
        this.indexFolderLocation = indexFolderLocation;
    }

    @Override
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("text/html");
        InputStream form = getClass().getClassLoader().getResourceAsStream("propertyForm.html");
        out.write(new Scanner(form).useDelimiter("\\A").next());
        baseRequest.setAttribute("format", "HTML");


        // If a discriminativity query has been made, writes the response
        new DiscriminativityHandler(indexFolderLocation).handle(target, baseRequest, request, response);
        baseRequest.setHandled(true);

    }
}