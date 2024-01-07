import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class CellExpr extends Cell{
    private String postfixExpr = "";

    public CellExpr(String name, String expr, Table table) {
        super(name, expr.toUpperCase().trim(), table);
    }

    public void update(){
        try {
            if (this.postfixExpr.isEmpty()){
                this.postfixExpr = ExprCalcFuncs.infixToPostfix(this.expr.substring(1));
            }
            // Это место надо оптимизировать
            this.updateChildrenAndParentsSet();
            table.checkCycle(this.name, new LinkedList<String>());
            HashMap<String, Double> cellDict = this.getChildrenDict();
            this.value = ExprCalcFuncs.calcPostfixExpr(this.postfixExpr, cellDict);
            this.isCorrectExpr = true;
            super.update();
        }
        catch (IncorrectExpr ignored) {
            this.isCorrectExpr = false;
            super.update();
        }
        catch (CyclicExprs ignored){
            System.out.println("Cyclic expressions!");
            System.out.println(table.cycle.toString());
        }
    }

    private HashMap<String, Double> getChildrenDict(){
        HashMap<String, Double> dict = new HashMap<>();
        for(String term: this.postfixExpr.split(" ")){
            if (Character.isAlphabetic(term.charAt(0))){
                dict.put(term, table.getCell(term).getValue());
            }
        }
        return  dict;
    };

    /*
    Обновляет список детей, то есть клеток, от которых зависит эта клетка.
    Клетки, от которых теперь зависит и не зависит эта клетка, соответственно изменяют список
    своих родителей.
    */
    private void updateChildrenAndParentsSet(){
        HashSet<String> newChildren = new HashSet<>();
        for(String term: this.postfixExpr.split(" ")){
            if (Character.isAlphabetic(term.charAt(0))){
                newChildren.add(term);
            }
        }
        HashSet<String> oldChildren = table.getChildren(this.name);
        for(String newChild: newChildren){
            if (!oldChildren.contains(newChild)){
                try {table.addParent(newChild, name);}
                catch (IncorrectExpr ignored){ throw new IncorrectExpr();}
            }
        }
        for(String oldChild: oldChildren){
            if (!newChildren.contains(oldChild)){
                table.removeParent(oldChild, name);
            }
        }
        table.setChildren(name, newChildren);
    }

}
