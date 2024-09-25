package plus.fserver.plugin;

import net.minecraft.launchwrapper.*;
import net.minecraftforge.fml.relauncher.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import plus.fserver.ElcaConfig;
import java.util.*;


public class PathTransformer implements IClassTransformer {
    static final HashMap<String, InjectInfo[]> injects = new HashMap<>();
    static String SIDE;
    static final String SIDE_ONLY_DESC = "Lnet/minecraftforge/fml/relauncher/SideOnly;";//Type.getDescriptor(SideOnly.class);
    static final String VAR_ONLY_DESC  = "Lplus/fserver/api/inject/VarOnly;"           ;//Type.getDescriptor(VarOnly.class);
    static final String LOAD_INJECT    = "Lplus/fserver/api/inject/LoadInjector;"      ;//Type.getDescriptor(LoadInjector.class);

    public PathTransformer(){}


    public static byte[] transformOnlyInjects(final String name, final byte[] bytes){
        final ClassNode classNode = new ClassNode();
        final ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        checkInjects(classNode.visibleAnnotations);

        InjectInfo[] field = injects.get(name);
        if(field != null){
            for (InjectInfo cur:field){
                injectField(classNode, cur);
            }
        } else {
            return bytes;
        }
        final ClassWriter writer = new ClassWriter(1);
        classNode.accept(writer);
        return writer.toByteArray();
    }


    @Override
    public byte[] transform(final String name, final String transformedName, final byte[] bytes) {
        if (bytes == null) return null;

        final ClassNode classNode = new ClassNode();
        final ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        if (this.remove(classNode.visibleAnnotations, SIDE)) {
            throw new RuntimeException(String.format("Attempted to load class %s for invalid side %s", classNode.name, SIDE));
        }
        checkInjects(classNode.visibleAnnotations);

        final Iterator<FieldNode> fields = classNode.fields.iterator();
        while (fields.hasNext()) {
            final FieldNode field = fields.next();
            if (this.remove(field.visibleAnnotations, SIDE)) {
                fields.remove();
            }
        }
        LambdaGatherer lambdaGatherer = new LambdaGatherer();
        Iterator<MethodNode> methods = classNode.methods.iterator();
        while (methods.hasNext()) {
            final MethodNode method = methods.next();
            if (this.remove(method.visibleAnnotations, SIDE)) {
                methods.remove();
                lambdaGatherer.accept(method);
            }
        }
        for (ArrayList<Handle> handles = lambdaGatherer.dynamicLambdaHandles;
                !handles.isEmpty(); handles = lambdaGatherer.dynamicLambdaHandles) {
            lambdaGatherer = new LambdaGatherer();
            methods = classNode.methods.iterator();
            while (methods.hasNext()) {
                final MethodNode method2 = methods.next();
                if ((method2.access & 0x1000) == 0x0) {
                    continue;
                }
                for (final Handle dynamicLambdaHandle : handles) {
                    if (method2.name.equals(dynamicLambdaHandle.getName()) && method2.desc.equals(dynamicLambdaHandle.getDesc())) {
                        methods.remove();
                        lambdaGatherer.accept(method2);
                    }
                }
            }
        }
        return completeTransforms(classNode, transformedName);
    }


    static byte[] completeTransforms(ClassNode node, String transformedName){
        InjectInfo[] field = injects.get(transformedName);
        if(field != null){
            for (InjectInfo cur:field){
                injectField(node, cur);
            }
        }

        final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }


    private static void injectField(ClassNode classNode, InjectInfo field){
        Class<?> type = field.type;
        String typeName;
        if(type == Object.class){
            typeName = "L"+field.tName.replace(".","/")+";";
        } else {
            typeName = "L"+type.getName().replace(".","/")+";";
        }
        classNode.visitField(0x1, field.name, typeName, null, null);
    }


    static InjectInfo build(List values){
        String target = null, name = null, tName = null;
        Class<?> type = null;

        for (int i = 0; i < values.size() - 1; i += 2) {
            String key = (String) values.get(i);
            Object value = values.get(i+1);
            switch (key){
                case "target":
                    target = (String) value;
                    break;
                case "name":
                    name = (String) value;
                    break;
                case "tName":
                    tName = (String) value;
                    break;
                case "type":
                    type = (Class<?>) value;
                    break;
            }
        }
        return new InjectInfo(target, name, type, tName);
    }


    public static void checkInjects(final List<AnnotationNode> anns){
        if (anns == null) return;
        for (final AnnotationNode ann : anns) {
            List<Object> values;
            if((values = ann.values) == null)continue;
            if (ann.desc.equals(LOAD_INJECT)) {
                String key = (String) values.get(0);
                if(key.equals("field")){
                    InjectInfo field = build((List) values.get(1));
                    checkAndAdd(field);
                } else if(key.equals("fields")){
                    List list = (List) values.get(1);
                    for (Object obj: list){
                        InjectInfo field = build(((AnnotationNode) obj).values);
                        checkAndAdd(field);
                    }
                }
            }
        }
    }


    private static void checkAndAdd(InjectInfo field){
        if(field.name.isEmpty()){
            if(field.target.isEmpty())return;
            throw new RuntimeException("Field Injector: Field`s name not defined!");
        }
        if(field.target.isEmpty()){
            throw new RuntimeException("Field Injector: Target`s name not defined!");
        }
        InjectInfo[] fields = injects.get(field.target);
        if(fields != null){
            InjectInfo[] newFields = new InjectInfo[fields.length+1];
            System.arraycopy(fields, 0, newFields, 0, fields.length);
            newFields[fields.length] = field;
            fields = newFields;
        } else {
            fields = new InjectInfo[]{field};
        }
        injects.put(field.target, fields);
    }


    static boolean checkVarOnly(AnnotationNode ann){
        if(ann.desc.equals(VAR_ONLY_DESC)){
            List<Object> values = ann.values;
            String var = null;
            Boolean defValue = null;
            for (int x = 0; x < values.size() - 1; x += 2) {
                final Object key = values.get(x);
                final Object value = values.get(x + 1);
                if(var == null && key.equals("var")){
                    var = key.toString();
                    if(defValue != null)break;
                }
                if(defValue == null && key.equals("def")){
                    if(value instanceof Boolean){
                        defValue = (Boolean) value;
                    } else {
                        defValue = false;
                    }
                    if(var != null)break;
                }
            }
            if(var != null){
                if(defValue == null)defValue = false;
                return ElcaConfig.getVar(var, defValue);
            }
        }
        return false;
    }


    private boolean remove(final List<AnnotationNode> anns, final String side) {
        if (anns == null) return false;

        for (final AnnotationNode ann : anns) {
            List<Object> values;
            if((values = ann.values) == null)continue;
            if (ann.desc.equals(SIDE_ONLY_DESC)) {
                for (int x = 0; x < values.size() - 1; x += 2) {
                    final Object key = values.get(x);
                    final Object value = values.get(x + 1);
                    if (key instanceof String && key.equals("value") && value instanceof String[] && !((String[])value)[1].equals(side)) {
                        return true;
                    }
                }
            } else if(checkVarOnly(ann))return true;
        }
        return false;
    }


    static {
        SIDE = FMLLaunchHandler.side().name();
    }


    private static class LambdaGatherer extends MethodVisitor {
        private static final Handle META_FACTORY;
        final ArrayList<Handle> dynamicLambdaHandles = new ArrayList<>();

        public LambdaGatherer() {
            super(327680);
        }

        public void accept(final MethodNode method) {
            ListIterator<AbstractInsnNode> iter = method.instructions.iterator();
            while (iter.hasNext()){
                AbstractInsnNode insnNode = iter.next();
                if(insnNode != null && insnNode.getType() == 6){
                    insnNode.accept(this);
                }
            }
        }


        @Override
        public void visitInvokeDynamicInsn(final String name, final String desc, final Handle bsm, final Object... bsmArgs) {
            if (LambdaGatherer.META_FACTORY.equals(bsm)) {
                final Handle dynamicLambdaHandle = (Handle)bsmArgs[1];
                this.dynamicLambdaHandles.add(dynamicLambdaHandle);
            }
        }


        static {
            META_FACTORY = new Handle(6, "java/lang/invoke/LambdaMetafactory", "metafactory", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;", false);
        }
    }


    public static class InjectInfo{
        final String target, name, tName;
        final Class<?> type;

        public InjectInfo(String target, String name, Class<?> type, String tName) {
            if (target == null) target = "";
            if (name == null) name = "";
            if (type == null) type = Object.class;
            if (tName == null) tName = "java.lang.Object";
            this.target = target;
            this.name = name;
            this.tName = tName;
            this.type = type;
        }
    }
}