package Demo;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;

public class DemoServer {
    public static void main(String[] args) throws Exception{
        Server server = new Server(8080);

        ContextHandler root = new ContextHandler("/");
        root.setHandler(new FrontEndHandler());
        ContextHandler api = new ContextHandler("/api");
        api.setHandler(new PropertyHandler());

        ContextHandlerCollection handlerCollection= new ContextHandlerCollection();
        handlerCollection.setHandlers(new Handler[]{root, api});

        server.setHandler(handlerCollection);
        server.start();
        server.join();
    }
}
