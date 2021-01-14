import Scanner.*;
import Parser.sym;
import java_cup.runtime.*;
import java.io.*;

public class MiniJava{
	
	public static void main (String[] args){
		
		
		if(args[0].equals("-S")){
			Scanner(args[1]);
		}
		else{
			System.err.println("Invalid arguments.");
			System.exit(1);
		}
	}
	
	public static int Scanner(String src){
		int errorCount = 0;
		try{
			ComplexSymbolFactory symbolFactory = new ComplexSymbolFactory();
			Reader input = new BufferedReader(new FileReader(src));
			scanner s = new scanner (input, symbolFactory);
			
			Symbol token = s.next_token();
			while(token.sym != sym.EOF){
				System.out.print(s.symbolToString(token) + "\n");
				if(token.sym == sym.error){
					errorCount = errorCount + 1;
				}
				token = s.next_token();
			}
				
			System.out.println();
			System.out.print("Finished");
			} catch (Exception e) {
				System.err.println("Compiler Error");
			}
		System.out.println("\nThere were " + errorCount + " errors");
		return errorCount;
		}
		
		
}
		