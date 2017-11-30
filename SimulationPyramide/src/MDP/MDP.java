package MDP;

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

public class MDP {
	public Etat etatInitial;
	public Etat etatFinal;
	public Cloud cloud;
	public int numeroMDP;
	//public AgentRL agent;
	
	public static double greedyEpsilon=0.1;
	public static double alpha=0.5;
	public static double gamma=0.85;
	public static double maxCharge=4;
	public static double maxRessources=10;
	public static double nbTypeRessources=2;
	
	public MDP(Etat etatInitial,Etat etatFinal,Cloud cloud) {
		this.etatInitial=etatInitial;
		this.etatFinal=etatFinal;
		this.cloud=cloud;
		//this.agent=agent;
		//this.numeroMDP=numeroMDP;
	}
	
	public void explorer(int numeroExploration) throws IOException {
		//Runtime rt = Runtime.getRuntime();
	    //long prevTotal = 0;
	    //long prevFree = rt.freeMemory();
	    
		Etat etatEnCours=etatInitial;
		double gainCumule=0;
		int temps=0;
		//FileWriter fw = new FileWriter("res.txt");
		//FileWriter fwe = new FileWriter("err.txt");
		FileWriter fw = new FileWriter("/projets/pyramide/mkandi/resNonPara2.txt");
		FileWriter fwe = new FileWriter("/projets/pyramide/mkandi/errPar2.txt");
		//FileWriter fw = new FileWriter("resPar"+numeroMDP+".txt");
		//FileWriter fwe = new FileWriter("errPar"+numeroMDP+".txt"); 
		//VariablesGlobales.writer_gmpt=new FileWriter("cost-time",true);
		
		int cptResolus=0,cptNonResolus=0,cptNull=0,cptExept=0;
		MDP.greedyEpsilon=1;
		while((etatFinal==null || etatEnCours!=etatFinal) && temps<80000) {
	        //long total = rt.totalMemory();
	        //long free = rt.freeMemory();
	        
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
	        
	        if(temps>25000) MDP.greedyEpsilon=0.5;
	        if(temps>50000) MDP.greedyEpsilon=0.05;
	        //System.out.println("1");
	        //if(temps>20000) MDP.greedyEpsilon=0.5;
	        //if(temps>40000) MDP.greedyEpsilon=0.05;
			
			//System.out.println("> "+temps);
			Etat prochainEtat;
			Action a=etatEnCours.choisirAction(null,null);
			//System.out.println("2");
			// tenir compte de l'action pour le calcul du nouveau taux de surcharge
			Set<TypeVM> cles = a.natureAction.keySet();
			Iterator<TypeVM> it = cles.iterator();
			double nbRajout=0;
			while (it.hasNext()){
			   TypeVM cle = it.next();
			   Integer nb = a.natureAction.get(cle);
			   nbRajout+=nb*VariablesGlobales.T;
			}
			double tauxSurcharge=cloud.nbRessourceNonDispo()/(nbRajout+cloud.nbRessourceTotal());
			//System.out.println("3");
			
			// tenir compte du taux de surcharge pour décider si les requetes sont acceptées
			int nbRequetes=0;
			if(tauxSurcharge<0.5) {
				int minR=0;
				int maxR=3;
				if(etatEnCours.heureJournee/24<8) {
					 minR=0;
					 maxR=0;
				}
				else if(etatEnCours.heureJournee/24<12) {
					minR=0;
					 maxR=3;
				}
				else if(etatEnCours.heureJournee/24<13) {
					minR=0;
					 maxR=1;
				}
				else if(etatEnCours.heureJournee/24<17) {
					minR=0;
					 maxR=3;
				}
				else {
					 minR=0;
					 maxR=0;
				}
				nbRequetes=minR+(int)(Math.random()*(maxR-minR+1));
				for(int i=0;i<nbRequetes;i++) {
					GenerateurRequetes.genererRequeteAleatoirement(cloud);
				}
			}
			//System.out.println("4");
			// MaJ le nombre de ressoruces
			
			this.majNbRessources(etatEnCours,a);	
			this.majRestePourDesactiver();
			//System.out.println("5");
			// Placement et ordonnancement
			Cout cout=new Cout();
			if(VariablesGlobales.verbose) System.out.println("Avant : "+cloud.tauxSurcharge());
			
			
			Gantt gantt;
			ModelePlacementGLPKTEZMDP mo=new ModelePlacementGLPKTEZMDP(cloud,cout);
			long startTime = System.currentTimeMillis();
			//System.out.println("6-");
			mo.resoudre(mo);
			//System.out.println("7-");
			long stopTime = System.currentTimeMillis();
		    long elapsedTime = stopTime - startTime;
		    if(VariablesGlobales.verbose) System.out.println("Temps Placement = "+elapsedTime);
		    
		    if(mo.resolu) {
		    	ModeleOrdonnancementGLPKTez glpk=new ModeleOrdonnancementGLPKTez(cloud,cout,mo.A);
			    startTime = System.currentTimeMillis();
			    gantt=glpk.resoudre(glpk);
			    stopTime = System.currentTimeMillis();
			    elapsedTime = elapsedTime+ (stopTime - startTime);
			    if(VariablesGlobales.verbose) System.out.println("Temps Ordonnancement = "+elapsedTime);
			    //gantt.ecrireDansFichier();
			    if(gantt!=null) {
			    	try {
			    	cloud.allouerRessourcesTez(gantt);
			    	}catch(ArrayIndexOutOfBoundsException e) {
			    		cptExept++;
			    	}
			    	if(nbRequetes==0) cptNull++;
				    else cptResolus++;
			    }
			    else {
			    	if(VariablesGlobales.verbose) System.out.println("====================================================== Non resolu Ordonnancement");
			    	cptNonResolus++;
			    }
			    //System.out.println("8");
			    
		    }
		    else{
		    	if(VariablesGlobales.verbose) System.out.println("====================================================== Non resolu Placement");
		    	cptNonResolus++;
		    	//System.out.println("8");
		    }
		    //System.out.println("######################## "+cptNonResolus+" - "+cptNull+" - "+cptResolus);
		    cloud.viderRequetesEnAttentes();
		    cloud.stageIndex=0;
		    //System.out.println("9");
		 // choisir le prochain état
		    prochainEtat=a.choisirProchainEtat(cloud.tauxSurcharge());
			
		    if(VariablesGlobales.verbose) System.out.println("Après : "+cloud.tauxSurcharge());
		    cloud.avancerDansTemps();
		    //System.out.println("10");
		    if(VariablesGlobales.verbose) System.out.println(">> "+prochainEtat);
		    if(VariablesGlobales.verbose) System.out.println(">> "+prochainEtat.nbRessources);
		    cles = prochainEtat.nbRessources.keySet();
			it = cles.iterator();
			Set<TypeVM> cles2 = etatEnCours.nbRessources.keySet();
			Iterator<TypeVM> it2 = cles2.iterator();
			int aa=etatEnCours.nbRessources.get(((TypeVM)it2.next())),
					bb=etatEnCours.nbRessources.get(((TypeVM)it2.next())),
					cc=prochainEtat.nbRessources.get(((TypeVM)it.next())),
					dd=prochainEtat.nbRessources.get(((TypeVM)it.next()));
		    
			double r=a.gagner(cloud,cout,etatEnCours,prochainEtat);
			gainCumule+=r;
			
			// System.out.println("11");
			fw.write(temps+";"+r+";"+cc+";"+dd+"\n");
			//fw.write(temps+";"+r+"\n");
			this.majQ(etatEnCours,prochainEtat,a,r);
			etatEnCours=prochainEtat;
			// System.out.println("12");
			//if(prochainEtat.charge>0) {
				
			//}
			/*if (total != prevTotal || free != prevFree) {
	            System.out.println(String.format("#%s, Total: %s, Free: %s, Diff: %s",temps,total,free,prevFree - free));
	            prevTotal = total;
	            prevFree = free;
	        }*/
	        
			temps++;
		}
		System.out.println("######################## "+cptNonResolus+" - "+cptNull+" - "+cptResolus+" - "+cptExept);
		fw.close();
		fwe.close();
		 System.out.println("13");
		//System.out.println(gainCumule+" "+this.etatInitial.listeActions.get(0).Q+" "+this.etatInitial.listeActions.get(1).Q);
		//System.out.println(numeroExploration+" "+gainCumule);
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
