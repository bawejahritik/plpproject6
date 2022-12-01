package edu.ufl.cise.plpfa22;

import edu.ufl.cise.plpfa22.IToken.Kind;

import java.util.ArrayList;

public class Lexer implements ILexer{
    private ArrayList<IToken> tokens = new ArrayList<>();
    private int pos = 0;
    private Kind kind;
    private StringBuffer s = new StringBuffer();
    private StringBuffer s0 = new StringBuffer();
    private StringBuffer characters = new StringBuffer();
    private boolean stringCheck = true;


    private enum State{
        START,
        IN_INDENT,
        IN_NUMLIT,
        IN_STRINGLIT,
        IN_BOOLEANIT,
        HAVE_LT,
        HAVE_GT,
        HAVE_EQ,
        HAVE_COLON,
        HAVE_SLASH
    }

    public Lexer(String input) throws LexicalException {
        int len = input.length();
        //input += '\0';
        int p = 0;
        int sPos = 0;
        int line = 1;
        int col = 1;
        State state = State.START;

        while(p < len){
            char[] chars = input.toCharArray();
            char ch = chars[p];
            //System.out.println(col);
            switch (state){
                case START -> {
                    sPos = p;
                    s = new StringBuffer();
                    switch (ch){
//                        case '\0' -> {
//                            p++;
//                        }
                        case ' ', '\r' -> {
                            p++;
                            col++;
                            //System.out.println("indide space");
                        }

                        case '\t' -> {
                            col += 4;
                            p++;
                        }

                        case '\n' -> {
                            col = 1;
                            line++;
                            p++;
                        }

                        case '.' -> {
                            Token token = new Token(Kind.DOT, Character.toString(ch), new IToken.SourceLocation(line, col), 1);
                            tokens.add(token);
                            p++;
                            col++;
                        }
                        case ',' -> {
                            Token token = new Token(Kind.COMMA, Character.toString(ch), new IToken.SourceLocation(line, col), 1);
                            tokens.add(token);
                            p++;
                            col++;
                        }

                        case ';' -> {
                            Token token = new Token(Kind.SEMI, Character.toString(ch), new IToken.SourceLocation(line, col), 1);
                            tokens.add(token);
                            p++;
                            col++;
                        }

                        case '(' -> {
                            Token token = new Token(Kind.LPAREN, Character.toString(ch), new IToken.SourceLocation(line, col), 1);
                            tokens.add(token);
                            p++;
                            col++;
                        }

                        case ')' -> {
                            Token token = new Token(Kind.RPAREN, Character.toString(ch), new IToken.SourceLocation(line, col), 1);
                            tokens.add(token);
                            p++;
                            col++;
                        }
                        case '+' -> {
                            Token token = new Token(Kind.PLUS, Character.toString(ch), new IToken.SourceLocation(line, col), 1);
                            tokens.add(token);
                            p++;
                            col++;
                        }
                        case '-' -> {
                            Token token = new Token(Kind.MINUS, Character.toString(ch), new IToken.SourceLocation(line, col), 1);
                            tokens.add(token);
                            p++;
                            col++;
                        }

                        //case '*'
                        case '*' -> {
                            Token token = new Token(Kind.TIMES, Character.toString(ch), new IToken.SourceLocation(line, col), 1);
                            tokens.add(token);
                            p++;
                            col++;

                        }

                        //case '/'
                        case '/' -> {
                            state = State.HAVE_SLASH;
                            p++;
                            s.append(ch);
                        }

                        //case '%'
                        case '%' -> {
                            //System.out.println("inside mod");
                            Token token = new Token(Kind.MOD, Character.toString(ch), new IToken.SourceLocation(line, col), 1);
                            tokens.add(token);
                            p++;
                            col++;
                        }

                        //case '?'
                        case '?' -> {
                            Token token = new Token(Kind.QUESTION, Character.toString(ch),new IToken.SourceLocation(line, col), 1);
                            tokens.add(token);
                            p++;
                            col++;
                        }

                        //case '!'
                        case '!' -> {
                            Token token = new Token(Kind.BANG, Character.toString(ch),new IToken.SourceLocation(line, col), 1);
                            tokens.add(token);
                            p++;
                            col++;
                        }

                        // case ':=' -> {
                        //     Token token = new Token(Kind., Character.toString(ch),new IToken.SourceLocation(line, col), 1);
                        //     pos++;
                        //     col++;
                        //     tokens.add(token);
                        // }

                        //case '='
                        case '=' -> {
                            Token token  = new Token(Kind.EQ, Character.toString(ch), new IToken.SourceLocation(line, col), 1);
                            tokens.add(token);
                            p++;
                            col++;
                        }

                        //case '#
                        case '#' -> {
                            Token token = new Token(Kind.NEQ, Character.toString(ch), new IToken.SourceLocation(line, col), 1);
                            tokens.add(token);
                            p++;
                            col++;
                        }

                        case ':' -> {
                            state = State.HAVE_COLON;
                            p++;
                            s.append(ch);
                        }

                        case '<' -> {
                            state = State.HAVE_LT;
                            p++;
                            s.append(ch);
                        }

                        // case '>'
                        case '>' ->{
                            state = State.HAVE_GT;
                            p++;
                            s.append(ch);
                        }

                        case '0' -> {
                            s.append(ch);
                            Token token= new Token(Kind.NUM_LIT, s.toString(), new IToken.SourceLocation(line, col), 0);
                            tokens.add(token);
                            p++;
                            col++;
                        }

                        case '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                            state = State.IN_NUMLIT;
                            s.append(ch);
                            if(p+1 == len){
                                try{
                                    Integer.parseInt(s.toString());
                                    Token token = new Token(Kind.NUM_LIT, s.toString(), new IToken.SourceLocation(line, col), s.length());
                                    tokens.add(token);
                                    col += s.length();
                                    state = State.START;
                                }catch (Exception NumberFormatException){
                                    Token token = new Token(Kind.ERROR, s.toString(), new IToken.SourceLocation(line, col), s.length());
                                    tokens.add(token);
                                    col += s.length();
                                    state = State.START;
                                    return;
                                }
                            }
                            p++;
                        }

                        case '"' -> {
                            state = State.IN_STRINGLIT;
                            kind = Kind.STRING_LIT;
                            stringCheck = false;
                            //characters.append(ch);
//                            Token token = new Token(Kind.QUOTE, s.toString(), new IToken.SourceLocation(line, col), s.length());
//                            tokens.add(token);
                            p++;
                        }

                        default -> {
                            if(('a' <= ch) && (ch <= 'z') || ('A' <= ch) && (ch <= 'Z') || ch == '_' || ch == '$') {
                                state = State.IN_INDENT;
                                s.append(ch);
                                if(p+1 == len){
                                    checkKW(s.toString(), line, col);
                                }
                                p++;
                            }
//                            else if((0 <= Character.getNumericValue(ch) && Character.getNumericValue(ch) <= 9)){
//                                System.out.println(ch);
//                                state = State.IN_NUMLIT;
//                                s.append(ch);
//                                if(p+1 == len){
//                                    try{
//                                        Integer.parseInt(s.toString());
//                                        Token token = new Token(Kind.NUM_LIT, s.toString(), new IToken.SourceLocation(line, col), s.length());
//                                        tokens.add(token);
//                                        col += s.length();
//                                        state = State.START;
//                                    }catch (Exception NumberFormatException){
//                                        Token token = new Token(Kind.ERROR, s.toString(), new IToken.SourceLocation(line, col), s.length());
//                                        tokens.add(token);
//                                        col += s.length();
//                                        state = State.START;
//                                        return;
//                                    }
//                                }
//                                p++;
//                            }
                            else {
                                Token token = new Token(Kind.ERROR, Character.toString(ch), new IToken.SourceLocation(line, col), 1);
                                tokens.add(token);
                                p++;
                                state = State.START;
                            }
                        }
                    }
                }
                case HAVE_SLASH -> {
                    if(ch == '/'){
                        while(ch != '\n'){
                            p++;
                            col++;
                            ch = chars[p];
                        }
                    }else{
                        Token token = new Token(Kind.DIV, Character.toString('/'), new IToken.SourceLocation(line, col), 1);
                        //p++;
                        //col++;
                        tokens.add(token);
                    }
                    state = State.START;
                }
                case HAVE_COLON -> {
                    switch (ch) {
                        case '=' -> {
                            s.append(ch);
                            Token token = new Token(Kind.ASSIGN, s.toString(), new IToken.SourceLocation(line, col), 2);
                            p++;
                            col += 2;
                            tokens.add(token);
                            state = State.START;
                        }
                        default -> {
                            //Doubt what will only ':' be categorized as ?
                            Token token = new Token(Kind.ERROR, s.toString(), new IToken.SourceLocation(line, col), 1);
                        }
                    }
                }

                case HAVE_LT -> {
                    switch (ch) {
                        case '=' -> {
                            s.append(ch);
                            Token token = new Token(Kind.LE, s.toString(), new IToken.SourceLocation(line, col), 2);
                            col += 2;
                            p++;
                            tokens.add(token);
                            state =State.START;
                        }
                        default -> {
                            Token token = new Token(Kind.LT, s.toString(), new IToken.SourceLocation(line, col), 1);
                            col++;
                            tokens.add(token);
                            state = State.START;
                        }
                    }
                }

                //case HAVE_GT
                case HAVE_GT -> {
                    switch (ch) {
                        case '=' -> {
                            s.append(ch);
                            Token token = new Token(Kind.GE, s.toString(), new IToken.SourceLocation(line, col), 2);
                            col += 2;
                            p++;
                            tokens.add(token);
                            state = State.START;
                        }
                        default -> {
                            Token token = new Token(Kind.GT, s.toString(), new IToken.SourceLocation(line, col), 1);
                            col++;
                            tokens.add(token);
                            state = State.START;
                        }
                    }
                }

                case IN_STRINGLIT -> {
                    //System.out.println(p);
//                    StringBuffer escape = new StringBuffer();
                    ch = chars[p];
                    if(ch == '\\'){
                        p++;
                        //s.append(ch);
                        ch = chars[p];
                        //characters.append('\\');
                        ch = chars[p];
                        switch (ch){
                            case 'b' -> {
                                p++;
                                //characters.append(ch);
                                characters.append('\b');
                            }
                            case 'n' -> {
                                p++;
                                //characters.append(ch);
                                characters.append('\n');
                            }
                            case 't' -> {
                                p++;
                                //characters.append(ch);
                                characters.append('\t');
                            }
                            case 'f' -> {
                                p++;
                                //characters.append(ch);
                                characters.append('\f');
                            }
                            case 'r' -> {
                                p++;
                                //characters.append(ch);
                                characters.append('\r');
                            }
                            case '"' -> {
                                p++;
                                characters.append('\"');
//                                char x = '\"';
//                                Token token = new Token(Kind.QUOTE, Character.toString(x), new IToken.SourceLocation(line, col), s.length());
//                                tokens.add(token);
                                //s0.append('\"');
                            }
                            case '\'' -> {
                                p++;
                                //characters.append(ch);
                                characters.append('\'');
                            }
                            case '\\' -> {
                                p++;
                                //characters.append(ch);
                                characters.append('\\');
                            }
                            case '\u0000' -> {
                                p++;
                            }
                            default -> {
                                Token token = new Token(Kind.ERROR, characters.toString(), new IToken.SourceLocation(line, col), characters.length());
                                tokens.add(token);
                                col += characters.length();
                                characters = new StringBuffer("");
                                state = State.START;
                            }
                        }
                    }else if(ch == '"'){
                        p++;
                        //col++;
                        stringCheck = true;
                        //characters.append(ch);
                        //characters.append("\"");
                        Token token = new Token(Kind.STRING_LIT, characters.toString(), new IToken.SourceLocation(line, col), characters.length());
                        tokens.add(token);
                        col += characters.length();
                        characters = new StringBuffer("");
                        s0 = new StringBuffer("");
                        state = State.START;
                    }else{
                        p++;
                        characters.append(ch);
                        col++;
                        s0.append(ch);
                    }
                }

                //case IN_NUMLIT
                case IN_NUMLIT -> {
                    //System.out.println("inside " + ch);
                    switch(ch){
//                        case '0' -> {
//                            Token token = new Token(Kind.NUM_LIT, Character.toString(ch), new IToken.SourceLocation(line, col), 1);
//                            tokens.add(token);
//                            col++;
//                            state = State.START;
//                            p++;
//                        }

                        case '0','1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                            s.append(ch);
                            if(p + 1 == len){
                                try{
                                    Integer.parseInt(s.toString());
                                    Token token = new Token(Kind.NUM_LIT, s.toString(), new IToken.SourceLocation(line, col), s.length());
                                    tokens.add(token);
                                    col += s.length();
                                    state = State.START;
                                }catch (Exception NumberFormatException){
                                    Token token = new Token(Kind.ERROR, s.toString(), new IToken.SourceLocation(line, col), s.length());
                                    tokens.add(token);
                                    col += s.length();
                                    state = State.START;
                                    return;
                                }
                            }
                            p++;
                        }

                        default -> {
                            try{
                                Integer.parseInt(s.toString());
                                Token token = new Token(Kind.NUM_LIT, s.toString(), new IToken.SourceLocation(line, col), s.length());
                                tokens.add(token);
                                col += s.length();
                                state = State.START;
                            }catch (Exception NumberFormatException){
                                Token token = new Token(Kind.ERROR, s.toString(), new IToken.SourceLocation(line, col), s.length());
                                tokens.add(token);
                                col += s.length();
                                state = State.START;
                                return;
                            }
                        }
                    }
                }
                //case IN_INDENT
                case IN_INDENT -> {
                    //System.out.println("ch:@" + ch);
                    if((('a' <= ch) && (ch <= 'z')) || (('A' <= ch) && (ch <= 'Z')) || (('0' <= ch) && (ch <= '9')) || ch == '_' || ch == '$'){
                        s.append(ch);
                        ch = chars[p];
                        if(p+1 == len){
                            checkKW(s.toString(), line, col);
                        }
                        p++;
                    }else{
                        state = State.START;
                        checkKW(s.toString(), line, col);
                        col += s.length();
                    }
                }
                default -> throw new IllegalStateException("lexer bug");
            }
        }
        //System.out.println("hit " + p + " " + col);
        if(!stringCheck) throw new LexicalException();
        Token token = new Token(Kind.EOF, input, new IToken.SourceLocation(line, col), 0);
        tokens.add(token);
    }
    public void checkKW(String input, int line, int col) {
        switch(input){
            case "TRUE", "FALSE" -> {
                Token token = new Token(Kind.BOOLEAN_LIT, input, new IToken.SourceLocation(line, col), input.length());
                tokens.add(token);
            }
            case "CONST" -> {
                Token token = new Token(Kind.KW_CONST, input, new IToken.SourceLocation(line, col), input.length());
                tokens.add(token);
            }
            case "VAR" -> {
                Token token = new Token(Kind.KW_VAR, input, new IToken.SourceLocation(line, col), input.length());
                tokens.add(token);
            }
            case "PROCEDURE" -> {
                Token token = new Token(Kind.KW_PROCEDURE, input, new IToken.SourceLocation(line, col), input.length());
                tokens.add(token);
            }
            case "CALL" -> {
                Token token = new Token(Kind.KW_CALL, input, new IToken.SourceLocation(line, col), input.length());
                tokens.add(token);
            }
            case "BEGIN" -> {
                Token token = new Token(Kind.KW_BEGIN, input, new IToken.SourceLocation(line, col), input.length());
                tokens.add(token);
            }
            case "END" -> {
                Token token = new Token(Kind.KW_END, input, new IToken.SourceLocation(line, col), input.length());
                tokens.add(token);
            }
            case "IF" -> {
                Token token = new Token(Kind.KW_IF, input, new IToken.SourceLocation(line, col), input.length());
                tokens.add(token);
            }
            case "THEN" -> {
                Token token = new Token(Kind.KW_THEN, input, new IToken.SourceLocation(line, col), input.length());
                tokens.add(token);
            }
            case "WHILE" -> {
                Token token = new Token(Kind.KW_WHILE, input, new IToken.SourceLocation(line, col), input.length());
                tokens.add(token);
            }
            case "DO" -> {
                Token token = new Token(Kind.KW_DO, input, new IToken.SourceLocation(line, col), input.length());
                tokens.add(token);
            }

            default -> {
                Token token = new Token(Kind.IDENT, input, new IToken.SourceLocation(line, col), input.length());
                tokens.add(token);
            }
        }
    }

    @Override
    public IToken next() throws LexicalException {
        IToken next = tokens.get(pos);
        pos++;
        if (next.getKind() == Kind.ERROR){
            throw new LexicalException("Lexical Exception");
        }
        else{
            return next;
        }
    }

    @Override
    public IToken peek() throws LexicalException {
        return tokens.get(pos);
    }
}
