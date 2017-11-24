package Gantt;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Gantt {
	public ArrayList<TrancheTempsAlloue> tab;
	
	public Gantt(){
		tab=new ArrayList<TrancheTempsAlloue>();
	}

	public void ajouterTrancheTemps(TrancheTempsAlloue tranche){
		tab.add(tranche);
	}
	
	public void ecrireDansFichier(){		
		int nbRessourcesMap=0;
		int nbRessourcesReduce=0;
		int nbRessourcesTez=0;
		try{
		    PrintWriter writer = new PrintWriter("resultat.txt", "UTF-8"); 
		    for(TrancheTempsAlloue tta:tab){
		    	writer.print(tta.type+" "+tta.indexRessource+" "+tta.indexRequete+" "+tta.indexJob+" "+tta.indexTache+" "+tta.dateDebut+" "+tta.dateFin+" ");
		    	if(tta.type==1){
		    		nbRessourcesMap=Math.max(nbRessourcesMap, tta.indexRessource);
		    	}
		    	else if(tta.type==2){
		    		nbRessourcesReduce=Math.max(nbRessourcesReduce, tta.indexRessource);
		    	}
		    	else {
		    		nbRessourcesTez=Math.max(nbRessourcesReduce, tta.indexRessource);
		    	}
		    }
		    for(int i=0;i<nbRessourcesMap;i++){
		    	writer.print(1+" "+i+" "+100+" "+100+" "+100+" "+0+" "+0+" ");
		    }
		    for(int i=0;i<nbRessourcesReduce;i++){
		    	writer.print(2+" "+i+" "+100+" "+100+" "+100+" "+0+" "+0+" ");
		    }
		    for(int i=0;i<nbRessourcesTez;i++){
		    	writer.print(3+" "+i+" "+100+" "+100+" "+100+" "+0+" "+0+" ");
		    }
		    writer.close();
		}
	    catch (IOException e) {
			   // do something
			}
		
		System.out.println("Working Directory = " +
	              System.getProperty("user.dir"));
	}
}
