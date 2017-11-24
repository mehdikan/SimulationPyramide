package Divers;

public class IntKey {
	    private final int ressource;
	    private final int tache;

	    public IntKey(int ressource, int tache) {
	        this.ressource =ressource;
	        this.tache = tache;
	     }

	    @Override
	    public boolean equals(Object o) {
	        if (this == o) return true;
	        if (!(o instanceof IntKey)) return false;
	        IntKey key = (IntKey) o;
	        return ressource == key.ressource && tache == key.tache;
	    }

	    @Override
	    public int hashCode() {
	        int result = ressource;
	        result = 31 * result + tache;
	        return result;
	    }
}
