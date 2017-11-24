package MDP;

public class Transition {
	public double proba;
	public Etat prochainEtat;
	
	public Transition(double proba,Etat prochainEtat) {
		this.proba=proba;
		//System.out.println(">>>>>>>>>>>>>>>>> "+prochainEtat);
		this.prochainEtat=prochainEtat;
	}
}
