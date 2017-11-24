package Entite;

public class GenerateurRequetes {
	public static int nbRequeues=2;
	public static RequeteTez genererRequeteAleatoirement(Cloud cloud) {
		double random=(int)(Math.random() * nbRequeues);
		if(random==1) { return requeteType1(cloud); }
		else  { return requeteType2(cloud); }
	}
	
	public static RequeteTez requeteType1(Cloud cloud) {
		RequeteTez req=new RequeteTez(0.25,10,cloud);
		req.rajouterStage(new StageTez(req,3,3,8*2,8*2,8*2));
		req.rajouterStage(new StageTez(req,2,2,8*1,8*1,8*1));
	    req.stageFinal=req.listeStages.get(req.listeStages.size()-1);
	    for(int j=0;j<req.nbStages();j++){
	    	for(int k=0;k<req.nbStages();k++){
	    		req.majQuantiteTransfertStages(req.getStage(j), req.getStage(k), 0);
	    		req.majTypeLien(req.getStage(j), req.getStage(k), 0);
	    	}
	    }
	    req.majQuantiteTransfertStages(req.getStage(0), req.getStage(1), 2);
	    req.majTypeLien(req.getStage(0), req.getStage(1), 1);
		cloud.listeClassesClient.get(0).requeteTezEnAttente.add(req);
		return req;
	}
	
	public static RequeteTez requeteType2(Cloud cloud) {
		RequeteTez req=new RequeteTez(0.5,30,cloud);
	    req.rajouterStage(new StageTez(req,3,3,8*2,8*2,8*2));
	    req.rajouterStage(new StageTez(req,3,2,8*1,8*1,8*1));
	    req.rajouterStage(new StageTez(req,2,2,8*1,8*1,8*1));
	    req.stageFinal=req.listeStages.get(req.listeStages.size()-1);
	    for(int j=0;j<req.nbStages();j++){
	    	for(int k=0;k<req.nbStages();k++){
	    		req.majQuantiteTransfertStages(req.getStage(j), req.getStage(k), 0);
	    		req.majTypeLien(req.getStage(j), req.getStage(k), 0);
	    	}
	    }
	    req.majQuantiteTransfertStages(req.getStage(0), req.getStage(1), 2);
	    req.majQuantiteTransfertStages(req.getStage(1), req.getStage(2), 2);
	    req.majTypeLien(req.getStage(0), req.getStage(1), 1);
	    req.majTypeLien(req.getStage(1), req.getStage(2), 1);
		cloud.listeClassesClient.get(0).requeteTezEnAttente.add(req);
		return req;
	}

}
