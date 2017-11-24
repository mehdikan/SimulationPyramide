package Entite;

import java.util.*;
import Divers.*;

public class Requete {
	public int index;
	public ArrayList<Job> listeJobs;
	public Job jobFinal;
	public double poids;
	public int dateLimite;
	public int dateFinReelle;
	public int tempsGMPT=0;
	Map<JobsKey, Integer> quantiteTransfertJobs;
	
	public Requete(double poids,int dateLimite){
		this.poids=poids;
		this.dateLimite=dateLimite;
		this.dateFinReelle=-1;
		listeJobs=new ArrayList<Job>();
		quantiteTransfertJobs=new HashMap<JobsKey, Integer>();
		this.index=VariablesGlobales.indexrequetes;
		VariablesGlobales.indexrequetes+=1;
	}
	
	public void rajouterJob(Job job){
		listeJobs.add(job);
		job.requete=this;
	}
	
	public void majQuantiteTransfertJobs(Job j1, Job j2,int quantite)
	{
		quantiteTransfertJobs.put(new JobsKey(j1,j2),quantite);
		if(quantite>0 && j1!=j2){
			for(int i=0;i<j2.groupesMapTaches.size();i++){
				for(int j=0;j<j1.groupesReduceTaches.size();j++){
					j2.groupesMapTaches.get(i).dependances.add(j1.groupesReduceTaches.get(j));
				}
			}
		}
	}
	
	public int nbJobs(){
		return listeJobs.size();
	}
	
	public Job getJob(int i){
		return this.listeJobs.get(i);
	}
	
	public int getQuantiteTransfertJobs(Job job1,Job job2){
		if(quantiteTransfertJobs.get(new JobsKey(job1,job2))!=null){
			return quantiteTransfertJobs.get(new JobsKey(job1,job2));
		}
		return 0;
	}
	
	public int getDepandance(Job job1,Job job2){
		if(quantiteTransfertJobs.get(new JobsKey(job1,job2))!=null && quantiteTransfertJobs.get(new JobsKey(job1,job2))>0){
			return 1;
		}
		return 0;
	}
}
