package Simulation;

import java.io.FileWriter;
import java.io.IOException;

public class Test1 {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		FileWriter fw = new FileWriter("out.txt");
		 
		for (int i = 0; i < 10; i++) {
			fw.write("something");
		}
	 
		fw.close();
	}

}
