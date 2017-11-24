package Algorithmes;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;
import org.gnu.glpk.GLPK;
import Divers.EvenementFinTaches;
import Divers.VariablesGlobales;
import Entite.*;
import Gantt.Gantt;
import Gantt.TrancheTempsAlloue;

public class FIFO {
	Cloud cloud;
	ArrayList<GroupeTaches> allGroupsMapTaches;
	ArrayList<GroupeTaches> allGroupsReduceTaches;
	ArrayList<GroupeRessources> allGroupsMapSlots;
	ArrayList<GroupeRessources> allGroupsReduceSlots;
	TreeSet<EvenementFinTaches> evenements;
	double coutComm=-1;
	double coutProc=-1;
	double coutMem=-1;
	double coutStor=-1;
	
	public FIFO(Cloud cloud){
		this.cloud=cloud;
		allGroupsMapTaches=new ArrayList<GroupeTaches>();
		allGroupsReduceTaches=new ArrayList<GroupeTaches>();
		allGroupsMapSlots=new ArrayList<GroupeRessources>();
		allGroupsReduceSlots=new ArrayList<GroupeRessources>();
		
		evenements=new TreeSet<EvenementFinTaches>();
		
		for(ClasseClients cc : cloud.listeClassesClient){
			for(Requete r : cc.requeteEnAttente){
				for(Job job : r.listeJobs){
					for(GroupeTaches map : job.groupesMapTaches){
						allGroupsMapTaches.add(map);
					}
					for(GroupeTaches reduce : job.groupesReduceTaches){
						allGroupsReduceTaches.add(reduce);
					}
				}
			}
		}
	}
	
	public void lancer(){
		EvenementFinTaches evCourant;
		int tCourant;
		evenements.add(new EvenementFinTaches(1,null,null));
		GroupeTaches elu;
		
		while(!evenements.isEmpty() )
		{
			evCourant=evenements.first();
			tCourant=evCourant.instant;
			System.out.println("Instant : "+tCourant);
			for(GroupeRessources gr:evCourant.ressorceALiberer){ 
				gr.setLibre();
				System.out.println("Groupe de Ressources "+gr.type+"-"+gr.index+" libérée");
			}
			for(GroupeTaches tache:evCourant.tachesFinies){ 
				tache.fini=1;
				tache.dateFin=tCourant;
				System.out.println("Groupe de taches "+tache.type+"-"+tache.index+" finies");
			}
			evenements.remove(evenements.first());
			while(allGroupsMapTaches.size()>0 || allGroupsReduceTaches.size()>0){	
				elu=groupeTachesElu(tCourant);
				
				if(elu!=null){
					elu.tempsDeclanchement=tCourant;
					GroupeRessources ress=this.placementNaif(cloud,elu,tCourant);
					if(ress!=null)
					System.out.println("Groupes de taches "+elu.type+"-"+elu.index+" est elue et placé dans la ressource "+ress.type+"-"+ress.index);
					
					if(elu.type==0){
						 Iterator<EvenementFinTaches> iterator = evenements.iterator(); 
						 boolean trouv=false;
					      while (iterator.hasNext()){
					         EvenementFinTaches ev=(EvenementFinTaches) iterator.next();
					         if(ev.instant==tCourant+elu.job.dureeTacheMap){
					        	 trouv=true;
					        	 System.out.println("1 "+ress);
					        	 ev.ressorceALiberer.add(ress);
					        	 ev.tachesFinies.add(elu);
					        	 break;
					         }
					      }
					      if(!trouv){
					    	  evenements.add(new EvenementFinTaches(tCourant+elu.job.dureeTacheMap,ress,elu));
					      }
					      allGroupsMapTaches.remove(elu);
					}
					else{
						Iterator<EvenementFinTaches> iterator = evenements.iterator(); 
						 boolean trouv=false;
					      while (iterator.hasNext()){
					         EvenementFinTaches ev=(EvenementFinTaches) iterator.next();
					         if(ev.instant==tCourant+elu.job.dureeTacheReduce){
					        	 trouv=true;
					        	 ev.ressorceALiberer.add(ress);
					        	 ev.tachesFinies.add(elu);
					        	 break;
					         }
					      }
					      if(!trouv){
					    	  evenements.add(new EvenementFinTaches(tCourant+elu.job.dureeTacheReduce,ress,elu));
					      }
						allGroupsReduceTaches.remove(elu);
					}
				}
				else{
					evenements.add(new EvenementFinTaches(tCourant+1,null,null));
					break;
				}
			}
		}
	}
	
	public GroupeTaches groupeTachesElu(int instantCourant){
		GroupeTaches elu=null;
		
		for(GroupeTaches map : allGroupsMapTaches){
			if(map.pret(cloud,instantCourant)){
				if(elu==null 
						|| map.job.ordreArrive<elu.job.ordreArrive)
				{
					elu=map;
					//return elu;
				}
			}
		}
		
		for(GroupeTaches reduce : allGroupsReduceTaches){
			if(reduce.pret(cloud,instantCourant)){
				if(elu==null 
						|| reduce.job.ordreArrive<reduce.job.ordreArrive)
				{
					elu=reduce;
					//return elu;
				}
			}
		}
		
		return elu;
	}
	
	public double coutOrdonnancementTotal(){
		for(ClasseClients cc : cloud.listeClassesClient){
			for(Requete r : cc.requeteEnAttente){
				for(Job job : r.listeJobs){
					for(GroupeTaches reduce : job.groupesReduceTaches){
						if(reduce.fini==1){
							if(reduce.dateFin>r.dateFinReelle){
								r.dateFinReelle=reduce.dateFin;
							}
						}
						else{
							System.out.println("Job non finie");
						}
					}
				}
			}
		}
		
		double coutTotal=0;
		for(ClasseClients cc : cloud.listeClassesClient){
			for(Requete r : cc.requeteEnAttente){
				if(r.dateFinReelle-r.dateLimite>0){
					coutTotal+=(r.dateFinReelle-r.dateLimite-1)*r.poids;
				}
			}
		}
		
		System.out.println("Cout pénalités : "+coutTotal);
		return coutTotal;
	}
	
	public void coutPlacementTotal(){
		for(ClasseClients cc : cloud.listeClassesClient){
			for(Requete r : cc.requeteEnAttente){
				for(Job job : r.listeJobs){
					for(GroupeTaches map : job.groupesMapTaches){
						allGroupsMapTaches.add(map);
					}
					for(GroupeTaches reduce : job.groupesReduceTaches){
						allGroupsReduceTaches.add(reduce);
					}
				}
			}
		}
		
		for(MachinePhysique mp:cloud.listeMachinesPhysique){
			for(VM vm:mp.ListeVMs){
				for(GroupeRessources map: vm.groupeMapRessources){
					allGroupsMapSlots.add(map);
				}
				for(GroupeRessources reduce: vm.groupeReduceRessources){
					allGroupsReduceSlots.add(reduce);
				}
			}
		}
		
		double W[][]=new double[allGroupsMapSlots.size()][allGroupsReduceSlots.size()];
		for(int i=0;i<allGroupsMapSlots.size();i++){
			for(int j=0;j<allGroupsReduceSlots.size();j++){
				W[i][j]=0;
			}
		}
			
		for(GroupeTaches map:allGroupsMapTaches){
			for(GroupeTaches reduce:allGroupsReduceTaches){
				if(map.job.requete.index==reduce.job.requete.index){
					W[map.ressource.index][reduce.ressource.index]=Math.max(W[map.ressource.index][reduce.ressource.index], map.job.requete.getQuantiteTransfertJobs(map.job, reduce.job));
					W[map.ressource.index][reduce.ressource.index]=Math.max(W[map.ressource.index][reduce.ressource.index], map.job.requete.getQuantiteTransfertJobs(reduce.job, map.job));
				}
			}
		}
		
		coutComm=0;
		for(GroupeRessources a:allGroupsMapSlots){
			for(GroupeRessources b:allGroupsReduceSlots){
				coutComm+=W[a.index][b.index]*cloud.getDistanceEntreSlots(a.index, b.index);
			}
		}
		coutComm*=VariablesGlobales.Pcomm;
		
		coutProc=0;
		coutMem=0;
		coutStor=0;
		for(GroupeTaches map:allGroupsMapTaches){
			coutProc+=map.ressource.vm.processeurMapSlots*map.job.dureeTacheMap;
			coutMem+=map.ressource.vm.memoireMapSlots*map.job.dureeTacheMap;
			coutStor+=map.ressource.vm.stockageMapSlots*map.job.dureeTacheMap;
		}
		for(GroupeTaches reduce:allGroupsReduceTaches){
			coutProc+=reduce.ressource.vm.processeurReduceSlots*reduce.job.dureeTacheReduce;
			coutMem+=reduce.ressource.vm.memoireReduceSlots*reduce.job.dureeTacheReduce;
			coutStor+=reduce.ressource.vm.stockageReduceSlots*reduce.job.dureeTacheReduce;
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
	    	for(Requete rq:cc.requeteEnAttente){
	    		for(Job jb:rq.listeJobs){
	    			for(GroupeTaches map:jb.groupesMapTaches){
	    				gantt.ajouterTrancheTemps(new TrancheTempsAlloue(1, map.ressource.index, rq.index, jb.indexJob, map.index, map.tempsDeclanchement, (map.dateFin-1)));
	    			}
	    		}
	    	}
	    }
	    
	    for(ClasseClients cc:this.cloud.listeClassesClient){
	    	for(Requete rq:cc.requeteEnAttente){
	    		for(Job jb:rq.listeJobs){
	    			for(GroupeTaches reduce:jb.groupesReduceTaches){
	    				gantt.ajouterTrancheTemps(new TrancheTempsAlloue(2, reduce.ressource.index, rq.index, jb.indexJob, reduce.index, reduce.tempsDeclanchement-1, (reduce.dateFin-1)));
	    			}
	    		}
	    	}
	    }
	    return gantt;
	}
	
	public GroupeRessources placementNaif(Cloud cloud,GroupeTaches tachesG,int instantCourant){
		GroupeRessources ggg=null;
		for(MachinePhysique mp : cloud.listeMachinesPhysique){
			for(VM vm : mp.ListeVMs){
				if(tachesG.type==0
						&& tachesG.job.processeurTacheMap<=vm.processeurMapSlots
						&& tachesG.job.memoireTacheMap<=vm.memoireMapSlots
						&& tachesG.job.stockageTacheMap<=vm.stockageMapSlots){
					for(GroupeRessources g:vm.groupeMapRessources){
						int indexRessouces=0;
						if(g.getDisponibilite()==1 && vm.verifierDisponibiliteMap(indexRessouces,instantCourant,tachesG.job.dureeTacheMap)) {
							boolean trouv=false;
							for(GroupeTaches gg:tachesG.job.groupesMapTaches){
								if(gg.ressource==g){
									trouv=true;
								}
							}
							if(!trouv){
								if(tachesG.ressource==null || g.ordreLiberation<tachesG.ressource.ordreLiberation){
									tachesG.ressource=g;
									ggg=g;
								}
							}
						}
						indexRessouces++;
					}
				}
				else if(tachesG.type==1
						&& tachesG.job.processeurTacheReduce<=vm.processeurReduceSlots
						&& tachesG.job.memoireTacheReduce<=vm.memoireReduceSlots
						&& tachesG.job.stockageTacheReduce<=vm.stockageReduceSlots){
					for(GroupeRessources g:vm.groupeReduceRessources){
						int indexRessouces=0;
						if(g.getDisponibilite()==1 && vm.verifierDisponibiliteReduce(indexRessouces,instantCourant,tachesG.job.dureeTacheReduce) /*vm.disponibliteTrancheTempsReduce[indexRessouces][instantCourant]==1*/) {
							boolean trouv=false;
							for(GroupeTaches gg:tachesG.job.groupesReduceTaches){
								if(gg.ressource==g){
									trouv=true;
								}
							}
							if(!trouv){
								if(tachesG.ressource==null || g.ordreLiberation<tachesG.ressource.ordreLiberation){
									tachesG.ressource=g;
									ggg=g;
								}
							}
						}
						indexRessouces++;
					}
				}
			}
		}
		if(ggg!=null) ggg.setAlloue();
		return ggg;
	}
	
}
