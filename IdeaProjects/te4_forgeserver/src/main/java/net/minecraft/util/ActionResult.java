package net.minecraft.util;


public class ActionResult<T> {
    private EnumActionResult type;
    private Object result;

    public ActionResult(final EnumActionResult typeIn, final T resultIn) {
        this.type = typeIn;
        this.result = resultIn;
    }


    public <R> ActionResult<R> init(final EnumActionResult typeIn, final R resultIn){
        this.type = typeIn;
        this.result = resultIn;
        return (ActionResult<R>) this;
    }


    public EnumActionResult getType() {
        return this.type;
    }


    public T getResult() {
        return (T) this.result;
    }


    public static <T> ActionResult<T> newResult(final EnumActionResult result, final T obj) {
        return new ActionResult<T>(result, obj);
    }
}