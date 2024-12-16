package plus.fserver.utls;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import plus.fserver.api.ChatPacketBuilder;
import plus.fserver.api.ServerCommand;
import plus.fserver.api.placeholders.PlayerVarProvider;
import plus.tson.TsonFunc;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static plus.tson.ext.TsonNumUtils.calc;


public class TsonCommand extends ServerCommand {
    final CompletionNode rootCompletionNode;
    final EvalNode evalNode;

    public TsonCommand(TsonMap map) {
        super(
                map.getStr("name"),
                map.getOrDefaultStr("usage", "/"+map.getStr("name")+" <args>"),
                calc(map, "prem-lvl", 0)
        );

        if(map.containsKey("completions")){
            rootCompletionNode = new CompletionNode(map.getMap("completions"));
        } else {
            rootCompletionNode = null;
        }
        evalNode = new EvalNode(null, map);
    }


    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        EvalNode last = evalNode.getLast(args, 0);
        TsonFunc func = last.func;
        if(func == null){
            ChatPacketBuilder msg = last.getInvalidArgsMsg();
            if(msg == null){
                reply(sender, "&cInvalid args!");
            } else {
                if(sender instanceof EntityPlayerMP){
                    EntityPlayerMP player = (EntityPlayerMP) sender;
                    HashMap<String,String> model = new HashMap<>();

                    for (int i = 0; i < args.length; i++){
                        model.put("arg"+i, args[i]);
                    }
                    msg.send(PlayerVarProvider.model(player, model));
                }
            }
        } else {
            try {
                int cArgs = func.countArgs();
                if(cArgs == 1) {
                    func.call(sender);
                } else {
                    func.call(server, sender, args);
                }
            } catch (Throwable e){
                onError(sender, getName(), e);
            }
        }
    }


    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        CompletionNode rc = rootCompletionNode;
        if(rc != null)return rc.getCompletions(sender, args, 0);
        return ImmutableList.of();
    }


    private static class CompletionNode{
        final ImmutableList<String> completions;
        final ImmutableMap<String,CompletionNode> nodes;

        public CompletionNode(TsonMap map){
            List<String> completions = new ArrayList<>();
            Map<String,CompletionNode> nodes = new HashMap<>();

            map.forEach((s, obj) -> {
                if(obj.isString()){
                    completions.add(obj.getStr());
                } else if(obj.isMap()){
                    nodes.put(s, new CompletionNode(obj.getMap()));
                }
            });
            this.completions = ImmutableList.copyOf(completions);
            this.nodes = ImmutableMap.copyOf(nodes);
        }


        public List<String> getCompletions(ICommandSender sender, String[] args, int cursor){
            if(cursor >= args.length)return ImmutableList.of();
            CompletionNode node = nodes.get(args[cursor]);
            if(node == null)return completions;
            return node.getCompletions(sender, args, cursor+1);
        }
    }


    private static class EvalNode{
        ImmutableMap<String, EvalNode> nodes;
        TsonFunc func;
        ChatPacketBuilder invalidArgs;
        EvalNode parent;

        public EvalNode(EvalNode parent, TsonObj obj){
            this.parent = parent;
            Map<String, EvalNode> nodes = ImmutableMap.of();
            if(obj.isMap()){
                TsonMap map = obj.getMap();
                if(map.containsKey("func")){
                    this.func = map.getFunc("func");
                }
                if(map.containsKey("nodes")){
                    TsonMap map1 = map.getMap("nodes");
                    nodes = new HashMap<>();
                    Map<String, EvalNode> finalNodes = nodes;
                    map1.forEach((s, obj1) -> finalNodes.put(s, new EvalNode(this, obj1)));
                }
                if(map.containsKey("invalid-msg")){
                    invalidArgs = ChatPacketBuilder.of(map.get("invalid-msg"));
                }
            } else if(obj.isFunc()){
                this.func = (TsonFunc) obj.getField();
            }
            this.nodes = ImmutableMap.copyOf(nodes);
        }


        public EvalNode getLast(String[] args, int cursor){
            if(cursor >= args.length)return this;
            EvalNode node = nodes.get(args[cursor]);
            if(node == null)return this;
            return node.getLast(args, cursor+1);
        }


        public ChatPacketBuilder getInvalidArgsMsg(){
            if(this.invalidArgs == null){
                if(this.parent == null)return null;
                return parent.getInvalidArgsMsg();
            }
            return invalidArgs;
        }
    }
}