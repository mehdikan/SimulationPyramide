package MDP;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.Set;

import Divers.VariablesGlobales;
import Entite.TypeVM;
import MDPParallel.Agent;

public class Etat {
	public ArrayList<Action> listeActions;
	public double charge;
	public LinkedHashMap<TypeVM,Integer> nbRessources; // (type ressource , nb ressources)
	public int heureJournee;
	public String codeEtat="";
	public int nbVisites=0;
	
	public Etat(double charge,LinkedHashMap<TypeVM,Integer> nbRessources,int heureJournee) {
		listeActions=new ArrayList<Action>();
		this.charge=charge;
		this.nbRessources=nbRessources;
		this.heureJournee=heureJournee;
		codeEtat=this.charge+"--"+this.heureJournee+"-";
		Set<TypeVM> cles = nbRessources.keySet();
		Iterator<TypeVM> it = cles.iterator();
		while (it.hasNext()){
		   TypeVM cle = it.next();
		   Integer valeur = nbRessources.get(cle);
		   codeEtat+="-"+valeur;
		}
	}
	
	public Action choisirAction(ArrayList<Agent> agents,Agent agentEnCours) {
		Action choix=null;
		if(Math.random()<MDP.greedyEpsilon) {
			choix=listeActions.get((int)(Math.random() * listeActions.size()));
		}
		else {
			this.majGreedyProba();
			double min=-1;
			boolean stop=false;
			Iterator<Action> it = listeActions.iterator();
			while (!stop && it.hasNext()) {
			   Action a = it.next();
		       if(min==-1 || a.getQglobal(agents,agentEnCours)<=min) {
		    	   min=a.getQglobal(agents,agentEnCours);
		    	   choix=a;
		       }
			}
		}
		if(choix!=null) choix.nombreEssai++;
		return choix;
	}
	
	public Action choisirActionMaxRess(ArrayList<Agent> agents,Agent agentEnCours) {
		Action choix=null;
		double max=-1000;
		boolean stop=false;
		Iterator<Action> it1 = listeActions.iterator();
		while (!stop && it1.hasNext()) {
		   Action a = it1.next();
		   
		   Set<TypeVM> cles = a.natureAction.keySet();
			Iterator<TypeVM> it2 = cles.iterator();
			int nbRajout=0;
			while (it2.hasNext()){
			   TypeVM cle = it2.next();
			   nbRajout += a.natureAction.get(cle);
			}
		   
	       if(nbRajout>max) {
	    	   max=nbRajout;
	    	   choix=a;
	       }
		}
		if(choix!=null) choix.nombreEssai++;
		return choix;
	}
	
	public Action choisirActionMinRess(ArrayList<Agent> agents,Agent agentEnCours) {
		Action choix=null;
		double min=-1000;
		boolean stop=false;
		Iterator<Action> it1 = listeActions.iterator();
		while (!stop && it1.hasNext()) {
		   Action a = it1.next();
		   
		   Set<TypeVM> cles = a.natureAction.keySet();
			Iterator<TypeVM> it2 = cles.iterator();
			int nbRajout=0;
			while (it2.hasNext()){
			   TypeVM cle = it2.next();
			   nbRajout += a.natureAction.get(cle);
			}
		   
	       if(min==-1000 || nbRajout<min) {
	    	   min=nbRajout;
	    	   choix=a;
	       }
		}
		if(choix!=null) choix.nombreEssai++;
		return choix;
	}
	
	

	
	public void majGreedyProba(){
		double somme=0;
		Iterator<Action> it = listeActions.iterator();
		while (it.hasNext()) {
		       Action a = it.next();
		       somme+=Math.exp(a.Q);
		}
		it = listeActions.iterator();
		while (it.hasNext()) {
		       Action a = it.next();
		       a.greedyProba=Math.exp(a.Q)/somme;
		}
	}
	
	@Override 
	public boolean equals(Object obj) { 
		if (obj == this) { 
			return true; 
		} 
		if (obj == null || obj.getClass() != this.getClass()) { 
			return false; 
		} 
		Etat guest = (Etat) obj; 
		return this.codeEtat == guest.codeEtat; 
	} 
	
	@Override 
	public int hashCode() { 
		return codeEtat.hashCode(); 
	}
}