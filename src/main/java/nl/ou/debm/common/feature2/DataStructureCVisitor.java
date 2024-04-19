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
    public static CParser.DeclarationContext toRegularDeclaration(ParserRuleContext ctx)
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
    public static String normalizeCode(String code) {
        return code.trim().replaceAll("\\s+", " ");
    }

    public static void parseStruct(CParser.StructOrUnionSpecifierContext ctx, NameInfo nameInfo)
    {
        nameInfo.addScope();
        /*
        for member in ctx {
            parseDeclaration(member, nameInfo);
        }
        */
        nameInfo.popScope();
    }

    public static CParser makeParser(String code) {
        var lexer = new CLexer(CharStreams.fromString(code));
        return new CParser(new CommonTokenStream(lexer));
    }

    // extracts newly defined names from a declaration
    //
    // There are two kinds of names extracted: type names and variable names. For type names there are two cases: structs/unions and typedefs. The name of a struct will include the word struct, as that is how it is referred to in code, and to distinguish struct names from typedefnames.
    //
    // Types are left partially unparsed, for two reasons:
    //      1. speed. For the purpose of interpreting codemarkers, only types that occur in code markers need to be fully parsed, while all declarations need to be parsed to find the names that are in scope.
    //      2. errors. Parsing types may cause errors, making this function even more complicated than it already is.
    // When it comes to parsing types, only the bare minimum is done, which can be understood as a kind of "lazy" parsing. For example, "sometype arr[10]" is parsed as an array of sometype, but parsing sometype is postponed until necessary.
    //
    // This function accepts a general ParserRuleContext (the base class for all context classes), because multiple kinds of declarations need to be accepted.
    public static void parseDeclaration(ParserRuleContext ctx, NameInfo dest, NameInfo.EScope scope)
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
                return null;
            }
        }).visit(declarationSpecifiers);


        // I handle part of parsing a typedef myself because it's complicated with the C parser. You can't just get the type and the name as a string easily.
        if (normalizedCode.startsWith("typedef"))
        {
            final int lastSpaceIdx = normalizedCode.lastIndexOf(' ');
            boolean isPointer = normalizedCode.charAt(lastSpaceIdx - 1) == '*';
            var T = new NormalForm.Unparsed(typeSpecifiers.get(0));

            var elt = new NameInfo.TypeInfo();
            elt.name = normalizedCode.substring(lastSpaceIdx + 1).replace(";", "");
            elt.scope = scope;
            if (isPointer)  elt.T = new NormalForm.Pointer(T);
            else            elt.T = T;
            dest.add(elt);

            System.out.println("parsed " + normalizedCode + " as typedef:\n" + elt);
            return;
        }


        // Here I handle a bug in the C grammar. The code "int i;" is parsed as a declarationSpecifier list containing the elements "int" and "i", even though "i" is not a declarationSpecifier. It happens because a declarationSpecifier can be a typeSpecifier, which in turn can be a typedefName, which can be a general Identifier, which "i" is. This is clearly wrong, because when the declaration declares a true list of names, that is more than 1, such as in "int i, j", only "int" is considered a specifier and the rest is parsed as a initDeclaratorList.
        // Why does the following loop correctly recognize the bug: If none of the breaks are triggered, the type that was found has the form [one or more type specifiers] typedefname, which in C declares a variable with the typedefname as name. I tested it with clang. You can do for example:
        // typedef int myint;
        // void f() {
        //     unsigned myint;
        //     myint = 10;
        // }
        // From this it can be concluded that "unsigned myint;" does not declare 0 variables of type "unsigned myint", but actually declares a variable named myint, overwriting the meaning of the typedefname in that scope. So even if the variable name is actually a valid typeDefName, it should still be parsed as a variable name.
        String variableNameBug = null;
        do {
            if (initDeclaratorList != null) break; //bug doesn't happen when multiple variables are declared, in which case there is an initDeclaratorList.

            int size = declarationSpecifiers.declarationSpecifier().size();
            if (size <= 1) break; //bug only occurs when multiple declarationSpecifiers are found, of which the variable name is one.

            var last = declarationSpecifiers.declarationSpecifier().get(size-1);
            var typeDefName = Optional.of(last)
                .map(x -> x.typeSpecifier())
                .map(x -> x.typedefName())
                .orElse(null);
            if (typeDefName == null) break; //bug only occurs when the last declarationSpecifier is parsed as a typeDefName

            // The bug was triggered. Correct for it:
            var lastIdx = typeSpecifiers.size() - 1;
            variableNameBug = typeSpecifiers.get(lastIdx);
            typeSpecifiers.remove(lastIdx);
        } while(false);


        // concatenate the typeSpecifiers to form the base type
        // I call this the "base" type because we don't know the full type yet. Some of the declared variables may have pointer or array type, while others do not. For example, this can be done in C: "int i, *j, k[10];" to define an int, a pointer to an int and an array of ints in one declaration.
        String strBaseType = String.join(" ", typeSpecifiers);
        var baseType = new NormalForm.Unparsed(strBaseType);

        // if the base type is a struct or union with a name, add it to the NameInfo object
        boolean isStruct = strBaseType.startsWith("struct");
        boolean isUnion = strBaseType.startsWith("union");
        if (isStruct || isUnion)
        {
            if (typeSpecifiers.size() > 1) throw new RuntimeException("todo does this occur?"); //should not happen because as far as I know "unsigned" and "signed" are the only typeSpecifiers that can occur as an additional specifier for another type, and there is no such thing as an unsigned or signed struct.

            var name = makeParser(strBaseType).structOrUnionSpecifier().Identifier();
            if (name != null) {
                var elt = new NameInfo.TypeInfo();
                elt.name = (isStruct ? "struct" : "union") + ' ' + name;
                elt.scope = scope;
                elt.T = new NormalForm.Unparsed(strBaseType);
                dest.add(elt);
            }
        }


        // Add any declared variables to the NameInfo object
        if (variableNameBug != null) {
            var elt = new NameInfo.VariableInfo();
            elt.scope = scope;
            elt.name = variableNameBug;
            elt.typeInfo.T = baseType; //is never a pointer or array because the bug does not occur with those
            dest.add(elt);
        }
        else if (initDeclaratorList != null) {
            var initDeclarators = initDeclaratorList.initDeclarator();

            for (var initDeclarator : initDeclarators)
            {
                var declarator = initDeclarator.declarator();
                var isPointer = declarator.pointer() != null;
                var strDirectDeclarator = declarator.directDeclarator().getText();

                // The direct declarator is the name of the variable, unless it is an array. Then it is of the form name[size].
                NormalForm.Type T;
                String variableName;
                {
                    var splitted = strDirectDeclarator.split("\\[");
                    if (splitted.length == 1) {
                        variableName = strDirectDeclarator;
                        T = baseType;
                    }
                    else {
                        variableName = splitted[0];

                        String strSize = splitted[1].substring(0, splitted[1].length()-1); //removes the trailing "]"
                        // If the size is a constant, it is a normal array. Otherwise it's a variable length array and we don't further interpret the size, as it is just some expression that is evaluated at runtime.
                        try {
                            int size = Integer.parseInt(strSize);
                            T = new NormalForm.Array(baseType, size);
                        } catch (NumberFormatException e) {
                            T = new NormalForm.VariableLengthArray(baseType);
                        }
                    }
                }
                if (isPointer) {
                    T = new NormalForm.Pointer(T);
                }

                var elt = new NameInfo.VariableInfo();
                elt.scope = scope;
                elt.name = variableName;
                elt.typeInfo.T = T;
                dest.add(elt);
            }
        }
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
        //Note: the scope cannot be anything else here because declarations like function parameters and struct declarations are not visited by this function, because they have different grammar rules.
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
