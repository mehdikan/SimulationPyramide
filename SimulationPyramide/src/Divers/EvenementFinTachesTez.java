package Divers;

import java.util.ArrayList;

import Entite.*;

public class EvenementFinTachesTez implements Comparable<EvenementFinTachesTez>{
	public int instant;
	public ArrayList<GroupeRessources> ressorceALiberer;
	public ArrayList<GroupeTachesTez> tachesFinies;

	public EvenementFinTachesTez(int instant,GroupeRessources ress,GroupeTachesTez tache){
		this.instant=instant;
		this.ressorceALiberer=new ArrayList<GroupeRessources>();
		this.tachesFinies=new ArrayList<GroupeTachesTez>();
		if(ress!=null)
			this.ressorceALiberer.add(ress);
		
		if(tache!=null){
			this.tachesFinies.add(tache);
		}
	}

	@Override
	public int compareTo(EvenementFinTachesTez o) {
		// TODO Auto-generated method stub
		return this.instant-o.instant;
	}
}
