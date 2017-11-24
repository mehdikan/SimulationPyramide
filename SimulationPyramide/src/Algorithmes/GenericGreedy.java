package Algorithmes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;
import Divers.EvenementFinTaches;
import Divers.Statistics;
import Divers.VariablesGlobales;
import Entite.*;
import Gantt.Gantt;
import Gantt.TrancheTempsAlloue;

public abstract class GenericGreedy {
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
	public int ordre=1;
	
	public GenericGreedy(){}
	
	public GenericGreedy(Cloud cloud){
		this.cloud=cloud;
		allGroupsMapTaches=new ArrayList<GroupeTaches>();
		allGroupsReduceTaches=new ArrayList<GroupeTaches>();
		allGroupsMapSlots=new ArrayList<GroupeRessources>();
		allGroupsReduceSlots=new ArrayList<GroupeRessources>();
		
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
	}
	
	public Cout lancer(){
		placer();
		for(GroupeTaches map:allGroupsMapTaches){
			map.fini=0;
		}
		for(GroupeTaches reduce:allGroupsReduceTaches){
			reduce.fini=0;
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
		HashSet<GroupeTaches> readyMaps=new HashSet<GroupeTaches>();
		HashSet<GroupeTaches> readyReduces=new HashSet<GroupeTaches>();
		// ajouter les taches sans dependances	
		for(GroupeTaches map : allGroupsMapTaches){
			if(map.dependances.size()==0){
				readyMaps.add(map);
			}
		}
		for(GroupeTaches reduce : allGroupsReduceTaches){
			if(reduce.dependances.size()==0){
				readyReduces.add(reduce);
			}
		}
		
		while(readyMaps.size()!=0 || readyReduces.size()!=0){
			GroupeTaches n=next(readyMaps,readyReduces);
			ArrayList<GroupeRessources> candidates=ressourcesCandidates(n);
			if(candidates.size()==0){
				System.out.println("EEEEEEEEEEEEreur");
				return;
			}
			
			GroupeRessources c=affecter(n,candidates);
			if(n.type==0){ readyMaps.remove(n); }
			else{ readyReduces.remove(n);}
			n.fini=1;
			n.ressource=c;
			n.ordre=this.ordre;
			this.ordre++;
			
			for(GroupeTaches map : allGroupsMapTaches){
				if(map.fini==0){
					boolean trouv=false;
					for(GroupeTaches depend : map.dependances){
						if(depend.fini==0){
							trouv=true;
							break;
						}
					}
					if(!trouv){
						readyMaps.add(map);
					}
				}
			}
			for(GroupeTaches reduce : allGroupsReduceTaches){
				if(reduce.fini==0){
					boolean trouv=false;
					for(GroupeTaches depend : reduce.dependances){
						if(depend.fini==0){
							trouv=true;
							break;
						}
					}
					if(!trouv){
						readyReduces.add(reduce);
					}
				}
			}
		}
	}
	
	public abstract GroupeTaches next(HashSet<GroupeTaches> readyMaps,HashSet<GroupeTaches> readyReduces);
	
	ArrayList<GroupeRessources> ressourcesCandidates(GroupeTaches n){
		ArrayList<GroupeRessources> ressCandidates=new ArrayList<GroupeRessources>();
		if(n.type==0){
			for(MachinePhysique mp : cloud.listeMachinesPhysique){
				for(VM vm : mp.ListeVMs){
					if(n.job.processeurTacheMap<=vm.processeurMapSlots
						&& n.job.memoireTacheMap<=vm.memoireMapSlots
						&& n.job.stockageTacheMap<=vm.stockageMapSlots){
						for(GroupeRessources g:vm.groupeMapRessources){
							ressCandidates.add(g);
						}
					}
				}
			}
		}
		else{
			for(MachinePhysique mp : cloud.listeMachinesPhysique){
				for(VM vm : mp.ListeVMs){
					if(n.job.processeurTacheReduce<=vm.processeurReduceSlots
						&& n.job.memoireTacheReduce<=vm.memoireReduceSlots
						&& n.job.stockageTacheReduce<=vm.stockageReduceSlots){
						for(GroupeRessources g:vm.groupeReduceRessources){
							ressCandidates.add(g);
						}
					}
				}
			}
		}
		return ressCandidates;
	}
	
	public abstract GroupeRessources affecter(GroupeTaches n,ArrayList<GroupeRessources> candidates);
	
	public Cout ordonnancer(boolean partiel){
		EvenementFinTaches evCourant;
		int tCourant;
		evenements=new TreeSet<EvenementFinTaches>();
		evenements.add(new EvenementFinTaches(1,null,null));
		GroupeTaches elu;
		int tempsMax=0;
		Cout c=new Cout();
		
		if(partiel) this.stock();
		while(!evenements.isEmpty() )
		{
			evCourant=evenements.first();
			tCourant=evCourant.instant;
			//if(tCourant>40) break;
			if(!partiel) System.out.println("Instant : "+tCourant);
			for(GroupeRessources gr:evCourant.ressorceALiberer){ 
				gr.setLibre();
				if(!partiel) System.out.println("Groupe de Ressources "+gr.type+"-"+gr.index+" libérée"+"- temps :"+tCourant);
			}
			for(GroupeTaches tache:evCourant.tachesFinies){ 
				tache.fini=1;
				tache.dateFin=tCourant;
				tempsMax=Math.max(tempsMax, tCourant);
				tache.job.requete.tempsGMPT=Math.max(tache.job.requete.tempsGMPT, tCourant);
				//System.out.println("# "+tempsMax);
				if(!partiel) System.out.println("Groupe de taches "+tache.type+"-"+tache.index+" finies");
			}
			
			evenements.remove(evenements.first());
			
			boolean trouvTache;
			if(partiel){
				trouvTache=false;
				for(GroupeTaches map:allGroupsMapTaches){
					if(map.ressource!=null){
						trouvTache=true; break;
					}
				}
				for(GroupeTaches reduce:allGroupsReduceTaches){
					if(reduce.ressource!=null){
						trouvTache=true; break;
					}
				}
			}
			else trouvTache=true;
			
			while(trouvTache && (allGroupsMapTaches.size()>0 || allGroupsReduceTaches.size()>0)){	
				//if(tCourant>40) break;
				
				elu=groupeTachesElu(tCourant,partiel);
				//if(tCourant<15) System.out.println("-- "+tCourant+" "+elu);				
				if(elu!=null){
					elu.tempsDeclanchement=tCourant;
					//GroupeRessources ress=this.placementNaif(cloud,elu,tCourant);
					//if(ress!=null)
					if(!partiel) System.out.println("Groupes de taches "+elu.type+"-"+elu.job.requete.index+"-"+elu.job.indexJob+"-"+elu.index+" est elue et placé dans la ressource "+elu.ressource.type+"-"+elu.ressource.index+"- temps: "+tCourant);
					
					if(elu.type==0){
						int indexRessouces=0;
						boolean trouv=false;
						for(GroupeTaches map : allGroupsMapTaches){
							for(MachinePhysique mp : cloud.listeMachinesPhysique){
								for(VM vm : mp.ListeVMs){
										indexRessouces=0;
										for(GroupeRessources g:vm.groupeMapRessources){
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
							elu.ressource.vm.setAlloueMap(indexRessouces,tCourant,elu.job.dureeTacheMap);
						}
						else{
							elu.ressource.vm.setAlloueMap(indexRessouces,tCourant,elu.job.dureeTacheMap);
						}
						 Iterator<EvenementFinTaches> iterator = evenements.iterator(); 
						 trouv=false;
					      while (iterator.hasNext()){
					         EvenementFinTaches ev=(EvenementFinTaches) iterator.next();
					         if(ev.instant==tCourant+elu.job.dureeTacheMap){
					        	 trouv=true;
					        	 ev.ressorceALiberer.add(elu.ressource);
					        	 ev.tachesFinies.add(elu);
					        	 break;
					         }
					      }
					      if(!trouv){
					    	  evenements.add(new EvenementFinTaches(tCourant+elu.job.dureeTacheMap,elu.ressource,elu));
					      }
					      allGroupsMapTaches.remove(elu);
					}
					else{
						int indexRessouces=0;
						boolean trouv=false;
						for(GroupeTaches reduce : allGroupsReduceTaches){
							for(MachinePhysique mp : cloud.listeMachinesPhysique){
								for(VM vm : mp.ListeVMs){
										indexRessouces=0;
										for(GroupeRessources g:vm.groupeReduceRessources){
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
						}
						
						elu.ressource.vm.setAlloueReduce(indexRessouces,tCourant,elu.job.dureeTacheReduce);
						
						Iterator<EvenementFinTaches> iterator = evenements.iterator(); 
						 trouv=false;
					      while (iterator.hasNext()){
					         EvenementFinTaches ev=(EvenementFinTaches) iterator.next();
					         if(ev.instant==tCourant+elu.job.dureeTacheReduce){
					        	 trouv=true;
					        	 ev.ressorceALiberer.add(elu.ressource);
					        	 ev.tachesFinies.add(elu);
					        	 break;
					         }
					      }
					      if(!trouv){
					    	  evenements.add(new EvenementFinTaches(tCourant+elu.job.dureeTacheReduce,elu.ressource,elu));
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
		
		c=calculCout(partiel);
		c.tempsExecTotal=tempsMax;
		int cpt=0;
		c.tempsExecMoyenRequete=0;
		for(ClasseClients cc : cloud.listeClassesClient){
			for(Requete r : cc.requeteEnAttente){
				c.tempsExecMoyenRequete+=(double)r.tempsGMPT;
				r.tempsGMPT=0;
				cpt++;
			}
		}
		if(cpt>0) { c.tempsExecMoyenRequete=c.tempsExecMoyenRequete/cpt;}
		if(partiel) this.back();
		return c;
	}
	
	public GroupeTaches groupeTachesElu(int instantCourant,boolean partiel){
		GroupeTaches elu=null;
		boolean trouv=false;
		int indexRessouces=0;
		
		for(GroupeTaches map : allGroupsMapTaches){
			trouv=false;
			for(MachinePhysique mp : cloud.listeMachinesPhysique){
				for(VM vm : mp.ListeVMs){
						indexRessouces=0;
						for(GroupeRessources g:vm.groupeMapRessources){
							if(g==map.ressource){
								trouv=true; break;
							}
							indexRessouces++;
						}
						if(trouv) break;
				}
				if(trouv) break;
			}
			if(map.ressource!=null && map.pret(cloud,instantCourant) && map.ressource.vm.verifierDisponibiliteMap(indexRessouces,instantCourant,map.job.dureeTacheMap)){
				if(elu==null 
						|| map.ordre<elu.ordre)
				{
					elu=map;
					//return elu;
				}
			}
		}
		
		for(GroupeTaches reduce : allGroupsReduceTaches){
			trouv=false;
			indexRessouces=0;
			for(MachinePhysique mp : cloud.listeMachinesPhysique){
				for(VM vm : mp.ListeVMs){
						indexRessouces=0;
						for(GroupeRessources g:vm.groupeReduceRessources){
							if(g==reduce.ressource){
								trouv=true; break;
							}
							indexRessouces++;
						}
						if(trouv) break;
				}
				if(trouv) break;
			}
			if(reduce.ressource!=null && reduce.pret(cloud,instantCourant) && reduce.ressource.vm.verifierDisponibiliteReduce(indexRessouces,instantCourant,reduce.job.dureeTacheReduce)){
				if(elu==null 
						|| reduce.ordre<reduce.ordre)
				{
					elu=reduce;
					//return elu;
				}
			}
		}
		
		if(elu!=null && !partiel){
			

		}
		else{
			//System.out.println("null");
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
		
		System.out.println("Cout pénalités - : "+coutTotal);
		return coutTotal;
	}
	
	
	public Cout calculCout(boolean partiel){
		Cout c=new Cout();
		ArrayList<GroupeTaches> allGroupsMapTaches;
		ArrayList<GroupeTaches> allGroupsReduceTaches;
		ArrayList<GroupeRessources> allGroupsMapSlots;
		ArrayList<GroupeRessources> allGroupsReduceSlots;
		allGroupsMapTaches=new ArrayList<GroupeTaches>();
		allGroupsReduceTaches=new ArrayList<GroupeTaches>();
		allGroupsMapSlots=new ArrayList<GroupeRessources>();
		allGroupsReduceSlots=new ArrayList<GroupeRessources>();
		
		double Pcomm=VariablesGlobales.Pcomm;
		double Pproc=VariablesGlobales.Pproc;
		double Pmem=VariablesGlobales.Pmem;
		double Pstor=VariablesGlobales.Pstor;
		double Ppenalites=VariablesGlobales.Ppenalites;
		
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
			
		if(partiel){
			for(GroupeTaches map:allGroupsMapTaches){
				if(map.ressource!=null){
					for(GroupeTaches reduce:allGroupsReduceTaches){
						if(reduce.ressource!=null && map.job.requete.index==reduce.job.requete.index){
							if(map.job.indexJob==reduce.job.indexJob){
								W[map.ressource.index][reduce.ressource.index]=Math.max(W[map.ressource.index][reduce.ressource.index], map.job.requete.getQuantiteTransfertJobs(map.job, reduce.job));
							}
							else{
								W[map.ressource.index][reduce.ressource.index]=Math.max(W[map.ressource.index][reduce.ressource.index], map.job.requete.getQuantiteTransfertJobs(reduce.job, map.job));
							}
						}
					}
				}
			}
		}
		else{
			for(GroupeTaches map:allGroupsMapTaches){
				if(map.ressource!=null){
					for(GroupeTaches reduce:allGroupsReduceTaches){
						if(reduce.ressource!=null && map.job.requete.index==reduce.job.requete.index){
							if(map.job.indexJob==reduce.job.indexJob){
								//System.out.println(map.ressource.index+"-"+reduce.ressource.index);
								W[map.ressource.index][reduce.ressource.index]+=map.job.requete.getQuantiteTransfertJobs(map.job, reduce.job);
							}else{
								W[map.ressource.index][reduce.ressource.index]+=map.job.requete.getQuantiteTransfertJobs(reduce.job, map.job);
							}
						}
					}
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
			if(map.ressource!=null){
				coutProc+=map.ressource.vm.processeurMapSlots*map.job.dureeTacheMap;
				coutMem+=map.ressource.vm.memoireMapSlots*map.job.dureeTacheMap;
				coutStor+=map.ressource.vm.stockageMapSlots*map.job.dureeTacheMap;
			}
		}
		for(GroupeTaches reduce:allGroupsReduceTaches){
			if(reduce.ressource!=null){
				coutProc+=reduce.ressource.vm.processeurReduceSlots*reduce.job.dureeTacheReduce;
				coutMem+=reduce.ressource.vm.memoireReduceSlots*reduce.job.dureeTacheReduce;
				coutStor+=reduce.ressource.vm.stockageReduceSlots*reduce.job.dureeTacheReduce;
			}
		}
		
		/////////////////////////
		
		for(ClasseClients cc : cloud.listeClassesClient){
			for(Requete r : cc.requeteEnAttente){
				for(Job job : r.listeJobs){
					for(GroupeTaches map : job.groupesMapTaches){
						if(map.fini==1){
							if(map.dateFin>r.dateFinReelle){
								r.dateFinReelle=map.dateFin;
							}
						}
					}
					for(GroupeTaches reduce : job.groupesReduceTaches){
						if(reduce.fini==1){
							if(reduce.dateFin>r.dateFinReelle){
								r.dateFinReelle=reduce.dateFin;
							}
						}
					}
				}
			}
		}
		
		double coutPenalite=0;
		for(ClasseClients cc : cloud.listeClassesClient){
			for(Requete r : cc.requeteEnAttente){
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
			for(Requete r : cc.requeteEnAttente){
				for(Job job : r.listeJobs){
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
	    				gantt.ajouterTrancheTemps(new TrancheTempsAlloue(2, reduce.ressource.index, rq.index, jb.indexJob, reduce.index, reduce.tempsDeclanchement, (reduce.dateFin-1)));
	    			}
	    		}
	    	}
	    }
	    return gantt;
	}	
	
	
	public void stock(){
		for(GroupeRessources gr:this.allGroupsMapSlots){
			gr.dispoStock();
		}
		for(GroupeRessources gr:this.allGroupsReduceSlots){
			gr.dispoStock();
		}
		for(GroupeTaches tache:this.allGroupsMapTaches){
			tache.attributsStock();
		}
		for(GroupeTaches tache:this.allGroupsReduceTaches){
			tache.attributsStock();
		}
		for(MachinePhysique mp : cloud.listeMachinesPhysique){
			for(VM vm : mp.ListeVMs){
				vm.disponibiliteTrancheTempsStock();
			}
		}
	}
	
	public void back(){
		allGroupsMapTaches=new ArrayList<GroupeTaches>();
		allGroupsReduceTaches=new ArrayList<GroupeTaches>();
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
		for(GroupeRessources gr:this.allGroupsMapSlots){
			gr.dispoback();
		}
		for(GroupeRessources gr:this.allGroupsReduceSlots){
			gr.dispoback();
		}
		for(GroupeTaches tache:this.allGroupsMapTaches){
			tache.attributsBack();
		}
		for(GroupeTaches tache:this.allGroupsReduceTaches){
			tache.attributsBack();
		}
		for(MachinePhysique mp : cloud.listeMachinesPhysique){
			for(VM vm : mp.ListeVMs){
				vm.disponibiliteTrancheTempsBack();
			}
		}
	}
}
