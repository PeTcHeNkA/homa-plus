package plus.forge;

public class FFJPatchLoader extends TweakEngine{
    public static FFJPatchLoader INSTANCE;

    public FFJPatchLoader(){
        super("plus/forge/FFJPatchLoader.class");
        add("net.minecraft.world.DimensionType");
        INSTANCE = this;
    }


    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        basicClass = super.transform(name, transformedName, basicClass);
        //todo add self access-transformers

        return basicClass;
    }
}