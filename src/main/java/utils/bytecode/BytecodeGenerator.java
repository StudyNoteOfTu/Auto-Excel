package utils.bytecode;

import org.objectweb.asm.*;

import java.util.Map;

import static org.objectweb.asm.Opcodes.*;

/**
 * 给到默认的字节码生成结构，并结合模板方法进行拓展
 */
public abstract class BytecodeGenerator implements IBytecodeGenerator{

    public byte[] generate(Map<Integer,String> map){
        //flag=0 不作任何计算处理
        ClassWriter cw = new ClassWriter(0);
        //1. 【生命周期】写入类信息
        //类信息
        cwVisit(cw);
        //类的注解【可拓展】
        cwAnnotationVisit(cw);
        // @ColumnName、@ExcelProperty 注解处理
        int index = 0;
        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            //field固定解析注解，以及拓展额外注解【可拓展】
            fieldVisit(cw,entry,index);
            //getter/setter 固定写法，无序拓展
            methodVisit(cw,entry,index);
            index++;
        }
        //无参构造器
        noArgumentsConstructorVisit(cw);
        //全部访问结束
        cw.visitEnd();
        //结束
        return cw.toByteArray();
    }

    //可拓展
    protected void fieldVisitInner1(Map.Entry<Integer, String> entry, int index, FieldVisitor fieldVisitor){}

    //可拓展
    protected void cwAnnotationVisit(ClassWriter cw){}

    //必须重写给到类名信息
    protected abstract String getName();

    //----------以下为默认实现----------

    private void noArgumentsConstructorVisit(ClassWriter cw) {
        MethodVisitor methodVisitor = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        methodVisitor.visitCode();
        Label label0 = new Label();
        methodVisitor.visitLabel(label0);
        methodVisitor.visitLineNumber(8, label0);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
        methodVisitor.visitInsn(RETURN);
        Label label1 = new Label();
        methodVisitor.visitLabel(label1);
        methodVisitor.visitLocalVariable("this", "L"+getName()+";", null, label0, label1, 0);
        methodVisitor.visitMaxs(1, 1);
        methodVisitor.visitEnd();
    }

    private void cwVisit(ClassWriter cw){
        cw.visit(V1_6
                , ACC_PUBLIC | ACC_SUPER
                , getName()
                , null
                , "java/lang/Object"
                , null);
    }

    private void fieldVisit(ClassWriter cw,Map.Entry<Integer, String> entry,int index) {
        FieldVisitor fieldVisitor = cw.visitField(ACC_PUBLIC
                ,"s"+entry.getKey()
                ,"Ljava/lang/String;"
                ,null
                ,null);
        //固定实现，处理 @ColumnName注解，与 @ExcelProperty注解
        fieldVisitInner(entry, index, fieldVisitor);
        //模板方法，拓展实现
        fieldVisitInner1(entry,index,fieldVisitor);
        fieldVisitor.visitEnd();
    }

    private void fieldVisitInner(Map.Entry<Integer, String> entry, int index, FieldVisitor fieldVisitor) {
        //注解1：ColumnName
        AnnotationVisitor annotationVisitor = fieldVisitor.visitAnnotation("Ltemplate/annotations/common/ColumnName;", true);
        annotationVisitor.visit("name","col"+entry.getKey());
        annotationVisitor.visitEnd();
        //注解2：ExcelProperty
        AnnotationVisitor annotationVisitor1 = fieldVisitor.visitAnnotation("Lcom/alibaba/excel/annotation/ExcelProperty;", true);
        AnnotationVisitor annotationVisitor2 = annotationVisitor1.visitArray("value");
        String propertyName = entry.getValue();
        if (propertyName==null){
            propertyName="";
        }
        annotationVisitor2.visit("value",propertyName);
        annotationVisitor2.visitEnd();
        annotationVisitor1.visit("index",index);
        annotationVisitor1.visitEnd();
    }

    private void methodVisit(ClassWriter cw, Map.Entry<Integer, String> entry, int index) {
        MethodVisitor methodVisitor = cw.visitMethod(ACC_PUBLIC, "getS" + entry.getKey(), "()Ljava/lang/String;", null, null);
        methodVisitor.visitCode();
        //指令：操作码，操作数。内容需要放到操作数栈中才可以被指令使用
        //先把this引用压入操作数栈
        methodVisitor.visitVarInsn(ALOAD,0);
        //在通过例如 this.s1 （操作数出栈拿到this）的方式拿到这个字段，将其值压入操作数栈
        methodVisitor.visitFieldInsn(GETFIELD,getName(),"s"+entry.getKey(),"Ljava/lang/String;");
        //操作数栈中这个东西是一个引用类型，所以使用ARETURN，A表示引用类型，类似的IRETURN表示int类型返回值，直接RETURN表示void类型返回值
        methodVisitor.visitInsn(ARETURN);
        //局部变量表 就1个（this）
        methodVisitor.visitMaxs(1,1);
        //方法结束，这个是ASM的访问者需要的结束标识，和字节码没什么关系
        methodVisitor.visitEnd();

        methodVisitor = cw.visitMethod(ACC_PUBLIC,"setS"+entry.getKey(),"(Ljava/lang/String;)V",null,null);
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(ALOAD,0);
        methodVisitor.visitVarInsn(ALOAD,1);
        methodVisitor.visitFieldInsn(PUTFIELD,getName(),"s"+entry.getKey(),"Ljava/lang/String;");
        //void返回值
        methodVisitor.visitInsn(RETURN);
        //maxStack, maxLocals
        methodVisitor.visitMaxs(2,2);
        methodVisitor.visitEnd();
    }


}
