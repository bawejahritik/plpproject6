/**  This code is provided for solely for use of students in the course COP5556 Programming Language Principles at the 
 * University of Florida during the Fall Semester 2022 as part of the course project.  No other use is authorized. 
 */

package edu.ufl.cise.plpfa22.ast;

import edu.ufl.cise.plpfa22.IToken;
import edu.ufl.cise.plpfa22.ast.Types.Type;

public abstract class Declaration extends ASTNode {
	
	Type type;
	String procedurePath;

	public Declaration(IToken firstToken) {
		super(firstToken);
	}

	public Type getType() {
		return type;
	}

	public String getCurrentDescriptor(){
		switch (type){
			case BOOLEAN -> {
				return "Z";
			}
			case NUMBER -> {
				return "I";
			}
			default -> {
				return "Ljava/lang/String;";
			}
		}
	}

	public void setType(Type type) {
		this.type = type;
	}

	int nest;
	public void setNest(int nest) {
		this.nest = nest;
	}
	public int getNest() {
		return nest;
	}

	public String getProcedurePath(){
		return procedurePath;
	}

	public void setProcedurePath(String procedurePath){
		this.procedurePath = procedurePath;
	}


}
