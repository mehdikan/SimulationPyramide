package MDPParallel;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import Divers.VariablesGlobales;
import Entite.Cloud;
import Entite.Cout;
import Entite.GenerateurRequetes;
import Entite.GroupeRessources;
import Entite.MachinePhysique;
import Entite.TypeVM;
import Entite.VM;
import Gantt.Gantt;
import PLNETEZ.ModeleOrdonnancementGLPKTez;
import PLNETEZ.ModelePlacementGLPKTEZ;
import PLNETEZ.ModelePlacementGLPKTEZMDP;
import MDP.*;


public class MDPParallel {
	public Etat etatInitial;
	public Etat etatFinal;
	public Etat prochainEtat;
	public Cloud cloud;
	public int numeroMDP;
	public Etat etatEnCours=null;
	public double gainCumule=0;
	public int cptResolus=0;
	public int cptNonResolus=0;
	public int cptNull=0;
	public int cptExept=0;
	
	//public AgentRL agent;
	
	public static double greedyEpsilon=1;
	public static double alpha=0.5;
	public static double gamma=0.85;
	public static double maxCharge=4;
	public static double maxRessources=10;
	public static double nbTypeRessources=2;
	
	public MDPParallel(Etat etatInitial,Etat etatFinal,Cloud cloud) {
		this.etatInitial=etatInitial;
		this.etatFinal=etatFinal;
		this.cloud=cloud;
		etatEnCours=etatInitial;
		
		//this.agent=agent;
		//this.numeroMDP=numeroMDP;
	}
	
	public void majQ(Etat s,Etat s_,Action a,double r) {
		double min=-1;
		Iterator<Action> it = s_.listeActions.iterator();
		while (it.hasNext()) {
		       Action action = it.next();
		       if(min==-1) min=action.Q;
		       else min=Math.max(min, action.Q);
		}
		a.Q=a.Q+MDP.alpha*(r+MDP.gamma*min-a.Q);
	}
	
	public void majNbRessources(Etat etatEnCours, Action action) {
		Set<TypeVM> cles = action.natureAction.keySet();
		Iterator<TypeVM> it = cles.iterator();
		while (it.hasNext()){
		   TypeVM typeVM = it.next();
		   
		   if(action.natureAction.get(typeVM)>0) {
			   	for(int i=0;i<action.natureAction.get(typeVM);i++)
			   		this.cloud.activeResssource(typeVM);
		   }
		   else if(action.natureAction.get(typeVM)<0) {
			   for(int i=0;i<-action.natureAction.get(typeVM);i++)
				   this.cloud.desactiveResssource(typeVM);
		   }
		}
	}
	
	public static Etat getEtat(ArrayList<Etat> listeEtats,String codeEtat) {
		Iterator<Etat> it=listeEtats.iterator();
		while (it.hasNext()){
			Etat etat=it.next();
			if(codeEtat.equals(etat.codeEtat)) {
				return etat;
			}
		}
		return null;
	}
	
	public void majRestePourDesactiver() {
		for(MachinePhysique p1:cloud.listeMachinesPhysique){
			for(VM vm1:p1.ListeVMs){
				for(GroupeRessources r: vm1.groupeTezRessources){
					if(r.restePourDesactiver>0) r.restePourDesactiver-=1;
				}
			}
		}
	}
}
