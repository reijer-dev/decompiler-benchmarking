package nl.ou.debm.common.feature2;

import nl.ou.debm.common.antlr.CBaseVisitor;
import nl.ou.debm.common.antlr.CLexer;
import nl.ou.debm.common.antlr.CParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class DataStructureCVisitor extends CBaseVisitor<Object>
{
    //
    //  Classes used in this namespace
    //

    enum ScopeKind {
        global, local, functionParameter, forDeclaration
    }

    //toto reconsider
    static class Testcase {
        enum Status {
            ok, variableNotFound, tooManyVariables
        }
        Status status;
        String strCodeMarker;
        String variableAddressExpr; //variable address expression created by the decompiler. This is the second argument of a DataStructureCodeMarker function call.
        SingleVariableInfo varInfo;

    }

    // Data class for an intermediate step in the processing of decompiled code. At this point, the name and type of a variable are known, but the type specifier is not yet parsed. It is raw code that specifies a type, for example "unsigned int", "struct {int i;}", "struct name" or just "name" if name is a valid typedef. Anything that specifies a type in C.
    static class SingleVariableInfo {
        String name;
        String baseTypeSpec;
        boolean isPointer;
        ScopeKind scopeKind;
    }

    // purpose: contain data about all variables in some scope
    static class ScopeVariableInfo {
        private ArrayList<SingleVariableInfo> variables = new ArrayList<>();
        private HashMap<String, Integer> indices = new HashMap<>(); //maps a variable name to its index in variables

        public boolean contains(String name) {
            var idx = indices.get(name);
            return idx != null;
        }
        public SingleVariableInfo get(String name) {
            var idx = indices.get(name);
            if (idx == null) return null;
            else             return variables.get(idx);
        }
        public void add(SingleVariableInfo variable) {
            variables.add(variable);
            var idx = variables.size() - 1;
            if (indices.containsKey(idx)) throw new RuntimeException("variable name " + variable.name + " occurs multiple times within the same scope. This is not supported."); //todo can this be somewhow tolerated?
            indices.put(variable.name, idx);
        }
    }

    // generalization of VariableInfo for nested scopes. A block in C code creates a new scope, which is represented in this class by adding a new ScopeVariableInfo to a stack of scopes. The meaning of a variable name is always the most recent one, so to get more information about a name, the stack of scopes must be iterated in reverse order.
    // Note: this class is used to keep track of which variables are in scope at any one time while traversing a parse tree. In that use case, it is constantly modified to remain up to date. This makes it easy to determine, for each code marker that is found, which variable it refers to, as it must be a variable that is currently in scope.
    static class NestedVariableInfo {
        private ArrayList<ScopeVariableInfo> scopeStack = new ArrayList<>(1); //not of type Stack because I need to iterate over it, but elements are only appended and popped.

        public NestedVariableInfo() {
            //create an initial scope
            addScope();
        }
        public NestedVariableInfo(ScopeVariableInfo initial_scope) {
            scopeStack.add(initial_scope);
        }

        public boolean contains(String name) {
            for (int i=scopeStack.size()-1; i>=0; i--) {
                var scope = scopeStack.get(i);
                if (scope.contains(name)) return true;
            }
            return false;
        }
        public SingleVariableInfo get(String name) {
            for (int i=scopeStack.size()-1; i>=0; i--) {
                var scope = scopeStack.get(i);
                if (scope.contains(name)) return scope.get(name);
            }
            return null;
        }
        // adds to the latest scope
        public void add(SingleVariableInfo variable) {
            var last_scope = scopeStack.get(scopeStack.size()-1);
            last_scope.add(variable);
        }

        // returns the newly added scope
        public ScopeVariableInfo addScope() {
            var ret = new ScopeVariableInfo();
            scopeStack.add(ret);
            return ret;
        }
        // removes and returns the top scope
        public ScopeVariableInfo popScope() {
            assert scopeStack.size() > 0;
            var ret = scopeStack.get(scopeStack.size() - 1);
            scopeStack.remove(scopeStack.size() - 1);
            return ret;
        }
        // same as popScope except the top scope is not removed
        public ScopeVariableInfo currentScope() {
            assert scopeStack.size() > 0;
            var ret = scopeStack.get(scopeStack.size() - 1);
            return ret;
        }
    }


    //
    //  Data members
    //
    ScopeVariableInfo globals = new ScopeVariableInfo();
    ArrayList<Testcase> recovered_testcases = new ArrayList<>();
    ArrayList<String> structDeclarations; //todo does this only contain structs?



    //
    //  Functions
    //

    //todo generally useful
    // This is an alternative to ctx.getText which doesn't return the original code. Instead, it gives code without spaces, which is sometimes problematic.
    public static String originalCode(ParserRuleContext ctx) {
        int a = ctx.start.getStartIndex();
        int b = ctx.stop.getStopIndex();
        Interval interval = new Interval(a,b);
        return ctx.start.getInputStream().getText(interval);
    }


    // Recovers DataStructureCodemarkers from a function definition context
    public ArrayList<Testcase> findTestcases(CParser.FunctionDefinitionContext ctx)
    {
        var testcases = new ArrayList<Testcase>();
        var variableInfo = new NestedVariableInfo(globals); //start with the global scope because function scope includes the global scope
        variableInfo.addScope(); //add a scope for the current function
        //todo parse function parameters and add them in a separate scope

        var visitor = new CBaseVisitor<Void>() {
            @Override
            public Void visitStatement(CParser.StatementContext ctx)
            {
                // This function doesn't do much with the statements themselves. It just handles a few cases where scopes must be created before further traversal of the parse tree.

                // check if this statement is a block statement. Then it needs its own scope.
                var compoundStatement = ctx.compoundStatement();
                if (compoundStatement != null) {
                    variableInfo.addScope();
                    visitChildren(ctx);
                    variableInfo.popScope();
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
                    var forLoopScope = variableInfo.addScope();
                    parseDeclaration(forDeclaration, forLoopScope, ScopeKind.forDeclaration);

                    // Continue parsing the for loop statement
                    // Note: this also traverses the forDeclaration again. Although it will probably never be useful, if the forDeclaration somehow contains a codemarker, it will be found and it may even refer to a variable from that same declaration, because they've been added to the variableInfo at this point.
                    visitChildren(ctx);
                    variableInfo.popScope();
                    return null;
                }

                // Do the default behavior for all other statements. This includes for loops without a declaration.
                visitChildren(ctx);
                return null;
            }
            @Override
            public Void visitDeclaration(CParser.DeclarationContext ctx) {
                parseDeclaration(ctx, variableInfo.currentScope(), ScopeKind.local);
                System.out.println("declaration found: " + originalCode(ctx));
                visitChildren(ctx);
                return null;
            }

            // Codemarkers are function calls, and function calls are expressions, so visiting the expressions will find all codemarkers.
            // Every codemarker that is found will hopefully have a second parameter that is (or contains) the name of a variable. Which variable this name refers to is lookup up in variableInfo, which contains information about all variables that are currently in scope.
            @Override
            public Void visitExpression(CParser.ExpressionContext ctx) {
                var foundTestcases = findTestcasesInCode(originalCode(ctx), variableInfo);
                testcases.addAll(foundTestcases);
                return null;
            }
        };

        visitor.visit(ctx);
        return testcases;
    }



    // This function accepts a general ParserRuleContext (the base class for all context classes), because I need to accept two kinds of contexts. That's because the C grammar makes a distinction between "declaration"s, and "forDeclaration"s (declarations made in a for-loop initializer). These are the grammar rules:
    // forDeclaration
    //    :   declarationSpecifiers initDeclaratorList?
    //    ;
    // declaration
    //    :   declarationSpecifiers initDeclaratorList? ';'
    //    |   staticAssertDeclaration
    //    ;
    static void parseDeclaration(ParserRuleContext ctx, ScopeVariableInfo dest, ScopeKind scopeKind)
    {
        CParser.DeclarationSpecifiersContext declarationSpecifiers;
        CParser.InitDeclaratorListContext initDeclaratorList;

        if (ctx instanceof CParser.DeclarationContext) {
            var ctx_cast = (CParser.DeclarationContext)ctx;
            declarationSpecifiers = ctx_cast.declarationSpecifiers();
            initDeclaratorList = ctx_cast.initDeclaratorList();
        }
        else if (ctx instanceof CParser.ForDeclarationContext) {
            var ctx_cast = (CParser.ForDeclarationContext)ctx;
            declarationSpecifiers = ctx_cast.declarationSpecifiers();
            initDeclaratorList = ctx_cast.initDeclaratorList();
        }
        else {
            throw new RuntimeException("ctx is not a declaration context");
        }

        // get all type specifiers
        var typeSpecifiers = new ArrayList<String>();
        (new CBaseVisitor<Void>() {
            @Override
            public Void visitTypeSpecifier(CParser.TypeSpecifierContext ctx) {
                typeSpecifiers.add(originalCode(ctx));
                return null;
            }
        }).visit(declarationSpecifiers);

        String variableNameBug = null;
        if (initDeclaratorList == null) {
            // This should never happen? Yet it happens because of what I think is a bug in the C grammar. The code "int i;" is parsed as a declarationSpecifier list containing the elements "int" and "i", even though "i" is not a specifier. I think it happens because a declarationSpecifier can be a typeSpecifier, which in turn can be a typedefName, which can be a general Identifier, which "i" is.
            // When the declaration declares a true list of names, that is more than 1, such as in "int i, j", only "int" is considered a specifier and the rest is parsed as a initDeclaratorList.
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
                for (var initDeclarator : initDeclarators) {
                    var declarator = initDeclarator.declarator();
                    var isPointer = declarator.pointer() != null;
                    var variableName = declarator.directDeclarator().getText();

                    var info = new SingleVariableInfo();
                    info.scopeKind = scopeKind;
                    info.name = variableName;
                    info.baseTypeSpec = baseTypeSpec;
                    info.isPointer = isPointer;
                    dest.add(info);
                }
            }
        }
        else if (variableNameBug != null) {
            // There was no declarator list because of the C grammar bug. Specific handling of that:
            var info = new SingleVariableInfo();
            info.scopeKind = scopeKind;
            info.name = variableNameBug;
            info.baseTypeSpec = baseTypeSpec;
            info.isPointer = false; // Always false because the bug does not occur with things like "int *i"
            dest.add(info);
        }
    }


    // finds globals
    // This function is not called for declarations in functions, because visitFunctionDefinition doesn't call visitChildren.
    public Object visitDeclaration(CParser.DeclarationContext ctx) {
        if (ctx.initDeclaratorList() == null) {
            //todo wat hiermee te doen? het wordt al snel een enorm complexe toestand omdat er zo veel mogelijk is in C. Je kunt in 1 keer een globale variabele aanmaken van een bepaald structtype, en ook dat structtype een naam geven zodat je er nog meer instanties van kunt maken. Bijv:
            // struct typenaam {
            //      int i;
            // } variablenaam;
            // typename variablenaam2; //nog een instantie
            // daardoor is variabeledeclaraties parsen niet los te zien van voorkomende types parsen
            return null;
        }
        else {
            parseDeclaration(ctx, globals, ScopeKind.global);
        }
        return null;
    }

    @Override
    public Object visitFunctionDefinition(CParser.FunctionDefinitionContext ctx) {
        /*
        Ghidra creates empty structs, with even incorrect C code.
        ANTLR sees these lines as function definitions.
        Therefore, we return when no function body is found
        */
        if (ctx.compoundStatement() == null || ctx.compoundStatement().blockItemList() == null)
            return null;

        var testcases = findTestcases(ctx);
        recovered_testcases.addAll(testcases);

        /*
        // walk through the parse tree of the current function body to gather all declarations
        var declarations = getDeclarations(ctx.compoundStatement());

        System.out.println("function declarator: " + originalCode(ctx.declarator()));
        //System.out.println("function name: " + getFunctionName(ctx));
        if (ctx.declarationSpecifiers() != null)
            System.out.println("function declarationSpecifiers: " + originalCode(ctx.declarationSpecifiers()));
        System.out.println("all declarations in function body:");
        for (var d : declarations) {
            System.out.println(originalCode(d));
        }

        var variableInfo = findVariableInfo(ctx);
*/
        return null;
    }

    // This requires a workaround because the C grammar we use doesn't allow for visiting all identifiers, which would have made this easy.
    public static void getIdentifiers(ParseTree o, List<String> dest)
    {
        // check if the current node has an Identifier method. If so, it may contain an identifier, but the method may also return null so we have to check for that as well.
        for (Method m : o.getClass().getMethods()) {
            if (m.getName().equals("Identifier")) {
                try {
                    Object result = m.invoke(o);
                    if (result instanceof TerminalNode) { //false if result is null
                        String identifier = ((TerminalNode) result).getText();
                        dest.add(identifier);
                    }
                } catch (Exception ignored) {}
                break;
            }
        }
        
        // Check all children for identifiers recursively.
        for (int i=0; i<o.getChildCount(); i++) {
            var child = o.getChild(i);
            getIdentifiers(child, dest);
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
    public static List<Testcase> findTestcasesInCode(String code, NestedVariableInfo variableInfo) {
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
            System.out.println("gevonden codemarker string: " + codemarkerString);
            var codemarker = new DataStructureCodeMarker(codemarkerString);

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
                System.out.println("codemarker: " + codemarkerString);
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
                if (variableInfo.contains(identifier)) {
                    variableName = identifier;
                    variables_found++;
                }
            }

            // create and add testcase
            var testcase = new Testcase();
            testcase.strCodeMarker = codemarkerString;
            testcase.variableAddressExpr = originalCode(variableAddressExpr);
            if (variables_found == 1)      testcase.status = Testcase.Status.ok;
            else if (variables_found == 0) testcase.status = Testcase.Status.variableNotFound;
            else                           testcase.status = Testcase.Status.tooManyVariables;

            if (testcase.status == Testcase.Status.ok)
            {
                testcase.varInfo = variableInfo.get(variableName);
            }

            ret.add(testcase);
        }

        return ret;
    }
    //todo: gaat dit goed om met onverwachte situaties?
}
