// Generated from GML.g4 by ANTLR 4.0
package eu.sisob.api.parser.gml.antlrfiles;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNSimulator;
import org.antlr.v4.runtime.atn.LexerATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class GMLLexer extends Lexer {
	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__2=1, T__1=2, T__0=3, KEY=4, NUMBER=5, STRING=6, MANTISSA=7, SIGN=8, 
		WS=9, CREATOR=10, COMMENT=11, VERSION=12;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] tokenNames = {
		"<INVALID>",
		"']'", "'graph'", "'['", "KEY", "NUMBER", "STRING", "MANTISSA", "SIGN", 
		"WS", "CREATOR", "COMMENT", "VERSION"
	};
	public static final String[] ruleNames = {
		"T__2", "T__1", "T__0", "KEY", "NUMBER", "STRING", "MANTISSA", "SIGN", 
		"WS", "CREATOR", "COMMENT", "VERSION"
	};


	public GMLLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "GML.g4"; }

	@Override
	public String[] getTokenNames() { return tokenNames; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	@Override
	public void action(RuleContext _localctx, int ruleIndex, int actionIndex) {
		switch (ruleIndex) {
		case 9: CREATOR_action((RuleContext)_localctx, actionIndex); break;

		case 10: COMMENT_action((RuleContext)_localctx, actionIndex); break;

		case 11: VERSION_action((RuleContext)_localctx, actionIndex); break;
		}
	}
	private void VERSION_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 2: skip();  break;
		}
	}
	private void COMMENT_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 1: skip();  break;
		}
	}
	private void CREATOR_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 0: skip();  break;
		}
	}

	public static final String _serializedATN =
		"\2\4\16\u008a\b\1\4\2\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b"+
		"\t\b\4\t\t\t\4\n\t\n\4\13\t\13\4\f\t\f\4\r\t\r\3\2\3\2\3\3\3\3\3\3\3\3"+
		"\3\3\3\3\3\4\3\4\3\5\3\5\7\5(\n\5\f\5\16\5+\13\5\3\6\5\6.\n\6\3\6\6\6"+
		"\61\n\6\r\6\16\6\62\3\6\3\6\6\6\67\n\6\r\6\16\68\3\6\5\6<\n\6\5\6>\n\6"+
		"\3\7\3\7\7\7B\n\7\f\7\16\7E\13\7\3\7\3\7\3\b\3\b\5\bK\n\b\3\b\3\b\3\t"+
		"\3\t\3\n\3\n\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\7\13\\\n\13"+
		"\f\13\16\13_\13\13\3\13\5\13b\n\13\3\13\3\13\3\13\3\13\3\f\3\f\7\fj\n"+
		"\f\f\f\16\fm\13\f\3\f\5\fp\n\f\3\f\3\f\3\f\3\f\3\r\3\r\3\r\3\r\3\r\3\r"+
		"\3\r\3\r\3\r\7\r\177\n\r\f\r\16\r\u0082\13\r\3\r\5\r\u0085\n\r\3\r\3\r"+
		"\3\r\3\r\6C]k\u0080\16\3\3\1\5\4\1\7\5\1\t\6\1\13\7\1\r\b\1\17\t\1\21"+
		"\n\1\23\13\1\25\f\2\27\r\3\31\16\4\3\2\n\4C\\c|\5\62;C\\c|\3\62;\3\62"+
		";\4GGgg\3\62;\4--//\5\13\f\17\17\"\"\u0097\2\3\3\2\2\2\2\5\3\2\2\2\2\7"+
		"\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2"+
		"\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\3\33\3\2\2\2\5"+
		"\35\3\2\2\2\7#\3\2\2\2\t%\3\2\2\2\13-\3\2\2\2\r?\3\2\2\2\17H\3\2\2\2\21"+
		"N\3\2\2\2\23P\3\2\2\2\25R\3\2\2\2\27g\3\2\2\2\31u\3\2\2\2\33\34\7_\2\2"+
		"\34\4\3\2\2\2\35\36\7i\2\2\36\37\7t\2\2\37 \7c\2\2 !\7r\2\2!\"\7j\2\2"+
		"\"\6\3\2\2\2#$\7]\2\2$\b\3\2\2\2%)\t\2\2\2&(\t\3\2\2\'&\3\2\2\2(+\3\2"+
		"\2\2)\'\3\2\2\2)*\3\2\2\2*\n\3\2\2\2+)\3\2\2\2,.\5\21\t\2-,\3\2\2\2-."+
		"\3\2\2\2.\60\3\2\2\2/\61\t\4\2\2\60/\3\2\2\2\61\62\3\2\2\2\62\60\3\2\2"+
		"\2\62\63\3\2\2\2\63=\3\2\2\2\64\66\7\60\2\2\65\67\t\5\2\2\66\65\3\2\2"+
		"\2\678\3\2\2\28\66\3\2\2\289\3\2\2\29;\3\2\2\2:<\5\17\b\2;:\3\2\2\2;<"+
		"\3\2\2\2<>\3\2\2\2=\64\3\2\2\2=>\3\2\2\2>\f\3\2\2\2?C\7$\2\2@B\13\2\2"+
		"\2A@\3\2\2\2BE\3\2\2\2CD\3\2\2\2CA\3\2\2\2DF\3\2\2\2EC\3\2\2\2FG\7$\2"+
		"\2G\16\3\2\2\2HJ\t\6\2\2IK\5\21\t\2JI\3\2\2\2JK\3\2\2\2KL\3\2\2\2LM\t"+
		"\7\2\2M\20\3\2\2\2NO\t\b\2\2O\22\3\2\2\2PQ\t\t\2\2Q\24\3\2\2\2RS\7E\2"+
		"\2ST\7t\2\2TU\7g\2\2UV\7c\2\2VW\7v\2\2WX\7q\2\2XY\7t\2\2Y]\3\2\2\2Z\\"+
		"\13\2\2\2[Z\3\2\2\2\\_\3\2\2\2]^\3\2\2\2][\3\2\2\2^a\3\2\2\2_]\3\2\2\2"+
		"`b\7\17\2\2a`\3\2\2\2ab\3\2\2\2bc\3\2\2\2cd\7\f\2\2de\3\2\2\2ef\b\13\2"+
		"\2f\26\3\2\2\2gk\7%\2\2hj\13\2\2\2ih\3\2\2\2jm\3\2\2\2kl\3\2\2\2ki\3\2"+
		"\2\2lo\3\2\2\2mk\3\2\2\2np\7\17\2\2on\3\2\2\2op\3\2\2\2pq\3\2\2\2qr\7"+
		"\f\2\2rs\3\2\2\2st\b\f\3\2t\30\3\2\2\2uv\7X\2\2vw\7g\2\2wx\7t\2\2xy\7"+
		"u\2\2yz\7k\2\2z{\7q\2\2{|\7p\2\2|\u0080\3\2\2\2}\177\13\2\2\2~}\3\2\2"+
		"\2\177\u0082\3\2\2\2\u0080\u0081\3\2\2\2\u0080~\3\2\2\2\u0081\u0084\3"+
		"\2\2\2\u0082\u0080\3\2\2\2\u0083\u0085\7\17\2\2\u0084\u0083\3\2\2\2\u0084"+
		"\u0085\3\2\2\2\u0085\u0086\3\2\2\2\u0086\u0087\7\f\2\2\u0087\u0088\3\2"+
		"\2\2\u0088\u0089\b\r\4\2\u0089\32\3\2\2\2\21\2)-\628;=CJ]ako\u0080\u0084";
	public static final ATN _ATN =
		ATNSimulator.deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
	}
}