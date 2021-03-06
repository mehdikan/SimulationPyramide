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

public class GMPM extends GenericGreedy {
		
	public GMPM(Cloud cloud){
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
			if(candidat==null || t.quantiteTotalOutput(cloud)>candidat.quantiteTotalOutput(cloud)){
				candidat=t;
			}
		}
		for(GroupeTaches t:readyMaps){
			if(candidat==null || t.quantiteTotalOutput(cloud)>candidat.quantiteTotalOutput(cloud)){
				candidat=t;
			}
		}	
		return candidat;
	}
	
	/*public GroupeTaches next(HashSet<GroupeTaches> readyMaps,HashSet<GroupeTaches> readyReduces){
		GroupeTaches candidat=null;
		for(GroupeTaches t:readyReduces){
			if(candidat==null || (double)t.job.requete.poids/(double)t.job.requete.dateLimite>candidat.job.requete.poids/(double)candidat.job.requete.dateLimite){
				candidat=t;
			}
		}
		for(GroupeTaches t:readyMaps){
			if(candidat==null || (double)t.job.requete.poids/(double)t.job.requete.dateLimite>candidat.job.requete.poids/(double)candidat.job.requete.dateLimite){
				candidat=t;
			}
		}	
		return candidat;
	}*/
	
	
	public GroupeRessources affecter(GroupeTaches n,ArrayList<GroupeRessources> candidates){
		GroupeRessources ressChoisie=null;
		double meilleurCout=-1;
		//System.out.println("=============== Type"+n.type+" "+" R"+n.job.requete.index+" J"+n.job.indexJob+" T"+n.index+"Duree"+n.duree);
		for(GroupeRessources ress:candidates){
			boolean trouv=false;
			
			if(n.type==0){
				for(GroupeTaches tacheMap:allGroupsMapTaches){
					if(tacheMap.job==n.job && tacheMap.ressource==ress){
						trouv=true;
					}
				}
			}else{
				for(GroupeTaches tacheReduce:allGroupsReduceTaches){
					if(tacheReduce.job==n.job && tacheReduce.ressource==ress){
						trouv=true;
					}
				}
			}
			
			
			if(!trouv){			
				n.fini=1;
				n.ressource=ress;

				Cout cout=ordonnancer(true);
				//System.out.println("============> "+ress.index+" "+temps);
				if(meilleurCout==-1 || cout.coutTotal()<meilleurCout){
					meilleurCout=cout.coutTotal();
					ressChoisie=ress;	
				}
			}
		}
		n.fini=0;
		n.ressource=null;
		//System.out.println("???> "+ressChoisie.index);
		return ressChoisie;
	}
}
