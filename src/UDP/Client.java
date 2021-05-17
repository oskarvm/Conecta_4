package UDP;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    /** Client/Jugador que ha d'encertar un numero del ServidorAdivinaUDP_Obj.java -> Comunicació UDP
     *  El servidor no s'aturarà fins que tots els jugadors encertin el número
     *  Exemple de com enviar i rebre objectes en una comunicació UDP
     *  Envia una Jugada i reb un Tauler de puntuacions
     *
     *  Quan s'encerta el número es connecta al socket multicast per obtenir l'estat del joc de la
     *  resta de jugadors
     **/

    private int portDesti;
    private int result;
    private String Nom, ipSrv, marca;
    private int intents;
    private InetAddress adrecaDesti;
    private Tauler t;
    private Jugada j;

    private MulticastSocket multisocket;
    private InetAddress multicastIP;
    boolean continueRunning = true;

    InetSocketAddress groupMulticast;
    NetworkInterface netIf;

    public Client(String ip, int port) {
        this.portDesti = port;
        result = -1;
        intents = 0;
        ipSrv = ip;
        j= new Jugada();
        try {
            multisocket = new MulticastSocket(5557);
            multicastIP = InetAddress.getByName("224.0.0.10");
            groupMulticast = new InetSocketAddress(multicastIP,5557);
            netIf = NetworkInterface.getByName("wlp0s20f3");
            adrecaDesti = InetAddress.getByName(ipSrv);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setNom(String n) {
        Nom=n;
    }

    public int getIntents () {
        return intents;
    }

    public void runClient() throws IOException {
        byte [] receivedData = new byte[1024];
        int tirada;
        DatagramPacket packet;
        DatagramSocket socket = new DatagramSocket();
        //Missatge de benvinguda
        System.out.println("Hola " + Nom + "! Comencem!");
        //Bucle de joc
        while(result!=0) {
            Scanner sc = new Scanner(System.in);
            tirada = sc.nextInt();
            j.Nom = Nom;
            j.num = tirada;
            j.marca = marca;
            //byte[] missatge = ByteBuffer.allocate(4).putInt(n).array();
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(os);
            oos.writeObject(j);
            byte[] missatge = os.toByteArray();

            //creació del paquet a enviar
            packet = new DatagramPacket(missatge, missatge.length, adrecaDesti, portDesti);
            //creació d'un sòcol temporal amb el qual realitzar l'enviament
            //socket = new DatagramSocket();
            //Enviament del missatge
            socket.send(packet);
            //creació del paquet per rebre les dades
            packet = new DatagramPacket(receivedData, 1024);
            //espera de les dades
            socket.setSoTimeout(5000);
            try {
                socket.receive(packet);
                //processament de les dades rebudes i obtenció de la resposta
                result = getDataToRequest(packet.getData(), packet.getLength());
            }catch(SocketTimeoutException e) {
                System.out.println("El servidor no respòn: " + e.getMessage());
                result=-2;
            }

        }
        socket.close();
        //Si és l'últim jugador no cal connexió multicast per saber l'estat del joc perquè
        //el servidor també acaba amb l'encert de l'últim jugador
        if(t.acabats < t.map_jugadors.size()) {
            multisocket.joinGroup(groupMulticast,netIf);
            while (continueRunning) {
                DatagramPacket mpacket = new DatagramPacket(receivedData, 1024);
                multisocket.receive(mpacket);
                continueRunning = printData(mpacket.getData());
            }
            multisocket.leaveGroup(groupMulticast,netIf);
            multisocket.close();
        }
    }

    private void setTauler(byte[] data) {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        try {
            ObjectInputStream ois = new ObjectInputStream(in);
            t = (Tauler) ois.readObject();
            for (int i = 0; i < t.tauler.length; ++i) {
                System.out.print("|");
                for(int j = 0; j < t.tauler[i].length; ++j) {
                    System.out.print(t.tauler[i][j]+"");
                    System.out.print("|");
                }
                System.out.println("");
            }
            System.out.println(" ----------------------------");
            t.map_jugadors.forEach((k,v)-> System.out.println("Tirada de: " + k + "->" + v));
            t.map_jugadors_control_tiradas.forEach((k,v)-> {
                if (t.map_jugadors_control_tiradas.get(k)){
                    System.out.println("Torn de: " + k);
                }
            });
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    private boolean printData(byte[] data) {
        setTauler(data);
        if(t.acabats == t.map_jugadors.size()) return false;
        else return true;

    }

    private int getDataToRequest(byte[] data, int length) {
        setTauler(data);
        String msg = null;
        switch (t.resultat) {
            case 0: msg = "Has guanyat"; break;
            case 1: msg = "Continua jugant"; break;
        }
        System.out.println(msg);
        return t.resultat;
    }

    public static void main(String[] args) {
        String jugador, ipSrv, marca;
        int opcio;
        boolean continuar=true;

        while (continuar) {
            Scanner sip = new Scanner(System.in);
            System.out.println("1- Jugar");
            System.out.println("2- Sortir");
            opcio = sip.nextInt();

            switch (opcio) {
                case 1 -> {
                    //Demanem la ip del servidor i nom del jugador
                    System.out.println("IP del servidor?");
                    ipSrv = sip.next();
                    System.out.println("Nom jugador:");
                    jugador = sip.next();
                    System.out.println("Marca casella:");
                    marca = sip.next();
                    Client cAdivina = new Client(ipSrv, 5556);
                    cAdivina.setNom(jugador);
                    cAdivina.marca = marca;
                    try {
                        cAdivina.runClient();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                case 2 -> continuar = false;
            }
        }

    }

}
