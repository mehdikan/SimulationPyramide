package MDP;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.Iterator;

import Divers.VariablesGlobales;
import Entite.Cloud;
import Entite.Cout;
import Entite.MachinePhysique;
import Entite.TypeVM;
import Entite.VM;
import MDPParallel.Agent;

public class Action {
	public ArrayList<Transition> listeTransitions;
	public double Q;
	public int nombreEssai;
	public double greedyProba;
	public LinkedHashMap<TypeVM,Integer> natureAction;
	public int numeroAction;
	
	public Action(LinkedHashMap<TypeVM,Integer> natureAction,int numeroAction) {
		this.listeTransitions=new ArrayList<Transition>();
		this.Q=0;
		this.nombreEssai=0;
		this.natureAction=natureAction;
		this.numeroAction=numeroAction;
	}
	
	public Etat choisirProchainEtat(double tauxSurcharge) {	
		Etat choix=null;
		boolean stop=false;
		
		Iterator<Transition> it = listeTransitions.iterator();
		Transition prec=null;
		while (!stop && it.hasNext()) {
		   Transition tr = it.next();
		   if((prec==null || prec.prochainEtat.charge<tauxSurcharge) && tr.prochainEtat.charge>=tauxSurcharge) {
			   choix=tr.prochainEtat;
			   stop=true;
		   }
		   prec=tr;
		}

		if(choix!=null) choix.nbVisites++;
		return choix;
	}
	
	public double gagner(Cloud cloud,Cout cout,Etat etatEnCours,Etat prochainEtat) {
		double coutRess=0;
		for(MachinePhysique mp:cloud.listeMachinesPhysique){
			for(VM vm:mp.ListeVMs){
				int somme=0;
				for(int i=0;i<vm.nbTezSlots;i++) {
					//somme+=vm.ressourceActive(i);
					if(vm.ressourceActive(i)==1 || (vm.ressourceActive(i)==0 && vm.groupeTezRessources.get(i).restePourDesactiver>0)) {
					//if(vm.ressourceActive(i)==1) {
						somme+=1;
					}
				
				}
				coutRess+=somme*(VariablesGlobales.Pproc*vm.processeurTezSlots+VariablesGlobales.Pmem*vm.memoireTezSlots);
				//coutRess+=somme;
			}
		}
		
		double coutActivation=0,coutDesactivation=0;
		Set<TypeVM> cles = prochainEtat.nbRessources.keySet();
		Iterator<TypeVM> it = cles.iterator();
		while (it.hasNext()){
		   TypeVM cle = it.next();
		   Integer pe = prochainEtat.nbRessources.get(cle);
		   Integer ee = etatEnCours.nbRessources.get(cle);
		   if(pe>ee) coutActivation+=VariablesGlobales.coutActivationRessource;
		   else if(pe<ee) coutDesactivation+=VariablesGlobales.coutDesactivationRessource;
		}
	
		
		VariablesGlobales.coutRessTempo=coutRess;
		VariablesGlobales.coutActivationTempo=coutActivation;
		VariablesGlobales.coutDesactivationTempo=coutDesactivation;
		return coutActivation+coutDesactivation+coutRess+cout.coutPenaliteStockage;
		//return coutRess;
	}
	
	
	public double getQglobal(ArrayList<Agent> agents,Agent agentEnCours) {
		///////////////////////////////////////////////////////////sdsqdqsqd
		if(agentEnCours.numeroAgent==2) return this.Q;
///////////////////////////////////////////////////////////sdsqdqsqd
		
		Iterator<Agent> itAG = agents.iterator();
		double QTotal=0;
		int nbEssaiTotal=0;
		while (itAG.hasNext()) {
		    Agent agent = itAG.next();
		    QTotal+=agent.listeActions.get(this.numeroAction).Q*(double)agent.listeActions.get(this.numeroAction).nombreEssai;
		    nbEssaiTotal+=agent.listeActions.get(this.numeroAction).nombreEssai;
		}
		if(nbEssaiTotal>0) {
			return QTotal/(double)nbEssaiTotal;
		}
		return 0;
	}
}