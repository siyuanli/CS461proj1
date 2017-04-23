package bantam.interp;

import bantam.ast.ExprList;
import bantam.ast.Field;
import bantam.ast.Method;
import bantam.util.ClassTreeNode;
import bantam.visitor.Visitor;

import java.util.HashMap;


public class InstantiationVisitor extends Visitor {
    private ObjectData objectData;
    private InterpreterVisitor interpreterVisitor;
    private HashMap<String, Object> fields;
    private HashMap<String, MethodBody> methods;

    public InstantiationVisitor(InterpreterVisitor interpreterVisitor){
        this.interpreterVisitor = interpreterVisitor;
    }

    public void initObject(ObjectData objectData, ClassTreeNode classTreeNode){
        this.objectData = objectData;
        this.addMembers(classTreeNode);
    }

    private void addMembers(ClassTreeNode classTreeNode){
        HashMap<String, Object> childFields = this.fields;
        HashMap<String, MethodBody> childMethods = this.methods;

        if (classTreeNode.isBuiltIn()) {
            switch (classTreeNode.getName()) {
                case "Object":

                    break;

                case "String":

                    break;

                case "Sys":

                    break;

                case "TextIO":

                    break;
            }
        }
        else {

            this.fields = new HashMap<>();
            this.objectData.pushField(fields);

            this.methods = new HashMap<>();
            this.objectData.pushMethods(methods);

        }

        if (classTreeNode.getParent() != null) {
            this.addMembers(classTreeNode.getParent());
            this.fields = childFields;
            this.methods = childMethods;
        }
        classTreeNode.getASTNode().accept(this);
    }

    public Object visit(Field node) {
        Object value = null;
        if (node.getType().equals("int")){
            value = 0;
        }
        else if (node.getType().equals("boolean")){
            value = false;
        }

        if (node.getInit() != null) {
            value = node.getInit().accept(this.interpreterVisitor);
        }

        this.fields.put(node.getName(), value);
        return null;
    }

    public Object visit(Method node) {
        this.methods.put(node.getName(), new MethodBody() {
            @Override
            public Object execute(ExprList actualParams) {
                interpreterVisitor.pushMethodScope();
                for(int i = 0; i< actualParams.getSize();i++){
                    String name = (String)node.getFormalList().get(i).accept(interpreterVisitor);
                    Object data = actualParams.get(i).accept(interpreterVisitor);
                    interpreterVisitor.getCurrentMethodScope().put(name, data);
                }
                Object returnValue = node.accept(interpreterVisitor);
                interpreterVisitor.popMethodScope();
                return returnValue;
            }
        });
        return null;
    }

}
