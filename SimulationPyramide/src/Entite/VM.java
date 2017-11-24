package Entite;

import java.util.ArrayList;

import Divers.VariablesGlobales;

public class VM {
	public TypeVM type;
	public int indexVM;
	public int processeurMapSlots;
	public int memoireMapSlots;
	public int stockageMapSlots;
	public int processeurReduceSlots;
	public int memoireReduceSlots;
	public int stockageReduceSlots;
	public int processeurTezSlots;
	public int memoireTezSlots;
	public int stockageTezSlots;
	public int nbMapSlots;
	public int nbReduceSlots;
	public int nbTezSlots;
	public int indexDebutSlotsMap;
	public int indexDebutSlotsReduce;
	public int indexDebutSlotsTez;
	public int[][] disponibliteTrancheTempsMap;
	public int[][] disponibliteTrancheTempsReduce;
	public int[][] disponibliteTrancheTempsTez;
	public int[][] disponibliteTrancheTempsMapBack;
	public int[][] disponibliteTrancheTempsReduceBack;
	public int[][] disponibliteTrancheTempsTezBack;
	public ArrayList<GroupeRessources> groupeMapRessources;
	public ArrayList<GroupeRessources> groupeReduceRessources;
	public ArrayList<GroupeRessources> groupeTezRessources;

	public MachinePhysique mp;
	
	public VM(MachinePhysique mp,TypeVM type,int nbMapSlots,int nbReduceSlots,int processeurMapSlots,int memoireMapSlots,int stockageMapSlots,int processeurReduceSlots,int memoireReduceSlots,int stockageReduceSlots){
		this.mp=mp;
		this.type=type;
		this.processeurMapSlots=processeurMapSlots;
		this.memoireMapSlots=memoireMapSlots;
		this.stockageMapSlots=stockageMapSlots;
		this.processeurReduceSlots=processeurReduceSlots;
		this.memoireReduceSlots=memoireReduceSlots;
		this.stockageReduceSlots=stockageReduceSlots;
		this.nbMapSlots=nbMapSlots;
		this.nbReduceSlots=nbReduceSlots;
		this.indexDebutSlotsMap=VariablesGlobales.mapSlotsIndex;
		this.indexDebutSlotsReduce=VariablesGlobales.reduceSlotsIndex;
		this.indexDebutSlotsTez=this.mp.cloud.tezSlotsIndex;
		this.indexVM=this.mp.cloud.vmIndex;
		this.mp.cloud.vmIndex++;
		VariablesGlobales.mapSlotsIndex+=nbMapSlots;
		VariablesGlobales.reduceSlotsIndex+=nbReduceSlots;
		this.mp.cloud.tezSlotsIndex+=nbTezSlots;
		disponibliteTrancheTempsMap=new int[nbMapSlots][VariablesGlobales.T];
		disponibliteTrancheTempsReduce=new int[nbReduceSlots][VariablesGlobales.T];
		disponibliteTrancheTempsTez=new int[nbTezSlots][VariablesGlobales.T];
		disponibliteTrancheTempsMapBack=new int[nbMapSlots][VariablesGlobales.T];
		disponibliteTrancheTempsReduceBack=new int[nbReduceSlots][VariablesGlobales.T];
		disponibliteTrancheTempsTezBack=new int[nbTezSlots][VariablesGlobales.T];
		
		this.initDisponibiliteTrancheTemps();
		
		
		groupeMapRessources=new ArrayList<GroupeRessources>();
		groupeReduceRessources=new ArrayList<GroupeRessources>();
		groupeTezRessources=new ArrayList<GroupeRessources>();
		
		for(int i=0;i<nbMapSlots;i++){
			groupeMapRessources.add(new GroupeRessources(this,0));
		}
		
		for(int i=0;i<nbReduceSlots;i++){
			groupeReduceRessources.add(new GroupeRessources(this,1));
		}
	}
	
	public VM(MachinePhysique mp,TypeVM type,int nbMapSlots,int nbReduceSlots,int nbTezSlots,int processeurMapSlots,int memoireMapSlots,int stockageMapSlots,int processeurReduceSlots,int memoireReduceSlots,int stockageReduceSlots,int processeurTezSlots,int memoireTezSlots,int stockageTezSlots){
		this.mp=mp;
		this.type=type;
		this.processeurMapSlots=processeurMapSlots;
		this.memoireMapSlots=memoireMapSlots;
		this.stockageMapSlots=stockageMapSlots;
		this.processeurReduceSlots=processeurReduceSlots;
		this.memoireReduceSlots=memoireReduceSlots;
		this.stockageReduceSlots=stockageReduceSlots;
		this.processeurTezSlots=processeurTezSlots;
		this.memoireTezSlots=memoireTezSlots;
		this.stockageTezSlots=stockageTezSlots;
		this.nbMapSlots=nbMapSlots;
		this.nbReduceSlots=nbReduceSlots;
		this.nbTezSlots=nbTezSlots;
		this.indexDebutSlotsMap=VariablesGlobales.mapSlotsIndex;
		this.indexDebutSlotsReduce=VariablesGlobales.reduceSlotsIndex;
		this.indexDebutSlotsTez=this.mp.cloud.tezSlotsIndex;
		this.indexVM=this.mp.cloud.vmIndex;
		this.mp.cloud.vmIndex++;
		VariablesGlobales.mapSlotsIndex+=nbMapSlots;
		VariablesGlobales.reduceSlotsIndex+=nbReduceSlots;
		this.mp.cloud.tezSlotsIndex+=nbTezSlots;
		disponibliteTrancheTempsMap=new int[nbMapSlots][VariablesGlobales.T];
		disponibliteTrancheTempsReduce=new int[nbReduceSlots][VariablesGlobales.T];
		disponibliteTrancheTempsTez=new int[nbTezSlots][VariablesGlobales.T];
		disponibliteTrancheTempsMapBack=new int[nbMapSlots][VariablesGlobales.T];
		disponibliteTrancheTempsReduceBack=new int[nbReduceSlots][VariablesGlobales.T];
		disponibliteTrancheTempsTezBack=new int[nbTezSlots][VariablesGlobales.T];
		
		this.initDisponibiliteTrancheTemps();
		
		
		groupeMapRessources=new ArrayList<GroupeRessources>();
		groupeReduceRessources=new ArrayList<GroupeRessources>();
		groupeTezRessources=new ArrayList<GroupeRessources>();
		
		for(int i=0;i<nbMapSlots;i++){
			groupeMapRessources.add(new GroupeRessources(this,0));
		}
		
		for(int i=0;i<nbReduceSlots;i++){
			groupeReduceRessources.add(new GroupeRessources(this,1));
		}
		
		for(int i=0;i<nbTezSlots;i++){
			groupeTezRessources.add(new GroupeRessources(this,2));
		}
	}
	
	public void initDisponibiliteTrancheTemps(){
		for(int t=0;t<VariablesGlobales.T;t++){
			for(int i=0;i<nbMapSlots;i++){
				if(Math.random()<VariablesGlobales.niveauDisponiblite){
					disponibliteTrancheTempsMap[i][t]=1;
				}
				else disponibliteTrancheTempsMap[i][t]=0;
			}
			for(int i=0;i<nbReduceSlots;i++){
				if(Math.random()<VariablesGlobales.niveauDisponiblite){
					disponibliteTrancheTempsReduce[i][t]=1;
				}
				else disponibliteTrancheTempsReduce[i][t]=0;
			}
			for(int i=0;i<nbTezSlots;i++){
				if(Math.random()<VariablesGlobales.niveauDisponiblite){
					disponibliteTrancheTempsTez[i][t]=1;
				}
				else disponibliteTrancheTempsTez[i][t]=0;
			}
		}
	}
	
	public void disponibiliteTrancheTempsStock(){
		for(int t=0;t<VariablesGlobales.T;t++){
			for(int i=0;i<nbMapSlots;i++){
				disponibliteTrancheTempsMapBack[i][t]=disponibliteTrancheTempsMap[i][t];
			}
			for(int i=0;i<nbReduceSlots;i++){
				disponibliteTrancheTempsReduceBack[i][t]=disponibliteTrancheTempsReduce[i][t];
			}
			for(int i=0;i<nbTezSlots;i++){
				disponibliteTrancheTempsTezBack[i][t]=disponibliteTrancheTempsTez[i][t];
			}
		}
	}
	
	public void disponibiliteTrancheTempsBack(){
		for(int t=0;t<VariablesGlobales.T;t++){
			for(int i=0;i<nbMapSlots;i++){
				disponibliteTrancheTempsMap[i][t]=disponibliteTrancheTempsMapBack[i][t];
			}
			for(int i=0;i<nbReduceSlots;i++){
				disponibliteTrancheTempsReduce[i][t]=disponibliteTrancheTempsReduceBack[i][t];
			}
			for(int i=0;i<nbTezSlots;i++){
				disponibliteTrancheTempsTez[i][t]=disponibliteTrancheTempsTezBack[i][t];
			}
		}
	}
	
	public void setAlloueMap(int indexRessouces,int instantdebut,int dureeTacheMap){
		for(int t=instantdebut-1;t<instantdebut+dureeTacheMap-1;t++){
			this.disponibliteTrancheTempsMap[indexRessouces][t]=0;
		}
	}
	
	public void setAlloueReduce(int indexRessouces,int instantdebut,int dureeTacheReduce){
		for(int t=instantdebut-1;t<instantdebut+dureeTacheReduce-1;t++){
			this.disponibliteTrancheTempsReduce[indexRessouces][t]=0;
		}
	}
	
	public void setAlloueTez(int indexRessouces,int instantdebut,int dureeTacheTez){
		for(int t=instantdebut-1;t<instantdebut+dureeTacheTez-1;t++){
			this.disponibliteTrancheTempsTez[indexRessouces][t]=0;
		}
	}
	
	public boolean verifierDisponibiliteMap(int indexRessouces,int instantdebut,int dureeTacheMap){
		for(int t=instantdebut-1;t<instantdebut+dureeTacheMap-1;t++){
			if(this.disponibliteTrancheTempsMap[indexRessouces][t]==0) return false;
		}
		return true;
	}
	
	public boolean verifierDisponibiliteReduce(int indexRessouces,int instantdebut,int dureeTacheMap){
		for(int t=instantdebut-1;t<instantdebut+dureeTacheMap-1;t++){
			if(this.disponibliteTrancheTempsReduce[indexRessouces][t]==0) return false;
		}
		return true;
	}
	
	public boolean verifierDisponibiliteTez(int indexRessouces,int instantdebut,int dureeTacheTez){
		for(int t=instantdebut-1;t<instantdebut+dureeTacheTez-1;t++){
			if(this.disponibliteTrancheTempsTez[indexRessouces][t]==0) return false;
		}
		return true;
	}
	
	public int ressourceActive(int indexRessource) {
		return this.groupeTezRessources.get(indexRessource).active;
	}
}
