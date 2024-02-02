package tp1;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.StringTokenizer;

public class Serveur {

	private static final int PORT = 2121;
	private static final String CHEMIN_SERVEUR = "./FichiersServeur/";
	private ServerSocket servSocket;
	private Socket cliSocket;
	private DataInputStream dIn;
	private DataOutputStream dOut;

	public Serveur() {
		try {
			servSocket = new ServerSocket(PORT);
			System.out.println("S : serveur actif");
		} catch (IOException e) {
			System.err.println("S : erreur d'instanciation de la socket du serveur");
			e.printStackTrace();
		}
	}

	public void ecouter() {

		System.out.println("On attend le client");
		while (true) {
			try {
				cliSocket = servSocket.accept();
				if (cliSocket != null) {
					break;
				}
			} catch (Exception e) {

			}

		}
	}

	public void envoyerManuel() {
		try {
			dOut.writeUTF("Je suis le manuel :\n - PUT <chemin>\n - GET <fichier> \n - Liste | <extension>");
		} catch (Exception e) {

		}
	}

	public void ouvrirFlux() {
		try {
			dIn = new DataInputStream(cliSocket.getInputStream());
			dOut = new DataOutputStream(cliSocket.getOutputStream());
			System.out.println("S : flux binaires ouverts");
		} catch (IOException e) {
			System.err.println("S : erreur d'ouverture des flux");
			e.printStackTrace();
		}
	}

	public void deconnecterClient() {
		try {
			dIn.close();
			dOut.close();
			cliSocket.close();
			System.out.println("S : client déconnecté par le serveur");
		} catch (IOException e) {
			System.err.println("S : erreur lors de la déconnexion du client");
			e.printStackTrace();
		}
	}

	public void lireCommande() {
		try {
			System.out.println("On attend le message");

			String message = "";
			while (message.equals("")) {
				message = dIn.readUTF();
				System.out.println("Reponse recu : ");
				System.out.println(message);
				StringTokenizer tok = new StringTokenizer(message, " ");
				String tok1 = tok.nextToken();
				if (tok1.equals("DESTROY")) {
					return;
				}
				if (tok1.equals("REPERTOIRE")) { // GET ou LIST
					if(tok.hasMoreTokens()){
						envoyerRepertoire(tok.nextToken());
						attendreReponseGet();
					}else{
						envoyerRepertoire();
						attendreReponseGet();
					}

				}
				if (Integer.parseInt(tok1) < 1000000) {
					dOut.writeUTF("Ok");
					gererUpload(tok.nextToken());

				}

			}

		} catch (Exception e) {

		}
	}

	public void attendreReponseGet() {
		try{

		String message = "";
		while (message.equals("")) {
			message = dOut.readUTF();
			System.out.println("Reponse recu : ");
			System.out.println(message);
			File file = new File(CHEMIN_SERVEUR + message);
			if (file.exists()) {
				System.out.println("On vous envoi le fichier M.bond");
				envoyerFichierGET(file);
			}
		}
		}catch(Exception e ){

		}
	}

	public void envoyerFichierGET(File file) {
		try {
			System.out.println("On va envoyer le fichier");
			byte[] buffer = new byte[1024];
			int bytesRead;
			FileInputStream fileInput = new FileInputStream(file);

			while ((bytesRead = fileInput.read(buffer)) != -1) {
				dOut.write(buffer, 0, bytesRead);
				dOut.flush(); // ca doit confirmer l'envoi ( je crois )
				System.out.println("test");
			}
			dOut.write(-1); // Du coup c le cas d'arret sur le serveur
			System.out.println("Fichier envoyé");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void envoyerRepertoire() {
		File dir = new File(CHEMIN_SERVEUR);
		File[] liste = dir.listFiles();
		String listeFichier = "";
		for (File item : liste) {
			if (item.isFile()) {
				listeFichier = listeFichier + " " + item.getName();
				System.out.format("Nom du fichier: %s%n", item.getName());
			} else if (item.isDirectory()) {
				System.out.format("Nom du répertoir: %s%n", item.getName());
			}
		}
		try {
			dOut.writeUTF(listeFichier);
		} catch (Exception e) {

		}
	}
	public void envoyerRepertoire(String extension) {
		File dir = new File(CHEMIN_SERVEUR);
		File[] liste = dir.listFiles();
		String listeFichier = "";
		for (File item : liste) {
			if (item.isFile()) {
				StringTokenizer string = new StringTokenizer(item.getName(),".");
				String nom = string.nextToken();
				if(string.nextToken().equals(extension)){
					listeFichier = listeFichier + " " + item.getName();

				}
				System.out.format("Nom du fichier: %s%n", item.getName());
			} else if (item.isDirectory()) {
				System.out.format("Nom du répertoir: %s%n", item.getName());
			}
		}
		try {
			dOut.writeUTF(listeFichier);
		} catch (Exception e) {

		}
	}
	public void gererUpload(String nom) {
		try {
			System.out.println("On va recevoir le fichier");
			byte[] buffer = new byte[1024];
			int bytesRead;

			FileOutputStream fileOutput = new FileOutputStream(CHEMIN_SERVEUR + nom);

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
			System.out.println("Fichier reçu");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	// aide
	// https://stackoverflow.com/questions/5690954/java-how-to-read-an-unknown-number-of-bytes-from-an-inputstream-socket-socke

	public static void main(String[] args) {
		Serveur serv = new Serveur();
		while (true) {
			serv.ecouter();
			serv.ouvrirFlux();
			serv.envoyerManuel();
			serv.lireCommande();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

}