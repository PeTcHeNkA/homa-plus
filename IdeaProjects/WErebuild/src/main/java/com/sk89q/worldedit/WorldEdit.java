package com.sk89q.worldedit;

import com.sk89q.worldedit.masks.Mask;
import com.sk89q.worldedit.patterns.Pattern;
import com.sk89q.worldedit.util.eventbus.*;
import com.sk89q.worldedit.extension.factory.*;
import com.sk89q.worldedit.entity.*;
import com.sk89q.worldedit.session.*;
import com.sk89q.worldedit.extension.input.*;
import com.sk89q.worldedit.function.pattern.*;
import com.sk89q.worldedit.function.mask.*;
import com.sk89q.worldedit.util.io.file.*;
import com.sk89q.worldedit.blocks.*;
import com.sk89q.worldedit.extent.inventory.*;
import com.sk89q.worldedit.event.platform.*;
import com.sk89q.worldedit.session.request.*;
import com.sk89q.worldedit.extension.platform.*;
import java.util.*;
import java.util.logging.*;
import java.io.*;
import com.sk89q.worldedit.scripting.*;
import com.google.common.base.*;
import com.sk89q.worldedit.util.logging.*;
import com.sk89q.worldedit.world.registry.*;


public class WorldEdit {
    public static final Logger logger;
    private static final WorldEdit instance;
    private static String version;
    private final EventBus eventBus;
    private final PlatformManager platformManager;
    private final EditSessionFactory editSessionFactory;
    private final SessionManager sessions;
    private final BlockFactory blockFactory;
    private final ItemFactory itemFactory;
    private final MaskFactory maskFactory;
    private final PatternFactory patternFactory;

    private WorldEdit() {
        this.eventBus = new EventBus();
        this.platformManager = new PlatformManager(this);
        this.editSessionFactory = (EditSessionFactory)new EditSessionFactory.EditSessionFactoryImpl(this.eventBus);
        this.sessions = new SessionManager(this);
        this.blockFactory = new BlockFactory(this);
        this.itemFactory = new ItemFactory(this);
        this.maskFactory = new MaskFactory(this);
        this.patternFactory = new PatternFactory(this);
    }

    public static WorldEdit getInstance() {
        return WorldEdit.instance;
    }

    public PlatformManager getPlatformManager() {
        return this.platformManager;
    }

    public EventBus getEventBus() {
        return this.eventBus;
    }

    public BlockFactory getBlockFactory() {
        return this.blockFactory;
    }

    public ItemFactory getItemFactory() {
        return this.itemFactory;
    }

    public MaskFactory getMaskFactory() {
        return this.maskFactory;
    }

    public PatternFactory getPatternFactory() {
        return this.patternFactory;
    }

    public SessionManager getSessionManager() {
        return this.sessions;
    }

    @Deprecated
    public LocalSession getSession(final String player) {
        return this.sessions.findByName(player);
    }

    @Deprecated
    public LocalSession getSession(final Player player) {
        return this.sessions.get((SessionOwner)player);
    }

    @Deprecated
    public void removeSession(final Player player) {
        this.sessions.remove((SessionOwner)player);
    }

    @Deprecated
    public void clearSessions() {
        this.sessions.clear();
    }

    @Deprecated
    public boolean hasSession(final Player player) {
        return this.sessions.contains((SessionOwner)player);
    }

    @Deprecated
    public BaseBlock getBlock(final Player player, final String arg, final boolean allAllowed) throws WorldEditException {
        return this.getBlock(player, arg, allAllowed, false);
    }

    @Deprecated
    public BaseBlock getBlock(final Player player, final String arg, final boolean allAllowed, final boolean allowNoData) throws WorldEditException {
        final ParserContext context = new ParserContext();
        context.setActor((Actor)player);
        context.setWorld(player.getWorld());
        context.setSession(this.getSession(player));
        context.setRestricted(!allAllowed);
        context.setPreferringWildcard(allowNoData);
        return (BaseBlock)this.getBlockFactory().parseFromInput(arg, context);
    }

    @Deprecated
    public BaseBlock getBlock(final Player player, final String id) throws WorldEditException {
        return this.getBlock(player, id, false);
    }

    @Deprecated
    public Set<BaseBlock> getBlocks(final Player player, final String list, final boolean allAllowed, final boolean allowNoData) throws WorldEditException {
        final String[] items = list.split(",");
        final Set<BaseBlock> blocks = new HashSet<BaseBlock>();
        for (final String id : items) {
            blocks.add(this.getBlock(player, id, allAllowed, allowNoData));
        }
        return blocks;
    }

    @Deprecated
    public Set<BaseBlock> getBlocks(final Player player, final String list, final boolean allAllowed) throws WorldEditException {
        return this.getBlocks(player, list, allAllowed, false);
    }

    @Deprecated
    public Set<BaseBlock> getBlocks(final Player player, final String list) throws WorldEditException {
        return this.getBlocks(player, list, false);
    }

    @Deprecated
    public Set<Integer> getBlockIDs(final Player player, final String list, final boolean allBlocksAllowed) throws WorldEditException {
        final String[] items = list.split(",");
        final Set<Integer> blocks = new HashSet<Integer>();
        for (final String s : items) {
            blocks.add(this.getBlock(player, s, allBlocksAllowed).getType());
        }
        return blocks;
    }

    @Deprecated
    public Pattern getBlockPattern(final Player player, final String input) throws WorldEditException {
        final ParserContext context = new ParserContext();
        context.setActor((Actor)player);
        context.setWorld(player.getWorld());
        context.setSession(this.getSession(player));
        return Patterns.wrap(this.getPatternFactory().parseFromInput(input, context));
    }

    @Deprecated
    public Mask getBlockMask(final Player player, final LocalSession session, final String input) throws WorldEditException {
        final ParserContext context = new ParserContext();
        context.setActor(player);
        context.setWorld(player.getWorld());
        context.setSession(session);
        return Masks.wrap(this.getMaskFactory().parseFromInput(input, context));
    }

    public File getSafeSaveFile(final Player player, final File dir, final String filename, final String defaultExt, final String... extensions) throws FilenameException {
        return this.getSafeFile(player, dir, filename, defaultExt, extensions, true);
    }

    public File getSafeOpenFile(final Player player, final File dir, final String filename, final String defaultExt, final String... extensions) throws FilenameException {
        return this.getSafeFile(player, dir, filename, defaultExt, extensions, false);
    }

    private File getSafeFile(final Player player, final File dir, String filename, final String defaultExt, String[] extensions, final boolean isSave) throws FilenameException {
        if (extensions != null && extensions.length == 1 && extensions[0] == null) {
            extensions = null;
        }
        File f;
        if (filename.equals("#")) {
            if (isSave) {
                f = player.openFileSaveDialog(extensions);
            }
            else {
                f = player.openFileOpenDialog(extensions);
            }
            if (f == null) {
                throw new FileSelectionAbortedException("No file selected");
            }
        }
        else {
            if (defaultExt != null && filename.lastIndexOf(46) == -1) {
                filename = filename + "." + defaultExt;
            }
            if (!filename.matches("^[A-Za-z0-9_\\- \\./\\\\'\\$@~!%\\^\\*\\(\\)\\[\\]\\+\\{\\},\\?]+\\.[A-Za-z0-9]+$")) {
                throw new InvalidFilenameException(filename, "Invalid characters or extension missing");
            }
            f = new File(dir, filename);
        }
        try {
            final String filePath = f.getCanonicalPath();
            final String dirPath = dir.getCanonicalPath();
            if (!filePath.substring(0, dirPath.length()).equals(dirPath) && !this.getConfiguration().allowSymlinks) {
                throw new FilenameResolutionException(filename, "Path is outside allowable root");
            }
            return f;
        }
        catch (IOException e) {
            throw new FilenameResolutionException(filename, "Failed to resolve path");
        }
    }

    public void checkMaxRadius(final double radius) throws MaxRadiusException {
        if (this.getConfiguration().maxRadius > 0 && radius > this.getConfiguration().maxRadius) {
            throw new MaxRadiusException();
        }
    }

    public void checkMaxBrushRadius(final double radius) throws MaxBrushRadiusException {
        if (this.getConfiguration().maxBrushRadius > 0 && radius > this.getConfiguration().maxBrushRadius) {
            throw new MaxBrushRadiusException();
        }
    }

    public File getWorkingDirectoryFile(final String path) {
        final File f = new File(path);
        if (f.isAbsolute()) {
            return f;
        }
        return new File(this.getConfiguration().getWorkingDirectory(), path);
    }

    public Vector getDirection(final Player player, String dirStr) throws UnknownDirectionException {
        dirStr = dirStr.toLowerCase();
        final PlayerDirection dir = this.getPlayerDirection(player, dirStr);
        switch (dir) {
            case WEST:
            case EAST:
            case SOUTH:
            case NORTH:
            case UP:
            case DOWN: {
                return dir.vector();
            }
            default: {
                throw new UnknownDirectionException(dir.name());
            }
        }
    }

    private PlayerDirection getPlayerDirection(final Player player, final String dirStr) throws UnknownDirectionException {
        PlayerDirection dir = null;
        switch (dirStr.charAt(0)) {
            case 'w': {
                dir = PlayerDirection.WEST;
                break;
            }
            case 'e': {
                dir = PlayerDirection.EAST;
                break;
            }
            case 's': {
                if (dirStr.indexOf(119) > 0) {
                    return PlayerDirection.SOUTH_WEST;
                }
                if (dirStr.indexOf(101) > 0) {
                    return PlayerDirection.SOUTH_EAST;
                }
                dir = PlayerDirection.SOUTH;
                break;
            }
            case 'n': {
                if (dirStr.indexOf(119) > 0) {
                    return PlayerDirection.NORTH_WEST;
                }
                if (dirStr.indexOf(101) > 0) {
                    return PlayerDirection.NORTH_EAST;
                }
                dir = PlayerDirection.NORTH;
                break;
            }
            case 'u': {
                dir = PlayerDirection.UP;
                break;
            }
            case 'd': {
                dir = PlayerDirection.DOWN;
                break;
            }
            case 'f':
            case 'm': {
                dir = player.getCardinalDirection(0);
                break;
            }
            case 'b': {
                dir = player.getCardinalDirection(180);
                break;
            }
            case 'l': {
                dir = player.getCardinalDirection(-90);
                break;
            }
            case 'r': {
                dir = player.getCardinalDirection(90);
                break;
            }
            default: {
                throw new UnknownDirectionException(dirStr);
            }
        }
        return dir;
    }

    public Vector getDiagonalDirection(final Player player, final String dirStr) throws UnknownDirectionException {
        return this.getPlayerDirection(player, dirStr.toLowerCase()).vector();
    }

    public CuboidClipboard.FlipDirection getFlipDirection(final Player player, final String dirStr) throws UnknownDirectionException {
        final PlayerDirection dir = this.getPlayerDirection(player, dirStr);
        switch (dir) {
            case WEST:
            case EAST: {
                return CuboidClipboard.FlipDirection.WEST_EAST;
            }
            case SOUTH:
            case NORTH: {
                return CuboidClipboard.FlipDirection.NORTH_SOUTH;
            }
            case UP:
            case DOWN: {
                return CuboidClipboard.FlipDirection.UP_DOWN;
            }
            default: {
                throw new UnknownDirectionException(dir.name());
            }
        }
    }

    public void flushBlockBag(final Actor actor, final EditSession editSession) {
        final BlockBag blockBag = editSession.getBlockBag();
        if (blockBag != null) {
            blockBag.flushChanges();
        }
        final Map<Integer, Integer> missingBlocks = (Map<Integer, Integer>)editSession.popMissingBlocks();
        if (!missingBlocks.isEmpty()) {
            final StringBuilder str = new StringBuilder();
            str.append("Missing these blocks: ");
            final int size = missingBlocks.size();
            int i = 0;
            for (final Integer id : missingBlocks.keySet()) {
                final BlockType type = BlockType.fromID((int)id);
                str.append((type != null) ? (type.getName() + " (" + id + ")") : id.toString());
                str.append(" [Amt: ").append(missingBlocks.get(id)).append("]");
                if (++i != size) {
                    str.append(", ");
                }
            }
            actor.printError(str.toString());
        }
    }

    public boolean handleArmSwing(final Player player) {
        final PlayerInputEvent event = new PlayerInputEvent(player, InputType.PRIMARY);
        this.getEventBus().post((Object)event);
        return event.isCancelled();
    }

    public boolean handleRightClick(final Player player) {
        final PlayerInputEvent event = new PlayerInputEvent(player, InputType.SECONDARY);
        this.getEventBus().post((Object)event);
        return event.isCancelled();
    }

    public boolean handleBlockRightClick(final Player player, final WorldVector clicked) {
        final BlockInteractEvent event = new BlockInteractEvent((Actor)player, clicked.toLocation(), Interaction.OPEN);
        this.getEventBus().post((Object)event);
        return event.isCancelled();
    }

    public boolean handleBlockLeftClick(final Player player, final WorldVector clicked) {
        final BlockInteractEvent event = new BlockInteractEvent((Actor)player, clicked.toLocation(), Interaction.HIT);
        this.getEventBus().post((Object)event);
        return event.isCancelled();
    }

    public void runScript(final Player player, final File f, final String[] args) throws WorldEditException {
        Request.reset();
        final String filename = f.getPath();
        final int index = filename.lastIndexOf(".");
        final String ext = filename.substring(index + 1, filename.length());
        if (!ext.equalsIgnoreCase("js")) {
            player.printError("Only .js scripts are currently supported");
            return;
        }
        String script;
        try {
            InputStream file;
            if (!f.exists()) {
                file = WorldEdit.class.getResourceAsStream("craftscripts/" + filename);
                if (file == null) {
                    player.printError("Script does not exist: " + filename);
                    return;
                }
            }
            else {
                file = new FileInputStream(f);
            }
            final DataInputStream in = new DataInputStream(file);
            final byte[] data = new byte[in.available()];
            in.readFully(data);
            in.close();
            script = new String(data, 0, data.length, "utf-8");
        }
        catch (IOException e) {
            player.printError("Script read error: " + e.getMessage());
            return;
        }
        final LocalSession session = this.getSessionManager().get((SessionOwner)player);
        final CraftScriptContext scriptContext = new CraftScriptContext(this, (Platform)this.getServer(), this.getConfiguration(), session, player, args);
        CraftScriptEngine engine = null;
        try {
            engine = new RhinoCraftScriptEngine();
        }
        catch (NoClassDefFoundError e6) {
            player.printError("Failed to find an installed script engine.");
            player.printError("Please see http://wiki.sk89q.com/wiki/WorldEdit/Installation");
            return;
        }
        engine.setTimeLimit(this.getConfiguration().scriptTimeout);
        final Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("argv", args);
        vars.put("context", scriptContext);
        vars.put("player", player);
        try {
            engine.evaluate(script, filename, vars);
        } catch (NumberFormatException | WorldEditException e3) {
            throw e3;
        } catch (Throwable e5) {
            player.printError("Failed to execute (see console):");
            player.printRaw(e5.getClass().getCanonicalName());
            WorldEdit.logger.log(Level.WARNING, "Failed to execute script", e5);
        }
        finally {
            for (final EditSession editSession : scriptContext.getEditSessions()) {
                editSession.flushQueue();
                session.remember(editSession);
            }
        }
    }


    public LocalConfiguration getConfiguration() {
        return this.getPlatformManager().getConfiguration();
    }


    public ServerInterface getServer() {
        return this.getPlatformManager().getServerInterface();
    }


    public EditSessionFactory getEditSessionFactory() {
        return this.editSessionFactory;
    }


    @Deprecated
    public void setEditSessionFactory(final EditSessionFactory factory) {
        Preconditions.checkNotNull((Object)factory);
        WorldEdit.logger.severe("Got request to set EditSessionFactory of type " + factory.getClass().getName() + " from " + factory.getClass().getPackage().getName() + " but EditSessionFactories have been removed in favor of extending EditSession's extents.\n\nThis may mean that any block logger / intercepters addons/plugins/mods that you have installed will not intercept WorldEdit's changes! Please notify the maintainer of the other addon about this.");
    }


    public static String getVersion() {
        if (WorldEdit.version != null) {
            return WorldEdit.version;
        }
        Package p = WorldEdit.class.getPackage();
        if (p == null) {
            p = Package.getPackage("com.sk89q.worldedit");
        }
        if (p == null) {
            WorldEdit.version = "(unknown)";
        }
        else {
            WorldEdit.version = p.getImplementationVersion();
            if (WorldEdit.version == null) {
                WorldEdit.version = "(unknown)";
            }
        }
        return WorldEdit.version;
    }


    @Deprecated
    public static void setVersion(final String version) {
    }


    static {
        logger = Logger.getLogger(WorldEdit.class.getCanonicalName());
        instance = new WorldEdit();
        WorldEditPrefixHandler.register("com.sk89q.worldedit");
        getVersion();
        BundledBlockData.getInstance();
    }
}