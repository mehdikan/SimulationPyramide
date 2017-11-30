package MDPParallel;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

import Entite.ClasseClients;
import Entite.Cloud;
import Entite.MachinePhysique;
import Entite.TypeVM;
import Entite.VM;
import MDP.Action;
import MDP.Etat;
import MDP.MDP;
import MDP.Transition;

public class Agent {
	public int numeroAgent;
	public Cloud cloud;
	public ArrayList<Etat> listeEtats;
	public ArrayList<Action> listeActions;
	MDPParallel mdp; 
	int minRess=3;
	int maxRess=5;
	int uniteTemps=10;
	FileWriter fw;
    //FileWriter fwe;
    
	public Agent(int numeroAgent) throws IOException {
		this.numeroAgent=numeroAgent;
		cloud=new Cloud(1,1,1,1);
		
		TypeVM type1=new TypeVM(36,70000,1000000);
		TypeVM type2=new TypeVM(36,80000,1000000);
		MachinePhysique mp=new MachinePhysique(cloud);
		cloud.listeMachinesPhysique.add(mp);		
		cloud.ajouterVM(0,new VM(mp,type2,1,1,1,8*2,8*2,8*2,8*2,8*2,8*2,8*2,8*2,8*2));
		cloud.ajouterVM(0,new VM(mp,type2,1,1,1,8*2,8*2,8*2,8*2,8*2,8*2,8*2,8*2,8*2));
		cloud.ajouterVM(0,new VM(mp,type2,1,1,1,8*2,8*2,8*2,8*2,8*2,8*2,8*2,8*2,8*2));
		cloud.ajouterVM(0,new VM(mp,type2,1,1,1,8*2,8*2,8*2,8*2,8*2,8*2,8*2,8*2,8*2));
		cloud.ajouterVM(0,new VM(mp,type2,1,1,1,8*2,8*2,8*2,8*2,8*2,8*2,8*2,8*2,8*2));
		cloud.vmIndex=0;
		mp=new MachinePhysique(cloud);
		cloud.listeMachinesPhysique.add(mp);
		cloud.ajouterVM(1,new VM(mp,type1,1,1,1,8*1,8*1,8*1,8*1,8*1,8*1,8*1,8*1,8*1));
		cloud.ajouterVM(1,new VM(mp,type1,1,1,1,8*1,8*1,8*1,8*1,8*1,8*1,8*1,8*1,8*1));
		cloud.ajouterVM(1,new VM(mp,type1,1,1,1,8*1,8*1,8*1,8*1,8*1,8*1,8*1,8*1,8*1));
		cloud.ajouterVM(1,new VM(mp,type1,1,1,1,8*1,8*1,8*1,8*1,8*1,8*1,8*1,8*1,8*1));
		cloud.ajouterVM(1,new VM(mp,type1,1,1,1,8*1,8*1,8*1,8*1,8*1,8*1,8*1,8*1,8*1));
		
		cloud.tousCandidatsMap();
		cloud.tousCandidatsReduce();
		cloud.setDistanceDefaultMR();
		
		cloud.tousCandidatsTez();
		cloud.setDistanceDefaultTez();

		cloud.listeClassesClient.add(new ClasseClients(1));
		
		 fw = new FileWriter("/projets/pyramide/mkandi/resAgent"+numeroAgent+".txt");
	   // FileWriter fwe = new FileWriter("/projets/pyramide/mkandi/errPar"+numeroAgent+".txt");
	    //fw = new FileWriter("resPar"+numeroAgent+".txt");
		//fw = new FileWriter("resPar"+numeroAgent+".txt");
	    //FileWriter fwe = new FileWriter("errPar"+numeroAgent+".txt");
		
		listeEtats=new ArrayList<Etat>();
		listeActions=new ArrayList<Action>();
		
		int numeroAction=0;
		for(double tauxDispoA=0.25;tauxDispoA<=1;tauxDispoA+=0.25) {
			for(int h=0;h<24*60;h+=uniteTemps) {
				for(int t1=maxRess;t1>=minRess;t1--) {
					for(int t2=maxRess;t2>=minRess;t2--) {
						LinkedHashMap<TypeVM,Integer> ress=new LinkedHashMap<TypeVM,Integer>();
						ress.put(type1, t1);
						ress.put(type2, t2);
						// Etat newEtat=new Etat(chargeA,ress,h); Back
						Etat newEtat=new Etat(tauxDispoA,ress,h);
						listeEtats.add(newEtat);
						
						for(int a=-1;a<=1;a++) {
							if(a==0) {
								for(int b=-1;b<=1;b+=1) {
									LinkedHashMap<TypeVM,Integer> natureAction=new LinkedHashMap<TypeVM,Integer>();
									natureAction.put(type1, a);
									natureAction.put(type2, b);
									Action action=new Action(natureAction,numeroAction);
									// for(double chargeP=0;chargeP<=3;chargeP++) { Back
									for(double tauxDispoB=0.25;tauxDispoB<=1;tauxDispoB+=0.25) { 	
					    			   if(newEtat.nbRessources.get(type1)+action.natureAction.get(type1)>=minRess && newEtat.nbRessources.get(type1)+action.natureAction.get(type1)<maxRess+1 && newEtat.nbRessources.get(type2)+action.natureAction.get(type2)>=minRess && newEtat.nbRessources.get(type2)+action.natureAction.get(type2)<maxRess+1) {
					    				   //Etat etat=MDP.getEtat(listeEtats,tauxDispoB+"--"+((newEtat.heureJournee+uniteTemps)%(24*60))+"--"+(newEtat.nbRessources.get(type1)+action.natureAction.get(type1))+"-"+(newEtat.nbRessources.get(type2)+action.natureAction.get(type2)));
					    				   //if(etat==null) System.out.println(etat);
					    				   //action.listeTransitions.add(new Transition(0.25,etat));
					    				   //fw.write("---"+newEtat.codeEtat+";;;;;;;;;"+chargeP+"--"+((newEtat.heureJournee+uniteTemps)%(24*60))+"--"+(newEtat.nbRessources.get(type1)+"-"+action.natureAction.get(type1))+"-"+(newEtat.nbRessources.get(type2)+"-"+action.natureAction.get(type2))+"\n");
					    				   newEtat.listeActions.add(action);
					    				   this.listeActions.add(action);
					    				   numeroAction++;
					    				   break;
					    			   }
						    		}
									//if(action.listeTransitions.size()>0)
									//	newEtat.listeActions.add(new Action(natureAction));
								}
							}
							else {
								LinkedHashMap<TypeVM,Integer> natureAction=new LinkedHashMap<TypeVM,Integer>();
								natureAction.put(type1, a);
								natureAction.put(type2, 0);
								Action action=new Action(natureAction,numeroAction);
								// for(double chargeP=0;chargeP<=3;chargeP++) { Back
								for(double tauxDispoB=0.25;tauxDispoB<=1;tauxDispoB+=0.25) {
				    			   if(newEtat.nbRessources.get(type1)+action.natureAction.get(type1)>=minRess && newEtat.nbRessources.get(type1)+action.natureAction.get(type1)<maxRess+1 && newEtat.nbRessources.get(type2)+action.natureAction.get(type2)>=minRess && newEtat.nbRessources.get(type2)+action.natureAction.get(type2)<maxRess) {
				    				   //Etat etat=MDP.getEtat(listeEtats,tauxDispoB+"--"+((newEtat.heureJournee+uniteTemps)%(24*60))+"--"+(newEtat.nbRessources.get(type1)+action.natureAction.get(type1))+"-"+(newEtat.nbRessources.get(type2)+action.natureAction.get(type2)));
				    				   //action.listeTransitions.add(new Transition(0.25,etat));
				    				   //fw.write("---"+newEtat.codeEtat+";;;;;;;;;"+chargeP+"--"+((newEtat.heureJournee+uniteTemps)%(24*60))+"--"+(newEtat.nbRessources.get(type1)+"-"+action.natureAction.get(type1))+"-"+(newEtat.nbRessources.get(type2)+"-"+action.natureAction.get(type2))+"\n");
				    				   newEtat.listeActions.add(action);
				    				   this.listeActions.add(action);
				    				   numeroAction++;
				    				   break;
				    			   }
					    		}
								//if(action.listeTransitions.size()>0)
								//	newEtat.listeActions.add(new Action(natureAction));
							}
						}
					}
				}
			}
		}
		
		Iterator<Etat> itE = listeEtats.iterator();

		while (itE.hasNext()) {
	       Etat etat = itE.next();
	       Iterator<Action> itA = etat.listeActions.iterator();
	       while(itA.hasNext()) {
	    	   Action action=itA.next();
    		   //for(double charge=0;charge<=3;charge++) { Back
	    	   for(double tauxDispo=0.25;tauxDispo<=1;tauxDispo+=0.25) {
    			   if(etat.nbRessources.get(type1)+action.natureAction.get(type1)>=minRess && etat.nbRessources.get(type1)+action.natureAction.get(type1)<maxRess+1 && etat.nbRessources.get(type2)+action.natureAction.get(type2)>=minRess && etat.nbRessources.get(type2)+action.natureAction.get(type2)<maxRess+1) {
    				   action.listeTransitions.add(new Transition(0.25,MDP.getEtat(listeEtats,tauxDispo+"--"+((etat.heureJournee+uniteTemps)%(24*60))+"--"+(etat.nbRessources.get(type1)+action.natureAction.get(type1))+"-"+(etat.nbRessources.get(type2)+action.natureAction.get(type2)))));
    			   }
    		   }
    	   }
		}
		
		mdp=new MDPParallel(listeEtats.get(0),null,cloud);
	}
}