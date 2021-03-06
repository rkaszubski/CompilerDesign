package AST;

import java.util.List;
import java.util.ArrayList;
import java_cup.runtime.ComplexSymbolFactory.Location;
import AST.Visitor.ObjectVisitor;
public class VarDeclList extends ASTNode {
   private List<VarDecl> list;

   public VarDeclList(Location pos) {
      super(pos);
      list = new ArrayList<VarDecl>();
   }

   public void add(VarDecl n) {
      list.add(n);
   }

   public VarDecl get(int i)  { 
      return list.get(i); 
   }

   public int size() { 
      return list.size(); 
   }
}
