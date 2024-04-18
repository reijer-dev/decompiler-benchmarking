package nl.ou.debm.common.feature2;

import nl.ou.debm.common.antlr.CBaseVisitor;
import nl.ou.debm.common.antlr.CLexer;
import nl.ou.debm.common.antlr.CParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

// This visitor extracts information about testcases from C code. A testcase here means a DatastructureCodeMarker. The result of the visiting operation is the array recovered_testcases, which contains partially unprocessed information. For example, the codemarker is stored as string and not yet parsed. This is because the purpose of the DataStructureCVisitor is to extract information that can then be further processed.
public class DataStructureCVisitor extends CBaseVisitor<Object>
{
    // Keeps track of which names are in scope at any moment during the traversal of the parse tree.
    NameInfo nameInfo = new NameInfo();
    ArrayList<Testcase> recovered_testcases = new ArrayList<>();



    //
    //  Helper functions
    //

    // This is an alternative to ctx.getText, which returns code without spaces, which is often problematic.
    public static String originalCode(ParserRuleContext ctx) {
        int a = ctx.start.getStartIndex();
        int b = ctx.stop.getStopIndex();
        Interval interval = new Interval(a,b);
        return ctx.start.getInputStream().getText(interval);
    }

    //todo there are 4. the fourth is struct member declarations, which are different because they can be a bitfield
    // There are 3 kinds of declarations used in the C grammar: regular declarations, forDeclarations and parameterDeclarations. To avoid having to write 3 different handlers for those declarations, I convert them to a common format, which is the regular declaration. Conversion is easy, as a function parameter declaration is a special case of a normal declaration (it always declares one variable and does not initialize), except that it doesn't end with a semicolon, so reparsing with an added semicolon works. For forDeclarations the same can be done.
    static CParser.DeclarationContext toRegularDeclaration(ParserRuleContext ctx)
    {
        var precondition = (
            (ctx instanceof CParser.DeclarationContext)
            || (ctx instanceof CParser.ForDeclarationContext)
            || (ctx instanceof CParser.ParameterDeclarationContext)
        );
        if ( ! precondition ) {
            throw new RuntimeException("ctx is not a declaration");
        }

        if (ctx instanceof CParser.DeclarationContext casted) {
            return casted;
        }
        else {
            var lexer = new CLexer(CharStreams.fromString(originalCode(ctx) + ";"));
            var parser = new CParser(new CommonTokenStream(lexer));
            return parser.declaration();
        }
    }

    // Removes leading and trailing whitespace, and replaces all other whitespace sequences (like tabs, newlines etc.) with a single space, so that more assumptions can be made about the code.
    // Examples:
    // normalizeCode(" int    i; ") returns "int i;"
    // normalizeCode("\tint i;\n\tint j;") returns "int i; int j;"
    static String normalizeCode(String code) {
        return code.trim().replaceAll("\\s+", " ");
    }

    static void parseStruct(CParser.StructOrUnionSpecifierContext ctx, NameInfo nameInfo)
    {
        nameInfo.addScope();
        /*
        for member in ctx {
            parseDeclaration(member, nameInfo);
        }
        */
        nameInfo.popScope();
    }

    // This function accepts a general ParserRuleContext (the base class for all context classes), because I need to accept two kinds of contexts. That's because the C grammar makes a distinction between "declaration"s, and "forDeclaration"s (declarations made in a for-loop initializer). These are the grammar rules:
    // forDeclaration
    //    :   declarationSpecifiers initDeclaratorList?
    //    ;
    // declaration
    //    :   declarationSpecifiers initDeclaratorList? ';'
    //    |   staticAssertDeclaration
    //    ;
    static void parseDeclaration(ParserRuleContext ctx, NameInfo dest, NameInfo.EScope scope)
    {
        var declarationContext = toRegularDeclaration(ctx);
        var declarationSpecifiers = declarationContext.declarationSpecifiers();
        var initDeclaratorList = declarationContext.initDeclaratorList();
        var normalizedCode = normalizeCode(originalCode(declarationContext));

        // get all type specifiers
        var typeSpecifiers = new ArrayList<String>();
        (new CBaseVisitor<Void>() {
            @Override
            public Void visitTypeSpecifier(CParser.TypeSpecifierContext ctx) {
                typeSpecifiers.add(originalCode(ctx));

                if (ctx.structOrUnionSpecifier() != null) {
                    System.out.println("sutrct or union: " + originalCode(ctx));
                }
                return null;
            }
        }).visit(declarationSpecifiers);

        //todo remove
        System.out.println("");
        System.out.println("parsing declaration: " + originalCode(ctx) + ". Found type specifiers:");
        for (var typeSpecifier : typeSpecifiers) {
            System.out.println(typeSpecifier);
        }
        System.out.println("initDeclaratorList == null : " + (initDeclaratorList == null));

        // I handle part of parsing a typedef myself because it's complicated with the C parser. You can't just get the type and the name as a string easily.
        if (normalizedCode.startsWith("typedef"))
        {
            final int lastSpaceIdx = normalizedCode.lastIndexOf(' ');

            var elt = new NameInfo.TypeInfo();
            elt.name = normalizedCode.substring(lastSpaceIdx + 1).replace(";", "");
            elt.scope = scope;
            elt.strType = typeSpecifiers.get(0);
            elt.isPointer = normalizedCode.charAt(lastSpaceIdx - 1) == '*';
            dest.add(elt);
            System.out.println("parsed as typedef: " + elt);
            return;
        }

        String variableNameBug = null;
        if (initDeclaratorList == null) {
            // This should never happen? Yet it happens because of what I think is a bug in the C grammar. The code "int i;" is parsed as a declarationSpecifier list containing the elements "int" and "i", even though "i" is not a specifier. I think it happens because a declarationSpecifier can be a typeSpecifier, which in turn can be a typedefName, which can be a general Identifier, which "i" is. This is clearly wrong, because when the declaration declares a true list of names, that is more than 1, such as in "int i, j", only "int" is considered a specifier and the rest is parsed as a initDeclaratorList.
            // Attempt to correct for this:
            var lastIdx = typeSpecifiers.size() - 1;
            variableNameBug = typeSpecifiers.get(lastIdx);
            typeSpecifiers.remove(lastIdx);
        }

        // concatenate the type speficiers to form the full base typename
        // I call this the "base" typespec because we don't know the full type yet. Some of the declared variables may have pointer type, while others do not. For example, this is legal C and declares both an int and a pointer to an int:
        // int i, *j;
        String baseTypeSpec;
        {
            var sb = new StringBuilder();
            //insert unsigned first to make typenames uniform (C allows both "int unsigned" and "unsigned int".)
            if (typeSpecifiers.contains("unsigned")) {
                sb.append("unsigned");
            }
            for (var typeSpec : typeSpecifiers) {
                if (typeSpec.equals("unsigned")) continue;
                if ( ! sb.isEmpty()) sb.append(" ");
                sb.append(typeSpec);
            }
            baseTypeSpec = sb.toString();
        }

        if (initDeclaratorList != null) {
            var initDeclarators = Optional.of(initDeclaratorList)
                    .map(x -> x.initDeclarator())
                    .orElse(null);
            if (initDeclarators != null) {
                for (var initDeclarator : initDeclarators)
                {
                    var declarator = initDeclarator.declarator();
                    var isPointer = declarator.pointer() != null;
                    var variableName = declarator.directDeclarator().getText();

                    var info = new NameInfo.VariableInfo();
                    info.scope = scope;
                    info.name = variableName;
                    info.typeInfo.strType = baseTypeSpec;
                    info.typeInfo.isPointer = isPointer;
                    dest.add(info);
                }
            }
            else throw new RuntimeException("geen initDeclarators"); //todo kan dit voorkomen? als het goed is niet
        }
        else if (variableNameBug != null) {
            // There was no declarator list because of the C grammar bug. Specific handling of that:
            var info = new NameInfo.VariableInfo();
            info.scope = scope;
            info.name = variableNameBug;
            info.typeInfo.strType = baseTypeSpec;
            info.typeInfo.isPointer = false; // Always false because the bug does not occur with things like "int *i"
            dest.add(info);
        }

        System.out.println("");
    }

    // This requires a workaround because the C grammar we use doesn't allow for visiting all identifiers, which would have made this easy.
    public static void getIdentifiers(ParseTree tree, List<String> dest)
    {
        for (int i = 0; i<tree.getChildCount(); i++){
            ParseTree child = tree.getChild(i);
            if (child instanceof TerminalNode node) {
                if (node.getSymbol().getType() == CParser.Identifier) {
                    dest.add(node.getText());
                }
            }
            else {
                getIdentifiers(child, dest);
            }
        }
    }

    // recovers DataStructureCodeMarkers from any C code
    //
    // Every DataStructureCodeMarker created by the producer is represented in the original C code as a function call with two parameters:
    // 1. the string representation of the code marker
    // 2. the address of the variable, of which we want to know if the decompiler can determine its type correctly
    //
    // After decompilation, some differences may occur. For example, the decompiled function call may have extra parameters. We want to have some tolerance for such mistakes, so we make as few assumptions about the structure of the C code as possible. What is assumed is the following:
    // - Code marker strings may occur anywhere. They are easily recognized by their characteristic prefix, which is highly unlikely to occur in code naturally.
    // - After a code marker string there is a comma (the separator for function call parameters) followed by an expression. This expression is the memory address of the variable being tested.
    public static List<Testcase> findTestcasesInCode(String code, NameInfo nameInfo) {
        var ret = new ArrayList<Testcase>();

        //todo replace breaks with error handling or continue??
        while (true) {
            // find the next codemarker
            var codemarkerStartIndex = code.indexOf(DataStructureCodeMarker.characteristic);
            if (codemarkerStartIndex == -1) break;
            var codemarkerEndIndex = code.indexOf('\"', codemarkerStartIndex); //this works because code markers don't contain quotes
            if (codemarkerEndIndex == -1) {
                System.out.println("error in parsing codemarker: no ending quote.");
                System.out.println("remaining code: " + code.substring(codemarkerStartIndex));
                break;
            }
            var codemarkerString = code.substring(codemarkerStartIndex, codemarkerEndIndex);
            DataStructureCodeMarker codemarker;
            try {
                codemarker = new DataStructureCodeMarker(codemarkerString);
            } catch (Exception e) {
                System.out.println("error in parsing codemarker string: " + codemarkerString);
                break;
            }

            // parse variable address expression
            code = code.substring(codemarkerEndIndex + 1); //+1 skips the quote
            code = code.trim();
            if (code.charAt(0) != ',') {
                System.out.println("error in parsing codemarker: comma expected.");
                System.out.println("codemarker: " + codemarkerString);
                System.out.println("remaining code: " + code);
                break;
            }
            code = code.substring(1); //removes the comma

            // Use the C parser to parse the remaining code as an expression
            // Parsing stops when the expression ends, so it doesn't matter if there is more code. One special case of this is important though: if the decompiler has created too many function parameters, which are separated by commas, all parameters are interpreted as one big comma-separated compound expression, of which we need only the first element. If at any point parsing fails, the parser stops parsing, but the successfully parsed parts are preserved, and we only need the first one, so this should work reliably.
            var lexer = new CLexer(CharStreams.fromString(code));
            var parser = new CParser(new CommonTokenStream(lexer));
            var subexprs = Optional.of(parser.expression())
                    .map(x -> x.assignmentExpression())
                    .orElse(null);
            if (subexprs == null || subexprs.isEmpty()) {
                System.out.println("error in parsing variable address expression");
                System.out.println("codemarker string: " + codemarkerString);
                System.out.println("remaining code: " + code);
                break;
            }
            var variableAddressExpr = subexprs.get(0);

            // Next step: extract the name of the variable being referred to in the expression
            // We first find all identifiers in the expression. Then we ignore those that are not a variable name that is currently in scope. That should leave exactly one result. If not, that indicates a decompilation problem.
            var identifiers = new ArrayList<String>();
            getIdentifiers(variableAddressExpr, identifiers);
            int variables_found = 0;
            String variableName = null;
            for (var identifier : identifiers) {
                if (nameInfo.contains(identifier)) {
                    variableName = identifier;
                    variables_found++;
                }
            }

            // create and add testcase
            var testcase = new Testcase();
            testcase.codemarker = codemarker;
            testcase.variableAddressExpr = originalCode(variableAddressExpr);
            if (variables_found == 1) testcase.status = Testcase.Status.ok;
            else                      testcase.status = Testcase.Status.variableNotFound;

            if (testcase.status == Testcase.Status.ok)
            {
                testcase.varInfo = (NameInfo.VariableInfo) nameInfo.get(variableName);
            }

            ret.add(testcase);
        }

        return ret;
    }


    //
    //  CBaseVisitor overrides
    //

    @Override
    public Object visitDeclaration(CParser.DeclarationContext ctx)
    {
        System.out.println("global declaration found: " + originalCode(ctx));
        //todo the scope can never be anything else because there is no context available here to determine the exact kind of scope. is that a problem? I do call parseDelcaration explicitly for function parameters and for declarations so I think it works well this way.
        NameInfo.EScope scope;
        if (nameInfo.stackSize() == 1) scope = NameInfo.EScope.global;
        else                           scope = NameInfo.EScope.local;
        parseDeclaration(ctx, nameInfo, scope);
        return null;
    }

    @Override
    public Object visitFunctionDefinition(CParser.FunctionDefinitionContext ctx)
    {
        /*
        Ghidra creates empty structs, with even incorrect C code.
        ANTLR sees these lines as function definitions.
        Therefore, we return when no function body is found
        */
        if (ctx.compoundStatement() == null || ctx.compoundStatement().blockItemList() == null)
            return null;

        nameInfo.addScope(); //scope for this function

        // parse the function parameters
        // I only visit the declarator to be safe, because maybe the function contains a function declaration somewhere that has its own parameters. I'm not sure if that would be a problem or not.
        (new CBaseVisitor<Void>() {
            @Override
            public Void visitParameterTypeList(CParser.ParameterTypeListContext ctx) {
                var parameterDeclarations = Optional.of(ctx)
                    .map(x -> x.parameterList())
                    .map(x -> x.parameterDeclaration())
                    .orElse(null);
                if (parameterDeclarations == null)
                    return null;

                for (var parameterDeclaration : parameterDeclarations) {
                    parseDeclaration(parameterDeclaration, nameInfo, NameInfo.EScope.functionParameter);
                }
                return null;
            }
        }).visit(ctx.declarator());

        // Traverse the function parse tree with a (sub)visitor. //todo dit hoeft geen subvisitor te zijn
        // This visitor finds all codemarkers, and keeps track of all names (type names and variable names) that are in scope, which is necessary to interpret the codemarkers.
        visitChildren(ctx);

        nameInfo.popScope();
        return null;
    }

    @Override
    public Void visitStatement(CParser.StatementContext ctx)
    {
        // This function doesn't do much with the statements themselves. It just handles a few cases where scopes must be created before further traversal of the parse tree.

        // A compound (block) statement needs its own scope.
        var compoundStatement = ctx.compoundStatement();
        if (compoundStatement != null) {
            nameInfo.addScope();
            visitChildren(ctx);
            nameInfo.popScope();
            return null;
        }

        // Check if this is a for loop that has a declaration. If so, the declaration gets its own scope. This is necessary if the for loop has a block statement that reuses a name, for example:
        // for (int i=0; i<length; i++) {
        //      int i = 10; //this doesn't affect the i defined above
        // }
        var forDeclaration = Optional.of(ctx)
            .map(x -> x.iterationStatement())
            .map(x -> x.forCondition())
            .map(x -> x.forDeclaration())
            .orElse(null);
        if (forDeclaration != null) {
            nameInfo.addScope();
            parseDeclaration(forDeclaration, nameInfo, NameInfo.EScope.forDeclaration);

            // Continue parsing the for loop statement
            // Note: this also traverses the forDeclaration again. Although it will probably never be useful, if the forDeclaration somehow contains a codemarker, it will be found and it may even refer to a variable from that same declaration, because they've been added to the nameInfo at this point. (I was surprised to learn that C allows that. You can do things like "int i=10, j=i;")
            visitChildren(ctx);
            nameInfo.popScope();
            return null;
        }

        // Do the default for all other statements. This includes for loops without a declaration.
        visitChildren(ctx);
        return null;
    }

    // Codemarkers are function calls, and function calls are expressions, so visiting the expressions will find all codemarkers.
    // Every codemarker that is found should have a second parameter that contains the name of a variable. Which variable this name refers to is lookup up in nameInfo, which contains information about all variables that are currently in scope.
    @Override
    public Void visitExpression(CParser.ExpressionContext ctx) {
        var foundTestcases = findTestcasesInCode(originalCode(ctx), nameInfo);
        recovered_testcases.addAll(foundTestcases);
        return null;
    }
}
