// Generated from GML.g4 by ANTLR 4.0
package eu.sisob.api.parser.gml.antlrfiles;

import org.antlr.v4.runtime.tree.ParseTreeListener;

public interface GMLListener extends ParseTreeListener {
	void enterGraph(GMLParser.GraphContext ctx);
	void exitGraph(GMLParser.GraphContext ctx);

	void enterValue(GMLParser.ValueContext ctx);
	void exitValue(GMLParser.ValueContext ctx);

	void enterList(GMLParser.ListContext ctx);
	void exitList(GMLParser.ListContext ctx);

	void enterGml(GMLParser.GmlContext ctx);
	void exitGml(GMLParser.GmlContext ctx);

	void enterKeyvalue(GMLParser.KeyvalueContext ctx);
	void exitKeyvalue(GMLParser.KeyvalueContext ctx);
}