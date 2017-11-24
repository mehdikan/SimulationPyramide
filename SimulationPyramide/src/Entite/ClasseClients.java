package Entite;

import java.util.*;

public class ClasseClients {
	public ArrayList<Requete> requeteEnAttente;
	public ArrayList<RequeteTez> requeteTezEnAttente;
	int priorite;
	
	public ClasseClients(int priorite){
		requeteEnAttente=new ArrayList<Requete>();
		requeteTezEnAttente=new ArrayList<RequeteTez>();
		this.priorite=priorite;
	}
	
	public void vider(){
		requeteEnAttente.clear();
		requeteTezEnAttente.clear();
	}
	
}
