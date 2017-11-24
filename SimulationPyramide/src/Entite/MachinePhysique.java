package Entite;

import java.util.*;

import Divers.VariablesGlobales;

public class MachinePhysique {
	public ArrayList<VM> ListeVMs;
	public int indexMachinePhysique;
	public Cloud cloud;
	
	public MachinePhysique(Cloud cloud){
		this.cloud=cloud;
		this.ListeVMs=new ArrayList<VM>();
		indexMachinePhysique=this.cloud.machinePhysiqueIndex;
		this.cloud.machinePhysiqueIndex++;
	}
}
