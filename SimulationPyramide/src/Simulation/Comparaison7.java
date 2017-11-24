package Simulation;

import java.io.IOException;

import Algorithmes.*;
import Divers.VariablesGlobales;
import Entite.ClasseClients;
import Entite.Cloud;
import Entite.Job;
import Entite.MachinePhysique;
import Entite.Requete;
import Entite.TypeVM;
import Entite.VM;
import Gantt.Gantt;
import PLNE.*;

public class Comparaison7 {
	public static void main(String[] args) throws IOException{		
		Gantt gantt;
		Cloud cloud=new Cloud(1,1,1,1);
		
		TypeVM type1=new TypeVM(36,60000,1000000);
		TypeVM type2=new TypeVM(36,60000,1000000);
		TypeVM type3=new TypeVM(36,60000,1000000);
		cloud.listeMachinesPhysique.add(new MachinePhysique());
		/*cloud.ajouterVM(0,new VM(type1,2,2,3,3,3,4,3,3));
		cloud.ajouterVM(0,new VM(type2,2,2,3,3,3,4,3,3));
		cloud.ajouterVM(0,new VM(type3,2,2,3,3,3,4,3,3));*/
		
		cloud.ajouterVM(0,new VM(type1,2,2,4,4,4,5,4,4));
		cloud.ajouterVM(0,new VM(type3,2,2,3,3,3,4,3,3));
		cloud.ajouterVM(0,new VM(type2,2,2,5,5,5,6,5,5));
		

		cloud.tousCandidatsMap();
		cloud.tousCandidatsReduce();
		cloud.setDistanceDefaultMR();
		
		cloud.listeClassesClient.add(new ClasseClients(1));
	    
		Requete req=new Requete(1,20);
	    req.rajouterJob(new Job(req,4,3,6,6,2,2,2,2,2,2));
	    req.rajouterJob(new Job(req,4,2,6,6,2,2,2,2,2,2));
	    req.rajouterJob(new Job(req,3,3,3,2,2,2,2,2,2,2));
	    req.jobFinal=req.listeJobs.get(req.listeJobs.size()-1);
	    for(int j=0;j<req.nbJobs();j++){
	    	for(int k=0;k<req.nbJobs();k++){
	    		req.majQuantiteTransfertJobs(req.getJob(j), req.getJob(k), 0);
	    	}
	    }
	    req.majQuantiteTransfertJobs(req.getJob(0), req.getJob(0), 1);
	    req.majQuantiteTransfertJobs(req.getJob(1), req.getJob(1), 1);
	    req.majQuantiteTransfertJobs(req.getJob(2), req.getJob(2), 1);
	    req.majQuantiteTransfertJobs(req.getJob(0), req.getJob(2), 1);
	    req.majQuantiteTransfertJobs(req.getJob(1), req.getJob(2), 1);
		cloud.listeClassesClient.get(0).requeteEnAttente.add(req);
		
		req=new Requete(5,20);
	    req.rajouterJob(new Job(req,5,2,3,3,2,2,2,2,2,2));
	    req.rajouterJob(new Job(req,4,3,1,1,2,2,2,2,2,2));
	    req.rajouterJob(new Job(req,3,2,2,2,2,2,2,2,2,2));
	    req.jobFinal=req.listeJobs.get(req.listeJobs.size()-1);
	    for(int j=0;j<req.nbJobs();j++){
	    	for(int k=0;k<req.nbJobs();k++){
	    		req.majQuantiteTransfertJobs(req.getJob(j), req.getJob(k), 0);
	    	}
	    }
	    req.majQuantiteTransfertJobs(req.getJob(0), req.getJob(0), 1);
	    req.majQuantiteTransfertJobs(req.getJob(1), req.getJob(1), 1);
	    req.majQuantiteTransfertJobs(req.getJob(2), req.getJob(2), 1);
	    req.majQuantiteTransfertJobs(req.getJob(0), req.getJob(2), 1);
	    req.majQuantiteTransfertJobs(req.getJob(1), req.getJob(2), 1);
		cloud.listeClassesClient.get(0).requeteEnAttente.add(req);
		
		req=new Requete(10,20);
	    req.rajouterJob(new Job(req,3,3,2,2,3,3,3,3,3,3));
	    req.rajouterJob(new Job(req,3,2,1,1,3,3,3,3,3,3));
	    req.jobFinal=req.listeJobs.get(req.listeJobs.size()-1);
	    for(int j=0;j<req.nbJobs();j++){
	    	for(int k=0;k<req.nbJobs();k++){
	    		req.majQuantiteTransfertJobs(req.getJob(j), req.getJob(k), 0);
	    	}
	    }
	    req.majQuantiteTransfertJobs(req.getJob(0), req.getJob(0), 1);
	    req.majQuantiteTransfertJobs(req.getJob(1), req.getJob(1), 1);
	    req.majQuantiteTransfertJobs(req.getJob(0), req.getJob(1), 1);
		cloud.listeClassesClient.get(0).requeteEnAttente.add(req);
		
		/*BaseLinePO algo=new BaseLinePO(cloud);	    
		algo.lancer();
		System.out.println("cout Ordonnancement = "+algo.coutOrdonnancementTotal());
		System.out.println("cout Placement = "+algo.coutPlacementTotal());
		algo.ecrireResultatsDansFichier();*/
		
		/*FIFO algoFifO=new FIFO(cloud);	    
		algoFifO.lancer();
		algoFifO.coutOrdonnancementTotal();
		algoFifO.coutPlacementTotal();
		gantt=algoFifO.ecrireResultats();*/
		
		GBRT gbrt=new GBRT(cloud);
		long startTime = System.currentTimeMillis();
		gbrt.lancer();
		long stopTime = System.currentTimeMillis();
	    long elapsedTime = stopTime - startTime;
	    System.out.println("Temps = "+elapsedTime);
	    gantt=gbrt.ecrireResultats();
	    
	    /*GMPT gmpt=new GMPT(cloud);
		long startTime = System.currentTimeMillis();
		gmpt.lancer();
		long stopTime = System.currentTimeMillis();
	    long elapsedTime = stopTime - startTime;
	    System.out.println("Temps = "+elapsedTime);
	    gantt=gmpt.ecrireResultats();*/
		
		/*GMPM gmpm=new GMPM(cloud);
		long startTime = System.currentTimeMillis();
		gmpm.lancer();
		long stopTime = System.currentTimeMillis();
	    long elapsedTime = stopTime - startTime;
	    System.out.println("Temps = "+elapsedTime);
	    gantt=gmpm.ecrireResultats();*/

		/*ModelePlacementGLPKV5 mo=new ModelePlacementGLPKV5(cloud);
		long startTime = System.currentTimeMillis();
		mo.resoudre();
		long stopTime = System.currentTimeMillis();
	    long elapsedTime = stopTime - startTime;
	    System.out.println("Temps Placement = "+elapsedTime);
	    
	    ModeleOrdonnancementGLPK glpk=new ModeleOrdonnancementGLPK(cloud,mo.Am,mo.Ar);
	    startTime = System.currentTimeMillis();
	    gantt=glpk.resoudre();
	    stopTime = System.currentTimeMillis();
	    elapsedTime = stopTime - startTime;
	    System.out.println("Temps Ordonnancement = "+elapsedTime);*/
		
		/*ModeleUnePhaseGLPKV2 mup=new ModeleUnePhaseGLPKV2(cloud);
		long startTime = System.currentTimeMillis();
		gantt=mup.resoudre();
		long stopTime = System.currentTimeMillis();
	    long elapsedTime = stopTime - startTime;
	    System.out.println("Temps Modele Une phase = "+elapsedTime);*/
	    
	    gantt.ecrireDansFichier();
	    cloud.allouerRessourcesMR(gantt);
	    
	    
	    //////////////////////////////////////////////////////////////////////////////////////
	    cloud.listeClassesClient.get(0).requeteEnAttente.clear();
	    cloud.decalerTempsMR(12);
	    VariablesGlobales.init();
	        
	    req=new Requete(1,15);
	    req.rajouterJob(new Job(req,4,3,6,6,2,2,2,2,2,2));
	    req.rajouterJob(new Job(req,4,2,2,3,2,2,2,2,2,2));
	    req.rajouterJob(new Job(req,3,3,3,2,2,2,2,2,2,2));
	    req.jobFinal=req.listeJobs.get(req.listeJobs.size()-1);
	    for(int j=0;j<req.nbJobs();j++){
	    	for(int k=0;k<req.nbJobs();k++){
	    		req.majQuantiteTransfertJobs(req.getJob(j), req.getJob(k), 0);
	    	}
	    }
	    req.majQuantiteTransfertJobs(req.getJob(0), req.getJob(0), 1);
	    req.majQuantiteTransfertJobs(req.getJob(1), req.getJob(1), 1);
	    req.majQuantiteTransfertJobs(req.getJob(2), req.getJob(2), 1);
	    req.majQuantiteTransfertJobs(req.getJob(0), req.getJob(2), 1);
	    req.majQuantiteTransfertJobs(req.getJob(1), req.getJob(2), 1);
		cloud.listeClassesClient.get(0).requeteEnAttente.add(req);
		
		req=new Requete(1,15);
	    req.rajouterJob(new Job(req,5,2,3,3,2,2,2,2,2,2));
	    req.rajouterJob(new Job(req,4,3,1,1,2,2,2,2,2,2));
	    req.rajouterJob(new Job(req,3,2,2,2,2,2,2,2,2,2));
	    req.jobFinal=req.listeJobs.get(req.listeJobs.size()-1);
	    for(int j=0;j<req.nbJobs();j++){
	    	for(int k=0;k<req.nbJobs();k++){
	    		req.majQuantiteTransfertJobs(req.getJob(j), req.getJob(k), 0);
	    	}
	    }
	    req.majQuantiteTransfertJobs(req.getJob(0), req.getJob(0), 1);
	    req.majQuantiteTransfertJobs(req.getJob(1), req.getJob(1), 1);
	    req.majQuantiteTransfertJobs(req.getJob(2), req.getJob(2), 1);
	    req.majQuantiteTransfertJobs(req.getJob(0), req.getJob(2), 1);
	    req.majQuantiteTransfertJobs(req.getJob(1), req.getJob(2), 1);
		cloud.listeClassesClient.get(0).requeteEnAttente.add(req);
		
		req=new Requete(1,15);
	    req.rajouterJob(new Job(req,3,3,2,2,3,3,3,3,3,3));
	    req.rajouterJob(new Job(req,3,2,1,1,3,3,3,3,3,3));
	    req.jobFinal=req.listeJobs.get(req.listeJobs.size()-1);
	    for(int j=0;j<req.nbJobs();j++){
	    	for(int k=0;k<req.nbJobs();k++){
	    		req.majQuantiteTransfertJobs(req.getJob(j), req.getJob(k), 0);
	    	}
	    }
	    req.majQuantiteTransfertJobs(req.getJob(0), req.getJob(0), 1);
	    req.majQuantiteTransfertJobs(req.getJob(1), req.getJob(1), 1);
	    req.majQuantiteTransfertJobs(req.getJob(0), req.getJob(1), 1);
		cloud.listeClassesClient.get(0).requeteEnAttente.add(req);
	    
		/*algoFifO=new FIFO(cloud);	    
		algoFifO.lancer();
		algoFifO.coutOrdonnancementTotal();
		algoFifO.coutPlacementTotal();
		gantt=algoFifO.ecrireResultats();*/
		
		/*mo=new ModelePlacementGLPKV5(cloud);
		startTime = System.currentTimeMillis();
		mo.resoudre();
		stopTime = System.currentTimeMillis();
	    elapsedTime = stopTime - startTime;
	    System.out.println("Temps Placement = "+elapsedTime);
	    
	    glpk=new ModeleOrdonnancementGLPK(cloud,mo.Am,mo.Ar);
	    startTime = System.currentTimeMillis();
	    gantt=glpk.resoudre();
	    stopTime = System.currentTimeMillis();
	    elapsedTime = stopTime - startTime;
	    System.out.println("Temps Ordonnancement = "+elapsedTime);*/
	    
	    
	    //gantt.ecrireDansFichier();
	    //cloud.allouerRessourcesMR(gantt);
	}
}
