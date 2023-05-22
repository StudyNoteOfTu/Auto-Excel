package utils.bytecode;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;

import java.util.Map;

public class DBBytecodeGenerator extends DefaultBytecodeGenerator {
    public DBBytecodeGenerator(String clzName) {
        super(clzName);
    }

    @Override
    protected void cwAnnotationVisit(ClassWriter cw) {
        //注入 @TableName 注解
        AnnotationVisitor annotationVisitor = cw.visitAnnotation("Ltemplate/annotations/db/TableName;", true);
        annotationVisitor.visit("value","auto_"+getName());
        annotationVisitor.visitEnd();
    }

    @Override
    protected void fieldVisitInner1(Map.Entry<Integer, String> entry, int index, FieldVisitor fieldVisitor) {
        //这里可以对entry做判断，例如判断是否设置为主键等
        //额外注入@ColumnInfo注解
        AnnotationVisitor annotationVisitor = fieldVisitor.visitAnnotation("Ltemplate/annotations/db/ColumnInfo;", true);
        annotationVisitor.visitEnd();
    }
}
