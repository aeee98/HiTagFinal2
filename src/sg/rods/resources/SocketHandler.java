package sg.rods.resources;

import java.net.Socket;

/**
 * Created by Lee on 3/8/2015.
 */
public class SocketHandler {
    private static Socket socket;

    public static synchronized Socket getSocket(){
        return socket;
    }

    public static synchronized void setSocket(Socket socket){
        SocketHandler.socket = socket;
    }

}
