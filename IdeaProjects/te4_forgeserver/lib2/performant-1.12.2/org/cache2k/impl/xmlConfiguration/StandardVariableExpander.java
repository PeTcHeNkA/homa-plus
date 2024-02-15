//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.impl.xmlConfiguration;

import java.util.*;

public class StandardVariableExpander implements VariableExpander
{
    private Map<String, ValueAccessor> scope2resolver;
    private static final String PROPERTIES = "PROPERTIES";
    
    public StandardVariableExpander() {
        (this.scope2resolver = new HashMap<String, ValueAccessor>()).put("env", new ValueAccessor() {
            @Override
            public String get(final ExpanderContext ctx, final String _variable) {
                return System.getenv(_variable);
            }
        });
        this.scope2resolver.put("sys", new ValueAccessor() {
            @Override
            public String get(final ExpanderContext ctx, final String _variable) {
                return System.getProperty(_variable);
            }
        });
        this.scope2resolver.put("top", new ValueAccessor() {
            @Override
            public String get(final ExpanderContext ctx, final String _variable) {
                final ConfigurationTokenizer.Property p = ctx.getTopLevelConfiguration().getPropertyByPath(_variable);
                return StandardVariableExpander.this.checkAndReturnValue(p);
            }
        });
        this.scope2resolver.put("", new ValueAccessor() {
            @Override
            public String get(final ExpanderContext ctx, final String _variable) {
                final ConfigurationTokenizer.Property p = ctx.getCurrentConfiguration().getPropertyByPath(_variable);
                return StandardVariableExpander.this.checkAndReturnValue(p);
            }
        });
        this.scope2resolver.put("PROPERTIES", new ValueAccessor() {
            @Override
            public String get(final ExpanderContext ctx, final String _variable) {
                final ParsedConfiguration pc = ctx.getTopLevelConfiguration().getSection("properties");
                if (pc == null) {
                    return null;
                }
                final ConfigurationTokenizer.Property p = pc.getPropertyMap().get(_variable);
                return StandardVariableExpander.this.checkAndReturnValue(p);
            }
        });
    }
    
    @Override
    public void expand(final ParsedConfiguration cfg) {
        new Process(cfg, new HashMap<String, ValueAccessor>(this.scope2resolver)).expand();
    }
    
    private String checkAndReturnValue(final ConfigurationTokenizer.Property p) {
        if (p == null) {
            return null;
        }
        if (!p.isExpanded()) {
            throw new NeedsExpansion();
        }
        return p.getValue();
    }
    
    private static class Process implements ExpanderContext
    {
        private final Map<String, ValueAccessor> scope2resolver;
        private final ParsedConfiguration top;
        private ParsedConfiguration current;
        private int forwardReference;
        private ConfigurationTokenizer.Property lastTroublemaker;
        
        Process(final ParsedConfiguration _top, final Map<String, ValueAccessor> _scope2resolver) {
            this.forwardReference = 0;
            this.top = _top;
            this.scope2resolver = _scope2resolver;
        }
        
        private void expand() {
            do {
                final int lastCounter = this.forwardReference;
                this.forwardReference = 0;
                this.recurse(this.top);
                if (lastCounter > 0 && lastCounter == this.forwardReference) {
                    throw new ConfigurationException("Cyclic reference", (SourceLocation)this.lastTroublemaker);
                }
            } while (this.forwardReference > 0);
        }
        
        private void recurse(final ParsedConfiguration cfg) {
            this.current = cfg;
            for (final ConfigurationTokenizer.Property p : cfg.getPropertyMap().values()) {
                if (p.isExpanded()) {
                    continue;
                }
                final String v0 = p.getValue();
                try {
                    final String v2 = this.expand(v0);
                    if (v0 != v2) {
                        p.setValue(v2);
                    }
                    p.setExpanded(true);
                }
                catch (NeedsExpansion ex) {
                    ++this.forwardReference;
                    this.lastTroublemaker = p;
                }
            }
            for (final ParsedConfiguration c2 : cfg.getSections()) {
                final String _context = c2.getPropertyContext();
                ValueAccessor _savedAccessor = null;
                if (_context != null) {
                    _savedAccessor = this.scope2resolver.get(_context);
                    final ParsedConfiguration _localScope = c2;
                    this.scope2resolver.put(_context, new ValueAccessor() {
                        @Override
                        public String get(final ExpanderContext ctx, final String _variable) {
                            return _localScope.getStringPropertyByPath(_variable);
                        }
                    });
                }
                this.recurse(c2);
                if (_savedAccessor != null) {
                    this.scope2resolver.put(_context, _savedAccessor);
                }
            }
        }
        
        @Override
        public ParsedConfiguration getCurrentConfiguration() {
            return this.current;
        }
        
        @Override
        public ParsedConfiguration getTopLevelConfiguration() {
            return this.top;
        }
        
        private String expand(String s) {
            int idx = 0;
            while (true) {
                idx = s.indexOf("${", idx);
                if (idx < 0) {
                    return s;
                }
                int _endIdx = s.indexOf(125, idx);
                if (_endIdx < 0) {
                    return s;
                }
                int _scopeIdx = s.indexOf(46, idx);
                String _scope;
                if (_scopeIdx >= 0) {
                    _scope = s.substring(idx + 2, _scopeIdx);
                }
                else {
                    _scope = "PROPERTIES";
                    _scopeIdx = idx + 1;
                }
                final int _defaultIdx = s.indexOf(":-", idx);
                int _varEndIdx = _endIdx;
                String _defaultValue = null;
                if (_defaultIdx >= 0 && _defaultIdx < _endIdx) {
                    _defaultValue = s.substring(_defaultIdx + 2, _endIdx);
                    _varEndIdx = _defaultIdx;
                }
                final String _varName = s.substring(_scopeIdx + 1, _varEndIdx);
                final ValueAccessor r = this.scope2resolver.get(_scope);
                Label_0216: {
                    if (r != null) {
                        String _substitutionString = r.get(this, _varName);
                        if (_substitutionString == null) {
                            if (_defaultValue == null) {
                                break Label_0216;
                            }
                            _substitutionString = _defaultValue;
                        }
                        s = s.substring(0, idx) + _substitutionString + s.substring(_endIdx + 1);
                        _endIdx = idx + _substitutionString.length();
                    }
                }
                idx = _endIdx;
            }
        }
    }
}
