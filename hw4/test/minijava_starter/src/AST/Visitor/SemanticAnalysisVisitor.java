package AST.Visitor;

import AST.*;
import Symtab.*;

public class SemanticAnalysisVisitor implements ObjectVisitor {

	SymbolTable st = null;
	public int errors = 0;

	public void setSymtab(SymbolTable s)
	{
		st = s;
	}

	public SymbolTable getSymtab()
	{
		return st;
	}

	public void report_error(int line, String msg)
	{
		System.out.println(line+": "+msg);
		++errors;
	}
	public Symbol getSym(ASTNode node) {
		String name = null;

		if (node == null) {
			return null;
		}
		else if (node instanceof Identifier) {
			Identifier i = (Identifier) node;
			name = i.s;
		}
		else if (node instanceof IdentifierExp) {
			IdentifierExp i = (IdentifierExp) node;
			name = i.s;
		}
		Symbol s = st.lookupSymbol(name);
		if (s != null) {
			return s;
		}

		return null;
	}

	// Display added for toy example language. Not used in regular MiniJava
	public Object visit(Display n) {
		if (n.e != null){
			n.e.accept(this);
		}

		return null;
	}

	// MainClass m;
	// ClassDeclList cl;
	public Object visit(Program n) {
		if(n.m != null){
			n.m.accept(this);
		}
		for (int i = 0; i < n.cl.size(); i++) {
				n.cl.get(i).accept(this);
		}
		return null;
	}

	// Identifier i1,i2;
	// Statement s;
	public Object visit(MainClass n) {
		Object o = new Object();
		if(n.i1 != null)
			o = n.i1.accept(this);
		else{
			o=null;
		}
		st = st.findScope(n.i1.toString());
		st = st.findScope("main");
		if(n.s != null){
			n.s.accept(this);
		}
		st = st.exitScope();
		st = st.exitScope();

		return o;
	}

	// Identifier i;
	// VarDeclList vl;
	// MethodDeclList ml;
	public Object visit(ClassDeclSimple n) {
		Object o = new Object();
		if(n.i != null){
			o = n.i.accept(this);
		}
		else{
			o=null;
		}

		st=st.findScope(n.i.toString());

		if(n.vl != null){
			for (int i = 0; i < n.vl.size(); i++) {
				if(n.vl.get(i) != null){
					n.vl.get(i).accept(this);
				}
			}
		}
		if(n.ml != null){
			for (int i = 0; i < n.ml.size(); i++) {
				if(n.ml.get(i) != null){
					n.ml.get(i).accept(this);
				}
			}
		}
		st=st.exitScope();
		return o;
	}

	// Identifier i;
	// Identifier j;
	// VarDeclList vl;
	// MethodDeclList ml;
	public Object visit(ClassDeclExtends n) {
		Object o = new Object();
		if(n.i != null){
			o = n.i.accept(this);
		}
		else{
			o=null;
		}
		if (n.j != null) n.j.accept(this);

		st = st.findScope(n.i.toString());
		if(n.vl != null){
			for (int i = 0; i < n.vl.size(); i++) {
				if(n.vl.get(i) != null){
					n.vl.get(i).accept(this);
				}
			}
		}
		if(n.ml != null){
			for (int i = 0; i < n.ml.size(); i++) {
				if(n.ml.get(i) != null){
					n.ml.get(i).accept(this);
				}
			}
		}
		st = st.exitScope();
		return o;
	}

	// Type t;
	// Identifier i;
	public Object visit(VarDecl n) {
		if(n.t != null){
			return n.t.accept(this);
		}
		else{
			return null;
		}

	}

	// Type t;
	// Identifier i;
	// FormalList fl;
	// VarDeclList vl;
	// StatementList sl;
	// Exp e;
	public Object visit(MethodDecl n) {
		Object o = new Object();
		if (n.t != null){
			o = n.t.accept(this);
		}
		else{
			o = null;
		}



		st = st.findScope(n.i.toString());
		if(n.fl != null){
			for (int i = 0; i < n.fl.size(); i++) {
				if(n.fl.get(i) != null){
					n.fl.get(i).accept(this);
				}
			}
		}

		if(n.vl != null){
			for (int i = 0; i < n.vl.size(); i++) {
				if(n.vl.get(i) != null){
					n.vl.get(i).accept(this);
				}
			}
		}
		if(n.sl != null){
			for (int i = 0; i < n.sl.size(); i++) {
				if(n.sl.get(i) != null){
					n.sl.get(i).accept(this);
				}
			}
		}
		Object o2 = new Object();
		if (n.e != null){
			o2 = n.e.accept(this);
		}
		else{
			o2=null;
		}
		if (o2 != null && !o2.equals(o)) {
			report_error(n.line_number, "Mismatched function call: " + n.i.toString() + " in MethodDecl.");
		}
		st = st.exitScope();
		return o;
	}

	// Type t;
	// Identifier i;
	public Object visit(Formal n) {
		if(n.t != null){
			return n.t.accept(this);
		}
		return null;
	}

	public Object visit(IntArrayType n) {
		return "int[]";
	}

	public Object visit(BooleanType n) {
		return "Boolean";
	}

	public Object visit(IntegerType n) { return "int";
	}

	// String s;
	public Object visit(IdentifierType n) {
		return n.s;
	}

	// StatementList sl;
	public Object visit(Block n) {
		if(n.sl != null){
			for (int i = 0; i < n.sl.size(); i++) {
				n.sl.get(i).accept(this);
			}
		}
		return null;
	}

	// Exp e;
	// Statement s1,s2;
	public Object visit(If n) {
		Object o = new Object();
		if (n.e != null){
			o = n.e.accept(this);
		}
		else{
			o = null;
		}
		Object o2 = new Object();
		if(n.s1 != null){
			o2 = n.s1.accept(this);
		}
		else {
			o2 = null;
		}
		Object o3 = new Object();
		if(n.s2 != null){
			o3 = n.s2.accept(this);
		}
		else{
			o3 = null;
		}


		if (o == null || !(o instanceof String) || !((String) o).equals("Boolean")) {
			report_error(n.line_number, "Expecting boolean for if conditional.");
		}
		return null;
	}

	// Exp e;
	// Statement s;
	public Object visit(While n) {
		Object o = new Object();
		if (n.e != null){
			o = n.e.accept(this);
		}
		else{
			o = null;
		}
		Object o2 = new Object();
		if (n.s != null){
			o2 = n.s.accept(this);
		}
		else{
			o2 = null;
		}
		if (o == null || !(o instanceof String) || !((String) o).equals("Boolean")) {
			report_error(n.line_number, "Expecting boolean  for while conditional.");
		}
		return null;
	}

	// Exp e;
	public Object visit(Print n) {
		Object o = new Object();
		if (n.e != null){
			o = n.e.accept(this);
		}
		else {
			o = null;
		}
		return "void";
	}

	// Identifier i;
	// Exp e;
	public Object visit(Assign n) {
		Object o1 = new Object();
		if(n.i != null){
			o1 = n.i.accept(this);
		}
		else{
			o1 = null;
		}

		Object o2 = new Object();
		if(n.e != null){
			o2 = n.e.accept(this);
		}
		else{
			o2 = null;
		}

		if (o1 == null || o2 == null || !o1.equals(o2)) {
			report_error(n.line_number, "Mismatched lhs & rhs in assignment statement.");
		}

		return o1;
	}

	// Identifier i;
	// Exp e1,e2;
	public Object visit(ArrayAssign n) {
		Object o;
		if(n.i != null)
			o = n.i.accept(this);
		else
			o = null;

		Object o1;
		if(n.e1 != null)
			o1 = n.e1.accept(this);
		else
			o1 = null;
		Object o2;
		if(n.e2 != null)
			o2 = n.e2.accept(this);
		else
			o2 = null;

		String t;
		if(o != null && o instanceof String)
			t = (String) o;
		else
			t = null;

		if (t == null || t != "int[]") {
			report_error(n.line_number, "Undefined array type.");
		} else {
			t = "int";
		}

		if (o1 == null || !(o1 instanceof String) || !((String) o1).equals("int")) {
			report_error(n.line_number, "Expecting int for array.");
		}

		if (t == null || o2 == null || !t.equals(o2)) {
			report_error(n.line_number, "Mismatched lhs & rhs in array assignment statement.");
		}

		return o;
	}

	// Exp e1,e2;
	public Object visit(And n) {
		Object o = new Object();
		if(n.e1 != null) {
			o = n.e1.accept(this);
		}
		else {
			o = null;
		}
		Object o2;
		if(n.e1 != null) {
			o2 = n.e2.accept(this);
		}
		else {
			o2 = null;
		}
		String s1;
		if(o != null && o instanceof String) {
			s1 = (String) o;
		}
		else {
			s1 = "";
		}
		String s2;
		if(o2 != null && o2 instanceof String) {
			s2 = (String) o2;
		}
		else {
			s2 = "";
		}

		if (s1 != s2 || s1 != "Boolean") {
			report_error(n.line_number, "Expecting boolean in AND.");
			return null;
		}

		return s1;
	}

	// Exp e1,e2;
	public Object visit(LessThan n) {
		String s1;
		if(n.e1 != null) {
			Object o = new Object();
			o = n.e1.accept(this);
			if(o!=null && (((String) o).equals("int")) && (o instanceof String)){
				s1 = "int";
			}
			else{
				s1 = null;
			}
		}
		else {
			s1 = null;
		}
		String s2;
		if(n.e2 != null) {
			Object o2 = new Object();
			o2 = n.e1.accept(this);
			if(o2!=null && (((String) o2).equals("int")) && (o2 instanceof String)){
				s2 = "int";
			}
			else{
				s2 = null;
			}
		}
		else {
			s2 = null;
		}
		if (s1 != s2 || s1 != "int") {
			report_error(n.line_number, "Expecting int in less than");
			return null;
		}

		return "Boolean";
	}

	// Exp e1,e2;
	public Object visit(Plus n) {
		String s1;
		if(n.e1 != null) {
			Object o = new Object();
			o = n.e1.accept(this);
			if(o!=null && (((String) o).equals("int")) && (o instanceof String)){
				s1 = "int";
			}
			else{
				s1 = null;
			}
		}
		else
			s1 = null;

		String s2;
		if(n.e2 != null){
			Object o2 = new Object();
			o2 = n.e2.accept(this);
			if(o2!=null && (((String) o2).equals("int")) && (o2 instanceof String)){
				s2 = "int";
			}
			else{
				s2 = null;
			}
		}
		else {
			s2 = null;
		}
		if (s1 != s2 || s1 != "int") {
			report_error(n.line_number, "Expecting int in plus.");
			return null;
		}

		return s1;
	}

	// Exp e1,e2;
	public Object visit(Minus n) {
		String s1;
		if(n.e1 != null) {
			Object o = new Object();
			o = n.e1.accept(this);
			if(o!=null && (((String) o).equals("int")) && (o instanceof String)){
				s1 = "int";
			}
			else{
				s1 = null;
			}
		}
		else
			s1 = null;

		String s2;
		if(n.e2 != null){
			Object o2 = new Object();
			o2 = n.e2.accept(this);
			if(o2!=null && (((String) o2).equals("int")) && (o2 instanceof String)){
				s2 = "int";
			}
			else{
				s2 = null;
			}
		}
		else {
			s2 = null;
		}
		if (s1 != s2 || s1 != "int") {
			report_error(n.line_number, "Expecting int in minus.");
			return null;
		}

		return s1;
	}

	// Exp e1,e2;
	public Object visit(Times n) {
		String s1;
		if(n.e1 != null){
			Object o = new Object();
			o = n.e1.accept(this);
			if(o!=null && (((String) o).equals("int")) && (o instanceof String)){
				s1 = "int";
			}
			else{
				s1 = null;
			}
		}
		else {
			s1 = null;
		}
		String s2;
		if(n.e2 != null){
			Object o2 = new Object();
			o2 = n.e2.accept(this);
			if(o2!=null && (((String) o2).equals("int")) && (o2 instanceof String)){
				s2 = "int";
			}
			else{
				s2 = null;
			}
		}
		else{
			s2 = null;
		}

		if (s1 != s2 || s1 != "int") {
			report_error(n.line_number, "Expecting int in multiply.");
			return null;
		}

		return s1;
	}

	// Exp e1,e2;
	public Object visit(ArrayLookup n) {
		Object o1;
		if(n.e1 != null)
			o1 = n.e1.accept(this);
		else
			o1 = null;

		String t;
		if(o1 != null && o1 instanceof String)
			t = (String) o1;
		else
			t = null;

		if (t == null) {
			report_error(n.line_number, "Undefined array type.");
		} else {
			t = "int";
		}


		Object o2;
		if(n.e2 != null)
			o2 = n.e2.accept(this);
		else
			o2 = null;

		if (o2 == null || !(o2 instanceof String) || !((String) o2).equals("int")) {
			report_error(n.line_number, "Expecting int for array index.");
		}

		return t;
	}

	// Exp e;
	public Object visit(ArrayLength n) {
		if (n.e!= null){
			n.e.accept(this);
		}
		return "int";
	}

	// Exp e;
	// Identifier i;
	// ExpList el;
	public Object visit(Call n) {

		String classsym = "";
		if (n.e != null){
			classsym = (String) n.e.accept(this);
		}



		if (classsym != null && classsym != "") {
			Symbol s = st.lookupSymbol(classsym);
			if (s == null || !(s instanceof ClassSymbol)) {
				report_error(n.e.line_number, "Class object \"" + classsym + "\" is undefined.");
				return null;
			}

			ClassSymbol cs = (ClassSymbol) s;
			MethodSymbol ms = cs.getMethod(n.i.s);

			if (ms != null) {
				if (ms.getParameters().size() != n.el.size()) {
					report_error(n.e.line_number, "Function call \"" + n.i.s + "\" expecting "
							+ ms.getParameters().size() + " argument(s), but " + n.el.size() + " provided.");
				}
				else {
					for (int i = 0; i < n.el.size(); i++) {
						String arg = ms.getParameters().get(i).getType();
						String param = (String) n.el.get(i).accept(this);
						Symbol paramSym;

						if (param != null){
							paramSym = st.lookupSymbol(param);
						}
						else{
							paramSym = null;
						}


						if (paramSym != null && paramSym instanceof ClassSymbol) {
							String paramExt = paramSym.getType();
							if (!param.equals(arg) && !paramExt.equals(arg)) {
								report_error(n.e.line_number, "Function call " + n.i.s + " argument " + i + " expecting " + arg + " type.");
							}
						} else if (!param.equals(arg)) {
							report_error(n.e.line_number,
									"Function call " + n.i.s + " argument " + i + " expecting " + arg + " type.");

						}
					}
				}
				return ms.getType();
			}

			report_error(n.line_number, "Function call \"" + n.i.s + "\" is undefined.");
			return null;

		}

		Symbol s = st.lookupSymbol(n.i.s);
		if(s != null && s instanceof MethodSymbol){
			MethodSymbol ms = (MethodSymbol) s;

			if(ms.getParameters().size() != n.el.size()){
				report_error(n.e.line_number, "Function call \""+n.i.s+"\" expecting "+ms.getParameters().size()+" arguments, but only "+n.el.size()+" were provided.");
			}
			else{
				for(int i = 0; i < n.el.size(); i++){
					Object o = new Object();
					o = n.el.get(i).accept(this);

					String arg = ms.getParameters().get(i).getType();
					/*
					String param = (String) n.el.get(i).accept(this);
					Symbol paramSym;

					if (param != null){
						paramSym = st.lookupSymbol(param);
					}
					else{
						paramSym = null;
					}

					 */
					if(!o.equals(arg))
						report_error(n.e.line_number, "Function call \""+n.i.s+"\" argument "+i+" expecting "+arg+" type.");

				}
			}

			return ms.getType();
		}

		report_error(n.line_number, "Function call \""+n.i.s+"\" is undefined.");
		return null;

	}



	// int i;
	public Object visit(IntegerLiteral n) {
		return "int";
	}

	public Object visit(True n) {
		return "Boolean";
	}

	public Object visit(False n) {
		return "Boolean";
	}

	// String s;
	public Object visit(IdentifierExp n) {
		Symbol s = getSym(n);
		if (s == null) {
			report_error(n.line_number, "Symbol " + n.s + " not declared.");
			return null;
		}

		return s.getType();
	}

	public Object visit(This n) {
		/*
		Symbol s = getSym(n);
		if(s == null){
			report_error(n.line_number, "This reference error.");
		}


		report_error(n.line_number, "This reference error.");
		*/
		return null;
	}

	// Exp e;
	public Object visit(NewArray n) {
		Object o = new Object();
		if (n.e != null) {
			o = n.e.accept(this);
		}
		else{
			o = null;
		}
		return "int[]";
	}

	// Identifier i;
	public Object visit(NewObject n) {
		Symbol s = getSym(n.i);
		if (s == null) {
			report_error(n.line_number, "Object not declared.");
		}

		return n.i.s;
	}

	// Exp e;
	public Object visit(Not n) {
		if (n.e != null) {
			return n.e.accept(this);
		}
		else{
			return null;
		}

	}

	// String s;
	public Object visit(Identifier n) {

		Symbol s = getSym(n);
		if (s == null) {
			report_error(n.line_number, "Symbol " + n.s + " not declared.");
			return null;
		}
		return s.getType();
	}
}