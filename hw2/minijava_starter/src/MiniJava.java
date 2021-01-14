import Scanner.*;
import Parser.sym;
import Parser.*;
import java_cup.runtime.Symbol;
import java_cup.runtime.ComplexSymbolFactory;
import java.io.*;
import AST.*;
import AST.Program;
import AST.Visitor.PrettyPrintVisitor;
public class MiniJava {
	
    public static void main(String [] args) 
    {
    	int status = 0;
    	
    	if ( args.length == 2 && args[0].equals("-S") )
    	{
        	// run scanner on the source file
        	status = scanner( args[1] );
    	}
		else if (args.length == 2 && args[0].equals("-P")){
			// run parser
			status = parser(args[1]);
		}
    	else
    	{
    		System.err.println("Invalid program arguments.\nUse: java Main -S <source_file>");
    		status = 1;
    	}
		System.exit(status);
   }
   
    public static int parser(String source_file)
    {
    	int errors = 0;
    	
        try {
	        // create a scanner on the input file
	        ComplexSymbolFactory sf = new ComplexSymbolFactory();
	        Reader in = new BufferedReader(new FileReader(source_file));
	        scanner s = new scanner(in, sf);
			parser p = new parser(s, sf);
			
	        Symbol root;
			root = p.parse();
	        Program program = (Program)root.value;
			program.accept(new PrettyPrintVisitor());
			
	        System.out.println("\nParsing completed"); 
	        System.out.println(errors + " errors were found.");
        
        } catch (Exception e) {
            // yuck: some kind of error in the compiler implementation
            // that we're not expecting (a bug!)
            System.err.println("Unexpected internal compiler error: " + 
                        e.toString());
            // print out a stack dump
            e.printStackTrace();
        }
        
        return errors == 0 ? 0 : 1;    	
    }
   
   
    
    public static int scanner(String source_file)
    {
    	int errors = 0;
    	
        try {
	        // create a scanner on the input file
	        ComplexSymbolFactory sf = new ComplexSymbolFactory();
	        Reader in = new BufferedReader(new FileReader(source_file));
	        scanner s = new scanner(in, sf);
	        Symbol t = s.next_token();
	        while (t.sym != sym.EOF){ 
	            if ( t.sym == sym.error ) ++errors;
	        	
	            // print each token that we scanned
                    System.out.print(s.symbolToString(t) + "\n");
	            t = s.next_token();
	        }
	        
	        System.out.println("\nLexical analysis completed"); 
	        System.out.println(errors + " errors were found.");
        
        } catch (Exception e) {
            // yuck: some kind of error in the compiler implementation
            // that we're not expecting (a bug!)
            System.err.println("Unexpected internal compiler error: " + 
                        e.toString());
            // print out a stack dump
            e.printStackTrace();
        }
        
        return errors == 0 ? 0 : 1;    	
    }
}


