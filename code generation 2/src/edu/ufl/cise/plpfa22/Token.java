package edu.ufl.cise.plpfa22;
import edu.ufl.cise.plpfa22.IToken;

public class Token implements IToken {

    public Token(Kind kind, String input, SourceLocation pos, int length) {
        //if(kind == Kind.STRING_LIT) input = "\"" + input + "\"";
        this.kind = kind;
        this.input = input;
        this.pos = pos;
        this.length = length;
    }

    final Kind kind;
    final String input;
    final SourceLocation pos;
    final int length;


    //Hritik
    @Override
    public Kind getKind() {
        return kind;
    }

    //Suneet
    @Override
    public char[] getText() {
        return input.toCharArray();
    }

    //Hritik
    @Override
    public SourceLocation getSourceLocation() {
        return pos;
    }
    //Suneet
    @Override
    public int getIntValue() {
        if(kind == Kind.NUM_LIT){
            return Integer.parseInt(input);
        }
        else{
            return 0;
        }
    }
    //Hritik
    @Override
    public boolean getBooleanValue() {
        if(kind == Kind.BOOLEAN_LIT) return Boolean.parseBoolean(input);
        else return false;
    }
    //Suneet
    @Override
    public String getStringValue() {
        return input;
    }
}
