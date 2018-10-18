// Generated from GML.g4 by ANTLR 4.0
package eu.sisob.api.parser.gml.antlrfiles;

import java.util.List;

import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNSimulator;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class GMLParser extends Parser {
	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__2=1, T__1=2, T__0=3, KEY=4, NUMBER=5, STRING=6, MANTISSA=7, SIGN=8, 
		WS=9, CREATOR=10, COMMENT=11, VERSION=12;
	public static final String[] tokenNames = {
		"<INVALID>", "']'", "'graph'", "'['", "KEY", "NUMBER", "STRING", "MANTISSA", 
		"SIGN", "WS", "CREATOR", "COMMENT", "VERSION"
	};
	public static final int
		RULE_gml = 0, RULE_graph = 1, RULE_list = 2, RULE_keyvalue = 3, RULE_value = 4;
	public static final String[] ruleNames = {
		"gml", "graph", "list", "keyvalue", "value"
	};

	@Override
	public String getGrammarFileName() { return "GML.g4"; }

	@Override
	public String[] getTokenNames() { return tokenNames; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public GMLParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class GmlContext extends ParserRuleContext {
		public GraphContext graph() {
			return getRuleContext(GraphContext.class,0);
		}
		public GmlContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_gml; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof GMLListener ) ((GMLListener)listener).enterGml(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof GMLListener ) ((GMLListener)listener).exitGml(this);
		}
	}

	public final GmlContext gml() throws RecognitionException {
		GmlContext _localctx = new GmlContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_gml);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(10); graph();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class GraphContext extends ParserRuleContext {
		public List<TerminalNode> WS() { return getTokens(GMLParser.WS); }
		public TerminalNode WS(int i) {
			return getToken(GMLParser.WS, i);
		}
		public ListContext list() {
			return getRuleContext(ListContext.class,0);
		}
		public GraphContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_graph; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof GMLListener ) ((GMLListener)listener).enterGraph(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof GMLListener ) ((GMLListener)listener).exitGraph(this);
		}
	}

	public final GraphContext graph() throws RecognitionException {
		GraphContext _localctx = new GraphContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_graph);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(12); match(2);
			setState(16);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==WS) {
				{
				{
				setState(13); match(WS);
				}
				}
				setState(18);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(19); list();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ListContext extends ParserRuleContext {
		public KeyvalueContext keyvalue(int i) {
			return getRuleContext(KeyvalueContext.class,i);
		}
		public List<TerminalNode> WS() { return getTokens(GMLParser.WS); }
		public TerminalNode WS(int i) {
			return getToken(GMLParser.WS, i);
		}
		public List<KeyvalueContext> keyvalue() {
			return getRuleContexts(KeyvalueContext.class);
		}
		public ListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof GMLListener ) ((GMLListener)listener).enterList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof GMLListener ) ((GMLListener)listener).exitList(this);
		}
	}

	public final ListContext list() throws RecognitionException {
		ListContext _localctx = new ListContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_list);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(21); match(3);
			setState(25);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,1,_ctx);
			while ( _alt!=2 && _alt!=-1 ) {
				if ( _alt==1 ) {
					{
					{
					setState(22); match(WS);
					}
					} 
				}
				setState(27);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,1,_ctx);
			}
			setState(42);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==KEY) {
				{
				{
				setState(28); keyvalue();
				setState(37);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
				while ( _alt!=2 && _alt!=-1 ) {
					if ( _alt==1 ) {
						{
						{
						setState(30); 
						_errHandler.sync(this);
						_la = _input.LA(1);
						do {
							{
							{
							setState(29); match(WS);
							}
							}
							setState(32); 
							_errHandler.sync(this);
							_la = _input.LA(1);
						} while ( _la==WS );
						setState(34); keyvalue();
						}
						} 
					}
					setState(39);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
				}
				}
				}
				setState(44);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(48);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==WS) {
				{
				{
				setState(45); match(WS);
				}
				}
				setState(50);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(51); match(1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class KeyvalueContext extends ParserRuleContext {
		public List<TerminalNode> WS() { return getTokens(GMLParser.WS); }
		public ValueContext value() {
			return getRuleContext(ValueContext.class,0);
		}
		public TerminalNode WS(int i) {
			return getToken(GMLParser.WS, i);
		}
		public TerminalNode KEY() { return getToken(GMLParser.KEY, 0); }
		public KeyvalueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_keyvalue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof GMLListener ) ((GMLListener)listener).enterKeyvalue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof GMLListener ) ((GMLListener)listener).exitKeyvalue(this);
		}
	}

	public final KeyvalueContext keyvalue() throws RecognitionException {
		KeyvalueContext _localctx = new KeyvalueContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_keyvalue);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(53); match(KEY);
			setState(55); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(54); match(WS);
				}
				}
				setState(57); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==WS );
			setState(59); value();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ValueContext extends ParserRuleContext {
		public ListContext list() {
			return getRuleContext(ListContext.class,0);
		}
		public TerminalNode STRING() { return getToken(GMLParser.STRING, 0); }
		public TerminalNode NUMBER() { return getToken(GMLParser.NUMBER, 0); }
		public ValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_value; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof GMLListener ) ((GMLListener)listener).enterValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof GMLListener ) ((GMLListener)listener).exitValue(this);
		}
	}

	public final ValueContext value() throws RecognitionException {
		ValueContext _localctx = new ValueContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_value);
		try {
			setState(64);
			switch (_input.LA(1)) {
			case NUMBER:
				enterOuterAlt(_localctx, 1);
				{
				setState(61); match(NUMBER);
				}
				break;
			case STRING:
				enterOuterAlt(_localctx, 2);
				{
				setState(62); match(STRING);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(63); list();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\2\3\16E\4\2\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\3\2\3\2\3\3\3\3\7\3\21"+
		"\n\3\f\3\16\3\24\13\3\3\3\3\3\3\4\3\4\7\4\32\n\4\f\4\16\4\35\13\4\3\4"+
		"\3\4\6\4!\n\4\r\4\16\4\"\3\4\7\4&\n\4\f\4\16\4)\13\4\7\4+\n\4\f\4\16\4"+
		".\13\4\3\4\7\4\61\n\4\f\4\16\4\64\13\4\3\4\3\4\3\5\3\5\6\5:\n\5\r\5\16"+
		"\5;\3\5\3\5\3\6\3\6\3\6\5\6C\n\6\3\6\2\7\2\4\6\b\n\2\2H\2\f\3\2\2\2\4"+
		"\16\3\2\2\2\6\27\3\2\2\2\b\67\3\2\2\2\nB\3\2\2\2\f\r\5\4\3\2\r\3\3\2\2"+
		"\2\16\22\7\4\2\2\17\21\7\13\2\2\20\17\3\2\2\2\21\24\3\2\2\2\22\20\3\2"+
		"\2\2\22\23\3\2\2\2\23\25\3\2\2\2\24\22\3\2\2\2\25\26\5\6\4\2\26\5\3\2"+
		"\2\2\27\33\7\5\2\2\30\32\7\13\2\2\31\30\3\2\2\2\32\35\3\2\2\2\33\31\3"+
		"\2\2\2\33\34\3\2\2\2\34,\3\2\2\2\35\33\3\2\2\2\36\'\5\b\5\2\37!\7\13\2"+
		"\2 \37\3\2\2\2!\"\3\2\2\2\" \3\2\2\2\"#\3\2\2\2#$\3\2\2\2$&\5\b\5\2% "+
		"\3\2\2\2&)\3\2\2\2\'%\3\2\2\2\'(\3\2\2\2(+\3\2\2\2)\'\3\2\2\2*\36\3\2"+
		"\2\2+.\3\2\2\2,*\3\2\2\2,-\3\2\2\2-\62\3\2\2\2.,\3\2\2\2/\61\7\13\2\2"+
		"\60/\3\2\2\2\61\64\3\2\2\2\62\60\3\2\2\2\62\63\3\2\2\2\63\65\3\2\2\2\64"+
		"\62\3\2\2\2\65\66\7\3\2\2\66\7\3\2\2\2\679\7\6\2\28:\7\13\2\298\3\2\2"+
		"\2:;\3\2\2\2;9\3\2\2\2;<\3\2\2\2<=\3\2\2\2=>\5\n\6\2>\t\3\2\2\2?C\7\7"+
		"\2\2@C\7\b\2\2AC\5\6\4\2B?\3\2\2\2B@\3\2\2\2BA\3\2\2\2C\13\3\2\2\2\n\22"+
		"\33\"\',\62;B";
	public static final ATN _ATN =
		ATNSimulator.deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
	}
}