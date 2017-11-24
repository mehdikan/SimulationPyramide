package Divers;

import Entite.*;

public class StagesKey {
    private final StageTez stage1;
    private final StageTez stage2;

    public StagesKey(StageTez stage1, StageTez stage2) {
        this.stage1 =stage1;
        this.stage2 = stage2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StagesKey)) return false;
        StagesKey key = (StagesKey) o;
        return stage1 == key.stage1 && stage2 == key.stage2;
    }

    @Override
    public int hashCode() {
        int result = stage1.hashCode();
        result = 31 * result + stage2.hashCode();
        return result;
    }

}