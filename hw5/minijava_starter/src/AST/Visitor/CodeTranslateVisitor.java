package AST.Visitor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import AST.*;
import Symtab.*;

public class CodeTranslateVisitor implements Visitor {

	SymbolTable st = null;
	public int errors = 0;
	int tabs = 0;
	int stack_pos = 0;
	int arg_pos = 0;
	int label_num = 0;
	final String regs[] = {"%rdi", "%rsi", "%rdx", "%rcx", "%r8", "%r9"};
	private HashMap<VarSymbol, Integer> symLocations = new HashMap<VarSymbol, Integer>();

	public void setSymtab(SymbolTable s) { st = s; }
	 
	public SymbolTable getSymtab() { return st; }
	
    public Integer getSymLoc(VarSymbol vs) {
        return symLocations.get(vs);
    }

    public void setSymLoc(VarSymbol vs, int loc) {
        symLocations.put(vs, new Integer(loc));
    }
	
	public String getLabel(String class_name, String call_name) {
		String label = !class_name.isEmpty() ? class_name+"$"+call_name : call_name;
		String os = System.getProperty("os.name");
		if ( os.contains("Windows") || os.contains("OS X") ) {
			return "_"+label;
		}
		return label;
	}

	
	public int getObjectSize(String t, boolean if_class) {
		if ( t == null || t == "" )
			return 0;
		else if ( t.equals("int") ) {
			return 8;
		}
		else if ( t.equals("int[]") ) {
			return 8;
		}
		else if ( t.equals("Boolean") || t.equals("boolean") ) {
			return 8;
		}
		else if ( if_class ) {
			return 8;
		}
		
		int size = 8;
		
		ClassSymbol sym = (ClassSymbol)st.lookupSymbol(t, "ClassSymbol");
		if ( sym != null && sym.getVariables() != null ) {
			List<VarSymbol> vl = sym.getVariables();
			for ( int i = 0; i < vl.size(); i++ ) {
				size += getObjectSize(vl.get(i).getType(), true);
			}
		}
		return size;
	}
	public void incTab() {
		tabs = tabs + 1;
	}

	public void decTab() {
		if (tabs <= 0)
			tabs = 0;
		else{
			tabs = tabs -1;
		}
	}

	public String setTabsOut() {
		String spaces = "";
		for (int i = 0; i < tabs; i++)
			spaces = spaces + "        ";
		return spaces;
	}
	
	
	public void printAssembly(String instr, String[] args) {
		System.out.print(setTabsOut());
		System.out.print(instr);
		int len = 8 - instr.length();
		if ( len > 0 ) {
			for (int t = 0; t < len; t++ )
				System.out.print(" ");
		}
		for ( int a = 0; a < args.length; a++ ) {
			if ( a > 0 ) System.out.print(", ");
			System.out.print(args[a]);
		}
		System.out.print("\n");
	}

	
	public void report_error(int line, String msg) {
		System.out.println(line + ": " + msg);
		++errors;
	}
	
	public String getExp(ASTNode n, boolean dst) {
		if ( n == null )return "";
		else if ( n instanceof IntegerLiteral ) {
			IntegerLiteral i = (IntegerLiteral)n;
			i.accept(this);
			return "%rax"; 
		}
		else if ( n instanceof False ) {
			False i = (False)n;
			i.accept(this);
			return "%rax"; 
		}
		else if ( n instanceof True ) {
			True i = (True)n;
			i.accept(this);
			return "%rax"; 
		}
		else if ( n instanceof IdentifierExp ) {
			Symbol s = st.getSymbol(((IdentifierExp)n).s);
			if ( s != null && s instanceof VarSymbol ) {
				String op = getSymLoc((VarSymbol)s) + "(%rbp)";
				if ( dst ) {
					return op;
				}
				printAssembly("movq", new String[]{op, "%rax"});
				return "%rax";
			}
			s = st.lookupSymbol(((IdentifierExp)n).s, "VarSymbol");
			if ( s != null && s instanceof VarSymbol ) {
				printAssembly("movq", new String[]{"-8(%rbp)", "%rax"});
				String op = getSymLoc((VarSymbol)s) + "(%rax)";
				if ( dst ){
					return op;
				}
				printAssembly("movq", new String[]{op, "%rax"});				
				return "%rax";
			}	
			ASTNode p = n;
			p = p.getParent();
			if ( p != null && p instanceof ClassDeclExtends ) {
				ClassDeclExtends c = (ClassDeclExtends)p;
				ClassSymbol cs = (ClassSymbol)st.lookupSymbol(c.j.s);			
				if ( cs != null ) {
					VarSymbol vs = cs.getVariable(((IdentifierExp)n).s);
					if ( vs != null ) {
						printAssembly("movq", new String[]{"-8(%rbp)", "%rax"});
						String op = getSymLoc(vs) + "(%rax)";
						if ( dst ){
							return op;
						}
						printAssembly("movq", new String[]{op, "%rax"});				
						return "%rax";
					}
				}
			}			
		}
		else if ( n instanceof Identifier ) {
			Identifier i = (Identifier)n;
			Symbol s = st.getSymbol(i.s);
			if ( s != null && s instanceof VarSymbol ) {
				String op = getSymLoc((VarSymbol)s) + "(%rbp)";
				if ( dst ) return op;
				printAssembly("movq", new String[]{op, "%rax"});
				return "%rax";
			}
			s = st.lookupSymbol(i.s, "VarSymbol");
			if ( s != null && s instanceof VarSymbol ) {
				printAssembly("movq", new String[]{"-8(%rbp)", "%rax"});
				String op = getSymLoc((VarSymbol)s) + "(%rax)";
				if ( dst ) return op;
				printAssembly("movq", new String[]{op, "%rax"});				
				return "%rax";
			}
			ASTNode p = n;
			p = p.getParent();
			
			if ( p != null && p instanceof ClassDeclExtends ) {
				ClassDeclExtends c = (ClassDeclExtends)p;
				ClassSymbol cs = (ClassSymbol)st.lookupSymbol(c.j.s);			
				if ( cs != null ) {
					VarSymbol vs = cs.getVariable(i.s);
					if ( vs != null ) {
						printAssembly("movq", new String[]{"-8(%rbp)", "%rax"});
						String op = getSymLoc(vs) + "(%rax)";
						if ( dst ) return op;
						printAssembly("movq", new String[]{op, "%rax"});				
						return "%rax";
					}
				}
			}						
		}
		else if ( n instanceof ArrayLookup ) {
			ArrayLookup e = (ArrayLookup)n;
			e.accept(this);
			return "%rax"; 
		}
		else if ( n instanceof Exp ) {
			Exp e = (Exp)n;
			e.accept(this);
			return "%rax"; 
		}
		else{
			return "";
		}
		return "";
	}
		
	// Display added for toy example language. Not used in regular MiniJava
	public void visit(Display n) {
		n.e.accept(this);
	}

	// MainClass m;
	// ClassDeclList cl;
	public void visit(Program n) {	
		
		if ( n.cl != null ) {
		    System.out.println(".data");
			for (int j = 0; j < n.cl.size(); j++) {
				ClassDecl c = n.cl.get(j);
				if ( c instanceof ClassDeclSimple ) {
					ClassDeclSimple cds = (ClassDeclSimple)c;
				    System.out.println( getLabel("",cds.i.s) + "$:");
				    incTab();
				    System.out.print(setTabsOut()); 
					System.out.println(".quad 0");
				    ClassSymbol cs = (ClassSymbol)getSymtab().lookupSymbol(cds.i.s, "ClassSymbol");
				    if ( cs != null ) {
					    for (int i = 0; i < cs.getMethods().size(); i++) {
					    	System.out.print(setTabsOut()); 
							System.out.println(".quad" + " " + getLabel(cds.i.s, cs.getMethods().get(i).getName()));
					    }
				    }

				    decTab();					
				}
				else if ( c instanceof ClassDeclExtends ) {
					ClassDeclExtends cde = (ClassDeclExtends)c;
				    System.out.println( getLabel("", cde.i.s) + "$:");
				    incTab();
				    System.out.print(setTabsOut()); 
					System.out.println(".quad " + getLabel(cde.j.s, ""));
				    ClassSymbol cs = (ClassSymbol)getSymtab().lookupSymbol(cde.i.s, "ClassSymbol");
				    
				    decTab();					
				}
			}
		}
		
		System.out.println("\n.text");
		System.out.println(".globl " + getLabel("", "asm_main")); 		
		n.m.accept(this);
		for (int i = 0; i < n.cl.size(); i++) {
			n.cl.get(i).accept(this);
		}
	}

	// Identifier i1,i2;
	// Statement s;
	public void visit(MainClass n) {
		n.i1.accept(this);
		st = st.findScope(n.i1.toString());
		n.i2.accept(this);
		st = st.findScope("main");
		stack_pos = 0;
		arg_pos = 0;
		System.out.println("");
		System.out.println( getLabel("", "asm_main") + ":" );
		incTab();
		printAssembly("pushq", new String[]{"%rbp"});
		printAssembly("movq", new String[]{"%rsp", "%rbp"});
		n.s.accept(this);
		System.out.print(setTabsOut()); 
		System.out.println("leave");
		System.out.print(setTabsOut()); 
		System.out.println("ret");
		decTab();
		st = st.exitScope();
		st = st.exitScope();
	}
	
	// Identifier i;
	// VarDeclList vl;
	// MethodDeclList ml;
	public void visit(ClassDeclSimple n) {
		n.i.accept(this);
		st = st.findScope(n.i.toString());		
		stack_pos = 0;
		arg_pos = 0;
		for (int i = 0; i < n.vl.size(); i++) {
			n.vl.get(i).accept(this);
		}
		for (int i = 0; i < n.ml.size(); i++) {
			n.ml.get(i).accept(this);
		}
		st = st.exitScope();
	}

	// Identifier i;
	// Identifier j;
	// VarDeclList vl;
	// MethodDeclList ml;
	public void visit(ClassDeclExtends n) {
		n.i.accept(this);
		n.j.accept(this);
		st = st.findScope(n.i.toString());		
		stack_pos = 0;
		arg_pos = 0;
		for (int i = 0; i < n.vl.size(); i++) {
			n.vl.get(i).accept(this);
		}
		for (int i = 0; i < n.ml.size(); i++) {
			n.ml.get(i).accept(this);
		}
		st = st.exitScope();
	}
	
	// Type t;
	// Identifier i;
	public void visit(VarDecl n) {
		if ( n.i == null ) return;
		
		Symbol sym = st.getVarTable().get(n.i.s);
        if ( sym != null && sym instanceof VarSymbol ) {
        	VarSymbol vs = (VarSymbol)sym;
        	stack_pos -= 8;
        	setSymLoc(vs, stack_pos);
		}					
	}

	// Type t;
	// Identifier i;
	// FormalList fl;
	// VarDeclList vl;
	// StatementList sl;
	// Exp e;
	public void visit(MethodDecl n) {

		ASTNode p = st.getScope();
		st = st.findScope(n.i.toString());	
		stack_pos = 0;
		arg_pos = 0;
		
		String func = n.i.s;
		String classname = "";
		
		if ( p != null && p instanceof ClassDeclSimple ) {
			ClassDeclSimple c = (ClassDeclSimple)p;
			classname = c.i.s;
		}
		else if ( p != null && p instanceof ClassDeclExtends ) {
			ClassDeclExtends c = (ClassDeclExtends)p;
			classname = c.i.s;
		}
		System.out.println( getLabel(classname, func) + ":");
		incTab();
		printAssembly("pushq", new String[] {"%rbp"});
		printAssembly("movq", new String[] {"%rsp", "%rbp"});
		
		int stack_size = 8 * (n.fl.size() + n.vl.size() + 1);
		if ( stack_size > 0 ) {
			stack_size += (stack_size % 16); 
			printAssembly("subq", new String[] {"$"+Integer.toString(stack_size), "%rsp"});
		}
    	stack_pos -= 8;
    	String stack_loc = Integer.toString(stack_pos) + "(%rbp)";
		printAssembly("movq", new String[] {regs[arg_pos++], stack_loc});
		for (int i = 0; i < n.fl.size() && i < 5; i++) {
			n.fl.get(i).accept(this);
		}
		for (int i = 0; i < n.vl.size(); i++) {
			n.vl.get(i).accept(this);
		}
		for (int i = 0; i < n.sl.size(); i++) {
			n.sl.get(i).accept(this);
		}
		if ( n.e != null ) {
			String ret = getExp(n.e, false);
			if ( ret != "%rax" ) {
				printAssembly("movq", new String[] {ret, "%rax"});
			}
		}
		st = st.exitScope();
		System.out.print(setTabsOut()); 
		System.out.println("leave");
		System.out.print(setTabsOut()); 
		System.out.println("ret");
		decTab();
	}

	// Type t;
	// Identifier i;
	public void visit(Formal n) {
		if ( n.i == null ) return;
		
		Symbol sym = st.getVarTable().get(n.i.s);
        if ( sym != null && sym instanceof VarSymbol ) {
        	VarSymbol vs = (VarSymbol)sym;
        	stack_pos -= 8;
        	setSymLoc(vs, stack_pos);
        	String stack_loc = Integer.toString(stack_pos) + "(%rbp)";
        	printAssembly("movq", new String[] {regs[arg_pos++], stack_loc});
		}					
	}

	public void visit(IntArrayType n) {
	}

	public void visit(BooleanType n) {
	}

	public void visit(IntegerType n) {
	}

	// String s;
	public void visit(IdentifierType n) {
	}

	// StatementList sl;
	public void visit(Block n) {
		for (int i = 0; i < n.sl.size(); i++) {
			n.sl.get(i).accept(this);
		}
	}

	// Exp e;
	// Statement s1,s2;
	public void visit(If n) {

		String l = getLabel("","L"+label_num++);
		String cond = getExp(n.e, false);
		printAssembly("cmp", new String[] {"$0", cond});
		printAssembly("je", new String[] {l});
		n.s1.accept(this);
		if ( n.s2 != null ) {
			String l2 = getLabel("","L"+label_num++);
			printAssembly("jmp", new String[] {l2});
			System.out.println(l+":");
			n.s2.accept(this);
			l = l2;
		}
		System.out.println(l+":");
	}

	// Exp e;
	// Statement s;
	public void visit(While n) {
		String l1 = getLabel("","L"+label_num++);
		String l2 = getLabel("","L"+label_num++);
		System.out.println(l1+":");
		printAssembly("cmp", new String[] {"$0", getExp(n.e, false)});
		printAssembly("je", new String[] {l2});
		
		n.s.accept(this);
		printAssembly("jmp", new String[] {l1});
		System.out.println(l2+":");		
	}

	// Exp e;
	public void visit(Print n) {
		n.e.accept(this);
		printAssembly("movq", new String[]{"%rax", "%rdi"} );
		printAssembly("call", new String[]{ getLabel("", "put") } );
	}

	// Identifier i;
	// Exp e;
	public void visit(Assign n) {
		String e = getExp(n.e, false);
		printAssembly("pushq", new String[]{e} );		
		String i = getExp(n.i, true);
		printAssembly("popq", new String[]{"%r10"} );
		printAssembly("movq", new String[] {"%r10", i});
	}

	// Identifier i;
	// Exp e1,e2;
	public void visit(ArrayAssign n) {
        printAssembly("pushq", new String[] {getExp(n.e2, false)});
		String arr = getExp(n.i, true);
        printAssembly("pushq", new String[] {arr});
		String s = getExp(n.e1, false);
        printAssembly("popq", new String[] {"%r10"});      
        printAssembly("popq", new String[] {"%rdx"});      
        printAssembly("movq", new String[] {"%rdx", "16(%r10,"+s+",8)"});
	}

	// Exp e1,e2;
	public void visit(And n) {
		
		getExp(n.e1, false);
		getExp(n.e2, false);
		printAssembly("pushq", new String[]{"%rax"});
		printAssembly("popq", new String[]{"%r10"});
		printAssembly("andq", new String[]{"%r10", "%rax"});
	}

	// Exp e1,e2;
	public void visit(LessThan n) {
		getExp(n.e1, false);
		printAssembly("pushq", new String[]{"%rax"});
		//
		getExp(n.e2, false);
		printAssembly("popq", new String[]{"%r10"});
		printAssembly("cmpq", new String[]{"%r10", "%rax"});
		printAssembly("setg", new String[]{"%al"});
		printAssembly("movzbq", new String[]{"%al", "%rax"});
	}

	// Exp e1,e2;
	public void visit(Plus n) {
		getExp(n.e1, false);
		printAssembly("pushq", new String[]{"%rax"});
		//n.e1.accept(this);
		//printAssembly("movq", new String[]{"%rax"});
		getExp(n.e2, false);
		printAssembly("popq", new String[]{"%r10"});
		

		printAssembly("addq", new String[]{"%r10", "%rax"});
		
		
	}

	// Exp e1,e2;
	public void visit(Minus n) {
		getExp(n.e2, false);
		printAssembly("pushq", new String[]{"%rax"});
		
		getExp(n.e1, false);
		
		printAssembly("popq", new String[]{"%r10"});
		
		printAssembly("subq", new String[]{"%r10", "%rax"});
	}

	// Exp e1,e2;
	public void visit(Times n) {
		getExp(n.e1, false);
		//n.e1.accept(this);
		//printAssembly("movq", new String[]{"%rax"});
		printAssembly("pushq", new String[]{"%rax"});
		
		getExp(n.e2, false);
		printAssembly("popq", new String[]{"%r10"});
		

		printAssembly("imulq", new String[]{"%r10", "%rax"});
	}

	// Exp e1,e2;
	public void visit(ArrayLookup n) {
        printAssembly("pushq", new String[] {getExp(n.e1, false)});
		String s = getExp(n.e2, false);
        printAssembly("popq", new String[] {"%r10"});      
        printAssembly("movq", new String[] {"16(%r10,"+s+",8)", "%rax"});
	}

	// Exp e;
	public void visit(ArrayLength n) {
		String arr = getExp(n.e, false);
        printAssembly("movq", new String[] {"0("+arr+")", "%rax"});        
	}

	// Exp e;
	// Identifier i;
	// ExpList el;
	public void visit(Call n) {
		String func = n.i.s;
		String class_name = "";
		int offset = -1;
		printAssembly("pushq", new String[]{getExp(n.e, false)});
		
		for (int i = 0; i < n.el.size() && i < 5; i++) printAssembly("pushq", new String[]{getExp(n.el.get(i), false)});	
		
		
		if ( n.e != null ) {
			if ( n.e instanceof Call ) {
				Call c = (Call)n.e;
				MethodSymbol msym = (MethodSymbol)st.lookupSymbol(c.i.s, "MethodSymbol");	
				if ( msym != null ) {
					class_name = msym.getType();
				}
			}
			else if ( n.e instanceof This ) {
				This t = (This)n.e;
				ASTNode p = t.getParent();
				while ( p != null && !(p instanceof ClassDecl) ) p = p.getParent();
				if ( p != null && p instanceof ClassDeclSimple ) {
					ClassDeclSimple c = (ClassDeclSimple)p;
					ClassSymbol csym = (ClassSymbol)st.lookupSymbol(c.i.s, "ClassSymbol");	
					if ( csym != null && csym.getMethod(func) != null ) {
						class_name = csym.getName();
					}
				}
				else if ( p != null && p instanceof ClassDeclExtends ) {
					ClassDeclExtends c = (ClassDeclExtends)p;
					MethodSymbol ms = null;
					ClassSymbol csym = (ClassSymbol)st.lookupSymbol(c.i.s, "ClassSymbol");		
					if ( csym != null ) {
						ms = csym.getMethod(func);						
						class_name = csym.getName();
					}

					ClassSymbol csym_ext = (ClassSymbol)st.lookupSymbol(c.j.s, "ClassSymbol");					
					if ( csym_ext != null ) {
						MethodSymbol mse = csym_ext.getMethod(func);
						if ( class_name == null || ms == mse ) {
							class_name = csym_ext.getName();
						}
					}
				}
			}
			else if ( n.e instanceof NewObject ) {
				NewObject o = (NewObject)n.e;
				class_name = o.i.s;
			}
		}
		
		for (int i = n.el.size(); i >= 0; i--) {
			printAssembly("popq", new String[]{regs[i]});
		}
				
		if ( class_name != null ) {
			ClassSymbol cs = (ClassSymbol)st.lookupSymbol(class_name, "ClassSymbol");
			MethodSymbol ms;
			if(cs != null){
				ms = cs.getMethod(func);
			}
			else{
				ms = null;
			}
			if (ms != null){
				offset = 8 * cs.getMethods().indexOf(ms) + 8;
			}
			else{
				offset= -1;
			}
		}

		if ( offset != -1 ) {
			printAssembly("movq", new String[]{ "0(%rdi)", "%rax" } );
			printAssembly("call", new String[]{ "*"+offset+"(%rax)" } );
		}
		else {
			printAssembly("call", new String[]{ getLabel(class_name, func) } );
			visit(n);
		}
	}

	// int i;
	public void visit(IntegerLiteral n) {
		String intl = "$" + Integer.toString(n.i);
		printAssembly("movq", new String[]{intl, "%rax"});		
	}

	public void visit(True n) {
		printAssembly("movq", new String[]{"$1", "%rax"});
	}

	public void visit(False n) {
		printAssembly("movq", new String[]{"$0", "%rax"});
	}

	// String s;
	public void visit(IdentifierExp n) {
	}

	public void visit(This n) {
        printAssembly("movq", new String[] {"-8(%rbp)", "%rax"});		
	}

	// Exp e;
	public void visit(NewArray n) {
		String e = getExp(n.e, false);
        printAssembly("pushq", new String[] {e});
        printAssembly("imulq", new String[] {"$8", e});
        printAssembly("addq", new String[] {"$16", e});
        printAssembly("movq", new String[] {e, "%rdi"});
        printAssembly("call", new String[] { getLabel("", "mjcalloc")});
        printAssembly("popq", new String[] {"0(%rax)"});      
        printAssembly("movq", new String[] {"$16", "8(%rax)"});
	}

	// Identifier i;
	public void visit(NewObject n) {
        String s = n.i.s;
        int objectSize = getObjectSize(s, false);
        objectSize += (objectSize % 16); 
		
		
        printAssembly("movq", new String[] {"$"+objectSize, "%rdi"});
        printAssembly("call", new String[] {getLabel("", "mjcalloc")});
        printAssembly("movabs", new String[] {"$"+getLabel(n.i.s,""), "%rdx"});
        printAssembly("movq", new String[] {"%rdx", "0(%rax)"});
	}

	// Exp e;
	public void visit(Not n) {
		n.e.accept(this);
		printAssembly("cmpq", new String[]{"$0", "%rax"});
		printAssembly("sete", new String[]{"%al"});
		printAssembly("movzbq", new String[]{"%al", "%rax"});
	}

	// String s;
	public void visit(Identifier n) {
	}
}