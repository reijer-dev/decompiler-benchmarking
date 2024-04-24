package nl.ou.debm.common.feature2;

import java.util.ArrayList;
import java.util.HashMap;

// todo explain
// generalization of VariableInfo for nested scopes. A block in C code creates a new scope, which is represented in this class by adding a new ScopeVariableInfo to a stack of scopes. The meaning of a variable name is always the most recent one, so to get more information about a name, the stack of scopes must be iterated in reverse order.
// Note: this class is used to keep track of which variables are in scope at any one time while traversing a parse tree. In that use case, it is constantly modified to remain up to date. This makes it easy to determine, for each code marker that is found, which variable it refers to, as it must be a variable that is currently in scope.
public class NameInfo {

    //
    // Static classes
    //

    public enum EScope {
        global, local, struct, functionParameter, forDeclaration
    }

    // This is supposed to be a union like type. A "NameInfoElt" object can hold either a VariableInfo or TypeInfo value.
    public static sealed class NameInfoElt permits VariableInfo, TypeInfo {
        public String name;
        public EScope scope;

        public String toString() {
            var sb = new StringBuilder();
            sb.append("name: " + name + "\n");

            String kind;
            if (this instanceof VariableInfo) kind = "VariableInfo";
            else                              kind = "TypeInfo";
            sb.append("kind: " + kind + "\n");

            sb.append("scope: " + scope + "\n");

            TypeInfo typeInfo;
            if (this instanceof VariableInfo casted) {
                typeInfo = casted.typeInfo;
            } else {
                typeInfo = (TypeInfo)this;
            }
            sb.append("typeInfo.T: " + typeInfo.T + "\n");

            return sb.toString();
        }
    }


    // Data class for an intermediate step in the processing of decompiled code. At this point, the name and type of a variable are known, but the type specifier is not yet parsed. It is raw code that specifies a type, for example "unsigned int", "struct {int i;}", "struct name" or just "name" if name is a valid typedef. strType can be anything that specifies a type in C.
    public static final class TypeInfo extends NameInfoElt {
        public NormalForm.Type T;
    }

    public static final class VariableInfo extends NameInfoElt {
        public TypeInfo typeInfo = new TypeInfo();
    }


    // purpose: contain data about all variables in a single scope
    public static class ScopeNameInfo {
        private ArrayList<NameInfoElt> names = new ArrayList<>();
        private HashMap<String, Integer> indices = new HashMap<>(); //maps a variable name to its index in variables

        public boolean contains(String name) {
            var idx = indices.get(name);
            return idx != null;
        }
        public NameInfoElt get(String name) {
            var idx = indices.get(name);
            if (idx == null) return null;
            else             return names.get(idx);
        }
        public void add(NameInfoElt elt) {
            names.add(elt);
            var idx = names.size() - 1;
            if (indices.containsKey(idx)) throw new RuntimeException("variable name " + elt.name + " occurs multiple times within the same scope. This is not supported."); //todo can this be somewhow tolerated?
            indices.put(elt.name, idx);
        }

        //dont write to this list
        public ArrayList<NameInfoElt> getNames() {
            return names;
        }
    }


    //
    //  Class contents
    //

    private ArrayList<ScopeNameInfo> scopeStack = new ArrayList<>(); //not of type Stack because I need to iterate over it, but elements are only appended and popped.

    public NameInfo() {
        //create an initial scope
        addScope();
    }
    public NameInfo(ScopeNameInfo initial_scope) {
        scopeStack.add(initial_scope);
    }

    public boolean contains(String name) {
        for (int i=scopeStack.size()-1; i>=0; i--) {
            var scope = scopeStack.get(i);
            if (scope.contains(name)) return true;
        }
        return false;
    }
    private NameInfoElt get(String name) {
        for (int i=scopeStack.size()-1; i>=0; i--) {
            var scope = scopeStack.get(i);
            if (scope.contains(name)) return scope.get(name);
        }
        return null;
    }
    // adds to the latest scope
    public void add(NameInfoElt elt) {
        var last_scope = scopeStack.get(scopeStack.size()-1);
        last_scope.add(elt);
    }

    // returns the newly added scope
    public ScopeNameInfo addScope() {
        var ret = new ScopeNameInfo();
        scopeStack.add(ret);
        return ret;
    }
    // removes and returns the top scope
    public ScopeNameInfo popScope() {
        assert scopeStack.size() > 0;
        var ret = scopeStack.get(scopeStack.size() - 1);
        scopeStack.remove(scopeStack.size() - 1);
        return ret;
    }
    // same as popScope except the top scope is not removed
    public ScopeNameInfo currentScope() {
        assert scopeStack.size() > 0;
        var ret = scopeStack.get(scopeStack.size() - 1);
        return ret;
    }

    public VariableInfo getVariableInfo(String name) {
        var nameInfoElt = get(name);
        if (nameInfoElt instanceof VariableInfo casted) {
            return casted;
        }
        else if (nameInfoElt == null) {
            throw new RuntimeException("name " + name + " is not in scope");
        }
        else {
            throw new RuntimeException("name " + name + " is not a type name");
        }
    }

    public TypeInfo getTypeInfo(String name) {
        var nameInfoElt = get(name);
        if (nameInfoElt instanceof TypeInfo casted) {
            return casted;
        }
        else if (nameInfoElt == null) {
            throw new RuntimeException("name " + name + " is not in scope");
        }
        else {
            throw new RuntimeException("name " + name + " is not a variable name");
        }
    }

    public int stackSize() { return scopeStack.size(); }
}
