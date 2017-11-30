package MDPParallel;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Set;

import com.sun.xml.internal.bind.v2.schemagen.xmlschema.List;

import java.util.Iterator;

import Divers.VariablesGlobales;
import Entite.ClasseClients;
import Entite.Cloud;
import Entite.Cout;
import Entite.GenerateurRequetes;
import Entite.MachinePhysique;
import Entite.TypeVM;
import Entite.VM;
import Gantt.Gantt;
import MDP.Action;
import MDP.Etat;
import MDP.MDP;
import MDP.Transition;
import PLNETEZ.ModeleOrdonnancementGLPKTez;
import PLNETEZ.ModelePlacementGLPKTEZMDP;

public class TestMDPParallelMinMaxRess {
	public static ArrayList<Agent> agents=new ArrayList<Agent>();
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		int nbRess=5;
		int nbAgents=2;
		//FileWriter fw = new FileWriter("outt.txt");
		
		for(int i=0;i<nbAgents;i++) {
			Agent ag=new Agent(i);
			agents.add(ag);
		}
		
		if(VariablesGlobales.verbose) System.out.println("--- Debut ---");
		explorer();
		if(VariablesGlobales.verbose) System.out.println("--- Fin ---");
		
		Iterator<Agent> itAG = agents.iterator();
		while (itAG.hasNext()) {
		    Agent agent = itAG.next();
			System.out.println("-------------------------------------------------------------");
			Iterator<Etat> itE = agent.listeEtats.iterator();
			double nbVisitesMoyenne=0;
			int nbEtatsViste=0;
			while (itE.hasNext()) {
		       Etat etat = itE.next();
		       nbVisitesMoyenne+=etat.nbVisites;
		       if(etat.nbVisites>0) nbEtatsViste+=1;
			}
			nbVisitesMoyenne=nbVisitesMoyenne/agent.listeEtats.size();
			System.out.println("Nombre d'états dans le MDP : "+agent.listeEtats.size());
			System.out.println("Nombre vistites moyenne par état : "+nbVisitesMoyenne);
			System.out.println("Nombre d'états visistés au moins une fois : "+nbEtatsViste);
			System.out.println("-------------------------------------------------------------");
		}
	}

	
	public static void explorer() throws IOException {
		//Runtime rt = Runtime.getRuntime();
	    //long prevTotal = 0;
	    //long prevFree = rt.freeMemory();
	    
		Iterator<Agent> itAG = agents.iterator();
		while (itAG.hasNext()) {
		    Agent agent = itAG.next();
		    
		}    
		
		//VariablesGlobales.writer_gmpt=new FileWriter("cost-time",true);
		
		int temps=0;
		
		MDP.greedyEpsilon=1;
		while(temps<50000) {
			itAG = agents.iterator(); 
			while (itAG.hasNext()) {
				Agent agent = itAG.next();
	        
		        //if(temps>75000) MDP.greedyEpsilon=0.1;
		        
				/*if(temps>10000) MDP.greedyEpsilon=0.9;
				else if(temps>20000) MDP.greedyEpsilon=0.8;
				else if(temps>30000) MDP.greedyEpsilon=0.7;
				else if(temps>40000) MDP.greedyEpsilon=0.6;
				else if(temps>50000) MDP.greedyEpsilon=0.5;
				else if(temps>60000) MDP.greedyEpsilon=0.4;
				else if(temps>70000) MDP.greedyEpsilon=0.3;
				else if(temps>80000) MDP.greedyEpsilon=0.2;
				else if(temps>90000) MDP.greedyEpsilon=0.1;*/
				
		        //if(temps>30000) MDP.greedyEpsilon=0.4;
		        
		        //if(temps>25000) MDP.greedyEpsilon=0.5;
		        //if(temps>50000) MDP.greedyEpsilon=0.05;
		        //System.out.println("1");
		        //if(temps>20000) MDP.greedyEpsilon=0.5;
		        //if(temps>40000) MDP.greedyEpsilon=0.05;
				
				//if(temps>20000) MDP.greedyEpsilon=0.5;
				
				if(temps>5000) MDP.greedyEpsilon=0.3;
				
				
				//System.out.println("> "+temps);
				
				

				double tauxSurchargeAvantAction=agent.cloud.nbRessourceNonDispo()/agent.cloud.nbRessourceTotal();
				
				Action a=agent.mdp.etatEnCours.choisirAction(agents,agent);
				//Action a=agent.mdp.etatEnCours.choisirActionPolitiqueASeuils(agents,agent,tauxSurchargeAvantAction);
				//System.out.println("2");
				// tenir compte de l'action pour le calcul du nouveau taux de surcharge
				Set<TypeVM> cles = a.natureAction.keySet();
				Iterator<TypeVM> itTVM = cles.iterator();
				double nbRajout=0;
				while (itTVM.hasNext()){
				   TypeVM cle = itTVM.next();
				   Integer nb = a.natureAction.get(cle);
				   nbRajout+=nb*VariablesGlobales.T;
				}
				double tauxSurchargeApresAction=agent.cloud.nbRessourceNonDispo()/(nbRajout+agent.cloud.nbRessourceTotal());
				//System.out.println("3");
				
				// tenir compte du taux de surcharge pour décider si les requetes sont acceptées
				int nbRequetes=0;
				if(tauxSurchargeApresAction<0.5) {
					int minR=0;
					int maxR=3;
					if(agent.mdp.etatEnCours.heureJournee/24<8) {
						 minR=0;
						 maxR=0;
					}
					else if(agent.mdp.etatEnCours.heureJournee/24<12) {
						minR=0;
						 maxR=3;
					}
					else if(agent.mdp.etatEnCours.heureJournee/24<13) {
						minR=0;
						 maxR=1;
					}
					else if(agent.mdp.etatEnCours.heureJournee/24<17) {
						minR=0;
						 maxR=3;
					}
					else {
						 minR=0;
						 maxR=0;
					}
					nbRequetes=minR+(int)(Math.random()*(maxR-minR+1));
					for(int i=0;i<nbRequetes;i++) {
						GenerateurRequetes.genererRequeteAleatoirement(agent.cloud);
					}
				}
				//System.out.println("4");
				// MaJ le nombre de ressoruces
				
				agent.mdp.majNbRessources(agent.mdp.etatEnCours,a);	
				agent.mdp.majRestePourDesactiver();
				//System.out.println("5");
				// Placement et ordonnancement
				Cout cout=new Cout();
				if(VariablesGlobales.verbose) System.out.println("Avant : "+agent.cloud.tauxSurcharge());
				
				
				Gantt gantt;
				ModelePlacementGLPKTEZMDP mo=new ModelePlacementGLPKTEZMDP(agent.cloud,cout);
				long startTime = System.currentTimeMillis();
				//System.out.println("6-");
				mo.resoudre(mo);
				//System.out.println("7-");
				long stopTime = System.currentTimeMillis();
			    long elapsedTime = stopTime - startTime;
			    if(VariablesGlobales.verbose) System.out.println("Temps Placement = "+elapsedTime);
			    
			    if(mo.resolu) {
			    	ModeleOrdonnancementGLPKTez glpk=new ModeleOrdonnancementGLPKTez(agent.cloud,cout,mo.A);
				    startTime = System.currentTimeMillis();
				    gantt=glpk.resoudre(glpk);
				    stopTime = System.currentTimeMillis();
				    elapsedTime = elapsedTime+ (stopTime - startTime);
				    if(VariablesGlobales.verbose) System.out.println("Temps Ordonnancement = "+elapsedTime);
				    //gantt.ecrireDansFichier();
				    if(gantt!=null) {
				    	try {
				    	agent.cloud.allouerRessourcesTez(gantt);
				    	}catch(ArrayIndexOutOfBoundsException e) {
				    		agent.mdp.cptExept++;
				    	}
				    	if(nbRequetes==0) agent.mdp.cptNull++;
					    else agent.mdp.cptResolus++;
				    }
				    else {
				    	if(VariablesGlobales.verbose) System.out.println("====================================================== Non resolu Ordonnancement");
				    	agent.mdp.cptNonResolus++;
				    }
				    //System.out.println("8");
				    
			    }
			    else{
			    	if(VariablesGlobales.verbose) System.out.println("====================================================== Non resolu Placement");
			    	agent.mdp.cptNonResolus++;
			    	//System.out.println("8");
			    }
			    //System.out.println("######################## "+cptNonResolus+" - "+cptNull+" - "+cptResolus);
			    agent.cloud.viderRequetesEnAttentes();
			    agent.cloud.stageIndex=0;
			    //System.out.println("9");
			 // choisir le prochain état
			    agent.mdp.prochainEtat=a.choisirProchainEtat(agent.cloud.tauxSurcharge());
				
			    if(VariablesGlobales.verbose) System.out.println("Après : "+agent.cloud.tauxSurcharge());
			    agent.cloud.avancerDansTemps();
			    //System.out.println("10");
			    if(VariablesGlobales.verbose) System.out.println(">> "+agent.mdp.prochainEtat);
			    if(VariablesGlobales.verbose) System.out.println(">> "+agent.mdp.prochainEtat.nbRessources);
			    cles = agent.mdp.prochainEtat.nbRessources.keySet();
				itTVM = cles.iterator();
				Set<TypeVM> cles2 = agent.mdp.etatEnCours.nbRessources.keySet();
				Iterator<TypeVM> it2 = cles2.iterator();
				int aa=agent.mdp.etatEnCours.nbRessources.get(((TypeVM)it2.next())),
						bb=agent.mdp.etatEnCours.nbRessources.get(((TypeVM)it2.next())),
						cc=agent.mdp.prochainEtat.nbRessources.get(((TypeVM)itTVM.next())),
						dd=agent.mdp.prochainEtat.nbRessources.get(((TypeVM)itTVM.next()));
			    
				double r=a.gagner(agent.cloud,cout,agent.mdp.etatEnCours,agent.mdp.prochainEtat);
				agent.mdp.gainCumule+=r;
				
				// System.out.println("11");
				agent.fw.write(temps+";"+r+";"+cc+";"+dd+";"+cout.coutPenaliteStockage+";"+VariablesGlobales.coutRessTempo+";"+VariablesGlobales.coutActivationTempo+";"+VariablesGlobales.coutDesactivationTempo+"\n");
				
				//fw.write(temps+";"+r+"\n");
				agent.mdp.majQ(agent.mdp.etatEnCours,agent.mdp.prochainEtat,a,r);
				agent.mdp.etatEnCours=agent.mdp.prochainEtat;
				// System.out.println("12");
				//if(prochainEtat.charge>0) {
					
				//}
				/*if (total != prevTotal || free != prevFree) {
		            System.out.println(String.format("#%s, Total: %s, Free: %s, Diff: %s",temps,total,free,prevFree - free));
		            prevTotal = total;
		            prevFree = free;
		        }*/
			}
	        
			temps++;
		}
		//System.out.println("######################## "+cptNonResolus+" - "+cptNull+" - "+cptResolus+" - "+cptExept);
		itAG = agents.iterator(); 
		while (itAG.hasNext()) {
			Agent agent = itAG.next();
			agent.fw.close();
		}
		//fwe.close();
		 System.out.println("13");
		//System.out.println(gainCumule+" "+this.etatInitial.listeActions.get(0).Q+" "+this.etatInitial.listeActions.get(1).Q);
		//System.out.println(numeroExploration+" "+gainCumule);
	}
}
