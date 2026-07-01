package magical.content;

import arc.struct.Seq;

public class FormulaStack {

    public Seq<Formula> formulas = new Seq<>();

    public void add(Formula f){
        formulas.add(f);
    }

    public Formula get(int i){
        if(formulas.isEmpty()) return null;
        return formulas.get(Math.min(i, formulas.size - 1));
    }

    public int size(){
        return formulas.size;
    }
}