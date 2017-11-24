package SimulationTez;

import java.util.HashMap;
import java.util.Map;


public class Test15 {
	static Map<Integer, Test15> map = new HashMap<Integer, Test15>();
	public static void main(String[] args) {
	    Runtime rt = Runtime.getRuntime();
	    long prevTotal = 0;
	    long prevFree = rt.freeMemory();

	    for (int i = 0; i < 2_000_000; i++) {
	        long total = rt.totalMemory();
	        long free = rt.freeMemory();
	        if (total != prevTotal || free != prevFree) {
	            System.out.println(
	                String.format("#%s, Total: %s, Free: %s, Diff: %s",
	                    i, 
	                    rt.totalMemory(),
	                    rt.freeMemory(),
	                    rt.freeMemory() - rt.freeMemory()));
	            prevTotal = total;
	            prevFree = free;
	        }
	        map.put(i, new Test15());
	    }
	}
}
