import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class Table {
    private HashMap<String, Cell> cells;
    private HashMap<String, HashSet<String>> cellParents;
    private HashMap<String, HashSet<String>> cellChildren;
    public LinkedList<String> cycle = new LinkedList<>();
    private char lastCollomn = 'I';
    private int lastRow = 10;

    CellFactory cellCreator;

    Table(){
        this.cells = new HashMap<>();
        this.cellParents = new HashMap<>();
        this.cellChildren = new HashMap<>();
        this.cellCreator = new CellFactory(this);
    }

    public void setCell(String name, String expr){
        Cell newCell = cellCreator.createCell(name, expr);
        cells.put(name, newCell);
        if (!cellParents.containsKey(name)) cellParents.put(name, new HashSet<String>());
        if (!cellChildren.containsKey(name)) cellChildren.put(name, new HashSet<String>());
        newCell.update();
    }

    public Cell getCell(String name) throws IncorrectExpr{
        if (!checkName(name)) throw new IncorrectExpr();
        if (!this.cells.containsKey(name)) {
            Cell newCell = cellCreator.createCell(name, "");
            cells.put(name, newCell);
            if (!cellParents.containsKey(name)) cellParents.put(name, new HashSet<String>());
            if (!cellChildren.containsKey(name)) cellChildren.put(name, new HashSet<String>());
            return newCell;
        }
        return cells.get(name);
    }

    public boolean checkName(String name){
        if (name.charAt(0) > lastCollomn) return false;
        try {
            int row = Integer.parseInt(name.substring(1));
            if (row > lastRow) return false;
        } catch (NumberFormatException nfe) {return false;}
        return true;
    }

    public void addParent(String cellName, String parentName) throws IncorrectExpr{
        try{getCell(cellName);}
        catch (IncorrectExpr ignored){throw new IncorrectExpr();}
        cellParents.get(cellName).add(parentName);
    }

    public void removeParent(String cellName, String parentName) {cellParents.get(cellName).remove(parentName);}

    public void setChildren(String cellName, HashSet<String> children){cellChildren.put(cellName, children);}

    public HashSet<String> getParents(String name) {return cellParents.get(name);}

    public HashSet<String> getChildren(String name) {return cellChildren.get(name);}

    /*
    Если с таблицей всё в порядке, то ничего не возвращает. Если есть циклические формулы то
    @throws исключение типа CyclicExprs.
     */
    public void checkCycle(String name, LinkedList<String> path) throws CyclicExprs{
        path.add(name);
        if (path.size() > 1 && name.equals(path.getFirst())){
            this.cycle = path;
            throw new CyclicExprs();
        }
        else{
            for (String child: cellChildren.get(name)){
                checkCycle(child, (LinkedList<String>) path.clone());
            }
        }
    }

    public void showTable(){
        for (String cellName: this.cells.keySet()){
            Cell x = this.getCell(cellName);
            System.out.printf("%s   %s   %s\n", x.getName(), x.getExpr(), x.getAppearance());
        }
    }

    public static void main(String[] args) {
        Table table = new Table();
        table.setCell("A1", "=A2+1");
        table.setCell("A3", "name");
        table.setCell("A4", "0.5");
        table.showTable();
        System.out.println();
        table.setCell("A2", "=B2+2");
        table.showTable();
        System.out.println();
        table.setCell("B2", "=A1+3");
        table.showTable();
    }


}
