package in.yagnyam.myid;

import com.google.api.server.spi.response.BadRequestException;

import java.util.regex.Pattern;

import in.yagnyam.myid.model.EntityNode;
import in.yagnyam.myid.model.GenesisNode;
import in.yagnyam.myid.model.RootNode;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NodeFactory {

    private static final Pattern twoLevelPath = Pattern.compile("/\\p{Alnum}+/\\p{Alnum}+");
    private static final Pattern threeLevelPath = Pattern.compile("/\\p{Alnum}+/\\p{Alnum}+/\\p{Alnum}+");
    private static final Pattern fourLevelPath = Pattern.compile("/\\p{Alnum}+/\\p{Alnum}+/\\p{Alnum}+/\\p{Alnum}+");

    public static EntityNode fromJson(@NonNull BlockChainNode blockChainNode) throws BadRequestException {
        String nodeType = getNodeType(blockChainNode.getPath());
        EntityNode node;
        if (RootNode.NODE_TYPE_VALUE.equals(nodeType)) {
            node = new RootNode();
        } else if (EntityNode.NODE_TYPE_VALUE.equals(nodeType)) {
            node = new EntityNode();
        } else if (GenesisNode.NODE_TYPE_VALUE.equals(nodeType)) {
            node = new GenesisNode();
        } else {
            log.info("Invalid Node Type: {}", nodeType);
            throw new BadRequestException("Invalid Node Type: " + nodeType);
        }
        node.populateFrom(blockChainNode);
        return node;
    }

    private static String getNodeType(@NonNull String nodePath) throws BadRequestException {
        if (nodePath.equals(GenesisNode.GENESIS_NODE_PATH)) {
            return GenesisNode.NODE_TYPE_VALUE;
        } else if (nodePath.startsWith("/root/")) {
            return RootNode.NODE_TYPE_VALUE;
        } else if (twoLevelPath.matcher(nodePath).matches()
                || threeLevelPath.matcher(nodePath).matches()
                || fourLevelPath.matcher(nodePath).matches()){
            return EntityNode.NODE_TYPE_VALUE;
        } else {
            throw new BadRequestException("Invalid Path: " + nodePath);
        }
    }

}
