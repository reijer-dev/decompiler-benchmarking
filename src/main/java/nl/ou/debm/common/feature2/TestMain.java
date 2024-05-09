package nl.ou.debm.common.feature2;

import nl.ou.debm.assessor.ETestCategories;
import nl.ou.debm.common.CompilerConfig;
import nl.ou.debm.common.EArchitecture;
import nl.ou.debm.common.ECompiler;
import nl.ou.debm.common.EOptimize;
import nl.ou.debm.common.antlr.CBaseVisitor;
import nl.ou.debm.common.antlr.CLexer;
import nl.ou.debm.common.antlr.CParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.misc.Utils;
import org.antlr.v4.runtime.tree.Tree;
import org.antlr.v4.runtime.tree.Trees;
import nl.ou.debm.common.BaseCodeMarker;
import nl.ou.debm.common.CodeMarker;
import nl.ou.debm.common.task.ProcessTask;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


// This is a program used to test things during development. todo: convert some things to unit tests



public class TestMain {

    // source: https://stackoverflow.com/a/50068645
    public static class TreeUtils {

        /** Platform dependent end-of-line marker */
        public static final String Eol = System.lineSeparator();
        /** The literal indent char(s) used for pretty-printing */
        public static final String Indents = "  ";
        private static int level;

        private TreeUtils() {}

        public static String toPrettyTree(final Tree t, final List<String> ruleNames) {
            level = 0;
            return process(t, ruleNames).replaceAll("(?m)^\\s+$", "").replaceAll("\\r?\\n\\r?\\n", Eol);
        }

        private static String process(final Tree t, final List<String> ruleNames) {
            if (t.getChildCount() == 0) return Utils.escapeWhitespace(Trees.getNodeText(t, ruleNames), false);
            StringBuilder sb = new StringBuilder();
            sb.append(lead(level));
            level++;
            String s = Utils.escapeWhitespace(Trees.getNodeText(t, ruleNames), false);
            sb.append(s + ' ');
            for (int i = 0; i < t.getChildCount(); i++) {
                sb.append(process(t.getChild(i), ruleNames));
            }
            level--;
            sb.append(lead(level));
            return sb.toString();
        }

        private static String lead(int level) {
            StringBuilder sb = new StringBuilder();
            if (level > 0) {
                sb.append(Eol);
                for (int cnt = 0; cnt < level; cnt++) {
                    sb.append(Indents);
                }
            }
            return sb.toString();
        }
    }

    // contains the rulenames for C
    public static class CTreeShower {
        public static List<String> ruleNamesList;
        public CTreeShower() {
            var lexer = new CLexer(CharStreams.fromString(""));
            var parser = new CParser(new CommonTokenStream(lexer));
            ruleNamesList = Arrays.asList(parser.getRuleNames());
        }

        public String show(ParseTree tree) {
            return TreeUtils.toPrettyTree(tree, ruleNamesList);
        }
    }

    public static String cm(String varname) {
        return new DataStructureCodeMarker(varname).toCode();
    }

    public static String mini_CGenerator()
    {
        var sb = new StringBuilder();

        // wat voor soorten typereferenties zijn er in C:
        // - ingebouwd:
        //      int, float, unsigned enz.
        // - typedefnamen:
        //      alle mogelijke identifiers
        // - struct zonder naam:
        //      struct {int i;}
        // - struct met naam:
        //      struct Naam {int i;}
        // - referentie naar een eerder benoemd struct:
        //      struct Naam


        //helps to make this test code compileable
        sb.append("""
            void DataStructureCodeMarker(char*, void*){};
            int main() { return 0; }
            """);

        sb.append("""
            struct {
                int i;
            } varname;
                    
            struct S {
                int i;
            };
                    
            typedef struct {
                int i;
            } S2;
                    
            struct S2 {
                struct localname {
                    int i;
                };
                struct localname l;
                int i;
            };
            
            typedef int* int_ptr;
            typedef int int_no_ptr;
            typedef struct{int i;}* struct_ptr;
            typedef struct{int i;} struct_no_ptr;
            typedef some_name* some_name_ptr;
            typedef some_name some_name_no_ptr;
            """);

        sb.append("""
            void blockscope_test() {
                int i = 10;
                %s
                {
                    unsigned i;
                    %s
                }
                %s
            }
            """.formatted(
            cm("i"),
            cm("i"),
            cm("i")
        ));

        sb.append("""
            void struct_type_test() {
                {
                    struct S s;
                    %s
                } {
                    struct Named {
                        int i;
                    } s;
                    %s
                } {
                    struct {
                        int i;
                    } s;
                    %s
                }
            }
            """.formatted(
            cm("s"),
            cm("s"),
            cm("s")
        ));

        return sb.toString();
    }

    public static void main(String[] args) throws Exception
    {
        var treeShower = new CTreeShower();

        //test the C visitor on testcode:
        if(false)
        {
            String code = mini_CGenerator();
            System.out.println("mini_CGenerator code:\n" + code);
            var lexer = new CLexer(CharStreams.fromString(code));
            var parser = new CParser(new CommonTokenStream(lexer));
            var visitor = new DataStructureCVisitor(EArchitecture.X64ARCH);
            visitor.visit(parser.compilationUnit());

            System.out.println("found testcases: ");
            for (var t : visitor.recovered_testcases) {
                System.out.println("codemarker: " + t.codemarker);
                System.out.println("status: " + t.status);
                if (t.varInfo != null) {
                    System.out.println(t.varInfo);
                }
                else {
                    System.out.println("no variable info because status is not ok");
                }
                System.out.println("");
            }
        }

        //test behavior of getIdentifiers (todo turn these kinds of tests into unit tests)
        // getIdentifiers uses (java) reflection as a workaround for the fact that, due to how the C grammar we use is constructed, the visitor pattern cannot be used to find identifiers.
        if(false)
        {
            String code = "rabbit + (1 + (int)monkey * 10) + 7";
            var lexer = new CLexer(CharStreams.fromString(code));
            var parser = new CParser(new CommonTokenStream(lexer));
            var dest = new ArrayList<String>();
            Parsing.getIdentifiers(parser.expression(), dest);

            System.out.println("gevonden identifiers:");
            for (var s : dest) {
                System.out.println(s);
            }
            System.out.println("");
        }

        // test if declarations in a for loop can be reparsed as a normal declaration (because the C grammas treats them separately, and I don't want to write duplicate code)
        if(false)
        {
            String code = """
                int i;
                for (int i=10; i<100; i++) { int i=20; }
                for (i=0; i<100; i++) { int i=30; }
                for (int i=10, *j; i<100; i++);
            """;
            var lexer = new CLexer(CharStreams.fromString(code));
            var parser = new CParser(new CommonTokenStream(lexer));

            //find all forDeclarations
            var forDeclarations = new ArrayList<String>();
            (new CBaseVisitor<Void>() {
                @Override
                public Void visitForDeclaration(CParser.ForDeclarationContext ctx) {
                    forDeclarations.add(Parsing.normalizedCode(ctx));
                    return null;
                }
            }).visit(parser.blockItemList());

            // now interpret those as normal declarations. This should work fine.
            for (var forDeclaration : forDeclarations) {
                var lexer1 = new CLexer(CharStreams.fromString(forDeclaration + ";"));
                var parser1 = new CParser(new CommonTokenStream(lexer1));
                var as_normal_declaration = parser1.declaration();
                System.out.println("found forDeclaration: " + forDeclaration);
                System.out.println("parsed as normal declaration: " + Parsing.normalizedCode(as_normal_declaration));
            }
            System.out.println("");
        }

        // Test extraction of function parameters
        if(false)
        {
            String code = """
                void function(int i, void* ptr, void *ptr2, struct S s, some_typename t) {
                }
            """;
            var lexer = new CLexer(CharStreams.fromString(code));
            var parser = new CParser(new CommonTokenStream(lexer));
            (new CBaseVisitor<Void>() {
                @Override
                public Void visitParameterTypeList(CParser.ParameterTypeListContext ctx) {
                    var parameterDeclarations = ctx.parameterList().parameterDeclaration();
                    for (var parameterDeclaration : parameterDeclarations) {
                        System.out.println("parameter found: " + Parsing.normalizedCode(parameterDeclaration));
                    }
                    return null;
                }
            }).visit(parser.compilationUnit());
            System.out.println("");
        }

        // Test what is considered a type specifier.
        if(false)
        {
            //parse this code as a typename. Result: const is not a type specifier, but unsigned and int are (so even though unsigned int is one type, it consists of multiple type specifiers!)
            var lexer = new CLexer(CharStreams.fromString("const unsigned int"));
            var parser = new CParser(new CommonTokenStream(lexer));
            var typeName = parser.typeName();
            var foundTypes = new ArrayList<CParser.TypeSpecifierContext>();
            class Visitor extends CBaseVisitor<Object> {
                public Object visitTypeSpecifier(CParser.TypeSpecifierContext ctx) {
                    foundTypes.add(ctx);
                    return null;
                }
            }
            var visitor = new Visitor();
            visitor.visit(typeName);
            System.out.println("found type specifiers:");
            for (var t : foundTypes) {
                System.out.println(Parsing.normalizedCode(t));
            }
        }
        if(false)
        {
            // Typedefs are considered declarations by the C parser, so I parse this as a declaration. Result: the entire struct is considered one type specifier.
            var lexer = new CLexer(CharStreams.fromString("typedef struct {int i;} S;"));
            var parser = new CParser(new CommonTokenStream(lexer));
            var declaration = parser.declaration();
            var foundTypes = new ArrayList<CParser.TypeSpecifierContext>();
            class Visitor extends CBaseVisitor<Object> {
                public Object visitTypeSpecifier(CParser.TypeSpecifierContext ctx) {
                    foundTypes.add(ctx);
                    return null;
                }
            }
            var visitor = new Visitor();
            visitor.visit(declaration);
            System.out.println("found type specifiers:");
            for (var t : foundTypes) {
                System.out.println(Parsing.normalizedCode(t));
            }
        }

        // Test parsing of type declarations. A type declaration is really the same as a normal declaration, because you can create a new type and make variables of that type in 1 statement. Example:
        // struct S {int i;} variablename;
        // The struct that was named by that declaration can be used again, which shows that there really is no difference between type declarations and variable declarations.
        if(false)
        {
            var lexer = new CLexer(CharStreams.fromString("""
                    typedef struct {int i;} S;
                    struct struct_name {int i;};
                    struct struct_name2 {int i;} variable_name;
                    struct {int i;} variable_name2;
                    
                    typedef int* int_ptr;
                    typedef int int_no_ptr;
                    typedef int int_no_ptr1, int_no_ptr2;
                    typedef struct{int i;}* struct_ptr;
                    typedef struct{int i;} struct_no_ptr;
                    typedef some_name* some_name_ptr;
                    typedef some_name some_name_no_ptr;
                    
                    float arr[10];
                    float vla[n];
                    
                    int i;
                    int i,j;
                    int* i;
                    int *i;
                    int i, *j;
                    int *i, j;
                    int* i, j;
                    
                    int *i[10];
                    int i, *j[10];
                    int *j[10], i;
                    
                    int *difficultArrayExpression[rabbits + functioncall(monkeys) - 20], i;
                    int j[ arr[0] ];
                    
                    union U { int i; float f; } u, us[10];
                    
                    int i[10][20];
                    int i[n][20];
                    int **i;
                    int **i[10];
                    int **i[10][20];
                    
                    int* arr1[8];
                    int (*arr2)[8];
                    int *(arr3[8]);
                    int (*((arr4[8])));
                    
                    int (*((arr5[8]))) = {(int*)90};
                    int (*(*(arr5[8]))) = {(int*)90}, next;
                    
                    int ****(  *nested[5]  )[10][20][30];
                    
                    int function();
                    int function(int i);
                    
                    struct incomplete;
                    struct incomplete varname;
                    
                    struct MyStruct { float f; } inst;
                    struct MyStruct inst2;
                    
                    struct WithComment {
                        int i; //comment
                        void /*comment*/ *v;
                    };
                    
                    typedef enum {
                      LOW = 25,
                      MEDIUM = 50,
                      HIGH = 75
                    } enumName;
                    
                    enum enumName2 {
                        TEST = 10
                    };
                """));
            var parser = new CParser(new CommonTokenStream(lexer));

            var declarations = new ArrayList<String>();
            (new CBaseVisitor<Void>() {
                @Override
                public Void visitDeclaration(CParser.DeclarationContext ctx) {
                    declarations.add(Parsing.normalizedCode(ctx));
                    return null;
                }
            }).visit(parser.compilationUnit());
            //assert declarations.size() == 12;

            var dest = new NameInfo();
            for (var declaration : declarations)
            {
                System.out.println("parsing as declaration: " + declaration);
                var lexer2 = new CLexer(CharStreams.fromString(declaration));
                var parser2 = new CParser(new CommonTokenStream(lexer2));
                var tree = parser2.declaration();

                System.out.println("parsetree: " + treeShower.show(tree));

                DataStructureCVisitor.parseDeclaration(tree, dest, NameInfo.EScope.global);
            }

            System.out.println("\nfound names: ");
            for (var nameInfo : dest.currentScope().getNames()) {
                System.out.println(nameInfo);
            }
        }

        // test DeclaratorParser
        if(false)
        {
            assert DataStructureCVisitor.DeclaratorParser.stripParens("").equals("");
            assert DataStructureCVisitor.DeclaratorParser.stripParens("(a)").equals("a");
            assert DataStructureCVisitor.DeclaratorParser.stripParens("  a ").equals("a");
            assert DataStructureCVisitor.DeclaratorParser.stripParens("a()").equals("a()");
            assert DataStructureCVisitor.DeclaratorParser.stripParens("(( (a)) )").equals("a");
            assert DataStructureCVisitor.DeclaratorParser.stripParens("(a").equals("(a");
            assert DataStructureCVisitor.DeclaratorParser.stripParens("((a)").equals("(a");
            assert DataStructureCVisitor.DeclaratorParser.stripParens("(a))").equals("a)");

            // assumptions made in the implementation
            var decl = Parsing.makeParser("**arr[10][n]))[m]").declarator();
            assert Parsing.normalizedCode(decl).equals("* * arr [ 10 ] [ n ]");

            decl = Parsing.makeParser("name").declarator();
            assert Parsing.normalizedCode(decl).equals("name");

            var expr = Parsing.makeParser("a][10]").expression();
            assert Parsing.normalizedCode(expr).equals("a");

            // behavior tests
            var strDeclarators = new ArrayList<String>();
            strDeclarators.add("arr");
            strDeclarators.add("arr[10]");
            strDeclarators.add("*arr");
            strDeclarators.add("*arr[n]");
            strDeclarators.add("**arr[10][n]");
            strDeclarators.add("*(arr)[10]");
            strDeclarators.add("*(*arr)[10]");
            strDeclarators.add("*(*arr[n])[10]");
            for (var strDeclarator : strDeclarators) {
                var declarator = Parsing.makeParser(strDeclarator).declarator();
                var baseType = new NormalForm.Builtin("int");
                var ret = new DataStructureCVisitor.DeclaratorParser(baseType, declarator);
                System.out.println("declarator parse test: " + strDeclarator);
                System.out.println("variableName: " + ret.name);
                System.out.println("T: " + ret.T);
            }
        }

        // test removeBitfields
        if(false)
        {
            var structSpec = Parsing.makeParser("""
                struct {
                    int i, j : 10;
                    unsigned : 5;
                    unsigned k, : 10;
                    unsigned : 10, L;
                    int ****(  *nested[5]  )[10][20][30];
                };
                """).structOrUnionSpecifier();

            (new CBaseVisitor<Void>() {
                @Override
                public Void visitStructDeclaration(CParser.StructDeclarationContext ctx) {
                    System.out.println("struct declaration found: " + Parsing.normalizedCode(ctx));
                    System.out.println("with bitfields removed  : " + DataStructureCVisitor.removeBitfields(ctx));
                    return null;
                }
            }).visit(structSpec);
        }


        if(false)
        {
            // does this throw? answer: no but getNumberOfSyntaxErrors is nonzero
            try {
                var parser = Parsing.makeParser("121312");
                var result = parser.typedefName();
                System.out.println("result is null? " + (result == null));
                System.out.println("result text: " + result.getText());
                System.out.println("number of errors: " + parser.getNumberOfSyntaxErrors());
            }
            catch (Exception e) {
                System.out.println("in the handler");
            }

            {
                // the second call to typeSpecifier returns the "int" part
                var parser = Parsing.makeParser("unsigned int");
                System.out.println(parser.typeSpecifier().getText());
                System.out.println(parser.typeSpecifier().getText());
            }
        }

        // test normalization with consecutive string literals
        if(false)
        {
            var parser = Parsing.makeParser("""
                void f() {
                    printf("a" "b");
                    printf("c""d");
                    printf(
                        "e"
                        "f"
                    );
                    printf(
                        "\\""
                        "a\\"b"
                    );
                }
                """);
            var tree = parser.functionDefinition();
            System.out.println( treeShower.show(tree) );
            System.out.println("normalized: " + Parsing.normalizedCode(tree));
        }

        // test parseCompletely
        if(false)
        {
            var nameInfo = new NameInfo();
            {
                var x = new NameInfo.TypeInfo();
                x.T = new NormalForm.Builtin("int");
                x.scope = NameInfo.EScope.global;
                x.name = "someName";
                nameInfo.add(x);
            }

            var toParse = new NormalForm.Unprocessed("""
                struct StructName {
                    struct LocalStructName {
                        float f;
                    } inst;
                    someName x;
                    someName *y, *z[10];
                    struct LocalStructName inst2;
                    struct NonExisting *ptr;
                    struct NonExisting nonExistingInst; //invalid C. instance of incomplete type
                    struct StructName* selfReference;
                    unsigned int u1;
                    int unsigned u2;
                    signed s;
                    unsigned u;
                    void* ptr2;
                    _Bool b : 1; //bitfield
                    unsigned : 10; //unnamed bitfield
                }
                """);

            var result = DataStructureCVisitor.parseCompletely(toParse, nameInfo);
            System.out.println("parseCompletely: " + result);

            {
                var parser = Parsing.makeParser("int **( *(name)[10][n] )[20];");
                var declaration = parser.declaration();
                Parsing.assertNoErrors(parser);
                DataStructureCVisitor.parseDeclaration(declaration, nameInfo, NameInfo.EScope.local);
                var partiallyParsed = nameInfo.getVariableInfo("name").typeInfo.T;
                result = DataStructureCVisitor.parseCompletely(partiallyParsed, nameInfo);
                System.out.println("parseCompletely: " + result);
            }

            //test what happens with enums
            //currently the answer is: named enums are ignored. enums in a typedef are kept as unparsed. If an attempt is made to parse, an exception is thrown because an enum does not match any expected case.
            {
                var parser = Parsing.makeParser("""
                    typedef enum {
                      LOW = 25,
                      MEDIUM = 50,
                      HIGH = 75
                    } enumName;
                    
                    enum enumName2 {
                        TEST = 10
                    };
                    """);
                var compUnit = parser.compilationUnit();
                Parsing.assertNoErrors(parser);

                var visitor = new DataStructureCVisitor(EArchitecture.X64ARCH);
                visitor.visit(compUnit);
                visitor.nameInfo.getTypeInfo("int64_t");

                try {
                    var typeInfo = visitor.nameInfo.getTypeInfo("enumName");
                    System.out.println("enumName found");
                    try {
                        var parsedCompletely = DataStructureCVisitor.parseCompletely(typeInfo.T, nameInfo);
                        System.out.println("completely parsed type of enumName: " + parsedCompletely);
                    } catch (Exception e) {
                        System.out.println("unable to completely parse the type of enumName");
                    }
                } catch (Exception e) { System.out.println("enumName not found"); }

                try {
                    var typeInfo = visitor.nameInfo.getTypeInfo("enumName2");
                    System.out.println("enumName2 found");
                } catch (Exception e) { System.out.println("enumName2 not found"); }

                System.out.println("all names:");
                //visitor.nameInfo.currentScope().printNames();
            }
        }
		
        if(false) {
            // test reinterpretation of a datastructure codemarker as a general codemarker. I need to recover only the ID. The functionality of the DataStructureCodeMarker class is only necessary in the producer.
            var cm = new DataStructureCodeMarker("varname");
            var id = cm.lngGetID();
            var str = cm.toString();
            var recovered = new BaseCodeMarker(str);
            var id_recovered = recovered.lngGetID();
            System.out.println("original ID: " + id);
            System.out.println("recovered ID: " + id_recovered);
        }

        //test problematic behavior of the c-parser. A statement like "typename* t;" is parsed as a multiplication, even if typename is indeed a typename, but "int* i;" is parsed as a declaration.
        if(true) {
            var parser = Parsing.makeParser("""
                _DWORD DS_var_3[10]; // weak
                void f() {
                    char* c;
                    char *c;
                    typename* inst;
                    typename * inst;
                    typename *inst;
                    typename * inst, inst2;
                    typename * inst, a * inst2; //this should actually be parsed as multiplications
                    undefined1 ( * in_RDX ) [ 10 ] ;
                }
            """);
            var tree = parser.compilationUnit();
            System.out.println( treeShower.show(tree) );
        }


        if(false) {
            var parser = Parsing.makeParser("""
                float DS_var_8;
                int* test;
                
                void f() {
                    // has no ampersand, so should refer to i
                    {
                        int i;
                        void* p = &i;
                        _CM_printf_ptr("c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8DS>>ID:b835,__POINTER_FIELD__:%p,CHECKSUM:E3FD", (__int64)p);
                    }
                    
                    // already has ampersand, so refers to DS_var_8
                    {
                        _CM_printf_ptr("c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8DS>>ID:b833,__POINTER_FIELD__:%p,CHECKSUM:C91B", (__int64)&DS_var_8);
                    }
                    
                    // should both be assumed to refer to variable test because there is no assignment expression to replace the "test" without ampersand with
                    {
                        _CM_printf_ptr("c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8DS>>ID:b833,__POINTER_FIELD__:%p,CHECKSUM:C91B", test);
                    }
                    {
                        _CM_printf_ptr("c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8DS>>ID:b833,__POINTER_FIELD__:%p,CHECKSUM:C91B", &test);
                    }
                    
                    // following multiple assignment expressions
                    {
                        int i;
                        void* p1 = &i;
                        void* p2 = p1;
                        _CM_printf_ptr("c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8DS>>ID:b833,__POINTER_FIELD__:%p,CHECKSUM:C91B", p2);
                    }
                    
                    // the same, except the assignments are not part of the declaration
                    {
                        int i;
                        void* p1;
                        p1 = &i;
                        void* p2;
                        p2 = p1;
                        _CM_printf_ptr("c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8DS>>ID:b833,__POINTER_FIELD__:%p,CHECKSUM:C91B", p2);
                    }
                }
                """);
            var tree = parser.compilationUnit();
            //System.out.println( treeShower.show(tree) );
            var visitor = new DataStructureCVisitor(EArchitecture.X64ARCH);
            visitor.visit(tree);
            for (var testcase : visitor.recovered_testcases) {
                System.out.println("testcase recovered: " + testcase.codemarker + ", " + testcase.varInfo);
            }
        }

        if(false) {
            for (int i=0; i<1000; i++) {
                int r = DataStructureProducer.randomIntBiased(3, Integer.MAX_VALUE);
                System.out.println(r);
            }
        }

        if(false) {
            var parser = Parsing.makeParser("""
                    struct_type s;
                """);
            var tree = parser.declaration();
            System.out.println( treeShower.show(tree) );
            var nameInfo = new NameInfo();
            DataStructureCVisitor.parseDeclaration(tree, nameInfo, NameInfo.EScope.global);
            System.out.println("defined names: ");
            nameInfo.currentScope().printNames();
        }
    }
}
