package nl.ou.debm.common.feature2;

import nl.ou.debm.assessor.ETestCategories;
import nl.ou.debm.common.CodeMarker;
import nl.ou.debm.common.antlr.CBaseVisitor;
import nl.ou.debm.common.antlr.CLexer;
import nl.ou.debm.common.antlr.CParser;
import nl.ou.debm.common.task.ProcessTask;
import nl.ou.debm.producer.CGenerator;
import nl.ou.debm.producer.DataType;
import nl.ou.debm.producer.Struct;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.util.ArrayList;
import java.util.function.Consumer;



//een programmatje om dingen gerelateerd aan feature2 te testen tijdens ontwikkeling
public class TestMain {

    public static String cm(ETestCategories category, String expected, String name) {
        return new DataStructureCodeMarker(category, expected, name).strPrintf();
    }

    public static String mini_CGenerator()
    {
        var sb = new StringBuilder();
        var local_builtin = ETestCategories.FEATURE2_LOCAL_BUILTIN;
        var local_struct = ETestCategories.FEATURE2_LOCAL_STRUCT;

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

        // In dat laatste, wordt "struct" gezien als typespecifier?

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
            cm(local_builtin, "int", "i"),
            cm(local_builtin, "unsigned", "i"),
            cm(local_builtin, "int", "i")
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
            cm(local_struct, "struct S", "s"),
            cm(local_struct, "struct Named", "s"),
            cm(local_struct, "struct { int i; }", "s")
        ));

        return sb.toString();
    }

    public static void main(String[] args) throws Exception
    {
        //test the C visitor on testcode:
        {
            String code = mini_CGenerator();
            System.out.println("mini_CGenerator code:\n" + code);
            var lexer = new CLexer(CharStreams.fromString(code));
            var parser = new CParser(new CommonTokenStream(lexer));
            var visitor = new DataStructureCVisitor();
            visitor.visit(parser.compilationUnit());

            System.out.println("found testcases: ");
            for (var t : visitor.recovered_testcases) {
                System.out.println("codemarker: " + t.codemarker);
                System.out.println("variableAddressExpr: " + t.variableAddressExpr);
                System.out.println("status: " + t.status);
                if (t.varInfo != null) {
                    System.out.println("varInfo.baseTypeSpec: " + t.varInfo.baseTypeSpec);
                    System.out.println("varInfo.name: " + t.varInfo.name);
                    System.out.println("varInfo.scope: " + t.varInfo.scope);
                    System.out.println("varInfo.isPointer: " + t.varInfo.isPointer);
                }
                else {
                    System.out.println("no variable info because status is not ok");
                }
                System.out.println("");
            }
        }

        //test behavior of getIdentifiers (todo turn these kinds of tests into unit tests)
        // getIdentifiers uses (java) reflection as a workaround for the fact that, due to how the C grammar we use is constructed, the visitor pattern cannot be used to find identifiers.
        {
            String code = "rabbit + (1 + (int)monkey * 10) + 7";
            var lexer = new CLexer(CharStreams.fromString(code));
            var parser = new CParser(new CommonTokenStream(lexer));
            var dest = new ArrayList<String>();
            DataStructureCVisitor.getIdentifiers(parser.expression(), dest);

            System.out.println("gevonden identifiers:");
            for (var s : dest) {
                System.out.println(s);
            }
            System.out.println("");
        }

        // test if declarations in a for loop can be reparsed as a normal declaration (because the C grammas treats them separately, and I don't want to write duplicate code)
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
                    forDeclarations.add(DataStructureCVisitor.originalCode(ctx));
                    return null;
                }
            }).visit(parser.blockItemList());

            // now interpret those as normal declarations. This should work fine.
            for (var forDeclaration : forDeclarations) {
                var lexer1 = new CLexer(CharStreams.fromString(forDeclaration + ";"));
                var parser1 = new CParser(new CommonTokenStream(lexer1));
                var as_normal_declaration = parser1.declaration();
                System.out.println("found forDeclaration: " + forDeclaration);
                System.out.println("parsed as normal declaration: " + DataStructureCVisitor.originalCode(as_normal_declaration));
            }
            System.out.println("");
        }

        // Test extraction of function parameters
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
                        System.out.println("parameter found: " + DataStructureCVisitor.originalCode(parameterDeclaration));
                    }
                    return null;
                }
            }).visit(parser.compilationUnit());
            System.out.println("");
        }

        // Test what is considered a type specifier.
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
                System.out.println(DataStructureCVisitor.originalCode(t));
            }
        }
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
                System.out.println(DataStructureCVisitor.originalCode(t));
            }
        }

        // Test parsing of type declarations. A type declaration is really the same as a normal declaration, because you can create a new type and make variables of that type in 1 statement. Example:
        // struct S {int i;} variablename;
        // The struct that was named by that declaration can be used again, which shows that there really is no difference between type declarations and variable declarations.
        {
            var lexer = new CLexer(CharStreams.fromString("""
                typedef struct {int i;} S;
                struct struct_name {int i;};
                struct struct_name2 {int i;} variable_name;
                struct {int i;} variable_name2;
            """));
            var parser = new CParser(new CommonTokenStream(lexer));

            // Test if all forms are parsed as a declaration (yes).
            var declarations = new ArrayList<String>();
            (new CBaseVisitor<Void>() {
                @Override
                public Void visitDeclaration(CParser.DeclarationContext ctx) {
                    declarations.add(DataStructureCVisitor.originalCode(ctx));
                    return null;
                }
            }).visit(parser.compilationUnit());
            assert declarations.size() == 4;

            var dest = new DataStructureCVisitor.ScopeVariableInfo();
            for (var declaration : declarations)
            {
                System.out.println("parsing as declaration: " + declaration);
                var lexer2 = new CLexer(CharStreams.fromString(declaration));
                var parser2 = new CParser(new CommonTokenStream(lexer2));
                DataStructureCVisitor.parseDeclaration(parser2.declaration(), dest, DataStructureCVisitor.EScope.global);
            }

            System.out.println("\nfound SingleVariableInfo instances: ");
            for (var varInfo : dest.getVariables()) {
                System.out.println("varInfo.baseTypeSpec: " + varInfo.baseTypeSpec);
                System.out.println("varInfo.name: " + varInfo.name);
                System.out.println("varInfo.scope: " + varInfo.scope);
                System.out.println("varInfo.isPointer: " + varInfo.isPointer);
                System.out.println("");
            }
        }
    }
}
