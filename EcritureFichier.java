package tp1;

import java.io.FileOutputStream;

public class EcritureFichier {
    String cheminLecture;
    String cheminEcriture;
    String nouveauNom;
    FileOutputStream fos;
    byte[] contenu;

    EcritureFichier( String cheminLecture, String cheminEcriture,byte[] contenu){
        this.cheminLecture = cheminLecture;
        this.cheminEcriture = cheminEcriture ;
        this.contenu = contenu;
    }

    public String getNouveauNom(){
        return this.nouveauNom;
    }
    public void genererNouveauNom(){

    }

    public void ecrireFichier(){
        
    }
}
