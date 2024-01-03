import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class Table {
    HashMap<String, Cell> cells;
    LinkedList<String> cycle = new LinkedList<>();

    Table(){this.cells = new HashMap<>();}

    public void addCell(String name, String expr){cells.put(name, new Cell(name, expr));}

    public void setCellExpr(String name, String newExpr){
        if (!this.cells.containsKey(name)) {this.addCell(name, "");}
        this.getCell(name).setExpr(newExpr);
    }

    public Cell getCell(String name){
        if (!this.cells.containsKey(name)) {this.addCell(name, "");}
        return this.cells.get(name);
    }

    public void showTable(){
        for (String cellName: this.cells.keySet()){
            Cell x = this.getCell(cellName);
            System.out.printf("%s   %s   %.3f\n", x.getName(), x.getExpr(), x.getValue());
        }
    }

    public void checkCycle(String name, LinkedList<String> path) throws CyclicExprs{
        path.add(name);
        if (path.size() > 1 && name.equals(path.getFirst())){
            this.cycle = path;
            throw new CyclicExprs();
        }
        else{
            for (String child: this.getCell(name).getChildren()){
                checkCycle(child, (LinkedList<String>) path.clone());
            }
        }
    }

    public static void main(String[] args) {
        Table table = new Table();
        table.setCellExpr("A1", "=A2+1");
        table.setCellExpr("A3", "name");
        table.setCellExpr("A4", "0.5");
        table.showTable();
        System.out.println();
        table.setCellExpr("A2", "=B2+2");
        table.showTable();
        System.out.println();
        table.setCellExpr("B2", "=A1+3");
        table.showTable();
    }

    public class Cell{
        private final String name;
        private String expr = "";
        private String postfixExpr = "";
        private Double value = 0.0;
        private HashSet<String> parents = new HashSet<>();
        private HashSet<String> children = new HashSet<>();
        public boolean isCorrectExpr = true;
        public CellType type = CellType.EXPR;

        public enum CellType{TEXT, EXPR;}

        public Cell(String name, String expr){
            this.name = name;
            if (!expr.isEmpty()) {this.setExpr(expr);}
        }

        public String getName(){return this.name;}

        public String getExpr(){return this.expr;}

        public Double getValue() throws IncorrectExpr {
            if (this.type == CellType.TEXT) throw new IncorrectExpr();
            else return this.value;
        }

        public HashSet<String> getChildren() {return this.children;}

        /*
            Проверяет нормализованное выражение на валидность.
            В частности, проверяет входят ли переменные в область [A1,I10].
            Изменяет значение атрибута correctExpr.
        */
        public void checkExpr(){
            this.isCorrectExpr = false;
            for (String term: this.postfixExpr.split(" ")){
                if (Character.isDigit(term.charAt(0))){
                    try {Double.parseDouble(term);}
                    catch (NumberFormatException nfe) {return;}
                }
                else if (Character.isAlphabetic(term.charAt(0))){
                    if (term.charAt(0) > 73) return;
                    try {
                        int row = Integer.parseInt(term.substring(1));
                        if (row > 10) return;
                    } catch (NumberFormatException nfe) {return;}

                }
                else if (!ExprCalcFuncs.operations.contains(term)) {return;}
            }
            this.isCorrectExpr = true;
        }

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
            for(String newChild: newChildren){
                if (!this.children.contains(newChild)){
                    getCell(newChild).addParent(this.name);
                }
            }
            for(String oldChild: this.children){
                if (!newChildren.contains(oldChild)){
                    getCell(oldChild).removeParent(this.name);
                }
            }
            this.children = newChildren;
        }

        public void setExpr(String newExpr){
            if ((newExpr.charAt(0) == '=')) setExprInExprCell(newExpr);
            else {
                try{
                    Double value = Double.parseDouble(newExpr);
                    this.expr = newExpr;
                    setExprInNumberCell(value);
                }
                catch (NumberFormatException ignore) {
                    setExprInTextCell(newExpr);
                }
            }
        }

        private void setExprInNumberCell(Double value){
            this.value = value;
            this.type = CellType.EXPR;
            this.postfixExpr = "";
            removeChildren();
        }

        private void setExprInTextCell(String newExpr){
            this.expr = newExpr;
            this.type = CellType.TEXT;
            this.postfixExpr = "";
            removeChildren();
            update();
        }

        private void setExprInExprCell(String newExpr) {
            this.type = CellType.EXPR;
            this.expr = newExpr.toUpperCase();
            this.postfixExpr = ExprCalcFuncs.infixToPostfix(this.expr.substring(1));
            this.checkExpr();
            if (this.isCorrectExpr) {
                try {
                    this.updateChildrenAndParentsSet();
                    checkCycle(this.name, new LinkedList<String>());
                    this.update();
                }
                catch (IncorrectExpr ignored) {this.isCorrectExpr = false;}
                catch (CyclicExprs ignored) {
                    System.out.println("Cyclic expressions!");
                    System.out.println(cycle.toString());
                }
            }
        }

        public void update(){
            if (this.type == CellType.EXPR){
                try {this.calcValue();}
                catch (IncorrectExpr ignore) {this.isCorrectExpr = false;}
            }
            for (String name: parents) getCell(name).update();
        }

        /*
        В поле value помещяется значение, вычисленное из выражения в postfixExpr.
         */
        private void calcValue() throws IncorrectExpr{
            HashMap<String, Double> dict = new HashMap<>();
            for(String term: this.postfixExpr.split(" ")){
                if (Character.isAlphabetic(term.charAt(0))){
                    dict.put(term, getCell(term).getValue());
                }
            }
            this.value = ExprCalcFuncs.calcPostfixExpr(this.postfixExpr, dict);
        }

        private void removeChildren(){
            for (String child: this.children){
                getCell(child).removeParent(this.name);
            }
            this.children = new HashSet<>();
        }

        /*
        Добавляет наблюдателя, то есть имя клетки, которая зависит от этой клетки.
         */
        private void addParent(String parent) {this.parents.add(parent);}

        /*
        Удаляет наблюдателя, то есть имя клетки, которая зависит от этой клетки.
         */
        private void removeParent(String parent) {this.parents.remove(parent);}
    }
}
