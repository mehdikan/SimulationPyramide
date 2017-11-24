package Algorithmes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;
import Divers.EvenementFinTaches;
import Divers.EvenementFinTachesTez;
import Divers.Statistics;
import Divers.VariablesGlobales;
import Entite.*;
import Gantt.Gantt;
import Gantt.TrancheTempsAlloue;

public abstract class GenericGreedyMDP {
	Cloud cloud;
	ArrayList<GroupeTachesTez> allGroupsTaches;
	ArrayList<GroupeRessources> allGroupsTezSlots;
	TreeSet<EvenementFinTachesTez> evenements;
	double coutComm=-1;
	double coutProc=-1;
	double coutMem=-1;
	double coutStor=-1;
	public int ordre=1;
	
	public GenericGreedyMDP(){}
	
	public GenericGreedyMDP(Cloud cloud){
		this.cloud=cloud;
		allGroupsTaches=new ArrayList<GroupeTachesTez>();
		allGroupsTezSlots=new ArrayList<GroupeRessources>();
		
		for(ClasseClients cc : cloud.listeClassesClient){
			for(RequeteTez r : cc.requeteTezEnAttente){
				for(StageTez stage : r.listeStages){
					for(GroupeTachesTez tache : stage.groupesTezTaches){
						allGroupsTaches.add(tache);
					}
				}
			}
		}
		
		for(MachinePhysique mp:cloud.listeMachinesPhysique){
			for(VM vm:mp.ListeVMs){
				for(GroupeRessources res: vm.groupeTezRessources){
					allGroupsTezSlots.add(res);
				}
			}
		}
	}
	
	public Cout lancer(){
		placer();
		for(GroupeTachesTez tache:allGroupsTaches){
			tache.fini=0;
		}

		Cout cout=ordonnancer(false);
		System.out.println("temps Total : "+cout.tempsExecTotal);
		System.out.println("temps Moyen par Requete : "+cout.tempsExecMoyenRequete);
		System.out.println("Cout Proc : "+cout.coutProcesseur);
		System.out.println("Cout Mem : "+cout.coutMemoire);
		System.out.println("Cout Stock : "+cout.coutStockage);
		System.out.println("Cout Comm : "+cout.coutComm);
		System.out.println("Cout Penalite : "+cout.coutPenalite);
		System.out.println("Cout Ressources : "+cout.coutRess());
		System.out.println("Cout Total : "+cout.coutTotal());
		return cout;
	}
	
	public void placer(){
		HashSet<GroupeTachesTez> readyTaches=new HashSet<GroupeTachesTez>();
		// ajouter les taches sans dependances	
		for(GroupeTachesTez tache : allGroupsTaches){
			if(tache.dependances.size()==0){
				readyTaches.add(tache);
			}
		}
		
		while(readyTaches.size()!=0){
			GroupeTachesTez n=next(readyTaches);
			ArrayList<GroupeRessources> candidates=ressourcesCandidates(n);
			if(candidates.size()==0){
				System.out.println("EEEEEEEEEEEEreur");
				return;
			}
			
			GroupeRessources c=affecter(n,candidates);
			readyTaches.remove(n);
			n.fini=1;
			n.ressource=c;
			n.ordre=this.ordre;
			this.ordre++;
			
			for(GroupeTachesTez tache : allGroupsTaches){
				if(tache.fini==0){
					boolean trouv=false;
					for(GroupeTachesTez depend : tache.dependances){
						if(depend.fini==0){
							trouv=true;
							break;
						}
					}
					if(!trouv){
						readyTaches.add(tache);
					}
				}
			}
		}
	}
	
	public abstract GroupeTachesTez next(HashSet<GroupeTachesTez> readyTache);
	
	ArrayList<GroupeRessources> ressourcesCandidates(GroupeTachesTez n){
		ArrayList<GroupeRessources> ressCandidates=new ArrayList<GroupeRessources>();
		for(MachinePhysique mp : cloud.listeMachinesPhysique){
			for(VM vm : mp.ListeVMs){
				if(n.stage.processeurTacheTez<=vm.processeurTezSlots
					&& n.stage.memoireTacheTez<=vm.memoireTezSlots){
					for(GroupeRessources g:vm.groupeTezRessources){
						ressCandidates.add(g);
					}
				}
			}
		}
		
		return ressCandidates;
	}
	
	public abstract GroupeRessources affecter(GroupeTachesTez n,ArrayList<GroupeRessources> candidates);
	
	public Cout ordonnancer(boolean partiel){
		EvenementFinTachesTez evCourant;
		int tCourant;
		evenements=new TreeSet<EvenementFinTachesTez>();
		evenements.add(new EvenementFinTachesTez(1,null,null));
		GroupeTachesTez elu;
		int tempsMax=0;
		Cout c=new Cout();
		
		if(partiel) this.stock();
		while(!evenements.isEmpty() )
		{
			evCourant=evenements.first();
			tCourant=evCourant.instant;
			if(!partiel) System.out.println("Instant : "+tCourant);
			for(GroupeRessources gr:evCourant.ressorceALiberer){ 
				gr.setLibre();
				if(!partiel) System.out.println("Groupe de Ressources "+gr.type+"-"+gr.index+" libérée"+"- temps :"+tCourant);
			}
			for(GroupeTachesTez tache:evCourant.tachesFinies){ 
				tache.fini=1;
				tache.dateFin=tCourant;
				tempsMax=Math.max(tempsMax, tCourant);
				tache.stage.requeteTez.tempsGMPT=Math.max(tache.stage.requeteTez.tempsGMPT, tCourant);
				if(!partiel) System.out.println("Groupe de taches "+tache.index+" finies");
			}
			
			evenements.remove(evenements.first());
			
			boolean trouvTache;
			if(partiel){
				trouvTache=false;
				for(GroupeTachesTez tache:allGroupsTaches){
					if(tache.ressource!=null){
						trouvTache=true; break;
					}
				}
			}
			else trouvTache=true;
			
			while(trouvTache && allGroupsTaches.size()>0){					
				elu=groupeTachesElu(tCourant,partiel);			
				if(elu!=null){
					elu.tempsDeclanchement=tCourant;
					if(!partiel) System.out.println("Groupes de taches "+elu.stage.requeteTez.index+"-"+elu.stage.indexStage+"-"+elu.index+" est elue et placé dans la ressource "+elu.ressource.index+"- temps: "+tCourant);
					
					int indexRessouces=0;
					boolean trouv=false;
					for(GroupeTachesTez tache : allGroupsTaches){
						for(MachinePhysique mp : cloud.listeMachinesPhysique){
							for(VM vm : mp.ListeVMs){
									indexRessouces=0;
									for(GroupeRessources g:vm.groupeTezRessources){
										if(g==elu.ressource){
											trouv=true; break;
										}
										indexRessouces++;
									}
									if(trouv) break;
							}
							if(trouv) break;
						}
						if(trouv) break;
					};
					if(!partiel){
						elu.ressource.vm.setAlloueTez(indexRessouces,tCourant,elu.stage.dureeTacheTez);
					}
					else{
						elu.ressource.vm.setAlloueTez(indexRessouces,tCourant,elu.stage.dureeTacheTez);
					}
					 Iterator<EvenementFinTachesTez> iterator = evenements.iterator(); 
					 trouv=false;
				      while (iterator.hasNext()){
				    	 EvenementFinTachesTez ev=(EvenementFinTachesTez) iterator.next();
				         if(ev.instant==tCourant+elu.stage.dureeTacheTez){
				        	 trouv=true;
				        	 ev.ressorceALiberer.add(elu.ressource);
				        	 ev.tachesFinies.add(elu);
				        	 break;
				         }
				      }
				      if(!trouv){
				    	  evenements.add(new EvenementFinTachesTez(tCourant+elu.stage.dureeTacheTez,elu.ressource,elu));
				      }
				      allGroupsTaches.remove(elu);
				}
				else{
					evenements.add(new EvenementFinTachesTez(tCourant+1,null,null));
					break;
				}
			}
		}
		
		c=calculCout(partiel);
		c.tempsExecTotal=tempsMax;
		int cpt=0;
		c.tempsExecMoyenRequete=0;
		for(ClasseClients cc : cloud.listeClassesClient){
			for(RequeteTez r : cc.requeteTezEnAttente){
				c.tempsExecMoyenRequete+=(double)r.tempsGMPT;
				r.tempsGMPT=0;
				cpt++;
			}
		}
		if(cpt>0) { c.tempsExecMoyenRequete=c.tempsExecMoyenRequete/cpt;}
		if(partiel) this.back();
		return c;
	}
	
	public GroupeTachesTez groupeTachesElu(int instantCourant,boolean partiel){
		GroupeTachesTez elu=null;
		boolean trouv=false;
		int indexRessouces=0;
		
		for(GroupeTachesTez tache : allGroupsTaches){
			trouv=false;
			for(MachinePhysique mp : cloud.listeMachinesPhysique){
				for(VM vm : mp.ListeVMs){
						indexRessouces=0;
						for(GroupeRessources g:vm.groupeTezRessources){
							if(g==tache.ressource){
								trouv=true; break;
							}
							indexRessouces++;
						}
						if(trouv) break;
				}
				if(trouv) break;
			}
			if(tache.ressource!=null && tache.pret(cloud,instantCourant) && tache.ressource.vm.verifierDisponibiliteTez(indexRessouces,instantCourant,tache.stage.dureeTacheTez)){
				if(elu==null 
						|| tache.ordre<elu.ordre)
				{
					elu=tache;
					//return elu;
				}
			}
		}
			
		return elu;
	}
	
	public double coutOrdonnancementTotal(){
		for(ClasseClients cc : cloud.listeClassesClient){
			for(RequeteTez r : cc.requeteTezEnAttente){
				for(StageTez stage : r.listeStages){
					for(GroupeTachesTez tache : stage.groupesTezTaches){
						if(tache.fini==1){
							if(tache.dateFin>r.dateFinReelle){
								r.dateFinReelle=tache.dateFin;
							}
						}
						else{
							System.out.println("Stage non finie");
						}
					}
				}
			}
		}
		
		double coutTotal=0;
		for(ClasseClients cc : cloud.listeClassesClient){
			for(RequeteTez r : cc.requeteTezEnAttente){
				if(r.dateFinReelle-r.dateLimite>0){
					coutTotal+=(r.dateFinReelle-r.dateLimite-1)*r.poids;
				}
			}
		}
		
		System.out.println("Cout pénalités - : "+coutTotal);
		return coutTotal;
	}
	
	
	public Cout calculCout(boolean partiel){
		Cout c=new Cout();
		ArrayList<GroupeTachesTez> allGroupsTezTaches;
		ArrayList<GroupeRessources> allGroupsTezSlots;
		allGroupsTezTaches=new ArrayList<GroupeTachesTez>();
		allGroupsTezSlots=new ArrayList<GroupeRessources>();
		
		double Pcomm=VariablesGlobales.Pcomm;
		double Pproc=VariablesGlobales.Pproc;
		double Pmem=VariablesGlobales.Pmem;
		double Pstor=VariablesGlobales.Pstor;
		double Ppenalites=VariablesGlobales.Ppenalites;
		
		for(ClasseClients cc : cloud.listeClassesClient){
			for(RequeteTez r : cc.requeteTezEnAttente){
				for(StageTez stage : r.listeStages){
					for(GroupeTachesTez tache : stage.groupesTezTaches){
						allGroupsTezTaches.add(tache);
					}
				}
			}
		}
		
		for(MachinePhysique mp:cloud.listeMachinesPhysique){
			for(VM vm:mp.ListeVMs){
				for(GroupeRessources tache: vm.groupeTezRessources){
					allGroupsTezSlots.add(tache);
				}
			}
		}
		
		double W[][]=new double[allGroupsTezSlots.size()][allGroupsTezSlots.size()];
		for(int i=0;i<allGroupsTezSlots.size();i++){
			for(int j=0;j<allGroupsTezSlots.size();j++){
				W[i][j]=0;
			}
		}
			
		if(partiel){
			for(GroupeTachesTez tache1:allGroupsTezTaches){
				if(tache1.ressource!=null){
					for(GroupeTachesTez tache2:allGroupsTezTaches){
						if(tache2.ressource!=null && tache1.stage.requeteTez.index==tache2.stage.requeteTez.index){
							W[tache1.ressource.index][tache2.ressource.index]=Math.max(W[tache1.ressource.index][tache2.ressource.index], tache1.stage.requeteTez.getQuantiteTransfertStages(tache1.stage, tache2.stage));
						}
					}
				}
			}
		}
		else{
			for(GroupeTachesTez tache1:allGroupsTezTaches){
				if(tache1.ressource!=null){
					for(GroupeTachesTez tache2:allGroupsTezTaches){
						if(tache2.ressource!=null && tache1.stage.requeteTez.index==tache2.stage.requeteTez.index){
							W[tache1.ressource.index][tache2.ressource.index]+=tache1.stage.requeteTez.getQuantiteTransfertStages(tache1.stage, tache2.stage);
						}
					}
				}
			}
		}
		
		coutComm=0;
		for(GroupeRessources a:allGroupsTezSlots){
			for(GroupeRessources b:allGroupsTezSlots){
				coutComm+=W[a.index][b.index]*cloud.getDistanceEntreSlots(a.index, b.index);
			}
		}
		coutComm*=VariablesGlobales.Pcomm;
		
		coutProc=0;
		coutMem=0;
		for(GroupeTachesTez tache:allGroupsTezTaches){
			if(tache.ressource!=null){
				coutProc+=tache.ressource.vm.processeurTezSlots*tache.stage.dureeTacheTez;
				coutMem+=tache.ressource.vm.memoireTezSlots*tache.stage.dureeTacheTez;
			}
		}
		
		/////////////////////////
		
		for(ClasseClients cc : cloud.listeClassesClient){
			for(RequeteTez r : cc.requeteTezEnAttente){
				for(StageTez stage : r.listeStages){
					for(GroupeTachesTez tache : stage.groupesTezTaches){
						if(tache.fini==1){
							if(tache.dateFin>r.dateFinReelle){
								r.dateFinReelle=tache.dateFin;
							}
						}
					}
				}
			}
		}
		
		double coutPenalite=0;
		for(ClasseClients cc : cloud.listeClassesClient){
			for(RequeteTez r : cc.requeteTezEnAttente){
				if(r.dateFinReelle-r.dateLimite>0){
					coutPenalite+=(r.dateFinReelle-r.dateLimite-1)*r.poids;
				}
				else{
					//if(partiel)
					//	coutPenalite+=((double)r.dateFinReelle/(double)r.dateLimite)*r.poids;
				}
			}
		}
		
		for(ClasseClients cc : cloud.listeClassesClient){
			for(RequeteTez r : cc.requeteTezEnAttente){
				for(StageTez stage : r.listeStages){
					r.dateFinReelle=0;
				}
			}
		}
		
		c.coutComm=coutComm;
		c.coutProcesseur=coutProc;
		c.coutMemoire=coutMem;
		c.coutStockage=coutStor;
		c.coutPenalite=coutPenalite;
		return c;
	}
	
	public void coutPlacementTotal(){
		for(ClasseClients cc : cloud.listeClassesClient){
			for(RequeteTez r : cc.requeteTezEnAttente){
				for(StageTez stage : r.listeStages){
					for(GroupeTachesTez tache : stage.groupesTezTaches){
						allGroupsTaches.add(tache);
					}
				}
			}
		}
		
		for(MachinePhysique mp:cloud.listeMachinesPhysique){
			for(VM vm:mp.ListeVMs){
				for(GroupeRessources res: vm.groupeTezRessources){
					allGroupsTezSlots.add(res);
				}
			}
		}
		
		double W[][]=new double[allGroupsTezSlots.size()][allGroupsTezSlots.size()];
		for(int i=0;i<allGroupsTezSlots.size();i++){
			for(int j=0;j<allGroupsTezSlots.size();j++){
				W[i][j]=0;
			}
		}
			
		for(GroupeTachesTez tache1:allGroupsTaches){
			for(GroupeTachesTez tache2:allGroupsTaches){
				if(tache1.stage.requeteTez.index==tache2.stage.requeteTez.index){
					W[tache1.ressource.index][tache2.ressource.index]=Math.max(W[tache1.ressource.index][tache2.ressource.index], tache1.stage.requeteTez.getQuantiteTransfertStages(tache1.stage, tache2.stage));
				}
			}
		}
		
		coutComm=0;
		for(GroupeRessources a:allGroupsTezSlots){
			for(GroupeRessources b:allGroupsTezSlots){
				coutComm+=W[a.index][b.index]*cloud.getDistanceEntreSlots(a.index, b.index);
			}
		}
		coutComm*=VariablesGlobales.Pcomm;
		
		coutProc=0;
		coutMem=0;
		coutStor=0;
		for(GroupeTachesTez tache:allGroupsTaches){
			coutProc+=tache.ressource.vm.processeurTezSlots*tache.stage.dureeTacheTez;
			coutMem+=tache.ressource.vm.memoireTezSlots*tache.stage.dureeTacheTez;
			coutStor+=tache.ressource.vm.stockageTezSlots*tache.stage.dureeTacheTez;
		}
		
		System.out.println("Cout communication : "+coutComm);
		System.out.println("Cout processeur : "+coutProc);
		System.out.println("Cout mémoire : "+coutMem);
		System.out.println("Cout stockage : "+coutStor);
		//return coutComm+coutProc+coutMem+coutStor;
	}

	public Gantt ecrireResultats(){
		Gantt gantt=new Gantt();
	    for(ClasseClients cc:this.cloud.listeClassesClient){
	    	for(RequeteTez rq:cc.requeteTezEnAttente){
	    		for(StageTez stage:rq.listeStages){
	    			for(GroupeTachesTez tache:stage.groupesTezTaches){
	    				gantt.ajouterTrancheTemps(new TrancheTempsAlloue(1, tache.ressource.index, rq.index, stage.indexStage, tache.index, tache.tempsDeclanchement, (tache.dateFin-1)));
	    			}
	    		}
	    	}
	    }

	    return gantt;
	}	
	
	
	public void stock(){
		for(GroupeRessources gr:this.allGroupsTezSlots){
			gr.dispoStock();
		}
		for(GroupeTachesTez tache:this.allGroupsTaches){
			tache.attributsStock();
		}
		for(MachinePhysique mp : cloud.listeMachinesPhysique){
			for(VM vm : mp.ListeVMs){
				vm.disponibiliteTrancheTempsStock();
			}
		}
	}
	
	public void back(){
		allGroupsTaches=new ArrayList<GroupeTachesTez>();
		for(ClasseClients cc : cloud.listeClassesClient){
			for(RequeteTez r : cc.requeteTezEnAttente){
				for(StageTez stage : r.listeStages){
					for(GroupeTachesTez tache : stage.groupesTezTaches){
						allGroupsTaches.add(tache);
					}
				}
			}
		}
		for(GroupeRessources gr:this.allGroupsTezSlots){
			gr.dispoback();
		}
		for(GroupeTachesTez tache:this.allGroupsTaches){
			tache.attributsBack();
		}
		for(MachinePhysique mp : cloud.listeMachinesPhysique){
			for(VM vm : mp.ListeVMs){
				vm.disponibiliteTrancheTempsBack();
			}
		}
	}
}
