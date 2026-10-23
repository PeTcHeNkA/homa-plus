package plus.inv.part;


import plus.inv.AInventoryClientRenderer;

public abstract class AbcPart {
    protected final AInventoryClientRenderer invrenderer;
    private boolean enabled = true;

    protected AbcPart(AInventoryClientRenderer invrenderer) {
        this.invrenderer = invrenderer;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public final void enable(){
        setEnabled(true);
    }
    public final void disable(){
        setEnabled(false);
    }
}