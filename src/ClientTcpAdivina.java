import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientTcpAdivina extends Thread {
    /* CLient TCP que ha endevinar un número pensat per SrvTcpAdivina.java */

    String hostname;
    int port;
    boolean continueConnected;

    public ClientTcpAdivina(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
        continueConnected = true;
    }

    public void run() {
        List<Integer> serverData;

        Socket socket;
        ObjectInputStream in;
        ObjectOutputStream out;

        try {
            socket = new Socket(InetAddress.getByName(hostname), port);
            in = new ObjectInputStream(socket.getInputStream());
            out = new ObjectOutputStream(socket.getOutputStream());
            //el client atén el port fins que decideix finalitzar
            while(continueConnected){
                List<Integer> integerList = new ArrayList<>();
                integerList.add(0);
                integerList.add(1);
                integerList.add(18);
                integerList.add(5);
                integerList.add(3);
                integerList.add(9);

                //enviament el número i els intents
                out.writeObject(integerList);
                out.flush();

                //processament de les dades rebudes i obtenció d'una nova petició
                serverData = (List<Integer>) in.readObject();
                getRequest(serverData);
            }
            close(socket);

        } catch (UnknownHostException ex) {
            System.out.println("Error de connexió. No existeix el host: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("Error de connexió indefinit: " + ex.getMessage());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    public void getRequest(List<Integer> serverData) {
        System.out.println(serverData.toString());
        continueConnected = false;
    }

    private void close(Socket socket){
        try {
            if(socket!=null && !socket.isClosed()){
                if(!socket.isInputShutdown()){
                    socket.shutdownInput();
                }
                if(!socket.isOutputShutdown()){
                    socket.shutdownOutput();
                }
                socket.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(ClientTcpAdivina.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {


        ClientTcpAdivina clientTcp = new ClientTcpAdivina("localhost",5558);
        clientTcp.start();
    }
}
