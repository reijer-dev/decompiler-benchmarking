package nl.ou.debm.common.feature2;

import nl.ou.debm.common.*;
import nl.ou.debm.common.antlr.CBaseVisitor;
import nl.ou.debm.common.antlr.CParser;
import nl.ou.debm.producer.IFeature;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// This visitor extracts information about testcases from C code. A testcase here means a DatastructureCodeMarker. The result of the visiting operation is the array recovered_testcases, which contains partially unprocessed information. For example, the codemarker is stored as string and not yet parsed. This is because the purpose of the DataStructureCVisitor is to extract information that can then be further processed.
public class DataStructureCVisitor extends CBaseVisitor<Object>
{
    // Keeps track of which names are in scope at any moment during the traversal of the parse tree.
    NameInfo nameInfo = new NameInfo();
    ArrayList<Testcase> recovered_testcases = new ArrayList<>();

    public DataStructureCVisitor(EArchitecture arch) {
        // add some common names that decompilers use
        String common = """
            typedef _Bool bool;
            int true = 1;
            int false = 0;
            """;
        if (arch == EArchitecture.X64ARCH || arch == EArchitecture.X86ARCH) {
            common += """
                typedef signed char		int8_t;
                typedef short int		int16_t;
                typedef int			int32_t;
                typedef long long int		int64_t;
                
                typedef unsigned char		uint8_t;
                typedef unsigned short int	uint16_t;
                typedef unsigned int		uint32_t;
                typedef unsigned long long int	uint64_t;
                """;
        }
        var parser = Parsing.makeParser(common);
        var parsed = parser.compilationUnit();
        visit(parsed);
    }


    //
    //  Helper functions
    //


    // returns the code behind ctx with all bitfields removed
    // To throw away the bitfield information, I traverse the entire parse tree, and add the original code of all terminal nodes to an accumulator. In principle, this results in the same code again. There is one exception: when a structDeclaratorList is encountered, I change the underlying code to remove all bitfield information. This results in the same declaration code without the bitfields.
    public static String removeBitfields(CParser.StructDeclarationContext ctx)
    {
        var accumulator = new StringBuilder();
        removeBitfieldsRecursive(ctx, accumulator);
        return accumulator.toString();
    }
    private static void removeBitfieldsRecursive(ParseTree tree, StringBuilder accumulator)
    {
        for (int i = 0; i<tree.getChildCount(); i++)
        {
            ParseTree child = tree.getChild(i);
            if (child instanceof CParser.StructDeclaratorListContext casted)
            {
                boolean first = true;
                for (var structDeclarator : casted.structDeclarator()) {
                    //when the declarator is null the bitfield is unnamed, which is a way to specify that there must be padding bits. I ignore those bitfields because they're irrelevant for my decompiler tests. This doesn't cause problems even if what remains is a declaration of 0 variables because that is allowed in C.
                    if (structDeclarator.declarator() == null) continue;

                    if (first) first = false;
                    else       accumulator.append(',');
                    accumulator.append(' ' + Parsing.normalizedCode(structDeclarator.declarator()));
                }
            }
            else if (child instanceof TerminalNode node) {
                accumulator.append(' ' + node.getText()); //I separate them all with spaces just to be safe.
            }
            else {
                removeBitfieldsRecursive(child, accumulator);
            }
        }
    }

    // There are 4 kinds of declarations used in the C grammar: regular declarations, structDeclaration, forDeclarations and parameterDeclarations. To avoid having to write different handlers for those declarations, I convert them to a common format, which is the regular declaration. For struct declarations this is a lossy conversion: I throw away the number of bits if it is a bitfield, because I don't currently test for that anyway. Conversion is otherwise easy, as the regular declaration is more general than the others. For example, a function parameter is a regular declaration with the special property that it always defines one variable name, except that it doesn't end with a semicolon, so reparsing with an added semicolon works. For forDeclarations the same can be done.
    public static CParser.DeclarationContext toRegularDeclaration(ParserRuleContext ctx)
    {
        var precondition = (
            (ctx instanceof CParser.DeclarationContext)
                || (ctx instanceof CParser.ForDeclarationContext)
                || (ctx instanceof CParser.ParameterDeclarationContext)
                || (ctx instanceof CParser.StructDeclarationContext)
        );
        if ( ! precondition ) {
            throw new RuntimeException("ctx is not a declaration");
        }

        if (ctx instanceof CParser.DeclarationContext casted) {
            return casted;
        }
        else if (ctx instanceof CParser.StructDeclarationContext casted) {
            String code = removeBitfields(casted);
            return Parsing.makeParser(code).declaration();
        }
        else {
            return Parsing.makeParser(Parsing.normalizedCode(ctx) + ";").declaration();
        }
    }


    // Parses the declarator part of a declaration. The general form of a declaration is something like this:
    // basetype declarator1, declarator2, ...;
    // This class is intended to be like a function. Immediately upon construction, it does its thing and stores the results in the created instance.
    //dontdo Function pointers and abstract declarators are not supported. Abstract declarators probably need a different design, unfortunately, but my intention was not to write a full C interpreter.
    static class DeclaratorParser {
        NormalForm.Type T;
        String name;

        public DeclaratorParser(NormalForm.Type baseType, CParser.DeclaratorContext declarator)
        {
            // In parsing complex declarators, the name is always the first identifier. For example:
            // int *((*arr)[10][some_size])
            // More identifiers may occur in array sizes, but the name is always on the left.
            var identifiers = new ArrayList<String>();
            Parsing.getIdentifiers(declarator, identifiers);
            if (identifiers.isEmpty()) throw new RuntimeException("declarator has no name");
            name = identifiers.get(0);

            var strDeclarator = Parsing.normalizedCode(declarator);
            // having the name between parentheses reduces the number of cases the parser needs to handle
            strDeclarator = strDeclarator.replace(name, "( " + name + " )"); //the spaces are important because of normalized code assumptions
            T = parseRecursive(baseType, strDeclarator);
        }

        // removes round parentheses (and unnecessary spaces)
        public static String stripParens(String a)
        {
            while(true) {
                a = a.trim();
                if (a.length()==0) break;
                if (a.charAt(0) != '(') break;
                if (a.charAt(a.length() - 1) != ')') break;
                a = a.substring(1, a.length() - 1);
            }
            return a.trim();
        }

        // This function does the real work.
        //
        // Explanation: reading a declarator (starting from the name) can be summarized as "go right when you can, go left when you must" (quote from http://unixwiz.net/techtips/reading-cdecl.html which also explains how it works in more detail).
        // An example:
        //      int **( *(name)[10][n] )[20];
        // This means (using the rule of when to go right and left):
        // name is
        //      an array of size 10 of
        //      arrays of size n of
        //      pointers to
        //      array of size 20 of
        //      pointers to pointers to
        //      int
        //
        // However, the strategy here is to parse from outside to inside. No matter how complex the declarator is, it is always of a form like this:
        // (( ****subdeclarator[1][n] ))
        // That is: it may be wrapped in any number of round parentheses. The interior starts with some stars (possibly 0), and ends with a sequence square brackets (possibly 0). After removing those, what is left is a subdeclarator that has the same form, and thus can be parsed recursively.
        //
        // Reading the example from outside to inside (like this parser does):
        // let T1 =
        //       array of size 20 of
        //       pointers to pointers to
        //       int
        // name is
        //      an array of size 10 of
        //      arrays of size n of
        //      pointers to
        //      T1
        //
        private NormalForm.Type parseRecursive(NormalForm.Type baseType, String strDeclarator)
        {
            // This copy of strDeclarator is repeatedly shortened during this function as it is parsed.
            String a = strDeclarator;
            a = stripParens(a);

            if (a.equals(name)) {
                return baseType;
            }

            int starCount = 0;
            while(true)
            {
                a = a.trim();
                if (a.charAt(0) != '*') break;

                starCount++;
                a = a.substring(1);
            }

            // After the stars follows a subdeclarator, which is always between parenthesis because of what the constructor does. I parse the subdeclarator with the CParser to obtain its length, so that I know where the square brackets start. Note that this only works because I skip the starting parenthesis. That makes the CParser not count the square brackets as part of the declarator. For example, parsing "**arr[10][n])[m]" (note the two closing parenthesis) results in only "**arr[10][n]" being parsed.
            if (a.charAt(0) != '(')  throw new RuntimeException("declarator " + strDeclarator + " has unexpected form");

            String strSubdeclarator = Parsing.normalizedCode(
                Parsing.makeParser(a.substring(1)).declarator()
            );
            a = a.substring(strSubdeclarator.length() + 4); //+4 for the parentheses and surrounding space (this works because the code is normalized)
            a = a.trim();

            // handle the square brackets
            var arraySizes = new ArrayList<String>();
            while(true)
            {
                a = a.trim();
                if (a.length() == 0) break;
                if (a.charAt(0) != '[') break;

                String strSize;
                if (a.charAt(1) == ']')
                    strSize = "";
                else
                    strSize = Parsing.normalizedCode(
                        Parsing.makeParser(a.substring(1)).expression()
                    );
                arraySizes.add(strSize);
                a = a.substring(strSize.length() + 4); //+4 for the brackets and surrounding space
            }

            // construct the new base type
            for (int i=0; i<starCount; i++) {
                baseType = new NormalForm.Pointer(baseType);
            }
            for (int i=arraySizes.size()-1; i>=0; i--) {
                var strSize = arraySizes.get(i);
                try {
                    int size = Integer.parseInt(strSize);
                    baseType = new NormalForm.Array(baseType, size);
                } catch (NumberFormatException e) {
                    baseType = new NormalForm.VariableLengthArray(baseType);
                }
            }
            return parseRecursive(baseType, strSubdeclarator);
        }
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
        var normalizedCode = Parsing.normalizedCode(declarationContext);

        // get all type specifiers
        var typeSpecifiers = new ArrayList<String>();
        (new CBaseVisitor<Void>() {
            @Override
            public Void visitTypeSpecifier(CParser.TypeSpecifierContext ctx) {
                typeSpecifiers.add(Parsing.normalizedCode(ctx));
                return null;
            }
        }).visit(declarationSpecifiers);


        // typedefs have the same syntax as other declarations, except what would normally be variable names are now type names, so the same logic can be used for parsing (including handling of that annoying bug (see below)).
        boolean isTypedef = normalizedCode.startsWith("typedef");


        // Here I handle a bug in the C grammar. The code "int i;" is parsed as a declarationSpecifier list containing the elements "int" and "i", even though "i" is not a declarationSpecifier. It happens because a declarationSpecifier can be a typeSpecifier, which in turn can be a typedefName, which can be a general Identifier, which "i" is. This is clearly wrong, because when the declaration declares a true list of names, that is more than 1, such as in "int i, j", only "int" is considered a specifier and the rest is parsed as a initDeclaratorList.
        // Why does the following loop correctly recognize the bug: If none of the breaks are triggered, the type that was found has the form
        //    [one or more type specifiers] typedefname
        // which in C declares a variable with the typeDefName as name. I tested it with clang. You can do for example:
        // typedef int myint;
        // void f() {
        //     unsigned myint;
        //     myint = 10;
        // }
        // From this it can be concluded that "unsigned myint;" does not declare 0 variables of type "unsigned myint", but actually declares a variable named myint, overwriting the meaning of the typeDefName in that scope. So even if the variable name is actually a valid typeDefName, it should still be parsed as a variable name.
        String buggedName = null;
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
            buggedName = typeSpecifiers.get(lastIdx);
            typeSpecifiers.remove(lastIdx);
        } while(false);


        // concatenate the typeSpecifiers to form the base type
        // I call this the "base" type because we don't know the full type yet. Some of the declared variables may have pointer or array type, while others do not. For example, this can be done in C: "int i, *j, k[10];" to define an int, a pointer to an int and an array of ints in one declaration.
        String strBaseType = String.join(" ", typeSpecifiers);
        var baseType = new NormalForm.Unprocessed(strBaseType);

        // if the base type is a struct or union with a name, add it to the NameInfo object
        boolean isStruct = strBaseType.startsWith("struct");
        boolean isUnion = strBaseType.startsWith("union");
        if (isStruct || isUnion)
        {
            if (typeSpecifiers.size() > 1) throw new RuntimeException("todo does this occur?"); //should not happen because as far as I know "unsigned" and "signed" are the only typeSpecifiers that can occur as an additional specifier for another type, and there is no such thing as an unsigned or signed struct.

            var parser = Parsing.makeParser(strBaseType);
            var structOrUnionSpecifier = parser.structOrUnionSpecifier();
            Parsing.assertNoErrors(parser);

            // If declarations is not null, the struct specifier defines the contents of a struct, so it is a definition. If it also gives a name to the struct, we save that name.
            var declarations = Optional.of(structOrUnionSpecifier)
                .map(x->x.structDeclarationList())
                .map(x->x.structDeclaration())
                .orElse(null);
            var name = structOrUnionSpecifier.Identifier();

            if (name != null && declarations != null) {
                var elt = new NameInfo.TypeInfo();
                elt.name = (isStruct ? "struct" : "union") + ' ' + name;
                elt.scope = scope;
                elt.T = baseType;
                dest.add(elt);
            }
        }


        // Add any declared variables/typenames to the NameInfo object
        if (buggedName != null) {
            if (isTypedef) {
                var elt = new NameInfo.TypeInfo();
                elt.scope = scope;
                elt.name = buggedName;
                elt.T = baseType; //is never a pointer or array because the bug does not occur with those
                dest.add(elt);
            }
            else {
                var elt = new NameInfo.VariableInfo();
                elt.scope = scope;
                elt.name = buggedName;
                elt.typeInfo.T = baseType; //is never a pointer or array because the bug does not occur with those
                dest.add(elt);
            }
        }
        else if (initDeclaratorList != null) {
            var initDeclarators = initDeclaratorList.initDeclarator();

            for (var initDeclarator : initDeclarators)
            {
                try {
                    var parsed = new DeclaratorParser(baseType, initDeclarator.declarator());
                    if (isTypedef) {
                        var elt = new NameInfo.TypeInfo();
                        elt.scope = scope;
                        elt.name = parsed.name;
                        elt.T = parsed.T;
                        dest.add(elt);
                    }
                    else {
                        var elt = new NameInfo.VariableInfo();
                        elt.scope = scope;
                        elt.name = parsed.name;
                        elt.typeInfo.T = parsed.T;
                        dest.add(elt);
                    }
                }
                catch (Exception e) { //ignore this declarator be tolerant of errors in the C code
                    // todo if this only happens with function/enum declarations, remove the printf, because that's expected
                    System.out.println("ignored exception in parsing declarator" + initDeclarator.getText() + ", message:" + e.getMessage());
                }
            }
        }
    }


    // Because the declaration parser leaves types partially unparsed, this function exists to finish the parsing where needed. This uses the declaration parser again, because structs/unions contain declarations.
    //
    // This function may modify T.
    public static NormalForm.Type parseCompletely(NormalForm.Type T, NameInfo nameInfo)
    {
        if (T instanceof NormalForm.Unprocessed casted) {
            T = parseBaseType(casted.specifier, nameInfo);
        }
        else if (T instanceof NormalForm.Builtin) {
            // nothing needs to be done
        }
        else if (T instanceof NormalForm.Pointer casted) {
            casted.T = parseCompletely(casted.T, nameInfo);
        }
        else if (T instanceof NormalForm.Array casted) {
            casted.T = parseCompletely(casted.T, nameInfo);
        }
        else if (T instanceof NormalForm.VariableLengthArray casted) {
            casted.T = parseCompletely(casted.T, nameInfo);
        }
        else if (T instanceof NormalForm.Struct casted) {
            casted.members.replaceAll(t -> parseCompletely(t, nameInfo));
        }
        else {
            assert false : "should be exhaustive";
        }
        return T;
    }




    // parameter code is normalized code (see function Parsing.normalizedCode)
    // The code must be a type specifier for a "base" type. That is anything that is not a pointer, array, pointer to array etc. Everything to do with arrays and pointer is part of the declarator, which is already parsed by the declarator parser, so it is not necessary for this function to handle those cases.
    // A base type can, however, be a struct, and structs can contain declarations of arrays, pointers and combinations of those. The declaration parser is used to parse those declarations.
    public static NormalForm.Type parseBaseType(String code, NameInfo nameInfo)
    {
        var parser = Parsing.makeParser(code);
        var typeSpecifier = parser.typeSpecifier();
        Parsing.assertNoErrors(parser);

        // base case: it's a builtin type
        if (NormalForm.builtins.contains(typeSpecifier.getText()))
        {
            // Because of how signed and unsigned work, there may be another type specifier, so I give the entire code to the constructor.
            return new NormalForm.Builtin(code);
        }


        if (typeSpecifier.typedefName() != null)
        {
            var name = typeSpecifier.typedefName().getText();
            var typeInfo = nameInfo.getTypeInfo(name); //throws if not found
            return parseCompletely(typeInfo.T, nameInfo);
        }


        boolean isStruct = code.startsWith("struct");
        boolean isUnion = code.startsWith("union");
        if (isStruct || isUnion)
        {
            var structOrUnionSpecifier = typeSpecifier.structOrUnionSpecifier();
            assert structOrUnionSpecifier != null;
            var declarations = Optional.of(structOrUnionSpecifier)
                .map(x->x.structDeclarationList())
                .map(x->x.structDeclaration())
                .orElse(null);

            // When there are no declarations, it's a specifier like "struct structname", so there must be a name that is defined elsewhere. Otherwise it may not have a name as in "struct{int i;}". This is also clear from the grammar rule:
            // structOrUnionSpecifier
            //     :   structOrUnion Identifier? '{' structDeclarationList '}'
            //     |   structOrUnion Identifier
            // ;
            if (declarations == null) {
                // The full name in nameInfo includes the word struct or union.
                var name =
                    structOrUnionSpecifier.structOrUnion().getText()
                    + " " + structOrUnionSpecifier.Identifier().getText();
                // Try to look up the name and continue parsing. If it doesn't exist, the type is an incomplete type and I replace it with void because I don't fully support incomplete types (see also the explanation above class NormalForm).
                NormalForm.Type lookedUpType;
                try {
                    var typeInfo = nameInfo.getTypeInfo(name);
                    lookedUpType = typeInfo.T;
                }
                catch (Exception e) {
                    return new NormalForm.Builtin("void");
                }
                return parseCompletely(lookedUpType, nameInfo);
            }
            else {
                // find and parse all struct members
                nameInfo.addScope();

                var members = new ArrayList<NormalForm.Type>();
                for (var declaration : declarations) {
                    parseDeclaration(declaration, nameInfo, NameInfo.EScope.struct);
                }
                var addedNames = nameInfo.currentScope().getNames();
                for (var nameInfoElt : addedNames) {
                    if (nameInfoElt instanceof NameInfo.VariableInfo asVarInfo) {
                        var memberType = parseCompletely(asVarInfo.typeInfo.T, nameInfo);
                        members.add(memberType);
                    }
                }

                nameInfo.popScope();

                var result = new NormalForm.Struct();
                result.members = members;
                return result;
            }
        }

        // This may happen if the type is something else that I don't support such as enum or _Complex.
        throw new RuntimeException("parsing base type matches no case");
    }


    // recovers DataStructureCodeMarkers from any C code
    //
    // Every DataStructureCodeMarker created by the producer is represented in the original C code as a function call with two parameters:
    // 1. the string representation of the code marker;
    // 2. the address of the variable, of which we want to know if the decompiler can determine its type correctly.
    //
    // After decompilation, some differences may occur. For example, the decompiled function call may have extra parameters. We want to have some tolerance for such mistakes, so we make as few assumptions about the structure of the C code as possible. What is assumed is the following:
    // - Code marker strings may occur anywhere. They are easily recognized by their characteristic prefix, which is highly unlikely to occur in code naturally.
    // - After a code marker string there is a comma (the separator for function call parameters) followed by an expression. This expression is the memory address of the variable being tested.
    // todo Jaap is able to recover codemarkers even if the string is not inside the function call. Example situation:
    // char* str = "metadata";
    // __CM_printf_ptr(str, &variableName);
    public static List<Testcase> findTestcasesInCode(String code, NameInfo nameInfo) {
        var ret = new ArrayList<Testcase>();

        while (true) {
            // find the next codemarker
            var codemarkerStartIndex = code.indexOf(DataStructureCodeMarker.characteristic);
            if (codemarkerStartIndex == -1) break;
            var codemarkerEndIndex = code.indexOf('\"', codemarkerStartIndex); //this works because code markers don't contain quotes
            if (codemarkerEndIndex == -1) {
                System.out.println("error in code marker string: no ending quote.");
                System.out.println("remaining code: " + code.substring(codemarkerStartIndex));
                break;
            }
            var codemarkerString = code.substring(codemarkerStartIndex, codemarkerEndIndex);

            // throw away all but the remaining code for further parsing
            code = code.substring(codemarkerEndIndex + 1); //+1 skips the quote
            code = code.trim();

            CodeMarker codemarker;
            {
                codemarker = new BaseCodeMarker(EFeaturePrefix.DATASTRUCTUREFEATURE);
                boolean parseResult = codemarker.fromString(codemarkerString);
                if ( ! parseResult) {
                    System.out.println("error in parsing codemarker string: " + codemarkerString);
                    continue;
                }
            }

            // parse variable address expression
            if (code.charAt(0) != ',') {
                System.out.println("error in parsing codemarker: comma expected.");
                System.out.println("codemarker: " + codemarkerString);
                System.out.println("remaining code: " + code);
                continue;
            }
            code = code.substring(1); //removes the comma

            // Use the C parser to parse the remaining code as an expression
            // Parsing stops when the expression ends, so it doesn't matter if there is more code. One special case of this is important though: if the decompiler has created too many function parameters, which are separated by commas, all parameters are interpreted as one big comma-separated compound expression, of which we need only the first element. If at any point parsing fails, the parser stops parsing, but the successfully parsed parts are preserved, and we only need the first one, so this should work reliably.
            var parser = Parsing.makeParser(code);
            var subexprs = Optional.of(parser.expression())
                .map(x -> x.assignmentExpression())
                .orElse(null);
            if (subexprs == null || subexprs.isEmpty()) {
                System.out.println("error in parsing variable address expression");
                System.out.println("codemarker string: " + codemarkerString);
                System.out.println("remaining code: " + code);
                continue;
            }
            var variableAddressExpr = subexprs.get(0);

            // Next step: extract the name of the variable being referred to in the expression
            // We first find all identifiers in the expression. Then we ignore those that are not a variable name that is currently in scope. That should leave exactly one result. If not, that indicates a decompilation problem. Note that there may be other identifiers if there is, for example, a cast like "(typeName)variableName".
            var identifiers = new ArrayList<String>();
            Parsing.getIdentifiers(variableAddressExpr, identifiers);
            int variables_found = 0;
            String variableName = null;
            for (var identifier : identifiers) {
                try {
                    nameInfo.getVariableInfo(identifier); //throws if not found
                    variableName = identifier;
                    variables_found++;
                }
                catch (Exception ignored){}
            }

            // create and add testcase
            var testcase = new Testcase();
            testcase.codemarker = codemarker;
            testcase.variableAddressExpr = Parsing.normalizedCode(variableAddressExpr);
            if (variables_found == 1) testcase.status = Testcase.Status.ok;
            else                      testcase.status = Testcase.Status.variableNotFound;

            if (testcase.status == Testcase.Status.ok)
            {
                var varInfo = nameInfo.getVariableInfo(variableName);
                // The varInfo object may contain a partially unparsed type because parseDeclaration is lazy. It will now be completely parsed.
                varInfo.typeInfo.T = parseCompletely(varInfo.typeInfo.T, nameInfo);
                testcase.varInfo = varInfo;
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
        var foundTestcases = findTestcasesInCode(Parsing.normalizedCode(ctx), nameInfo);
        recovered_testcases.addAll(foundTestcases);
        return null;
    }
}
