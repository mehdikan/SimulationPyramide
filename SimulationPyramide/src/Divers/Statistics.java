package Divers;

import java.util.Arrays;
import java.util.HashMap;

import Entite.GroupeRessources;

public class Statistics 
{
    double[] data;
    int size;   

    public Statistics(double[] data) 
    {
        this.data = data;
        size = data.length;
    }
    
    public Statistics(HashMap<GroupeRessources,Integer> data) 
    {
    	this.data=new double[data.size()];
    	int i=0;
    	for(double v:data.values()){
    		this.data[i]=v;
    		i++;
    	}
        size =data.size();
    } 

    public double getMean()
    {
        double sum = 0.0;
        for(double a : data)
            sum += a;
        return sum/size;
    }

    public double getVariance()
    {
        double mean = getMean();
        double temp = 0;
        for(double a :data)
            temp += (a-mean)*(a-mean);
        return temp/(size-1);
    }

    public double getStdDev()
    {
        return Math.sqrt(getVariance());
    }

    public double median() 
    {
       Arrays.sort(data);

       if (data.length % 2 == 0) 
       {
          return (data[(data.length / 2) - 1] + data[data.length / 2]) / 2.0;
       } 
       return data[data.length / 2];
    }
}