package mycalendar.modele.serveur;

import mycalendar.modele.bdd.GestionnaireBDD;
import mycalendar.modele.calendrier.Message;
import mycalendar.modele.exceptions.BadRequestExeption;
import mycalendar.modele.exceptions.MessageCodeException;
import mycalendar.modele.exceptions.NoRequestException;
import mycalendar.modele.utilisateur.Utilisateur;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Classe qui correspond à une connexion d'un client
 */
public class ConnexionClient implements Runnable{

    // Socket du client et In Out
    private Socket socket;
    private PrintWriter pred;
    private BufferedReader bos;

    // Méthode de la requête et version http
    private String httpVersion;
    private String method;

    /**
     * Constructeur
     * @param socket socket du client
     */
    public ConnexionClient(Socket socket){
        this.socket = socket;
    }

    /**
     * Méthode qui s'effectue lorsqu'un client a été trouvé
     */
    @Override
    public void run(){
        try {
            System.out.println("\n-------------------------\nNOUVEAU CLIENT");

            System.out.println("INITIALISATION");

            // Création de la connexion
            GestionnaireBDD.getInstance().createConnection();

            // Permet de lire les données
            bos = new BufferedReader(
                    new InputStreamReader(
                            socket.getInputStream()));
            // Permet de répondre au client
            pred = new PrintWriter(
                    new BufferedWriter(
                            new OutputStreamWriter(
                                    socket.getOutputStream()
                            )
                    ), true
            );

            // Formatage de la requête
            HashMap<String, String> donnees = formateRequest();

            // Création de la réponse
            String reponse = createReponse(donnees);

            System.out.println("DONNEE ENVOYEE : " + reponse);
            // Envoi de la réponse
            sendResponse(reponse);

            // Fermeture des objets
            closeAll();

            System.out.println("FIN CLIENT\n-------------------------\n");
        }catch (BadRequestExeption e){
            System.out.println("MAUVAISE REQUETE");
        }catch (NoRequestException e){
            System.out.println("REQUETE INEXISTANTE");
        }catch (SQLException e){
            System.out.println("PROBLEME BDD");
        }catch (IOException e){
            System.out.println("PROBLEME In/Out");
        }catch (Exception e){
            System.out.println("ERREUR CLIENT : \r\t");
            System.out.println(e.getMessage());
        }finally {
            closeAll();
        }
    }

    /**
     * Méthode qui permet de formater la requete du client
     * @return une hashmap contenant la requête
     * @throws IOException
     */
    private HashMap<String, String> formateRequest() throws IOException {
        // Formatage de la requête
        String requete = bos.readLine();
        StringBuilder json = new StringBuilder();
        String[] lignes;

        // Récupération de la version de http
        String[] tab = requete.split(" ");
        httpVersion = tab[tab.length-1];
        // Récupération de la méthode
        method = tab[0];

        if (method.equals("GET")){
            // Si c'est un get, on récupère les paramètres dans le header
            json.append(requete.substring(4,requete.length()-8).
                    replaceAll("%22", "\"").
                    replace("/?{", "{"));
        }else if(method.equals("POST")){
            // Lecture du body
            ArrayList<Byte> data = new ArrayList<Byte>();
            // tant qu'il y en a
            while(bos.ready()){
                // on lie les caractères
                data.add((byte)bos.read());
            }
            // ensuite, on les place dans un tableau
            byte[] cbo = new byte[data.size()];
            for(int i = 0 ; i < data.size(); i++){
                cbo[i] = data.get(i);
            }
            // puis on les transforme en chaîne de caractères
            requete = new String(cbo, Charset.defaultCharset());
            lignes = requete.split("\n");
            boolean trouve = false;
            for(int i = 0; i < lignes.length; i++){
                if(lignes[i].equals("\r")){
                    trouve = true;
                }
                if(trouve){
                    json.append(lignes[i]);
                }
            }
            if(!trouve){
                throw new BadRequestExeption("Body non trouvé");
            }
            System.out.printf("json :"+json.toString());
        }


        System.out.println("DONNEE RECUE : " + json.toString());

        HashMap<String, String> donnees = ParseurJson.getInstance().decode(json.toString());

        if (!donnees.containsKey("Request")) {
            bos.close();
            socket.close();
            throw new NoRequestException();
        }

        // Décodage de la chaîne JSON
        return donnees;
    }

    /**
     * Méthode permettant de créer la reponse
     * @param donnees données fournies par l'utilisateur
     * @return Chaîne à renvoyer au client
     * @throws BadRequestExeption
     * @throws SQLException
     */
    private String createReponse(HashMap<String, String> donnees) throws BadRequestExeption, SQLException {
        String result = "";
        HashMap<String, String> rep = new HashMap<>();
        HashMap<String, Object> repObj;
        ParseurJson parseur = ParseurJson.getInstance();
        String email = donnees.get("Email");
        String mdp = donnees.get("Mdp");
        String request = donnees.get("Request");
        // Traitement de la requête
        switch (request) {
            // Authentification
            case "SignIn": {
                rep = ApplicationServeur.getInstance().authentification(
                        email,
                        mdp
                );
                result = parseur.encode(rep);
                break;
            }
            // Inscription
            case "SignUp": {
                rep = ApplicationServeur.getInstance().inscription(
                        email,
                        mdp,
                        donnees.get("Prenom"),
                        donnees.get("Nom")
                );
                result = parseur.encode(rep);
                break;
            }
            default: {
                // Vérification de l'existence d'un utilisateur
                if (Verification.checkPassword(email, mdp)) {
                    switch (request) {
                        // Ajout d'un événement
                        case "AddEvent": {
                            String calendarName = donnees.get("CalendarName");
                            String eventName = donnees.get("EventName");
                            String eventDescription = donnees.get("EventDescription");
                            String image = "";
                            String eventDateDeb = donnees.get("EventDate");
                            String eventDateFin = donnees.get("EventDateFin");
                            String eventLocation = donnees.get("EventLocation");
                            String eventColor = donnees.get("EventColor");
                            if (Verification.checkCalendarByName(email, calendarName)) {
                                boolean eventVisibility = Boolean.parseBoolean(donnees.get("EventVisibility"));
                                rep = ApplicationServeur.getInstance().creationEvenement(calendarName, eventName, eventDescription,
                                        image, eventDateDeb, eventDateFin, eventLocation, eventColor, email, eventVisibility);
                            } else {
                                MessageCodeException.calendar_not_found(rep);
                            }
                            result = parseur.encode(rep);
                            break;
                        }
                        // Suppression d'un événement
                        case "DeleteEvent": {
                            int idEv = Integer.parseInt(donnees.get("ID"));
                            if (Verification.checkEvent(email, idEv)) {
                                rep = ApplicationServeur.getInstance().suppressionEvenement(idEv);
                            } else {
                                MessageCodeException.event_not_found(rep);
                            }
                            result = parseur.encode(rep);
                            break;
                        }
                        // Modification d'un événement
                        case "ModifyEvent": {
                            int idevent = Integer.parseInt(donnees.get("IdEvent"));
                            String idCalendar = donnees.get("IdCalendar");
                            String eventName = donnees.get("EventName");
                            String eventDescription = donnees.get("EventDescription");
                            String eventPicture = donnees.get("EventPicture");
                            String eventDate = donnees.get("EventDate");
                            String eventDateFin = donnees.get("EventDateFin");
                            String eventLocation = donnees.get("EventLocation");
                            String eventColor = donnees.get("EventColor");
                            if (Verification.checkEvent(email, idevent)) {
                                rep = ApplicationServeur.getInstance().modificationEvenement
                                        (idevent, Integer.parseInt(idCalendar), eventName, eventDescription, eventPicture,
                                                eventDate, eventDateFin, eventLocation, eventColor, email);
                            } else {
                                MessageCodeException.event_not_found(rep);
                            }
                            result = parseur.encode(rep);
                            break;
                        }
                        // Consultation d'un événement
                        case "ConsultEvent": {
                            String idevent = donnees.get("IdEvent");
                            if (Verification.checkEvent(email, Integer.parseInt(idevent))) {
                                rep = ApplicationServeur.getInstance().consultationEvenement(idevent);
                            } else {
                                MessageCodeException.event_not_found(rep);
                            }
                            result = parseur.encode(rep);
                            break;
                        }
                        // Consultation d'un calendrier
                        case "CreateCalendar": {
                            String nom = donnees.get("Nom");
                            String desc = donnees.get("Description");
                            String couleur = donnees.get("Couleur");
                            String theme = donnees.get("Theme");
                            rep = ApplicationServeur.getInstance().creationCalendrier(nom, desc, couleur, theme, email);
                            result = parseur.encode(rep);
                            break;
                        }
                        case "DeleteCalendar": {
                            int idCalendar = Integer.parseInt(donnees.get("IDCalendar"));
                            boolean b = Boolean.parseBoolean(donnees.get("SuppEv"));
                            if (Verification.checkCalendar(email, idCalendar)) {
                                rep = ApplicationServeur.getInstance().suppressionCalendrier(email, idCalendar, b);
                            } else {
                                MessageCodeException.calendar_not_found(rep);
                            }
                            result = parseur.encode(rep);
                            break;
                        }
                        case "ModifyCalendar": {
                            int idCalendar = Integer.parseInt(donnees.get("IdCalendar"));
                            String nom = donnees.get("Nom");
                            String couleur = donnees.get("Couleur");
                            String theme = donnees.get("Theme");
                            String description = donnees.get("Description");
                            if (Verification.checkCalendar(email, idCalendar)) {
                                rep = ApplicationServeur.getInstance().modificationCalendrier(
                                        idCalendar,
                                        nom,
                                        couleur,
                                        theme,
                                        description
                                );
                            } else {
                                MessageCodeException.calendar_not_found(rep);
                            }
                            result = parseur.encode(rep);
                            break;
                        }
                        case "AddFriend": {
                            String email2 = donnees.get("Email2");
                            rep = ApplicationServeur.getInstance().ajoutAmi(email, email2);
                            result = parseur.encode(rep);
                            break;
                        }
                        // Récupération de plusieurs utilisateurs
                        case "GetUsers": {
                            String nom = donnees.get("FirstName");
                            String prenom = donnees.get("LastName");
                            repObj = ApplicationServeur.getInstance().getUtilisateurs(nom, prenom);
                            result = parseur.encodeObj(repObj);
                            break;
                        }
                        // Chargement d'un calendrier et de ses événements
                        case "ConsultCalendar": {
                            int id = Integer.parseInt(donnees.get("ID"));
                            if (Verification.checkCalendar(email, id)) {
                                rep = ApplicationServeur.getInstance().consultCalendar(
                                        id
                                );
                            } else {
                                MessageCodeException.calendar_not_found(rep);
                            }
                            result = parseur.encode(rep);
                            break;
                        }
                        // Chargement de la liste des calendriers d'un utilisateur
                        case "LoadCalendars": {
                            repObj = ApplicationServeur.getInstance().loadCalendars(
                                    email
                            );
                            result = parseur.encodeObj(repObj);
                            break;
                        }
                        case "GetTheme": {
                            repObj = ApplicationServeur.getInstance().getThemes();
                            result = parseur.encodeObj(repObj);
                            break;
                        }
                        case "LoadEvents": {
                            String cal = donnees.get("CalendarName");
                            if (Verification.checkCalendarByName(email, cal)) {
                                repObj = ApplicationServeur.getInstance().loadEvents(email, cal);
                            } else {
                                repObj = new HashMap<>();
                                repObj.put("Result", MessageCodeException.C_NOT_FOUND);
                                repObj.put("Message", MessageCodeException.M_CALENDAR_NOT_FOUND);
                            }
                            result = parseur.encodeObj(repObj);
                            break;
                        }
                        case "TransfertEventOwnership": {
                            String member = donnees.get("Member");
                            String event = donnees.get("EventName");
                            if (Verification.checkEventByName(email, event)) {
                                rep = ApplicationServeur.getInstance().transfererPropriete(member, email, event);
                            } else {
                                MessageCodeException.event_not_found(rep);
                            }
                            result = parseur.encode(rep);
                            break;
                        }
                        case "TransfertCalendarOwnership": {
                            String calendarName = donnees.get("Calendar");
                            String newOwner = donnees.get("NewOwner");
                            if (Verification.checkEventByName(email, calendarName)) {
                                rep = ApplicationServeur.getInstance().modifAdminCalend(calendarName, email, newOwner);
                            } else {
                                MessageCodeException.calendar_not_found(rep);
                            }
                            result = parseur.encode(rep);
                            break;
                        }
                        case "ModifyAccount": {
                            String nom = donnees.get("Nom");
                            String prenom = donnees.get("Prenom");
                            rep = ApplicationServeur.getInstance().modifierCompte(email, nom, prenom, mdp);
                            result = parseur.encode(rep);
                            break;
                        }
                        case "ModifyGroup":
                        {
                            int idGroup = Integer.parseInt(donnees.get("GroupId"));
                            String newName = donnees.get("NewName");
                            ArrayList<String> liste = ParseurJson.getInstance().getUsers(donnees);
                            // Vérification de l'existence du groupe pour cet utilisateur
                            if(Verification.checkGroup(email, idGroup)){
                                rep = ApplicationServeur.getInstance().modifierGroupe(email, idGroup, newName, liste);
                            }else{
                                MessageCodeException.group_not_found(rep);
                            }
                            result = parseur.encode(rep);
                            break;
                        }
                        default: {
                            throw new BadRequestExeption(donnees.get("Request"));
                        }
                    }
                } else {
                    MessageCodeException.user_not_found(rep);
                    result = parseur.encode(rep);
                }
            }
        }
        return result;
    }

    /**
     * Méthode qui permet d'envoyer une réponse au client
     * @param reponse la réponse
     */
    private void sendResponse(String reponse){
        // Réponse
        StringBuilder httpResponse = new StringBuilder();
        httpResponse.append(httpVersion+" 200 OK\n");
        httpResponse.append("Connection: Closed\n");
        httpResponse.append("Content-Type: json\n\n");
        httpResponse.append(reponse);

        // Envoi
        pred.println(httpResponse.toString());

        // Fermeture
        closeAll();
    }

    /**
     * Méthode qui permet de fermer tous les objets
     */
    private void closeAll(){
        // Fermeture des objets
        try {
            pred.close();
            bos.close();
            socket.close();
            GestionnaireBDD.getInstance().closeConnection();
        }catch (IOException e){
            System.out.println("Problème lors de la fermeture d'un In/Out côté client");
        }catch (SQLException e){
            System.out.println("Problème lors de la fermeture de la connexion à la bdd");
        }
    }
}
