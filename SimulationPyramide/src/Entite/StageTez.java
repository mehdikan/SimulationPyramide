package Entite;

import java.util.ArrayList;

import Divers.*;

public class StageTez {
	public RequeteTez requeteTez;
	public int dureeTacheTez;
	public int nombreTachesTez;
	public int processeurTacheTez;
	public int memoireTacheTez;
	public int indexStage;
	public int indexDebutTasksTez;
	public ArrayList<GroupeTachesTez> groupesTezTaches;
	public long ordreArrive;
	public double quantiteStockeApresStage;
	
	public StageTez(RequeteTez reqTez,int dureeTacheTez, int nombreTachesTez,int processeurTacheTez,int memoireTacheTez,double quantiteStockeApresStage){
		this.requeteTez=reqTez;
		this.dureeTacheTez=dureeTacheTez;
		this.nombreTachesTez=nombreTachesTez;
		this.processeurTacheTez=processeurTacheTez;
		this.memoireTacheTez=memoireTacheTez;
		this.quantiteStockeApresStage=quantiteStockeApresStage;
		
		this.ordreArrive=this.requeteTez.cloud.stageOrdreArrive;
		this.requeteTez.cloud.stageOrdreArrive+=1;
		
		this.indexStage=this.requeteTez.cloud.stageIndex;
		this.requeteTez.cloud.stageIndex++;
		this.indexDebutTasksTez=this.requeteTez.cloud.tezTasksIndex;
		this.requeteTez.cloud.tezTasksIndex+=nombreTachesTez;
		
		this.groupesTezTaches=new ArrayList<GroupeTachesTez>();
		for(int i=0;i<nombreTachesTez;i++){
			this.groupesTezTaches.add(new GroupeTachesTez(this,dureeTacheTez));
		}
		
		/*for(int i=0;i<nombreTachesTez;i++){
			for(int j=0;j<nombreTachesTez;j++){
				this.groupesReduceTaches.get(j).dependances.add(this.groupesMapTaches.get(i));   revoir
			}
		}*/
	}
}
