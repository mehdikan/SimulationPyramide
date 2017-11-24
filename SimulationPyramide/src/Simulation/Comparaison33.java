package Simulation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import Algorithmes.*;
import Divers.CSVUtils;
import Divers.VariablesGlobales;
import Entite.ClasseClients;
import Entite.Cloud;
import Entite.Cout;
import Entite.Job;
import Entite.MachinePhysique;
import Entite.Requete;
import Entite.TypeVM;
import Entite.VM;
import Gantt.Gantt;
import PLNE.*;

public class Comparaison33 {
	public static void main(String[] args) throws IOException{		
		int v=Integer.parseInt(args[0]);
		VariablesGlobales.T=Integer.parseInt(args[1]);
		int ligneNum=Integer.parseInt(args[2]);
		
		try (BufferedReader br = new BufferedReader(new FileReader("gen.txt"))) {
		    String line;
		    int cpt=1;
		    while ((line = br.readLine()) != null) {
		    	if(cpt==ligneNum){
		        	VariablesGlobales.niveauDisponiblite=Double.parseDouble(line);
		        }
		        cpt++;
		    }
		}
		Cloud cloud=new Cloud(1,1,1,1);
		
		TypeVM type1=new TypeVM(36,60000,1000000);
		TypeVM type2=new TypeVM(36,60000,1000000);
		cloud.listeMachinesPhysique.add(new MachinePhysique());

		cloud.ajouterVM(0,new VM(type2,1,1,8*2,8*2,8*2,8*2,8*2,8*2));
		cloud.ajouterVM(0,new VM(type2,1,1,8*2,8*2,8*2,8*2,8*2,8*2));
		cloud.ajouterVM(0,new VM(type2,1,1,8*2,8*2,8*2,8*2,8*2,8*2));
		cloud.ajouterVM(0,new VM(type2,1,1,8*2,8*2,8*2,8*2,8*2,8*2));
		cloud.ajouterVM(0,new VM(type2,1,1,8*2,8*2,8*2,8*2,8*2,8*2));
		cloud.ajouterVM(0,new VM(type1,1,1,8*1,8*1,8*1,8*1,8*1,8*1));
		cloud.ajouterVM(0,new VM(type1,1,1,8*1,8*1,8*1,8*1,8*1,8*1));
		cloud.ajouterVM(0,new VM(type1,1,1,8*1,8*1,8*1,8*1,8*1,8*1));
		cloud.ajouterVM(0,new VM(type1,1,1,8*1,8*1,8*1,8*1,8*1,8*1));
		cloud.ajouterVM(0,new VM(type1,1,1,8*1,8*1,8*1,8*1,8*1,8*1));

		cloud.tousCandidatsMap();
		cloud.tousCandidatsReduce();
		cloud.setDistanceDefaultMR();
		
		cloud.listeClassesClient.add(new ClasseClients(1));
	    
		Requete req=new Requete(1,45);
	    req.rajouterJob(new Job(req,3,1,4,2,8*2,8*2,8*2,8*1,8*1,8*1));
	    req.rajouterJob(new Job(req,2,2,3,2,8*1,8*1,8*1,8*1,8*1,8*1));
	    req.jobFinal=req.listeJobs.get(req.listeJobs.size()-1);
	    for(int j=0;j<req.nbJobs();j++){
	    	for(int k=0;k<req.nbJobs();k++){
	    		req.majQuantiteTransfertJobs(req.getJob(j), req.getJob(k), 0);
	    	}
	    }
	    req.majQuantiteTransfertJobs(req.getJob(0), req.getJob(0), 2);
	    req.majQuantiteTransfertJobs(req.getJob(1), req.getJob(1), 2);
	    req.majQuantiteTransfertJobs(req.getJob(0), req.getJob(1), 2);
		cloud.listeClassesClient.get(0).requeteEnAttente.add(req);
		
		req=new Requete(1,40);
	    req.rajouterJob(new Job(req,3,1,4,2,8*1,8*1,8*1,8*1,8*1,8*1));
	    req.rajouterJob(new Job(req,2,2,3,2,8*2,8*2,8*2,8*2,8*2,8*2));
	    req.jobFinal=req.listeJobs.get(req.listeJobs.size()-1);
	    for(int j=0;j<req.nbJobs();j++){
	    	for(int k=0;k<req.nbJobs();k++){
	    		req.majQuantiteTransfertJobs(req.getJob(j), req.getJob(k), 0);
	    	}
	    }
	    req.majQuantiteTransfertJobs(req.getJob(0), req.getJob(0), 2);
	    req.majQuantiteTransfertJobs(req.getJob(1), req.getJob(1), 2);
	    req.majQuantiteTransfertJobs(req.getJob(0), req.getJob(1), 2);
		cloud.listeClassesClient.get(0).requeteEnAttente.add(req);
		
		
		req=new Requete(1.5,35);
		req.rajouterJob(new Job(req,3,2,5,5,8*2,8*2,8*2,8*2,8*2,8*2));
	    req.rajouterJob(new Job(req,3,1,4,2,8*1,8*1,8*1,8*1,8*1,8*1));
	    req.rajouterJob(new Job(req,2,2,3,2,8*1,8*1,8*1,8*1,8*1,8*1));
	    req.rajouterJob(new Job(req,3,2,5,5,8*2,8*2,8*2,8*2,8*2,8*2));
	    req.rajouterJob(new Job(req,3,1,4,2,8*1,8*1,8*1,8*1,8*1,8*1));
	    req.rajouterJob(new Job(req,2,2,3,2,8*1,8*1,8*1,8*1,8*1,8*1));
	    req.rajouterJob(new Job(req,3,1,4,2,8*1,8*1,8*1,8*1,8*1,8*1));
	    req.jobFinal=req.listeJobs.get(req.listeJobs.size()-1);
	    for(int j=0;j<req.nbJobs();j++){
	    	for(int k=0;k<req.nbJobs();k++){
	    		req.majQuantiteTransfertJobs(req.getJob(j), req.getJob(k), 0);
	    	}
	    }
	    req.majQuantiteTransfertJobs(req.getJob(0), req.getJob(0), 2);
	    req.majQuantiteTransfertJobs(req.getJob(1), req.getJob(1), 2);
	    req.majQuantiteTransfertJobs(req.getJob(2), req.getJob(2), 2);
	    req.majQuantiteTransfertJobs(req.getJob(3), req.getJob(3), 2);
	    req.majQuantiteTransfertJobs(req.getJob(4), req.getJob(4), 2);
	    req.majQuantiteTransfertJobs(req.getJob(5), req.getJob(5), 1);
	    req.majQuantiteTransfertJobs(req.getJob(6), req.getJob(6), 1);
	    req.majQuantiteTransfertJobs(req.getJob(0), req.getJob(1), 1);
	    req.majQuantiteTransfertJobs(req.getJob(1), req.getJob(2), 1);
	    req.majQuantiteTransfertJobs(req.getJob(2), req.getJob(3), 1);
	    req.majQuantiteTransfertJobs(req.getJob(3), req.getJob(4), 2);
	    req.majQuantiteTransfertJobs(req.getJob(4), req.getJob(5), 2);
	    req.majQuantiteTransfertJobs(req.getJob(5), req.getJob(6), 2);
		cloud.listeClassesClient.get(0).requeteEnAttente.add(req);
		
		req=new Requete(0.5,30);
	    req.rajouterJob(new Job(req,3,2,5,5,8*2,8*2,8*2,8*2,8*2,8*2));
	    req.rajouterJob(new Job(req,3,1,4,2,8*1,8*1,8*1,8*1,8*1,8*1));
	    req.rajouterJob(new Job(req,2,2,3,2,8*1,8*1,8*1,8*1,8*1,8*1));
	    req.rajouterJob(new Job(req,3,2,5,5,8*2,8*2,8*2,8*2,8*2,8*2));
	    req.rajouterJob(new Job(req,3,1,4,2,8*1,8*1,8*1,8*1,8*1,8*1));
	    req.rajouterJob(new Job(req,2,2,3,2,8*1,8*1,8*1,8*1,8*1,8*1));
	    req.rajouterJob(new Job(req,3,1,4,2,8*1,8*1,8*1,8*1,8*1,8*1));
	    req.jobFinal=req.listeJobs.get(req.listeJobs.size()-1);
	    for(int j=0;j<req.nbJobs();j++){
	    	for(int k=0;k<req.nbJobs();k++){
	    		req.majQuantiteTransfertJobs(req.getJob(j), req.getJob(k), 0);
	    	}
	    }
	    req.majQuantiteTransfertJobs(req.getJob(0), req.getJob(0), 2);
	    req.majQuantiteTransfertJobs(req.getJob(1), req.getJob(1), 2);
	    req.majQuantiteTransfertJobs(req.getJob(2), req.getJob(2), 2);
	    req.majQuantiteTransfertJobs(req.getJob(3), req.getJob(3), 2);
	    req.majQuantiteTransfertJobs(req.getJob(4), req.getJob(4), 2);
	    req.majQuantiteTransfertJobs(req.getJob(5), req.getJob(5), 2);
	    req.majQuantiteTransfertJobs(req.getJob(6), req.getJob(6), 2);
	    req.majQuantiteTransfertJobs(req.getJob(0), req.getJob(1), 2);
	    req.majQuantiteTransfertJobs(req.getJob(1), req.getJob(2), 2);
	    req.majQuantiteTransfertJobs(req.getJob(2), req.getJob(3), 2);
	    req.majQuantiteTransfertJobs(req.getJob(3), req.getJob(4), 2);
	    req.majQuantiteTransfertJobs(req.getJob(4), req.getJob(5), 2);
	    req.majQuantiteTransfertJobs(req.getJob(5), req.getJob(6), 2);
		cloud.listeClassesClient.get(0).requeteEnAttente.add(req);
		
		if(v==1){
			VariablesGlobales.writer_gbrt=new FileWriter("comp33-gbrt.csv",true);
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
		    VariablesGlobales.writer_gmpt=new FileWriter("comp33-gmpt.csv",true);
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
			else if(v==3){
		    VariablesGlobales.writer_gmpm=new FileWriter("comp33-gmpm.csv",true);
			Gantt gantt;
			GMPM gmpm=new GMPM(cloud);
			long startTime = System.currentTimeMillis();
			Cout cout=gmpm.lancer();
			long stopTime = System.currentTimeMillis();
		    long elapsedTime = stopTime - startTime;
		    System.out.println("Temps = "+elapsedTime);
		    gantt=gmpm.ecrireResultats();
		    gantt.ecrireDansFichier();
		    cloud.allouerRessourcesMR(gantt);
		    CSVUtils.writeLine(VariablesGlobales.writer_gmpm, Arrays.asList(Long.toString(elapsedTime), Double.toString(cout.tempsExecTotal), Double.toString(cout.coutRess()),Double.toString(cout.coutComm),Double.toString(cout.coutPenalite),Double.toString(cout.sommeCouts())));
			VariablesGlobales.writer_gmpm.flush();
			VariablesGlobales.writer_gmpm.close();
			}
			else if(v==4){
		    VariablesGlobales.writer_pl2p=new FileWriter("comp33-pl2p.csv",true);
		    Cout cout=new Cout();
			Gantt gantt;
			ModelePlacementGLPKV7 mo=new ModelePlacementGLPKV7(cloud,cout);
			long startTime = System.currentTimeMillis();
			mo.resoudre();
			long stopTime = System.currentTimeMillis();
		    long elapsedTime = stopTime - startTime;
		    System.out.println("Temps Placement = "+elapsedTime);
		    ModeleOrdonnancementGLPK glpk=new ModeleOrdonnancementGLPK(cloud,cout,mo.Am,mo.Ar);
		    startTime = System.currentTimeMillis();
		    gantt=glpk.resoudre();
		    stopTime = System.currentTimeMillis();
		    elapsedTime = elapsedTime+ (stopTime - startTime);
		    System.out.println("Temps Ordonnancement = "+elapsedTime);
		    gantt.ecrireDansFichier();
		    cloud.allouerRessourcesMR(gantt);
		    CSVUtils.writeLine(VariablesGlobales.writer_pl2p, Arrays.asList(Long.toString(elapsedTime), Double.toString(0), Double.toString(cout.coutRess()),Double.toString(cout.coutComm),Double.toString(cout.coutPenalite),Double.toString(cout.sommeCouts())));
			
			VariablesGlobales.writer_pl2p.flush();
			VariablesGlobales.writer_pl2p.close();
			}
	    
	   
	}
}
