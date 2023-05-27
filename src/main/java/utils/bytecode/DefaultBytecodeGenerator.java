package utils.bytecode;

public class DefaultBytecodeGenerator extends BytecodeGenerator {
    private String clzName = null;

    public DefaultBytecodeGenerator(){

    }


    public void setName(String clzName){
        this.clzName = clzName;
    }

    @Override
    protected String getName() {
        if (clzName==null){
            throw new RuntimeException("must set clzName before bytecode generating");
        }
        return clzName;
    }
}
