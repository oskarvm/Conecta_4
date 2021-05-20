package UDP;

import java.io.*;
import java.net.*;

public class Servidor {
    /** Servidor que es pensa un número i ClientAdivinaUDP_Obj.java l'ha d'encertar.
     ** Comunicació UDP amb transmissió d'objectes.
     ** Reb un Jugada i envia un Tauler de puntuacions
     ** Accepta varis clients i es tanca la comunicació quan tots els clients ja hagin encertat el número.
     **/

    DatagramSocket socket;
    int port, fi, acabats, multiport=5557;
    boolean acabat;
    Tauler tauler;
    MulticastSocket multisocket;
    InetAddress multicastIp;

    public Servidor(int port, int max) {
        try {
            socket = new DatagramSocket(port);
            multisocket = new MulticastSocket(multiport);
            multicastIp = InetAddress.getByName("224.0.0.10");
            NetworkInterface.networkInterfaces().forEach(i -> System.out.println(i.toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.port = port;
        tauler = new Tauler();
        acabat = false;
        acabats = 0;
        fi=-1;
    }

    public void runServer() throws IOException{
        byte [] receivingData = new byte[1024];
        byte [] sendingData;
        InetAddress clientIP;
        int clientPort;

        //el servidor atén el port mentre hi hagi jugadors
        while(acabats < tauler.map_jugadors.size() || acabats==0){
            DatagramPacket packet = new DatagramPacket(receivingData, receivingData.length);
            socket.receive(packet);
            sendingData = processData(packet.getData(), packet.getLength());
            clientIP = packet.getAddress();
            clientPort = packet.getPort();
            packet = new DatagramPacket(sendingData, sendingData.length,
                    clientIP, clientPort);
            socket.send(packet);
            //A cada jugada també enviem les dades del tauler per multicast
            //perquè el clients que ja hagin acabat puguin seguir el jic
            DatagramPacket multipacket = new DatagramPacket(sendingData, sendingData.length,
                    multicastIp,multiport);
            multisocket.send(multipacket);
            }
        socket.close();
    }

    //Processar la Jugada: Nom i numero
    private byte[] processData(byte[] data, int length) {
        Jugada j = null;
        ByteArrayInputStream in = new ByteArrayInputStream(data);

        try {
            ObjectInputStream ois = new ObjectInputStream(in);
            j = (Jugada) ois.readObject();
            System.out.println("jugada:" + j.Nom + " " + j.num);

            if(!tauler.map_jugadors.containsKey(j.Nom)) {
                if (tauler.map_jugadors.size()%2 == 0){
                    tauler.map_jugadors.put(j.Nom, 1);
                }else tauler.map_jugadors.put(j.Nom, 0);
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        if(tauler.num_tiradas % 2 == tauler.map_jugadors.get(j.Nom)){
            //afegir al tauler
            if (j.num<8 && j.num>0){
                for (int i=tauler.tauler.length-1; i>=0;i--) {
                    if (tauler.tauler[i][j.num - 1].equals(" ~ ")) {
                        tauler.tauler[i][j.num - 1] = " "+j.marca+" ";
                        tauler.num_tiradas++;
                        break;
                    }
                }
            }
        }

        //comprovació
        boolean guanyador = verificarguanyador(j);
        for (int m = 0; m < tauler.tauler.length; m++) {
        }
        if (guanyador){ tauler.resultat =0;}
        else {tauler.resultat = 1;}

        //La resposta és el tauler amb les dades de tots els jugadors
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(os);
            oos.writeObject(tauler);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] resposta = os.toByteArray();
        return resposta;
    }

    private boolean verificarguanyador(Jugada x) {

        for (int i = 1; i < tauler.tauler.length; i++) { //horizontal
            for (int m = 0; m < 6-2; m++) {
                if (tauler.tauler[i][m].equals(" "+ x.marca + " ") && tauler.tauler[i][m + 1].equals(" "+ x.marca + " ") && tauler.tauler[i][m + 2].equals(" "+ x.marca + " ") && tauler.tauler[i][m + 3].equals(" "+ x.marca + " ")) {
                    return true;
                }
            }
        }
        for (int i = 0; i < tauler.tauler.length; i++) {//vertical
            for (int j = 0; j < 6-2; j++) {
                if (tauler.tauler[j][i].equals(" "+ x.marca + " ") && tauler.tauler[j + 1][i].equals(" "+ x.marca + " ") && tauler.tauler[j + 2][i].equals(" "+ x.marca + " ") && tauler.tauler[j + 3][i].equals(" "+ x.marca + " ")) {
                    return true;
                }
            }
        }
        for (int i = 0; i < tauler.tauler.length - 4 + 1; i++) { //diagonal
            for (int j = 0; j < 6-3+1; j++) {
                if (tauler.tauler[j][i].equals(" "+ x.marca + " ") && tauler.tauler[j+1][i+1].equals(" "+ x.marca + " ") && tauler.tauler[j+2][i+2].equals(" "+ x.marca + " ") && tauler.tauler[j+3][i+3].equals(" "+ x.marca + " ")) {
                    return true;
                }
            }
        }
        for (int i = tauler.tauler.length; i > 3; i--) { //diagonal 2
            for (int j = 0; j < 6-2; j++) {
                if (tauler.tauler[j][i - 1].equals(" "+ x.marca + " ") && tauler.tauler[j + 1][i - 2].equals(" "+ x.marca + " ") && tauler.tauler[j + 2][i - 3].equals(" "+ x.marca + " ") && tauler.tauler[j + 3][i - 4].equals(" "+ x.marca + " ")) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void main(String[] args) throws SocketException, IOException {
        Servidor sAdivina = new Servidor(5556, 100);

        try {
            sAdivina.runServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Fi Servidor");



    }

}
