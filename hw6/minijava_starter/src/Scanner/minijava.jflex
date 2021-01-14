/**
 * JFlex specification for lexical analysis of a simple demo language.
 * Change this into the scanner for your implementation of MiniJava.
 *
 */


package Scanner;

import java_cup.runtime.Symbol;
import java_cup.runtime.ComplexSymbolFactory;
import java_cup.runtime.ComplexSymbolFactory.ComplexSymbol;
import java_cup.runtime.ComplexSymbolFactory.Location;
import Parser.sym;

%%
%public
%final
%class scanner
%unicode
%cup
%line
%column

/* The following code block is copied literally into the generated scanner
 * class. You can use this to define methods and/or declare fields of the
 * scanner class, which the lexical actions may also reference. Most likely,
 * you will only need to tweak what's already provided below.
 *
 * We use CUP's ComplexSymbolFactory and its associated ComplexSymbol class
 * that tracks the source location of each scanned symbol.
 */
%{
  /** The CUP symbol factory, typically shared with parser. */
  private ComplexSymbolFactory symbolFactory = new ComplexSymbolFactory();

  /** Initialize scanner with input stream and a shared symbol factory. */
  public scanner(java.io.Reader in, ComplexSymbolFactory sf) {
    this(in);
    this.symbolFactory = sf;
  }

  /**
   * Construct a symbol with a given lexical token, a given
   * user-controlled datum, and the matched source location.
   *
   * @param code     identifier of the lexical token (i.e., sym.<TOKEN>)
   * @param value    user-controlled datum to associate with this symbol
   * @effects        constructs new ComplexSymbol via this.symbolFactory
   * @return         a fresh symbol storing the above-desribed information
   */
  private Symbol symbol(int code, Object value) {
    // Calculate symbol location
    int yylen = yylength();
    Location left = new Location(yyline + 1, yycolumn + 1, yychar);
    Location right = new Location(yyline + 1, yycolumn + yylen, yychar + yylen);
    // Calculate symbol name
    int max_code = sym.terminalNames.length;
    String name = code < max_code ? sym.terminalNames[code] : "<UNKNOWN(" + yytext() + ")>";
    return this.symbolFactory.newSymbol(name, code, left, right, value);
  }

  /**
   * Construct a symbol with a given lexical token and matched source
   * location, leaving the user-controlled value field uninitialized.
   *
   * @param code     identifier of the lexical token (i.e., sym.<TOKEN>)
   * @effects        constructs new ComplexSymbol via this.symbolFactory
   * @return         a fresh symbol storing the above-desribed information
   */
  private Symbol symbol(int code) {
    // Calculate symbol location
    int yylen = yylength();
    Location left = new Location(yyline + 1, yycolumn + 1, yychar);
    Location right = new Location(yyline + 1, yycolumn + yylen, yychar + yylen);
    // Calculate symbol name
    int max_code = sym.terminalNames.length;
    String name = code < max_code ? sym.terminalNames[code] : "<UNKNOWN(" + yytext() + ")>";
    return this.symbolFactory.newSymbol(name, code, left, right);
  }

  /**
   * Convert the symbol generated by this scanner into a string.
   *
   * This method is useful to include information in the string representation
   * in addition to the plain CUP name for a lexical token.
   *
   * @param symbol   symbol instance generated by this scanner
   * @return         string representation of the symbol
   */
   public String symbolToString(Symbol s) {
     // All symbols generated by this class are ComplexSymbol instances
     ComplexSymbol cs = (ComplexSymbol)s; 
     if (cs.sym == sym.IDENTIFIER) {
       return "ID(" + (String)cs.value + ")";
     } else if (cs.sym == sym.error) {
       return "<UNEXPECTED(" + (String)cs.value + ")>";
     } else {
       return cs.getName();
     }
   }
%}

/* Helper definitions */
letter = [a-zA-Z]
digit = [0-9]
eol = [\r\n]
white = {eol}|[ \t]
comment_line = "//"
comment_start = "/*"
comment_end = "*/"

NOT_STAR=[^*]
NOT_STAR_OR_SLASH=[^*/]
COMMENT="/*"{NOT_STAR}*("*"({NOT_STAR_OR_SLASH}{NOT_STAR}*)?)*"*/"  

%%

/* Token definitions */

/* reserved words (first so that they take precedence over identifiers) */
"display" 				{ return symbol(sym.DISPLAY); }
"class"					{ return symbol(sym.CLASS); }
"public"				{ return symbol(sym.PUBLIC); }
"static"				{ return symbol(sym.STATIC); }
"void"					{ return symbol(sym.VOID); }
"main"					{ return symbol(sym.MAIN); }
"extends"				{ return symbol(sym.EXTENDS); }
"int"					{ return symbol(sym.INT); }
"boolean"				{ return symbol(sym.BOOLEAN); }
"if"					{ return symbol(sym.IF); }
"else"					{ return symbol(sym.ELSE); }
"while"					{ return symbol(sym.WHILE); }
"System.out.println"	{ return symbol(sym.PRINTLN); }
"length"				{ return symbol(sym.LENGTH); }
"true"					{ return symbol(sym.TRUE); }
"false"					{ return symbol(sym.FALSE); }
"this"					{ return symbol(sym.THIS); }
"new"					{ return symbol(sym.NEW); }
"return"				{ return symbol(sym.RETURN); }
"String"				{ return symbol(sym.STRING); }

/* operators */
"+" 	{ return symbol(sym.PLUS); }
"-" 	{ return symbol(sym.MINUS); }
"=" 	{ return symbol(sym.BECOMES); }
"&&"	{ return symbol(sym.AND); }
"<"		{ return symbol(sym.LT); }
"/"		{ return symbol(sym.DIVIDE); }
"*"		{ return symbol(sym.MULT); }
"!"		{ return symbol(sym.NOT); }


/* delimiters */
"(" { return symbol(sym.LPAREN); }
")" { return symbol(sym.RPAREN); }
"[" { return symbol(sym.LBRACKET); }
"]" { return symbol(sym.RBRACKET); }
"{" { return symbol(sym.LCURLYBRACKET); }
"}" { return symbol(sym.RCURLYBRACKET); }
";" { return symbol(sym.SEMICOLON); }
"." { return symbol(sym.PERIOD); }
"," { return symbol(sym.COMMA); }

/* identifiers */
{letter} ({letter}|{digit}|_)* {
  return symbol(sym.IDENTIFIER, yytext());
}

{digit}+ {
  return symbol(sym.INTEGER_LITERAL, yytext());
}

/* whitespace */
{white}+ { /* ignore whitespace */ }

/* comments */
{comment_line}.*  							{ /* comment - ignore to end of line */ }
//{comment_start}(.|\r|\n)*?{comment_end}	{ /* comment - ignore to end of comment block */ }
{COMMENT} 									{ /* comment - ignore to end of comment block */ }
          
/* lexical errors (last so other matches take precedence) */
. {
    System.err.printf(
      "%nUnexpected character '%s' on line %d at column %d of input.%n",
      yytext(), yyline + 1, yycolumn + 1
    );
    return symbol(sym.error, yytext());
  }

<<EOF>> { return symbol(sym.EOF); }