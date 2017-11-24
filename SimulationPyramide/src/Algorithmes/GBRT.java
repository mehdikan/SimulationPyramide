package Algorithmes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;

import Divers.CSVUtils;
import Divers.EvenementFinTaches;
import Divers.Statistics;
import Divers.VariablesGlobales;
import Entite.*;
import Gantt.Gantt;
import Gantt.TrancheTempsAlloue;

public class GBRT extends GenericGreedy {
		
	public GBRT(Cloud cloud){
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
	
	
	
	
	public GroupeTaches next(HashSet<GroupeTaches> readyMaps,HashSet<GroupeTaches> readyReduces){
		GroupeTaches candidat=null;
		for(GroupeTaches t:readyReduces){
			if(candidat==null || t.duree>candidat.duree){
				candidat=t;
			}
		}
		for(GroupeTaches t:readyMaps){
			if(candidat==null || t.duree>candidat.duree){
				candidat=t;
			}
		}	
		return candidat;
	}
	
	
	public GroupeRessources affecter(GroupeTaches n,ArrayList<GroupeRessources> candidates){
		HashMap<GroupeRessources,Integer> chargeRessources=new HashMap<GroupeRessources,Integer>();
		GroupeRessources ressChoisie=null;
		if(n.type==0){
			for(GroupeRessources ressMap:allGroupsMapSlots){
				chargeRessources.put(ressMap, 0);
			}
			for(GroupeTaches tacheMap:allGroupsMapTaches){
				if(tacheMap.ressource!=null){
					chargeRessources.put(tacheMap.ressource, chargeRessources.get(tacheMap.ressource)+tacheMap.duree);
				}
			}
			
			double deviation=-1;
			for(GroupeRessources ressMap:candidates){
				if(ressMap.type==0){
					chargeRessources.put(ressMap, chargeRessources.get(ressMap)+n.duree);
					Statistics stat=new Statistics(chargeRessources);
					double var=stat.getVariance();
					if(deviation==-1 || var<deviation){
						boolean trouv=false;
						for(GroupeTaches tacheMap:allGroupsMapTaches){
							if(tacheMap.job==n.job && tacheMap.ressource==ressMap){
								trouv=true;
							}
						}
						if(!trouv){
							deviation=var;
							ressChoisie=ressMap;
						}
					}
					chargeRessources.put(ressMap, chargeRessources.get(ressMap)-n.duree);
				}
			}
		}
		else{
			for(GroupeRessources ressReduce:allGroupsReduceSlots){
				chargeRessources.put(ressReduce, 0);
			}
			for(GroupeTaches tacheReduce:allGroupsReduceTaches){
				if(tacheReduce.ressource!=null){
					chargeRessources.put(tacheReduce.ressource, chargeRessources.get(tacheReduce.ressource)+tacheReduce.duree);
				}
			}
			
			double deviation=-1;
			for(GroupeRessources ressReduce:candidates){
				if(ressReduce.type==1){
					chargeRessources.put(ressReduce, chargeRessources.get(ressReduce)+n.duree);
					Statistics stat=new Statistics(chargeRessources);
					double var=stat.getVariance();
					if(deviation==-1 || var<deviation){
						boolean trouv=false;
						for(GroupeTaches tacheReduce:allGroupsReduceTaches){
							if(tacheReduce.job==n.job && tacheReduce.ressource==ressReduce){
								trouv=true;
							}
						}
						if(!trouv){
							deviation=var;
							ressChoisie=ressReduce;
						}
					}
					chargeRessources.put(ressReduce, chargeRessources.get(ressReduce)-n.duree);
				}
			}
		}
		
		return ressChoisie;
	}
}
