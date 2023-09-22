// Generated from /home/jaap/VAF/decompiler-benchmarking/src/main/java/nl/ou/debm/assessor/g.g4 by ANTLR 4.13.1
package nl.ou.debm;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link gParser}.
 */
public interface gListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link gParser#prule}.
	 * @param ctx the parse tree
	 */
	void enterPrule(gParser.PruleContext ctx);
	/**
	 * Exit a parse tree produced by {@link gParser#prule}.
	 * @param ctx the parse tree
	 */
	void exitPrule(gParser.PruleContext ctx);
	/**
	 * Enter a parse tree produced by {@link gParser#name}.
	 * @param ctx the parse tree
	 */
	void enterName(gParser.NameContext ctx);
	/**
	 * Exit a parse tree produced by {@link gParser#name}.
	 * @param ctx the parse tree
	 */
	void exitName(gParser.NameContext ctx);
}