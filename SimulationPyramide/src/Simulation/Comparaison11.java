package Simulation;

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

public class Comparaison11 {
	public static void main(String[] args){		
		Gantt gantt;
		Cloud cloud=new Cloud(1,1,1,1);
		
		TypeVM type1=new TypeVM(36,60000,1000000);
		TypeVM type2=new TypeVM(36,60000,1000000);
		cloud.listeMachinesPhysique.add(new MachinePhysique());

		cloud.ajouterVM(0,new VM(type2,1,1,8*2,8*512,8*512,8*2,8*512,8*512));
		cloud.ajouterVM(0,new VM(type2,1,1,8*2,8*512,8*512,8*2,8*512,8*512));
		cloud.ajouterVM(0,new VM(type2,1,1,8*2,8*512,8*512,8*2,8*512,8*512));
		cloud.ajouterVM(0,new VM(type2,1,1,8*2,8*512,8*512,8*2,8*512,8*512));
		cloud.ajouterVM(0,new VM(type2,1,1,8*2,8*512,8*512,8*2,8*512,8*512));
		cloud.ajouterVM(0,new VM(type1,1,1,8*1,8*256,8*256,8*1,8*256,8*256));
		cloud.ajouterVM(0,new VM(type1,1,1,8*1,8*256,8*256,8*1,8*256,8*256));
		cloud.ajouterVM(0,new VM(type1,1,1,8*1,8*256,8*256,8*1,8*256,8*256));
		cloud.ajouterVM(0,new VM(type1,1,1,8*1,8*256,8*256,8*1,8*256,8*256));
		cloud.ajouterVM(0,new VM(type1,1,1,8*1,8*256,8*256,8*1,8*256,8*256));

		cloud.tousCandidatsMap();
		cloud.tousCandidatsReduce();
		cloud.setDistanceDefaultMR();
		
		cloud.listeClassesClient.add(new ClasseClients(1));
	    
		Requete req=new Requete(50000,50);
		 req.rajouterJob(new Job(req,3,2,5,5,8*2,8*512,8*512,8*2,8*512,8*512));;
	    req.rajouterJob(new Job(req,3,1,4,2,8*1,8*256,8*256,8*1,8*256,8*256));
	    req.rajouterJob(new Job(req,2,2,3,2,8*1,8*256,8*256,8*1,8*256,8*256));
	    req.rajouterJob(new Job(req,3,2,5,5,8*2,8*512,8*512,8*2,8*512,8*512));
	    req.rajouterJob(new Job(req,3,2,5,5,8*2,8*512,8*512,8*2,8*512,8*512));
	    req.rajouterJob(new Job(req,3,2,5,5,8*2,8*512,8*512,8*2,8*512,8*512));
	    req.rajouterJob(new Job(req,3,1,4,2,8*1,8*256,8*256,8*1,8*256,8*256));
	    req.rajouterJob(new Job(req,2,2,3,2,8*1,8*256,8*256,8*1,8*256,8*256));
	    req.rajouterJob(new Job(req,3,1,4,2,8*1,8*256,8*256,8*1,8*256,8*256));
	    req.jobFinal=req.listeJobs.get(req.listeJobs.size()-1);
	    for(int j=0;j<req.nbJobs();j++){
	    	for(int k=0;k<req.nbJobs();k++){
	    		req.majQuantiteTransfertJobs(req.getJob(j), req.getJob(k), 0);
	    	}
	    }
	    req.majQuantiteTransfertJobs(req.getJob(0), req.getJob(0), 1000);
	    req.majQuantiteTransfertJobs(req.getJob(1), req.getJob(1), 1000);
	    req.majQuantiteTransfertJobs(req.getJob(2), req.getJob(2), 1000);
	    req.majQuantiteTransfertJobs(req.getJob(3), req.getJob(3), 1000);
	    req.majQuantiteTransfertJobs(req.getJob(4), req.getJob(4), 1000);
	    req.majQuantiteTransfertJobs(req.getJob(5), req.getJob(5), 1000);
	    req.majQuantiteTransfertJobs(req.getJob(6), req.getJob(6), 1000);
	    req.majQuantiteTransfertJobs(req.getJob(7), req.getJob(7), 1000);
	    req.majQuantiteTransfertJobs(req.getJob(8), req.getJob(8), 1000);
	    req.majQuantiteTransfertJobs(req.getJob(0), req.getJob(1), 1000);
	    req.majQuantiteTransfertJobs(req.getJob(1), req.getJob(2), 1000);
	    req.majQuantiteTransfertJobs(req.getJob(2), req.getJob(3), 1000);
	    req.majQuantiteTransfertJobs(req.getJob(3), req.getJob(4), 1000);
	    req.majQuantiteTransfertJobs(req.getJob(4), req.getJob(5), 1000);
	    req.majQuantiteTransfertJobs(req.getJob(5), req.getJob(6), 1000);
	    req.majQuantiteTransfertJobs(req.getJob(6), req.getJob(7), 1000);
	    req.majQuantiteTransfertJobs(req.getJob(7), req.getJob(8), 1000);
		cloud.listeClassesClient.get(0).requeteEnAttente.add(req);
		
		
		req=new Requete(100000,45);
		req.rajouterJob(new Job(req,3,2,5,5,8*2,8*512,8*512,8*2,8*512,8*512));
	    req.rajouterJob(new Job(req,3,1,4,2,8*1,8*256,8*256,8*1,8*256,8*256));
	    req.rajouterJob(new Job(req,2,2,3,2,8*1,8*256,8*256,8*1,8*256,8*256));
	    req.rajouterJob(new Job(req,3,2,5,5,8*2,8*512,8*512,8*2,8*512,8*512));
	    req.rajouterJob(new Job(req,3,1,4,2,8*1,8*256,8*256,8*1,8*256,8*256));
	    req.rajouterJob(new Job(req,2,2,3,2,8*1,8*256,8*256,8*1,8*256,8*256));
	    req.rajouterJob(new Job(req,3,1,4,2,8*1,8*256,8*256,8*1,8*256,8*256));
	    req.jobFinal=req.listeJobs.get(req.listeJobs.size()-1);
	    for(int j=0;j<req.nbJobs();j++){
	    	for(int k=0;k<req.nbJobs();k++){
	    		req.majQuantiteTransfertJobs(req.getJob(j), req.getJob(k), 0);
	    	}
	    }
	    req.majQuantiteTransfertJobs(req.getJob(0), req.getJob(0), 1000);
	    req.majQuantiteTransfertJobs(req.getJob(1), req.getJob(1), 1000);
	    req.majQuantiteTransfertJobs(req.getJob(2), req.getJob(2), 1000);
	    req.majQuantiteTransfertJobs(req.getJob(3), req.getJob(3), 1000);
	    req.majQuantiteTransfertJobs(req.getJob(4), req.getJob(4), 1000);
	    req.majQuantiteTransfertJobs(req.getJob(5), req.getJob(5), 1000);
	    req.majQuantiteTransfertJobs(req.getJob(6), req.getJob(6), 1000);
	    req.majQuantiteTransfertJobs(req.getJob(0), req.getJob(1), 1000);
	    req.majQuantiteTransfertJobs(req.getJob(1), req.getJob(2), 1000);
	    req.majQuantiteTransfertJobs(req.getJob(2), req.getJob(3), 1000);
	    req.majQuantiteTransfertJobs(req.getJob(3), req.getJob(4), 1000);
	    req.majQuantiteTransfertJobs(req.getJob(4), req.getJob(5), 1000);
	    req.majQuantiteTransfertJobs(req.getJob(5), req.getJob(6), 1000);
		cloud.listeClassesClient.get(0).requeteEnAttente.add(req);
		
		req=new Requete(150000,40);
	    req.rajouterJob(new Job(req,3,2,5,5,8*2,8*512,8*512,8*2,8*512,8*512));
	    req.rajouterJob(new Job(req,3,1,4,2,8*1,8*256,8*256,8*1,8*256,8*256));
	    req.rajouterJob(new Job(req,2,2,3,2,8*1,8*256,8*256,8*1,8*256,8*256));
	    req.rajouterJob(new Job(req,3,2,5,5,8*2,8*512,8*512,8*2,8*512,8*512));
	    req.rajouterJob(new Job(req,3,1,4,2,8*1,8*256,8*256,8*1,8*256,8*256));
	    req.rajouterJob(new Job(req,2,2,3,2,8*1,8*256,8*256,8*1,8*256,8*256));
	    req.rajouterJob(new Job(req,3,1,4,2,8*1,8*256,8*256,8*1,8*256,8*256));
	    req.jobFinal=req.listeJobs.get(req.listeJobs.size()-1);
	    for(int j=0;j<req.nbJobs();j++){
	    	for(int k=0;k<req.nbJobs();k++){
	    		req.majQuantiteTransfertJobs(req.getJob(j), req.getJob(k), 0);
	    	}
	    }
	    req.majQuantiteTransfertJobs(req.getJob(0), req.getJob(0), 1000);
	    req.majQuantiteTransfertJobs(req.getJob(1), req.getJob(1), 1000);
	    req.majQuantiteTransfertJobs(req.getJob(2), req.getJob(2), 1000);
	    req.majQuantiteTransfertJobs(req.getJob(3), req.getJob(3), 1000);
	    req.majQuantiteTransfertJobs(req.getJob(4), req.getJob(4), 1000);
	    req.majQuantiteTransfertJobs(req.getJob(5), req.getJob(5), 1000);
	    req.majQuantiteTransfertJobs(req.getJob(6), req.getJob(6), 1000);
	    req.majQuantiteTransfertJobs(req.getJob(0), req.getJob(1), 1000);
	    req.majQuantiteTransfertJobs(req.getJob(1), req.getJob(2), 1000);
	    req.majQuantiteTransfertJobs(req.getJob(2), req.getJob(3), 1000);
	    req.majQuantiteTransfertJobs(req.getJob(3), req.getJob(4), 1000);
	    req.majQuantiteTransfertJobs(req.getJob(4), req.getJob(5), 1000);
	    req.majQuantiteTransfertJobs(req.getJob(5), req.getJob(6), 1000);
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
		
		/*GBRT gbrt=new GBRT(cloud);
		long startTime = System.currentTimeMillis();
		gbrt.lancer();
		long stopTime = System.currentTimeMillis();
	    long elapsedTime = stopTime - startTime;
	    System.out.println("Temps = "+elapsedTime);
	    gantt=gbrt.ecrireResultats();*/
	   
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

		ModelePlacementGLPKV6 mo=new ModelePlacementGLPKV6(cloud);
		long startTime = System.currentTimeMillis();
		mo.resoudre();
		long stopTime = System.currentTimeMillis();
	    long elapsedTime = stopTime - startTime;
	    System.out.println("Temps Placement = "+elapsedTime);
	    
	    ModeleOrdonnancementGLPK glpk=new ModeleOrdonnancementGLPK(cloud,null,mo.Am,mo.Ar);
	    startTime = System.currentTimeMillis();
	    gantt=glpk.resoudre();
	    stopTime = System.currentTimeMillis();
	    elapsedTime = stopTime - startTime;
	    System.out.println("Temps Ordonnancement = "+elapsedTime);
		
		/*ModeleUnePhaseGLPKV2 mup=new ModeleUnePhaseGLPKV2(cloud);
		long startTime = System.currentTimeMillis();
		gantt=mup.resoudre();
		long stopTime = System.currentTimeMillis();
	    long elapsedTime = stopTime - startTime;
	    System.out.println("Temps Modele Une phase = "+elapsedTime);*/
	    
	    gantt.ecrireDansFichier();
	    cloud.allouerRessourcesMR(gantt);
	    
	   
	}
}
