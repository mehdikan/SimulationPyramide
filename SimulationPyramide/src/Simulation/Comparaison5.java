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

public class Comparaison5 {
	public static void main(String[] args){		
		Gantt gantt;
		Cloud cloud=new Cloud(1,1,1,1);
		
		TypeVM type1=new TypeVM(36,60000,1000000);
		TypeVM type2=new TypeVM(36,60000,1000000);
		TypeVM type3=new TypeVM(36,60000,1000000);
		cloud.listeMachinesPhysique.add(new MachinePhysique());
		cloud.ajouterVM(0,new VM(type1,2,2,3,3,3,4,3,3));
		cloud.ajouterVM(0,new VM(type2,2,2,3,3,3,4,3,3));
		cloud.ajouterVM(0,new VM(type3,2,2,4,3,3,4,3,3));
		
		cloud.tousCandidatsMap();
		cloud.tousCandidatsReduce();
		cloud.setDistanceDefaultMR();
		
		cloud.listeClassesClient.add(new ClasseClients(1));
	    
		Requete req=new Requete(1,8);
	    req.rajouterJob(new Job(req,1,2,3,3,2,2,2,2,2,2));
	    req.rajouterJob(new Job(req,2,1,2,3,2,2,2,2,2,2));
	    req.rajouterJob(new Job(req,1,1,3,2,2,2,2,2,2,2));
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
		
		req=new Requete(5,8);
	    req.rajouterJob(new Job(req,1,1,3,3,2,2,2,2,2,2));
	    req.rajouterJob(new Job(req,2,1,2,2,2,2,2,2,2,2));
	    req.rajouterJob(new Job(req,1,2,2,2,2,2,2,2,2,2));
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
		
		req=new Requete(100,8);
	    req.rajouterJob(new Job(req,1,2,2,2,3,3,3,3,3,3));
	    req.rajouterJob(new Job(req,2,1,2,2,3,3,3,3,3,3));
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
		
		/*FIFO algoFifO=new FIFO(cloud);	    
		algoFifO.lancer();
		algoFifO.coutOrdonnancementTotal();
		algoFifO.coutPlacementTotal();
		gantt=algoFifO.ecrireResultats();*/

		ModelePlacementGLPKV5 mo=new ModelePlacementGLPKV5(cloud);
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
