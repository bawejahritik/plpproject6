package edu.ufl.cise.plpfa22;

import edu.ufl.cise.plpfa22.IToken.Kind;
import edu.ufl.cise.plpfa22.ast.*;
import edu.ufl.cise.plpfa22.ast.Types.Type;
import org.objectweb.asm.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


//Your submission for this assignment should have an implementation of visitBlock, visitProgram,
// visitStatementOutput, visitStatementBlock, visitStatementIf, visitExpressionBinary, visitExpressionNumLit,
// visitExpressionStringLit, and visitExpressionBooleanLit.

//FALSE < TRUE, + means OR, and * means AND.   For Strings S0 and S1, S0+S1 is the concatenation of S0 with S1.
// S0 < S1 means that S0 is a prefix of S1 and they are not equal to each other.
// S0 > S1 means that S1 is a suffix of S0 and they are not equal.
// In the java/lang/String class, methods concat, startsWith, endsWith, and equals will likely be useful.
// " ! exp " means output the value of the expression on the standard output.


public class CodeGenVisitor implements ASTVisitor, Opcodes {

	final String packageName;
	final String className;
	final String sourceFileName;
	final String fullyQualifiedClassName;
	final String classDesc;
	final String ownerVirtual = "java/lang/String";
	final String ownerStatic = "edu/ufl/cise/plpfa22/stringRuntime";
	final String descriptorStatic = "("+"Ljava/lang/String;"+"Ljava/lang/String;"+")"+"Z";
	final String runnable = "java/lang/Runnable";
	final String object = "java/lang/Object";
	final String currDescriptorEmpty = "()V";
	String currentProcedurePath;
	int checkProcedureFlag;
	List<CodeGenUtils.GenClass> byteCodeList = new ArrayList<>();

	ClassWriter classWriter;
	protected ClassWriter classWriter1;


	public CodeGenVisitor(String className, String packageName, String sourceFileName) {
		super();
		this.packageName = packageName;
		this.className = className;
		this.sourceFileName = sourceFileName;
		this.fullyQualifiedClassName = packageName + "/" + className;
		this.classDesc="L"+this.fullyQualifiedClassName+';';
	}

	//already given
//	visitBlock
//• Visits VarDecs
//• Visits ProcDecs, passes current className
//• Creates a MethodVisitor for run method, passes MethodVisitor to statement.visit  (all
//	Statements should get MethodVisitor from arg)
//			• Adds return statement and finishes up method (visitMaxs, visitEnd)
//
	@Override
	public Object visitBlock(Block block, Object arg) throws PLPException {

		switch (checkProcedureFlag){
			case 1 -> {
				classWriter1 = (ClassWriter) arg;
				int i = 0;
				while(i < block.procedureDecs.size()){
					block.procedureDecs.get(i).visit(this, arg);
					i++;
				}
			}
			case 0 -> {
				ClassWriter classWriter1 = (ClassWriter) arg;
				int j=0, k=0;
				while(j < block.varDecs.size()){
					block.varDecs.get(j).visit(this, arg);
					j++;
				}
				while (k < block.procedureDecs.size()){
					block.procedureDecs.get(k).visit(this, arg);
				}

				MethodVisitor methodVisitor = classWriter1.visitMethod(0, "run", currDescriptorEmpty, null, null);
				methodVisitor.visitCode();

				block.statement.visit(this, methodVisitor);
				methodVisitor.visitInsn(RETURN);
				methodVisitor.visitMaxs(3, 1);
				methodVisitor.visitEnd();
			}
		}

		return null;

	}

//	visitProgram
//• Creates ClassWriter for main class
//• Invokes a simple ASTVisitor to visit all procedure declarations and annotate them with their JVM
//	names.  This is necessary because the
//• generates code for <init>
//• generates code for main
//• passes ClassWriter to block.visit
//• some other bookkeeping details
//• Returns a List of GenClass records defined in updated CodeGenUtils
//•  public record GenClass(String className, byte[] byteCode) {}
//	//already given
	@Override
	public Object visitProgram(Program program, Object arg) throws PLPException {
		//create a classWriter and visit it
		currentProcedurePath = fullyQualifiedClassName;
		classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		//Hint:  if you get failures in the visitMaxs, try creating a ClassWriter with 0
		// instead of ClassWriter.COMPUTE_FRAMES.  The result will not be a valid classfile,
		// but you will be able to print it so you can see the instructions.  After fixing,
		// restore ClassWriter.COMPUTE_FRAMES
		classWriter.visit(V18, ACC_PUBLIC | ACC_SUPER, fullyQualifiedClassName, null, "java/lang/Object", new String[] {runnable});
		classWriter.visitSource(className.concat(".java"), null);

		checkProcedureFlag = 1;
		program.block.visit(this, classWriter);
		checkProcedureFlag = 0;

		String currMethod = "<init>";


		MethodVisitor methodVisitor = classWriter.visitMethod(ACC_PUBLIC, currMethod, currDescriptorEmpty, null, null);
		methodVisitor.visitCode();
		methodVisitor.visitVarInsn(ALOAD, 0);
		methodVisitor.visitMethodInsn(INVOKESPECIAL, object, currMethod, currDescriptorEmpty, false);
		methodVisitor.visitInsn(RETURN);
		methodVisitor.visitMaxs(1, 1);
		methodVisitor.visitEnd();


		//get a method visitor for the main method.

		MethodVisitor methodVisitor1 = classWriter.visitMethod(ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
		methodVisitor1.visitCode();
		methodVisitor1.visitTypeInsn(NEW, fullyQualifiedClassName);
		methodVisitor1.visitInsn(DUP);

		methodVisitor1.visitMethodInsn(INVOKESPECIAL, fullyQualifiedClassName, currMethod, currDescriptorEmpty, false);
		methodVisitor1.visitMethodInsn(INVOKEVIRTUAL, fullyQualifiedClassName, "run", currDescriptorEmpty, false);
		methodVisitor1.visitInsn(RETURN);
		methodVisitor1.visitMaxs(1, 1);
		methodVisitor1.visitEnd();

		//visit the block, passing it the classWriter
		program.block.visit(this, classWriter);
		//finish up the class
		classWriter.visitEnd();

		byteCodeList.add(new CodeGenUtils.GenClass(fullyQualifiedClassName, classWriter.toByteArray()));
		Collections.reverse(byteCodeList);
		//return the byteCodeList making up the classfile
		System.out.println("end");
		return byteCodeList;
	}

//	visitStatementAssign
//• visit the expression
//• visit the ident

	@Override
	public Object visitStatementAssign(StatementAssign statementAssign, Object arg) throws PLPException {
		statementAssign.expression.visit(this, arg);
		statementAssign.ident.visit(this, arg);
		return null;
	}

	@Override
	public Object visitVarDec(VarDec varDec, Object arg) throws PLPException {
		ClassWriter cw = (ClassWriter) arg;
		String varDecText = new String(String.valueOf(varDec.ident.getText()));
		String currentDescriptor = varDec.getCurrentDescriptor();
		System.out.println("current " + varDec.getCurrentDescriptor());
		FieldVisitor fv = cw.visitField(0, varDecText, currentDescriptor, null, null);
		fv.visitEnd();

		return null;
	}

//	visitStatementCall
//• Create instance of class corresponding to procedure
//• The <init> method takes instance of lexically enclosing class as parameter.  If the procedure is
//	enclosed in this one, ALOAD_0 works.  (Recall that we are in a virtual method, run, so the JVM
//	will have automatically loaded “this” into local variable slot 0.)  Otherwise follow the chain of
//	this$n references to find an instance of the enclosing class of the procedure.  (Use nesting
//	levels)
//			• Invoke run method.

	@Override
	public Object visitStatementCall(StatementCall statementCall, Object arg) throws PLPException {
		MethodVisitor mv = (MethodVisitor) arg;
		String path = fullyQualifiedClassName + statementCall.ident.getDec().getProcedurePath();
		int check = Objects.equals(currentProcedurePath, path) ? 1 : 0;
		switch (check) {
			case 1 -> {
				mv.visitVarInsn(ALOAD, 0);
				mv.visitMethodInsn(INVOKEVIRTUAL, path, "run", currDescriptorEmpty, false);
			}

			case 2 -> {
				mv.visitTypeInsn(NEW, path);
				mv.visitInsn(DUP);

				mv.visitVarInsn(ALOAD, 0);
				String currentSuperClass = new String(currentProcedurePath);
				String currentSubClass = new String(currentProcedurePath);

				int i = statementCall.ident.getNest()-1;
				int j = statementCall.ident.getDec().getNest();
				while (i >= j) {
					currentSubClass = currentSuperClass;
					int k = currentSubClass.length();
					while(k >= 0){
						if (currentSubClass.charAt(k) == '$'){
							currentSuperClass = currentSubClass.substring(0, k);
							break;
						}
						k--;
					}
					String currName = "this$" + i;
					String currDescriptor = "L" + currentSuperClass + ";";
					mv.visitFieldInsn(GETFIELD, currentSubClass, currName, currDescriptor);
					i--;
				}

				String currentSuperClassName = new String("");
				int i1 = currentSuperClassName.length()-1;
				while(i1 >= 0){
					if(path.charAt(i1) == '$'){
						currentSuperClassName = path.substring(0, i1);
						break;
					}
					String currName = "<init>";
					String currDescriptor = "(L" + currentSuperClassName + ";)V";
					mv.visitMethodInsn(INVOKESPECIAL, path, currName, currDescriptor, false);
					mv.visitMethodInsn(INVOKEVIRTUAL, path, "run", currDescriptorEmpty, false);
					i1--;
				}
			}
		}

		return null;
	}

	@Override
	public Object visitStatementInput(StatementInput statementInput, Object arg) throws PLPException {
		throw new UnsupportedOperationException();
	}

	//already given
	@Override
	public Object visitStatementOutput(StatementOutput statementOutput, Object arg) throws PLPException {
		MethodVisitor mv = (MethodVisitor)arg;
		mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
		statementOutput.expression.visit(this, arg);
		Type etype = statementOutput.expression.getType();
		String JVMType = (etype.equals(Type.NUMBER) ? "I" : (etype.equals(Type.BOOLEAN) ? "Z" : "Ljava/lang/String;"));
		String printlnSig = "(" + JVMType +")V";
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", printlnSig, false);
		return null;
	}

	//Hritik
	@Override
	public Object visitStatementBlock(StatementBlock statementBlock, Object arg) throws PLPException {
		int i = 0;
		while(i < statementBlock.statements.size()){
			statementBlock.statements.get(i).visit(this, arg);
			i++;
		}

		return null;
	}

	//Suneet
	@Override
	public Object visitStatementIf(StatementIf statementIf, Object arg) throws PLPException {
		MethodVisitor methodVisitor = (MethodVisitor)arg;
		statementIf.expression.visit(this, arg);
		System.out.println(statementIf.expression.getType());
		Label label = new Label();
		methodVisitor.visitInsn(ICONST_1);
		methodVisitor.visitJumpInsn(IF_ICMPNE, label);
		statementIf.statement.visit(this, arg);
		methodVisitor.visitLabel(label);
		methodVisitor.visitEnd();
		return null;
	}

//	visitStatementWhile
//	There are multiple ways to do this, this is what the Java compiler does
//	GOTO  GuardLabel
//	BodyLabel
//	Loop body
//	GuardLabel
//	Evaluate guard expression
//	IFNE  BodyLabel

	@Override
	public Object visitStatementWhile(StatementWhile statementWhile, Object arg) throws PLPException {
		MethodVisitor methodVisitor = (MethodVisitor) arg;
		Label startOfWhile = new Label();
		Label endOfWhile = new Label();

		Expression guardExpression = statementWhile.expression;
		methodVisitor.visitLabel(startOfWhile);
		guardExpression.visit(this, arg);
		methodVisitor.visitInsn(ICONST_1);
		methodVisitor.visitJumpInsn(IF_ICMPLT, endOfWhile);
		statementWhile.statement.visit(this, arg);
		methodVisitor.visitJumpInsn(GOTO, startOfWhile);
		methodVisitor.visitLabel(endOfWhile);

		return null;
	}

	//Hritik
	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws PLPException {
		MethodVisitor mv = (MethodVisitor) arg;
		Type argType = expressionBinary.e0.getType();
		Kind op = expressionBinary.op.getKind();
		switch (argType) {
			case NUMBER -> {
				expressionBinary.e0.visit(this, arg);
				expressionBinary.e1.visit(this, arg);
				switch (op) {
					case PLUS -> mv.visitInsn(IADD);
					case MINUS -> mv.visitInsn(ISUB);
					case TIMES -> mv.visitInsn(IMUL);
					case DIV -> mv.visitInsn(IDIV);
					case MOD -> mv.visitInsn(IREM);
					case EQ -> {
						Label labelNumEqFalseBr = new Label();
						mv.visitJumpInsn(IF_ICMPNE, labelNumEqFalseBr);
						mv.visitInsn(ICONST_1);
						Label labelPostNumEq = new Label();
						mv.visitJumpInsn(GOTO, labelPostNumEq);
						mv.visitLabel(labelNumEqFalseBr);
						mv.visitInsn(ICONST_0);
						mv.visitLabel(labelPostNumEq);
					}
					case NEQ -> {
						Label labelNumEqFalseBr = new Label();
						mv.visitJumpInsn(IF_ICMPEQ, labelNumEqFalseBr);
						mv.visitInsn(ICONST_1);
						Label labelPostNumEq = new Label();
						mv.visitJumpInsn(GOTO, labelPostNumEq);
						mv.visitLabel(labelNumEqFalseBr);
						mv.visitInsn(ICONST_0);
						mv.visitLabel(labelPostNumEq);
					}
					case LT -> {
						Label labelNumEqFalseBr = new Label();
						mv.visitJumpInsn(IF_ICMPGE, labelNumEqFalseBr);
						mv.visitInsn(ICONST_1);
						Label labelPostNumEq = new Label();
						mv.visitJumpInsn(GOTO, labelPostNumEq);
						mv.visitLabel(labelNumEqFalseBr);
						mv.visitInsn(ICONST_0);
						mv.visitLabel(labelPostNumEq);
					}
					case LE -> {
						Label labelNumEqFalseBr = new Label();
						mv.visitJumpInsn(IF_ICMPGT, labelNumEqFalseBr);
						mv.visitInsn(ICONST_1);
						Label labelPostNumEq = new Label();
						mv.visitJumpInsn(GOTO, labelPostNumEq);
						mv.visitLabel(labelNumEqFalseBr);
						mv.visitInsn(ICONST_0);
						mv.visitLabel(labelPostNumEq);
					}
					case GT -> {
						Label labelNumEqFalseBr = new Label();
						mv.visitJumpInsn(IF_ICMPLE, labelNumEqFalseBr);
						mv.visitInsn(ICONST_1);
						Label labelPostNumEq = new Label();
						mv.visitJumpInsn(GOTO, labelPostNumEq);
						mv.visitLabel(labelNumEqFalseBr);
						mv.visitInsn(ICONST_0);
						mv.visitLabel(labelPostNumEq);
					}
					case GE -> {
						Label labelNumEqFalseBr = new Label();
						mv.visitJumpInsn(IF_ICMPLT, labelNumEqFalseBr);
						mv.visitInsn(ICONST_1);
						Label labelPostNumEq = new Label();
						mv.visitJumpInsn(GOTO, labelPostNumEq);
						mv.visitLabel(labelNumEqFalseBr);
						mv.visitInsn(ICONST_0);
						mv.visitLabel(labelPostNumEq);
					}
					default -> {
						throw new IllegalStateException("code gen bug in visitExpressionBinary NUMBER");
					}
				}
				;
			}
			case BOOLEAN -> {
				expressionBinary.e0.visit(this, arg);
				expressionBinary.e1.visit(this, arg);
				switch (op) {
					case PLUS -> mv.visitInsn(IADD);
					case TIMES -> mv.visitInsn(IMUL);
					case EQ -> {
						Label labelNumEqFalseBr = new Label();
						mv.visitJumpInsn(IF_ICMPNE, labelNumEqFalseBr);
						mv.visitInsn(ICONST_1);
						Label labelPostNumEq = new Label();
						mv.visitJumpInsn(GOTO, labelPostNumEq);
						mv.visitLabel(labelNumEqFalseBr);
						mv.visitInsn(ICONST_0);
						mv.visitLabel(labelPostNumEq);
					}
					case NEQ -> {
						Label labelNumEqFalseBr = new Label();
						mv.visitJumpInsn(IF_ICMPEQ, labelNumEqFalseBr);
						mv.visitInsn(ICONST_1);
						Label labelPostNumEq = new Label();
						mv.visitJumpInsn(GOTO, labelPostNumEq);
						mv.visitLabel(labelNumEqFalseBr);
						mv.visitInsn(ICONST_0);
						mv.visitLabel(labelPostNumEq);
					}
					case LT -> {
						Label labelNumEqFalseBr = new Label();
						mv.visitJumpInsn(IF_ICMPGE, labelNumEqFalseBr);
						mv.visitInsn(ICONST_1);
						Label labelPostNumEq = new Label();
						mv.visitJumpInsn(GOTO, labelPostNumEq);
						mv.visitLabel(labelNumEqFalseBr);
						mv.visitInsn(ICONST_0);
						mv.visitLabel(labelPostNumEq);
					}
					case LE -> {
						Label labelNumEqFalseBr = new Label();
						mv.visitJumpInsn(IF_ICMPGT, labelNumEqFalseBr);
						mv.visitInsn(ICONST_1);
						Label labelPostNumEq = new Label();
						mv.visitJumpInsn(GOTO, labelPostNumEq);
						mv.visitLabel(labelNumEqFalseBr);
						mv.visitInsn(ICONST_0);
						mv.visitLabel(labelPostNumEq);
					}
					case GT -> {
						Label labelNumEqFalseBr = new Label();
						mv.visitJumpInsn(IF_ICMPLE, labelNumEqFalseBr);
						mv.visitInsn(ICONST_1);
						Label labelPostNumEq = new Label();
						mv.visitJumpInsn(GOTO, labelPostNumEq);
						mv.visitLabel(labelNumEqFalseBr);
						mv.visitInsn(ICONST_0);
						mv.visitLabel(labelPostNumEq);
					}
					case GE -> {
						Label labelNumEqFalseBr = new Label();
						mv.visitJumpInsn(IF_ICMPLT, labelNumEqFalseBr);
						mv.visitInsn(ICONST_1);
						Label labelPostNumEq = new Label();
						mv.visitJumpInsn(GOTO, labelPostNumEq);
						mv.visitLabel(labelNumEqFalseBr);
						mv.visitInsn(ICONST_0);
						mv.visitLabel(labelPostNumEq);
					}
					default -> {
						throw new IllegalStateException("code gen bug in visitExpressionBinary NUMBER");
					}
				}
			}
//As explained in class example for Invokevirtual
//			methodVisitor.visitVarInsn(ALOAD, 1);
//			methodVisitor.visitInsn(ICONST_3);
//			methodVisitor.visitMethodInsn(INVOKEVIRTUAL,
//					"cop5556fa21/temp/SimpleProgram", "sumXY", "(I)I", false);
//			methodVisitor.visitVarInsn(ISTORE, 2);
			case STRING -> {
				expressionBinary.e0.visit(this, arg);
				expressionBinary.e1.visit(this, arg);
				switch (op){

					case PLUS -> mv.visitMethodInsn(INVOKEVIRTUAL, ownerVirtual, "concat", "("+"Ljava/lang/String;" +")"+"Ljava/lang/String;", false);
					case EQ -> mv.visitMethodInsn(INVOKEVIRTUAL, ownerVirtual, "equals", "("+"Ljava/lang/Object;"+")"+"Z", false);
					case NEQ -> {
						mv.visitMethodInsn(INVOKEVIRTUAL, ownerVirtual, "equals", "("+"Ljava/lang/Object;"+")"+"Z", false);
						mv.visitMethodInsn(INVOKESTATIC, ownerStatic, "not", "(Z)Z", false);
					}
					case LT -> mv.visitMethodInsn(INVOKESTATIC, ownerStatic, "lt", descriptorStatic, false);
					case GT -> mv.visitMethodInsn(INVOKESTATIC, ownerStatic, "gt", descriptorStatic, false);
					case LE -> mv.visitMethodInsn(INVOKESTATIC, ownerStatic, "le", descriptorStatic, false);
					case GE -> mv.visitMethodInsn(INVOKESTATIC, ownerStatic, "ge", descriptorStatic, false);
					default -> throw new IllegalStateException("code gen bug in visitExpressionBinary String");
				}
			}
			default -> {
				throw new IllegalStateException("code gen bug in visitExpressionBinary");
			}
		}
		return null;
	}
//	visitExpressionIdent
//if variable is constant, just load the value
//	otherwise, load address of containing instance (follow the this$n chain if not local) and use GETFIELD to
//	load the value of the variable on top of the stack.
	@Override
	public Object visitExpressionIdent(ExpressionIdent expressionIdent, Object arg) throws PLPException {
		MethodVisitor methodVisitor = (MethodVisitor) arg;
		int check = expressionIdent.getDec() instanceof ConstDec ? 1 : 0;
		switch (check){
			case 1 -> {
				System.out.println("inside case 1");
				methodVisitor.visitLdcInsn(((ConstDec) expressionIdent.getDec()).val);
			}
			case 0 -> {
				methodVisitor.visitVarInsn(ALOAD, 0);
				long  currentDeclartionLvl = expressionIdent.getDec().getNest();
				String currentSuperClass = currentProcedurePath;
				String currentSubClass = new String("");

				long currentNest = expressionIdent.getNest();
				long i = currentNest;
				while(i > currentDeclartionLvl){
					currentSubClass = currentSuperClass;
					int j = currentSubClass.length();
					while(j >= 0){
						if(currentProcedurePath.charAt(j) == '$'){
							currentSuperClass = currentSubClass.substring(0, j);
							break;
						}
						j--;
					}

					String currName = "this$" + (j-1);
					String currDescriptor = "L" + currentSuperClass + ";";
					methodVisitor.visitFieldInsn(GETFIELD, currentSubClass, currName, currDescriptor);
				}
				String currName = String.valueOf(expressionIdent.firstToken.getText());
				String currDescriptor = expressionIdent.getDec().getCurrentDescriptor();
				methodVisitor.visitFieldInsn(GETFIELD, currentSuperClass, currName, currDescriptor);
			}
		}

		return null;
	}

	@Override
	public Object visitExpressionNumLit(ExpressionNumLit expressionNumLit, Object arg) throws PLPException {
		MethodVisitor mv = (MethodVisitor)arg;
		mv.visitLdcInsn(expressionNumLit.getFirstToken().getIntValue());
		return null;
	}

	//Suneet
	@Override
	public Object visitExpressionStringLit(ExpressionStringLit expressionStringLit, Object arg) throws PLPException {
		MethodVisitor mv = (MethodVisitor)arg;
		mv.visitLdcInsn(expressionStringLit.getFirstToken().getStringValue());
		return null;
	}

	//Suneet
	@Override
	public Object visitExpressionBooleanLit(ExpressionBooleanLit expressionBooleanLit, Object arg) throws PLPException {
		MethodVisitor mv = (MethodVisitor)arg;
		mv.visitLdcInsn(expressionBooleanLit.getFirstToken().getBooleanValue());
		return null;
	}
//	visitProcedure
//• create a classWriter object for new class
//• add field for reference to enclosing class (this$n where n is nesting level)
//			• create init method that takes an instance of enclosing class as parameter and initializes this$n,
//			then invokes superclass constructor (java/lang/Object).
//			• Visit block to create run method
//
	@Override
	public Object visitProcedure(ProcDec procDec, Object arg) throws PLPException {
		currentProcedurePath = fullyQualifiedClassName.concat(procDec.getProcedurePath());
		switch (checkProcedureFlag){
			case 1 -> {
				ClassWriter classWriter2 = (ClassWriter) arg;
				classWriter2.visitNestMember(currentProcedurePath);
				classWriter2.visitInnerClass(currentProcedurePath, fullyQualifiedClassName, String.valueOf(procDec.ident.getText()), 0);
				procDec.block.visit(this, classWriter2);
			}
			case 0 -> {
				ClassWriter classWriter2 = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
				String classNameCurrent = fullyQualifiedClassName.concat(procDec.getProcedurePath());
				String classNameParent = new String("");
				int i = classNameCurrent.length()-1;
				while(i >= 0){
					if(className.charAt(i) == '$'){
						classNameParent = classNameCurrent.substring(0, i);
						break;
					}
					i--;
				}
				classWriter2.visit(V18, ACC_SUPER, fullyQualifiedClassName.concat(procDec.getProcedurePath()), null, object, new String[] {"java/lang/Runnable"});
				classWriter2.visitSource(className.concat(".java"), null);
				classWriter2.visitNestHost(fullyQualifiedClassName);
				classWriter2.visitInnerClass(fullyQualifiedClassName.concat(procDec.getProcedurePath()), classNameParent, String.valueOf(procDec.ident.getText()), 0);

				String fieldDesc = "L" + classNameParent + ";";
				FieldVisitor fv = classWriter2.visitField(ACC_FINAL | ACC_SYNTHETIC, "this$" + procDec.getNest(), fieldDesc, null, null);
				fv.visitEnd();

				MethodVisitor mv = classWriter2.visitMethod(0, "<init>", ("(L".concat(classNameParent)).concat(";)V"), null, null);
				mv.visitEnd();

				mv.visitVarInsn(ALOAD, 0);
				mv.visitVarInsn(ALOAD, 1);

				mv.visitFieldInsn(PUTFIELD, fullyQualifiedClassName.concat(procDec.getProcedurePath()), "this$" + procDec.getNest(), ("L".concat(classNameParent)).concat(";"));

				mv.visitVarInsn(ALOAD, 0);

				mv.visitMethodInsn(INVOKESPECIAL, object, "<init>", currDescriptorEmpty, false);
				mv.visitInsn(RETURN);
				mv.visitMaxs(2,2);
				mv.visitEnd();

				currentProcedurePath = fullyQualifiedClassName.concat(procDec.getProcedurePath());
				procDec.block.visit(this, classWriter2);
				int j = currentProcedurePath.length()-1;
				while (j >= 0 ){
					if (currentProcedurePath.charAt(j) == '$'){
						currentProcedurePath = currentProcedurePath.substring(0, j);
						break;
					}
					j--;
				}

				classWriter2.visitEnd();
				byteCodeList.add(new CodeGenUtils.GenClass(fullyQualifiedClassName.concat(procDec.getProcedurePath()), classWriter2.toByteArray()));
			}
		}

		return null;
	}

	@Override
	public Object visitConstDec(ConstDec constDec, Object arg) throws PLPException {
		return null;
	}

	@Override
	public Object visitStatementEmpty(StatementEmpty statementEmpty, Object arg) throws PLPException {
		return null;
	}

//	visitIdent
//• Found only on left side of assignments and input statements.  Assume a value is on top of the
//	stack and generate code to store the value in the variable indicated by the ident.
//• Use nesting level to go up chain of this$n vars for non-local variable.

	@Override
	public Object visitIdent(Ident ident, Object arg) throws PLPException {
		System.out.println("inside visit ident");
		MethodVisitor methodVisitor = (MethodVisitor) arg;
		methodVisitor.visitVarInsn(ALOAD, 0);

		long nestLvl = ident.getDec().getNest();
		long currentNestLvl = ident.getNest();
		System.out.println("current " + currentProcedurePath);
		String currentSuperClass = currentProcedurePath;
		String currentSubClass;

//		String[] directoryList = currentSuperClass.split("$");
//		currentSubClass = directoryList[directoryList.length - 1];

		long i = currentNestLvl;
		System.out.println("i " + i);
		System.out.println("nestLvl " + nestLvl);
		System.out.println("outside while");
		while(i > nestLvl){
			System.out.println("inside while");
			currentSubClass = currentSuperClass;
			int j = currentSubClass.length()-1;
			while (j >= 0){
				if(currentProcedurePath.charAt(j) == '$'){
					currentSuperClass = currentSubClass.substring(0, j);
					break;
				}
				j--;
			}
			System.out.println("before");
			methodVisitor.visitFieldInsn(GETFIELD, currentSubClass, "this$"+(currentNestLvl-1), "L" + currentSuperClass +";");
			System.out.println("first field insn");
			i--;
		}
		methodVisitor.visitInsn(SWAP);
		System.out.println("super " + currentSuperClass);

		methodVisitor.visitFieldInsn(PUTFIELD, currentSuperClass, String.valueOf(ident.firstToken.getText()), ident.getDec().getCurrentDescriptor());
		System.out.println("after putfield");
		return null;
	}

}

