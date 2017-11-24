package Entite;

import java.util.ArrayList;

import Divers.VariablesGlobales;

public class GroupeTaches {
	public int type; // 0 Map - 1 Reduce 
	public int fini; // 0 non - 1 oui
	public GroupeRessources ressource=null;
	public Job job;
	public int tempsDeclanchement;
	public ArrayList<GroupeTaches> dependances;
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
	
	public GroupeTaches(Job job,int type,int duree){
		this.job=job;
		this.type=type;
		this.duree=duree;
		this.fini=0;
		this.dateFin=-1;
		this.dependances=new ArrayList<GroupeTaches>();
		if(type==0){
			index=VariablesGlobales.indextachesmap;
			VariablesGlobales.indextachesmap++;
		}
		else{
			index=VariablesGlobales.indextachereduce;
			VariablesGlobales.indextachereduce++;
		}
	}
	
	public double quantiteTotalOutput(Cloud cloud){
		double q=0;
		for(ClasseClients c : cloud.listeClassesClient){
			for(Requete r : c.requeteEnAttente){
				for(Job job : r.listeJobs){
					for(GroupeTaches map:job.groupesMapTaches){
						q+=this.job.requete.getQuantiteTransfertJobs(this.job, map.job);
					}
					for(GroupeTaches reduce:job.groupesReduceTaches){
						q+=this.job.requete.getQuantiteTransfertJobs(this.job, reduce.job);
					}
				}
			}
		}
		return q;
	}
	
	public boolean pret(Cloud cloud,int instantCourant){
		for(GroupeTaches tacheG : dependances){
			if(tacheG.fini==0) {return false;}
		}
		//System.out.println("ress non disp "+this.type);
		boolean res=cloud.ressourcesDispoMR(this,instantCourant);
		return res;
	}
}
