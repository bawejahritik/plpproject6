package edu.ufl.cise.plpfa22;

import edu.ufl.cise.plpfa22.ast.*;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

public class TypeInferenceVisitor implements ASTVisitor {

    Boolean checkType = true;
    Boolean checkTypeCompletion = true;
    int nesting;
    Hashtable<String, HashSet> nestTable = new Hashtable<>();
    HashSet<String> nestSet = new HashSet<>();
    int nest = 0;
    int numberofChanges;
    boolean last = false;

    //Hritik
    @Override
    public Object visitBlock(Block block, Object arg) throws PLPException {
        Block b = block;
        nestTable.put(Integer.toString(nest),nestSet);
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
            nest = nest + 1;
            procDecList.get(k).visit(this, arg);
            k++;
        }

        b.statement.visit(this, arg);
        nestSet = nestTable.get(Integer.toString(nest));

        return null;
    }

    //Hritik
    @Override
    public Object visitProgram(Program program, Object arg) throws PLPException {
        nestSet.clear();
        //System.out.println("in");
        nestTable.put("0", nestSet);
        //System.out.println("out");
        while(checkType && checkTypeCompletion){
            numberofChanges = 0;
            program.block.visit(this,arg);
            if(numberofChanges == 0){
                checkTypeCompletion = false;
                break;
            }
        }
        last = true;
        program.block.visit(this, arg);
        return null;
    }

    //Hritik
    @Override
    public Object visitStatementAssign(StatementAssign statementAssign, Object arg) throws PLPException {
        statementAssign.ident.visit(this,arg);
        //System.out.println("in");
        statementAssign.expression.visit(this, arg);

        Declaration dec = statementAssign.ident.getDec();
        Expression e = statementAssign.expression;

        if(dec.getClass().getName() == "edu.ufl.cise.plpfa22.ast.ConstDec" || dec.getClass().getName() == "edu.ufl.cise.plpfa22.ast.ProcDec"){
            throw new TypeCheckException();
            //System.out.println("in");
        } else if (dec.getType() == null) {
            if(e.getType() != null) numberofChanges = numberofChanges+1;

            dec.setType(e.getType());
        } else if (dec.getType() != null) {
            if(e.getType() == null){
                //System.out.println("in");
                numberofChanges = numberofChanges + 1;
                e.setType(dec.getType());
            }
        } else if (dec.getType() == e.getType()) {
            dec.setType(e.getType());
            //System.out.println("in");
        } else{
            //System.out.println("in");
            throw  new TypeCheckException();
        }
        return null;
    }

    //Hritik
    @Override
    public Object visitVarDec(VarDec varDec, Object arg) throws PLPException {
        return null;
    }

    //Hritik
    @Override
    public Object visitStatementCall(StatementCall statementCall, Object arg) throws PLPException {
        StringBuffer stringBuffer = new StringBuffer(statementCall.ident.getDec().getClass().getName());
        String s = "edu.ufl.cise.plpfa22.ast.ProcDec";
        if(!s.contentEquals(stringBuffer)){
            //System.out.println("in");
            throw new TypeCheckException();
        }
        return null;
    }

    //Hritik
    @Override
    public Object visitStatementInput(StatementInput statementInput, Object arg) throws PLPException {
        if(statementInput.ident.getDec().getType() == null){
            if(last){
                //System.out.println("in");
                throw new TypeCheckException();
            }
        } else if (statementInput.ident.getDec().getType() == Types.Type.PROCEDURE) {
            //System.out.println("in");
            throw new TypeCheckException();
        } else if (statementInput.ident.getDec().getClass().getName() == "edu.ufl.cise.plpfa22.ast.ConstDec") {
            //System.out.println("in");
            throw new TypeCheckException();
        }
        return null;
    }

    //Hritik
    @Override
    public Object visitStatementOutput(StatementOutput statementOutput, Object arg) throws PLPException {
        statementOutput.expression.visit(this, arg);
        if(statementOutput.expression.getType()==null){
            if(last){
                //System.out.println("in");
                throw new TypeCheckException();
            }
        } else if (statementOutput.expression.getType() == Types.Type.PROCEDURE) {
            //System.out.println("in");
            throw new TypeCheckException();
        }
        return null;
    }

    //Hritik
    @Override
    public Object visitStatementBlock(StatementBlock statementBlock, Object arg) throws PLPException {
        List<Statement> statementList = statementBlock.statements;
        int i=0;
        while(i < statementList.size()){
            //System.out.println("in");
            statementList.get(i).visit(this, arg);
            i+=1;
        }
        return null;
    }

    //Hritik
    @Override
    public Object visitStatementIf(StatementIf statementIf, Object arg) throws PLPException {
        statementIf.expression.visit(this, arg);
        if(statementIf.expression.getType() != Types.Type.BOOLEAN){
            if(last){
                //System.out.println("in");
                throw new TypeCheckException();
            }
        }

        statementIf.statement.visit(this, arg);
        return null;
    }

    //Hritik
    @Override
    public Object visitStatementWhile(StatementWhile statementWhile, Object arg) throws PLPException {
        // System.out.println("Test");
        String a = "4";
        statementWhile.expression.visit(this, arg);
        if(statementWhile.expression.getType() != Types.Type.BOOLEAN){
            if(last){
                // System.out.println("Test 1 1");
                throw new TypeCheckException();
            }
        }

        statementWhile.statement.visit(this, arg);
        return null;
    }

    @Override
    public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws PLPException {
        expressionBinary.e0.visit(this, arg);
        expressionBinary.e1.visit(this, arg);
        // System.out.println("Test");

        StringBuffer operator = new StringBuffer(String.valueOf(expressionBinary.op.getText()));
        Expression l = expressionBinary.e0;
        Expression r = expressionBinary.e1;

        Types.Type type = null;
        String id;
        switch (operator.toString()) {
            case "*" -> {
                //check if left exp and right exp are null
                if(l.getType() == null && r.getType() == null && last) throw new TypeCheckException();
                //check if left and right types aren't equal
                else if (l.getType() != r.getType() && last) throw new TypeCheckException();
                //check if left and right types are null but expression binary type is not null
                else if (l.getType() == null && r.getType() == null && expressionBinary.getType() != null){
                    numberofChanges = numberofChanges + 1;
                    l.setType(expressionBinary.getType());
                    r.setType(expressionBinary.getType());
                }
                //check if left type is not null but right type is null
                else if (l.getType() != null && r.getType() == null) {
                    if(l.getType() != Types.Type.PROCEDURE && r.getType() != Types.Type.STRING){
                        numberofChanges = numberofChanges + 1;
                        r.setType(l.getType());
                        type = l.getType();
                    }
                }
                //check if left type is null but right type is not null
                else if (l.getType() == null && r.getType() != null) {
                    if(r.getType() != Types.Type.PROCEDURE && r.getType() != Types.Type.STRING){
                        numberofChanges = numberofChanges + 1;
                        l.setType(r.getType());
                        type = r.getType();
                    }
                }
                //check if right and left types are the same
                else if (l.getType() == r.getType()) {
                    if(l.getType() != Types.Type.PROCEDURE && r.getType() != Types.Type.STRING){
                        type = l.getType();
                    }
                }
                //default case
                else throw new TypeCheckException();
            }
            case "+" -> {
                //check if left and right types are null
                if(l.getType() == null && r.getType() == null && last) throw new TypeCheckException();
                //check if left and right types are not equal
                else if(l.getType() != r.getType() && last) throw new TypeCheckException();
                //check if left and right types are null but expression binary type is not null
                else if (l.getType() == null && r.getType() == null && expressionBinary.getType() != null) {
                    numberofChanges = numberofChanges + 1;
                    l.setType(expressionBinary.getType());
                    r.setType(expressionBinary.getType());
                }
                //check if left type is not null but right type is null
                else if (l.getType() != null && r.getType()== null) {
                    if(l.getType() != Types.Type.PROCEDURE){
                        numberofChanges = numberofChanges + 1;
                        r.setType(l.getType());
                        type = l.getType();
                    }
                }
                //check if left type is null but right type is not null
                else if (l.getType() == null && r.getType() != null) {
                    if (r.getType() != Types.Type.PROCEDURE){
                        numberofChanges = numberofChanges + 1;
                        l.setType(r.getType());
                        type = r.getType();
                    }
                }
                //check if left and right types are equal
                else if (l.getType() == r.getType()) {
                    if(l.getType() != Types.Type.PROCEDURE){
                        type = l.getType();
                    }
                }
                //default case
                else throw new TypeCheckException();
            }
            case "-", "/", "%" -> {
                //check if left and right types are null
                if(l.getType() == null && r.getType() == null && last) throw new TypeCheckException();
                //check if left type is not equal to the right type
                else if(l.getType() != r.getType() && last) throw new TypeCheckException();
                //check if left type and right type are null but expression binary type is not null
                else if (l.getType() == null && r.getType() == null && expressionBinary.getType() != null) {
                    numberofChanges = numberofChanges + 1;
                    l.setType(expressionBinary.getType());
                    r.setType(expressionBinary.getType());
                }
                //check if left is not null but right type is null
                else if (l.getType() != null && r.getType()== null) {
                    if(l.getType() != Types.Type.PROCEDURE && l.getType() != Types.Type.BOOLEAN && l.getType() != Types.Type.STRING){
                        numberofChanges = numberofChanges + 1;
                        r.setType(l.getType());
                        type = l.getType();
                    }
                }
                //check if left type is null and right type is not null
                else if (l.getType() == null && r.getType() != null) {
                    if (r.getType() != Types.Type.PROCEDURE && r.getType() != Types.Type.BOOLEAN && r.getType() != Types.Type.STRING){
                        numberofChanges = numberofChanges + 1;
                        l.setType(r.getType());
                        type = r.getType();
                    }
                }
                //check if left type is equal to right type
                else if (l.getType() == r.getType()) {
                    if(l.getType() != Types.Type.PROCEDURE && l.getType() != Types.Type.BOOLEAN && l.getType() != Types.Type.STRING){
                        type = l.getType();
                    }
                }
                //default case
                else throw new TypeCheckException();
            }
            case "=", "#", ">", "<", ">=", "<=" -> {
                //check if left type and right type are null
                if(l.getType() == null && r.getType() == null && last) throw new TypeCheckException();
                //check if left type and right type are not equal
                else if(l.getType() != r.getType() && last) throw new TypeCheckException();
                //check if left type is not null but right type is null
                else if (l.getType() != null && r.getType()== null) {
                    if(l.getType() != Types.Type.PROCEDURE){
                        numberofChanges = numberofChanges + 1;
                        r.setType(l.getType());
                        type = Types.Type.BOOLEAN;
                    }
                }
                //check if left type is null but right type is not null
                else if (l.getType() == null && r.getType() != null) {
                    if (r.getType() != Types.Type.PROCEDURE){
                        numberofChanges = numberofChanges + 1;
                        l.setType(r.getType());
                        type = Types.Type.BOOLEAN;
                    }
                }
                //check if left type is equal to right type
                else if (l.getType() == r.getType()) {
                    if(l.getType() != Types.Type.PROCEDURE){
                        type = Types.Type.BOOLEAN;
                    }
                }
                //default case
                else throw new TypeCheckException();
            }
        }

        if(expressionBinary.getType() == null && type == null) expressionBinary.setType(type);
        else if (expressionBinary.getType() == null && type != null) {
            numberofChanges = numberofChanges + 1;
            expressionBinary.setType(type);
        } else if (expressionBinary.getType() == type)expressionBinary.setType(type);
        else if (expressionBinary.getType() != null && type == null) type = expressionBinary.getType();
        else if (expressionBinary.getType() != type) throw new TypeCheckException();

        return null;
    }

    @Override
    public Object visitExpressionIdent(ExpressionIdent expressionIdent, Object arg) throws PLPException {
        // System.out.println("Test");
        // String name = statementInput.ident.getText().toString();
        Declaration expIdent = expressionIdent.getDec();
        if(expressionIdent.getType()==null){
            // System.out.println("Test 1");
            expressionIdent.setType(expIdent.getType());
        }
        else if(expressionIdent.getType()!=null && expIdent.getType()==null){
            // System.out.println("Test 2");
            numberofChanges++;
            expIdent.setType(expressionIdent.getType());
        }
        else if (expressionIdent.getType()==expIdent.getType()){
            // System.out.println("Test 3");
            expressionIdent.setType(expIdent.getType());
        }
        else{
            // System.out.println("Test 4");
            throw new TypeCheckException("Type of Ident not compatible");
        }
        System.out.println("only for final: "+String.valueOf(expressionIdent.firstToken.getText())+":"+expressionIdent.getType());
        return expressionIdent.getNest();
    }

    @Override
    public Object visitExpressionNumLit(ExpressionNumLit expressionNumLit, Object arg) throws PLPException {
        // System.out.println("Test");
        String a = "4";
        expressionNumLit.setType(Types.Type.NUMBER);
        return null;
    }

    @Override
    public Object visitExpressionStringLit(ExpressionStringLit expressionStringLit, Object arg) throws PLPException {
        // System.out.println("Test");
        String alb = "4";
        expressionStringLit.setType(Types.Type.STRING);
        return null;
    }

    @Override
    public Object visitExpressionBooleanLit(ExpressionBooleanLit expressionBooleanLit, Object arg) throws PLPException {
        // String name = statementInput.ident.getText().toString();
        // String name = statementInput.ident.getText().toString();
        expressionBooleanLit.setType(Types.Type.BOOLEAN);
        return null;
    }

    @Override
    public Object visitProcedure(ProcDec procDec, Object arg) throws PLPException {
        // System.out.println("Test");
        String a = "4";
        if(procDec.getType()!=Types.Type.PROCEDURE){
            String al = "4";
            // System.out.println("Test 1");
            numberofChanges++;
            procDec.setType(Types.Type.PROCEDURE);
        }
        procDec.block.visit(this, arg);
        return null;
    }

    @Override
    public Object visitConstDec(ConstDec constDec, Object arg) throws PLPException {
        // System.out.println("Test");
        String typeConstDec = constDec.val.getClass().getName();
        String alb = "4";

        if(typeConstDec=="java.lang.Integer"){
            // System.out.println("Test 1");
            constDec.setType(Types.Type.NUMBER);
        }
        else if(typeConstDec=="java.lang.String"){
            // System.out.println("Test 2");
            constDec.setType(Types.Type.STRING);
        }
        else if(typeConstDec=="java.lang.Boolean"){
            // System.out.println("Test 3");
            constDec.setType(Types.Type.BOOLEAN);
        }
        return null;
    }

    @Override
    public Object visitStatementEmpty(StatementEmpty statementEmpty, Object arg) throws PLPException {
        // System.out.println("Test");
        return null;
    }

    @Override
    public Object visitIdent(Ident ident, Object arg) throws PLPException {
        // System.out.println("Test");
        return null;
    }
}

