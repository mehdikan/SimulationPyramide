package Entite;

import Divers.VariablesGlobales;

public class Cout {
	public double coutProcesseur;
	public double coutMemoire;
	public double coutStockage;
	public double coutComm;
	public double coutPenalite;
	public double coutPenaliteStockage; // Tez
	public double tempsExecTotal;
	public double tempsExecMoyenRequete;
	
	public Cout(){
		coutProcesseur=0;
		coutMemoire=0;
		coutStockage=0;
		coutComm=0;
		coutPenalite=0;
		tempsExecTotal=0;
		tempsExecMoyenRequete=0;
	}
	
	public double coutRess(){
		return VariablesGlobales.Pproc*coutProcesseur+VariablesGlobales.Pmem*coutMemoire+VariablesGlobales.Pstor*coutStockage;
	}
	
	public double coutTotal(){
		//return VariablesGlobales.Pcomm*coutComm+VariablesGlobales.Pproc*coutProcesseur+VariablesGlobales.Pmem*coutMemoire+VariablesGlobales.Pstor*coutStockage+VariablesGlobales.Ppenalites*coutPenalite;
		return coutComm+VariablesGlobales.Pproc*coutProcesseur+VariablesGlobales.Pmem*coutMemoire+VariablesGlobales.Pstor*coutStockage+VariablesGlobales.Ppenalites*coutPenalite;
	}
	
	public double sommeCouts(){
		return coutComm+coutRess()+coutPenalite;
	}
}
