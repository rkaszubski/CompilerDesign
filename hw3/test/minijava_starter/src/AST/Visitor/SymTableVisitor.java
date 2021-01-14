package AST.Visitor;

import java.util.HashMap;
import java.util.Iterator;

import AST.*;
import Symtab.*;


public class SymTableVisitor implements Visitor {

	SymbolTable st = new SymbolTable();
	public int errors = 0;
	
	public SymbolTable getSymtab() {
		return st;
	}
	
	public void print()
	{
		st.print(0);
	}
	
	public String getTypeString(Type t) {
		/* TO DO */
		if(t==null){
			return "";
		}
		else if (t instanceof BooleanType){
			return "Boolean";
		}
		else if (t instanceof IntegerType){
			return "int";
		}
		else if (t instanceof IntArrayType){
			return "int[]";
		}
		else if (t instanceof IdentifierType){
			return ((IdentifierType)t).s;
		}
		else{
		return "";
		}
	}
	
	public void report_error(int line, String msg)
	{
		System.out.println(line+": "+msg);
		++errors;
	}	
	
	// MainClass m;
	// ClassDeclList cl;
	public void visit(Program n) {
		if ( n.m != null ) n.m.accept(this);
		for (int i = 0; i < n.cl.size(); i++) {
			n.cl.get(i).accept(this);
		}
	}

	// Identifier i1,i2;
	// Statement s;
	public void visit(MainClass n) {
		/* TO DO */
		String i = n.i1.s;
		String j = n.i2.s;
		ClassSymbol cs = new ClassSymbol(i,"");
		st.addSymbol(cs);
		st=st.enterScope(i,n);
		MethodSymbol ms = new MethodSymbol("main", "void");
		ms.addParameter(new VarSymbol(j, "String[]"));
		st.addSymbol(ms);
		st=st.enterScope("main",n);
		st.addSymbol(new VarSymbol(j, "String[]"));
		n.s.accept(this);
		st = st.exitScope();
		
		st = st.exitScope();
		
	}

	// Identifier i;
	// VarDeclList vl;
	// MethodDeclList ml;
	public void visit(ClassDeclSimple n) {
		/* TO DO */
		String j = n.i.s;
		ClassSymbol cs = new ClassSymbol(j, "");
		st.addSymbol(cs);
		st = st.enterScope(j, n);
		
		for(int i = 0; i<n.vl.size(); i++){
			n.vl.get(i).accept(this);
			VarDecl v = n.vl.get(i);
			Symbol s = st.getVarTable().get(v.i.s);
			if (s instanceof VarSymbol){
				cs.addVariable((VarSymbol)s);
			}
		}
		for(int i = 0; i <n.ml.size(); i++){
			n.ml.get(i).accept(this);
			MethodDecl m = n.ml.get(i);
			Symbol s = st.getMethodTable().get(m.i.s);
			if (s instanceof MethodSymbol){
				cs.addMethod((MethodSymbol)s);
			}
		}
		
		st = st.exitScope();
	}

	// Identifier i;
	// Identifier j;
	// VarDeclList vl;
	// MethodDeclList ml;
	public void visit(ClassDeclExtends n) {
		/* TO DO */
		String k = n.i.s;
		String j = n.j.s;
		ClassSymbol cs = new ClassSymbol(k,j);
		st.addSymbol(cs);
		SymbolTable extend = st.getChild(j);
		if (extend != null){
			if(extend.lookupSymbol(j) != null) cs.extendsClass((ClassSymbol) extend.lookupSymbol(j));
		}
		else report_error(140,"No extended class");
		st = st.enterScope(k,n);
		
		for(int i = 0; i < n.vl.size(); i++){
			VarDecl v = n.vl.get(i);
			v.accept(this);
			
			Symbol s = st.getVarTable().get(v.i.s);
			if(s instanceof VarSymbol){
				cs.addVariable((VarSymbol)s);
			}
		}
		
		for (int i = 0; i<n.ml.size(); i++){
			MethodDecl m = n.ml.get(i);
			m.accept(this);
			
			Symbol s = st.getMethodTable().get(m.i.s);
			if(s instanceof MethodSymbol){
				cs.addMethod((MethodSymbol) s);
			}
		}
		st = st.exitScope();
	}
	

	// Type t;
	// Identifier i;
	public void visit(VarDecl n) {
		/* TO DO */
		String i = n.i.s;
		if(st.lookupSymbol(i) != null){
			report_error(171,"Already Declared");
		}
		String t = getTypeString(n.t);
		st.addSymbol(new VarSymbol(i,t));
	}

	// Type t;
	// Identifier i;
	// FormalList fl;
	// VarDeclList vl;
	// StatementList sl;
	// Exp e;
	public void visit(MethodDecl n) {
		/* TO DO */
		String in = n.i.s;
		if(st.lookupSymbol(in) != null){
			report_error(189,"Already Declared");
		}
		
		String t = getTypeString(n.t);
		
		MethodSymbol ms = new MethodSymbol(in,t);
		
		for(int i = 0; i<n.fl.size(); i++){
			Formal f = n.fl.get(i);
			String ft = getTypeString(f.t);
			String fi = f.i.s;
			if(f!=null) ms.addParameter(new VarSymbol(fi,ft));
		}
		
		st.addSymbol(ms);
		st = st.enterScope(in,n);
		
		for(int i = 0; i<n.fl.size(); i++){
			n.fl.get(i).accept(this);
		}
		for(int i = 0; i<n.sl.size(); i++){
			n.sl.get(i).accept(this);
		}
		for(int i = 0; i<n.vl.size(); i++){
			n.vl.get(i).accept(this);
		}
		st = st.exitScope();
		
	}

	// Type t;
	// Identifier i;
	public void visit(Formal n) {
		/* TO DO */
		String t = getTypeString(n.t);
		String i = n.i.s;
		st.addSymbol(new VarSymbol(i, t));
	}

	// StatementList sl;
	public void visit(Block n) {
	}

	// Exp e;
	// Statement s1,s2;
	public void visit(If n) {
	}

	// Exp e;
	// Statement s;
	public void visit(While n) {
	}

	// Exp e;
	public void visit(Print n) {
	}

	// Identifier i;
	// Exp e;
	public void visit(Assign n) {
	}

	// Identifier i;
	// Exp e1,e2;
	public void visit(ArrayAssign n) {
	}

	// Exp e1,e2;
	public void visit(And n) {
	}

	// Exp e1,e2;
	public void visit(LessThan n) {
	}

	// Exp e1,e2;
	public void visit(Plus n) {
	}

	// Exp e1,e2;
	public void visit(Minus n) {
	}

	// Exp e1,e2;
	public void visit(Times n) {
	}

	// Exp e1,e2;
	public void visit(ArrayLookup n) {
	}

	// Exp e;
	public void visit(ArrayLength n) {
	}

	// Exp e;
	// Identifier i;
	// ExpList el;
	public void visit(Call n) {
	}

	// int i;
	public void visit(IntegerLiteral n) {
	}

	public void visit(True n) {
	}

	public void visit(False n) {
	}

	public void visit(This n) {
	}

	// Exp e;
	public void visit(NewArray n) {
	}

	// Identifier i = new Identifier();
	public void visit(NewObject n) {
	}

	// Exp e;
	public void visit(Not n) {
	}

	// String s;
	public void visit(IdentifierExp n) {
	}

	// String s;
	public void visit(Identifier n) {
	}
	
	// int[] i;
	public void visit(IntArrayType n) {
	}

	// Bool b;
	public void visit(BooleanType n) {
	}

	// Int i;
	public void visit(IntegerType n) {
	}

	// String s;
	public void visit(IdentifierType n) {
	}	

	// Display added for toy example language. Not used in regular MiniJava
	public void visit(Display n) {
	}
}
