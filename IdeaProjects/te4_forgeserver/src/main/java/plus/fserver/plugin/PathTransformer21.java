package plus.fserver.plugin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import static plus.fserver.plugin.PathTransformer.*;


public class PathTransformer21 implements IClassTransformer {
    private static boolean useJ21 = false;
    private static int ASM_9 = -1;

    private static int getDefOpc(){
        if(ASM_9 == -1){
            try {
                ASM_9 = Opcodes.class.getDeclaredField("ASM9").getInt(null);
                useJ21 = true;
            } catch (NoSuchFieldException | IllegalAccessException e) {
                ASM_9 = 327680;
            }
        }
        return ASM_9;
    }


    public static boolean canUse(){
        if(ASM_9 == -1){
            getDefOpc();
        }
        return useJ21;
    }



    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (bytes == null)return null;

        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        if (remove(classNode.visibleAnnotations, SIDE) > 0) {
            throw new RuntimeException(String.format("Attempted to load class %s for invalid side %s", classNode.name, SIDE));
        }

        Iterator<FieldNode> fields = classNode.fields.iterator();
        while(fields.hasNext()) {
            FieldNode field = fields.next();
            if (remove(field.visibleAnnotations, SIDE) > 0) {
                fields.remove();
            }
        }

        LambdaGatherer lambdaGatherer = new LambdaGatherer();
        Iterator<MethodNode> methods = classNode.methods.iterator();
        while(methods.hasNext()) {
            final MethodNode method = methods.next();
            int res = this.remove(method.visibleAnnotations, SIDE);
            if (res > 0) {
                if(res == 1){
                    methods.remove();
                    lambdaGatherer.accept(method);
                } else {
                    removeCode(method);
                }
            }
        }

        // remove dynamic synthetic lambda methods that are inside of removed methods
        for (List<Handle> dynamicLambdaHandles = lambdaGatherer.dynamicLambdaHandles;
             !dynamicLambdaHandles.isEmpty(); dynamicLambdaHandles = lambdaGatherer.dynamicLambdaHandles) {
            lambdaGatherer = new LambdaGatherer();
            methods = classNode.methods.iterator();
            while (methods.hasNext()) {
                MethodNode method = methods.next();
                if ((method.access & Opcodes.ACC_SYNTHETIC) == 0) continue;
                for (Handle dynamicLambdaHandle : dynamicLambdaHandles) {
                    if (method.name.equals(dynamicLambdaHandle.getName()) && method.desc.equals(dynamicLambdaHandle.getDesc())) {
                        methods.remove();
                        lambdaGatherer.accept(method);
                    }
                }
            }
        }
        return completeTransforms(classNode, transformedName);
    }


    private int remove(List<AnnotationNode> anns, String side) {
        if (anns == null) return 0;

        for (AnnotationNode ann : anns) {
            if(ann.values == null)continue;
            if (ann.desc.equals(SIDE_ONLY_DESC)) {
                List<Object> values = ann.values;
                for (int x = 0; x < values.size() - 1; x += 2) {
                    Object key = values.get(x);
                    Object value = values.get(x+1);
                    if (key instanceof String && key.equals("value")) {
                        if (value instanceof String[] ) {
                            if (!((String[])value)[1].equals(side)) {
                                return 1;
                            }
                        }
                    }
                }
            } else {
                int res = checkVarOnly(ann);
                if(res > 0)return res;
            }
        }
        return 0;
    }


    private static class LambdaGatherer extends MethodVisitor {
        private static final Handle META_FACTORY = new Handle(Opcodes.H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory",
                "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",
                false);
        final ArrayList<Handle> dynamicLambdaHandles = new ArrayList<>();

        public LambdaGatherer() {
            super(getDefOpc());
        }

        public void accept(MethodNode method) {
            ListIterator<AbstractInsnNode> iter = method.instructions.iterator();
            while (iter.hasNext()) {
                AbstractInsnNode insnNode;
                if ((insnNode = iter.next()).getType() == AbstractInsnNode.INVOKE_DYNAMIC_INSN)
                    insnNode.accept(this);
            }
        }


        @Override
        public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
            if (!META_FACTORY.equals(bsm)) return;
            Handle dynamicLambdaHandle = (Handle) bsmArgs[1];
            dynamicLambdaHandles.add(dynamicLambdaHandle);
        }
    }
}