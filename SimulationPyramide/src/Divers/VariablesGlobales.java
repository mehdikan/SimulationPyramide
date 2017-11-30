package Divers;

import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

public class VariablesGlobales {
	public static int jobIndex=0;   
	// public static int stageIndex=0;					//
	public static int mapTasksIndex=0;
	public static int reduceTasksIndex=0;
	// public static int tezTasksIndex=0;   			//
	public static int mapSlotsIndex=0;
	public static int reduceSlotsIndex=0;
	// public static int tezSlotsIndex=0;				//
	// public static int indexrequetes=0;				//
	public static int indexressourcesmap=0;			
	public static int indexressourcesreduce=0;
	// public static int indexressourcestez=0;			//
	// public static int indextachestez=0;				//
	public static int indextachesmap=0;
	public static int indextachereduce=0;
	// public static int machinePhysiqueIndex=0;		//
	// public static int vmIndex=0;					//
	public static long jobOrdreArrive=0;
	// public static long stageOrdreArrive=0;			//
	// public static long ordreLiberation=0;			//
	public static boolean verbose=false;
	public static FileWriter writer_gbrt;
	public static FileWriter writer_gmpt;
	public static FileWriter writer_gmpm;
	// public static FileWriter writer_pl2p;			//
	
	public static final ReentrantLock lock = new ReentrantLock();
	
	public static int T=50;
	public static double Pcomm=0.05/4;   // 0.05 dollar pour 1go
	public static double Pproc=1.5/3/32/6;  // 1.5 dollar l'heure
	public static double Pmem=1.5/3/32/6;
	public static double Pstor=1.5/3/32/6;
	public static double Ppenalites=1;
	public static double PXnonPlacees=10000;
	public static double PYnonPlacees=10000;
	public static double coefRepartition=0;
	public static double niveauDisponiblite=1;
	public static double coutActivationRessource=1;
	public static double coutDesactivationRessource=1;
	
	//public static double coutActivationRessource=0.0000000001;
	//public static double coutDesactivationRessource=0.0000000001;
	
	public static double coutRessTempo=0;
	public static double coutActivationTempo=0;
	public static double coutDesactivationTempo=0;

	public static void init(){
		jobIndex=0;
		// stageIndex=0; //
		mapTasksIndex=0;
		// tezTasksIndex=0;  //
		reduceTasksIndex=0;
		// indextachestez=0; //
		mapSlotsIndex=0;
		reduceSlotsIndex=0;
		// tezSlotsIndex=0; //
		//indexrequetes=0; //
		indexressourcesmap=0;
		indexressourcesreduce=0;
		// indexressourcestez=0; //
		indextachesmap=0;
		indextachereduce=0;
		// machinePhysiqueIndex=0; //
		// vmIndex=0; //
		jobOrdreArrive=0;
		// stageOrdreArrive=0; //
		//ordreLiberation=0; //
	}
}