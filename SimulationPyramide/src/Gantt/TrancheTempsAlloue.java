package Gantt;

public class TrancheTempsAlloue {
	public int type; // Map=1 - Reduce=2
	public int indexRessource;
	public int indexRequete;
	public int indexJob;
	public int indexTache;
	public int dateDebut;
	public int dateFin;
	
	public TrancheTempsAlloue(int type,int indexRessource,int indexRequete,int indexJob,int indexTache,int dateDebut,int dateFin){
		this.type=type;
		this.indexRessource=indexRessource;
		this.indexRequete=indexRequete;
		this.indexJob=indexJob;
		this.indexTache=indexTache;
		this.dateDebut=dateDebut;
		this.dateFin=dateFin;
	}
}
