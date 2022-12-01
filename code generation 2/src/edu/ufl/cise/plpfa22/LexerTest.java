/**  This code is provided for solely for use of students in the course COP5556 Programming Language Principles at the 
 * University of Florida during the Fall Semester 2022 as part of the course project.  No other use is authorized. 
 */

package edu.ufl.cise.plpfa22;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
//check
import edu.ufl.cise.plpfa22.CompilerComponentFactory;
import edu.ufl.cise.plpfa22.ILexer;
import edu.ufl.cise.plpfa22.IToken;
import edu.ufl.cise.plpfa22.IToken.Kind;
import edu.ufl.cise.plpfa22.LexicalException;

class LexerTest {


	/*** Useful functions ***/
	ILexer getLexer(String input) throws LexicalException {
		return CompilerComponentFactory.getLexer(input);
	}

	//makes it easy to turn output on and off (and less typing than System.out.println)
	static final boolean VERBOSE = true;
	void show(Object obj) {
		if(VERBOSE) {
			System.out.println(obj);
		}
	}

	//check that this token has the expected kind
	void checkToken(IToken t, Kind expectedKind) {
		assertEquals(expectedKind, t.getKind());
	}

	//check that the token has the expected kind and position
	void checkToken(IToken t, Kind expectedKind, int expectedLine, int expectedColumn){
		assertEquals(expectedKind, t.getKind());
		assertEquals(new IToken.SourceLocation(expectedLine,expectedColumn), t.getSourceLocation());
	}

	//check that this token is an IDENT and has the expected name
	void checkIdent(IToken t, String expectedName){
		assertEquals(Kind.IDENT, t.getKind());
		assertEquals(expectedName, String.valueOf(t.getText()));
	}

	//check that this token is an IDENT, has the expected name, and has the expected position
	void checkIdent(IToken t, String expectedName, int expectedLine, int expectedColumn){
		checkIdent(t,expectedName);
		assertEquals(new IToken.SourceLocation(expectedLine,expectedColumn), t.getSourceLocation());
	}


	//check that this token is an NUM_LIT with expected int value
	void checkInt(IToken t, int expectedValue) {
		assertEquals(Kind.NUM_LIT, t.getKind());
		assertEquals(expectedValue, t.getIntValue());
	}

	//check that this token  is an NUM_LIT with expected int value and position
	void checkInt(IToken t, int expectedValue, int expectedLine, int expectedColumn) {
		checkInt(t,expectedValue);
		assertEquals(new IToken.SourceLocation(expectedLine,expectedColumn), t.getSourceLocation());
	}

	//check that this token is the EOF token
	void checkEOF(IToken t) {
		checkToken(t, Kind.EOF);
	}

	/***Tests****/

	//The lexer should add an EOF token to the end.
	@Test
	void testEmpty() throws LexicalException {
		String input = "";
		show(input);
		ILexer lexer = getLexer(input);
		show(lexer);
		checkEOF(lexer.next());
	}

	//	identifier.
	@Test
	void testID() throws LexicalException {
		String input = """
					ad23
					""";
		show(input);
		ILexer lexer = getLexer(input);
		show(lexer);
		checkToken(lexer.next(), Kind.IDENT, 1,1);
		checkEOF(lexer.next());
	}

	//A couple of single character tokens
	@Test
	void testSingleChar0() throws LexicalException {
		String input = """
				+
				-
				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.PLUS, 1,1);
		checkToken(lexer.next(), Kind.MINUS, 2,1);
		checkEOF(lexer.next());
	}

	//A couple of single character tokens
	@Test
	void testSingleChar1() throws LexicalException {
		String input = """
				=
				=
				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.EQ, 1,1);
		checkToken(lexer.next(), Kind.EQ, 2,1);
//		checkToken(lexer.next(), Kind.MINUS, 2,1);
		checkEOF(lexer.next());
	}

	@Test
	void testblock1() throws LexicalException {
		String input = """
				CONST a = 3, b = TRUE, c = "hello";
				.
				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.KW_CONST);
		checkToken(lexer.next(), Kind.IDENT);
		checkToken(lexer.next(), Kind.EQ);
		checkToken(lexer.next(), Kind.NUM_LIT);
		checkToken(lexer.next(), Kind.COMMA);
		checkToken(lexer.next(), Kind.IDENT);
		checkToken(lexer.next(), Kind.EQ);
		checkToken(lexer.next(), Kind.BOOLEAN_LIT);
		checkToken(lexer.next(), Kind.COMMA);
		checkToken(lexer.next(), Kind.IDENT);
		checkToken(lexer.next(), Kind.EQ);
		checkToken(lexer.next(), Kind.STRING_LIT);
		checkToken(lexer.next(), Kind.SEMI);
		checkToken(lexer.next(), Kind.DOT);
//		checkToken(lexer.next(), Kind.MINUS, 2,1);
		checkEOF(lexer.next());
	}

	//comments should be skipped
	@Test
	void testComment0() throws LexicalException {
		//Note that the quotes around "This is a string" are passed to the lexer.
		String input = """
				"This is a string"
				// this is a comment
				*
				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.STRING_LIT, 1,1);
		checkToken(lexer.next(), Kind.TIMES, 3,1);
		checkEOF(lexer.next());
	}

	//Example for testing input with an illegal character
	@Test
	void testError0() throws LexicalException {
		String input = """
				abc
				@
				""";
		show(input);
		ILexer lexer = getLexer(input);
		//this check should succeed
		checkIdent(lexer.next(), "abc");
		//this is expected to throw an exception since @ is not a legal
		//character unless it is part of a string or comment
		assertThrows(LexicalException.class, () -> {
			@SuppressWarnings("unused")
			IToken token = lexer.next();
		});
	}

	//Several identifiers to test positions
	@Test
	public void testIdent0() throws LexicalException {
		String input = """
				abc
				  def
				     ghi

				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkIdent(lexer.next(), "abc", 1,1);
		checkIdent(lexer.next(), "def", 2,3);
		checkIdent(lexer.next(), "ghi", 3,6);
		checkEOF(lexer.next());
	}


	@Test
	public void testIdenInt() throws LexicalException {
		String input = """
				a123 456b
				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkIdent(lexer.next(), "a123", 1,1);
		checkInt(lexer.next(), 456, 1,6);
		checkIdent(lexer.next(), "b",1,9);
		checkEOF(lexer.next());
	}


	//Example showing how to handle number that are too big.
	@Test
	public void testIntTooBig() throws LexicalException {
		String input = """
				42
				99999999999999999999999999999999999999999999999999999999999999999999999
				""";
		ILexer lexer = getLexer(input);
		checkInt(lexer.next(),42);
		Exception e = assertThrows(LexicalException.class, () -> {
			lexer.next();
		});
	}



	@Test
	public void testEscapeSequences0() throws LexicalException {
		String input = "\"\\b \\t \\n \\f \\r \"";
		show(input);
		ILexer lexer = getLexer(input);
		IToken t = lexer.next();
		String val = t.getStringValue();
		String expectedStringValue = "\b \t \n \f \r ";
		assertEquals(expectedStringValue, val);
		String text = String.valueOf(t.getText());
		String expectedText = "\"\\b \\t \\n \\f \\r \"";
		assertEquals(expectedText,text);
	}

	@Test
	public void testEscapeSequences1() throws LexicalException {
		String input = "   \" ...  \\\"  \\\'  \\\\  \"";
		show(input);
		ILexer lexer = getLexer(input);
		IToken t = lexer.next();
		String val = t.getStringValue();
		String expectedStringValue = " ...  \"  \'  \\  ";
		assertEquals(expectedStringValue, val);
		String text = String.valueOf(t.getText());
		String expectedText = "\" ...  \\\"  \\\'  \\\\  \""; //almost the same as input, but white space is omitted
		assertEquals(expectedText,text);
	}



	//series
	@Test
	void testSingleChar2() throws LexicalException {
		String input = """
				+
				-
				 =
				%
				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.PLUS, 1,1);
		checkToken(lexer.next(), Kind.MINUS, 2,1);
		checkToken(lexer.next(), Kind.EQ, 3,2);
		checkToken(lexer.next(), Kind.MOD, 4,1);
		checkEOF(lexer.next());
	}

	//Mod
	@Test
	void testSingleMod() throws LexicalException {
		String input = """
				%
				/
				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.MOD, 1,1);
		checkToken(lexer.next(), Kind.DIV, 2,1);
		checkEOF(lexer.next());
	}

	//booleans
	//check that this token is an BOOLEAN_LIT with expected value
	void checkBoolean(IToken t, boolean expectedValue) {
		assertEquals(Kind.BOOLEAN_LIT, t.getKind());
		assertEquals(expectedValue, t.getBooleanValue());
	}

	//boolian
	@Test
	public void testBooleans() throws LexicalException {
		String input = """
          TRUE
          FALSE
          a123
          """;
		ILexer lexer = getLexer(input);
		checkBoolean(lexer.next(), true);
		checkBoolean(lexer.next(), false);
		checkIdent(lexer.next(), "a123", 3,1);
		checkEOF(lexer.next());
	}


	//Mix of ID's, Num_lit, comments, string_lit's
	@Test
	public void testIDNNUM() throws LexicalException {
		String input = """
          df123 345 g546 IF
          //next is string
           "Hello, World"
          """;
		ILexer lexer = getLexer(input);
		checkIdent(lexer.next(), "df123", 1,1);
		checkInt(lexer.next(), 345, 1,7);
		checkIdent(lexer.next(), "g546", 1,11);
		checkToken(lexer.next(), Kind.KW_IF, 1,16);
		checkToken(lexer.next(), Kind.STRING_LIT, 3,2);
		checkEOF(lexer.next());
	}

	//. , ; ( ) + - * / % ? ! := = # < <= > >=
	@Test
	public void testAllSymmbols() throws LexicalException {
		String input ="""
          . , ; ( ) + - * / %
          //next is line 3
          ? ! := = # < <= > >=
          """;

		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.DOT, 1,1);
		checkToken(lexer.next(), Kind.COMMA, 1,3);
		checkToken(lexer.next(), Kind.SEMI, 1,5);
		checkToken(lexer.next(), Kind.LPAREN, 1,7);
		checkToken(lexer.next(), Kind.RPAREN, 1,9);
		checkToken(lexer.next(), Kind.PLUS, 1,11);
		checkToken(lexer.next(), Kind.MINUS, 1,13);
		checkToken(lexer.next(), Kind.TIMES, 1,15);
		checkToken(lexer.next(), Kind.DIV, 1,17);
		checkToken(lexer.next(), Kind.MOD, 1,19);
		checkToken(lexer.next(), Kind.QUESTION, 3,1);
		checkToken(lexer.next(), Kind.BANG, 3,3);
		checkToken(lexer.next(), Kind.ASSIGN, 3,5);
		checkToken(lexer.next(), Kind.EQ, 3,8);
		checkToken(lexer.next(), Kind.NEQ, 3,10);
		checkToken(lexer.next(), Kind.LT, 3,12);
		checkToken(lexer.next(), Kind.LE, 3,14);
		checkToken(lexer.next(), Kind.GT, 3,17);
		checkToken(lexer.next(), Kind.GE, 3,19);
		checkEOF(lexer.next());
	}


	//reserved words
//	@Test
//	public void testAllreserved() throws LexicalException {
//		String input ="""
//          CONSTVARPROCEDURE
//          //next is line 3
//
//          """;
//
//		ILexer lexer = getLexer(input);
//		checkToken(lexer.next(), Kind.KW_CONST, 1,1);
//		checkToken(lexer.next(), Kind.KW_VAR, 1,6);
//		checkToken(lexer.next(), Kind.KW_PROCEDURE, 1,9);
//		checkEOF(lexer.next());
//	}

	//reserved words
	@Test
	public void testAllreserved1() throws LexicalException {
		String input ="""
          CONST VAR PROCEDURE
          		CALL BEGIN END
          			//next is line 3
          			IF THEN WHILE DO
          
          """;

		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.KW_CONST, 1,1);
		checkToken(lexer.next(), Kind.KW_VAR, 1,7);
		checkToken(lexer.next(), Kind.KW_PROCEDURE, 1,11);
		checkToken(lexer.next(), Kind.KW_CALL, 2,3);
		checkToken(lexer.next(), Kind.KW_BEGIN, 2,8);
		checkToken(lexer.next(), Kind.KW_END, 2,14);
		checkToken(lexer.next(), Kind.KW_IF, 4,4);
		checkToken(lexer.next(), Kind.KW_THEN, 4,7);
		checkToken(lexer.next(), Kind.KW_WHILE, 4,12);
		checkToken(lexer.next(), Kind.KW_DO, 4,18);
		checkEOF(lexer.next());
	}

	//12+3
	@Test
	public void testNoSpace() throws LexicalException {
		String input ="""
          12+3
          """;

		ILexer lexer = getLexer(input);
		checkInt(lexer.next(), 12, 1, 1);
		checkToken(lexer.next(), Kind.PLUS, 1, 3 );
		checkInt(lexer.next(), 3, 1, 4);
		checkEOF(lexer.next());
	}

	//Test 7
	void checkString(IToken t, String expectedValue, int expectedLine, int expectedColumn) {
		assertEquals(Kind.STRING_LIT, t.getKind());
		assertEquals(expectedValue, t.getStringValue());
		assertEquals(new IToken.SourceLocation(expectedLine,expectedColumn), t.getSourceLocation());
	}

	@Test
	public void testStringLineNum() throws LexicalException {
		String input = """
        "Hello\\nWorld"
        "Hello\\tAgain"
        """;
		show(input);
		ILexer lexer = getLexer(input);
		// escape char within string affects line number
		checkString(lexer.next(), "Hello\nWorld", 1, 1);
		checkString(lexer.next(), "Hello\tAgain", 2, 1);
		checkEOF(lexer.next());
	}

	//Test 8
	@Test
	void testAllChars() throws LexicalException {
		String input = """
				.,; ()+-*/%?!:==#<<=>>=
				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.DOT, 1,1);
		checkToken(lexer.next(), Kind.COMMA, 1,2);
		checkToken(lexer.next(), Kind.SEMI, 1,3);
		checkToken(lexer.next(), Kind.LPAREN, 1,5);
		checkToken(lexer.next(), Kind.RPAREN, 1,6);
		checkToken(lexer.next(), Kind.PLUS, 1,7);
		checkToken(lexer.next(), Kind.MINUS, 1,8);
		checkToken(lexer.next(), Kind.TIMES, 1,9);
		checkToken(lexer.next(), Kind.DIV, 1,10);
		checkToken(lexer.next(), Kind.MOD, 1,11);
		checkToken(lexer.next(), Kind.QUESTION, 1,12);
		checkToken(lexer.next(), Kind.BANG, 1,13);
		checkToken(lexer.next(), Kind.ASSIGN, 1,14);
		checkToken(lexer.next(), Kind.EQ, 1,16);
		checkToken(lexer.next(), Kind.NEQ, 1,17);
		checkToken(lexer.next(), Kind.LT, 1,18);
		checkToken(lexer.next(), Kind.LE, 1,19);
		checkToken(lexer.next(), Kind.GT, 1,21);
		checkToken(lexer.next(), Kind.GE, 1,22);
		checkEOF(lexer.next());
	}

	//Test 9
	@Test
// make sure your program does not confuse comments with divide
	void testCommentWithDiv() throws LexicalException {
		String input = """
				///
				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkEOF(lexer.next());
	}

	//	Test 10
	@Test
// nonzero numbers can’t start with 0
	public void testNumberStartWithZero() throws LexicalException {
		String input = """
				010
				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkInt(lexer.next(), 0, 1,1);
		checkInt(lexer.next(), 10, 1,2);
		checkEOF(lexer.next());
	}

	//Test 11
	@Test
	public void testKeywordBacktoBack() throws LexicalException {
		String input = """
				DOWHILE
				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkIdent(lexer.next(), "DOWHILE", 1, 1);
		checkEOF(lexer.next());
	}

	//Test 12
	@Test
	public void testInvalidIdent() throws LexicalException {
		String input = """
				$valid_123
				valid_and_symbol+
				invalid^
				""";
		show(input);
		ILexer lexer = getLexer(input);
// all good
		checkIdent(lexer.next(), "$valid_123", 1,1);
// broken up into an ident and a plus
		checkIdent(lexer.next(), "valid_and_symbol", 2,1);
		checkToken(lexer.next(), Kind.PLUS, 2, 17);
// broken up into a valid ident and an invalid one (throws ex)
		checkIdent(lexer.next(), "invalid", 3,1);
		assertThrows(LexicalException.class, () -> {
			lexer.next();
		});
	}

	//Test 13
	@Test
	public void testUnterminatedString() throws LexicalException {
		String input = """
				"unterminated
				""";
		ILexer lexer = getLexer(input);
		assertThrows(LexicalException.class, () -> {
			lexer.next();
		});
	}

	//Test 14
	@Test
	public void testInvalidEscapeSequence() throws LexicalException {
		String input = """
  	"esc\\"
  				""";
		show(input);
		ILexer lexer = getLexer(input);
		//checkIdent(lexer.next(), "invalid", 1,1);
		assertThrows(LexicalException.class, () -> {
			lexer.next();
		});
	}

//	//Test 14
//	@Test
//	public void testInvalidEscapeSequence1() throws LexicalException {
//		String input = """
//				"esc\\"
//				""";
//		show(input);
//		ILexer lexer = getLexer(input);
//		checkIdent(lexer.next(), "invalid", 1,1);
//		assertThrows(LexicalException.class, () -> {
//			lexer.next();
//		});
//	}

	//Test 15
	@Test
	public void testLongInput0() throws LexicalException {
		String input = """
        VAR x = 0;
        VAR y = TRUE;
        VAR z = "a";
        DO
            x = x + 1;
            y = !y;
            z = z + "a";
        WHILE (x < 10)
        """;
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.KW_VAR, 1, 1);
		checkIdent(lexer.next(), "x", 1, 5);
		checkToken(lexer.next(), Kind.EQ, 1, 7);
		checkInt(lexer.next(), 0, 1, 9);
		checkToken(lexer.next(), Kind.SEMI, 1, 10);

		checkToken(lexer.next(), Kind.KW_VAR, 2, 1);
		checkIdent(lexer.next(), "y", 2, 5);
		checkToken(lexer.next(), Kind.EQ, 2, 7);
//		checkBool(lexer.next(), true, 2, 9);
		checkBoolean(lexer.next(), true);
		checkToken(lexer.next(), Kind.SEMI, 2, 13);

		checkToken(lexer.next(), Kind.KW_VAR, 3, 1);
		checkIdent(lexer.next(), "z", 3, 5);
		checkToken(lexer.next(), Kind.EQ, 3, 7);
		checkString(lexer.next(), "a", 3, 9);
		checkToken(lexer.next(), Kind.SEMI, 3, 12);

		checkToken(lexer.next(), Kind.KW_DO, 4, 1);

		checkIdent(lexer.next(), "x", 5, 5);
		checkToken(lexer.next(), Kind.EQ, 5, 7);
		checkIdent(lexer.next(), "x", 5, 9);
		checkToken(lexer.next(), Kind.PLUS, 5, 11);
		checkInt(lexer.next(), 1, 5, 13);
		checkToken(lexer.next(), Kind.SEMI, 5, 14);

		checkIdent(lexer.next(), "y", 6, 5);
		checkToken(lexer.next(), Kind.EQ, 6, 7);
		checkToken(lexer.next(), Kind.BANG, 6, 9);
		checkIdent(lexer.next(), "y", 6, 10);
		checkToken(lexer.next(), Kind.SEMI, 6, 11);

		checkIdent(lexer.next(), "z", 7, 5);
		checkToken(lexer.next(), Kind.EQ, 7, 7);
		checkIdent(lexer.next(), "z", 7, 9);
		checkToken(lexer.next(), Kind.PLUS, 7, 11);
		checkString(lexer.next(), "a", 7, 13);
		checkToken(lexer.next(), Kind.SEMI, 7, 16);

		checkToken(lexer.next(), Kind.KW_WHILE, 8, 1);
		checkToken(lexer.next(), Kind.LPAREN, 8, 7);
		checkIdent(lexer.next(), "x", 8, 8);
		checkToken(lexer.next(), Kind.LT, 8, 10);
		checkInt(lexer.next(), 10, 8, 12);
		checkToken(lexer.next(), Kind.RPAREN, 8, 14);

		checkEOF(lexer.next());
	}


//Test 16


	// colon cannot be followed by anything but = sign
	@Test
	void testColon() throws LexicalException {
		String input = """
        foo
        :bar
                """;
		show(input);
		ILexer lexer = getLexer(input);
		checkIdent(lexer.next(), "foo");
		assertThrows(LexicalException.class, () -> {
			@SuppressWarnings("unused")
			IToken token = lexer.next();
		});
	}




	//Test 17
//to test correct function of newline in string
	@Test
	void stringSpaces() throws LexicalException
	{

		String input = """
   			 "Line 1 \n"
   			 "Line 3 \\n"
   			 "Line 4"
   			 "Column\t""Column\\t"abc
   			 """;


		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.STRING_LIT, 1,1);
		checkToken(lexer.next(), Kind.STRING_LIT, 3,1);
		checkToken(lexer.next(), Kind.STRING_LIT, 4,1);
		checkToken(lexer.next(), Kind.STRING_LIT, 5,1);
		checkToken(lexer.next(), Kind.STRING_LIT, 5,10);
		checkToken(lexer.next(), Kind.IDENT, 5,20);
	}



	// 13 14 15 16 17 18 19 20 21 1 2 3 4


	//	Test 1 # REMOVED A SPACE
//	String input = "0 1 2 3 4 ";
//
	@Test
	void test1() throws LexicalException
	{

		String input = """
		   0 1 2 3 4 
				 """;


		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.NUM_LIT, 1,1);
		checkToken(lexer.next(), Kind.NUM_LIT, 1,3);
		checkToken(lexer.next(), Kind.NUM_LIT, 1,5);
		checkToken(lexer.next(), Kind.NUM_LIT, 1,7);
		checkToken(lexer.next(), Kind.NUM_LIT, 1,9);
		checkEOF(lexer.next());
	}
	@Test
	void test11() throws LexicalException
	{

		String input = "0 1 2 3 4 ";


		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.NUM_LIT, 1,1);
		checkToken(lexer.next(), Kind.NUM_LIT, 1,3);
		checkToken(lexer.next(), Kind.NUM_LIT, 1,5);
		checkToken(lexer.next(), Kind.NUM_LIT, 1,7);
		checkToken(lexer.next(), Kind.NUM_LIT, 1,9);
		checkEOF(lexer.next());
	}







	//
//	Test 2 # REMOVED A SPACE
//	String input = "5 6 7 8 9 ";
//
	@Test
	void test2() throws LexicalException
	{

		String input = "5 6 7 8 9 ";


		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.NUM_LIT, 1,1);
		checkToken(lexer.next(), Kind.NUM_LIT, 1,3);
		checkToken(lexer.next(), Kind.NUM_LIT, 1,5);
		checkToken(lexer.next(), Kind.NUM_LIT, 1,7);
		checkToken(lexer.next(), Kind.NUM_LIT, 1,9);
		checkEOF(lexer.next());
	}







	//
//	Test 3 # REMOVED A SPACE
//	String input = "5a a4";
//
	@Test
	void test3() throws LexicalException
	{

		String input = "5a a4";


		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.NUM_LIT, 1,1);
		checkToken(lexer.next(), Kind.IDENT, 1,2);
		checkToken(lexer.next(), Kind.IDENT, 1,4);
		checkEOF(lexer.next());
	}
	//
//	Test 4
//	String input = "a+2 2-x";
	@Test
	void test4() throws LexicalException
	{

		String input = "a+2 2-x";


		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.IDENT, 1,1);
		checkToken(lexer.next(), Kind.PLUS, 1,2);
		checkToken(lexer.next(), Kind.NUM_LIT, 1,3);

		checkToken(lexer.next(), Kind.NUM_LIT, 1,5);
		checkToken(lexer.next(), Kind.MINUS, 1,6);
		checkToken(lexer.next(), Kind.IDENT, 1,7);
		checkEOF(lexer.next());
	}



	//	Test 13
//	String input = "_abc _ $ a$b c_d";
	@Test
	void test13() throws LexicalException
	{

		String input = "_abc _ $ a$b c_d";


		show(input);
		ILexer lexer = getLexer(input);
		checkIdent(lexer.next(), "_abc", 1,1);
		checkIdent(lexer.next(), "_", 1,6);
		checkIdent(lexer.next(), "$", 1,8);
		checkIdent(lexer.next(), "a$b", 1,10);
		checkIdent(lexer.next(), "c_d", 1,14);
		checkEOF(lexer.next());
	}
	//
//
//
//
//
//
//
//
//
//	Test 14
//	String input = "_abc1 _123 _ $ ";
//
	@Test
	void test14() throws LexicalException
	{

		String input = "_abc1 _123 _ $ ";


		show(input);
		ILexer lexer = getLexer(input);
		checkIdent(lexer.next(), "_abc1", 1,1);
		checkIdent(lexer.next(), "_123", 1,7);
		checkIdent(lexer.next(), "_", 1,12);
		checkIdent(lexer.next(), "$", 1,14);
		checkEOF(lexer.next());
	}
	//
//	Test 15
//	String input = " TRUE FALSE TRUEFALSE FALSETRUE";
//
	@Test
	void test15() throws LexicalException
	{

		String input = " TRUE FALSE TRUEFALSE FALSETRUE";


		show(input);
		ILexer lexer = getLexer(input);
		checkBoolean(lexer.next(), true);
		checkBoolean(lexer.next(), false);
		checkIdent(lexer.next(), "TRUEFALSE", 1,13);
		checkIdent(lexer.next(), "FALSETRUE", 1,23);
		checkEOF(lexer.next());
	}
	//
//	Test 16
//	String input = " TRUE123 FALSEabc abcTRUE 123FALSE";
//
	@Test
	void test16() throws LexicalException
	{

		String input = " TRUE123 FALSEabc abcTRUE 123FALSE";


		show(input);
		ILexer lexer = getLexer(input);
		checkIdent(lexer.next(), "TRUE123", 1,2);
		checkIdent(lexer.next(), "FALSEabc", 1,10);
		checkIdent(lexer.next(), "abcTRUE", 1,19);
		checkToken(lexer.next(), Kind.NUM_LIT, 1,27);
		checkBoolean(lexer.next(), false);
		checkEOF(lexer.next());
	}
	//
//	Test 17
//	String input = " CONST VAR PROCEDURE";
//
	@Test
	void test17() throws LexicalException
	{

		String input = " CONST VAR PROCEDURE";


		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.KW_CONST, 1,2);
		checkToken(lexer.next(), Kind.KW_VAR, 1,8);
		checkToken(lexer.next(), Kind.KW_PROCEDURE, 1,12);
		checkEOF(lexer.next());
	}
	//
//	Test 18
//	String input = " CALL BEGIN END";
//
	@Test
	void test18() throws LexicalException
	{

		String input = " CALL BEGIN END";


		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.KW_CALL, 1,2);
		checkToken(lexer.next(), Kind.KW_BEGIN, 1,7);
		checkToken(lexer.next(), Kind.KW_END, 1,13);
		checkEOF(lexer.next());
	}
	//
//	Test 19
//	String input = " IF THEN WHILE DO";
//
	@Test
	void test19() throws LexicalException
	{

		String input = " IF THEN WHILE DO";


		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.KW_IF, 1,2);
		checkToken(lexer.next(), Kind.KW_THEN, 1,5);
		checkToken(lexer.next(), Kind.KW_WHILE, 1,10);
		checkToken(lexer.next(), Kind.KW_DO, 1,16);
		checkEOF(lexer.next());
	}
	//
//	Test 20
//	String input = " 123DO DOabc DO123 DO_";
//
	@Test
	void test20() throws LexicalException
	{

		String input = " 123DO DOabc DO123 DO_";


		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.NUM_LIT, 1,2);
		checkToken(lexer.next(), Kind.KW_DO, 1,5);

		checkIdent(lexer.next(), "DOabc", 1,8);
		checkIdent(lexer.next(), "DO123", 1,14);
		checkIdent(lexer.next(), "DO_", 1,20);
		checkEOF(lexer.next());
	}
//
//	Test 21
//	String input = "123VAR PROCEDUREabc BEGIN123 end_";

	@Test
	void test21() throws LexicalException
	{

		String input = "123VAR PROCEDUREabc BEGIN123 end_";


		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.NUM_LIT, 1,1);
		checkToken(lexer.next(), Kind.KW_VAR, 1,4);

		checkIdent(lexer.next(), "PROCEDUREabc", 1,8);
		checkIdent(lexer.next(), "BEGIN123", 1,21);
		checkIdent(lexer.next(), "end_", 1,30);
		checkEOF(lexer.next());
	}

	@Test
	void parsertest() throws LexicalException
	{

		String input = """
				CONST a=3;
				VAR x,y,z;
				PROCEDURE p;
				  VAR j;
				  BEGIN
				     ? x;
				     IF x = 0 THEN ! y ;
				     WHILE j < 24 DO CALL z
				  END;
				! a+b - (c/e) * 35/(3+4)
				.
				""";
		//! a+b - (c/e) * 35/(3+4)

		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.KW_CONST);
		checkToken(lexer.next(), Kind.IDENT);
		checkToken(lexer.next(), Kind.EQ);
		checkToken(lexer.next(), Kind.NUM_LIT);
		checkToken(lexer.next(), Kind.SEMI);

		checkToken(lexer.next(), Kind.KW_VAR);
		checkToken(lexer.next(), Kind.IDENT);
		checkToken(lexer.next(), Kind.COMMA);
		checkToken(lexer.next(), Kind.IDENT);
		checkToken(lexer.next(), Kind.COMMA);
		checkToken(lexer.next(), Kind.IDENT);
		checkToken(lexer.next(), Kind.SEMI);

		checkToken(lexer.next(), Kind.KW_PROCEDURE);
		checkToken(lexer.next(), Kind.IDENT);
		checkToken(lexer.next(), Kind.SEMI);
		checkToken(lexer.next(), Kind.KW_VAR);
		checkToken(lexer.next(), Kind.IDENT);
		checkToken(lexer.next(), Kind.SEMI);
		checkToken(lexer.next(), Kind.KW_BEGIN);
		checkToken(lexer.next(), Kind.QUESTION);
		checkToken(lexer.next(), Kind.IDENT);
		checkToken(lexer.next(), Kind.SEMI);

		checkToken(lexer.next(), Kind.KW_IF);
		checkToken(lexer.next(), Kind.IDENT);
		checkToken(lexer.next(), Kind.EQ);
		checkToken(lexer.next(), Kind.NUM_LIT);
		checkToken(lexer.next(), Kind.KW_THEN);
		checkToken(lexer.next(), Kind.BANG);
		checkToken(lexer.next(), Kind.IDENT);
		checkToken(lexer.next(), Kind.SEMI);

		checkToken(lexer.next(), Kind.KW_WHILE);
		checkToken(lexer.next(), Kind.IDENT);
		checkToken(lexer.next(), Kind.LT);
		checkToken(lexer.next(), Kind.NUM_LIT);
		checkToken(lexer.next(), Kind.KW_DO);
		checkToken(lexer.next(), Kind.KW_CALL);
		checkToken(lexer.next(), Kind.IDENT);
		checkToken(lexer.next(), Kind.KW_END);
		checkToken(lexer.next(), Kind.SEMI);

		checkToken(lexer.next(), Kind.BANG);
		checkToken(lexer.next(), Kind.IDENT);
		checkToken(lexer.next(), Kind.PLUS);
		checkToken(lexer.next(), Kind.IDENT);
		checkToken(lexer.next(), Kind.MINUS);

		checkToken(lexer.next(), Kind.LPAREN);
		checkToken(lexer.next(), Kind.IDENT);
		checkToken(lexer.next(), Kind.DIV);
		checkToken(lexer.next(), Kind.IDENT);
		checkToken(lexer.next(), Kind.RPAREN);

		checkToken(lexer.next(), Kind.TIMES);
		checkToken(lexer.next(), Kind.NUM_LIT);
		checkToken(lexer.next(), Kind.DIV);
		checkToken(lexer.next(), Kind.LPAREN);
		checkToken(lexer.next(), Kind.NUM_LIT);
		checkToken(lexer.next(), Kind.PLUS);
		checkToken(lexer.next(), Kind.NUM_LIT);
		checkToken(lexer.next(), Kind.RPAREN);


		checkToken(lexer.next(), Kind.DOT);

		checkEOF(lexer.next());
	}

}


