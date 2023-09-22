// Generated from /home/jaap/VAF/decompiler-benchmarking/src/main/java/nl/ou/debm/assessor/Test.g4 by ANTLR 4.13.1
package nl.ou.debm.common;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link TestParser}.
 */
public interface TestListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link TestParser#prule}.
	 * @param ctx the parse tree
	 */
	void enterPrule(TestParser.PruleContext ctx);
	/**
	 * Exit a parse tree produced by {@link TestParser#prule}.
	 * @param ctx the parse tree
	 */
	void exitPrule(TestParser.PruleContext ctx);
	/**
	 * Enter a parse tree produced by {@link TestParser#name}.
	 * @param ctx the parse tree
	 */
	void enterName(TestParser.NameContext ctx);
	/**
	 * Exit a parse tree produced by {@link TestParser#name}.
	 * @param ctx the parse tree
	 */
	void exitName(TestParser.NameContext ctx);
}