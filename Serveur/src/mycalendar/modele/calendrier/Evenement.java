package mycalendar.modele.calendrier;

import mycalendar.modele.bdd.GestionnaireBDD;
import mycalendar.modele.droits.Admin;
import mycalendar.modele.droits.Droit;
import mycalendar.modele.utilisateur.Utilisateur;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Observable;

public abstract class Evenement extends Observable {

    private int idEv;
    private int calendrierID;
    private String nomE;
    private String description;
    private String image;
    private Date date;
    private String lieu;
    private String auteur;

    protected boolean visibilite;

    private ArrayList<Message> messages;
    private ArrayList<Droit> droits;

    /**
     * Constructeur d'un événement
     *
     * @param id
     * @param calID
     * @param nom
     * @param description
     * @param image
     * @param date
     * @param lieu
     * @param auteur
     */
    public Evenement(int id, int calID, String nom, String description, String image, Date date, String lieu, String auteur) {
        this.idEv = id;
        this.calendrierID = calID;
        this.nomE = nom;
        this.description = description;
        this.image = image;
        this.date = date;
        this.lieu = lieu;
        this.auteur = auteur;
        this.messages = new ArrayList<>();
        this.droits = new ArrayList<>();
        droits.add(new Admin());
    }

    public void prevenirVues() {
        this.setChanged();
        this.notifyObservers();
    }

    /**
     * Méthode de sauvegarde d'un événement dans la BDD
     *
     * @return true si la sauvegarde s'est bien passé, false sinon
     * @throws SQLException
     */
    public boolean save() throws SQLException {
        Connection connect = GestionnaireBDD.getInstance().getConnection();
        {
            String request = "INSERT INTO Evenement VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";
            PreparedStatement prep = connect.prepareStatement(request);
            prep.setInt(1, this.idEv);
            prep.setInt(2, this.calendrierID);
            prep.setString(3, this.nomE);
            java.sql.Timestamp t = new java.sql.Timestamp(this.date.getTime());
            prep.setTimestamp(4, t);
            prep.setString(5, this.description);
            prep.setString(6, this.image);
            prep.setString(7, this.lieu);
            prep.setString(8, this.auteur);
            prep.setBoolean(9, this.visibilite);
            if (prep.executeUpdate() == 0) { // Pas de nouvelles lignes insérées lors de l'exécution de la requête, il y a donc un problème
                return false;
            }

        }
        return true;
    }

    /**
     * Méthode permettant de récupérer la liste des événements présent dans un calendrier
     *
     * @param idc l'id du calendrier
     * @param Email email de l'auteur
     * @return liste des événements
     * @throws SQLException
     */
    public static ArrayList<Evenement> find(int idc, String Email) throws SQLException {
        Connection connect = GestionnaireBDD.getInstance().getConnection();
        ArrayList<Evenement> events = new ArrayList<>();
        {
            String request = "SELECT * FROM Evenement WHERE idC=? and auteur = ?;";
            PreparedStatement prep = connect.prepareStatement(request);
            prep.setInt(1, idc);
            prep.setString(2,Email);
            prep.execute();
            ResultSet rs = prep.getResultSet();
            if (rs.next()) {
                if (rs.getBoolean("idc")) {
                    events.add(new EvenementPublic(rs.getInt("ide"), rs.getInt("idc"), rs.getString("nomE"), rs.getString("description"), rs.getString("image"), rs.getDate("dateE"), rs.getString("lieu"), rs.getString("auteur")));
                } else {
                    events.add(new EvenementPrive(rs.getInt("ide"), rs.getInt("idc"), rs.getString("nomE"), rs.getString("description"), rs.getString("image"), rs.getDate("dateE"), rs.getString("lieu"), rs.getString("auteur")));
                }
            }
        }
        return events;
    }

    /**
     * Récupère l'ID de l'événement le plus élevé
     *
     * @return
     * @throws SQLException
     */
    public static int getHighestID() throws SQLException {
        Connection connect = GestionnaireBDD.getInstance().getConnection();
        {
            String request = "SELECT MAX(ide) AS max FROM Evenement;";
            PreparedStatement prep = connect.prepareStatement(request);
            prep.execute();
            ResultSet rs = prep.getResultSet();
            if (rs.next()) {
                return rs.getInt("max");
            }
            return -1;
        }
    }

    /**
     * Méthode de suppression d'un événement dans la BDD
     *
     * @return false si erreur lors de l'exécution de la requête, true sinon
     * @throws SQLException
     */
    public boolean delete() throws SQLException {
        Connection connect = GestionnaireBDD.getInstance().getConnection();
        {
            String request = "DELETE FROM Evenement WHERE ide=? AND idc=? AND auteur=? AND nomE=?;";
            PreparedStatement prep = connect.prepareStatement(request);
            prep.setInt(1, this.idEv);
            prep.setInt(2, this.calendrierID);
            prep.setString(3, this.auteur);
            prep.setString(4, this.nomE);
            if (prep.executeUpdate() == 0) { // Pas de nouvelles lignes insérées lors de l'exécution de la requête, il y a donc un problème
                return false;
            }
        }
        return true;
    }

    /**
     * Méthode récupérant la liste des participant d'un événement
     *
     * @return La liste des utilisateurs participant à l'événement
     * @throws SQLException
     */
    public ArrayList<Utilisateur> findInvites() throws SQLException {
        ArrayList<Utilisateur> alUsrs = new ArrayList<>();
        Connection connect = GestionnaireBDD.getInstance().getConnection();
        {
            String request = "SELECT Email FROM utilisateur_evenement WHERE ide=?;";
            PreparedStatement prep = connect.prepareStatement(request);
            prep.setInt(1, this.idEv);
            prep.execute();
            ResultSet rs = prep.getResultSet();
            while (rs.next()) {
                request = "SELECT Email, nom, mdp, prenom FROM Utilisateur WHERE Email=?;";
                prep = connect.prepareStatement(request);
                prep.setString(1, rs.getString("Email"));
                prep.execute();
                ResultSet rsd = prep.getResultSet();
                alUsrs.add(new Utilisateur(rsd.getString("Email"), rsd.getString("nom"), rsd.getString("mdp"), rsd.getString("prenom")));
            }
        }
        return alUsrs;
    }

    /**
    * Méthode qui retourne l'id de l'événement
    * @return id de l'événement
    */
    public int getId(){return idEv;}

    /**
    * Méthode qui retourne un booléen concernant les droits d'administration de l'événement
    * @return booleen sur le droit d'admin
    */

    public boolean getAdmin() {
        return droits.get(0).getDroit();
    }

    /**
     * Méthode de consultaion d'un événement
     * @return Une HashMap contenant les données de l'événement
     */
    public HashMap<String, String> consult(){
        HashMap<String, String> res = new HashMap<>();
        res.put("nomE", nomE);
        res.put("description", description);
        res.put("image", image);
        res.put("date", date.toString());
        res.put("lieu", lieu);
        res.put("auteur", auteur);
        return res;
    }

    /**
     * Méthode de modification d'un événement dans la BDD
     * @return false si erreur lors de l'exécution de la requête, true sinon
     * @throws SQLException
     */
    public boolean modify(int calendrierID, String nomE, String description, String image, Date date, String lieu, String auteur) throws SQLException {
        this.calendrierID=calendrierID;
        this.nomE=nomE;
        this.description=description;
        this.image=image;
        this.date=date;
        this.lieu=lieu;
        this.auteur=auteur;
        return save();
    }
}
