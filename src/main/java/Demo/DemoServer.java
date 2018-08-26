package Demo;

import org.eclipse.jetty.server.Server;

public class DemoServer {
    public static void main(String[] args) throws Exception{
        Server server = new Server(8080);
        server.setHandler(new PropertyHandler());
        server.start();
        server.join();
    }
}
