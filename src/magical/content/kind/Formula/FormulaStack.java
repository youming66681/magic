package magical.content;

import arc.struct.Seq;

public class FormulaStack {

    public Seq<Formula> formulas = new Seq<>();

    public void add(Formula f){
        if(f != null) formulas.add(f);
    }

    public Formula get(int i){
        if(formulas == null || formulas.isEmpty()) return null;

        i = Math.max(0, Math.min(i, formulas.size - 1));
        return formulas.get(i);
    }

    public int size(){
        return formulas == null ? 0 : formulas.size;
    }

    public boolean valid(){
        return formulas != null && formulas.size > 0;
    }
}