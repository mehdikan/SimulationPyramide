package Entite;

import java.io.FileWriter;
import java.util.*;

import com.sun.javafx.collections.MapAdapterChange;

import Divers.*;
import Gantt.Gantt;
import Gantt.TrancheTempsAlloue;

public class Cloud {
	public ArrayList<ClasseClients> listeClassesClient;
	public ArrayList<MachinePhysique> listeMachinesPhysique;
	Map<IntKey, Integer> candidatsMap;
	Map<IntKey, Integer> candidatsReduce;
	Map<IntKey, Integer> candidatsTez;
	Map<IntKey, Integer> distanceSlots;
	public static int heureJournee;
	public static int minuteJournee;
	
	public int stageIndex=0;			//
	public int tezTasksIndex=0;			//
	public int tezSlotsIndex=0;			//
	public int indexrequetes=0;			//
	public int indexressourcestez=0;	//
	public int indextachestez=0;		//
	public int machinePhysiqueIndex=0;  //
	public int vmIndex=0;				//
	public long stageOrdreArrive=0;		//
	public long ordreLiberation=0;		//
	public FileWriter writer_pl2p;		//
	
	public Cloud(int Pcomm,int Pproc,int Pmem,int Pstor){
		listeClassesClient=new ArrayList<ClasseClients>();
		listeMachinesPhysique=new ArrayList<MachinePhysique>();
		candidatsMap= new HashMap<IntKey, Integer>();
		candidatsReduce= new HashMap<IntKey, Integer>();
		candidatsTez= new HashMap<IntKey, Integer>();
		distanceSlots=new HashMap<IntKey, Integer>();
		heureJournee=0;
		minuteJournee=0;
	}
	
	public void ajouterMinutes(int minutesAajouter) {
		minuteJournee=(minuteJournee+minutesAajouter)%60;
		heureJournee=heureJournee+(minuteJournee+minutesAajouter)/60;
	}
	
	public void ajouterVM(int indexMachinePhysique,VM vm){
		this.listeMachinesPhysique.get(indexMachinePhysique).ListeVMs.add(vm);
	}
	
	public void tousCandidatsMap(){
		for(int i=0;i<VariablesGlobales.mapSlotsIndex;i++){
			for(int j=0;j<VariablesGlobales.mapTasksIndex;j++){
				candidatsMap.put(new IntKey(i,j), 1);
			}
		}
	}
	
	public void tousCandidatsReduce(){
		for(int i=0;i<VariablesGlobales.reduceSlotsIndex;i++){
			for(int j=0;j<VariablesGlobales.reduceTasksIndex;j++){
				candidatsReduce.put(new IntKey(i,j), 1);
			}
		}
	}
	
	public void tousCandidatsTez(){
		for(int i=0;i<this.tezSlotsIndex;i++){
			for(int j=0;j<this.tezTasksIndex;j++){
				candidatsTez.put(new IntKey(i,j), 1);
			}
		}
	}
	
	public void setDistanceDefaultMR(){
		for(MachinePhysique p1:listeMachinesPhysique){
			for(VM vm1:p1.ListeVMs){
				for(GroupeRessources map: vm1.groupeMapRessources){
					for(MachinePhysique p2:listeMachinesPhysique){
						for(VM vm2:p2.ListeVMs){
							for(GroupeRessources reduce: vm2.groupeReduceRessources){
								if(vm1==vm2){
									distanceSlots.put(new IntKey(map.index,reduce.index), 0);
								}
								else{
									distanceSlots.put(new IntKey(map.index,reduce.index), 1);
								}
							}
						}
					}
				}
			}
				
		}
	}
	
	public void setDistanceDefaultTez(){
		for(MachinePhysique p1:listeMachinesPhysique){
			for(VM vm1:p1.ListeVMs){
				for(GroupeRessources t1: vm1.groupeTezRessources){
					for(MachinePhysique p2:listeMachinesPhysique){
						for(VM vm2:p2.ListeVMs){
							for(GroupeRessources t2: vm2.groupeTezRessources){
								if(vm1==vm2){
									distanceSlots.put(new IntKey(t1.index,t1.index), 0);
								}
								else{
									distanceSlots.put(new IntKey(t1.index,t2.index), 1);
								}
							}
						}
					}
				}
			}
		}
	}
	
	public int getNbJobs(){
		int nb=0;
		for(ClasseClients c : listeClassesClient){
			for(Requete r : c.requeteEnAttente){
				nb+=r.nbJobs();
			}
		}
		return nb;
	}
	
	public int getNbStages(){
		int nb=0;
		for(ClasseClients c : listeClassesClient){
			for(RequeteTez r : c.requeteTezEnAttente){
				nb+=r.nbStages();
			}
		}
		return nb;
	}
	
	public int getDistanceEntreSlots(int mapSlot,int reduceSlot){
		return distanceSlots.get(new IntKey(mapSlot,reduceSlot));
	}
	
	public int savoirSiCandidatsMap(int slot,int tache){
		if(candidatsMap.get(new IntKey(slot,tache))!=null && candidatsMap.get(new IntKey(slot,tache))==1) return 1;
		return 0;
	}
	
	public int savoirSiCandidatsReduce(int slot,int tache){
		if(candidatsReduce.get(new IntKey(slot,tache))!=null && candidatsReduce.get(new IntKey(slot,tache))==1) return 1;
		return 0;
	}
	
	public boolean ressourcesDispoMR(GroupeTaches tachesG,int instantCourant){
		for(MachinePhysique mp : listeMachinesPhysique){
			for(VM vm : mp.ListeVMs){
				if(tachesG.type==0
						&& tachesG.job.processeurTacheMap<=vm.processeurMapSlots
						&& tachesG.job.memoireTacheMap<=vm.memoireMapSlots
						&& tachesG.job.stockageTacheMap<=vm.stockageMapSlots){
					for(GroupeRessources g:vm.groupeMapRessources){
						int indexRessouces=0;
						if(g.getDisponibilite()==1 && vm.verifierDisponibiliteMap(indexRessouces,instantCourant,tachesG.job.dureeTacheMap)){
							boolean trouv=false;
							for(GroupeTaches gg:tachesG.job.groupesMapTaches){
								if(tachesG!=gg && gg.ressource==g){
									trouv=true;
								}
							}
							if(!trouv){
								return true;
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
						if(g.getDisponibilite()==1 && vm.verifierDisponibiliteReduce(indexRessouces,instantCourant,tachesG.job.dureeTacheReduce)) {
							boolean trouv=false;
							for(GroupeTaches gg:tachesG.job.groupesReduceTaches){
								if(tachesG!=gg && gg.ressource==g){
									trouv=true;
								}
							}
							if(!trouv){return true;}
						}
						indexRessouces++;
					}
				}
			}
		}
		return false;
	}
	
	
	public boolean ressourcesDispoTez(GroupeTachesTez tachesG,int instantCourant){
		for(MachinePhysique mp : listeMachinesPhysique){
			for(VM vm : mp.ListeVMs){
				if(tachesG.stage.processeurTacheTez<=vm.processeurTezSlots
					&& tachesG.stage.memoireTacheTez<=vm.memoireTezSlots){
					for(GroupeRessources g:vm.groupeTezRessources){
						int indexRessouces=0;
						if(g.getDisponibilite()==1 && vm.verifierDisponibiliteTez(indexRessouces,instantCourant,tachesG.stage.dureeTacheTez)){
							boolean trouv=false;
							for(GroupeTachesTez gg:tachesG.stage.groupesTezTaches){
								if(tachesG!=gg && gg.ressource==g){
									trouv=true;
								}
							}
							if(!trouv){
								return true;
							}
						}
						indexRessouces++;
					}
				}
			}
		}
		return false;
	}
	
	public void allouerRessourcesMR(Gantt gantt){
		for(TrancheTempsAlloue tta:gantt.tab){
			for(int t=tta.dateDebut;t<=tta.dateFin;t++){
				if(tta.type==1){
					this.listeMachinesPhysique.get(this.getIndexMachinePhysiqueMR(tta.indexRessource, 1)).ListeVMs.get(this.getIndexVMMR(tta.indexRessource, 1)).disponibliteTrancheTempsMap[this.getIndexRessourceDansVMMR(tta.indexRessource, 1)][t-1]=0;
				}
				else{
					this.listeMachinesPhysique.get(this.getIndexMachinePhysiqueMR(tta.indexRessource, 2)).ListeVMs.get(this.getIndexVMMR(tta.indexRessource, 2)).disponibliteTrancheTempsReduce[this.getIndexRessourceDansVMMR(tta.indexRessource, 2)][t-1]=0;	
				}
			}
		}
	}
	
	public void allouerRessourcesTez(Gantt gantt){
		if(VariablesGlobales.verbose) System.out.println("iii>"+gantt);
		for(TrancheTempsAlloue tta:gantt.tab){
			for(int t=tta.dateDebut;t<=tta.dateFin;t++){
				//System.out.println(">>> ");
				//System.out.println(this.getIndexMachinePhysiqueTez(tta.indexRessource));
				this.listeMachinesPhysique.get(this.getIndexMachinePhysiqueTez(tta.indexRessource)).ListeVMs.get(this.getIndexVMTez(tta.indexRessource)).disponibliteTrancheTempsTez[this.getIndexRessourceDansVMTez(tta.indexRessource)][t-1]=0;
				//System.out.println(">>>>>>>>>>>>>>>>>>>>>>>> tta.dateDebut="+tta.dateDebut+" tta.dateFin="+tta.dateFin+" tta.indexRessource="+tta.indexRessource+" this.getIndexRessource="+this.getIndexRessourceDansVMTez(tta.indexRessource)+" t-1="+(t-1));
			}
		}
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	public void decalerTempsMR(int temps){
		for(MachinePhysique mp:this.listeMachinesPhysique){
			for(VM vm:mp.ListeVMs){
				for(int t=0;t<VariablesGlobales.T-temps;t++){
					for(int i=0;i<vm.nbMapSlots;i++){
						vm.disponibliteTrancheTempsMap[i][t]=vm.disponibliteTrancheTempsMap[i][temps+t];
					}
					for(int i=0;i<vm.nbReduceSlots;i++){
						vm.disponibliteTrancheTempsReduce[i][t]=vm.disponibliteTrancheTempsReduce[i][temps+t];
					}
				}
				for(int t=VariablesGlobales.T-temps;t<VariablesGlobales.T;t++){
					for(int i=0;i<vm.nbMapSlots;i++){
						vm.disponibliteTrancheTempsMap[i][t]=1;
					}
					for(int i=0;i<vm.nbReduceSlots;i++){
						vm.disponibliteTrancheTempsReduce[i][t]=1;
					}
				}
			}
		}
	}
		
	public void decalerTempsTez(int temps){
		for(MachinePhysique mp:this.listeMachinesPhysique){
			for(VM vm:mp.ListeVMs){
				for(int t=0;t<VariablesGlobales.T-temps;t++){
					for(int i=0;i<vm.nbTezSlots;i++){
						vm.disponibliteTrancheTempsTez[i][t]=vm.disponibliteTrancheTempsTez[i][temps+t];
					}
				}
				for(int t=VariablesGlobales.T-temps;t<VariablesGlobales.T;t++){
					for(int i=0;i<vm.nbTezSlots;i++){
						vm.disponibliteTrancheTempsTez[i][t]=1;
					}
				}
			}
		}
	}
	

	
	public int getIndexMachinePhysiqueMR(int indexRessource,int typeMouR){
		int index=0;
		for(MachinePhysique mp:this.listeMachinesPhysique){
			for(VM vm:mp.ListeVMs){
				if(typeMouR==1){
					for(int i=0;i<vm.nbMapSlots;i++){
						if(index==indexRessource)
							return mp.indexMachinePhysique;
						index++;
					}
				}
				else{
					for(int i=0;i<vm.nbReduceSlots;i++){
						if(index==indexRessource)
							return mp.indexMachinePhysique;
						index++;
					}
				}
			}
		}
		return -1;
	}
	
	public int getIndexMachinePhysiqueTez(int indexRessource){
		int index=0;
		for(MachinePhysique mp:this.listeMachinesPhysique){
			for(VM vm:mp.ListeVMs){
				for(int i=0;i<vm.nbTezSlots;i++){
					if(index==indexRessource)
						return mp.indexMachinePhysique;
					index++;
				}
			}
		}
		return -1;
	}
	
	public int getIndexVMMR(int indexRessource,int typeMouR){
		int index=0;
		for(MachinePhysique mp:this.listeMachinesPhysique){
			for(VM vm:mp.ListeVMs){
				if(typeMouR==1){
					for(int i=0;i<vm.nbMapSlots;i++){
						if(index==indexRessource)
							return vm.indexVM;
						index++;
					}
				}
				else{
					for(int i=0;i<vm.nbReduceSlots;i++){
						if(index==indexRessource)
							return vm.indexVM;
						index++;
					}
				}
			}
		}
		return -1;
	}
	
	
	public int getIndexVMTez(int indexRessource){
		int index=0;
		for(MachinePhysique mp:this.listeMachinesPhysique){
			for(VM vm:mp.ListeVMs){
				for(int i=0;i<vm.nbTezSlots;i++){
					if(index==indexRessource)
						return vm.indexVM;
					index++;
				}
			}
		}
		return -1;
	}
	
	public int getIndexRessourceDansVMMR(int indexRessource,int typeMouR){
		int index=0;
		for(MachinePhysique mp:this.listeMachinesPhysique){
			for(VM vm:mp.ListeVMs){
				if(typeMouR==1){
					for(int i=0;i<vm.nbMapSlots;i++){
						if(index==indexRessource)
							return index-vm.indexDebutSlotsMap;
						index++;
					}
				}
				else{
					for(int i=0;i<vm.nbReduceSlots;i++){
						if(index==indexRessource)
							return index-vm.indexDebutSlotsReduce;
						index++;
					}
				}
			}
		}
		return -1;
	}
	
	public int getIndexRessourceDansVMTez(int indexRessource){
		int index=0;
		for(MachinePhysique mp:this.listeMachinesPhysique){
			for(VM vm:mp.ListeVMs){
				for(int i=0;i<vm.nbTezSlots;i++){
					if(index==indexRessource)
						return index-vm.indexDebutSlotsMap;
					index++;
				}
			}
		}
		return -1;
	}
	
	public void activeResssource(TypeVM type) {
		for(MachinePhysique p1:listeMachinesPhysique){
			for(VM vm1:p1.ListeVMs){
				for(GroupeRessources t: vm1.groupeTezRessources){
					if(t.vm.type==type && t.active==0) {
						t.active=1;
						t.restePourDesactiver=0;
						for(int tt=0;tt<VariablesGlobales.T;tt++){
							vm1.disponibliteTrancheTempsTez[t.index-t.vm.indexDebutSlotsTez][tt]=1;
						}
						return;
					}
				}
			}
		}
	}
	
	public void desactiveResssource(TypeVM type) {
		
		GroupeRessources r=null;
		int cpt1=0,cpt3=0;
		for(MachinePhysique p1:listeMachinesPhysique){
			for(VM vm1:p1.ListeVMs){
				for(GroupeRessources t: vm1.groupeTezRessources){
					
					cpt3=0;
					if(t.vm.type==type && t.active==1) {
						if(r==null) {
							r=t;
							cpt1=0;
							for(int tt=0;tt<VariablesGlobales.T;tt++){
								cpt1+=t.vm.disponibliteTrancheTempsTez[t.index-t.vm.indexDebutSlotsTez][tt];
								if(t.vm.disponibliteTrancheTempsTez[t.index-t.vm.indexDebutSlotsTez][tt]==0) cpt3=tt;
							}
						}
						else {
							int cpt4=0;
							int cpt2=0;
							for(int tt=0;tt<VariablesGlobales.T;tt++){
								cpt2=t.vm.disponibliteTrancheTempsTez[t.index-t.vm.indexDebutSlotsTez][tt];
								if(t.vm.disponibliteTrancheTempsTez[t.index-t.vm.indexDebutSlotsTez][tt]==0) cpt4=tt;
							}
							if(cpt2<=cpt1) {
								r=t;
								cpt1=cpt2;
								cpt3=cpt4;
							}
						}
					}
				}
			}
		}
		if(r!=null) {
			r.active=0;
			r.restePourDesactiver=cpt3;
		}
	}
	
	public double tauxSurcharge() {
		double nbNonDisponibles=0;
		double total=0;
		for(MachinePhysique mp:this.listeMachinesPhysique) {
			for(VM vm:mp.ListeVMs) {
				for(int i=0;i<vm.nbTezSlots;i++) {
					for(int t=0;t<VariablesGlobales.T;t++) {
						if(vm.disponibliteTrancheTempsTez[i][t]==0) nbNonDisponibles++;
						total++;
					}
				}
			}
		}
		return nbNonDisponibles/total;
	}
	
	public double nbRessourceNonDispo() {
		double nbNonDisponibles=0;
		double total=0;
		for(MachinePhysique mp:this.listeMachinesPhysique) {
			for(VM vm:mp.ListeVMs) {
				for(int i=0;i<vm.nbTezSlots;i++) {
					for(int t=0;t<VariablesGlobales.T;t++) {
						if(vm.disponibliteTrancheTempsTez[i][t]==0) nbNonDisponibles++;
						total++;
					}
				}
			}
		}
		return nbNonDisponibles;
	}
	
	public double nbRessourceTotal() {
		double nbNonDisponibles=0;
		double total=0;
		for(MachinePhysique mp:this.listeMachinesPhysique) {
			for(VM vm:mp.ListeVMs) {
				for(int i=0;i<vm.nbTezSlots;i++) {
					for(int t=0;t<VariablesGlobales.T;t++) {
						if(vm.disponibliteTrancheTempsTez[i][t]==0) nbNonDisponibles++;
						total++;
					}
				}
			}
		}
		return total;
	}
	
	public void avancerDansTemps() {
		for(MachinePhysique mp:this.listeMachinesPhysique) {
			for(VM vm:mp.ListeVMs) {
				for(int i=0;i<vm.nbTezSlots;i++) {
					for(int t=0;t<VariablesGlobales.T-1;t++) {
						vm.disponibliteTrancheTempsTez[i][t]=vm.disponibliteTrancheTempsTez[i][t+1];
					}
					vm.disponibliteTrancheTempsTez[i][VariablesGlobales.T-1]=1;
				}
			}
		}
	}
	
	public void viderRequetesEnAttentes() {
		for(ClasseClients c : listeClassesClient){
			c.requeteTezEnAttente.clear();
		}
	}
}
