package edu.ufl.cise.plpfa22;

import edu.ufl.cise.plpfa22.ast.*;

import java.util.ArrayList;
import java.util.List;


public class Parser implements IParser{

    ILexer lexer;
    IToken t;
    List<Statement> statements = new ArrayList<Statement>();

    public Parser(ILexer lexer) throws SyntaxException, LexicalException {
        this.lexer = lexer;
        t = lexer.next();
    }

    public Program parse() throws SyntaxException, LexicalException {
        Program p = program();
        ReachedEOF();
        return p;
    }

    public Program program() throws SyntaxException, LexicalException {
        Block b = block();
        Program p = new Program(t, b);
        match(IToken.Kind.DOT);
        //ReachedEOF();
        return p;
    }

    Block block() throws SyntaxException, LexicalException {
        List<ConstDec> constDecList = new ArrayList<ConstDec>();
        List<VarDec> varDecList = new ArrayList<VarDec>();
        List<ProcDec> procDecList = new ArrayList<ProcDec>();
        IToken firstT = t;

        while(t.getKind() == IToken.Kind.KW_CONST) {
            IToken temp = null;
            consume();
            temp = t;
            IToken c = t;
            //constDecList.add(new ConstDec(firstT, c));
            match(IToken.Kind.IDENT);
            match(IToken.Kind.EQ);
            if (t.getKind() == IToken.Kind.NUM_LIT) {
                constDecList.add(new ConstDec(temp, c, t.getIntValue()));
                consume();
            } else if (t.getKind() == IToken.Kind.STRING_LIT) {
                constDecList.add(new ConstDec(temp, c, t.getStringValue()));
                consume();
            } else if (t.getKind() == IToken.Kind.BOOLEAN_LIT) {
                constDecList.add(new ConstDec(temp, c, t.getBooleanValue()));
                consume();
            }

            while (t.getKind() == IToken.Kind.COMMA) {
                consume();
                temp = t;
                c = t;
                match(IToken.Kind.IDENT);
                match(IToken.Kind.EQ);

                if (t.getKind() == IToken.Kind.NUM_LIT) {
                    constDecList.add(new ConstDec(temp, c, t.getIntValue()));
                    consume();
                } else if (t.getKind() == IToken.Kind.STRING_LIT) {
                    constDecList.add(new ConstDec(temp, c, t.getStringValue()));
                    consume();
                } else if (t.getKind() == IToken.Kind.BOOLEAN_LIT) {
                    constDecList.add(new ConstDec(temp, c, t.getBooleanValue()));
                    consume();
                }
            }
            match(IToken.Kind.SEMI);
        }

        while(t.getKind() == IToken.Kind.KW_VAR){
            IToken firstToken = t;
            consume();
            IToken var = t;
            varDecList.add(new VarDec(firstToken, var));
            match(IToken.Kind.IDENT);

            while (t.getKind() == IToken.Kind.COMMA){
                consume();
                IToken v = t;
                varDecList.add(new VarDec(firstToken, v));
                consume();
            }
            match(IToken.Kind.SEMI);
        }

        while(t.getKind() == IToken.Kind.KW_PROCEDURE) {
            IToken firstToken = t;
            consume();
            IToken name = t;
            match(IToken.Kind.IDENT);
            match(IToken.Kind.SEMI);
            Block body = block();
            match(IToken.Kind.SEMI);
            procDecList.add(new ProcDec(firstToken, name, body));
        }
        Statement s = statement();
        Block b = new Block(firstT, constDecList, varDecList, procDecList, s);
        return b;
    }

//    void statement() throws SyntaxException {
//        throw new UnsupportedOperationException();
//    }

//    void expression() throws SyntaxException {
//        additive_expression();
//
//        while(isKind(IToken.Kind.LT, t) || isKind(IToken.Kind.GT, t) || isKind(IToken.Kind.EQ, t) || isKind(IToken.Kind.NEQ, t) || isKind(IToken.Kind.LE, t) || isKind(IToken.Kind.GE, t)){
//            switch (t.getKind()) {
//                case LT -> {
//                    match(IToken.Kind.LT);
//                    break;
//                }
//                case GT -> {
//                    match(IToken.Kind.GT);
//                    break;
//                }
//                case EQ -> {
//                    match(IToken.Kind.EQ);
//                    break;
//                }
//                case NEQ -> {
//                    match(IToken.Kind.NEQ);
//                    break;
//                }
//                case LE -> {
//                    match(IToken.Kind.LE);
//                    break;
//                }
//                case GE -> {
//                    match(IToken.Kind.GE);
//                    break;
//                }
//            }
//        }
//        additive_expression();
//    }

    Statement statement() throws SyntaxException, LexicalException {
        IToken firstToken = t;
        Statement s = null;
        Expression e = null;

        if(isKind(IToken.Kind.IDENT, t)){
            Ident name = new Ident(t);
            consume();
            match(IToken.Kind.ASSIGN);
            //consume();
            e = expression();
            s = new StatementAssign(firstToken, name,e); //what will  come in this
        }else if(isKind(IToken.Kind.KW_CALL, t)){
            consume();
            Ident name = new Ident(t);
            match(IToken.Kind.IDENT);
            s = new StatementCall(firstToken, name);
        }else if(isKind(IToken.Kind.QUESTION, t)){
            consume();
            Ident name = new Ident(t);
            match(IToken.Kind.IDENT);
            s = new StatementInput(firstToken, name);
        }else if(isKind(IToken.Kind.BANG, t)){
            consume();
            e = expression();
            s = new StatementOutput(firstToken, e);
        }else if(isKind(IToken.Kind.KW_BEGIN, t)){
            consume();
            Statement s0 = statement();
            statements.add(s0);
            while(t.getKind() == IToken.Kind.SEMI){
                consume();
                Statement s1 = statement();
                statements.add(s1);
            }
            match(IToken.Kind.KW_END);
            s = new StatementBlock(firstToken,statements);
        }else if(isKind(IToken.Kind.KW_IF, t)){
            consume();
            Expression e1 = expression();
            match(IToken.Kind.KW_THEN);
            Statement s1 = statement();
            s = new StatementIf(firstToken,e1, s1);
        } else if (isKind(IToken.Kind.KW_WHILE, t)) {
            consume();
            Expression e1 = expression();
            match(IToken.Kind.KW_DO);
            Statement s1 = statement();
            s = new StatementWhile(firstToken, e1, s1);
        }else{
            s = new StatementEmpty(firstToken);
        }
        return s;
    }

    public Expression expression() throws SyntaxException, LexicalException {
        IToken firstToken = t;
        Expression left = null;
        Expression right = null;
        left = additive_expression();
        while(isKind(IToken.Kind.LT, t) || isKind(IToken.Kind.GT, t) || isKind(IToken.Kind.EQ, t) || isKind(IToken.Kind.NEQ, t) || isKind(IToken.Kind.LE, t) || isKind(IToken.Kind.GE, t)){
            IToken op = t;
            consume();
            right = additive_expression();
            left = new ExpressionBinary(firstToken, left, op, right);
            }
            return left;
    }

    Expression additive_expression() throws SyntaxException, LexicalException {
        IToken firstToken = t;
        Expression left = null;
        Expression right = null;
        left = multiplicative_expression();
        while(isKind(IToken.Kind.PLUS, t) || isKind(IToken.Kind.MINUS, t)){
            IToken op = t;
            consume();
            right = multiplicative_expression();
            left = new ExpressionBinary(firstToken, left, op, right);
        }
        return left;
    }

//    void additive_expression() throws SyntaxException {
//        multiplicative_expression();
//
//        while (isKind(IToken.Kind.PLUS, t) || isKind(IToken.Kind.MINUS, t)){
//            switch(t.getKind()) {
//                case PLUS -> {
//                    match(IToken.Kind.PLUS);
//                    break;
//                }
//                case MINUS -> {
//                    match(IToken.Kind.MINUS);
//                    break;
//                }
//            }
//        }
//        multiplicative_expression();
//    }

    Expression multiplicative_expression() throws SyntaxException, LexicalException {
        IToken firstToken = t;
        Expression left = null;
        Expression right = null;
        left = primary_expression();
        while(isKind(IToken.Kind.TIMES, t) || isKind(IToken.Kind.DIV, t) || isKind(IToken.Kind.MOD, t)){
            IToken op = t;
            consume();
            right = primary_expression();
            left = new ExpressionBinary(firstToken, left, op, right);
        }

        return left;
    }

//    void multiplicative_expression() throws SyntaxException{
//        primary_expression();
//
//        while(isKind(IToken.Kind.TIMES, t) || isKind(IToken.Kind.DIV, t) || isKind(IToken.Kind.MOD, t)){
//            switch(t.getKind()) {
//                case TIMES -> {
//                    match(IToken.Kind.TIMES);
//                    break;
//                }
//                case DIV -> {
//                    match(IToken.Kind.DIV);
//                    break;
//                }
//                case MOD -> {
//                    match(IToken.Kind.MOD);
//                    break;
//                }
//            }
//            primary_expression();
//        }
//    }

    Expression primary_expression() throws SyntaxException, LexicalException {
        IToken firstToken = t;
        Expression e = null;
        if(isKind(IToken.Kind.IDENT, t)){
            e = new ExpressionIdent(firstToken);
            consume();
        }else if(isKind(IToken.Kind.NUM_LIT, t) ||isKind(IToken.Kind.STRING_LIT, t) || isKind(IToken.Kind.BOOLEAN_LIT, t)){
            e = const_val();
        }else if (isKind(IToken.Kind.LPAREN, t)){
            consume();
            e = expression();
            match(IToken.Kind.RPAREN);
        }else throw new SyntaxException();
        return e;
    }
//    void primary_expression() throws SyntaxException {
//        if(isKind(IToken.Kind.IDENT, t)){
//            match(IToken.Kind.IDENT);
//        } else if (isKind(IToken.Kind.LPAREN, t)) {
//            match(IToken.Kind.LPAREN);
//            expression();
//            match(IToken.Kind.RPAREN);
//        }else {
//            const_val();
//        }
//        //how to handle const_val
//    }

    Expression const_val() throws SyntaxException, LexicalException {
        IToken firstToken = t;
        Expression e = null;
        if(isKind(IToken.Kind.NUM_LIT, t)){
            e = new ExpressionNumLit(firstToken);
            consume();
        }else if (isKind(IToken.Kind.STRING_LIT, t)) {
            e = new ExpressionStringLit(firstToken);
            consume();
        }else if (isKind(IToken.Kind.BOOLEAN_LIT, t)) {
            e = new ExpressionBooleanLit(firstToken);
            consume();
        }else throw new SyntaxException();

        return e;
    }
//    void const_val() throws  SyntaxException{
//        if(isKind(IToken.Kind.NUM_LIT, t)) {
//            match(IToken.Kind.NUM_LIT);
//        } else if (isKind(IToken.Kind.STRING_LIT, t)) {
//            match(IToken.Kind.STRING_LIT);
//        } else if (isKind(IToken.Kind.BOOLEAN_LIT, t)){
//            match(IToken.Kind.BOOLEAN_LIT);
//        }else{
//            // handle error
//        }
//    }


     IToken consume() throws SyntaxException, LexicalException {
        t = lexer.next();
        return t;
    }

    IToken ReachedEOF() throws SyntaxException{
        if(this.t.getKind() == IToken.Kind.EOF) {
            return this.t;
        }
        throw new SyntaxException("",t.getSourceLocation().line(), t.getSourceLocation().column());
    }

    boolean match(IToken.Kind kind) throws SyntaxException, LexicalException {
        if(isKind(kind, t)) {
            consume();
            return true;
        }
        else throw new SyntaxException("",t.getSourceLocation().line(), t.getSourceLocation().column());
    }

    protected boolean isKind(IToken.Kind kind, IToken t) {
        return t.getKind() == kind;
    }
}