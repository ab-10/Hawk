package Demo;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;

public class DemoServer {
    public static void main(String[] args) throws Exception{
        // Location of the index folder relative to the location from which the program is run
        String indexFolderLocation;
        try {
            indexFolderLocation = args[0];
        }catch (ArrayIndexOutOfBoundsException e){
            System.out.println("Please provide the path to the 'indexes' folder" +
                    " relative to the location from which the script is run");
            return;
        }

        Server server = new Server(8080);

        ContextHandler root = new ContextHandler("/");
        root.setHandler(new FrontEndHandler(indexFolderLocation));
        ContextHandler api = new ContextHandler("/api");
        api.setHandler(new PropertyHandler(indexFolderLocation));

        ContextHandlerCollection handlerCollection= new ContextHandlerCollection();
        handlerCollection.setHandlers(new Handler[]{root, api});

        server.setHandler(handlerCollection);
        server.start();
        server.join();
    }
}
