package utils.bytecode;

import java.util.Map;

/**
 * 可拓展性，未来可以根据此接口，替换为其他字节码生成器
 */
public interface IBytecodeGenerator {
    byte[] generate(Map<Integer,String> map);
}
