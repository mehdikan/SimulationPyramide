package SimulationTez;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

import Algorithmes.*;
import Divers.CSVUtils;
import Divers.VariablesGlobales;
import Entite.ClasseClients;
import Entite.Cloud;
import Entite.Cout;
import Entite.Job;
import Entite.MachinePhysique;
import Entite.Requete;
import Entite.RequeteTez;
import Entite.StageTez;
import Entite.TypeVM;
import Entite.VM;
import Gantt.Gantt;
import PLNE.*;
import PLNETEZ.ModeleOrdonnancementGLPKTez;
import PLNETEZ.ModelePlacementGLPKTEZ;

public class ComparaisonTez1 {
	public static void main(String[] args) throws IOException{
		//int v=Integer.parseInt(args[0]);
		//VariablesGlobales.T=Integer.parseInt(args[1]);
		VariablesGlobales.T=40;
		//int ligneNum=Integer.parseInt(args[2]);
		int ligneNum=5;
		try (BufferedReader br = new BufferedReader(new FileReader("gen.txt"))) {
		    String line;
		    int cpt=1;
		    while ((line = br.readLine()) != null) {
		    	if(cpt==ligneNum){
		        	//VariablesGlobales.niveauDisponiblite=Double.parseDouble(line);
		        	VariablesGlobales.niveauDisponiblite=1;
		        }
		        cpt++;
		    }
		}
		//System.out.println(">>>="+VariablesGlobales.niveauDisponiblite);
		
		Cloud cloud=new Cloud(1,1,1,1);
		
		TypeVM type1=new TypeVM(36,60000,1000000);
		TypeVM type2=new TypeVM(36,60000,1000000);
		cloud.listeMachinesPhysique.add(new MachinePhysique(cloud));

		cloud.ajouterVM(0,new VM(type2,1,1,1,8*2,8*2,8*2,8*2,8*2,8*2,8*2,8*2,8*2));
		cloud.ajouterVM(0,new VM(type2,1,1,1,8*2,8*2,8*2,8*2,8*2,8*2,8*2,8*2,8*2));
		cloud.ajouterVM(0,new VM(type2,1,1,1,8*2,8*2,8*2,8*2,8*2,8*2,8*2,8*2,8*2));
		cloud.ajouterVM(0,new VM(type2,1,1,1,8*2,8*2,8*2,8*2,8*2,8*2,8*2,8*2,8*2));
		cloud.ajouterVM(0,new VM(type2,1,1,1,8*2,8*2,8*2,8*2,8*2,8*2,8*2,8*2,8*2));
		cloud.ajouterVM(0,new VM(type1,1,1,1,8*1,8*1,8*1,8*1,8*1,8*1,8*1,8*1,8*1));
		cloud.ajouterVM(0,new VM(type1,1,1,1,8*1,8*1,8*1,8*1,8*1,8*1,8*1,8*1,8*1));
		cloud.ajouterVM(0,new VM(type1,1,1,1,8*1,8*1,8*1,8*1,8*1,8*1,8*1,8*1,8*1));
		cloud.ajouterVM(0,new VM(type1,1,1,1,8*1,8*1,8*1,8*1,8*1,8*1,8*1,8*1,8*1));
		cloud.ajouterVM(0,new VM(type1,1,1,1,8*1,8*1,8*1,8*1,8*1,8*1,8*1,8*1,8*1));

		cloud.tousCandidatsMap();
		cloud.tousCandidatsReduce();
		cloud.setDistanceDefaultMR();
		
		cloud.tousCandidatsTez();
		cloud.setDistanceDefaultTez();

		cloud.listeClassesClient.add(new ClasseClients(1));
	    
		RequeteTez req=new RequeteTez(1,4,cloud);
		req.rajouterStage(new StageTez(req,3,4,8*2,8*2,8*2));
		req.rajouterStage(new StageTez(req,2,3,8*1,8*1,8*1));
	    req.stageFinal=req.listeStages.get(req.listeStages.size()-1);
	    for(int j=0;j<req.nbStages();j++){
	    	for(int k=0;k<req.nbStages();k++){
	    		req.majQuantiteTransfertStages(req.getStage(j), req.getStage(k), 0);
	    		req.majTypeLien(req.getStage(j), req.getStage(k), 0);
	    	}
	    }
	    req.majQuantiteTransfertStages(req.getStage(0), req.getStage(1), 2);
	    req.majTypeLien(req.getStage(0), req.getStage(1), 2);
		cloud.listeClassesClient.get(0).requeteTezEnAttente.add(req);
		
		req=new RequeteTez(2,4,cloud);
	    req.rajouterStage(new StageTez(req,3,5,8*2,8*2,8*2));
	    req.rajouterStage(new StageTez(req,3,4,8*2,8*2,8*2));
	    req.rajouterStage(new StageTez(req,2,3,8*1,8*1,8*1));
	    req.stageFinal=req.listeStages.get(req.listeStages.size()-1);
	    for(int j=0;j<req.nbStages();j++){
	    	for(int k=0;k<req.nbStages();k++){
	    		req.majQuantiteTransfertStages(req.getStage(j), req.getStage(k), 0);
	    		req.majTypeLien(req.getStage(j), req.getStage(k), 0);
	    	}
	    }
	    req.majQuantiteTransfertStages(req.getStage(0), req.getStage(1), 2);
	    req.majQuantiteTransfertStages(req.getStage(1), req.getStage(2), 2);
	    req.majTypeLien(req.getStage(0), req.getStage(1), 2);
	    req.majTypeLien(req.getStage(1), req.getStage(2), 2);
		cloud.listeClassesClient.get(0).requeteTezEnAttente.add(req);
		
		/*req=new Requete(0.75,20);
	    req.rajouterJob(new Job(req,3,2,5,5,8*2,8*2,8*2,8*2,8*2,8*2));
	    req.rajouterJob(new Job(req,3,1,4,2,8*1,8*1,8*1,8*1,8*1,8*1));
	    req.rajouterJob(new Job(req,2,2,3,2,8*1,8*1,8*1,8*1,8*1,8*1));
	    req.jobFinal=req.listeJobs.get(req.listeJobs.size()-1);
	    for(int j=0;j<req.nbJobs();j++){
	    	for(int k=0;k<req.nbJobs();k++){
	    		req.majQuantiteTransfertJobs(req.getJob(j), req.getJob(k), 0);
	    	}
	    }
	    req.majQuantiteTransfertJobs(req.getJob(0), req.getJob(0), 2);
	    req.majQuantiteTransfertJobs(req.getJob(1), req.getJob(1), 2);
	    req.majQuantiteTransfertJobs(req.getJob(2), req.getJob(2), 2);
	    req.majQuantiteTransfertJobs(req.getJob(0), req.getJob(1), 2);
	    req.majQuantiteTransfertJobs(req.getJob(1), req.getJob(2), 2);
		cloud.listeClassesClient.get(0).requeteEnAttente.add(req);*/
		
		/*if(v==1){
		VariablesGlobales.writer_gbrt=new FileWriter("comp61-gbrt.csv",true);
		Gantt gantt;
		GBRT gbrt=new GBRT(cloud);
		long startTime = System.currentTimeMillis();
		Cout cout=gbrt.lancer();
		long stopTime = System.currentTimeMillis();
	    long elapsedTime = stopTime - startTime;
	    System.out.println("Temps = "+elapsedTime);
	    gantt=gbrt.ecrireResultats();
	    gantt.ecrireDansFichier();
	    cloud.allouerRessourcesMR(gantt);
	    CSVUtils.writeLine(VariablesGlobales.writer_gbrt, Arrays.asList(Long.toString(elapsedTime), Double.toString(cout.tempsExecTotal), Double.toString(cout.coutRess()),Double.toString(cout.coutComm),Double.toString(cout.coutPenalite),Double.toString(cout.sommeCouts())));
		VariablesGlobales.writer_gbrt.flush();
		VariablesGlobales.writer_gbrt.close();
		}
		else if(v==2){
	    VariablesGlobales.writer_gmpt=new FileWriter("comp61-gmpt.csv",true);
		Gantt gantt;
	    GMPT gmpt=new GMPT(cloud);
		long startTime = System.currentTimeMillis();
		Cout cout=gmpt.lancer();
		long stopTime = System.currentTimeMillis();
	    long elapsedTime = stopTime - startTime;
	    System.out.println("Temps = "+elapsedTime);
	    gantt=gmpt.ecrireResultats();
	    gantt.ecrireDansFichier();
	    cloud.allouerRessourcesMR(gantt);
	    CSVUtils.writeLine(VariablesGlobales.writer_gmpt, Arrays.asList(Long.toString(elapsedTime), Double.toString(cout.tempsExecTotal), Double.toString(cout.coutRess()),Double.toString(cout.coutComm),Double.toString(cout.coutPenalite),Double.toString(cout.sommeCouts())));
		VariablesGlobales.writer_gmpt.flush();
		VariablesGlobales.writer_gmpt.close();
		}
		else if(v==3){*/
	    /*VariablesGlobales.writer_gmpm=new FileWriter("comp99-gmpm.csv",true);
		Gantt gantt;
		GMPMTEZ gmpm=new GMPMTEZ(cloud);
		long startTime = System.currentTimeMillis();
		Cout cout=gmpm.lancer();
		long stopTime = System.currentTimeMillis();
	    long elapsedTime = stopTime - startTime;
	    System.out.println("Temps = "+elapsedTime);
	    gantt=gmpm.ecrireResultats();
	    gantt.ecrireDansFichier();
	    cloud.allouerRessourcesTez(gantt);
	    CSVUtils.writeLine(VariablesGlobales.writer_gmpm, Arrays.asList(Long.toString(elapsedTime), Double.toString(cout.tempsExecTotal), Double.toString(cout.coutRess()),Double.toString(cout.coutComm),Double.toString(cout.coutPenalite),Double.toString(cout.sommeCouts())));
		VariablesGlobales.writer_gmpm.flush();
		VariablesGlobales.writer_gmpm.close();*/
		//}
		//else if(v==4){
		
	    VariablesGlobales.writer_pl2p=new FileWriter("compTez1.csv",true);
	    Cout cout=new Cout();
		Gantt gantt;
		ModelePlacementGLPKTEZ mo=new ModelePlacementGLPKTEZ(cloud,cout);
		long startTime = System.currentTimeMillis();
		mo.resoudre();
		long stopTime = System.currentTimeMillis();
	    long elapsedTime = stopTime - startTime;
	    if(VariablesGlobales.verbose) System.out.println("Temps Placement = "+elapsedTime);
	    ModeleOrdonnancementGLPKTez glpk=new ModeleOrdonnancementGLPKTez(cloud,cout,mo.A);
	    startTime = System.currentTimeMillis();
	    gantt=glpk.resoudre();
	    stopTime = System.currentTimeMillis();
	    elapsedTime = elapsedTime+ (stopTime - startTime);
	    if(VariablesGlobales.verbose) System.out.println("Temps Ordonnancement = "+elapsedTime);
	    gantt.ecrireDansFichier();
	    cloud.allouerRessourcesTez(gantt);
	    CSVUtils.writeLine(VariablesGlobales.writer_pl2p, Arrays.asList(Long.toString(elapsedTime), Double.toString(0), Double.toString(cout.coutRess()),Double.toString(cout.coutComm),Double.toString(cout.coutPenalite),Double.toString(cout.sommeCouts())));
		VariablesGlobales.writer_pl2p.flush();
		VariablesGlobales.writer_pl2p.close();
		
		//}
		/*else if(v==5){
			Gantt gantt;
			ModeleUnePhaseGLPKV2 mup=new ModeleUnePhaseGLPKV2(cloud);
			long startTime = System.currentTimeMillis();
			gantt=mup.resoudre();
			long stopTime = System.currentTimeMillis();
		    long elapsedTime = stopTime - startTime;
		    System.out.println("Temps Modele Une phase = "+elapsedTime);
		}*/
	} 
}
