import java.util.HashSet;

public class Cell{
    protected final String name;
    protected String expr = "";
    protected Double value = 0.0;
    protected Table table;
    public boolean isCorrectExpr = true;

    public Cell(String name, String expr, Table table){
        this.name = name;
        this.expr = expr;
        this.table = table;
    }

    public String getName(){return this.name;}

    public String getExpr(){return this.expr;}

    public Double getValue() throws IncorrectExpr {
        if (!this.isCorrectExpr) throw new IncorrectExpr();
        return this.value;
    }

    public String getAppearance() {
        if (isCorrectExpr) return this.value.toString();
        return "Error";
    }

    public void update() {for (String parent: table.getParents(name)) table.getCell(parent).update();}

    /*
Обновляет список детей, то есть клеток, от которых зависит эта клетка.
Клетки, от которых теперь зависит и не зависит эта клетка, соответственно изменяют список
своих родителей.
*/
    protected void removeChildren(){
        for(String oldChild: table.getChildren(this.name)){
            table.removeParent(oldChild, name);
        }
        table.setChildren(name, new HashSet<String>());
    }
}