public class CellText extends Cell{
    public CellText(String name, String expr, Table table) {
        super(name, expr, table);
    }

    public Double getValue() throws IncorrectExpr{
        throw new IncorrectExpr();
    }

    public String getAppearance() {return this.getExpr();}

    public void update(){
        removeChildren();
        super.update();
    }

}
