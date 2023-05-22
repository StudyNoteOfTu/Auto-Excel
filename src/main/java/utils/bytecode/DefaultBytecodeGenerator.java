package utils.bytecode;

public class DefaultBytecodeGenerator extends BytecodeGenerator {
    private String clzName;

    public DefaultBytecodeGenerator(String clzName) {
        this.clzName = clzName;
    }

    public void setClzName(String clzName){
        this.clzName = clzName;
    }

    @Override
    protected String getName() {
        return clzName;
    }
}
