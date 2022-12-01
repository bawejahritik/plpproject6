package edu.ufl.cise.plpfa22;

import edu.ufl.cise.plpfa22.ast.ASTVisitor;

public class CompilerComponentFactory {
    public static ILexer getLexer(String input) throws LexicalException {
        return new Lexer(input);
    }

    public static IParser getParser(ILexer lexer) throws LexicalException, SyntaxException {
        return new Parser(lexer);
    }

    public static ASTVisitor getScopeVisitor(){
        return new ScopeVisitor();
    }

    public static ASTVisitor getTypeInferenceVisitor(){
        return new TypeInferenceVisitor();
    }

    public static ASTVisitor getCodeGenVisitor(String packageName, String className, String s) {
        return  new CodeGenVisitor(packageName, className, s);
    }
}
