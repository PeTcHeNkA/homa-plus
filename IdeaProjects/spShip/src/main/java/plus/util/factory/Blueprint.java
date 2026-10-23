package plus.util.factory;

import plus.util.cfg.Sector;


public interface Blueprint<Result> {
    Result build(Sector c, String path);
}