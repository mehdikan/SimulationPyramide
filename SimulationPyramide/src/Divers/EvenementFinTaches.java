package Divers;

import java.util.ArrayList;

import Entite.*;

public class EvenementFinTaches implements Comparable<EvenementFinTaches>{
	public int instant;
	public ArrayList<GroupeRessources> ressorceALiberer;
	public ArrayList<GroupeTaches> tachesFinies;

	public EvenementFinTaches(int instant,GroupeRessources ress,GroupeTaches tache){
		this.instant=instant;
		this.ressorceALiberer=new ArrayList<GroupeRessources>();
		this.tachesFinies=new ArrayList<GroupeTaches>();
		if(ress!=null)
			this.ressorceALiberer.add(ress);
		
		if(tache!=null){
			this.tachesFinies.add(tache);
		}
	}

	@Override
	public int compareTo(EvenementFinTaches o) {
		// TODO Auto-generated method stub
		return this.instant-o.instant;
	}
}
