package tp1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Scanner;
import java.util.StringTokenizer;

public class Client {

	private static final String CHEMIN_CLIENT = "./tp1/FichiersClient/";
	private String hote;
	private int port;
	private Socket cliSocket;
	private DataInputStream dIn;
	private DataOutputStream dOut;

	public Client(String h, int p) {
		hote = h;
		port = p;
	}

	public void initierConnexion() {
		try {
			cliSocket = new Socket(hote, port);
			System.out.println("C : connecté au serveur");

			dIn = new DataInputStream(cliSocket.getInputStream());
			dOut = new DataOutputStream(cliSocket.getOutputStream());
			System.out.println("C : flux binaires ouverts");
		} catch (UnknownHostException e) {
			System.err.println("C : erreur de connection au serveur, hôte inconnu");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("C : erreur d'ouverture des flux");
			e.printStackTrace();
		}
	}

	public void seDeconnecter() {
		try {
			dIn.close();
			dOut.close();
			cliSocket.close();
			System.out.println("C : déconnexion du serveur");
		} catch (IOException e) {
			System.err.println("C : erreur lors de la déconnexion");
			e.printStackTrace();
		}

	}

	public void attendreConfirmationConnexion() {
		try {

			String message = "";
			while (message.equals("")) {
				message = dIn.readUTF();
				System.out.println(message);
			}

		} catch (Exception e) {

		}
	}

	public void envoyerDemande() {
		Scanner sc = new Scanner(System.in);
		String reponse = sc.nextLine();
		StringTokenizer string = new StringTokenizer(reponse, " ");

		String dem = string.nextToken();

		if (dem.equals("PUT")) {
			// Commande PUT
			String filePath = string.nextToken();

			System.out.println("C : Demande de commande PUT sur le fichier : " + filePath);
			File file = new File(filePath);
			if (file.exists()) {
				System.out.println("Le fichier fait : " + file.length() + " Octets ");
				try {
					String ext;
					StringTokenizer str = new StringTokenizer(filePath, ".");
					ext = str.nextToken();
					ext = str.nextToken();
					String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
					dOut.writeUTF(file.length() + " " + timeStamp + "." + ext);
					attendreReception(file);
					return;

				} catch (Exception e) {

				}
			} else {
				System.out.println("Le fichier n'existe pas");
				return;
			}

		}
		if (dem.equals("LIST")) {
			String repServ;
			if (string.hasMoreTokens()) {
				repServ = envoyerViaSocket("REPERTOIRE " + string.nextToken());

			} else {
				repServ = envoyerViaSocket("REPERTOIRE");

			}
			AfficherRep(repServ);
			return;

		}
		if (dem.equals("GET")) {
			String repServ = envoyerViaSocket("REPERTOIRE");
			AfficherRep(repServ);
			DemanderFichierGet();
			return;
		}
		System.out.println("C : Erreur sur le message -> Exit");
		return;

	}

	public void DemanderFichierGet() {
		try {

			System.out.println("Voici la liste des fichiers : ");
			Scanner sc = new Scanner(System.in);
			String nom = sc.nextLine();
			File file = new File("./tp1/FichiersServeur/" + nom);
			if (file.exists()) {
				dOut.writeUTF(nom);
				String ext;
				StringTokenizer str = new StringTokenizer(nom,".");
				ext= str.nextToken();
				ext= str.nextToken();
				attendreCopieFichierGET(ext);
			}else{
				System.out.println("Le fichier n'existe pas -> " + nom);
			}
		} catch (Exception e) {

		}
	}

	public void attendreCopieFichierGET(String ext) {
		try {
			System.out.println("On va recevoir le fichier");
			byte[] buffer = new byte[1024];
			int bytesRead;
			String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
			FileOutputStream fileOutput = new FileOutputStream(CHEMIN_CLIENT + timeStamp + "."+ ext);

			// tant qu'on peut on lit
			while ((bytesRead = dIn.read(buffer)) != -1) {
				// cas de fin
				if (bytesRead == 1 && buffer[0] == -1) {
					break;
				}
				// On ecrit dans le file
				fileOutput.write(buffer, 0, bytesRead);
			}
			// si on est la tout va bien houraa!
			// System.out.println("Fichier reçu");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String envoyerViaSocket(String message) {
		try {

			dOut.writeUTF(message);

			String mes = "";
			while (mes.equals("")) {
				mes = dIn.readUTF();
				System.out.println(mes);

			}
			return mes;
		} catch (Exception e) {

		}
		return "";

	}

	public void AfficherRep(String repServ) {
		System.out.println("Affichage du repertoire de fichier : ");
		StringTokenizer string = new StringTokenizer(repServ, " ");
		while (string.hasMoreTokens()) {
			System.out.println(" - " + string.nextToken());
		}

	}

	public void attendreReception(File file) {
		try {
			String message = "";
			while (message.equals("")) {
				message = dIn.readUTF();
				System.out.println(message);

				if (message.equals("Ok")) {
					// En envoi
					envoyerFichier(file);
				}
			}

		} catch (Exception e) {

		}
	}

	public void envoyerFichier(File file) {
		try {
			byte[] buffer = new byte[1024];
			int bytesRead;
			FileInputStream fileInput = new FileInputStream(file);

			while ((bytesRead = fileInput.read(buffer)) != -1) {
				dOut.write(buffer, 0, bytesRead);
				dOut.flush(); // ca doit confirmer l'envoi ( je crois )
			}
			dOut.write(-1); // Du coup c le cas d'arret sur le serveur
			System.out.println("C : Envoi du fichier terminé");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Client client = new Client("127.0.0.1", 2121);
		client.initierConnexion();
		client.attendreConfirmationConnexion();
		client.envoyerDemande();
	}

}
