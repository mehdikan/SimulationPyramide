package Entite;

import Divers.VariablesGlobales;

public class GroupeRessources {
	public int type; // 0 Map - 1 Reduce 
	private int disponibilite; // 0 non - 1 oui
	private int disponibiliteBack; // 0 non - 1 oui
	public VM vm;
	public int index;
	public long ordreLiberation;
	public int active;
	public double restePourDesactiver;
	
	public GroupeRessources(VM vm,int type){
		this.active=1;
		this.restePourDesactiver=0;
		this.type=type;
		this.disponibilite=1;
		this.vm=vm;
		if(type==0){
			index=VariablesGlobales.indexressourcesmap;
			VariablesGlobales.indexressourcesmap++;
		}
		else if(type==1){
			index=VariablesGlobales.indexressourcesreduce;
			VariablesGlobales.indexressourcesreduce++;
		}
		else {
			index=this.vm.mp.cloud.indexressourcestez;
			this.vm.mp.cloud.indexressourcestez++;
		}
		this.setLibre();
	}
	
	public void dispoStock(){
		this.disponibiliteBack=this.disponibilite;
	}
	
	public void dispoback(){
		this.disponibilite=this.disponibiliteBack;
	}
	
	public void setLibre(){
		disponibilite=1;
		ordreLiberation=this.vm.mp.cloud.ordreLiberation;
		this.vm.mp.cloud.ordreLiberation+=1;
	}
	
	public void setLibre2(){
		disponibilite=1;
	}
	
	public void setAlloue(){
		disponibilite=0;
	}
	
	public int getDisponibilite(){
		return this.disponibilite;
	}
}