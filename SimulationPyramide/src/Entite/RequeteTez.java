package Entite;

import java.util.*;
import Divers.*;

public class RequeteTez {
	public int index;
	public ArrayList<StageTez> listeStages;
	public StageTez stageFinal;
	public Cloud cloud;
	public double poids;
	public int dateLimite;
	public int dateFinReelle;
	public int tempsGMPT=0;
	Map<StagesKey, Integer> quantiteTransfertStages;
	Map<StagesKey, Integer> typeLien;
	//public double quantiteStockeApresStage;
	
	public RequeteTez(double poids,int dateLimite,Cloud cloud){
		this.cloud=cloud;
		this.poids=poids;
		this.dateLimite=dateLimite;
		this.dateFinReelle=-1;
		listeStages=new ArrayList<StageTez>();
		quantiteTransfertStages=new HashMap<StagesKey, Integer>();
		typeLien=new HashMap<StagesKey, Integer>();
		this.index= this.cloud.indexrequetes;
		this.cloud.indexrequetes+=1;
	}
	
	public void rajouterStage(StageTez stage){
		listeStages.add(stage);
		stage.requeteTez=this;
	}
	
	public void majQuantiteTransfertStages(StageTez s1, StageTez s2,int quantite)
	{
		quantiteTransfertStages.put(new StagesKey(s1,s2),quantite);
		if(quantite>0 && s1!=s2){
			for(int i=0;i<s2.groupesTezTaches.size();i++){
				for(int j=0;j<s1.groupesTezTaches.size();j++){
					s2.groupesTezTaches.get(i).dependances.add(s1.groupesTezTaches.get(j));
				}
			}
		}
	}
	
	public void majTypeLien(StageTez s1, StageTez s2,int type)
	{
		typeLien.put(new StagesKey(s1,s2),type);
	}
	
	public int nbStages(){
		return listeStages.size();
	}
	
	public StageTez getStage(int i){
		return this.listeStages.get(i);
	}
	
	public int getQuantiteTransfertStages(StageTez stage1,StageTez stage2){
		if(quantiteTransfertStages.get(new StagesKey(stage1,stage2))!=null){
			return quantiteTransfertStages.get(new StagesKey(stage1,stage2));
		}
		return 0;
	}
	
	public int getDepandance(StageTez stage1,StageTez stage2){
		if(quantiteTransfertStages.get(new StagesKey(stage1,stage2))!=null && quantiteTransfertStages.get(new StagesKey(stage1,stage2))>0){
			return 1;
		}
		return 0;
	}
	
	public int getLien(StageTez stage1,StageTez stage2){
		if(typeLien.get(new StagesKey(stage1,stage2))!=null){
			return typeLien.get(new StagesKey(stage1,stage2));
		}
		return 0;
	}
}
