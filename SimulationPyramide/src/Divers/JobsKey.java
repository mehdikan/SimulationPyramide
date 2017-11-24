package Divers;

import Entite.*;

public class JobsKey {
    private final Job job1;
    private final Job job2;

    public JobsKey(Job job1, Job job2) {
        this.job1 =job1;
        this.job2 = job2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JobsKey)) return false;
        JobsKey key = (JobsKey) o;
        return job1 == key.job1 && job2 == key.job2;
    }

    @Override
    public int hashCode() {
        int result = job1.hashCode();
        result = 31 * result + job2.hashCode();
        return result;
    }

}