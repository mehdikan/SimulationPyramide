package Entite;

import java.util.ArrayList;

import Divers.*;

public class Job {
	public Requete requete;
	public int dureeTacheMap;
	public int dureeTacheReduce;
	public int nombreTachesMap;
	public int nombreTachesReduce;
	public int processeurTacheMap;
	public int memoireTacheMap;
	public int stockageTacheMap;
	public int processeurTacheReduce;
	public int memoireTacheReduce;
	public int stockageTacheReduce;
	public int indexJob;
	public int indexDebutTasksMap;
	public int indexDebutTasksReduce;
	public ArrayList<GroupeTaches> groupesMapTaches;
	public ArrayList<GroupeTaches> groupesReduceTaches;
	public long ordreArrive;
	
	public Job(Requete req,int dureeTacheMap, int dureeTacheReduce,int nombreTachesMap,int nombreTachesReduce,int processeurTacheMap,int memoireTacheMap,int stockageTacheMap,int processeurTacheReduce,int memoireTacheReduce,int stockageTacheReduce){
		this.requete=req;
		this.dureeTacheMap=dureeTacheMap;
		this.dureeTacheReduce=dureeTacheReduce;
		this.nombreTachesMap=nombreTachesMap;
		this.nombreTachesReduce=nombreTachesReduce;
		this.processeurTacheMap=processeurTacheMap;
		this.memoireTacheMap=memoireTacheMap;
		this.stockageTacheMap=stockageTacheMap;
		this.processeurTacheReduce=processeurTacheReduce;
		this.memoireTacheReduce=memoireTacheReduce;
		this.stockageTacheReduce=stockageTacheReduce;
		
		this.ordreArrive=VariablesGlobales.jobOrdreArrive;
		VariablesGlobales.jobOrdreArrive+=1;
		
		this.indexJob=VariablesGlobales.jobIndex;
		VariablesGlobales.jobIndex++;
		this.indexDebutTasksMap=VariablesGlobales.mapTasksIndex;
		this.indexDebutTasksReduce=VariablesGlobales.reduceTasksIndex;
		VariablesGlobales.mapTasksIndex+=nombreTachesMap;
		VariablesGlobales.reduceTasksIndex+=nombreTachesReduce;
		
		this.groupesMapTaches=new ArrayList<GroupeTaches>();
		this.groupesReduceTaches=new ArrayList<GroupeTaches>();
		for(int i=0;i<nombreTachesMap;i++){
			this.groupesMapTaches.add(new GroupeTaches(this, 0,dureeTacheMap));
		}
		for(int j=0;j<nombreTachesReduce;j++){
			this.groupesReduceTaches.add(new GroupeTaches(this, 1,dureeTacheReduce));
		}
		
		for(int i=0;i<nombreTachesMap;i++){
			for(int j=0;j<nombreTachesReduce;j++){
				this.groupesReduceTaches.get(j).dependances.add(this.groupesMapTaches.get(i));
			}
		}
	}
}
