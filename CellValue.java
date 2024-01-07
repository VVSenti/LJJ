public class CellValue extends Cell{
    public CellValue(String name, String expr, Table table) {
        super(name, expr, table);
        this.value = Double.parseDouble(expr);
    }
    public void update(){
        removeChildren();
        super.update();
    }

}
