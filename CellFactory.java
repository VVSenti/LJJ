public class CellFactory {
    Table table;
    public CellFactory(Table table) {this.table = table;}
    public Cell createCell(String name, String expr){
        if (expr.isEmpty()) return new CellValue(name, "0.0", this.table);
        if ((expr.charAt(0) == '=')) return new CellExpr(name, expr, this.table);
        try{
            Double.parseDouble(expr);
            return new CellValue(name, expr, this.table);
        }
        catch (NumberFormatException ignore) {
            return new CellText(name, expr, this.table);
        }
    }
}
