//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.impl.xmlConfiguration;

public class ConfigurationParser
{
    static ParsedConfiguration parse(final ConfigurationTokenizer _parser) throws Exception {
        final ParsedConfiguration c = new ParsedConfiguration(_parser.getSource(), _parser.getLineNumber());
        parseTopLevelSections(_parser, c);
        final ConfigurationTokenizer.Item _item = _parser.next();
        return c;
    }
    
    private static void parseSection(final ConfigurationTokenizer _parser, final ParsedConfiguration _container) throws Exception {
        while (true) {
            final ConfigurationTokenizer.Item _item = _parser.next();
            if (_item == null) {
                throw new ConfigurationException("null item", (SourceLocation)_parser);
            }
            if (_item instanceof ConfigurationTokenizer.Unnest) {
                return;
            }
            if (_item instanceof ConfigurationTokenizer.Property) {
                _container.addProperty((ConfigurationTokenizer.Property)_item);
            }
            else {
                if (!(_item instanceof ConfigurationTokenizer.Nest)) {
                    continue;
                }
                parseSections(((ConfigurationTokenizer.Nest)_item).getSectionName(), _parser, _container);
            }
        }
    }
    
    private static void parseSections(final String _containerName, final ConfigurationTokenizer _parser, final ParsedConfiguration _container) throws Exception {
        boolean _maybeSection = true;
        while (true) {
            final ConfigurationTokenizer.Item _item = _parser.next();
            if (_item == null) {
                return;
            }
            if (_item instanceof ConfigurationTokenizer.Unnest) {
                return;
            }
            if (_item instanceof ConfigurationTokenizer.Property && _maybeSection) {
                final ParsedConfiguration _nestedContainer = new ParsedConfiguration(_parser.getSource(), _parser.getLineNumber());
                _nestedContainer.setName(_containerName);
                _nestedContainer.setPropertyContext(_containerName);
                _nestedContainer.setContainer("#DIRECT");
                _nestedContainer.addProperty((ConfigurationTokenizer.Property)_item);
                parseSection(_parser, _nestedContainer);
                _container.addSection(_nestedContainer);
                return;
            }
            if (!(_item instanceof ConfigurationTokenizer.Nest)) {
                throw new ConfigurationException("section start expected", (SourceLocation)_item);
            }
            final ConfigurationTokenizer.Nest _sectionStart = (ConfigurationTokenizer.Nest)_item;
            final ParsedConfiguration _nestedContainer2 = new ParsedConfiguration(_parser.getSource(), _parser.getLineNumber());
            _nestedContainer2.setName(_sectionStart.getSectionName());
            _nestedContainer2.setPropertyContext(_sectionStart.getSectionName());
            _nestedContainer2.setContainer(_containerName);
            parseSection(_parser, _nestedContainer2);
            _container.addSection(_nestedContainer2);
            _maybeSection = false;
        }
    }
    
    private static void parseTopLevelSections(final ConfigurationTokenizer _parser, final ParsedConfiguration _container) throws Exception {
        ConfigurationTokenizer.Item _item = _parser.next();
        if (!(_item instanceof ConfigurationTokenizer.Nest)) {
            throw new ConfigurationException("start expected", (SourceLocation)_item);
        }
        while (true) {
            _item = _parser.next();
            if (_item == null) {
                return;
            }
            if (_item instanceof ConfigurationTokenizer.Unnest) {
                return;
            }
            if (_item instanceof ConfigurationTokenizer.Property) {
                _container.addProperty((ConfigurationTokenizer.Property)_item);
            }
            else {
                if (!(_item instanceof ConfigurationTokenizer.Nest)) {
                    continue;
                }
                final ConfigurationTokenizer.Nest _sectionStart = (ConfigurationTokenizer.Nest)_item;
                ParsedConfiguration _nestedContainer = new ParsedConfiguration(_parser.getSource(), _parser.getLineNumber());
                _nestedContainer.setName(_sectionStart.getSectionName());
                parseSections(((ConfigurationTokenizer.Nest)_item).getSectionName(), _parser, _nestedContainer);
                if (_nestedContainer.getPropertyMap().isEmpty() && _nestedContainer.getSections().size() == 1 && _nestedContainer.getSections().get(0).getContainer().equals("#DIRECT")) {
                    _nestedContainer = _nestedContainer.getSections().get(0);
                }
                _container.addSection(_nestedContainer);
            }
        }
    }
}
