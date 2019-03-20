package mycalendar.modele.clientTest;

import mycalendar.modele.serveur.ApplicationServeur;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class  ClientTest {
    private static void lancerClient(String requete, boolean post) throws IOException {
        // Création socket
        int port = 3307;
        InetAddress ip = InetAddress.getByName(ApplicationServeur.URL);

        Socket socket = new Socket(ip, port);
        PrintWriter pred = new PrintWriter(
                new BufferedWriter(
                        new OutputStreamWriter(
                                socket.getOutputStream()
                        )
                ),true
        );

        BufferedReader bos = new BufferedReader(
                new InputStreamReader(
                        socket.getInputStream()));

        String request = "";
        if(post){
            request = "POST / HTTP/1.1\n\r\n"+requete;
        }else{
            request = "GET "+requete+" HTTP/1.1";
        }
        System.out.println("DONNEE ENVOYEE : " + request);
        pred.println(request);

        StringBuilder line = new StringBuilder();
        line.append("\n\n");
        String l = bos.readLine();
        while(l != null){
            line.append(l+"\n");
            l = bos.readLine();
        }
        System.out.println("DONNEE RECUES :"+line.toString());
        bos.close();
        pred.close();
    }

    public static void main(String[] args) {
        try{
            //lancerClient("{\"Request\":\"TransfertEventOwnership\",\"Member\":\"squeeze@my.hog\",\"Owner\":\"test@test\"," +
             //       "\"EventName\":\"azeaze\"}", true);
            lancerClient("{\"Request\":\"AddFriend\",\"Email1\":\"util@util\",\"Email2\":\"test@test\"}",false);
           /* DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            Date dateP = dateFormat.parse("19/03/2019 16:15");
            System.out.println(dateP.toString());*/
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
