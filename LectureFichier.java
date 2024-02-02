package tp1;

import java.io.FileInputStream;

public class LectureFichier {
    String chemin;
    int taille;
    byte[] contenu;
    FileInputStream fis;



    LectureFichier(String chemin){
        this.chemin = chemin;
    }

    public int getTaille(){
        return this.taille;
    }

    public byte[] getContenu(){
        return this.contenu;
    }

    public void lireFichier(){

    }
    

}
