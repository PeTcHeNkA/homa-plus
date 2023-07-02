package plus.te4net.core.mapping;

import io.netty.util.collection.IntObjectHashMap;
import plus.te4net.core.api.IRequestProcessor;


class RouteNode extends IntObjectHashMap<RouteNode> {
    private final int hs;
    protected IRequestProcessor route;

    RouteNode(int hs) {
        this.hs = hs;
    }

    public int size() {
        int size = 0;
        for (RouteNode node : this.values()) {
            size += (node.size());
        }
        return route == null ? size : ++size;
    }

    @Override
    public int hashCode() {
        return hs;
    }
}
