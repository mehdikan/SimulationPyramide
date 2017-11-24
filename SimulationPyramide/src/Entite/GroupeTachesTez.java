package Entite;

import java.util.ArrayList;

import Divers.VariablesGlobales;

public class GroupeTachesTez {
	public int fini; // 0 non - 1 oui
	public GroupeRessources ressource=null;
	public StageTez stage;
	public int tempsDeclanchement;
	public ArrayList<GroupeTachesTez> dependances;
	public int index;
	public int dateFin;
	public int duree;
	public int ordre=0;
	
	public int finiBack; // 0 non - 1 oui
	public int tempsDeclanchementBack;
	public int dateFinBack;
	
	public void attributsStock(){
		this.finiBack=this.fini;
		this.tempsDeclanchementBack=this.tempsDeclanchement;
		this.dateFinBack=this.dateFin;
	}
	
	public void attributsBack(){
		this.fini=this.finiBack;
		this.tempsDeclanchement=this.tempsDeclanchementBack;
		this.dateFin=this.dateFinBack;
	}
	
	public GroupeTachesTez(StageTez stage,int duree){
		this.stage=stage;
		this.duree=duree;
		this.fini=0;
		this.dateFin=-1;
		this.dependances=new ArrayList<GroupeTachesTez>();
		index=this.stage.requeteTez.cloud.indextachestez;
		this.stage.requeteTez.cloud.indextachestez++;
	}
	
	public double quantiteTotalOutput(Cloud cloud){
		double q=0;
		for(ClasseClients c : cloud.listeClassesClient){
			for(RequeteTez r : c.requeteTezEnAttente){
				for(StageTez stage : r.listeStages){
					for(GroupeTachesTez tacheG:stage.groupesTezTaches){
						q+=this.stage.requeteTez.getQuantiteTransfertStages(this.stage, tacheG.stage);
					}
				}
			}
		}
		return q;
	}
	
	public boolean pret(Cloud cloud,int instantCourant){
		for(GroupeTachesTez tacheG : dependances){
			if(tacheG.fini==0) {return false;}
		}
		//System.out.println("ress non disp "+this.type);
		boolean res=cloud.ressourcesDispoTez(this,instantCourant);
		return res;
	}
}
