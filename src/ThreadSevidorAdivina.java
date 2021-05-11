import java.io.*;
import java.net.Socket;
import java.util.Collections;
import java.util.List;

public class ThreadSevidorAdivina implements Runnable {
    /* Thread que gestiona la comunicació de SrvTcPAdivina.java i un cllient ClientTcpAdivina.java */

    Socket clientSocket = null;
    ObjectInputStream in;
    ObjectOutputStream out;
    List<Integer> msgEntrant, msgSortint;
    boolean acabat;

    public ThreadSevidorAdivina(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        acabat = false;
        out= new ObjectOutputStream(clientSocket.getOutputStream());
        in = new ObjectInputStream(clientSocket.getInputStream());
    }

    @Override
    public void run() {
        try {
            while(!acabat) {
                msgEntrant = (List<Integer>) in.readObject();

                msgSortint = generaResposta(msgEntrant);

                out.writeObject(msgSortint);
                out.flush();
            }
        }catch(IOException | ClassNotFoundException e){
            System.out.println(e.getLocalizedMessage());
        }
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Integer> generaResposta(List<Integer> en) {
        List<Integer> listaOrdenada;
        Collections.sort(en);
        listaOrdenada = en;
        return listaOrdenada;
    }

}