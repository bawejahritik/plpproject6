package edu.ufl.cise.plpfa22;

import edu.ufl.cise.plpfa22.ast.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

//LeBlanc-Cook Symbol Table

//Scope rules for our language
//1) Block structure
//2) Identifiers must be declared before they are used except procedure names are visible in entire scope.
//- Visitor will need to make two passes over list of procedure declaration.
//- First pass enters the name into symbol table
//- Second pass looks up identifiers etc


public class ScopeVisitor implements ASTVisitor {

    boolean pass = false;
    String scopeId = createID();
    SymbolTable symbolTable = new SymbolTable();

    private static AtomicLong idCounter = new AtomicLong();

    public static String createID()
    {
        return String.valueOf(idCounter.getAndIncrement());
    }


    //hritik
    @Override
    public Object visitBlock(Block block, Object arg) throws PLPException {
        Block b = block;
        List<ConstDec> constDecList = b.constDecs;
        List<VarDec> varDecList = b.varDecs;
        List<ProcDec> procDecList = b.procedureDecs;
        int i=0, j=0, k=0;
        while(i < constDecList.size()){
            constDecList.get(i).visit(this, arg);
            i++;
        }
        while(j < varDecList.size()){
            varDecList.get(j).visit(this, arg);
            j++;
        }
        while(k < procDecList.size()){
            procDecList.get(k).visit(this, arg);
            k++;
        }

        b.statement.visit(this, arg);

        return null;
    }

    //suneet
    @Override
    public Object visitProgram(Program program, Object arg) throws PLPException {
        Block block  = program.block;
        block.visit(this, arg);
        pass = true;
        block.visit(this, arg);
        return null;
    }

    //hritik
    @Override
    public Object visitStatementAssign(StatementAssign statementAssign, Object arg) throws PLPException {
        Ident vStatement = statementAssign.ident;
        Expression eStatement = statementAssign.expression;

        vStatement.visit(this, arg);
        eStatement.visit(this, arg);
        return null;
    }

    //suneet
    @Override
    public Object visitVarDec(VarDec varDec, Object arg) throws PLPException {
        if(pass){
//            String name = varDec.ident.getText().toString();
            String name = String.valueOf(varDec.ident.getText());
            varDec.setNest(symbolTable.currentLevel);
            symbolTable.insert(name, varDec);
            System.out.println(name + " " + symbolTable.currentLevel);
        }
        return null;
    }

    //hritik
    @Override
    public Object visitStatementCall(StatementCall statementCall, Object arg) throws PLPException {
        if(pass){
            Ident ident = statementCall.ident;
            ident.visit(this, arg);

//            String name = statementCall.ident.getText().toString();
            String name = String.valueOf(statementCall.ident.getText());
            Declaration dec = symbolTable.lookup(name);
            if(dec == null) throw new ScopeException();
        }

        return null;
    }

    //suneet
    @Override
    public Object visitStatementInput(StatementInput statementInput, Object arg) throws PLPException {
        if(pass){
//            String name = statementInput.ident.getText().toString();
            String name = String.valueOf(statementInput.ident.getText());
            Declaration dec = symbolTable.lookup(name);
            if(dec == null) throw new ScopeException();
            statementInput.ident.visit(this, arg);
        }

        return null;
    }

    //hritik
    @Override
    public Object visitStatementOutput(StatementOutput statementOutput, Object arg) throws PLPException {
        statementOutput.expression.visit(this, arg);
        return null;
    }

    //suneet
    @Override
    public Object visitStatementBlock(StatementBlock statementBlock, Object arg) throws PLPException {
        List<Statement> statementList = statementBlock.statements;
        int i=0;
        while(i < statementList.size()){
            statementList.get(i).visit(this, arg);
            i++;
        }
        return null;
    }

    //hritik
    @Override
    public Object visitStatementIf(StatementIf statementIf, Object arg) throws PLPException {
        //System.out.println("pass:" + pass);
        statementIf.expression.visit(this, arg);
        //System.out.println("here");
        statementIf.statement.visit(this, arg);
        return null;
    }

    //suneet
    @Override
    public Object visitStatementWhile(StatementWhile statementWhile, Object arg) throws PLPException {
        statementWhile.expression.visit(this,arg);
        statementWhile.statement.visit(this, arg);
        return null;
    }

    //hritik
    @Override
    public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws PLPException {
        expressionBinary.e0.visit(this, arg);
        expressionBinary.e1.visit(this, arg);
        return null;
    }

    //suneet
    @Override
    public Object visitExpressionIdent(ExpressionIdent expressionIdent, Object arg) throws PLPException {
        if(pass){
//            String name = expressionIdent.firstToken.getText().toString();
            String name = String.valueOf(expressionIdent.firstToken.getText());
            Declaration dec = symbolTable.lookup(name);

            expressionIdent.setNest(symbolTable.currentLevel);
            if(dec == null) throw new ScopeException();
            expressionIdent.setDec(dec);
        }

        return null;
    }

    //hritik
    @Override
    public Object visitExpressionNumLit(ExpressionNumLit expressionNumLit, Object arg) throws PLPException {
        return null;
    }

    //suneet
    @Override
    public Object visitExpressionStringLit(ExpressionStringLit expressionStringLit, Object arg) throws PLPException {
        return null;
    }

    //hritik
    @Override
    public Object visitExpressionBooleanLit(ExpressionBooleanLit expressionBooleanLit, Object arg) throws PLPException {
        return null;
    }

    //suneet
    @Override
    public Object visitProcedure(ProcDec procDec, Object arg) throws PLPException {
        if(pass){
            procDec.setNest(symbolTable.currentLevel);
            symbolTable.enterScope();
            procDec.block.visit(this, arg);
            symbolTable.closeScope();
        } else{
            // String name = procDec.ident.getText().toString();
            String name = String.valueOf(procDec.ident.getText());
            symbolTable.insert(name, procDec);
            procDec.setNest(symbolTable.currentLevel);
            symbolTable.entry();
            procDec.block.visit(this, arg);
            symbolTable.closeScope();
        }
        // Doubt how to pass this twice as discussed in lecture?

        return null;
    }

    //hritik
    @Override
    public Object visitConstDec(ConstDec constDec, Object arg) throws PLPException {
        if(pass){
//            String name = constDec.ident.getText().toString();
            String name = String.valueOf(constDec.ident.getText());
            constDec.setNest(symbolTable.currentLevel);
            symbolTable.insert(name, constDec);

        }

        return null;
    }

    @Override
    public Object visitStatementEmpty(StatementEmpty statementEmpty, Object arg) throws PLPException {
        return null;
    }

    //suneet
    @Override
    public Object visitIdent(Ident ident, Object arg) throws PLPException {
        if(pass){
//            String name = ident.firstToken.getText().toString();
            String name = String.valueOf(ident.firstToken.getText());
            ident.setNest(symbolTable.currentLevel);
            Declaration dec = symbolTable.lookup(name);
            if(dec == null) throw new ScopeException();
            ident.setDec(dec);
        }

        return null;
    }
}

